/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: EllipsisisMessage.java,v 1.3 2008/01/24 02:29:19 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IColor;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;

public class EllipsisisMessage extends AsyncMessage implements ITimeRange {

    @Override
    public int getX() {
        if (startLifeline == null)
            return super.getX() + super.getWidth() - 16;
        else
            return super.getX();
    }

    @Override
    public int getY() {
        return super.getY() + 3;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    protected void drawMessage(IGC context) {
        // temporay store the coordinates to avoid more methods calls
        int x = super.getX();
        int y = getY();
        int width = super.getWidth();
        int height = getHeight();

        // UML2 found message (always drawn from left to right)
        if (startLifeline == null && endLifeline != null) {
            // Draw the message label above the message and centered
            // The label is truncated if it cannot fit between the two message end
            // 2*Metrics.MESSAGES_NAME_SPACING = space above the label + space below the label
            context.drawTextTruncatedCentred(getName(), x, y - Metrics.getMessageFontHeigth() - 2 * Metrics.MESSAGES_NAME_SPACING, width, 2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth(), !isSelected());

            int currentStyle = context.getLineStyle();
            context.setLineStyle(context.getLineSolidStyle());
            // Draw the message main line
            context.drawRectangle(x + width - 5, y, x + width - 6, y + height);
            context.drawRectangle(x + width - 10, y, x + width - 11, y + height);
            context.drawRectangle(x + width - 15, y, x + width - 16, y + height);
            context.setLineStyle(currentStyle);

            IColor storedColor = context.getBackground();
            context.setBackground(context.getForeground());
            context.fillRectangle(x + width - 5, y, x + width - 6, y + height);
            context.fillRectangle(x + width - 10, y, x + width - 11, y + height);
            context.fillRectangle(x + width - 15, y, x + width - 16, y + height);
            context.setBackground(storedColor);
        }
        // UML2 lost message (always drawn from left to right)
        else if (endLifeline == null && startLifeline != null) {
            // Draw the message label above the message and centered
            // The label is truncated if it cannot fit between the two message end
            // 2*Metrics.MESSAGES_NAME_SPACING = space above the label + space below the label
            context.drawTextTruncatedCentred(getName(), x, y - Metrics.getMessageFontHeigth() - 2 * Metrics.MESSAGES_NAME_SPACING, width, 2 * Metrics.MESSAGES_NAME_SPACING + Metrics.getMessageFontHeigth(), !isSelected());

            int currentStyle = context.getLineStyle();
            context.setLineStyle(context.getLineSolidStyle());
            // Draw the message main line
            context.drawRectangle(x + 5, y, 1, 1);
            context.drawRectangle(x + 10, y, 1, 1);
            context.drawRectangle(x + 15, y, 1, 1);

            context.setLineStyle(currentStyle);

            IColor storedColor = context.getBackground();
            context.setBackground(context.getForeground());
            context.fillRectangle(x + 5, y, 1, 1);
            context.fillRectangle(x + 10, y, 1, 1);
            context.fillRectangle(x + 15, y, 1, 1);

            context.setBackground(storedColor);

        } else
            super.draw(context);
    }

    @Override
    public void draw(IGC context) {
        if (!isVisible())
            return;
        // Draw it selected?*/
        if (isSelected()) {
            /*
             * Draw it twice First time, bigger inverting selection colors Second time, regular drawing using selection
             * colors This create the highlight effect
             */
            context.setForeground(Frame.getUserPref().getBackGroundColorSelection());
            context.setLineWidth(Metrics.SELECTION_LINE_WIDTH);
            drawMessage(context);
            context.setBackground(Frame.getUserPref().getBackGroundColorSelection());
            context.setForeground(Frame.getUserPref().getForeGroundColorSelection());
            // Second drawing is done after the else
        } else {
            context.setBackground(Frame.getUserPref().getBackGroundColor(ISDPreferences.PREF_ASYNC_MESS));
            context.setForeground(Frame.getUserPref().getForeGroundColor(ISDPreferences.PREF_ASYNC_MESS));
        }
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(true);
        }
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        drawMessage(context);
        context.setLineWidth(Metrics.NORMAL_LINE_WIDTH);
        if (hasFocus()) {
            context.setDrawTextWithFocusStyle(false);
        }
    }
}
