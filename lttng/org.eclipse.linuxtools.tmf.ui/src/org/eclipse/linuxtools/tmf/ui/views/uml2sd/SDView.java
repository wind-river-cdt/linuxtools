/**********************************************************************
 * Copyright (c) 2005, 2008, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: SDView.java,v 1.2 2008/01/24 02:29:01 apnan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.BaseMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.Frame;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.SyncMessageReturn;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ConfigureMinMax;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.FirstPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.KeyBindingsManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.LastPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.MoveToMessage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.NextPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDFiltersDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDFindDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.OpenSDPagesDialog;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.PrevPage;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Print;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Zoom;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.Zoom.ZoomType;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.IExtendedFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.IExtendedFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDCollapseProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDExtendedActionBarProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDFindProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDGraphNodeSupporter;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPagingProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDPropertiesProvider;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.LoadersManager;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.SDMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/**
 * @author sveyrier
 * 
 */
public class SDView extends ViewPart {

    protected SDWidget sdWidget = null;
    protected TimeCompressionBar timeCompressionBar = null;
    protected ISDFindProvider sdFindProvider = null;
    protected ISDPagingProvider sdPagingProvider = null;
    protected ISDFilterProvider sdFilterProvider = null;
    protected IExtendedFilterProvider sdExFilterProvider = null;
    protected IExtendedFindProvider sdExFindProvider = null;
    protected ISDExtendedActionBarProvider sdExtendedActionBarProvider = null;
    protected ISDPropertiesProvider sdPropertiesProvider = null;

    protected NextPage nextPageButton = null;
    protected PrevPage prevPageButton = null;
    protected FirstPage firstPageButton = null;
    protected LastPage lastPageButton = null;

    protected MenuManager menuMgr = null;
    
