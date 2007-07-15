/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005-2006 Feed'n Read Development Team                        *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/
package org.mailster.gui.prefs.widgets;

import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 * Interface to be implemented by <code>IContentProviders</code> to enable
 * generic sorting support for JFace <code>TableViewer</code> instances by the
 * help of the
 * {@link org.mailster.gui.prefs.widgets.TableViewerSorter} class.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 * @see org.mailster.gui.prefs.widgets.TableViewerSorter
 */
public interface ITableContentProvider extends IStructuredContentProvider
{
    /**
     * Gets the column value for the specified <code>element</code> at the
     * given <code>columnIndex</code>.
     * 
     * @param element the model element for which to query the colum value
     * @param columnIndex the index of the column to query the value for
     * @return the value for the <code>element</code> at the given
     *         <code>columnIndex</code>
     */
    public Object getColumnValue(Object element, int columnIndex);
}
