/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.internal.GraphingAPIUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.preferences.GraphingAPIPreferenceConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class GraphPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GraphPreferencePage() {
		super(GRID);
		setPreferenceStore(GraphingAPIUIPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("GraphPreferencePage.GraphDisplayPreferences"));
	}
	
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(
				GraphingAPIPreferenceConstants.P_SHOW_GRID_LINES, 
				Localization.getString("GraphPreferencePage.ShowGridLines"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_MAX_DATA_ITEMS,
				Localization.getString("GraphPreferencePage.MaxDataItems"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_VIEWABLE_DATA_ITEMS,
				Localization.getString("GraphPreferencePage.ViewableDataItems"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_X_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.XSeriesTicks"),
				getFieldEditorParent()));

		addField(
				new IntegerFieldEditor(
				GraphingAPIPreferenceConstants.P_Y_SERIES_TICKS,
				Localization.getString("GraphPreferencePage.YSeriesTicks"),
				getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {}
	
	public void dispose() {
		super.dispose();
	}
}

