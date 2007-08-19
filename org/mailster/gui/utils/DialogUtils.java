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
package org.mailster.gui.utils;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;

/**
 * Contains <code>Dialog</code> specific utility methods.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 *         This file has been used and modified.
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class DialogUtils
{
    /**
     * Center a shell on screen.
     * 
     * @param shell the shell to center on screen
     */
    public static void centerShellOnScreen(Shell shell)
    {
        Rectangle shellRect = shell.getBounds();
        Rectangle displayRect = shell.getDisplay().getBounds();
        int x = (displayRect.width - shellRect.width) / 2;
        int y = (displayRect.height - shellRect.height) / 2;
        shell.setLocation(x, y);
    }

    /**
     * Center a shell relatively to it's parent.
     * 
     * @param shell the shell to center on screen
     */
    public static void centerShellOnParentShell(Shell shell)
    {
        Rectangle shellRect = shell.getBounds();
        Rectangle displayRect = shell.getParent().getShell().getBounds();
        Point pt = shell.getParent().getLocation();
        int x = pt.x + (displayRect.width - shellRect.width) / 2;
        int y = pt.y + (displayRect.height - shellRect.height) / 2;
        shell.setLocation(x, y);
    }
    
    /**
     * Creates a question dialog. Note that the dialog will have no visual
     * representation (no widgets) until it is told to open. The
     * <code>open</code> method will return either
     * {@link org.eclipse.jface.dialogs.IDialogConstants#YES_ID} if the question
     * dialog was confirmed with YES,
     * {@link org.eclipse.jface.dialogs.IDialogConstants#NO_ID} if the dialog
     * was left with NO or <i>-1</i> if the dialog was dismissed without
     * pressing a button(ESC, etc.). Note that the <code>open</code> method
     * blocks.
     * 
     * @param parentShell the parent shell
     * @param title the dialog title, or <code>null</code> if none
     * @param message the dialog message
     * @param titleImage the dialog title image, or <code>null</code> if none
     * @return the created <code>MessageDialog</code>
     */
    public static MessageDialog createQuestionDialog(Shell parentShell,
            String title, String message, Image titleImage)
    {
        MessageDialog dialog = new MessageDialog(parentShell, title,
                titleImage, message, MessageDialog.QUESTION, new String[] {}, 0) {
            protected void createButtonsForButtonBar(Composite parent)
            {
                Button yesButton = null;
                Button noButton = null;

                if (Display.getDefault().getDismissalAlignment() == SWT.LEFT)
                {
                    // Default dismissal button (YES) has to be on the left side

                    // Create YES Button
                    yesButton = this.createButton(parent,
                            IDialogConstants.YES_ID, Messages
                                    .getString("yesButton"), true);

                    // Create NO Button
                    noButton = this.createButton(parent,
                            IDialogConstants.NO_ID, Messages
                                    .getString("noButton"), false);
                }
                else
                {
                    // Default dismissal button (YES) has to be on the right

                    // Create NO Button
                    noButton = this.createButton(parent,
                            IDialogConstants.NO_ID, Messages
                                    .getString("noButton"), true);

                    // Create YES Button
                    yesButton = this.createButton(parent,
                            IDialogConstants.YES_ID, Messages
                                    .getString("yesButton"), false);
                }
                
                // Set Image for YES button and adjust layout
                yesButton.setImage(SWTHelper.loadImage("button_ok.png"));
                yesButton.setAlignment(SWT.RIGHT);
                this.setButtonLayoutData(yesButton);

                // Set Image for NO button and adjust layout
                noButton.setImage(SWTHelper.loadImage("button_cancel.png"));
                noButton.setAlignment(SWT.RIGHT);
                this.setButtonLayoutData(noButton);
            }
        };
        return dialog;
    }
    
    public static void selectComboValue(ComboViewer viewer, String key, IPreferenceStore store)
    {
    	String[] input = (String[]) viewer.getInput();
    	try
    	{
    		viewer.setSelection(new StructuredSelection(input[store.getInt(key)]));
    	}
    	catch (Exception ex)
    	{
    		viewer.setSelection(new StructuredSelection(input[0]));
    	}
    }
}