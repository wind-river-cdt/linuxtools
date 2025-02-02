/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Yuriy Vashchuk - Initial API and implementation
 *   based on Francois Chouinard ProjectView code.
 */

package org.eclipse.linuxtools.tmf.ui.views.filter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.linuxtools.tmf.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.filter.model.TmfFilterNode;
import org.eclipse.linuxtools.tmf.filter.model.TmfFilterRootNode;
import org.eclipse.linuxtools.tmf.filter.xml.TmfFilterXMLParser;
import org.eclipse.linuxtools.tmf.filter.xml.TmfFilterXMLWriter;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.internal.Messages;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.xml.sax.SAXException;

/**
 * <b><u>FilterView</u></b>
 * <p>
 * View that contain UI to the TMF filter.
 */
public class FilterView extends TmfView {

	public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.filter"; //$NON-NLS-1$

	private static final Image SAVE_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/save_button.gif"); //$NON-NLS-1$
    private static final Image ADD_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/add_button.gif"); //$NON-NLS-1$
    private static final Image DELETE_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/delete_button.gif"); //$NON-NLS-1$
    private static final Image IMPORT_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/import_button.gif"); //$NON-NLS-1$
    private static final Image EXPORT_IMAGE = TmfUiPlugin.getDefault().getImageFromPath("/icons/elcl16/export_button.gif"); //$NON-NLS-1$
    
    // ------------------------------------------------------------------------
    // Main data structures
    // ------------------------------------------------------------------------

	private FilterViewer fViewer;
    private ITmfFilterTreeNode fRoot;

    private IWorkspace fWorkspace;

    private SaveAction fSaveAction;
	private AddAction fAddAction;
	private DeleteAction fDeleteAction;
	private ExportAction fExportAction;
	private ImportAction fImportAction;

    /**
     * Getter for the Filter Tree Root
     * 
     * @return The root of builded tree
     */
    public ITmfFilterTreeNode getFilterRoot() {
    	return fRoot;
    }

    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     *  Default Constructor
     */
	public FilterView() {
		super("Filter"); //$NON-NLS-1$
		
		fWorkspace = ResourcesPlugin.getWorkspace();
		try {
            fWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        
        fRoot = new TmfFilterRootNode();
        for (ITmfFilterTreeNode node : FilterManager.getSavedFilters()) {
        	fRoot.addChild(node);
        }
	}

	
	/**
	 * Refresh the tree widget
	 */
	public void refresh() {
		fViewer.refresh();
	}
	
	/**
	 * Setter for selection
	 * 
	 * @param node The node to select
	 */
	public void setSelection(ITmfFilterTreeNode node) {
		fViewer.setSelection(node, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		fViewer = new FilterViewer(parent, SWT.NONE);
		fViewer.setInput(fRoot);
		
		contributeToActionBars();
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!(event.getSelection().isEmpty()) && event.getSelection() instanceof IStructuredSelection) {
					fDeleteAction.setEnabled(true);
					fExportAction.setEnabled(true);
				} else {
					fDeleteAction.setEnabled(false);
					fExportAction.setEnabled(false);
				}
			}
		});
	}

	
	// ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[FilterView]"; //$NON-NLS-1$
	}	

	
    /**
     * Builds the menu toolbar
     */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		//fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	
	/**
	 * Build the popup menu
	 * 
	 * @param manager The manager to build
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		
		fSaveAction = new SaveAction();
		fSaveAction.setImageDescriptor(ImageDescriptor.createFromImage(SAVE_IMAGE));
		fSaveAction.setToolTipText(Messages.FilterView_SaveActionToolTipText);
		
		fAddAction = new AddAction();
		fAddAction.setImageDescriptor(ImageDescriptor.createFromImage(ADD_IMAGE));
		fAddAction.setToolTipText(Messages.FilterView_AddActionToolTipText);	

		fDeleteAction = new DeleteAction();
		fDeleteAction.setImageDescriptor(ImageDescriptor.createFromImage(DELETE_IMAGE));
		fDeleteAction.setToolTipText(Messages.FilterView_DeleteActionToolTipText);
		fDeleteAction.setEnabled(false);

		fExportAction = new ExportAction();
		fExportAction.setImageDescriptor(ImageDescriptor.createFromImage(EXPORT_IMAGE));
		fExportAction.setToolTipText(Messages.FilterView_ExportActionToolTipText);

		fImportAction = new ImportAction();
		fImportAction.setImageDescriptor(ImageDescriptor.createFromImage(IMPORT_IMAGE));
		fImportAction.setToolTipText(Messages.FilterView_ImportActionToolTipText);

		manager.add(fSaveAction);
		manager.add(new Separator());
		manager.add(fAddAction);
		manager.add(fDeleteAction);
		manager.add(new Separator());
		manager.add(fExportAction);
		manager.add(fImportAction);
	}

	private class SaveAction extends Action {
		@Override
		public void run() {
			FilterManager.setSavedFilters(fRoot.getChildren());
		}
	}
	
	private class AddAction extends Action {
		@Override
		public void run() {
			
			TmfFilterNode newNode = new TmfFilterNode(fRoot, ""); //$NON-NLS-1$
			refresh();
			setSelection(newNode);
		}
	}
	
	private class DeleteAction extends Action {
		@Override
		public void run() {
			ITmfFilterTreeNode node = fViewer.getSelection();
			if (node != null) {
				node.remove();
			}
			refresh();
		}
	}
	
	private class ExportAction extends Action {
		@Override
		public void run() {
			try {
				FileDialog dlg = new FileDialog(new Shell(), SWT.SAVE);
				dlg.setFilterNames(new String[] {Messages.FilterView_FileDialogFilterName + " (*.filter.xml)"}); //$NON-NLS-1$
				dlg.setFilterExtensions(new String[] {"*.filter.xml"}); //$NON-NLS-1$
				
				String fn = dlg.open();
		        if (fn != null) {
					TmfFilterXMLWriter writerXML = new TmfFilterXMLWriter(fRoot);
					writerXML.saveTree(fn);
		        }
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ImportAction extends Action {
		@Override
		public void run() {
			if (fViewer != null) {
				ITmfFilterTreeNode root = null;
				try {
					FileDialog dlg = new FileDialog(new Shell(), SWT.OPEN);
					dlg.setFilterNames(new String[] {Messages.FilterView_FileDialogFilterName + " (*.filter.xml)"}); //$NON-NLS-1$
					dlg.setFilterExtensions(new String[] {"*.filter.xml"}); //$NON-NLS-1$
					
					TmfFilterXMLParser parserXML = null;
					String fn = dlg.open();
			        if (fn != null) {
			        	parserXML = new TmfFilterXMLParser(fn);
						root = parserXML.getTree();
			        }
					
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (root != null) {
    				for (ITmfFilterTreeNode node : root.getChildren()) {
    					if (node instanceof TmfFilterNode) {
    						fRoot.addChild(node);
    						refresh();
    						fViewer.setSelection(node);
    					}
    				}
				}
			}
		}
	}
	
}