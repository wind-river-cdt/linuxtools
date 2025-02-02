/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AsyncMessageReturn.java,v 1.2 2006/09/20 20:56:25 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.IGC;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.drawings.ISDPreferences;

/**
 * The message return graph node implementation.<br>
 * This class differs on the AsynMessage class only on the drawing line style (dashed instead od plain line).<br>
 * Message return are generally associated to a message. This means, they are connected to the same lifelines than the
 * associated message but in the opposite direction and for a different event occurrence.<br>
 * <br>
 * WARNING: The association validity is not checked, it is not necessary to provide a valid association, not even needed
 * to set an association to drawn a message with a message return style.<br>
 * 
 * 
 * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessage AsyncMessage for usage example
 * @author sveyrier
 * 
 */
public class AsyncMessageReturn extends AsyncMessage {

    protected AsyncMessage message;

    public static final String ASYNC_MESS_RET_TAG = "AsyncMessageRet"; //$NON-NLS-1$

    public AsyncMessageReturn() {
        prefId = ISDPreferences.PREF_ASYNC_MESS_RET;
    }

    /**
     * Set the associated message (the message it is the return).<br>
     * Setting the association will activate the navigation in the default sequence diagram implementation to the
     * message when the user right click on this message return.<br>
     * 
     * @param parentMessage the message to associate
     */
    public void setMessage(AsyncMessage parentMessage) {
        message = parentMessage;
    }

    @Override
    public void draw(IGC context) {
        if (!isVisible())
            return;
        prefId = ISDPreferences.PREF_ASYNC_MESS_RET;
        int oldStyle = context.getLineStyle();
        // Message return are dashed
        context.setLineStyle(context.getLineDotStyle());
        if (!isSelected()) {
            context.setBackground(Frame.getUserPref().getBackGroundColor(prefId));
            context.setForeground(Frame.getUserPref().getForeGroundColor(prefId));
        }
        super.draw(context);
        // restore the context
        context.setLineStyle(oldStyle);
    }

    @Override
    public String getArrayId() {
        return ASYNC_MESS_RET_TAG;
    }
}
