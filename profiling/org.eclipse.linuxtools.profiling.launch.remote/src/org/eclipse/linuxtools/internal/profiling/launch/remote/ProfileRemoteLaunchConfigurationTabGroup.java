/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.profiling.launch.remote;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

public abstract class ProfileRemoteLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new CMainTab(true));
		tabs.add(new RemoteArgumentsTab());
		
		tabs.addAll(Arrays.asList(getProfileTabs()));
		
		tabs.add(new EnvironmentTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());
		
		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}
	
	public abstract AbstractLaunchConfigurationTab[] getProfileTabs();

}
