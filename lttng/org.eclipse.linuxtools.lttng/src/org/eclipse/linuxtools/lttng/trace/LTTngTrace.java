/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson, MontaVista Software
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *   Yufen Kuo       (ykuo@mvista.com) - add support to allow user specify trace library path
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.trace;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.linuxtools.lttng.TraceHelper;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventSource;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngLocation;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.exceptions.LttngException;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

class LTTngTraceException extends LttngException {
    static final long serialVersionUID = -1636648737081868146L;

    public LTTngTraceException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>LTTngTrace</u></b>
 * <p>
 * 
 * LTTng trace implementation. It accesses the C trace handling library (seeking, reading and parsing) through the JNI
 * component.
 */
public class LTTngTrace extends TmfTrace<LttngEvent> {

    public static boolean PrintDebug = false;
    public static boolean UniqueEvent = true;

    private final static boolean SHOW_LTT_DEBUG_DEFAULT = false;
    private final static boolean IS_PARSING_NEEDED_DEFAULT = !UniqueEvent;
    private final static int CHECKPOINT_PAGE_SIZE = 50000;

    // Reference to our JNI trace
    private JniTrace currentJniTrace = null;

    // ***
    // UNHACKED : We can no longer do that because TCF need to maintain several
    // events at once.
    // This is very slow to do so in LTTng, this has to be temporary.
    // *** HACK ***
    // To save time, we will declare all component of the LttngEvent during the
    // construction of the trace
    // Then, while reading the trace, we will just SET the values instead of
    // declaring new object
    // ***
    LttngTimestamp eventTimestamp = null;
    LttngEventSource eventSource = null;
    LttngEventContent eventContent = null;
    LttngEventReference eventReference = null;

    // The actual event
    LttngEvent currentLttngEvent = null;

    // The current location
    LttngLocation previousLocation = null;

    LttngEventType eventType = null;
    // Hashmap of the possible types of events (Tracefile/CPU/Marker in the JNI)
    HashMap<Integer, LttngEventType> traceTypes = null;
    // This vector will be used to quickly find a marker name from a position
    Vector<Integer> traceTypeNames = null;
    private String traceLibPath = null;

    public LTTngTrace() {
    }

    public boolean validate(IProject project, String path) {
        if (super.validate(project, path)) {
            String traceLibPath = TraceHelper.getTraceLibDirFromProject(project);
            try {
                LTTngTraceVersion version = new LTTngTraceVersion(path, traceLibPath);
                return version.isValidLttngTrace();
            } catch (LttngException e) {
            }
        }
        return false;
    }

    @Override
    public void initTrace(String path, Class<LttngEvent> eventType, boolean indexTrace) throws FileNotFoundException {
        super.initTrace(path, eventType, indexTrace);
        try {
            currentJniTrace = JniTraceFactory.getJniTrace(path, traceLibPath, SHOW_LTT_DEBUG_DEFAULT);
        } catch (Exception e) {
            throw new FileNotFoundException(e.getMessage());
        }

        // Export all the event types from the JNI side
        traceTypes = new HashMap<Integer, LttngEventType>();
        traceTypeNames = new Vector<Integer>();
        initialiseEventTypes(currentJniTrace);

        // *** VERIFY ***
        // Verify that all those "default constructor" are safe to use
        eventTimestamp = new LttngTimestamp();
        eventSource = new LttngEventSource();
        this.eventType = new LttngEventType();
        eventContent = new LttngEventContent(currentLttngEvent);
        eventReference = new LttngEventReference(this.getName());

        // Create the skeleton event
        currentLttngEvent = new LttngEvent(this, eventTimestamp, eventSource, this.eventType, eventContent,
                eventReference, null);

        // Create a new current location
        previousLocation = new LttngLocation();

        // Set the currentEvent to the eventContent
        eventContent.setEvent(currentLttngEvent);

        // // Bypass indexing if asked
        // if ( bypassIndexing == false ) {
        // indexTrace(true);
        // }
        // else {
        // Even if we don't have any index, set ONE checkpoint
        // fCheckpoints.add(new TmfCheckpoint(new LttngTimestamp(0L) , new
        // LttngLocation() ) );

        // Set the time range of the trace
        TmfContext context = seekLocation(null);
        LttngEvent event = getNextEvent(context);
        LttngTimestamp startTime = new LttngTimestamp(event.getTimestamp());
        LttngTimestamp endTime = new LttngTimestamp(currentJniTrace.getEndTime().getTime());

        setTimeRange(new TmfTimeRange(startTime, endTime));
    }