    protected boolean needInit = true;

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite c) {
        Composite parent = new Composite(c, SWT.NONE);
        GridLayout parentLayout = new GridLayout();
        parentLayout.numColumns = 2;
        parentLayout.marginWidth = 0;
        parentLayout.marginHeight = 0;
        parent.setLayout(parentLayout);

        GridData timeLayoutdata = new GridData(GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        timeLayoutdata.widthHint = 10;
        GridData seqDiagLayoutData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.VERTICAL_ALIGN_FILL);
        timeCompressionBar = new TimeCompressionBar(parent, SWT.NONE);
        timeCompressionBar.setLayoutData(timeLayoutdata);
        sdWidget = new SDWidget(parent, SWT.NONE);// SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        sdWidget.setLayoutData(seqDiagLayoutData);
        sdWidget.setSite(this);
        sdWidget.setTimeBar(timeCompressionBar);

        // Add this view to the key bindings manager
        KeyBindingsManager.getInstance().add(this.getSite().getId());
        
        createCoolbarContent();

        hookContextMenu();

        timeCompressionBar.setVisible(false);
        parent.layout(true);

        Print print = new Print(this);
        getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(), print);

        needInit = restoreLoader();
    }

    /**
     * Load a blank page that is supposed to explain that a kind of interaction must be chosen
     */
    protected void loadBlank() {
        IUml2SDLoader l = new IUml2SDLoader() {
            /*
             * (non-Javadoc)
             * @see
             * org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader#setViewer(org.eclipse.linuxtools.tmf.ui
             * .views.uml2sd.SDView)
             */
            @Override
            public void setViewer(SDView viewer) {
                // Nothing to do
                Frame f = new Frame();
                f.setName(""); //$NON-NLS-1$
                viewer.setFrame(f);
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader#getTitleString()
             */
            @Override
            public String getTitleString() {
                return ""; //$NON-NLS-1$
            }

            /*
             * (non-Javadoc)
             * @see org.eclipse.linuxtools.tmf.ui.views.uml2sd.load.IUml2SDLoader#aboutToBeReplaced()
             */
            @Override
            public void dispose() {
            }
        };
        l.setViewer(this);
        setContentDescription(l.getTitleString());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (sdWidget != null) {
            // update actions for key bindings
            KeyBindingsManager.getInstance().setSdView(this);
            sdWidget.setFocus();
        }
        if (isViewReady() && needInit) {
            needInit = restoreLoader();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        KeyBindingsManager.getInstance().remove(this.getSite().getId());
        super.dispose();
    }

    /**
     * @return The SD widget.
     */
    public SDWidget getSDWidget() {
        return sdWidget;
    }
    
    /**
     * Set the find provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the find menu item will not be available in the viewer<br>
     * A find provider is called back when the user perform a find action<br>
     * The find provider is responsible to move the sequence diagram to the GraphNode which match the 
     * find criteria as well as to highlight the GraphNode
     * 
     * @param provider the search provider
     */
    public void setSDFindProvider(ISDFindProvider provider) {
        sdFindProvider = provider;
        sdExFindProvider = null;
        createCoolbarContent();
        if (provider != null) {
            KeyBindingsManager.getInstance().setFindEnabled(true);
        }
        else {
            KeyBindingsManager.getInstance().setFindEnabled(false);
        }
    }

    /**
     * Set the find provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the find menu item will not be available in the viewer<br>
     * A find provider is called back when the user perform a find action<br>
     * If the extended find provider is set, it replaces the regular find provider (sdFindProvider).<br>
     * @param provider
     */
    public void setExtendedFindProvider(IExtendedFindProvider provider) {
        sdExFindProvider = provider;
        sdFindProvider = null;
        createCoolbarContent();
        if (provider != null) {
            KeyBindingsManager.getInstance().setFindEnabled(true);
        }
        else {
            KeyBindingsManager.getInstance().setFindEnabled(false);
        }
    }

    /**
     * Returns the extended find provider
     * 
     * @return extended find provider.
     */
    public IExtendedFindProvider getExtendedFindProvider() {
        return sdExFindProvider;
    }

    /**
     * Resets all providers.
     */
    public void resetProviders() {
        KeyBindingsManager.getInstance().setFindEnabled(false);
        sdFindProvider = null;
        sdExFindProvider = null;
        sdFilterProvider = null;
        sdExFilterProvider = null;
        sdPagingProvider = null;
        sdExtendedActionBarProvider = null;
        sdPropertiesProvider = null;
        if ((sdWidget != null) && (!sdWidget.isDisposed())) {
            sdWidget.setCollapseProvider(null);
        }
    }

    /**
     * Set the filter provider for the opened sequence diagram viewer<br>
     * If the provider is not set, the filter menu item will not be available in the viewer<br>
     * A filter provider is called back when the user perform a filter action<br>
     * 
     * @param provider the filter provider
     */
    public void setSDFilterProvider(ISDFilterProvider provider) {
        sdFilterProvider = provider;
        // Both systems can be used now, commenting out next statement
        // sdExFilterProvider = null;
        createCoolbarContent();
    }
    
    /**
     * Sets the extended filter provider for the opend sequence diagram viewer.
     * @param provider
     */
    public void setExtendedFilterProvider(IExtendedFilterProvider provider) {
        sdExFilterProvider = provider;
        // Both systems can be used now, commenting out next statement
        // sdFilterProvider = null;
        createCoolbarContent();
    }

    /**
     * Returns the extended find provider.
     * 
     * @return The extended find provider.
     */
    public IExtendedFilterProvider getExtendedFilterProvider() {
        return sdExFilterProvider;
    }

    /**
     * Register the given provider to support Drag and Drop collapsing. This provider is 
     * responsible of updating the Frame.
     * 
     * @param provider - the provider to register
     */
    public void setCollapsingProvider(ISDCollapseProvider provider) {
        if ((sdWidget != null) && (!sdWidget.isDisposed())) {
            sdWidget.setCollapseProvider(provider);
        }
    }

    /**
     * Set the page provider for the opened sequence diagram viewer<br>
     * If the sequence diagram provided (see setFrame) need to be split in many parts, a paging provider must be
     * provided in order to handle page change requested by the user<br>
     * Set a page provider will create the next and previous page buttons in the viewer coolBar
     * 
     * @param provider the paging provider
     */
    public void setSDPagingProvider(ISDPagingProvider provider) {
        sdPagingProvider = provider;
        createCoolbarContent();
    }

    /**
     * Returns the current page provider for the view
     * 
     * @return the paging provider
     */
    public ISDPagingProvider getSDPagingProvider() {
        return sdPagingProvider;
    }

    /**
     * Returns the current find provider for the view
     * 
     * @return the find provider
     */
    public ISDFindProvider getSDFindProvider() {
        return sdFindProvider;
    }

    /**
     * Returns the current filter provider for the view
     * 
     * @return the filter provider
     */
    public ISDFilterProvider getSDFilterProvider() {
        return sdFilterProvider;
    }

    /**
     * Set the extended action bar provider for the opened sequence diagram viewer<br>
     * This allow to add programmatically actions in the coolbar and/or in the drop-down menu
     * 
     * @param provider the search provider
     */
    public void setSDExtendedActionBarProvider(ISDExtendedActionBarProvider provider) {
        sdExtendedActionBarProvider = provider;
        createCoolbarContent();
    }

    /**
     * Returns the current extended action bar provider for the view
     * 
     * @return the extended action bar provider
     */
    public ISDExtendedActionBarProvider getSDExtendedActionBarProvider() {
        return sdExtendedActionBarProvider;
    }

    /**
     * Set the properties view provider for the opened sequence diagram viewer<br>
     * 
     * @param provider the properties provider
     */
    public void setSDPropertiesProvider(ISDPropertiesProvider provider) {
        sdPropertiesProvider = provider;
    }

    /**
     * Returns the current extended action bar provider for the view
     * 
     * @return the extended action bar provider
     */
    public ISDPropertiesProvider getSDPropertiesProvider() {
        return sdPropertiesProvider;
    }

    /**
     * Creates the basic sequence diagram menu
     */
    protected void hookContextMenu() {
        menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(sdWidget.getViewControl());
        sdWidget.getViewControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, sdWidget.getSelectionProvider());
    }

    /**
     * Returns the context menu manager
     * 
     * @return the menu manager
     */
    public MenuManager getMenuManager() {
        return menuMgr;
    }

    /**
     * Fills the basic sequence diagram menu and define the dynamic menu item insertion point
     * 
     * @param manager the menu manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        manager.add(new Separator("Additions")); //$NON-NLS-1$
        if (getSDWidget() != null && getSDWidget().currentGraphNode != null) {
            ISelectionProvider selProvider = sdWidget.getSelectionProvider();
            ISelection sel = selProvider.getSelection();
            int nbMessage = 0;
            Iterator<?> it = ((StructuredSelection) sel).iterator();
            while (it.hasNext()) {
                Object node = it.next();
                if (node instanceof BaseMessage) {
                    nbMessage++;
                }
            }
            if (nbMessage != 1) {
                return;
            }
            GraphNode node = getSDWidget().currentGraphNode;
            if (node instanceof SyncMessageReturn) {
                if (((SyncMessageReturn) node).getMessage() != null) {
                    Action goToMessage = new MoveToMessage(this);
                    goToMessage.setText(SDMessages._39);
                    manager.add(goToMessage);
                }
            }
            if (node instanceof SyncMessage) {
                if (((SyncMessage) node).getMessageReturn() != null) {
                    Action goToMessage = new MoveToMessage(this);
                    goToMessage.setText(SDMessages._40);
                    manager.add(goToMessage);
                }
            }
        }
        manager.add(new Separator("MultiSelectAdditions")); //$NON-NLS-1$
    }

    /**
     * Enables/Disables an action with given name.
     * 
     * @param actionName The action name
     * @param state true or false
     */
    public void setEnableAction(String actionName, boolean state) {
        IActionBars bar = getViewSite().getActionBars();
        if (bar != null) {
            IContributionItem item = bar.getMenuManager().find(actionName);
            if ((item != null) && (item instanceof ActionContributionItem)) {
                IAction action = ((ActionContributionItem) item).getAction();
                if (action != null) {
                    action.setEnabled(state);
                }
                item.setVisible(state);
                bar.updateActionBars();
            }
        }
    }

    /**
     * Creates the coolBar icon depending on the actions supported by the Sequence Diagram provider<br>
     * - Navigation buttons are displayed if ISDPovider.HasPaging return true<br>
     * - Navigation buttons are enabled depending on the value return by ISDPovider.HasNext and HasPrev<br>
     * 
     * @see ISDGraphNodeSupporter Action support definition
     * @see SDView#setSDFilterProvider(ISDFilterProvider)
     * @see SDView#setSDFindProvider(ISDFindProvider)
     * @see SDView#setSDPagingProvider(ISDPagingProvider)
     */
    protected void createCoolbarContent() {
        IActionBars bar = getViewSite().getActionBars();

        bar.getMenuManager().removeAll();
        bar.getToolBarManager().removeAll();

        createMenuGroup();

        Zoom resetZoom = new Zoom(this, ZoomType.ZOOM_RESET);
        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", resetZoom);//$NON-NLS-1$
        bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", resetZoom); //$NON-NLS-1$

        Zoom noZoom = new Zoom(this, ZoomType.ZOOM_NONE);
        noZoom.setChecked(true);
        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", noZoom);//$NON-NLS-1$
        bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", noZoom); //$NON-NLS-1$

        Zoom zoomIn = new Zoom(this, ZoomType.ZOOM_IN);
        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", zoomIn);//$NON-NLS-1$
        bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", zoomIn); //$NON-NLS-1$

        Zoom zoomOut = new Zoom(this, ZoomType.ZOOM_OUT);
        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", zoomOut);//$NON-NLS-1$
        bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", zoomOut); //$NON-NLS-1$

        MenuManager navigation = new MenuManager(SDMessages._77);

        ShowNodeStart showNodeStart = new ShowNodeStart(this);
        showNodeStart.setText(SDMessages.uml_25);

        showNodeStart.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$
        showNodeStart.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeStart");//$NON-NLS-1$
        navigation.add(showNodeStart);

        ShowNodeEnd showNodeEnd = new ShowNodeEnd(this);
        showNodeEnd.setText(SDMessages.uml_23);

        showNodeEnd.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$
        showNodeEnd.setActionDefinitionId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ShowNodeEnd");//$NON-NLS-1$
        navigation.add(showNodeEnd);

        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", navigation); //$NON-NLS-1$

        ConfigureMinMax minMax = new ConfigureMinMax(this);
        minMax.setText(SDMessages.uml_45);
        minMax.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.ConfigureMinMax");//$NON-NLS-1$
        bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", minMax); //$NON-NLS-1$

        if ((sdWidget.getFrame() != null) && (sdWidget.getFrame().hasTimeInfo()))
            minMax.setEnabled(true);
        else
            minMax.setEnabled(false);

        // Do we need to display a paging item
        if (sdPagingProvider != null) {
            nextPageButton = new NextPage(this);
            bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", nextPageButton); //$NON-NLS-1$
            nextPageButton.setEnabled(sdPagingProvider.hasNextPage());
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", nextPageButton); //$NON-NLS-1$

            prevPageButton = new PrevPage(this);
            bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", prevPageButton); //$NON-NLS-1$
            prevPageButton.setEnabled(sdPagingProvider.hasPrevPage());
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", prevPageButton); //$NON-NLS-1$
            
            firstPageButton = new FirstPage(this);
            bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", firstPageButton); //$NON-NLS-1$
            firstPageButton.setEnabled(sdPagingProvider.hasPrevPage());
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", firstPageButton); //$NON-NLS-1$

            lastPageButton = new LastPage(this);
            bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", lastPageButton); //$NON-NLS-1$
            lastPageButton.setEnabled(sdPagingProvider.hasNextPage());
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", lastPageButton); //$NON-NLS-1$
        }

        if (sdExFilterProvider != null) {
            Action action = sdExFilterProvider.getFilterAction();
            if (action != null) {
                if (action.getId() == null)
                    action.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.extendedFilter"); //$NON-NLS-1$
                if (action.getImageDescriptor() == null)
                    action.setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_FILTERS));
                if (action.getText() == null || action.getText().length() == 0)
                    action.setText(SDMessages._42);
                bar.getMenuManager().prependToGroup("UML2SD_FILTERING", action); //$NON-NLS-1$
                bar.getToolBarManager().prependToGroup("UML2SD_FILTERING", action); //$NON-NLS-1$
            }
        }
        // Both systems can be used now: commenting out else keyword
        /* else */if (sdFilterProvider != null) {
            bar.getMenuManager().appendToGroup("UML2SD_FILTERING", new OpenSDFiltersDialog(this, sdFilterProvider)); //$NON-NLS-1$	
            // No longer in the coolbar: commenting out next statement
            //bar.getToolBarManager().appendToGroup("UML2SD_FILTERING",new OpenSDFiltersDialog(this, sdFilterProvider));	//$NON-NLS-1$	
        }
        if (sdPagingProvider != null && sdPagingProvider instanceof ISDAdvancedPagingProvider) {
            IContributionItem sdPaging = bar.getMenuManager().find(OpenSDPagesDialog.ID);
            if (sdPaging != null) {
                bar.getMenuManager().remove(sdPaging);
                sdPaging = null;
            }
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", new OpenSDPagesDialog(this, (ISDAdvancedPagingProvider) sdPagingProvider)); //$NON-NLS-1$
            updatePagesMenuItem(bar);
        }

        if (sdExFindProvider != null) {
            Action action = sdExFindProvider.getFindAction();
            if (action != null) {
                if (action.getId() == null)
                    action.setId("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.extendedFind"); //$NON-NLS-1$
                if (action.getImageDescriptor() == null)
                    action.setImageDescriptor(TmfUiPlugin.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SEARCH_SEQ));
                if (action.getText() == null)
                    action.setText(SDMessages._41);
                bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", action); //$NON-NLS-1$
                bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", action); //$NON-NLS-1$
            }
        } else if (sdFindProvider != null) {
            bar.getMenuManager().appendToGroup("UML2SD_OTHER_COMMANDS", new OpenSDFindDialog(this)); //$NON-NLS-1$
            bar.getToolBarManager().appendToGroup("UML2SD_OTHER_COMMANDS", new OpenSDFindDialog(this)); //$NON-NLS-1$
        }

        if (sdExtendedActionBarProvider != null) {
            sdExtendedActionBarProvider.supplementCoolbarContent(bar);
        }

        bar.updateActionBars();
    }

    /**
     * Updates the view coolbar buttons state according to the value return by: -
     * ISDExtendedActionBarProvider.hasNextPage()<br>
     * - ISDExtendedActionBarProvider.hasPrevPage()<br>
     * 
     */
    public void updateCoolBar() {
        if (sdPagingProvider != null) {
            IActionBars bar = getViewSite().getActionBars();
            if (bar == null)
                return;
            IToolBarManager barManager = bar.getToolBarManager();
            if (barManager == null)
                return;
            IContributionItem nextPage = barManager.find(NextPage.ID);
            if (nextPage != null && nextPage instanceof ActionContributionItem) {
                IAction nextPageAction = ((ActionContributionItem) nextPage).getAction();
                if (nextPageAction != null && nextPageAction instanceof NextPage) {
                    ((NextPage) nextPageAction).setEnabled(sdPagingProvider.hasNextPage());
                }
            }

            IContributionItem prevPage = barManager.find(PrevPage.ID);
            if (prevPage != null && prevPage instanceof ActionContributionItem) {
                IAction prevPageAction = ((ActionContributionItem) prevPage).getAction();
                if (prevPageAction != null && prevPageAction instanceof PrevPage) {
                    ((PrevPage) prevPageAction).setEnabled(sdPagingProvider.hasPrevPage());
                }
            }

            IContributionItem firstPage = barManager.find(FirstPage.ID);
            if (firstPage != null && firstPage instanceof ActionContributionItem) {
                IAction firstPageAction = ((ActionContributionItem) firstPage).getAction();
                if (firstPageAction != null && firstPageAction instanceof FirstPage) {
                    ((FirstPage) firstPageAction).setEnabled(sdPagingProvider.hasPrevPage());
                }
            }

            IContributionItem lastPage = barManager.find(LastPage.ID);
            if (lastPage != null && lastPage instanceof ActionContributionItem) {
                IAction lastPageAction = ((ActionContributionItem) lastPage).getAction();
                if (lastPageAction != null && lastPageAction instanceof LastPage) {
                    ((LastPage) lastPageAction).setEnabled(sdPagingProvider.hasNextPage());
                }
            }
            
            updatePagesMenuItem(bar);
        }
    }

    /**
     * Enables or disables the Pages... menu item, depending on the number of pages
     * 
     * @param bar the bar containing the action
     */
    protected void updatePagesMenuItem(IActionBars bar) {
        if (sdPagingProvider instanceof ISDAdvancedPagingProvider) {
            IMenuManager menuManager = bar.getMenuManager();
            ActionContributionItem contributionItem = (ActionContributionItem) menuManager.find(OpenSDPagesDialog.ID);
            IAction openSDPagesDialog = null;
            if (contributionItem != null) {
                openSDPagesDialog = contributionItem.getAction();
            }

            if (openSDPagesDialog != null && openSDPagesDialog instanceof OpenSDPagesDialog) {
                openSDPagesDialog.setEnabled(((ISDAdvancedPagingProvider) sdPagingProvider).pagesCount() > 1);
            }
        }
    }

    /**
     * The frame to render (the sequence diagram)
     * 
     * @param frame the frame to display
     */
    public void setFrame(Frame frame) {
        setFrame(frame, true);
    }

    /**
     * The frame to render (the sequence diagram)
     * 
     * @param frame the frame to display
     */
    protected void setFrame(Frame frame, boolean resetPosition) {
        if (sdWidget == null)
            return;

        if (frame == null) {
            loadBlank();
            return;
        }

        IUml2SDLoader l = LoadersManager.getInstance().getCurrentLoader(getViewSite().getId(), this);

        if ((l != null) && (l.getTitleString() != null)) {
            setContentDescription(l.getTitleString());
        }

        if (getSDWidget() != null)
            getSDWidget().setFrame(frame, resetPosition);

        if (timeCompressionBar != null)
            timeCompressionBar.setFrame(frame);
        updateCoolBar();
        if (!frame.hasTimeInfo()) {
            Composite parent = timeCompressionBar.getParent();
            timeCompressionBar.setVisible(false);
            parent.layout(true);
        } else {
            Composite parent = timeCompressionBar.getParent();
            timeCompressionBar.setVisible(true);
            parent.layout(true);
        }
        IContributionItem shortKeysMenu = getViewSite().getActionBars().getMenuManager().find("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers");//$NON-NLS-1$
        MenuManager shortKeys = (MenuManager) shortKeysMenu;
        if (shortKeys != null) {
            IContributionItem[] items = shortKeys.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof ActionContributionItem) {
                    IAction action = ((ActionContributionItem) items[i]).getAction();
                    if (action != null)
                        action.setEnabled(true);
                }
            }
        }
        createCoolbarContent();
    }

    /**
     * Activate or deactivate the short key command given in parameter (see plugin.xml)
     * 
     * @param id the command id defined in the plugin.xml
     * @param value the state value
     */
    public void setEnableCommand(String id, boolean value) {
        IContributionItem shortKeysMenu = getViewSite().getActionBars().getMenuManager().find("org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers");//$NON-NLS-1$
        MenuManager shortKeys = (MenuManager) shortKeysMenu;
        if (shortKeys == null)
            return;
        IContributionItem item = shortKeys.find(id);
        if ((item != null) && (item instanceof ActionContributionItem)) {
            IAction action = ((ActionContributionItem) item).getAction();
            if (action != null)
                action.setEnabled(value);
        }
    }

    /**
     * Set the frame from an other thread than the one executing the main loop
     * 
     * @param frame
     */
    public void setFrameSync(final Frame frame) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                setFrame(frame);
            }
        });

    }

    /**
     * Ensure an object is visible from an other thread than the one executing the main loop
     * 
     * @param sm
     */
    public void ensureVisibleSync(final GraphNode sm) {
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                getSDWidget().ensureVisible(sm);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible from an other thread than the one executing the main loop
     * 
     * @param sm
     */
    public void setFrameAndEnsureVisibleSync(final Frame frame, final GraphNode sm) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                setFrameAndEnsureVisible(frame, sm);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible
     * 
     * @param sm
     */
    public void setFrameAndEnsureVisible(Frame frame, GraphNode sm) {
        getSDWidget().clearSelection();
        setFrame(frame, false);
        getSDWidget().ensureVisible(sm);
    }

    /**
     * Set the frame and ensure an object is visible from an other thread than the one executing the main loop
     * 
     * @param sm
     */
    public void setFrameAndEnsureVisibleSync(final Frame frame, final int x, final int y) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }
        
        getSDWidget().getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                setFrameAndEnsureVisible(frame, x, y);
            }
        });
    }

    /**
     * Set the frame and ensure an object is visible
     * 
     * @param sm
     */
    public void setFrameAndEnsureVisible(Frame frame, int x, int y) {
        getSDWidget().clearSelection();
        setFrame(frame, false);
        getSDWidget().ensureVisible(x, y);
        getSDWidget().redraw();
    }

    /**
     * waitCursor is the cursor to be displayed when long tasks are running
     */
    protected Cursor waitCursor;

    /**
     * Toggle between default and wait cursors from an other thread than the one executing the main loop
     */
    public void toggleWaitCursorAsync(final boolean wait_) {
        if (getSDWidget() == null || getSDWidget().isDisposed()) {
            return;
        }

        getSDWidget().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (getSDWidget() == null || getSDWidget().isDisposed()) {
                    return;
                }
                if (wait_) {
                    if (waitCursor != null && !waitCursor.isDisposed()) {
                        waitCursor.dispose();
                    }
                    waitCursor = new Cursor(getSDWidget().getDisplay(), SWT.CURSOR_WAIT);
                    getSDWidget().setCursor(waitCursor);
                    getSDWidget().getDisplay().update();
                } else {
                    if (waitCursor != null && !waitCursor.isDisposed()) {
                        waitCursor.dispose();
                    }
                    waitCursor = null;
                    getSDWidget().setCursor(null);
                    getSDWidget().getDisplay().update();
                }
            }
        });
    }

    /**
     * Return the time compression bar widget
     * 
     * @return the time compression bar
     */
    public TimeCompressionBar getTimeCompressionBar() {
        return timeCompressionBar;
    }

    /**
     * Returns the current Frame (the sequence diagram container)
     * 
     * @return the frame
     */
    public Frame getFrame() {
        if (getSDWidget() != null) {
            return getSDWidget().getFrame();
        }
        else {
            return null;
        }
    }

    protected boolean restoreLoader() {
        String id = getViewSite().getId();
        // System.err.println("restoreLoader() id="+id);
        if (id == null) {
            return true;
        }
        IUml2SDLoader l = LoadersManager.getInstance().getCurrentLoader(id, this);
        // System.err.println("restoreLoader() l="+l);
        if ((l != null)) {// &&( LoadersManager.getLoadersManager().getViewer(l)==this)){
            l.setViewer(this);
            return false;
        } else {
            loadBlank();
            return true;
        }
    }

    protected boolean isViewReady() {
        IWorkbenchPage persp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (persp == null)
            return false;

        IViewReference[] ref = persp.getViewReferences();
        for (int i = 0; i < ref.length; i++) {
            if (ref[i].getView(false) == this) {
                return true;
            }
        }
        return false;
    }

    protected void createMenuGroup() {
        IActionBars bar = getViewSite().getActionBars();
        if (bar == null) {
            return;
        }
        bar.getToolBarManager().add(new Separator("UML2SD_VIEW_MODES")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_WORKING_SET")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_SORTING")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_FILTERING")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_VIEW_LAYOUT")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_LINK_EDITOR")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_OTHER_COMMANDS")); //$NON-NLS-1$
        bar.getToolBarManager().add(new Separator("UML2SD_OTHER_PLUGINS_COMMANDS")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_VIEW_MODES")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_WORKING_SET")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_SORTING")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_FILTERING")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_VIEW_LAYOUT")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_LINK_EDITOR")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_OTHER_COMMANDS")); //$NON-NLS-1$
        bar.getMenuManager().add(new Separator("UML2SD_OTHER_PLUGINS_COMMANDS")); //$NON-NLS-1$
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class _adapter) {
        Object obj = super.getAdapter(_adapter);
        if (sdPropertiesProvider != null && _adapter.equals(IPropertySheetPage.class)) {
            return sdPropertiesProvider.getPropertySheetEntry();
        }

        return obj;
    }
}
