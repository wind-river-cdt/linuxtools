/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: Stop.java,v 1.2 2006/09/20 20:56:25 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;

/**
 * It is the UML2 stop graphical representation in the sequence diagram viewer.<br>
 * This draw a cross on the lifeline. The stop y coordinate depend on the event occurrence when it appears.<br>
 * A stop is never drawn it is assigned to a lifeline.<br>
 * <br>
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Lifeline#setStop(Stop)
 * @author sveyrier
 * 
 */
public class Stop extends GraphNode {

    /**
     * The owning lifeline on which the stop appears
     */
    protected Lifeline lifeline = null;

    /**
     * The graphNode ID
     */
    public static final String STOP = "STOP"; //$NON-NLS-1$

    /**
     * This basically represents the time when the stop occurs on the owning Lifeline
     * 
     * @see Lifeline Lifeline for more event occurence details
     */
    protected int eventOccurrence = 0;

    @Override
    public int getX() {
        if (lifeline == null)
            return 0;
        return lifeline.getX() + Metrics.getLifelineWidth() / 2 - Metrics.STOP_WIDTH / 2;
    }

    @Override
    public int getY() {
        if (lifeline == null)
            return 0;
        return lifeline.getY() + lifeline.getHeight() + (Metrics.getMessageFontHeigth() + Metrics.getMessagesSpacing()) * eventOccurrence - Metrics.STOP_WIDTH / 2;
    }

    @Override
    public int getWidth() {
        if (lifeline == null)
            return 0;
        return Metrics.STOP_WIDTH;
    }

    @Override
    public int getHeight() {
        if (lifeline == null)
            return 0;
        return Metrics.STOP_WIDTH;
    }

    /**
     * Set the lifeline on which the stop must be draw
     * 
     * @param theLifeline The the stop owing lifeline
     */
    public void setLifeline(Lifeline theLifeline) {
        lifeline = theLifeline;
    }

    /**
     * Set the event occurrence when this stop appears
     * 
     * @param occurrence the eventOccurence to assign to the stop
     */
    public void setEventOccurrence(int occurrence) {
        eventOccurrence = occurrence;
    }

    @Override
    public void draw(IGC context) {
        // Set the appropriate color depending if the graph node if selected or not
        if (lifeline.isSelected()) {
            context.setForeground(Frame.getUserPref().getBackGroundColorSelection());
            context.setLineWidth(Metrics.SELECTION_LINE_WIDTH);
            int lastWidth = context.getLineWidth();
            context.setLineWidth(9);
            // Draw a cross on the lifeline
            context.drawLine(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            context.drawLine(getX() + getWidth(), getY(), getX(), getY() + getHeight());
            // restore the context
            context.setLineWidth(lastWidth);
            context.setBackground(Frame.getUserPref().getBackGroundColorSelection());
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
        } else {
            context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_LIFELINE));
            context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_LIFELINE));
        }
        int lastWidth = context.getLineWidth();
        context.setLineWidth(3);
        // Draw a cross on the lifeline
        context.drawLine(getX(), getY(), getX() + getWidth(), getY() + getHeight());
        context.drawLine(getX() + getWidth(), getY(), getX(), getY() + getHeight());
        // restore the context
        context.setLineWidth(lastWidth);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#getArrayId()
     */
    @Override
    public String getArrayId() {
        return STOP;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode#contains(int, int)
     */
    @Override
    public boolean contains(int x, int y) {
        // TODO Auto-generated method stub
        return false;
    }
}
