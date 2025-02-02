/*******************************************************************************
 * Copyright (c) 2004, 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.autotools.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.linuxtools.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class AutotoolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String PREF_BUILD_TARGET_IN_BACKGROUND = "MakeTargetPrefs.buildTargetInBackground"; //$NON-NLS-1$
	private static final String TARGET_BUILDS_IN_BACKGROUND = "MakeTargetPreferencePage.buildTargetInBackground.label"; //$NON-NLS-1$

	public AutotoolsPreferencePage() {
		super(GRID);
		setPreferenceStore(AutotoolsPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see FieldEditorPreferencePage#createControl(Composite)
	 */
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		BooleanFieldEditor targetBackgroundEditor = new BooleanFieldEditor(PREF_BUILD_TARGET_IN_BACKGROUND,
				AutotoolsUIPlugin.getResourceString(TARGET_BUILDS_IN_BACKGROUND), parent);
		addField(targetBackgroundEditor);
	}

	public static boolean isBuildTargetInBackground() {
		return AutotoolsPlugin.getDefault().getPreferenceStore().getBoolean(PREF_BUILD_TARGET_IN_BACKGROUND);
	}

	public static void setBuildTargetInBackground(boolean enable) {
		AutotoolsPlugin.getDefault().getPreferenceStore().setValue(PREF_BUILD_TARGET_IN_BACKGROUND, enable);
	}

	/**
	 * Initializes the default values of this page in the preference bundle.
	 */
	public static void initDefaults(IPreferenceStore prefs) {
		prefs.setDefault(PREF_BUILD_TARGET_IN_BACKGROUND, true);
	}

	public void init(IWorkbench workbench) {
	}
}
