/*******************************************************************************
 * Copyright (c) 2008 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API. 
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.editors.stp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.linuxtools.systemtap.ui.editor.ColorManager;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.linuxtools.systemtap.ui.ide.internal.IDEPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class STPEditor extends SimpleEditor {

	private ColorManager colorManager;
	
    private ProjectionSupport stpProjectionSupport;
	private Annotation[] stpOldAnnotations;
	private ProjectionAnnotationModel stpAnnotationModel;

	public STPEditor() {
		super();
		URL completionURL = null;
	
		completionURL = buildCompletionDataLocation("completion/stp_completion.properties");
		STPMetadataSingleton completionDataStore = STPMetadataSingleton.getInstance();
		
		if (completionURL != null)
			completionDataStore.build(completionURL);

		colorManager = new ColorManager();
		setSourceViewerConfiguration(new STPConfiguration(colorManager,this));
		setDocumentProvider(new STPDocumentProvider());
	}

	public void createPartControl(Composite parent)
	{
	    super.createPartControl(parent);
	   ProjectionViewer viewer =(ProjectionViewer)getSourceViewer();
	   stpProjectionSupport = new ProjectionSupport(viewer,getAnnotationAccess(),getSharedColors());
	   stpProjectionSupport.install();
	   viewer.doOperation(ProjectionViewer.TOGGLE);
	   stpAnnotationModel = viewer.getProjectionAnnotationModel();
	}
	
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		
		ISourceViewer viewer = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);
		getSourceViewerDecorationSupport(viewer);
		return viewer;
	}

		
	public void updateFoldingStructure(ArrayList<Position> updatedPositions)
	{
		ProjectionAnnotation annotation;
		Annotation[] updatedAnnotations = new Annotation[updatedPositions.size()];
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();
		for(int i =0;i<updatedPositions.size();i++)
		{
			annotation = new ProjectionAnnotation();	
			newAnnotations.put(annotation,updatedPositions.get(i));
			updatedAnnotations[i]=annotation;
		}
		stpAnnotationModel.modifyAnnotations(stpOldAnnotations,newAnnotations,null);		
		stpOldAnnotations = updatedAnnotations;
	}
	
	protected void createActions() {
		Action action = new ContentAssistAction(ResourceBundle.getBundle("org.eclipse.linuxtools.systemtap.ui.ide.editors.stp.strings"), "ContentAssistProposal.", this); 
		String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
		action.setActionDefinitionId(id);
		setAction("ContentAssistProposal", action); 
		markAsStateDependentAction("ContentAssistProposal", true);
		super.createActions();
	}
	
	public ISourceViewer getMySourceViewer() {
		return this.getSourceViewer();
	}
	
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu) {

		super.editorContextMenuAboutToShow(menu);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_RIGHT);
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT,
				ITextEditorActionConstants.SHIFT_LEFT);

	}
	
	private URL buildCompletionDataLocation(String completionDataLocation) {
		URL completionURLLocation = null; 
		try {
			completionURLLocation = getCompletionURL(completionDataLocation);			
		} catch (IOException e) {
			completionURLLocation = null;
		}
		
		if (completionURLLocation == null) {
			IDEPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID, 
					IStatus.OK, "Cannot locate plug-in location for System Tap completion metadata " +
							"(completion/stp_completion.properties). Completions are not available.", null));
			return null;
		} 
		
		File completionFile = new File(completionURLLocation.getFile());
		if ((completionFile == null) || (!completionFile.exists()) || (!completionFile.canRead())) {
			IDEPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID, 
					IStatus.OK, "Cannot find System Tap completion metadata at  " +completionFile.getPath() + 
					"Completions are not available.", null));
					
			return null;
		}

		return completionURLLocation;
		
	}
	private URL getCompletionURL(String completionLocation) throws IOException {
		URL fileURL = null;
		URL location = IDEPlugin.getDefault().getBundle().getEntry(completionLocation);

		if (location != null)
			fileURL = FileLocator.toFileURL(location);		
		return fileURL;
	}
}