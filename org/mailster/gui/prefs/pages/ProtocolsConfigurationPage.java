package org.mailster.gui.prefs.pages;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultFieldEditorConfigurationPage;

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
 * ProtocolsConfigurationPage.java - Configuration page for protocols generic settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class ProtocolsConfigurationPage 
    extends DefaultFieldEditorConfigurationPage 
{
    /**
     * <code>FieldEditor</code> to decide if SMTP service should start 
     * on UI startup.
     */
    private BooleanFieldEditor startSMTPAtStartUpEditor;
    
    /**
     * <code>FieldEditor</code> to decide if POP3 service should start 
     * on SMTP server startup.
     */
    private BooleanFieldEditor startPOP3OnSMTPStartEditor;
    
    /**
	 * Creates a new <code>ProtocolsConfigurationPage</code> instance.
	 */
    public ProtocolsConfigurationPage() 
    {
        super(Messages
                .getString("protocolsConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/connectionConfig32.png"),
                DefaultFieldEditorConfigurationPage.GRID);    
    }    
    
    /**
     * Creates the <code>FieldEditor</code>s of this
     * <code>ProtocolsConfigurationPage</code>.
     * 
     * @see FieldEditorPreferencePage#createFieldEditors()
     */    
    public void createFieldEditors() 
    {
    	startSMTPAtStartUpEditor = new BooleanFieldEditor(
                ConfigurationManager.START_SMTP_ON_STARTUP_KEY,
                Messages.getString("startSMTPAtStartUpLabel"), 
                        getFieldEditorParent());
        addField(startSMTPAtStartUpEditor);
        startSMTPAtStartUpEditor.load();
        
        startPOP3OnSMTPStartEditor = new BooleanFieldEditor(
                ConfigurationManager.START_POP3_ON_SMTP_START_KEY,
                Messages.getString("startPOP3OnSMTPStartLabel"), 
                        getFieldEditorParent());
        addField(startPOP3OnSMTPStartEditor);
        startPOP3OnSMTPStartEditor.load();
    }
}