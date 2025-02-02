/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: BasicFrame.java,v 1.2 2008/01/24 02:28:49 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.preferences.SDViewPref;

/**
 * The Frame class is the base sequence diagram graph nodes container.<br>
 * For instance, only one frame can be drawn in the View.<br>
 * Lifelines, Messages and Stop which are supposed to represent a Sequence diagram are drawn in a Frame.<br>
 * Only the graph node added to their representing list will be drawn.
 * 
 * The lifelines are appended along the X axsis when added in a frame.<br>
 * The syncMessages are ordered along the Y axsis depending on the event occurrence they are attached to.<br>
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline Lifeline for more event occurence details
 * @author sveyrier
 * @version 1.0
 */
public class BasicFrame extends GraphNode {

    /**
     * Contains the max elapsed time between two consecutive messages in the whole frame
     */
    protected TmfTimestamp maxTime = new TmfTimestamp(0);
    /**
     * Contains the min elapsed time between two consecutive messages in the whole frame
     */
    protected TmfTimestamp minTime = new TmfTimestamp(0);

    /**
     * Indicate if the min and max elapsed time between two consecutive messages in the whole frame need to be computed
     */
    protected boolean computeMinMax = true;

    /**
     * Store the preference set by the user regarding the external time. This flag is used determine if the min and max
     * need to be recomputed in case this preference is changed.
     */
    protected boolean lastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();

    /**
     * The greater event occurrence created on graph nodes drawn in this Frame This directly impact the Frame height
     */
    protected int verticalIndex = 0;

    /**
     * The index along the x axis where the next lifeline will is drawn This directly impact the Frame width
     */
    protected int horizontalIndex = 0;

    protected boolean timeInfo = false;

    /**
     * The current Frame visible area
     */
    protected int visibleAreaX;
    protected int visibleAreaY;
    protected int visibleAreaWidth;
    protected int visibleAreaHeight;

    static ISDPreferences userPref = null;

    protected int forceEventOccurrenceSpacing = -1;

    protected boolean customMinMax = false;

    protected TmfTimestamp minSDTime = new TmfTimestamp();
    protected TmfTimestamp maxSDTime = new TmfTimestamp();
    protected boolean initSDMin = true;

    /**
     * Creates an empty frame.
     */
    public BasicFrame() {
        Metrics.setForcedEventSpacing(forceEventOccurrenceSpacing);
    }

    /**
     * 
     * Returns the greater event occurence known by the Frame
     * 
     * @return the greater event occurrence
     */
    protected int getMaxEventOccurrence() {
        return verticalIndex;
    }

    /**
     * Set the greater event occurrence created in GraphNodes included in the frame
     * 
     * @param eventOccurrence the new greater event occurrence
     */
    protected void setMaxEventOccurrence(int eventOccurrence) {
        verticalIndex = eventOccurrence;
    }

    /**
     * This method increase the lifeline place holder The return value is usually assign to a lifeline. This can be used
     * to set the lifelines drawing order. Also, calling this method two times and assigning only the last given index
     * to a lifeline will increase this lifeline draw spacing (2 times the default spacing) from the last added
     * lifeline.
     * 
     * @return a new lifeline index
     */
    protected int getNewHorizontalIndex() {
        return ++horizontalIndex;
    }

    /**
     * Returns the current horizontal index
     * 
     * @return the current horizontal index
     * @see Frame#getNewHorizontalIndex() for horizontal index description
     */
    protected int getHorizontalIndex() {
        return horizontalIndex;
    }

    /**
     * Add a GraphNode into the frame
     * 
     * @param nodeToAdd the node to add
     */
    @Override
    public void addNode(GraphNode nodeToAdd) {
        computeMinMax = true;
        super.addNode(nodeToAdd);
    }