    /**
     * Default Constructor.
     * <p>
     * 
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String path) throws Exception {
        // Call with "wait for completion" true and "skip indexing" false
        this(path, null, true, false);
    }

    /**
     * Constructor, with control over the indexing.
     * <p>
     * 
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * @param waitForCompletion
     *            Should we wait for indexign to complete before moving on.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String path, boolean waitForCompletion) throws Exception {
        // Call with "skip indexing" false
        this(path, null, waitForCompletion, false);
    }

    /**
     * Default constructor, with control over the indexing and possibility to bypass indexation
     * <p>
     * 
     * @param path
     *            Path to a <b>directory</b> that contain an LTTng trace.
     * @param traceLibPath
     *            Path to a <b>directory</b> that contains LTTng trace libraries.
     * @param waitForCompletion
     *            Should we wait for indexign to complete before moving on.
     * @param bypassIndexing
     *            Should we bypass indexing completly? This is should only be useful for unit testing.
     * 
     * @exception Exception
     *                (most likely LTTngTraceException or FileNotFoundException)
     * 
     */
    public LTTngTrace(String path, String traceLibPath, boolean waitForCompletion, boolean bypassIndexing)
            throws Exception {
        super(path, LttngEvent.class, path, CHECKPOINT_PAGE_SIZE, false);
        try {
            currentJniTrace = JniTraceFactory.getJniTrace(path, traceLibPath, SHOW_LTT_DEBUG_DEFAULT);
        } catch (Exception e) {
            throw new FileNotFoundException(e.getMessage());
        }

        this.traceLibPath = traceLibPath;
        // Export all the event types from the JNI side
        traceTypes = new HashMap<Integer, LttngEventType>();
        traceTypeNames = new Vector<Integer>();
        initialiseEventTypes(currentJniTrace);

        // *** VERIFY ***
        // Verify that all those "default constructor" are safe to use
        eventTimestamp = new LttngTimestamp();
        eventSource = new LttngEventSource();
        eventType = new LttngEventType();
        eventContent = new LttngEventContent(currentLttngEvent);
        eventReference = new LttngEventReference(this.getName());

        // Create the skeleton event
        currentLttngEvent = new LttngEvent(this, eventTimestamp, eventSource, eventType, eventContent, eventReference,
                null);

        // Create a new current location
        previousLocation = new LttngLocation();

        // Set the currentEvent to the eventContent
        eventContent.setEvent(currentLttngEvent);

        // // Bypass indexing if asked
        // if ( bypassIndexing == false ) {
        // indexTrace(true);
        // }
        // else {
        // Even if we don't have any index, set ONE checkpoint
        // fCheckpoints.add(new TmfCheckpoint(new LttngTimestamp(0L) , new
        // LttngLocation() ) );

        // Set the time range of the trace
        TmfContext context = seekLocation(null);
        LttngEvent event = getNextEvent(context);
        LttngTimestamp startTime = new LttngTimestamp(event.getTimestamp());
        LttngTimestamp endTime = new LttngTimestamp(currentJniTrace.getEndTime().getTime());

        setTimeRange(new TmfTimeRange(startTime, endTime));
    }

    /*
     * Copy constructor is forbidden for LttngEvenmStream
     */
    public LTTngTrace(LTTngTrace oldTrace) throws Exception {
        this(oldTrace.getPath(), oldTrace.getTraceLibPath(), false, true);

        // *** VERIFY ***
        // Is this safe?
        this.fCheckpoints = oldTrace.fCheckpoints;

        /*
         * // This would only work if the index is already done this.fCheckpoints = new Vector<TmfCheckpoint>(
         * oldTrace.fCheckpoints.size() ); for (int x = 0; x<oldTrace.fCheckpoints.size(); x++){ TmfCheckpoint
         * tmpCheckPoint = oldTrace.fCheckpoints.get(x); this.fCheckpoints.add( new
         * TmfCheckpoint(tmpCheckPoint.getTimestamp(), tmpCheckPoint.getLocation()) ); }
         */

        // Set the start time of the trace
        setTimeRange(new TmfTimeRange(new LttngTimestamp(oldTrace.getStartTime()), new LttngTimestamp(
                oldTrace.getEndTime())));
    }

    @Override
    public LTTngTrace copy() {
        LTTngTrace returnedTrace = null;

        try {
            returnedTrace = new LTTngTrace(this);
        } catch (Exception e) {
            System.out
                    .println("ERROR : Could not create LTTngTrace copy (createTraceCopy).\nError is : " + e.getStackTrace()); //$NON-NLS-1$
        }

        return returnedTrace;
    }

