/*******************************************************************************
 * Copyright (c) 2008 Alexander Kurtakov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.resolutions;

/**
 * Quick fix for the hardcoded-packager-tag warning.
 * This is as simple as removing the line.
 *
 */
public class HardcodedPackagerTag extends ARemoveLineResolution {
	/**
	 * Rpmlint warning id.
	 */
	public static final String ID = "hardcoded-packager-tag"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	public String getDescription() {
		return Messages.HardcodedPackagerTag_0;
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return ID;
	}
}
