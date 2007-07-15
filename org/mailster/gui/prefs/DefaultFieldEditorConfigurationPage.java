/*******************************************************************************
 * Copyright notice * * Copyright (c) 2005-2006 Feed'n Read Development Team *
 * http://sourceforge.net/fnr * * All rights reserved. * * This program and the
 * accompanying materials are made available under the * terms of the Common
 * Public License v1.0 which accompanies this distribution,* and is available at *
 * http://www.eclipse.org/legal/cpl-v10.html * * A copy is found in the file
 * cpl-v10.html and important notices to the * license from the team is found in
 * the textfile LICENSE.txt distributed * in this package. * * This copyright
 * notice MUST APPEAR in all copies of the file. * * Contributors: * Feed'n Read -
 * initial API and implementation * (smachhau@users.sourceforge.net) *
 ******************************************************************************/
package org.mailster.gui.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;

/**
 * <code>DefaultFieldEditorConfigurationPage</code> extends
 * <code>FieldEditorPreferencePage</code> to make use of localized button
 * texts and to display the default button images for the ok, cancel, apply and
 * defaults button. All configuration pages that create default
 * <code>FieldEditors</code> should inherit this class.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public abstract class DefaultFieldEditorConfigurationPage
        extends FieldEditorPreferencePage
{
    /**
     * Creates a new <code>DefaultFieldEditorConfigurationPage</code> with the
     * given style, an empty title, and no image.
     * 
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected DefaultFieldEditorConfigurationPage(int style)
    {
        super(style);
    }

    /**
     * Creates a new <code>DefaultFieldEditorConfigurationPage</code> with the
     * given title and style, but no image.
     * 
     * @param title the title of this preference page
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected DefaultFieldEditorConfigurationPage(String title, int style)
    {
        super(title, style);
    }

    /**
     * Creates a new <code>DefaultFieldEditorConfigurationPage</code> with the
     * given title, image, and style.
     * 
     * @param title the title of this preference page
     * @param image the image for this preference page, or <code>null</code>
     *            if none
     * @param style either <code>GRID</code> or <code>FLAT</code>
     */
    protected DefaultFieldEditorConfigurationPage(String title,
            ImageDescriptor image, int style)
    {
        super(title, image, style);
    }

    /**
     * Creates the control for this
     * <code>DefaultFieldEditorConfigurationPage</code>.
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
}