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


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.mailster.MailsterSWT;
import org.mailster.core.mail.SmtpHeadersInterface;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultConfigurationPage;
import org.mailster.gui.prefs.store.MailsterPrefStore;
import org.mailster.gui.prefs.utils.JobExecutionInterval;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;

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
 * GeneralConfigurationPage.java - Configuration page for general application settings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author: kods $ / $Date: 2009/01/30 01:32:30 $
 */
public class GeneralConfigurationPage 
    extends DefaultConfigurationPage 
{
    /**
	 * <code>Button</code> for the ask on mail removal setting
	 */
	private Button askOnRemoveMail;

    /**
     * <code>Button</code> to save window size,position and 
     * sash positionnings setting
     */
    private Button saveWindowParamaters;
    
    /**
     * <code>ComboViewer</code> to select the mail queue refresh 
     * interval
     */
    private ComboViewer mailQueueRefreshIntervalViewer;
    
    /**
     * <code>ComboViewer</code> to select the preferred browser
     */
    private ComboViewer preferredBrowserViewer;    
    
    /**
     * <code>ComboViewer</code> to select the preferred browser
     */
    private ComboViewer preferredContentTypeViewer;  
    
    private boolean isXulOptionnal;
    
    private void checkBrowser()
    {
        // If OS is a Windows system and Xul is available, then give a chance to
        // use it.
        boolean xulAvailable = false;
        Shell testShell = new Shell();
        try
        {
            new Browser(testShell, SWT.BORDER | SWT.MOZILLA);            
            xulAvailable = true;
        }
        catch(SWTError e)
        {
        }
        finally
        {
            testShell.dispose();
        }

        isXulOptionnal = xulAvailable
                && System.getProperty("os.name").toLowerCase().startsWith("win");
    }
    
    /**
	 * Creates a new <code>GeneralConfigurationPage</code> instance.
	 */
    public GeneralConfigurationPage() 
    {        
        super(Messages
                .getString("generalConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/generalConfig32.png"));
        checkBrowser();
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
        MailsterSWT main = MailsterSWT.getInstance();
        
        store.setValue(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY, 
                askOnRemoveMail.getSelection());

        store.setValue(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY, 
                saveWindowParamaters.getSelection());
        
        long timeout = ((JobExecutionInterval)((IStructuredSelection)
                mailQueueRefreshIntervalViewer.getSelection()).getFirstElement()).getPeriod();
        store.setValue(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY, timeout);
        main.getSMTPService().setQueueRefreshTimeout(timeout / 1000);
        
        if (isXulOptionnal)
        {
	        int index = preferredBrowserViewer.getCombo().getSelectionIndex();
	        store.setValue(ConfigurationManager.PREFERRED_BROWSER_KEY, index); 
	        main.getMultiView().setForcedMozillaBrowserUse(index != 0);
        }
        else
        {
        	store.setValue(ConfigurationManager.PREFERRED_BROWSER_KEY, 0); 
	        main.getMultiView().setForcedMozillaBrowserUse(false);
        }
        
        int index = preferredContentTypeViewer.getCombo().getSelectionIndex();
        main.getMultiView().getMailView().setPreferredContentType(index == 0 ? 
        			SmtpHeadersInterface.TEXT_HTML_CONTENT_TYPE : 
        			SmtpHeadersInterface.TEXT_PLAIN_CONTENT_TYPE);
        store.setValue(ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY, index);
        
        return true;
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
        content.setLayout(
        		LayoutUtils.createGridLayout(1, false, 0, 0, 5, 0, 0, 0, 3, 0));
        
        // Create display confirmations group
        Group uiGroup = new Group(content, SWT.NONE);
        uiGroup.setLayout(
        		LayoutUtils.createGridLayout(2, false, 5, 5, 0, 0, 0, 0, 5, 5));
        uiGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        uiGroup.setText(Messages.getString("uiGroupHeader"));
        
        Label refreshIntervalLabel = new Label(uiGroup, SWT.LEFT);      
        refreshIntervalLabel.setText(Messages
                .getString("mailQueueRefreshIntervalLabel"));
        refreshIntervalLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false, 2, 1));               
        
        Label refreshIntervalImgLabel = new Label(uiGroup, SWT.LEFT);
        refreshIntervalImgLabel.setImage(
                SWTHelper.loadImage("schedule32.png"));
        refreshIntervalImgLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false));
        
        mailQueueRefreshIntervalViewer = new ComboViewer(
                uiGroup, SWT.BORDER | SWT.READ_ONLY);
        mailQueueRefreshIntervalViewer.setContentProvider(
                new ArrayContentProvider());
        mailQueueRefreshIntervalViewer.setInput(JobExecutionInterval.DEFAULT_INTERVALS);
        
        // Separator
        new Label(uiGroup, SWT.NONE);
        
        askOnRemoveMail = new Button(uiGroup, SWT.CHECK);
        askOnRemoveMail.setText(Messages.getString("askOnRemoveMailLabel"));
        askOnRemoveMail.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.CENTER, false, false, 2, 1));
        
        saveWindowParamaters = new Button(uiGroup, SWT.CHECK);
        saveWindowParamaters.setText(Messages.getString("saveMainWindowParamsLabel"));
        saveWindowParamaters.setLayoutData(
        		LayoutUtils.createGridData(GridData.BEGINNING, 
                		GridData.CENTER, false, false, 2, 1));
        
        // Separator
        new Label(content, SWT.NONE);
        
        // Create display confirmations group
        Group browserGroup = new Group(content, SWT.NONE);
        browserGroup.setLayout(new GridLayout(2, false));                
        browserGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        browserGroup.setText(Messages.getString("browserGroupHeader"));
        
        if (isXulOptionnal)
        {
            Label browserLabel = new Label(browserGroup, SWT.LEFT);      
            browserLabel.setText(Messages
                    .getString("preferredBrowserLabel"));
            browserLabel.setLayoutData(new GridData(GridData.BEGINNING,
                    GridData.CENTER, false, false)); 
            
            preferredBrowserViewer = new ComboViewer(
                    browserGroup, SWT.BORDER | SWT.READ_ONLY);
            preferredBrowserViewer.setContentProvider(
                    new ArrayContentProvider());
            preferredBrowserViewer.setInput(
                    new String[] {
                            Messages.getString("MailsterSWT.default.browser"),  //$NON-NLS-1$
                            Messages.getString("MailsterSWT.mozillaXUL.browser")  //$NON-NLS-1$
                    });        	
        }
                
        Label contentTypeLabel = new Label(browserGroup, SWT.LEFT);      
        contentTypeLabel.setText(Messages
                .getString("preferredContentTypeLabel"));
        contentTypeLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false)); 
        
        preferredContentTypeViewer = new ComboViewer(
                browserGroup, SWT.BORDER | SWT.READ_ONLY);
        preferredContentTypeViewer.setContentProvider(
                new ArrayContentProvider());
        preferredContentTypeViewer.setInput(
        		new String[] { 
                        Messages.getString("MailsterSWT.contentType.html"),  //$NON-NLS-1$
                        Messages.getString("MailsterSWT.contentType.plain"),  //$NON-NLS-1$
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
        
        mailQueueRefreshIntervalViewer.setSelection(
                new StructuredSelection(JobExecutionInterval.getInterval(
                        store.getLong(ConfigurationManager.MAIL_QUEUE_REFRESH_INTERVAL_KEY))));
    
        if (isXulOptionnal)
        	DialogUtils.selectComboValue(preferredBrowserViewer, 
        			ConfigurationManager.PREFERRED_BROWSER_KEY, store);
        
        DialogUtils.selectComboValue(preferredContentTypeViewer, 
    			ConfigurationManager.PREFERRED_CONTENT_TYPE_KEY, store);
        
        askOnRemoveMail.setSelection(
                store.getBoolean(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY));
        
        saveWindowParamaters.setSelection(
                store.getBoolean(ConfigurationManager.APPLY_MAIN_WINDOW_PARAMS_KEY));
    }   
}