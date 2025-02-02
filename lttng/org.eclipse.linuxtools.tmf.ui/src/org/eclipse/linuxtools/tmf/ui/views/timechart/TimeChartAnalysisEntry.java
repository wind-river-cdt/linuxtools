/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public class TimeChartAnalysisEntry implements ITmfTimeAnalysisEntry {

    private ITmfTrace fTrace;
    private String fGroup;
    private Vector<TimeChartEvent> fTraceEvents;
    private int fPower = 0; // 2^fPower nanoseconds per vector position
    private long fReferenceTime = -1; // time corresponding to beginning of index 0
    private long fStartTime = -1; // time of first event
    private long fStopTime = -1; // time of last event
    private long fLastRank = -1; // rank of last processed trace event

    TimeChartAnalysisEntry(ITmfTrace trace, int modelSize) {
        fTrace = trace;
        fTraceEvents = new Vector<TimeChartEvent>(modelSize);
    }
    
    TimeChartAnalysisEntry(ITmfTrace trace, String group, int modelSize) {
        fTrace = trace;
        fTraceEvents = new Vector<TimeChartEvent>(modelSize);
        fGroup = group;
    }
    
    @Override
    public String getGroupName() {
        return fGroup;
    }

    @Override
    public int getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getName() {
        return fTrace.getName();
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getStopTime() {
        return fStopTime;
    }

    @Override
    @Deprecated public <T extends ITimeEvent> Vector<T> getTraceEvents() {
        return null;
    }

    @Override
    public Iterator<ITimeEvent> getTraceEventsIterator() {
        return new EntryIterator(0, Long.MAX_VALUE, 0);
    }
    
    @Override
    public Iterator<ITimeEvent> getTraceEventsIterator(long startTime, long stopTime, long maxDuration) {
        return new EntryIterator(startTime, stopTime, maxDuration);
    }
    
    private class EntryIterator implements Iterator<ITimeEvent> {
        private final long fIteratorStartTime;
        private final long fIteratorStopTime;
        private final long fIteratorMaxDuration;
        private long lastTime = -1;
        private TimeChartEvent next = null;
        private Iterator<ITimeEvent> nestedIterator = null;
        
        public EntryIterator(long startTime, long stopTime, long maxDuration) {
            fIteratorStartTime = startTime;
            fIteratorStopTime = stopTime;
            fIteratorMaxDuration = maxDuration;
        }
        
        @Override
        public boolean hasNext() {
            synchronized (fTraceEvents) {
                if (next != null) return true;
                if (nestedIterator != null) {
                    if (nestedIterator.hasNext()) {
                        return true;
                    } else {
                        nestedIterator = null;
                    }
                }
                long time = (lastTime == -1) ? fStartTime : lastTime;
                int index = (fReferenceTime == -1) ? 0 : (int) ((time - fReferenceTime) >> fPower);
                while (index < fTraceEvents.size()) {
                    TimeChartEvent event = fTraceEvents.get(index++);
                    if (event != null && (lastTime == -1 || event.getTime() > time)) {
                        if (event.getTime() + event.getDuration() >= fIteratorStartTime && event.getTime() <= fIteratorStopTime) {
                            if (event.getItemizedEntry() == null || event.getDuration() <= fIteratorMaxDuration) {
                                lastTime = event.getTime() + event.getDuration();
                                next = event;
                                return true;
                            } else {
                                nestedIterator = event.getItemizedEntry().getTraceEventsIterator(fIteratorStartTime, fIteratorStopTime, fIteratorMaxDuration);
                                return nestedIterator.hasNext();
                            }
                        }
                    }
                }
                return false;
            }
        }

        @Override
        public TimeChartEvent next() {
            synchronized (fTraceEvents) {
                if (nestedIterator != null) {
                    TimeChartEvent event = (TimeChartEvent) nestedIterator.next();
                    lastTime = event.getTime() + event.getDuration();
                    return event;  
                }
                if (hasNext()) {
                    TimeChartEvent event = next;
                    next = null;
                    return event;
                }
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
            
    }

    @Override
    public void addTraceEvent(ITimeEvent timeEvent) {
        long time = timeEvent.getTime();
        synchronized (fTraceEvents) {
            long index = (fReferenceTime == -1) ? 0 : (time - fReferenceTime) >> fPower;
            if (index < 0) {
                if (fTraceEvents.capacity() - fTraceEvents.size() < -index) {
                    int powershift = (-index + fTraceEvents.size() <= 2 * fTraceEvents.capacity()) ? 1 :
                        (int) Math.ceil(Math.log((double) (-index + fTraceEvents.size()) / fTraceEvents.capacity()) / Math.log(2));
                    merge(powershift);
                    index = (int) ((time - fReferenceTime) >> fPower);
                }
                shift((int) -index);
                index = 0;
                fTraceEvents.set(0, (TimeChartEvent) timeEvent);
            } else if (index < fTraceEvents.capacity()) {
                if (index >= fTraceEvents.size()) {
                    fTraceEvents.setSize((int) index + 1);
                }
            } else {
                int powershift = (index < 2 * fTraceEvents.capacity()) ? 1 :
                    (int) Math.ceil(Math.log((double) (index + 1) / fTraceEvents.capacity()) / Math.log(2));
                merge(powershift);
                index = (int) ((time - fReferenceTime) >> fPower);
                fTraceEvents.setSize((int) index + 1);
            }
            TimeChartEvent event = (TimeChartEvent) fTraceEvents.get((int) index);
            if (event == null) {
                fTraceEvents.set((int) index, (TimeChartEvent) timeEvent);
            } else {
                if (event.getItemizedEntry() == null) {
                    event.merge((TimeChartEvent) timeEvent);
                } else {
                	event.mergeDecorations((TimeChartEvent) timeEvent);
                    event.getItemizedEntry().addTraceEvent(timeEvent);
                }
            }
            if (fReferenceTime == -1 || time < fReferenceTime) {
                fReferenceTime = (time >> fPower) << fPower;
            }
            if (fStartTime == -1 || time < fStartTime) {
                fStartTime = time;
            }
            if (fStopTime == -1 || time > fStopTime) {
                fStopTime = time;
            }
        }
    }

    private void merge(int powershift) {
        fPower += powershift;
        fReferenceTime = (fReferenceTime >> fPower) << fPower;
        int index = 0;
        for (int i = 0; i < fTraceEvents.size(); i++) {
            TimeChartEvent event = fTraceEvents.get(i);
            if (event != null) {
                index = (int) ((event.getTime() - fReferenceTime) >> fPower);
                TimeChartEvent mergedEvent = (TimeChartEvent) fTraceEvents.get(index);
                if (mergedEvent == null) {
                    fTraceEvents.set(index, event);
                } else {
                    mergedEvent.merge(event);
                }
                if (i != index) {
                    fTraceEvents.set(i, null);
                }
            }
        }
        fTraceEvents.setSize(index + 1);
    }

    private void shift(int indexshift) {
        int oldSize = fTraceEvents.size();
        fTraceEvents.setSize(oldSize + indexshift);
        for (int i = oldSize - 1; i >= 0; i--) {
            fTraceEvents.set(i + indexshift, fTraceEvents.get(i));
        }
        for (int i = 0; i < indexshift; i++) {
            fTraceEvents.set(i, null);
        }
    }
    
    public ITmfTrace getTrace() {
        return fTrace;
    }
    
    public void setLastRank(long rank) {
        fLastRank = rank;
    }
    
    public long getLastRank() {
        return fLastRank;
    }
}
