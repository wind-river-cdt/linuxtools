/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.rpm.rpmlint.Activator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for setting rpmlint specific preferences.
 *
 */
public class RpmlintPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	/**
	 * default constructor
	 */
	public RpmlintPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		FileFieldEditor rpmlintFileFieldEditor = new FileFieldEditor(
				PreferenceConstants.P_RPMLINT_PATH, Messages.RpmlintPreferencePage_0,
				getFieldEditorParent());
		addField(rpmlintFileFieldEditor);
		RadioGroupFieldEditor spacesAndTabsRadioGroup = new RadioGroupFieldEditor(
				PreferenceConstants.P_RPMLINT_TABS_AND_SPACES,
				Messages.RpmlintPreferencePage_1, 1, new String[][] {
						{ Messages.RpmlintPreferencePage_2,
								PreferenceConstants.P_RPMLINT_SPACES },
						{ Messages.RpmlintPreferencePage_3,
								PreferenceConstants.P_RPMLINT_TABS } },
				getFieldEditorParent(), true);
		addField(spacesAndTabsRadioGroup);

	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// Do nothing
	}

}
