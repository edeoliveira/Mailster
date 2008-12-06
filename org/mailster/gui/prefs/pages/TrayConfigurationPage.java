/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005-2006 Feed'n Read Development Team                             *
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.prefs.DefaultConfigurationPage;
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * ProtocolsConfigurationPage.java - Configuration page to change the 
 * application's tray behaviour.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class TrayConfigurationPage 
    extends DefaultConfigurationPage 
{
    /** 
     * <code>FieldEditor</code> to toggle send on tray on minimize option
     */
    private BooleanFieldEditor sendToTrayOnMinimizeEditor;

	/**
	 * <code>FieldEditor</code> to toggle send to tray on close option
	 */
	private BooleanFieldEditor sendToTrayOnCloseEditor;

	/**
	 * <code>FieldEditor</code> to toggle send to tray on server start
	 * option
	 */
	private BooleanFieldEditor sendToTrayOnServerStartEditor;

	/**
	 * <code>FieldEditor</code> for the maximum number of news to display in a
	 * notification popup
	 */
	private BooleanFieldEditor autoHideNotificationsEditor;

	/**
	 * <code>FieldEditor</code> to select the display time of a notification
	 * popup
	 */
	private BooleanFieldEditor notifyOnNewMessagesReceivedEditor;
    
    
    /**
     * Creates a new <code>TrayConfigurationPage</code> instance.
     */
    public TrayConfigurationPage() 
    {
        super(Messages
                .getString("trayConfigurationPageTitle"), SWTHelper
                .getImageDescriptor("wizard/trayConfig32.png"));     
    }
    
    /**
     * Notifies that the OK button of this page's container has been pressed.
     * 
     * @return <code>false</code> to abort the container's OK processing and
     * <code>true</code> to allow the OK to happen
     */
    public boolean performOk() 
    {
        sendToTrayOnMinimizeEditor.store();
        sendToTrayOnCloseEditor.store();
        sendToTrayOnServerStartEditor.store();
        autoHideNotificationsEditor.store();
        notifyOnNewMessagesReceivedEditor.store();
        
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
                LayoutUtils.createGridLayout(1, false, 0, 0, 5, 5, 0, 0, 0, 0));
        
        // Create tray options group
        Group trayOptionsGroup = new Group(content, SWT.NONE);
        trayOptionsGroup.setLayout(new GridLayout());                
        trayOptionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        trayOptionsGroup.setText(Messages.getString("trayOptionsGroupHeader"));
        
        Composite trayOptions = new Composite(trayOptionsGroup, SWT.NONE);
        trayOptions.setLayout(
                LayoutUtils.createGridLayout(1, false, 5, 5, 5, 5, 0, 5, 0, 0));
        
        sendToTrayOnMinimizeEditor = new BooleanFieldEditor(
                ConfigurationManager.SEND_TO_TRAY_ON_MINIMIZE_KEY, Messages
                        .getString("sendToTrayOnMinimizeLabel"), trayOptions);
        setupEditor(sendToTrayOnMinimizeEditor);

        sendToTrayOnCloseEditor = new BooleanFieldEditor(
                ConfigurationManager.SEND_TO_TRAY_ON_CLOSE_KEY, Messages
                        .getString("sendToTrayOnCloseLabel"), trayOptions);
        setupEditor(sendToTrayOnCloseEditor);

        sendToTrayOnServerStartEditor = new BooleanFieldEditor(
                ConfigurationManager.SEND_TO_TRAY_ON_SERVER_START_KEY, Messages
                        .getString("sendToTrayOnServerStartLabel"), trayOptions);
        setupEditor(sendToTrayOnServerStartEditor);                
                           
        // Separator
        new Label(content, SWT.NONE);        
        
        // Create tray notification options group
        Group trayNotificationOptionsGroup = new Group(content, SWT.NONE);
        trayNotificationOptionsGroup.setLayout(new GridLayout());
        trayNotificationOptionsGroup.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        trayNotificationOptionsGroup.setText(Messages
                .getString("trayNotificationOptionsGroupHeader"));
        
        Composite trayNotificationOptions = new Composite(
                trayNotificationOptionsGroup, SWT.NONE);
        trayNotificationOptions.setLayout(LayoutUtils.createGridLayout(1,
                false, 5, 5, 5, 5, 0, 5, 0, 0));
        
        autoHideNotificationsEditor = new BooleanFieldEditor(
                ConfigurationManager.AUTO_HIDE_NOTIFICATIONS_KEY, Messages
                        .getString("autoHideNotificationsLabel"), trayNotificationOptions);
        setupEditor(autoHideNotificationsEditor);              

        notifyOnNewMessagesReceivedEditor = new BooleanFieldEditor(
                ConfigurationManager.NOTIFY_ON_NEW_MESSAGES_RECEIVED_KEY, Messages
                        .getString("notifyOnNewMessagesReceivedLabel"), trayNotificationOptions);
        setupEditor(notifyOnNewMessagesReceivedEditor);              

        return content;
    }
}