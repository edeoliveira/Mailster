/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005 Feed'n Read Development Team                             *
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
package org.mailster.gui.prefs.pages;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultFieldEditorConfigurationPage;
import org.mailster.gui.prefs.LanguageTableContentProvider;
import org.mailster.gui.prefs.LanguageTableLabelProvider;
import org.mailster.gui.prefs.widgets.TableFieldEditor;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * LanguageConfigurationPage.java - Configuration page to select the application
 * user interface language used.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author: kods $ / $Date: 2009/04/27 23:40:31 $
 */

public class LanguageConfigurationPage
        extends DefaultFieldEditorConfigurationPage
{    
	/**
     * The <code>FieldEditor</code> to select the user interface language
     */
    private TableFieldEditor languageEditor;

    /**
     * Creates a new <code>LanguageConfigurationPage</code> instance.
     */
    public LanguageConfigurationPage()
    {
        super(Messages.getString("languageConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/languageConfig32.png"),
                DefaultFieldEditorConfigurationPage.GRID);
    }

    /**
     * Creates the <code>FieldEditor</code>s of this
     * <code>LanguageConfigurationPage</code>.
     * 
     * @see FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors()
    {
        LanguageTableContentProvider contentProvider = new LanguageTableContentProvider();
        LanguageTableLabelProvider labelProvider = new LanguageTableLabelProvider();

        String[] columns = { Messages.getString("languageHeader"),
				        		Messages.getString("translationDateHeader"),                 
				        		Messages.getString("versionHeader"),
                                Messages.getString("translatorHeader")};

        languageEditor = new TableFieldEditor(
                ConfigurationManager.LANGUAGE_KEY, 
                        Messages.getString("languageLabel"), getFieldEditorParent(), 
                        contentProvider, labelProvider, columns, 
                        Messages.getAvailableLanguageResources());
        languageEditor.setSortingEnabled(true);
        languageEditor.sort(0, true);
        languageEditor.setSelectionColumn(0);
        languageEditor.setColumnWidth(0, 120);
        languageEditor.setColumnAlignment(2, SWT.CENTER);
        addField(languageEditor);
        
        // Disable default and apply buttons
        noDefaultAndApplyButton();
    }
}
