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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

public class RPMBuildConfiguration implements IProjectConfiguration {
	
	private IProject project;
	
	private IFolder rpmsFolder;
    private IFolder srpmsFolder;
    private IFolder specsFolder;
    private IFolder sourcesFolder;
    private IFolder buildFolder;
	
	public RPMBuildConfiguration(IProject project) throws CoreException {
		this.project = project;
		initialize();
	}
	
	/**
	 * Sets the internal folder fields according to stored properties
	 * in the workspace project, or according to the default properties
	 * if no stored properties are found.  If the folders do not exist,
	 * they are created.
	 * @throws CoreException if:
	 * <ul>
	 * <li>Getting or setting project properties fails</ul>
	 * <li>Creating project folders fails</li>
	 * </ul>
	 */
    private void initialize() throws CoreException {
		String pluginID = RPMCorePlugin.ID;
        
		String sourcesPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SOURCES_FOLDER));
		if(sourcesPath == null) {
			sourcesFolder = project.getFolder(IRPMConstants.SOURCES_FOLDER);
			if(!sourcesFolder.exists()) {
				sourcesFolder.create(false, true, null);
			}
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SOURCES_FOLDER), 
	                sourcesFolder.getName());
        } else {
			sourcesFolder = project.getFolder(sourcesPath);
			if(!sourcesFolder.exists()) {
				sourcesFolder.create(false, true, null);
			}
        }
		
		String srcRpmPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SRPMS_FOLDER));
        if(srcRpmPath == null) {
			srpmsFolder = project.getFolder(IRPMConstants.SRPMS_FOLDER);
			if(!srpmsFolder.exists()) {
				srpmsFolder.create(false, true, null);
			}
			srpmsFolder.setDerived(true, new NullProgressMonitor());
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SRPMS_FOLDER),
					srpmsFolder.getName());
        } else {
			srpmsFolder = project.getFolder(srcRpmPath);
			if(!srpmsFolder.exists()) {
				srpmsFolder.create(false, true, null);
			}
        }
		
		String buildPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.BUILD_FOLDER));
        if(buildPath == null) {
            buildFolder = project.getFolder(IRPMConstants.BUILD_FOLDER);
            if(!buildFolder.exists()) {
				buildFolder.create(false, true, null);
			}
			buildFolder.setDerived(true, new NullProgressMonitor());
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.BUILD_FOLDER), 
					buildFolder.getName());
        } else {
			buildFolder = project.getFolder(buildPath);
			if(!buildFolder.exists()) {
				buildFolder.create(false, true, null);
			}
        }
		
		String rpmPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.RPMS_FOLDER));
        if(rpmPath == null) {
			rpmsFolder = project.getFolder(IRPMConstants.RPMS_FOLDER);
			if(!rpmsFolder.exists()) {
				rpmsFolder.create(false, true, null);
			}
			rpmsFolder.setDerived(true, new NullProgressMonitor());
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.RPMS_FOLDER), 
	                rpmsFolder.getName());
        } else {
			rpmsFolder = project.getFolder(rpmPath);
			if(!rpmsFolder.exists()) {
				rpmsFolder.create(false, true, null);
			}
        }
		
		String specPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SPECS_FOLDER));
        if(specPath == null) {
            specsFolder = project.getFolder(IRPMConstants.SPECS_FOLDER);
            if(!specsFolder.exists()) {
				specsFolder.create(false, true, null);
			}
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SPECS_FOLDER),
					specsFolder.getName());
        } else {
			specsFolder = project.getFolder(specPath);
			if(!specsFolder.exists()) {
				specsFolder.create(false, true, null);
			}
        }
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.core.IProjectConfiguration#getBuildFolder()
	 */
	public IFolder getBuildFolder() {
		return buildFolder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.core.IProjectConfiguration#getRpmsFolder()
	 */
	public IFolder getRpmsFolder() {
		return rpmsFolder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.core.IProjectConfiguration#getSourcesFolder()
	 */
	public IFolder getSourcesFolder() {
		return sourcesFolder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.core.IProjectConfiguration#getSpecsFolder()
	 */
	public IFolder getSpecsFolder() {
		return specsFolder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.rpm.core.IProjectConfiguration#getSrpmsFolder()
	 */
	public IFolder getSrpmsFolder() {
		return srpmsFolder;
	}

}
