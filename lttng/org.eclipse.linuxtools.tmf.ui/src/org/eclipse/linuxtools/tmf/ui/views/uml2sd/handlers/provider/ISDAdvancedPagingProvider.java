/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ISDAdvancedPagingProvider.java,v 1.2 2006/09/20 20:56:26 ewchan Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers.provider;

/**
 * An advanced paging provider is able to compute number of pages, and to display the number of items it treats on each
 * page and for total counts.<br>
 * An item can be a message, a node or anything meaningful from loader standpoint.<br>
 * Items are only here for information to the user.
 */
public interface ISDAdvancedPagingProvider extends ISDPagingProvider {

    /**
     * @return the current page the loader is dealing with <b>Note<b> that first page has the 0 index (indexes are from
     *         0 to pagesCount()-1).
     */
    public int currentPage();

    /**
     * @return number of pages the loader is dealing with
     */
    public int pagesCount();

    /**
     * Instructs a load of the &lt;pageNumber_&gt;<i>th</i> page.<br>
     * <b>Note<b> that first page has the index 0 (indexes are from 0 to pagesCount()-1).
     * 
     * @param pageNumber_ index of the page to load
     */
    public void pageNumberChanged(int pageNumber_);

}
