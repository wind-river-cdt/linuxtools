/**********************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.uml2sd;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

public interface ITmfAsyncSequenceDiagramEvent extends ITmfSyncSequenceDiagramEvent {
    /**
     * @return End timestamp of message (i.e. receive time)
     */
    public TmfTimestamp getEndTime();
}
