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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultFieldEditorConfigurationPage;
import org.mailster.gui.prefs.widgets.DirectoryFieldEditor;

/**
 * ---<br>
 * Mailster (C) 2007 De Oliveira Edouard
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * EnclosuresConfigurationPage.java - Configuration page for enclosures specific settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class EnclosuresConfigurationPage 
    extends DefaultFieldEditorConfigurationPage 
{   
    /**
	 * <code>FieldEditor</code> to select the directory in which to store
	 * attachments.
	 */
	private DirectoryFieldEditor enclosuresDirectoryEditor;

	/**
	 * <code>FieldEditor</code> to decide if enclosures should be executed
	 * subsequent to their download.
	 */
	private BooleanFieldEditor executeEnclosureOnClickEditor;
    
    
    /**
	 * Creates a new <code>EnclosuresConfigurationPage</code>.
	 */
    public EnclosuresConfigurationPage() 
    {
        super(Messages
                .getString("enclosuresConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/enclosureConfig32.png"),
                DefaultFieldEditorConfigurationPage.GRID);
    }
    
    
    /**
     * Creates the <code>FieldEditor</code>s of this
     * <code>EnclosuresConfigurationPage</code>.
     * 
     * @see FieldEditorPreferencePage#createFieldEditors()
     */    
    public void createFieldEditors() 
    {        
        enclosuresDirectoryEditor = new DirectoryFieldEditor(
                ConfigurationManager.DEFAULT_ENCLOSURES_DIRECTORY_KEY,
                Messages.getString("enclosuresDirectoryLabel"), 
                getFieldEditorParent(),
                DirectoryFieldEditor.VALIDATE_ON_KEY_STROKE);
        enclosuresDirectoryEditor.setChangeButtonText(Messages
                .getString("browseButton"));
        enclosuresDirectoryEditor
                .setTextLimit(DirectoryFieldEditor.UNLIMITED);
        enclosuresDirectoryEditor.setErrorMessage(Messages
                .getString("invalidDirectoryMessage"));
        addField(enclosuresDirectoryEditor);

        executeEnclosureOnClickEditor = new BooleanFieldEditor(
                ConfigurationManager.EXECUTE_ENCLOSURE_ON_CLICK_KEY,
                Messages.getString("executeEnclosureOnClickLabel"), 
                getFieldEditorParent());
        addField(executeEnclosureOnClickEditor);
    }
}