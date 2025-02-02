/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: LoadersManager.java,v 1.5 2008/01/24 02:29:16 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.load;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Manager class for the UML2SD extension point.
 */
public class LoadersManager {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    public static final String LOADER_TAG = "uml2SDLoader"; //$NON-NLS-1$
    public static final String LOADER_PREFIX = LOADER_TAG + "."; //$NON-NLS-1$

    // The instance
    private static LoadersManager loadersManager;

    // Maps for caching information
    protected HashMap<String, IUml2SDLoader> fViewLoaderMap = new HashMap<String, IUml2SDLoader>();
    protected HashMap<String, ArrayList<IConfigurationElement>> fViewLoadersList = new HashMap<String, ArrayList<IConfigurationElement>>();
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * This should not be used by the clients
     */
    private LoadersManager() {
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * A static method to get the manager instance.
     * 
     * @return the manager instance
     */
    public static LoadersManager getInstance() {
        if (loadersManager == null) {
            loadersManager = new LoadersManager();
        }
        return loadersManager;
    }
    
    /**
     * Creates a loader instance and associate it to the view. It requires
     * that the loader-view-association was created by an eclipse extension.
     * 
     * @param className The name of the class to create an instance from
     * @param view The UML2 Sequence Diagram view instance
     * @return The created loader
     */
    public IUml2SDLoader createLoader(String className, SDView view) {

        // Safety check
        if (view == null) {
            return null;
        }

        String viewId = view.getViewSite().getId();

        // Get loaders from all extensions for given view
        List<IConfigurationElement> loaderElements = getLoaderConfigurationElements(viewId);
        IConfigurationElement ce = getLoaderConfigurationElement(className, loaderElements);

        if (ce != null) {
            // Assign a loader instance to this view
            createLoaderForView(viewId, ce);
            IUml2SDLoader loader = fViewLoaderMap.get(viewId);
            if (loader != null) {
                loader.setViewer(view);
                return loader;
            }
        }
        return null; 
    }

    /**
     * Sets the loader to null for this view, a kind of clean-up while disposing.
     * 
     * @param viewId the id of the view
     */
    public void resetLoader(String viewId) {
        IUml2SDLoader loader = (IUml2SDLoader) fViewLoaderMap.get(viewId);
        if (loader != null) {
            loader.dispose();
        }
        fViewLoaderMap.put(viewId, null);
    }

    /**
     * Returns the loader in use in given Sequence Diagram View
     * 
     * @param viewId The Sequence Diagram viewId.
     * @return the current loader if any - null otherwise
     */
    public IUml2SDLoader getCurrentLoader(String viewId) {
        return getCurrentLoader(viewId, null);
    }

    /**
     * Returns the loader in use in this Sequence Diagram View
     * 
     * @param viewId The Sequence Diagram viewId
     * @param view The Sequence Diagram view (if known). Use null to reference the primary SD View.
     * @return the current loader if any - null otherwise
     */
    public IUml2SDLoader getCurrentLoader(String viewId, SDView view) {
        if (viewId == null) {
            return null;
        }

        IWorkbenchPage persp = TmfUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();

        SDView sdView = view;

        try {
            // Search the view corresponding to the viewId
            if (sdView == null) {
                IViewReference viewref = (IViewReference) persp.findViewReference(viewId);
                if (viewref != null) {
                    sdView = (SDView) viewref.getView(false);
                }
                
                if (sdView == null) {
                    // no corresponding view exists -> return null for the loader
                    return null;
                }
            }

            // Return the loader corresponding to that view (if any)
            IUml2SDLoader loader = fViewLoaderMap.get(viewId);
            if (loader == null) {
                createLastLoaderIfAny(viewId);
                loader = fViewLoaderMap.get(viewId);
            }

            return loader;
        } catch (Exception e) {
            if (Tracer.isErrorTraced()) {
                Tracer.traceError("Exception during getCurrentLoder(): " + e); //$NON-NLS-1$
            }
        }
        return null;
    }

    /**
     * Returns the loader class name that have been saved last time.
     * 
     * @param viewId The view this loader belongs to
     * @return the class name of the saved loader
     */
    public String getSavedLoader(String viewId) {
        IPreferenceStore p = TmfUiPlugin.getDefault().getPreferenceStore();
        return p.getString(LOADER_PREFIX + viewId);
    }

    /**
     * Saves the last loader in order to reload it on next session.
     */
    public void saveLastLoader(String id, String id2) {
        IPreferenceStore p = TmfUiPlugin.getDefault().getPreferenceStore();
        p.setValue(LOADER_PREFIX + id2, id);
    }

    /**
     * Changes the current unique loader to the given secondary viewId.
     * 
     * @param loader The current loader
     * @param id the view secondary id or null
     */
    private void setCurrentLoader(IUml2SDLoader loader, String id) {
        if (id == null) {
            return;
        }

        // Get the loader in use
        IUml2SDLoader currentLoader = fViewLoaderMap.get(id);

        if ((currentLoader != null) && (currentLoader != loader)) {
            if (loader != null) {
                IWorkbenchPage persp = TmfUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    // Search view corresponding to the viewId
                    SDView sdview = null;
                    IViewReference viewref = (IViewReference) persp.findViewReference(id);
                    if (viewref != null) {
                        sdview = (SDView) viewref.getView(false);
                    }

                    // Make everything clean for the new loader
                    if (sdview != null) {
                        sdview.resetProviders();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // The old loader is going to be kicked
            currentLoader.dispose();
        }

        // Replace the current loader by the new one in the map
        fViewLoaderMap.put(id, loader);

        // Store this loader in the preferences to be able to restore it when the workbench will be re-launched
        if (loader != null) {
            saveLastLoader(loader.getClass().getName(), id);
        }
    }
    
    /**
     * Creates the last loader and saves it. If not last is not available, it creates
     * and saves the default loader, else no loader is created.
     * 
     * @param viewId The view ID.
     */
    private void createLastLoaderIfAny(String viewId) {
        // Get saved loader from preferences
        String loaderName = getSavedLoader(viewId);

        // Get loaders from all extensions for given view
        List<IConfigurationElement> loaderElements = getLoaderConfigurationElements(viewId);
        IConfigurationElement ce = getLoaderConfigurationElement(loaderName, loaderElements);

        if (ce == null) {
            ce = getDefaultLoader(loaderElements);
        }

        if (ce != null) {
            createLoaderForView(viewId, ce);
        }
    }

    /**
     * Gets a list of loader configuration elements from the extension point registry for a given view.
     * @param viewId The view ID
     * @return List of extension point configuration elements.
     */
    private List<IConfigurationElement> getLoaderConfigurationElements(String viewId) {
        List<IConfigurationElement> list = (List<IConfigurationElement>) fViewLoadersList.get(viewId);
        if (list != null) {
            return list;
        }
        ArrayList<IConfigurationElement> ret = new ArrayList<IConfigurationElement>();
        IExtensionPoint iep = Platform.getExtensionRegistry().getExtensionPoint(TmfUiPlugin.PLUGIN_ID, LOADER_TAG);
        if (iep == null) {
            return ret;
        }
        
        IExtension[] ie = iep.getExtensions();
        if (ie == null) {
            return ret;
        }

        for (int i = 0; i < ie.length; i++) {
            IConfigurationElement c[] = ie[i].getConfigurationElements();
            for (int j = 0; j < c.length; j++) {
                if (viewId.equals(c[j].getAttribute("view"))) { //$NON-NLS-1$
                    ret.add(c[j]);
                }
            }
        }
        fViewLoadersList.put(viewId, ret);
        return ret;
    }

    /**
     * Returns the loader configuration element for given loader class name and for the given 
     * list of configuration elements, if available else null.
     * 
     * @param loaderClassName The loader class name.
     * @param loaderElements  The list of loader configuration elements
     * @return Extension point configuration element 
     */
    private IConfigurationElement getLoaderConfigurationElement(String loaderClassName, List<IConfigurationElement> loaderElements) {
        if (loaderClassName != null && loaderClassName.length() > 0) {
            // Find configuration element corresponding to the saved loader
            for (Iterator<IConfigurationElement> i = loaderElements.iterator(); i.hasNext();) {
                IConfigurationElement ce = (IConfigurationElement) i.next();
                if (ce.getAttribute("class").equals(loaderClassName)) { //$NON-NLS-1$
                    return ce;
                }
            }
        }
        return null;
    }

    /**
     * Returns the loader configuration element for the given list of configuration elements, if available else null. 
     * Note that if multiple default loaders are defined it selects the first one

     * @param loaderElements The list of loader configuration elements
     * @return The default extension point configuration element.
     */
    private IConfigurationElement getDefaultLoader(List<IConfigurationElement> loaderElements) {
        // Look for a default loader
        for (Iterator<IConfigurationElement> i = loaderElements.iterator(); i.hasNext();) {
            IConfigurationElement ce = (IConfigurationElement) i.next();
            if (Boolean.valueOf(ce.getAttribute("default")).booleanValue()) { //$NON-NLS-1$
                return ce;
            }
        }
        return null;
    }

    /**
     * Creates an instance of the loader class for a given extension point configuration element and
     * also sets it as current loader for the given view.
     * @param viewId The view ID.
     * @param ce The extension point configuration element
     */
    private void createLoaderForView(String viewId, IConfigurationElement ce) {
        try {
            Object obj = ce.createExecutableExtension("class"); //$NON-NLS-1$
            IUml2SDLoader l = (IUml2SDLoader) obj;
            if (viewId != null) {
                setCurrentLoader(l, viewId);
            }
        } catch (CoreException e4) {
            System.err.println("Error 'uml2SDLoader' Extension point :" + e4); //$NON-NLS-1$
        } catch (Exception e5) {
            e5.printStackTrace();
            System.err.println("Error 'uml2SDLoader' Extension point :" + e5); //$NON-NLS-1$
        }
    }
}
