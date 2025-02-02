/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

public class SourceRPM {
    
	private IFile sourceRPM;
	private IFolder sourcesFolder;
	
	public SourceRPM(IFile sourceRPM) {
		this.sourceRPM = sourceRPM;
	}
	
	public IFile getFile() {
		return sourceRPM;
	}
	
	public IFolder getSourcesFolder() {
		return sourcesFolder;
	}
	
	public void setSourcesFolder(IFolder sourcesFolder) {
		this.sourcesFolder = sourcesFolder;
	}
}