    @Override
    public synchronized LTTngTrace clone() {
        LTTngTrace clone = null;
        try {
            clone = (LTTngTrace) super.clone();
            try {
                clone.currentJniTrace = JniTraceFactory.getJniTrace(getPath(), getTraceLibPath(),
                        SHOW_LTT_DEBUG_DEFAULT);
            } catch (JniException e) {
                // e.printStackTrace();
            }

            // Export all the event types from the JNI side
            clone.traceTypes = new HashMap<Integer, LttngEventType>();
            clone.traceTypeNames = new Vector<Integer>();
            clone.initialiseEventTypes(clone.currentJniTrace);

            // Verify that all those "default constructor" are safe to use
            clone.eventTimestamp = new LttngTimestamp();
            clone.eventSource = new LttngEventSource();
            clone.eventType = new LttngEventType();
            clone.eventContent = new LttngEventContent(clone.currentLttngEvent);
            clone.eventReference = new LttngEventReference(this.getName());

            // Create the skeleton event
            clone.currentLttngEvent = new LttngEvent(this, clone.eventTimestamp, clone.eventSource, clone.eventType,
                    clone.eventContent, clone.eventReference, null);

            // Create a new current location
            clone.previousLocation = new LttngLocation();

            // Set the currentEvent to the eventContent
            clone.eventContent.setEvent(clone.currentLttngEvent);

            // Set the start time of the trace
            setTimeRange(new TmfTimeRange(new LttngTimestamp(clone.currentJniTrace.getStartTime().getTime()),
                    new LttngTimestamp(clone.currentJniTrace.getEndTime().getTime())));
        } catch (CloneNotSupportedException e) {
        }

        return clone;
    }

    public String getTraceLibPath() {
        return traceLibPath;
    }

    /*
     * Fill out the HashMap with "Type" (Tracefile/Marker)
     * 
     * This should be called at construction once the trace is open
     */
    private void initialiseEventTypes(JniTrace trace) {
        // Work variables
        LttngEventType tmpType = null;
        String[] markerFieldsLabels = null;

        String newTracefileKey = null;
        Integer newMarkerKey = null;

        JniTracefile newTracefile = null;
        JniMarker newMarker = null;

        // First, obtain an iterator on TRACEFILES of owned by the TRACE
        Iterator<String> tracefileItr = trace.getTracefilesMap().keySet().iterator();

        while (tracefileItr.hasNext()) {
            newTracefileKey = tracefileItr.next();
            newTracefile = trace.getTracefilesMap().get(newTracefileKey);

            // From the TRACEFILE read, obtain its MARKER
            Iterator<Integer> markerItr = newTracefile.getTracefileMarkersMap().keySet().iterator();
            while (markerItr.hasNext()) {
                newMarkerKey = markerItr.next();
                newMarker = newTracefile.getTracefileMarkersMap().get(newMarkerKey);

                // From the MARKER we can obtain the MARKERFIELDS keys (i.e.
                // labels)
                markerFieldsLabels = newMarker.getMarkerFieldsHashMap().keySet()
                        .toArray(new String[newMarker.getMarkerFieldsHashMap().size()]);

                tmpType = new LttngEventType(newTracefile.getTracefileName(), newTracefile.getCpuNumber(),
                        newMarker.getName(), newMarkerKey.intValue(), markerFieldsLabels);

                // Add the type to the map/vector
                addEventTypeToMap(tmpType);
            }
        }
    }

    /*
     * Add a new type to the HashMap
     * 
     * As the hashmap use a key format that is a bit dangerous to use, we should always add using this function.
     */
    private void addEventTypeToMap(LttngEventType newEventType) {
        int newTypeKey = EventTypeKey.getEventTypeHash(newEventType);

        this.traceTypes.put(newTypeKey, newEventType);
        this.traceTypeNames.add(newTypeKey);
    }

    // /**
    // * Index the current trace.
    // *
    // * @param useless This boolean is only to comply to the interface and will
    // be ignored.
    // */
    // @Override
    // public synchronized void indexTrace(boolean useless) {
    //
    // long nbEvents=0L;
    //
    // // Start time need to be null to detect none have been set
    // // LastTime need to exist so we can ajust it as we go
    // LttngTimestamp startTime = null;
    // LttngTimestamp lastTime = new LttngTimestamp();
    //
    // // Position the trace at the beginning
    // TmfContext context = seekEvent( new LttngTimestamp(0L) );
    //
    // // Read the first event and extract the location
    // LttngEvent tmpEvent = (LttngEvent)getNextEvent(context);
    //
    // // If we read the first event, define the start time.
    // if ( tmpEvent != null ) {
    // startTime = new LttngTimestamp( tmpEvent.getTimestamp() );
    // lastTime.setValue(tmpEvent.getTimestamp().getValue());
    // }
    //
    // // Now, we read each event until we hit the end of the trace
    // // We will create a new checkpoint every "getCacheSize()" event
    // while ( tmpEvent != null) {
    // // Update the last time each time we read a new event
    // lastTime.setValue(tmpEvent.getTimestamp().getValue());
    //
    // // Save a check point if needed
    // if ((nbEvents++ % getCacheSize()) == 0) {
    // // *** IMPORTANT
    // // We need to NEW each stuff we put in checkpoint
    // // Otherwise everything will be the same!
    // LttngTimestamp tmpTimestamp = new LttngTimestamp(
    // (LttngTimestamp)tmpEvent.getTimestamp() );
    // LttngLocation newLocation = new LttngLocation(
    // (LttngTimestamp)tmpEvent.getTimestamp() );
    //
    // fCheckpoints.add(new TmfCheckpoint(tmpTimestamp, newLocation ) );
    // }
    // // Read the next event
    // tmpEvent = (LttngEvent)getNextEvent(context);
    // }
    //
    // // If we have a start time, we should have an end time as well
    // // Issue the new range
    // if (startTime != null) {
    // setTimeRange( new TmfTimeRange(startTime, lastTime) );
    // notifyListeners(getTimeRange() );
    // }
    //
    // // Ajust the total number of event in the trace
    // fNbEvents = nbEvents;
    // //printCheckpointsVector();
    // //printDebug = true;
    // }

