/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;


import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.valgrind.ui.CoreMessagesViewer;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;

public class ExpandCollapseTest extends AbstractHelgrindTest {
	
	protected CoreMessagesViewer viewer;
	protected Menu contextMenu;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	
	public void testExpand() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testHelgrindGeneric"); //$NON-NLS-1$
		
		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		viewer = view.getMessagesViewer();
		contextMenu = viewer.getTreeViewer().getTree().getMenu();
		
		// Select first error and expand it
		IValgrindMessage[] messages = (IValgrindMessage[]) viewer.getTreeViewer().getInput();
		IValgrindMessage element = messages[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { element }));
		viewer.getTreeViewer().setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(0).notifyListeners(SWT.Selection, null);
		
		checkExpanded(element, true);
	}
	
	public void testCollapse() throws Exception {
		// Expand the element first
		testExpand();
		
		// Then collapse it
		IValgrindMessage[] messages = (IValgrindMessage[]) viewer.getTreeViewer().getInput();
		IValgrindMessage element = messages[0];
		TreeSelection selection = new TreeSelection(new TreePath(new Object[] { element }));
		viewer.getTreeViewer().setSelection(selection);
		contextMenu.notifyListeners(SWT.Show, null);
		contextMenu.getItem(1).notifyListeners(SWT.Selection, null);
		
		checkExpanded(element, false);
	}

	private void checkExpanded(IValgrindMessage element, boolean expanded) {
		if (element.getChildren().length > 0) {
			// only applicable to internal nodes
			if (expanded) {
				assertTrue(viewer.getTreeViewer().getExpandedState(element));
			}
			else {
				assertFalse(viewer.getTreeViewer().getExpandedState(element));
			}
		}
		for (IValgrindMessage child : element.getChildren()) {
			checkExpanded(child, expanded);
		}
	}
}
