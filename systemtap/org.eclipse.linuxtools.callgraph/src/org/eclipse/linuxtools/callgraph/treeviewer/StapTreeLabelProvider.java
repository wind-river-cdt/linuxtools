/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.callgraph.treeviewer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.linuxtools.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.callgraph.StapData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class StapTreeLabelProvider implements ILabelProvider {

	@Override
	public Image getImage(Object element) {
		Image im = null;
		if ( ((StapData) element).isMarked())
			im = new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "/icons/public_co.gif"); //$NON-NLS-1$
		else
			im = new Image(Display.getCurrent(), CallGraphConstants.getPluginLocation() + "/icons/compare_method.gif"); //$NON-NLS-1$
		return im;
	}

	@Override
	public String getText(Object element) {
		return ((StapData) element).timesCalled + ": " + ((StapData) element).name; //$NON-NLS-1$
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	
	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {		
	}

}