    /**
     * Return the latest saved location. Note : Modifying the returned location may result in buggy positionning!
     * 
     * @return The LttngLocation as it was after the last operation.
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     */
    @Override
    public synchronized ITmfLocation<?> getCurrentLocation() {
        return previousLocation;
    }

    /**
     * Position the trace to the event at the given location.
     * <p>
     * NOTE : Seeking by location is very fast compare to seeking by position but is still slower than "ReadNext", avoid
     * using it for small interval.
     * 
     * @param location
     *            Location of the event in the trace. If no event available at this exact location, we will position
     *            ourself to the next one.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekLocation(ITmfLocation<?> location) {

        // // [lmcfrch]
        // lastTime = 0;

        if (PrintDebug) {
            System.out.println("seekLocation(location) location -> " + location); //$NON-NLS-1$
        }

        // If the location in context is null, create a new one
        LttngLocation curLocation = null;
        if (location == null) {
            curLocation = new LttngLocation();
            TmfContext context = seekEvent(curLocation.getOperationTime());
            context.setRank(ITmfContext.INITIAL_RANK);
            return context;
        } else {
            curLocation = (LttngLocation) location;
        }

        // *** NOTE :
        // Update to location should (and will) be done in SeekEvent.

        // The only seek valid in LTTng is with the time, we call
        // seekEvent(timestamp)
        TmfContext context = seekEvent(curLocation.getOperationTime());

        // // Adjust the previousLocation flags
        // if (location instanceof LttngLocation) {
        // LttngLocation lttngLocation = (LttngLocation) location;
        // if (lttngLocation.isLastOperationReadNext()) {
        // previousLocation.setLastOperationReadNext();
        // } else if (lttngLocation.isLastOperationParse()) {
        // previousLocation.setLastOperationParse();
        // } else if (lttngLocation.isLastOperationSeek()) {
        // previousLocation.setLastOperationSeek();
        // }
        // }

        return context;
    }

    /**
     * Position the trace to the event at the given time.
     * <p>
     * NOTE : Seeking by time is very fast compare to seeking by position but is still slower than "ReadNext", avoid
     * using it for small interval.
     * 
     * @param timestamp
     *            Time of the event in the trace. If no event available at this exact time, we will position ourself to
     *            the next one.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekEvent(TmfTimestamp timestamp) {

        // // [lmcfrch]
        // lastTime = 0;

        if (PrintDebug) {
            System.out.println("seekEvent(timestamp) timestamp -> " + timestamp); //$NON-NLS-1$
        }

        // Call JNI to seek
        currentJniTrace.seekToTime(new JniTime(timestamp.getValue()));

        // Save the time at which we seeked
        previousLocation.setOperationTime(timestamp.getValue());
        // Set the operation marker as seek, to be able to detect we did "seek"
        // this event
        previousLocation.setLastOperationSeek();

        // *** VERIFY ***
        // Is that too paranoid?
        //
        // We don't trust what upper level could do with our internal location
        // so we create a new one to return instead
        LttngLocation curLocation = new LttngLocation(previousLocation);

        return new TmfContext(curLocation);
    }

    /**
     * Position the trace to the event at the given position (rank).
     * <p>
     * NOTE : Seeking by position is very slow in LTTng, consider seeking by timestamp.
     * 
     * @param position
     *            Position (or rank) of the event in the trace, starting at 0.
     * 
     * @return The TmfContext that point to this event
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.trace.TmfContext
     */
    @Override
    public synchronized TmfContext seekEvent(long position) {

        if (PrintDebug) {
            System.out.println("seekEvent(position) position -> " + position); //$NON-NLS-1$
        }

        TmfTimestamp timestamp = null;
        long index = position / getCacheSize();

        // Get the timestamp of the closest check point to the given position
        if (fCheckpoints.size() > 0) {
            if (index >= fCheckpoints.size()) {
                index = fCheckpoints.size() - 1;
            }
            timestamp = fCheckpoints.elementAt((int) index).getTimestamp();
        }
        // If none, take the start time of the trace
        else {
            timestamp = getStartTime();
        }

        // Seek to the found time
        TmfContext tmpContext = seekEvent(timestamp);
        tmpContext.setRank((index + 1) * fIndexPageSize);
        previousLocation = (LttngLocation) tmpContext.getLocation();

        // Ajust the index of the event we found at this check point position
        Long currentPosition = index * getCacheSize();

        Long lastTimeValueRead = 0L;

        // Get the event at current position. This won't move to the next one
        JniEvent tmpJniEvent = currentJniTrace.findNextEvent();
        // Now that we are positionned at the checkpoint,
        // we need to "readNext" (Position - CheckpointPosition) times or until
        // trace "run out"
        while ((tmpJniEvent != null) && (currentPosition < position)) {
            tmpJniEvent = currentJniTrace.readNextEvent();
            currentPosition++;
        }

        // If we found our event, save its timestamp
        if (tmpJniEvent != null) {
            lastTimeValueRead = tmpJniEvent.getEventTime().getTime();
        }

        // Set the operation marker as seek, to be able to detect we did "seek"
        // this event
        previousLocation.setLastOperationSeek();
        // Save read event time
        previousLocation.setOperationTime(lastTimeValueRead);

        // *** VERIFY ***
        // Is that too paranoid?
        //
        // We don't trust what upper level could do with our internal location
        // so we create a new one to return instead
        LttngLocation curLocation = new LttngLocation(previousLocation);

        return new TmfContext(curLocation);
    }

