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


public interface IRPMConstants {

	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>rpm</code> binary.
	 */
	public static final String RPM_CMD = "RPM_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>rpmbuild</code> binary.
	 */
	public static final String RPMBUILD_CMD = "RPMBUILD_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the path to the system's <code>diff</code> binary.
	 */
	public static final String DIFF_CMD = "DIFF_CMD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the name of the RPM log viewer.
	 */
	public static final String RPM_DISPLAYED_LOG_NAME = "RPM_DISPLAYED_LOG_NAME"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the preference store key for storing and retrieving
	 * the name of the RPM log.
	 */
	public static final String RPM_LOG_NAME = "RPM_LOG_NAME"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default RPMS folder in an RPM project.
	 */
	public static final String RPMS_FOLDER = "RPMS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SRPMS folder in an RPM project.
	 */
	public static final String SRPMS_FOLDER = "SRPMS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SPECS folder in an RPM project.
	 */
	public static final String SPECS_FOLDER = "SPECS"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default SOURCES folder in an RPM project.
	 */
	public static final String SOURCES_FOLDER = "SOURCES"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the default BUILD folder in an RPM project.
	 */
	public static final String BUILD_FOLDER = "BUILD"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store the project-relative
	 * path of an RPM project's source RPM.
	 */
	public static final String SRPM_PROPERTY = "SRPM_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store the project-relative
	 * path of an RPM project's spec file.
	 */
	public static final String SPEC_FILE_PROPERTY = "SPEC_FILE_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the name of the project property used to store an RPM project's
	 * checksum value.
	 */
	public static final String CHECKSUM_PROPERTY = "CHECKSUM_PROPERTY"; //$NON-NLS-1$
	
	/**
	 * Contains the system's file separator.
	 */
	public static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Contains the system's line separator.
	 */
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	
	/**
	 * Contains the plug-ins default error message.
	 */
	public static final String ERROR = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	
}
