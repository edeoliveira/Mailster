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
package org.mailster.gui.prefs;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.mailster.gui.Messages;
import org.mailster.gui.prefs.utils.LanguageResource;
import org.mailster.gui.prefs.widgets.ITableContentProvider;
import org.mailster.util.StringUtilities;

/**
 * Provides the data for the <code>TableFieldEditor</code> used in the
 * <code>LanguageConfigurationPage</code>.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public class LanguageTableContentProvider extends ArrayContentProvider
        implements ITableContentProvider
{

    /**
     * Gets the column value for the specified <code>element</code> at the
     * given <code>columnIndex</code>.
     * 
     * @param element the model element for which to query the colum value
     * @param columnIndex the index of the column to query the value for
     * @return the value for the <code>element</code> at the given
     *         <code>columnIndex</code>
     * @see org.mailster.gui.prefs.widgets.ITableContentProvider#getColumnValue(java.lang.Object,
     *      int)
     */
    public Object getColumnValue(Object element, int columnIndex)
    {
        LanguageResource resource = (LanguageResource) element;
        switch (columnIndex)
        {
            case 0 :
            {
                /* Use english language identifiers by default */
                return resource
                        .getLanguage(Messages.getLanguageResource("en"));
            }
            case 1 :
            {
                return resource.getFullISOLanguage();
            }
            case 2 :
            {
                return resource.getTranslator();
            }
            case 3 :
            {
                return resource.getTranslationDate();
            }
            default :
            {
                return element != null
                        ? element.toString()
                        : StringUtilities.EMPTY_STRING;
            }
        }
    }
}