    @Override
    public TmfContext seekLocation(double ratio) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getLocationRatio(ITmfLocation<?> location) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Return the event in the trace according to the given context. Read it if necessary.
     * <p>
     * Similar (same?) as ParseEvent except that calling GetNext twice read the next one the second time.
     * 
     * @param context
     *            Current TmfContext where to get the event
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.trace.TmfContext
     */

    public int nbEventsRead = 0;

    @Override
    public synchronized LttngEvent getNextEvent(TmfContext context) {

        if (PrintDebug) {
            System.out.println("getNextEvent(context) context.getLocation() -> " //$NON-NLS-1$
                    + context.getLocation());
        }

        LttngEvent returnedEvent = null;
        LttngLocation curLocation = null;

        curLocation = (LttngLocation) context.getLocation();
        // If the location in context is null, create a new one
        if (curLocation == null) {
            curLocation = getCurrentLocation(context);
        }

        // // [lmcfrch]
        // TmfContext savedContext = context.clone();
        // // [/lmcfrch]

        // *** HACK ***
        // TMF assumes it is possible to read (GetNextEvent) to the next Event
        // once ParseEvent() is called
        // In LTTNG, there is not difference between "Parsing" and "Reading" an
        // event.
        // Since parsing/reading invalidate the previous event,
        // we need to make sure the sequence ParseEvent() -> GetNextEvent() will
        // not actually move to the next event.
        // To do so, we avoid moving for call to "GetNextEvent()" that follow
        // call to a call to "ParseEvent()".
        // However, calling ParseEvent() -> GetNextEvent() -> GetNextEvent()
        // will only move next by one.

        // *** Positioning trick :
        // GetNextEvent only read the trace if :
        // 1- The last operation was NOT a ParseEvent --> A read is required
        // OR
        // 2- The time of the previous location is different from the current
        // one --> A seek + a read is required
        if ((!(curLocation.isLastOperationParse()))
                || (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue())) {
            if (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue()) {
                if (PrintDebug) {
                    System.out.println("\t\tSeeking in getNextEvent. [ LastTime : " //$NON-NLS-1$
                            + previousLocation.getOperationTimeValue() + " CurrentTime" //$NON-NLS-1$
                            + curLocation.getOperationTimeValue() + " ]"); //$NON-NLS-1$
                }
                seekEvent(curLocation.getOperationTime());
            }
            // Read the next event from the trace. The last one will NO LONGER
            // BE VALID.
            returnedEvent = readNextEvent(curLocation);

        } else {
            // No event was read, just return the one currently loaded (the last
            // one we read)
            returnedEvent = currentLttngEvent;

            // *** IMPORTANT!
            // Reset (erase) the operation marker to both location, to be able
            // to detect we did NOT "read" this event
            previousLocation.resetLocationState();
            curLocation.resetLocationState();
        }

        // If we read an event, set it's time to the locations (both previous
        // and current)
        if (returnedEvent != null) {
            setPreviousAndCurrentTimes(context, returnedEvent, curLocation);

            // // [lmcfrch]
            // LttngLocation prevLocation = (LttngLocation)
            // savedContext.getLocation();
            // LttngLocation currLocation = (LttngLocation)
            // context.getLocation();
            // if (prevLocation.equals(currLocation)) {
            // System.out.println("Aie");
            // }
            // Tracer.trace("Trc: " + context.getRank() + ": " +
            // returnedEvent.getTimestamp().toString() + " (" +
            // (prevLocation.isLastOperationParse() ? "T" : "F") + "," +
            // (prevLocation.isLastOperationReadNext() ? "T" : "F") + "," +
            // (prevLocation.isLastOperationSeek() ? "T" : "F") + "), (" +
            // (currLocation.isLastOperationParse() ? "T" : "F") + "," +
            // (currLocation.isLastOperationReadNext() ? "T" : "F") + "," +
            // (currLocation.isLastOperationSeek() ? "T" : "F") + ")"
            // );
            // // [/lmcfrch]

        }

        return returnedEvent;
    }

