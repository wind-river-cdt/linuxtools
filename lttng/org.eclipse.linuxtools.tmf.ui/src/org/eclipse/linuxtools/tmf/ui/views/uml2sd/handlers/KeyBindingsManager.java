/**********************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * <b><u>KeyBindingsManager</u></b>
 * <p>
 * Singleton class that manages key-bindings for certain commands across multiple sequence 
 * diagram view instances.
 * </p>
 */
public class KeyBindingsManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    // The instance
    private final static KeyBindingsManager fInstance = new KeyBindingsManager();
    
    // Storage of views
    private Set<String> fViews = new HashSet<String>(); 
    
    // Activations to store
    private List<IHandlerActivation> fHandlerActivations = new Vector<IHandlerActivation>();

    // Actions for the keys bindings 
    private MoveToMessage fGoToMessageForKeyBinding;
    private OpenSDFindDialog fFindForKeyBinding;
    private MoveSDUp fMoveUpForKeyBinding;
    private MoveSDDown fMoveDownForKeyBinding;
    private MoveSDLeft fMoveLeftForKeyBinding;
    private MoveSDRight fMoveRightForKeyBinding;
    private ShowNodeStart fShowNodeStartForKeyBinding;
    private ShowNodeEnd fShowNodeEndForKeyBinding;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    private KeyBindingsManager() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    public static KeyBindingsManager getInstance() {
        return fInstance;
    }
    
    /**
     * @param viewId Id of SD view to add and to manage
     */
    public void add(String viewId) {
        
        if (fViews.size() == 0) {
            initialize();
        }
        
        if(!fViews.contains(viewId)) {
            fViews.add(viewId);
        }
    }
    
    /**
     * @param viewId Id of SD view to remove
     */
    public void remove(String viewId) {
        if (fViews.contains(viewId)) {
            fViews.remove(viewId);
        }
        if (fViews.size() == 0) {
            dispose();
        }
    }
    
    private void initialize() {
        fGoToMessageForKeyBinding = new MoveToMessage();
        IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
        AbstractHandler handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fGoToMessageForKeyBinding.run();
                return null;
            }
        };
        IHandlerActivation activation = service.activateHandler(fGoToMessageForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fMoveUpForKeyBinding = new MoveSDUp();
        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fMoveUpForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fMoveUpForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fMoveDownForKeyBinding = new MoveSDDown();
        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fMoveDownForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fMoveDownForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fMoveLeftForKeyBinding = new MoveSDLeft();
        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fMoveLeftForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fMoveLeftForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fMoveRightForKeyBinding = new MoveSDRight();
        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fMoveRightForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fMoveRightForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fFindForKeyBinding = new OpenSDFindDialog();
        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fFindForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fFindForKeyBinding.getActionDefinitionId(), handler);
        fFindForKeyBinding.setEnabled(false);
        fHandlerActivations.add(activation);

        fShowNodeStartForKeyBinding = new ShowNodeStart();
        fShowNodeStartForKeyBinding.setText(SDMessages.uml_25);

        fShowNodeStartForKeyBinding.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$
        fShowNodeStartForKeyBinding.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$

        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fShowNodeStartForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fShowNodeStartForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

        fShowNodeEndForKeyBinding = new ShowNodeEnd();
        fShowNodeEndForKeyBinding.setText(SDMessages.uml_23);
        fShowNodeEndForKeyBinding.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$
        fShowNodeEndForKeyBinding.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$

        handler = new AbstractHandler() {
            @Override
            public Object execute(ExecutionEvent event) throws ExecutionException {
                fShowNodeEndForKeyBinding.run();
                return null;
            }
        };
        activation = service.activateHandler(fShowNodeEndForKeyBinding.getActionDefinitionId(), handler);
        fHandlerActivations.add(activation);

    }
    
    private void dispose() {
        IHandlerService service = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IHandlerService.class);
        for(IHandlerActivation activation : fHandlerActivations) {
            service.deactivateHandler(activation);
        }
        
        fGoToMessageForKeyBinding = null;
        fFindForKeyBinding = null;
        fMoveUpForKeyBinding = null;
        fMoveDownForKeyBinding = null;
        fMoveLeftForKeyBinding = null;
        fMoveRightForKeyBinding = null;
        fShowNodeStartForKeyBinding = null;
        fShowNodeEndForKeyBinding = null;
    }

    /**
     * @param view to set in global actions
     */
    public void setSdView(SDView view) {
        if (fViews.size() > 0) {
            fGoToMessageForKeyBinding.setView(view);
            fFindForKeyBinding.setView(view);
            fMoveUpForKeyBinding.setView(view);
            fMoveDownForKeyBinding.setView(view);
            fMoveLeftForKeyBinding.setView(view);
            fMoveRightForKeyBinding.setView(view);
            fShowNodeStartForKeyBinding.setView(view);
            fShowNodeEndForKeyBinding.setView(view);
        }
    }

    /**
     * Enable / disable find action
     * @param enabled
     */
    public void setFindEnabled(boolean enabled) {
        if (fFindForKeyBinding != null) {
            fFindForKeyBinding.setEnabled(enabled);
        }
    }
}
