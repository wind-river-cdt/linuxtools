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

package org.eclipse.linuxtools.tmf.ui.viewers.events;

import org.eclipse.linuxtools.tmf.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

public interface ITmfEventsFilterListener {

	public void filterApplied(ITmfFilter filter, ITmfTrace<?> trace);
	
	public void searchApplied(ITmfFilter filter, ITmfTrace<?> trace);
	
}