    /**
     * @return the frame x axis value in the containing view
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getX() {
        return Metrics.FRAME_H_MARGIN;
    }

    /**
     * @return the frame y axis value in the containing view
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getX()
     */
    @Override
    public int getY() {
        return Metrics.FRAME_V_MARGIN;
    }

    /**
     * The frame width depends on the number of lifeline added in the frame
     * 
     * @return the frame width
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getWidth()
     */
    @Override
    public int getWidth() {
        if (horizontalIndex == 0)
            return 3 * Metrics.swimmingLaneWidth() + Metrics.LIFELINE_H_MAGIN * 2 - Metrics.FRAME_H_MARGIN - Metrics.LIFELINE_SPACING / 2;
        else
            return horizontalIndex * Metrics.swimmingLaneWidth() + Metrics.LIFELINE_H_MAGIN * 2 + 1 - Metrics.LIFELINE_SPACING;
    }

    /**
     * The Frame height depends on the maximum number of messages added to a lifeline( Taking all lifelines into
     * account)
     * 
     * @return the frame height
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getHeight()
     */
    @Override
    public int getHeight() {
        if (verticalIndex == 0)
            return 5 * (Metrics.getMessagesSpacing() + Metrics.getMessageFontHeigth()) + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getFrameFontHeigth() + Metrics.LIFELINE_VT_MAGIN + Metrics.LIFELINE_VB_MAGIN
                    + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getLifelineFontHeigth() * 2;
        if (forceEventOccurrenceSpacing >= 0)
            Metrics.setForcedEventSpacing(forceEventOccurrenceSpacing);
        return verticalIndex * (Metrics.getMessagesSpacing() + Metrics.getMessageFontHeigth()) + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getFrameFontHeigth() + Metrics.LIFELINE_VT_MAGIN + Metrics.LIFELINE_VB_MAGIN
                + Metrics.LIFELINE_NAME_H_MARGIN + Metrics.FRAME_NAME_H_MARGIN + Metrics.getLifelineFontHeigth() * 2;
    }

    /**
     * Returns the graph node which contains the point given in parameter for the given graph node list and starting the
     * iteration at the given index<br>
     * WARNING: Only graph nodes with smaller coordinates than the current visible area can be returned.<br>
     * 
     * @param x the x coordinate of the point to test
     * @param y the y coordinate of the point to test
     * @param list the list to search in
     * @param fromIndex list browsing starting point
     * @return the graph node containing the point given in parameter, null otherwise
     */
    @Override
    protected GraphNode getNodeFromListAt(int x, int y, List<GraphNode> list, int fromIndex) {
        if (list == null)
            return null;
        for (int i = fromIndex; i < list.size(); i++) {
            GraphNode node = (GraphNode) list.get(i);
            // only lifeline list is x ordered
            // Stop browsing the list if the node is outside the visible area
            // all others nodes will be not visible
            if ((node instanceof Lifeline) && (node.getX() > visibleAreaX + visibleAreaWidth))
                break;
            if (node.getHeight() < 0) {
                if (node.getY() + node.getHeight() > visibleAreaY + visibleAreaHeight)
                    break;
            } else {
                if (node.getY() > visibleAreaY + visibleAreaHeight)
                    break;
            }
            if (node.contains(x, y))
                return node;
        }
        return null;
    }

