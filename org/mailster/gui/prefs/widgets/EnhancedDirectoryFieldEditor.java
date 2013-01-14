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
package org.mailster.gui.prefs.widgets;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.swt.widgets.Composite;

/**
 * Extends the original JFace <code>DirectoryFieldEditor</code> to enable the
 * configuration of the validation strategy via constructor. This can either be
 * <code>VALIDATE_ON_KEY_STROKE</code> to perform on the fly checking (the
 * default), or <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation only
 * after the text has been typed in.
 * 
 * @author <a href="mailto:Sebastian.Machhausen@gmail.com">Sebastian Machhausen</a>
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class EnhancedDirectoryFieldEditor
        extends org.eclipse.jface.preference.DirectoryFieldEditor
{
    /**
     * Creates a new <code>DirectoryFieldEditor</code>.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     * @param strategy either <code>VALIDATE_ON_KEY_STROKE</code> to perform
     *            on the fly checking (the default), or
     *            <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation
     *            only after the text has been typed in
     */
    public EnhancedDirectoryFieldEditor(String name, String labelText,
            Composite parent, int strategy)
    {
        init(name, labelText);
        setErrorMessage(JFaceResources
                .getString("DirectoryFieldEditor.errorMessage"));
        setChangeButtonText(JFaceResources.getString("openBrowse"));
        setValidateStrategy(strategy);
        createControl(parent);
    }
}
