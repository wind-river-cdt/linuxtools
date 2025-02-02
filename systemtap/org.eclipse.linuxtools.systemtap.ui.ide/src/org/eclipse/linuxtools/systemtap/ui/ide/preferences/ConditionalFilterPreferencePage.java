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

package org.eclipse.linuxtools.systemtap.ui.ide.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.IDEPlugin;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.ui.ListEditor;
import org.eclipse.linuxtools.systemtap.ui.structures.validators.ConditionalExpressionValidator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;



public class ConditionalFilterPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public ConditionalFilterPreferencePage() {
		super(GRID);
		LogManager.logDebug("Start ConditionalFilterPreferencePage:", this);
		setPreferenceStore(IDEPlugin.getDefault().getPreferenceStore());
		setDescription(Localization.getString("ConditionalFilterPreferencePage.ConditiionalFilterSelector"));
		LogManager.logDebug("End ConditionalFilterPreferencePage:", this);
	}
	
	public void createFieldEditors() {
		LogManager.logDebug("Start createFieldEditors:", this);
		
		addField(new ListEditor(IDEPreferenceConstants.P_CONDITIONAL_FILTERS,
				Localization.getString("ConditionalFilterPreferencePage.ConditionalFilters"), Localization.getString("ConditionalFilterPreferencePage.NewFilter"), "if()", new ConditionalExpressionValidator(), getFieldEditorParent()));
		LogManager.logDebug("End createFieldEditors:", this);
	}

	public void init(IWorkbench workbench) {
		LogManager.logInfo("Initializing", this);
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
	}
}
