/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.views;


import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardModuleLocator;

/**
 * This is a basic browser view for the dashboard perspective.  It contains a list
 * of all of the available dashboard modules.
 * @author Ryan Morse
 */
public class DashboardModuleBrowserView extends ModuleView {
	public DashboardModuleBrowserView() {
		super();
		LogManager.logInfo("Initializing", this);
	}
	
	/**
	 * This method sends requests to get all of the modules that are
	 * avialable on the system.  Once then are found, it will
	 * set the viewer's content to the tree of modules that were found.
	 */
	protected void generateModuleTree() {
		TreeNode modules = DashboardModuleLocator.getModules();
		
		if(null != modules)
			viewer.setInput(modules);
		else
			viewer.setInput(new TreeNode("", false));
	
	}
	
	/**
	 * This method builds the actions for the items inside this view.  It adds a
	 * double click listener to each of the Items so they will be run if they
	 * are actual modules.  It also sets up the layout for popup menu when users
	 * right click on a module element.
	 */
	protected void makeActions() {
		dblClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				//Disabled for now, until find way to disable this like menu
				//RunModuleAction act = new RunModuleAction();
				//act.run();
			}
		};
		
		viewer.addDoubleClickListener(dblClickListener);
		
		//Gets items from plugin.xml
		MenuManager manager = new MenuManager("modulePopup");
		Control control = this.viewer.getControl();
		manager.add(new Separator("file.ext"));
		manager.add(new Separator("build.ext"));
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		Menu menu = manager.createContextMenu(control);
		control.setMenu(menu);
		getSite().registerContextMenu(manager, viewer);
		super.makeActions();
	}
	
	/**
	 * This method removes all internal references. Nothing should be called/referenced after
	 * this method is run.
	 */
	public void dispose() {
		LogManager.logInfo("disposing", this);
		viewer.removeDoubleClickListener(dblClickListener);
		super.dispose();
	}


	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView";
	private IDoubleClickListener dblClickListener;
	
}

