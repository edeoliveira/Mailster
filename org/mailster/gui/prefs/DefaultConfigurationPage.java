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

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;

/**
 * <code>DefaultConfigurationPage</code> extends <code>PreferencePage</code>
 * to make use of localized button texts and to display the default button
 * images for the ok, cancel, apply and defaults button. All configuration pages
 * that need finer control on the created editor <code>Controls</code> should
 * inherit this class.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public abstract class DefaultConfigurationPage 
    extends PreferencePage
{
    /**
     * <code>DefaultConfigurationPage</code> default constructor.
     */
    public DefaultConfigurationPage()
    {
        super();
    }

    /**
     * Creates a new abstract <code>DefaultConfigurationPage</code> with the
     * given title.
     * 
     * @param title the page title
     */
    public DefaultConfigurationPage(String title)
    {
        super(title);
    }

    /**
     * Creates a new abstract <code>DefaultConfigurationPage</code> with the
     * given title and image.
     * 
     * @param title the page title
     * @param image the image for this page, or <code>null</code> if none
     */
    public DefaultConfigurationPage(String title, ImageDescriptor image)
    {
        super(title, image);
    }

    /**
     * Creates the control for this <code>DefaultConfigurationPage</code>.
     * 
     * @param parent the parent container to embed the created control into
     */
    public void createControl(Composite parent)
    {
        super.createControl(parent);

        Button applyButton = this.getApplyButton();
        Button defaultsButton = this.getDefaultsButton();
        if (applyButton != null && defaultsButton != null)
        {
            /* Apply and default button are shown */

            /* Customize apply button (text + image) */
            applyButton.setText(Messages.getString("applyButton"));
            applyButton.setImage(SWTHelper.loadImage("save.gif"));
            this.setButtonLayoutData(applyButton);

            /* Customize defaults button (text + image) */
            defaultsButton.setText(Messages.getString("defaultsButton"));
            defaultsButton.setImage(SWTHelper.loadImage("clear.gif"));
            this.setButtonLayoutData(defaultsButton);
        }
    }
    
    /**
     * Adds the editor to the current preference page. Links it with the 
     * page IPreferenceStore and then load stored value.
     * 
     * @param editor the field editor to setup 
     */
    public void setupEditor(FieldEditor editor)
    {
        editor.setPage(this);
        editor.setPreferenceStore(getPreferenceStore());
        editor.load();
    }
}
