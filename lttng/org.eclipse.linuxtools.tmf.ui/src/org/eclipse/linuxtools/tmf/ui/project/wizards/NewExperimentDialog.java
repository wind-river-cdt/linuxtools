/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

/**
 * <b><u>NewExperimentHandler</u></b>
 * <p>
 */
public class NewExperimentDialog extends SelectionStatusDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private Text fExperimentName;
    private final IContainer fExperimentFolder;
    private TmfProjectElement fProject;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public NewExperimentDialog(Shell shell, TmfExperimentFolder experimentFolder) {
        super(shell);
        fExperimentFolder = experimentFolder.getResource();
        fProject = experimentFolder.getProject();
        setTitle(Messages.NewExperimentDialog_DialogTitle);
        setStatusLineAboveButtons(true);
    }

    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createExperimentNameGroup(composite);
        return composite;
    }

    private void createExperimentNameGroup(Composite parent) {
        Font font = parent.getFont();
        Composite folderGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        folderGroup.setLayout(layout);
        folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // New experiment label
        Label experimentLabel = new Label(folderGroup, SWT.NONE);
        experimentLabel.setFont(font);
        experimentLabel.setText(Messages.NewExperimentDialog_ExperimentName);

        // New experiment name entry field
        fExperimentName = new Text(folderGroup, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        fExperimentName.setLayoutData(data);
        fExperimentName.setFont(font);
        fExperimentName.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
            	validateNewExperimentName();
            }
        });
    }

    private void validateNewExperimentName() {
        
    	String name = fExperimentName.getText();
        IWorkspace workspace = fExperimentFolder.getWorkspace();
        IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);

        if ("".equals(name)) { //$NON-NLS-1$
        	updateStatus(new Status(IStatus.ERROR, TmfUiPlugin.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_EmptyNameError, null));
        	return;
        }
        
        if (nameStatus.isOK() == false) {
        	updateStatus(nameStatus);
        	return;
        }
        
        IPath path = new Path(name);
        if (fExperimentFolder.getFolder(path).exists() || fExperimentFolder.getFile(path).exists()) {
            updateStatus(new Status(IStatus.ERROR, TmfUiPlugin.PLUGIN_ID, IStatus.ERROR, Messages.Dialog_ExistingNameError, null));
            return;
        }

        updateStatus(new Status(IStatus.OK, TmfUiPlugin.PLUGIN_ID, "")); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // SelectionStatusDialog
    // ------------------------------------------------------------------------

    @Override
    protected void computeResult() {
    }

    @Override
    public void create() {
        super.create();
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        IFolder folder = createNewExperiment(fExperimentName.getText());
        if (folder == null) {
            return;
        }
        setSelectionResult(new IFolder[] { folder });
        super.okPressed();

        if (fProject != null) {
            fProject.refresh();
        }
    }

    private IFolder createNewExperiment(String experimentName) {

    	final IFolder experimentFolder = createExperiment(experimentName);

        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) throws CoreException {
                try {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                    experimentFolder.create(false, true, monitor);
                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(operation);
        } catch (InterruptedException exception) {
            return null;
        } catch (InvocationTargetException exception) {
            MessageDialog.openError(getShell(), "", NLS.bind("", exception.getTargetException().getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } catch (RuntimeException exception) {
            return null;
        }

        return experimentFolder;
    }

    private IFolder createExperiment(String experimentName) {
        IWorkspaceRoot workspaceRoot = fExperimentFolder.getWorkspace().getRoot();
        IPath folderPath = fExperimentFolder.getFullPath().append(experimentName);
        IFolder folder = workspaceRoot.getFolder(folderPath);

        return folder;
    }

}
