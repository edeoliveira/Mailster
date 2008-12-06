package org.mailster.gui.prefs.pages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.mailster.MailsterSWT;
import org.mailster.crypto.MailsterKeyStoreFactory;
import org.mailster.crypto.X509SecureSocketFactory.SSLProtocol;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultConfigurationPage;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.prefs.widgets.SpinnerFieldEditor;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.pop3.connection.MinaPop3Connection;

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
 * ProtocolsConfigurationPage.java - Configuration page for protocols generic settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class ProtocolsConfigurationPage 
    extends DefaultConfigurationPage 
{
    /**
     * <code>Button</code> to decide if SMTP service should start 
     * on UI startup.
     */
    private Button startSMTPAtStartUpEditor;
    
    /**
     * <code>Button</code> to decide if POP3 service should start 
     * on SMTP server startup.
     */
    private Button startPOP3OnSMTPStartEditor;
    
    /**
     * <code>Button</code> to decide if SSL clients should be authenticated. 
     */
    private Button authSSLClientsStartEditor;    
    
    /**
     * <code>ComboViewer</code> to select the preferred SSL protocol
     */
    private ComboViewer preferredSSLProtocolViewer;
    
    /**
     * <code>FieldEditor</code> to select the crypto strength of the automatically 
     * generated certificates.
     */
    private SpinnerFieldEditor cryptoStrengthEditor;
    
    /**
	 * Creates a new <code>ProtocolsConfigurationPage</code> instance.
	 */
    public ProtocolsConfigurationPage() 
    {
        super(Messages
                .getString("protocolsConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/connectionConfig32.png"));    
    }    
    
    /**
     * Creates and returns the SWT control for the customized body of this
     * preference page under the given parent composite.
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
        content.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 5, 5, 0, 0, 0, 0));

        // Create general group
        Group generalGroup = new Group(content, SWT.NONE);
        generalGroup.setLayout(
        		LayoutUtils.createGridLayout(2, false, 5, 5, 0, 0, 0, 0, 5, 5));
        generalGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        generalGroup.setText(Messages.getString("protocolsGroupHeader"));
        
    	startSMTPAtStartUpEditor = new Button(generalGroup, SWT.CHECK);
    	startSMTPAtStartUpEditor.setText(Messages.getString("startSMTPAtStartUpLabel"));
    	startSMTPAtStartUpEditor.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.CENTER, false, false, 2, 1));
        
        startPOP3OnSMTPStartEditor = new Button(generalGroup, SWT.CHECK);
        startPOP3OnSMTPStartEditor.setText(Messages.getString("startPOP3OnSMTPStartLabel"));
        startPOP3OnSMTPStartEditor.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
                		GridData.CENTER, false, false, 2, 1));
        
        // Separator
        new Label(content, SWT.NONE);
        
        // Create SSL group
        Group sslGroup = new Group(content, SWT.NONE);
        sslGroup.setLayout(LayoutUtils.createGridLayout(2, false, 0, 0, 5, 5, 5, 5, 5, 5));
        sslGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sslGroup.setText(Messages.getString("sslGroupHeader"));

        authSSLClientsStartEditor = new Button(sslGroup, SWT.CHECK);
        authSSLClientsStartEditor.setText(Messages.getString("authSSLClientsStartLabel"));
        authSSLClientsStartEditor.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
                		GridData.CENTER, false, false, 2, 1));
        
        Label protocolLabel = new Label(sslGroup, SWT.LEFT);      
        protocolLabel.setText(Messages.getString("preferredSSLProtocolLabel"));
        protocolLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false)); 
        
        preferredSSLProtocolViewer = new ComboViewer(sslGroup, SWT.BORDER | SWT.READ_ONLY);
        preferredSSLProtocolViewer.setContentProvider(new ArrayContentProvider());
        preferredSSLProtocolViewer.setInput(
                new String[] {
                        SSLProtocol.SSL.toString(),
                        SSLProtocol.TLS.toString(),
                });
        
        // Separator
        new Label(content, SWT.NONE);
        
        // Create crypto group
        Group cryptoGroup = new Group(content, SWT.NONE);
        cryptoGroup.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 5, 5, 5, 5, 5, 5));
        cryptoGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        cryptoGroup.setText(Messages.getString("cryptoGroupHeader"));
        
        Composite c = new Composite(cryptoGroup, SWT.NONE);
        c.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 5, 5, 0, 0, 0, 0));
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        cryptoStrengthEditor = new SpinnerFieldEditor(
                ConfigurationManager.CRYPTO_STRENGTH_KEY, Messages.getString("cryptoStrengthLabel"), c, 4);
        cryptoStrengthEditor.setMinimum(512);
        cryptoStrengthEditor.setMaximum(4096);        
        cryptoStrengthEditor.setIncrement(128);
        cryptoStrengthEditor.setPageIncrement(512);
        setupEditor(cryptoStrengthEditor);
        
        Button generateButton = new Button(cryptoGroup, SWT.PUSH);
        generateButton.setText(Messages.getString("generateCryptoKeysAndCerts"));
        generateButton.setLayoutData(
        		LayoutUtils.createGridData(GridData.END, GridData.CENTER, true, false, 2, 1));
        generateButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					MailsterKeyStoreFactory.regenerate();
				} 
				catch (Exception ex) 
				{
					MailsterSWT.getInstance().log(ex.getMessage());
				}
			}		
		});
        
        load();        
        return content;
    }
    
    /**
     * Loads all stored values in the <code>FieldEditor</code>s.
     */
    protected void load() 
    {
    	IPreferenceStore store = getPreferenceStore();
    	
    	startSMTPAtStartUpEditor.setSelection(
    			store.getBoolean(ConfigurationManager.START_SMTP_ON_STARTUP_KEY));

    	startPOP3OnSMTPStartEditor.setSelection(
    			store.getBoolean(ConfigurationManager.START_POP3_ON_SMTP_START_KEY));
    	
    	authSSLClientsStartEditor.setSelection(
    			store.getBoolean(ConfigurationManager.AUTH_SSL_CLIENT_KEY));
        
        DialogUtils.selectComboValue(preferredSSLProtocolViewer, 
        			ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY, store);
    }
    
    /**
     * Notifies that the OK button of this page's container has been pressed.
     * 
     * @return <code>false</code> to abort the container's OK processing and
     * <code>true</code> to allow the OK to happen
     */
    public boolean performOk() 
    {
        MailsterPrefStore store = (MailsterPrefStore) getPreferenceStore();
        
    	store.setValue(ConfigurationManager.START_SMTP_ON_STARTUP_KEY, 
    			startSMTPAtStartUpEditor.getSelection());

        store.setValue(ConfigurationManager.START_POP3_ON_SMTP_START_KEY, 
        		startPOP3OnSMTPStartEditor.getSelection());
        
        store.setValue(ConfigurationManager.AUTH_SSL_CLIENT_KEY, 
        		authSSLClientsStartEditor.getSelection());
        
        cryptoStrengthEditor.store();
        
        int index = preferredSSLProtocolViewer.getCombo().getSelectionIndex();
        store.setValue(ConfigurationManager.PREFERRED_SSL_PROTOCOL_KEY, index);
        String selection = preferredSSLProtocolViewer.getCombo().getItem(index).toString();
        SSLProtocol protocol = SSLProtocol.SSL.equals(selection) ? SSLProtocol.SSL : SSLProtocol.TLS;
        MinaPop3Connection.setupSSLParameters(protocol, authSSLClientsStartEditor.getSelection());
        
        return true;
    }    
}