    /**
     * Draw the Frame rectangle
     * 
     * @param context the context to draw to
     */
    protected void drawFrame(IGC context) {
        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_FRAME));

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        // Draw the frame main rectangle
        context.fillRectangle(x, y, w, h);
        context.drawRectangle(x, y, w, h);

        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME_NAME));
        context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_FRAME_NAME));
        context.setFont(Frame.getUserPref().getFont(ISDPreferences.PREF_FRAME_NAME));

        int nameWidth = context.textExtent(getName()) + 2 * Metrics.FRAME_NAME_V_MARGIN;
        int nameHeight = Metrics.getFrameFontHeigth() + +Metrics.FRAME_NAME_H_MARGIN * 2;

        // Draw the frame name area
        if (nameWidth > w)
            nameWidth = w;

        int[] points = { x, y, x + nameWidth, y, x + nameWidth, y - 11 + nameHeight, x - 11 + nameWidth, y + nameHeight, x, y + nameHeight, x, y + nameHeight };
        context.fillPolygon(points);
        context.drawPolygon(points);
        context.drawLine(x, y, x, y + nameHeight);

        context.setForeground(Frame.getUserPref().getFontColor(ISDPreferences.PREF_FRAME_NAME));
        context.drawTextTruncatedCentred(getName(), x, y, nameWidth - 11, nameHeight, false);

        context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_FRAME));
        context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_FRAME));
    }

    /**
     * Draws the Frame on the given context.<br>
     * This method start width GraphNodes ordering if needed.<br>
     * After, depending on the visible area, only visible GraphNodes are drawn.<br>
     * 
     * @param context the context to draw to
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(IGC)
     */
    @Override
    public void draw(IGC context) {
        draw(context, true);
    }

    /**
     * Draws the Frame on the given context.<br>
     * This method start width GraphNodes ordering if needed.<br>
     * After, depending on the visible area, only visible GraphNodes are drawn.<br>
     * 
     * @param context the context to draw to
     * @param drawFrame indicate if the frame rectangle need to be redrawn
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#draw(IGC)
     */
    protected void draw(IGC context, boolean drawFrame) {
        visibleAreaHeight = context.getVisibleHeight();
        visibleAreaWidth = context.getVisibleWidth();
        visibleAreaX = context.getContentsX();
        visibleAreaY = context.getContentsY();

        if (forceEventOccurrenceSpacing >= 0)
            Metrics.setForcedEventSpacing(forceEventOccurrenceSpacing);
        else
            Metrics.setForcedEventSpacing(-1);
        if (userPref == null)
            return;
        super.drawChildenNodes(context);
    }

    public static void setUserPref(ISDPreferences pref) {
        userPref = pref;
    }

    public static ISDPreferences getUserPref() {
        return userPref;
    }

    public void forceEventOccurrenceSpacing(int space) {
        forceEventOccurrenceSpacing = space;
    }

    /**
     * Return the X coordinates of the frame visible area
     * 
     * @return the X coordinates of the frame visible area
     */
    public int getVisibleAreaX() {
        return visibleAreaX;
    }

    /**
     * Return the frame visible area width
     * 
     * @return the frame visible area width
     */
    public int getVisibleAreaWidth() {
        return visibleAreaWidth;
    }

    /**
     * Return the frame visible area height
     * 
     * @return the frame visible area height
     */
    public int getVisibleAreaHeight() {
        return visibleAreaHeight;
    }

    /**
     * Return the X coordinates of the frame visible area
     * 
     * @return the X coordinates of the frame visible area
     */
    public int getVisibleAreaY() {
        return visibleAreaY;
    }

    /**
     * Return the minimum time stored in the frame taking all GraphNodes into account
     * 
     * @return the minimum GraphNode time
     */
    public TmfTimestamp getMinTime() {
        if (lastExternalTimePref != SDViewPref.getInstance().excludeExternalTime()) {
            lastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();
            computeMinMax = true;
        }
        if ((computeMinMax) && (!customMinMax)) {
            computeMinMax();
            computeMinMax = false;
        }
        return minTime;
    }

    public void setMin(TmfTimestamp min) {
        minTime = min;
        customMinMax = true;
    }

    public void setMax(TmfTimestamp max) {
        maxTime = max;
        customMinMax = true;
    }

    public void resetCustomMinMax() {
        customMinMax = false;
        computeMinMax = true;
    }

    /**
     * Return the maximum time stored in the frame taking all GraphNodes into account
     * 
     * @return the maximum GraphNode time
     */
    public TmfTimestamp getMaxTime() {
        if (lastExternalTimePref != SDViewPref.getInstance().excludeExternalTime()) {
            lastExternalTimePref = SDViewPref.getInstance().excludeExternalTime();
            computeMinMax = true;
        }
        if (computeMinMax) {
            computeMinMax();
            computeMinMax = false;
        }
        return maxTime;
    }

    protected void computeMaxMinTime() {
        if (!initSDMin)
            return;

        List<SDTimeEvent> timeArray = buildTimeArray();
        if (timeArray == null)
            return;
        for (int i = 0; i < timeArray.size(); i++) {
            SDTimeEvent m = (SDTimeEvent) timeArray.get(i);

            if (m.getTime().compareTo(maxSDTime, true) > 0) {
                maxSDTime = m.getTime();
            }

            if ((m.getTime().compareTo(minSDTime, true) < 0) || (initSDMin == true)) {
                minSDTime = m.getTime();
                initSDMin = false;
            }
        }
    }

    public TmfTimestamp getSDMinTime() {
        computeMaxMinTime();
        return minSDTime;
    }

    public TmfTimestamp getSDMaxTime() {
        computeMaxMinTime();
        return maxSDTime;
    }

    /**
     * Browse all the GraphNode to compute the min and max times store in the Frame
     */
    protected void computeMinMax() {
        List<SDTimeEvent> timeArray = buildTimeArray();
        if (timeArray == null)
            return;
        for (int i = 0; i < timeArray.size() - 1; i++) {
            SDTimeEvent m1 = (SDTimeEvent) timeArray.get(i);
            SDTimeEvent m2 = (SDTimeEvent) timeArray.get(i + 1);
          
            updateMinMax(m1, m2);
            
        }
    }

    protected void updateMinMax(SDTimeEvent m1, SDTimeEvent m2) {
        TmfTimestamp delta = m2.getTime().getDelta(m1.getTime());
        if (computeMinMax) {
            minTime = delta.clone();
            if (minTime.compareTo(TmfTimestamp.Zero, false) < 0) {
                minTime = new TmfTimestamp(0, m1.getTime().getScale(), m1.getTime().getPrecision());
            }
            maxTime = minTime.clone();
            computeMinMax = false;
        }

        if ((delta.compareTo(minTime, true) < 0) && (delta.compareTo(TmfTimestamp.Zero, false) > 0)) {
            minTime = delta.clone();
        }

        if ((delta.compareTo(maxTime, true) > 0) && (delta.compareTo(TmfTimestamp.Zero, false) > 0)) {
            maxTime = delta.clone();
        }
    }

    protected List<SDTimeEvent> buildTimeArray() {
        if (!hasChilden)
            return null;

        Iterator<String> it = fSort.keySet().iterator();
        List<SDTimeEvent> timeArray = new ArrayList<SDTimeEvent>();
        while (it.hasNext()) {
            String nodeType = it.next();
            List<GraphNode> list = (List<GraphNode>) nodes.get(nodeType);
            for (int i = 0; i < list.size(); i++) {
                Object timedNode = list.get(i);
                if ((timedNode instanceof ITimeRange) && ((ITimeRange) timedNode).hasTimeInfo()) {
                    int event = ((GraphNode) list.get(i)).getStartOccurrence();
                    TmfTimestamp time = ((ITimeRange) list.get(i)).getStartTime();
                    SDTimeEvent f = new SDTimeEvent(time, event, (ITimeRange) list.get(i));
                    timeArray.add(f);
                    if (event != ((GraphNode) list.get(i)).getEndOccurrence()) {
                        event = ((AsyncMessage) list.get(i)).getEndOccurrence();
                        time = ((ITimeRange) list.get(i)).getEndTime();
                        f = new SDTimeEvent(time, event, (ITimeRange) list.get(i));
                        timeArray.add(f);
                    }
                }
            }
        }
        return timeArray;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#contains(int, int)
     */
    @Override
    public boolean contains(int x, int y) {
        return false;
    }
}
