/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.editors.automake;


import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class CompletionProposalComparator implements Comparator<ICompletionProposal>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for CompletionProposalComparator.
	 */
	public CompletionProposalComparator() {
	}
	
	/* (non-Javadoc)
	 * @see Comparator#compare(Object, Object)
	 */
	public int compare(ICompletionProposal o1, ICompletionProposal o2) {
		ICompletionProposal c1= (ICompletionProposal) o1;
		ICompletionProposal c2= (ICompletionProposal) o2;
		return c1.getDisplayString().compareToIgnoreCase(c2.getDisplayString());
	}	
	
}
