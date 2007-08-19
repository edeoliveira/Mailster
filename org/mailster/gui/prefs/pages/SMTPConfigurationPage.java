package org.mailster.gui.prefs.pages;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultFieldEditorConfigurationPage;
import org.mailster.gui.prefs.widgets.HostFieldEditor;
import org.mailster.gui.prefs.widgets.SpinnerFieldEditor;


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
 * SMTPConfigurationPage.java - Configuration page for smtp settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class SMTPConfigurationPage 
    extends DefaultFieldEditorConfigurationPage 
{
    /**
     * <code>FieldEditor</code> for the connection timeout: This specifies the
     * timeout (in milliseconds) to establish the connection to the host.
     */
    private SpinnerFieldEditor connectionTimeoutEditor;   
    
    /**
     * <code>FieldEditor</code> for the SMTP server host/address.
     */
    private HostFieldEditor smtpServerEditor;
    
    /**
     * <code>FieldEditor</code> for the SMTP port.
     */
    private SpinnerFieldEditor smtpPortEditor;
    
    /**
     * Creates a new <code>SMTPConfigurationPage</code> instance.
     */
    public SMTPConfigurationPage() 
    {
        super(Messages
                .getString("smtpConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/connectionConfig32.png"),
                DefaultFieldEditorConfigurationPage.GRID);    
    }
    
    /**
     * Validates the settings in this <code>SMTPConfigurationPage</code>.
     * 
     * @return <code>true</code> if this <code>SMTPConfigurationPage</code>
     *         is valid; <code>false</code> if invalid
     */
    public boolean isValid()
    {
        MailsterSWT main = MailsterSWT.getInstance();
        
        if (smtpPortEditor.getIntValue() == main.getSMTPService().getPop3Service().getPort())
        {
        	setValid(false);
            setErrorMessage(Messages.getString("invalidPortMessage"));
            return false;
        }
        
        boolean valid = smtpServerEditor.isValid();
        
        setValid(valid);
        return valid;
    }
    
    /**
     * Notifies that the OK button of this page's container has been pressed.
     * 
     * @return <code>false</code> to abort the container's OK processing and
     * <code>true</code> to allow the OK to happen
     */
    public boolean performOk() 
    {
        if (!isValid())
            return false;

        super.performOk();
        
        MailsterSWT main = MailsterSWT.getInstance();

        main.getSMTPService().setPort(smtpPortEditor.getIntValue());
        main.getSMTPService().setHostName(smtpServerEditor.getStringValue());
        main.getSMTPService().setConnectionTimeout(connectionTimeoutEditor.getIntValue());
        
        return true;
    }    
    
    /**
     * Creates the <code>FieldEditor</code>s of this
     * <code>SMTPConfigurationPage</code>.
     * 
     * @see FieldEditorPreferencePage#createFieldEditors()
     */    
    public void createFieldEditors() 
    {        
        connectionTimeoutEditor = new SpinnerFieldEditor(
                ConfigurationManager.SMTP_CONNECTION_TIMEOUT_KEY, Messages
                        .getString("connectionTimeoutLabel"), getFieldEditorParent());
        connectionTimeoutEditor.setMinimum(1);
        connectionTimeoutEditor.setMaximum(1000);
        connectionTimeoutEditor.setIncrement(1);
        connectionTimeoutEditor.setPageIncrement(10);
        addField(connectionTimeoutEditor);
        
        Label sep = new Label(getFieldEditorParent(), SWT.NONE);
        sep.setLayoutData(
                new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, false, false, 2, 1));
        
        smtpServerEditor = new HostFieldEditor(
                ConfigurationManager.SMTP_SERVER_KEY, Messages
                        .getString("smtpServerLabel"), getFieldEditorParent());
        smtpServerEditor.setEmptyStringAllowed(true);
        smtpServerEditor.setOnlyLocalAddressAllowed(true);
        addField(smtpServerEditor);
        
        smtpPortEditor = new SpinnerFieldEditor(
                ConfigurationManager.SMTP_PORT_KEY, Messages
                        .getString("smtpPortLabel"), getFieldEditorParent());
        smtpPortEditor.setMinimum(0);
        smtpPortEditor.setMaximum(65535);        
        smtpPortEditor.setIncrement(1);
        smtpPortEditor.setPageIncrement(100);
        addField(smtpPortEditor);
    }      
}