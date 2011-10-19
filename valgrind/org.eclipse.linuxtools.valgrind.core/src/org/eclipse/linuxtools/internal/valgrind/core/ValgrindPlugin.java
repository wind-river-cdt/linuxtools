/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.core;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ValgrindPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = PluginConstants.CORE_PLUGIN_ID;

	// The shared instance
	private static ValgrindPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ValgrindPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ValgrindPlugin getDefault() {
		return plugin;
	}
	
	
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		ValgrindCommand valCommand = new ValgrindCommand();
		try {
			store.setDefault(ValgrindPreferencePage.VALGRIND_ENABLE, true);
			if(System.getProperty("os.name").toLowerCase().startsWith("windows")) //$NON-NLS-1$ //$NON-NLS-2$
				store.setDefault(ValgrindPreferencePage.VALGRIND_PATH, "");
			else
				store.setDefault(ValgrindPreferencePage.VALGRIND_PATH, valCommand.whichValgrind());
		} catch (IOException e) {
			// No Valgrind installed, make disabled by default
			store.setDefault(ValgrindPreferencePage.VALGRIND_ENABLE, false);
		}
	}

}