    // this method was extracted for profiling purposes
    private void setPreviousAndCurrentTimes(TmfContext context, LttngEvent returnedEvent, LttngLocation curLocation) {

        TmfTimestamp eventTimestamp = returnedEvent.getTimestamp();
        // long eventTime = eventTimestamp.getValue();
        previousLocation.setOperationTime(eventTimestamp.getValue());
        curLocation.setOperationTime(eventTimestamp.getValue());
        updateIndex(context, context.getRank(), eventTimestamp);
        context.updateRank(1);
    }

    protected void updateIndex(TmfContext context, long rank, TmfTimestamp timestamp) {

        if (getStartTime().compareTo(timestamp, false) > 0)
            setStartTime(timestamp);
        if (getEndTime().compareTo(timestamp, false) < 0)
            setEndTime(timestamp);
        if (rank != ITmfContext.UNKNOWN_RANK) {
            if (fNbEvents <= rank)
                fNbEvents = rank + 1;
            // Build the index as we go along
            if ((rank % fIndexPageSize) == 0) {
                // Determine the table position
                long position = rank / fIndexPageSize;
                // Add new entry at proper location (if empty)
                if (fCheckpoints.size() == position) {
                    addCheckPoint(context, timestamp);
                }
            }
        }
    }

    private void addCheckPoint(TmfContext context, TmfTimestamp timestamp) {
        ITmfLocation<?> location = context.getLocation().clone();
        fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
    }

    // this method was extracted for profiling purposes
    private LttngEvent readNextEvent(LttngLocation curLocation) {
        LttngEvent returnedEvent;
        // Read the next event from the trace. The last one will NO LONGER BE
        // VALID.
        returnedEvent = readEvent(curLocation);
        nbEventsRead++;

        // Set the operation marker as read to both location, to be able to
        // detect we did "read" this event
        previousLocation.setLastOperationReadNext();
        curLocation.setLastOperationReadNext();
        return returnedEvent;
    }

    // this method was extracted for profiling purposes
    private LttngLocation getCurrentLocation(TmfContext context) {
        LttngLocation curLocation;
        curLocation = new LttngLocation();
        context.setLocation(curLocation);
        return curLocation;
    }

    /**
     * Return the event in the trace according to the given context. Read it if necessary.
     * <p>
     * Similar (same?) as GetNextEvent except that calling ParseEvent twice will return the same event
     * 
     * @param context
     *            Current TmfContext where to get the event
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * @see org.eclipse.linuxtools.tmf.trace.TmfContext
     */
    @Override
    public synchronized LttngEvent parseEvent(TmfContext context) {

        if (PrintDebug) {
            System.out.println("parseEvent(context) context.getLocation() -> " //$NON-NLS-1$
                    + context.getLocation());
        }

        LttngEvent returnedEvent = null;
        LttngLocation curLocation = null;

        // If the location in context is null, create a new one
        if (context.getLocation() == null) {
            curLocation = new LttngLocation();
            context.setLocation(curLocation);
        }
        // Otherwise, we use the one in context; it should be a valid
        // LttngLocation
        else {
            curLocation = (LttngLocation) context.getLocation();
        }

        // *** HACK ***
        // TMF assumes it is possible to read (GetNextEvent) to the next Event
        // once ParseEvent() is called
        // In LTTNG, there is not difference between "Parsing" and "Reading" an
        // event.
        // So, before "Parsing" an event, we have to make sure we didn't "Read"
        // it alreafy.
        // Also, "Reading" invalidate the previous Event in LTTNG and seek back
        // is very costly,
        // so calling twice "Parse" will return the same event, giving a way to
        // get the "Currently loaded" event

        // *** Positionning trick :
        // ParseEvent only read the trace if :
        // 1- The last operation was NOT a ParseEvent or a GetNextEvent --> A
        // read is required
        // OR
        // 2- The time of the previous location is different from the current
        // one --> A seek + a read is required
        if (((!(curLocation.isLastOperationParse())) && ((!(curLocation.isLastOperationReadNext()))))
                || (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue())) {
            // Previous time != Current time : We need to reposition to the
            // current time
            if (previousLocation.getOperationTimeValue() != curLocation.getOperationTimeValue()) {
                if (PrintDebug) {
                    System.out.println("\t\tSeeking in getNextEvent. [ LastTime : " //$NON-NLS-1$
                            + previousLocation.getOperationTimeValue() + " CurrentTime" //$NON-NLS-1$
                            + curLocation.getOperationTimeValue() + " ]"); //$NON-NLS-1$
                }
                seekEvent(curLocation.getOperationTime());
            }

            // Read the next event from the trace. The last one will NO LONGER
            // BE VALID.
            returnedEvent = readEvent(curLocation);
        } else {
            // No event was read, just return the one currently loaded (the last
            // one we read)
            returnedEvent = currentLttngEvent;
        }

        // If we read an event, set it's time to the locations (both previous
        // and current)
        if (returnedEvent != null) {
            previousLocation.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
            curLocation.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
        }

        // Set the operation marker as parse to both location, to be able to
        // detect we already "read" this event
        previousLocation.setLastOperationParse();
        curLocation.setLastOperationParse();

        return returnedEvent;
    }

