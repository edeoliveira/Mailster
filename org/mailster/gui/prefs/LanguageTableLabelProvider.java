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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.utils.LanguageResource;
import org.mailster.util.StringUtilities;

/**
 * Converts the data for the <code>TableFieldEditor</code> used in the
 * <code>LanguageConfigurationPage</code> to ui representations.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public class LanguageTableLabelProvider extends LabelProvider
        implements
            ITableLabelProvider
{
    /**
     * Gets the image for the specified element at the specified column index.
     * 
     * @param element the element to obtain the image for
     * @param columnIndex the index of the column to obtain the image for
     * @return the image for the specified element/columnIndex combination
     */
    public Image getColumnImage(Object element, int columnIndex)
    {
        LanguageResource resource = (LanguageResource) element;
        if (columnIndex == 0)
        {
            try
            {
                return (SWTHelper.loadImage("lang/" + resource.getFullISOLanguage()
                        + ".gif"));
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Gets the textual representation of the element at the specified column
     * index.
     * 
     * @param element the element to get the text for
     * @param columnIndex the index of the column to get the text for
     * @return the textual representation of the specified element/columnIndex
     *         combination
     */
    public String getColumnText(Object element, int columnIndex)
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

    /**
     * Disposes all resources claimed by this
     * <code>LanguageTableLabelProvider</code>.
     */
    public void dispose()
    {
        /* Nothing to dispose */
    }
}