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

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.structures.ZipArchive;
import org.eclipse.linuxtools.systemtap.ui.systemtapgui.SystemTapGUISettings;

/**
 * This class is responsible for searching through the Dashboard module
 * directory and building a tree with their modules.
 * @author Ryan Morse
 */
public class DashboardGraphsTreeBuilder {
	public DashboardGraphsTreeBuilder() {
		tree = new TreeNode("Root", "", false);
	}
	
	public DashboardGraphsTreeBuilder(TreeNode t) {
		tree = t;
	}
	
	/**
	 * Returns the tree containing all of the dashboard modules
	 * @return TreeNode with dashboard modules
	 */
	public TreeNode getTree() {
		return tree;
	}

	/**
	 * This method is used to generate the tree of dashboard modules for
	 * the provided folder.
	 * @param folder The directory to search for dashboard modules
	 */
	public void generateTree(File folder) {
		scanNextLevel(folder);
	}
	
	/**
	 * This is the main method for this class. It searches through the provided
	 * file/folder and builds the tree with any dashboard modules it finds.
	 * @param f The file/folder to scan for modules
	 */
	private void scanNextLevel(File f) {
		File[] fs = f.listFiles(new DashboardModuleFileFilter());
		DashboardMetaData dgd;
		DashboardModule dg;
		
		TreeNode location;
		for(int i=0; i<fs.length; i++) {
			if(fs[i].isDirectory())
				scanNextLevel(fs[i]);
			else {
				try {
					File folder = new File(SystemTapGUISettings.tempDirectory + "/bundles/");
					if(!folder.exists())
						folder.mkdirs();
					
					File file = new File(folder + "/" + fs[i].getName() + ".tmp");
					file.createNewFile();
					ZipArchive.uncompressFile(file.getAbsolutePath(), fs[i].getAbsolutePath());
					ZipArchive.unzipFiles(file.getAbsolutePath(), folder.getAbsolutePath());
					dgd = new DashboardMetaData(folder.getAbsolutePath() + DashboardModule.metaFileName);
					dg = dgd.getModule();
					dg.archiveFile = fs[i];
					location = findInsertLocation(dg.category);
					location.add(new ModuleTreeNode(dg, dg.display, true));
					
					File[] files = folder.listFiles();
					for(int j=0; j<files.length; j++)
						files[j].delete();
					folder.delete();
				} catch(IOException ioe) {}
			}
		}
	}
	
	/**
	 * This method searches through the tree to find the correct location
	 * to add the new module.  If the path does not exist yet, a new node will
	 * be created.
	 * @param path The category the module should be added to
	 * @return The tree node matching the provided path
	 */
	private TreeNode findInsertLocation(String path) {
		String[] folders = path.split("\\p{Punct}");
		TreeNode level = tree;
		
		for(int j,i=0; i<folders.length; i++) {
			for(j=0; j<level.getChildCount(); j++) {
				if(level.getChildAt(j).toString().equals(folders[i]))
					break;
			}
			if(j >= level.getChildCount())
				level.add(new TreeNode("", folders[i], false));
			level = level.getChildAt(j);
		}
		return level;
	}
	
	private TreeNode tree;
}