    /*
     * Read the next event from the JNI and convert it as Lttng Event<p>
     * 
     * @param location Current LttngLocation that to be updated with the event timestamp
     * 
     * @return The LttngEvent we read of null if no event are available
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngLocation
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    private synchronized LttngEvent readEvent(LttngLocation location) {
        LttngEvent returnedEvent = null;
        JniEvent tmpEvent = null;

        // Read the next event from JNI. THIS WILL INVALIDATE THE CURRENT LTTNG
        // EVENT.
        tmpEvent = currentJniTrace.readNextEvent();

        if (tmpEvent != null) {
            // *** NOTE
            // Convert will update the currentLttngEvent
            returnedEvent = convertJniEventToTmf(tmpEvent);

            location.setOperationTime((LttngTimestamp) returnedEvent.getTimestamp());
        }
        // *** NOTE
        // If the read failed (likely the last event in the trace), set the
        // LastReadTime to the JNI time
        // That way, even if we try to read again, we will step over the bogus
        // seek and read
        else {
            location.setOperationTime(getEndTime().getValue() + 1);
        }

        return returnedEvent;
    }

    /**
     * Method to convert a JniEvent into a LttngEvent.
     * <p>
     * 
     * Note : This method will call LttngEvent convertEventJniToTmf(JniEvent, boolean) with a default value for
     * isParsingNeeded
     * 
     * @param newEvent
     *            The JniEvent to convert into LttngEvent
     * 
     * @return The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public synchronized LttngEvent convertJniEventToTmf(JniEvent newEvent) {
        currentLttngEvent = convertJniEventToTmf(newEvent, IS_PARSING_NEEDED_DEFAULT);

        return currentLttngEvent;
    }

    /**
     * Method to convert a JniEvent into a LttngEvent
     * 
     * @param jniEvent
     *            The JniEvent to convert into LttngEvent
     * @param isParsingNeeded
     *            A boolean value telling if the event should be parsed or not.
     * 
     * @return The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public synchronized LttngEvent convertJniEventToTmf(JniEvent jniEvent, boolean isParsingNeeded) {

        if (UniqueEvent) {

            // ***
            // UNHACKED : We can no longer do that because TCF need to maintain
            // several events at once.
            // This is very slow to do so in LTTng, this has to be temporary.
            // *** HACK ***
            // To save time here, we only set value instead of allocating new
            // object
            // This give an HUGE performance improvement
            // all allocation done in the LttngTrace constructor
            // ***
            eventTimestamp.setValue(jniEvent.getEventTime().getTime());
            eventSource.setSourceId(jniEvent.requestEventSource());

            eventType = traceTypes.get(EventTypeKey.getEventTypeHash(jniEvent));

            eventReference.setValue(jniEvent.getParentTracefile().getTracefilePath());
            eventReference.setTracepath(this.getName());

            eventContent.emptyContent();

            currentLttngEvent.setType(eventType);
            // Save the jni reference
            currentLttngEvent.updateJniEventReference(jniEvent);

            // Parse now if was asked
            // Warning : THIS IS SLOW
            if (isParsingNeeded) {
                eventContent.getFields();
            }

            return currentLttngEvent;
        } else {
            return convertJniEventToTmfMultipleEventEvilFix(jniEvent, isParsingNeeded);
        }

    }

    /**
     * This method is a temporary fix to support multiple events at once in TMF This is expected to be slow and should
     * be fixed in another way. See comment in convertJniEventToTmf();
     * 
     * @param jniEvent
     *            The current JNI Event
     * @return Current Lttng Event fully parsed
     */
    private synchronized LttngEvent convertJniEventToTmfMultipleEventEvilFix(JniEvent jniEvent, boolean isParsingNeeded) {
        // *** HACK ***
        // Below : the "fix" with all the new and the full-parse
        // Allocating new memory is slow.
        // Parsing every events is very slow.
        eventTimestamp = new LttngTimestamp(jniEvent.getEventTime().getTime());
        eventSource = new LttngEventSource(jniEvent.requestEventSource());
        eventReference = new LttngEventReference(jniEvent.getParentTracefile().getTracefilePath(), this.getName());
        eventType = new LttngEventType(traceTypes.get(EventTypeKey.getEventTypeHash(jniEvent)));
        eventContent = new LttngEventContent(currentLttngEvent);
        currentLttngEvent = new LttngEvent(this, eventTimestamp, eventSource, eventType, eventContent, eventReference,
                null);

        // The jni reference is no longer reliable but we will keep it anyhow
        currentLttngEvent.updateJniEventReference(jniEvent);
        // Ensure that the content is correctly set
        eventContent.setEvent(currentLttngEvent);

        // Parse the event if it was needed
        // *** WARNING ***
        // ONLY for testing, NOT parsing events with non-unique events WILL
        // result in segfault in the JVM
        if (isParsingNeeded) {
            eventContent.getFields();
        }

        return currentLttngEvent;
    }

