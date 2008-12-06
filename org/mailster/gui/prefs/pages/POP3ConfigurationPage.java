package org.mailster.gui.prefs.pages;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultConfigurationPage;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.prefs.widgets.HostFieldEditor;
import org.mailster.gui.prefs.widgets.SpinnerFieldEditor;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.pop3.Pop3ProtocolHandler;
import org.mailster.pop3.mailbox.UserManager;

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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * POP3ConfigurationPage.java - Configuration page for POP3 settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class POP3ConfigurationPage 
    extends DefaultConfigurationPage
{
    /**
     * <code>Button</code> to decide if only the secured authentication 
     * methods are allowed.
     */
    private Button requireSecuredAuthMethodsEditor;
    
    /**
     * <code>Button</code> to decide if APOP authentication is allowed.
     */
    private Button allowAPOPEditor;    

    /**
     * <code>FieldEditor</code> for the POP3 server host/address.
     */
    private HostFieldEditor pop3ServerEditor;
    
    /**
     * <code>FieldEditor</code> for the POP3 port.
     */
    private SpinnerFieldEditor pop3PortEditor;
    
    /**
     * <code>FieldEditor</code> for the connection timeout: This specifies the
     * timeout (in milliseconds) to establish the connection to the host.
     */
    private SpinnerFieldEditor connectionTimeoutEditor;
    
    /**
     * <code>FieldEditor</code> for the special account name
     */
    private StringFieldEditor pop3SpecialAccountNameEditor;
    
    /**
     * <code>FieldEditor</code> for pop3 accounts password
     */
    private StringFieldEditor pop3PasswordEditor;
    
    /**
     * Creates a new <code>Pop3ConfigurationPage</code> instance.
     */
    public POP3ConfigurationPage() 
    {
        super(Messages
                .getString("pop3ConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/proxyConfig32.png"));    
    }
    
    /**
     * Validates the settings in this <code>Pop3ConfigurationPage</code>.
     * 
     * @return <code>true</code> if this <code>Pop3ConfigurationPage</code>
     *         is valid; <code>false</code> if invalid
     */
    public boolean isValid()
    {
        MailsterSWT main = MailsterSWT.getInstance();
        
        if (pop3PortEditor.getIntValue() == main.getSMTPService().getPort())
        {
            setErrorMessage(Messages.getString("invalidPortMessage"));
            setValid(false);
            return false;
        }
        
        boolean valid = pop3ServerEditor.isValid();
        
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
        
        MailsterPrefStore store = (MailsterPrefStore) getPreferenceStore();
        MailsterSWT main = MailsterSWT.getInstance();
        
        store.setValue(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY, 
        		requireSecuredAuthMethodsEditor.getSelection());
        store.setValue(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY, 
        		allowAPOPEditor.getSelection());
        
        pop3ServerEditor.store();
        pop3PortEditor.store();
        pop3PasswordEditor.store();
        pop3SpecialAccountNameEditor.store();
        connectionTimeoutEditor.store();
        
        main.getSMTPService().getPop3Service().setPort(pop3PortEditor.getIntValue());
        main.getSMTPService().getPop3Service().
        	getUserManager().getMailBoxManager().
        	setPop3SpecialAccountLogin(pop3SpecialAccountNameEditor.getStringValue());
        main.getSMTPService().getPop3Service().setHost(pop3ServerEditor.getStringValue());
        main.getSMTPService().getPop3Service().
        	setUsingAPOPAuthMethod(allowAPOPEditor.getSelection());
        main.getSMTPService().getPop3Service().
    		setSecureAuthRequired(requireSecuredAuthMethodsEditor.getSelection());
        UserManager.setDefaultPassword(pop3PasswordEditor.getStringValue());
        Pop3ProtocolHandler.setTimeout(connectionTimeoutEditor.getIntValue());
        
        return true;
    }    
    
    /**
     * <p>
     * Creates and returns the SWT control for the customized body of this
     * preference page under the given parent composite.
     * </p>
     * 
     * @param parent the parent composite
     * @return the new control
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {                
        // Create content composite
        Composite content = new Composite(parent, SWT.NONE);
        content.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 5, 5, 0, 0, 0, 0));
        
        // Create archive directory group
        Group pop3GeneralGroup = new Group(content, SWT.NONE);
        pop3GeneralGroup.setLayout(new GridLayout());
        pop3GeneralGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pop3GeneralGroup.setText(Messages.getString("pop3GeneralGroupHeader"));
        
        Composite pop3GeneralOptions = new Composite(pop3GeneralGroup, SWT.NONE);
        pop3GeneralOptions.setLayout(
                LayoutUtils.createGridLayout(1, false, 5, 5, 5, 5, 0, 5, 0, 0));
        pop3GeneralOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        connectionTimeoutEditor = new SpinnerFieldEditor(
                ConfigurationManager.POP3_CONNECTION_TIMEOUT_KEY, Messages
                        .getString("connectionTimeoutLabel"), pop3GeneralOptions);
        connectionTimeoutEditor.setMinimum(1);
        connectionTimeoutEditor.setMaximum(1000);
        connectionTimeoutEditor.setIncrement(1);
        connectionTimeoutEditor.setPageIncrement(10);
        setupEditor(connectionTimeoutEditor);

        // Separator
        Label sep = new Label(pop3GeneralOptions, SWT.NONE);
        sep.setLayoutData(
                new GridData(GridData.FILL_HORIZONTAL, GridData.CENTER, false, false, 2, 1));
        
        pop3ServerEditor = new HostFieldEditor(
                ConfigurationManager.POP3_SERVER_KEY, Messages
                        .getString("pop3ServerLabel"), pop3GeneralOptions);
        pop3ServerEditor.setEmptyStringAllowed(true);
        pop3ServerEditor.setOnlyLocalAddressAllowed(true);
        setupEditor(pop3ServerEditor);
        
        pop3PortEditor = new SpinnerFieldEditor(
                ConfigurationManager.POP3_PORT_KEY, Messages
                        .getString("pop3PortLabel"), pop3GeneralOptions, 5);
        pop3PortEditor.setMinimum(0);
        pop3PortEditor.setMaximum(65535);        
        pop3PortEditor.setIncrement(1);
        pop3PortEditor.setPageIncrement(100);        
        setupEditor(pop3PortEditor);
        
    	requireSecuredAuthMethodsEditor = new Button(pop3GeneralOptions, SWT.CHECK);
    	requireSecuredAuthMethodsEditor.setText(Messages.getString("requireSecuredAuthMethodsLabel"));
    	requireSecuredAuthMethodsEditor.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.CENTER, false, false, 2, 1));
    	requireSecuredAuthMethodsEditor.setSelection(
    			getPreferenceStore().getBoolean(ConfigurationManager.POP3_REQUIRE_SECURE_AUTH_METHOD_KEY));
    	
    	allowAPOPEditor = new Button(pop3GeneralOptions, SWT.CHECK);
    	allowAPOPEditor.setText(Messages.getString("allowAPOPLabel"));
    	allowAPOPEditor.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.CENTER, false, false, 2, 1));
    	allowAPOPEditor.setSelection(
    			getPreferenceStore().getBoolean(ConfigurationManager.POP3_ALLOW_APOP_AUTH_METHOD_KEY));    	
        
        // Separator
        new Label(content, SWT.NONE);
        
        // Create pop3 accounts group
        Group pop3AccountsGroup = new Group(content, SWT.NONE);
        pop3AccountsGroup.setLayout(new GridLayout());                
        pop3AccountsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        pop3AccountsGroup.setText(Messages.getString("pop3AccountsGroupHeader"));
        
        Composite pop3AccountsOptions = new Composite(pop3AccountsGroup, SWT.NONE);
        pop3AccountsOptions.setLayout(
                LayoutUtils.createGridLayout(1, false, 5, 5, 5, 5, 0, 5, 0, 0));
        pop3AccountsOptions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        pop3SpecialAccountNameEditor = new StringFieldEditor(
                ConfigurationManager.POP3_SPECIAL_ACCOUNT_KEY, Messages
                        .getString("pop3SpecialAccountLabel"),
                        pop3AccountsOptions);
        pop3SpecialAccountNameEditor.setPage(this);
        pop3SpecialAccountNameEditor.setPreferenceStore(this.getPreferenceStore());  
        setupEditor(pop3SpecialAccountNameEditor);
        
        pop3PasswordEditor = new StringFieldEditor(
                ConfigurationManager.POP3_PASSWORD_KEY, Messages
                        .getString("pop3PasswordLabel"),
                        pop3AccountsOptions);
        pop3PasswordEditor
                .getTextControl(pop3AccountsOptions).setEchoChar('*');
        setupEditor(pop3PasswordEditor);
        
        return content;
    }
}