    /**
     * Reference to the current LttngTrace we are reading from.
     * <p>
     * 
     * Note : This bypass the framework and should not be use, except for testing!
     * 
     * @return Reference to the current LttngTrace
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getCurrentJniTrace() {
        return currentJniTrace;
    }

    /**
     * Return a reference to the current LttngEvent we have in memory.
     * 
     * @return The current (last read) LttngEvent
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public synchronized LttngEvent getCurrentEvent() {
        return currentLttngEvent;
    }

    /**
     * Get the major version number for the current trace
     * 
     * @return Version major or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMajor() {
        if (currentJniTrace != null) {
            return currentJniTrace.getLttMajorVersion();
        } else {
            return -1;
        }
    }

    /**
     * Get the minor version number for the current trace
     * 
     * @return Version minor or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMinor() {
        if (currentJniTrace != null) {
            return currentJniTrace.getLttMinorVersion();
        } else {
            return -1;
        }
    }

    /**
     * Get the number of CPU for this trace
     * 
     * @return Number of CPU or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public int getCpuNumber() {
        if (currentJniTrace != null) {
            return currentJniTrace.getCpuNumber();
        } else {
            return -1;
        }
    }

    /**
     * Print the content of the checkpoint vector.
     * <p>
     * 
     * This is intended for debug purpose only.
     */
    public void printCheckpointsVector() {
        System.out.println("StartTime : " //$NON-NLS-1$
                + getTimeRange().getStartTime().getValue());
        System.out.println("EndTime   : " //$NON-NLS-1$
                + getTimeRange().getEndTime().getValue());

        for (int pos = 0; pos < fCheckpoints.size(); pos++) {
            System.out.print(pos + ": " + "\t"); //$NON-NLS-1$ //$NON-NLS-2$
            System.out.print(fCheckpoints.get(pos).getTimestamp() + "\t"); //$NON-NLS-1$
            System.out.println(fCheckpoints.get(pos).getLocation());
        }
    }

    @Override
    public synchronized void dispose() {
        if (currentJniTrace != null)
            currentJniTrace.closeTrace();
        super.dispose();
    }

    /**
     * Return a String identifying this trace.
     * 
     * @return String that identify this trace
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        String returnedData = "";

        returnedData += "Path :" + getPath() + " ";
        returnedData += "Trace:" + currentJniTrace + " ";
        returnedData += "Event:" + currentLttngEvent;

        return returnedData;
    }

}

/*
 * EventTypeKey inner class
 * 
 * This class is used to make the process of generating the HashMap key more transparent and so less error prone to use
 */
final class EventTypeKey {
    // *** WARNING ***
    // These two getEventTypeKey() functions should ALWAYS construct the key the
    // same ways!
    // Otherwise, every type search will fail!

    // added final to encourage inlining.

    // generating a hash code by hand to avoid a string creation
    final static public int getEventTypeHash(LttngEventType newEventType) {
        return generateHash(newEventType.getTracefileName(), newEventType.getCpuId(), newEventType.getMarkerName());
    }

    final private static int generateHash(String traceFileName, long cpuNumber, String markerName) {
        // 0x1337 is a prime number. The number of CPUs is always under 8192 on
        // the current kernel, so this will work with the current linux kernel.
        int cpuHash = (int) (cpuNumber * (0x1337));
        return traceFileName.hashCode() ^ (cpuHash) ^ markerName.hashCode();
    }

    // generating a hash code by hand to avoid a string creation
    final static public int getEventTypeHash(JniEvent newEvent) {
        return generateHash(newEvent.getParentTracefile().getTracefileName(), newEvent.getParentTracefile()
                .getCpuNumber(), newEvent.requestEventMarker().getName());
    }

}
