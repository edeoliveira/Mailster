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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.mailster.gui.SWTHelper;

/**
 * The DialogMessageArea is a resusable component for adding an accessible
 * message area to a dialog.
 * <p>
 * When the message is normal a CLabel is used but an errors replaces the
 * message area with a non editable text that can take focus for use by screen
 * readers.
 * </p>
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class DialogMessageArea
{
    /** 
     * The gradient used for banner titles 
     */
    public final static Color[] DEFAULT_GRADIENT_BACKGROUND = new Color[] {
            SWTHelper.createColor(243, 245, 248),
            Display.getDefault().getSystemColor(SWT.COLOR_WHITE),
            SWTHelper.createColor(243, 245, 248) };

    /** 
     * The Text component to hold the message 
     */
    private Text messageText;
    
    /** 
     * The label to hold the Image of the message 
     */
    private Label messageImageLabel;
    
    /** 
     * The container to hold message and message Image 
     */
    private Composite messageComposite;
    
    /** 
     * The last message text 
     */
    private String lastMessageText;
    
    /** 
     * The last message type 
     */
    private int lastMessageType;
    
    /** 
     * The label to hold the title 
     */
    private CLabel titleLabel;

    /**
     * Create the contents for the receiver.
     * 
     * @param parent the Composite that the children will be created in
     */
    public void createContents(Composite parent)
    {
        /* Create the title label */
        this.titleLabel = new CLabel(parent, SWT.LEFT);
        int[] alpha = new int[] { 75, 100 };
        this.titleLabel.setBackground(DEFAULT_GRADIENT_BACKGROUND, alpha);
        this.titleLabel.setFont(JFaceResources.getBannerFont());
        this.titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        this.titleLabel.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e)
            {
                e.gc.setForeground(Display.getCurrent().getSystemColor(
                        SWT.COLOR_DARK_GRAY));
                Rectangle bounds = titleLabel.getClientArea();
                bounds.height -= 2;
                bounds.width -= 1;
                e.gc.drawRectangle(bounds);
            }
        });

        /* Create the message container */
        this.messageComposite = new Composite(parent, SWT.NONE);
        GridLayout messageLayout = new GridLayout(2, false);
        messageLayout.marginWidth = 0;
        messageLayout.marginHeight = 0;
        this.messageComposite.setLayout(messageLayout);

        /* Create the message image holder */
        this.messageImageLabel = new Label(this.messageComposite, SWT.NONE);
        this.messageImageLabel.setImage(JFaceResources
                .getImage(Dialog.DLG_IMG_MESSAGE_INFO));
        this.messageImageLabel.setLayoutData(new GridData(
                GridData.VERTICAL_ALIGN_CENTER));

        /* Create the message text holder */
        this.messageText = new Text(this.messageComposite, SWT.NONE);
        this.messageText.setEditable(false);

        GridData textData = new GridData(GridData.GRAB_HORIZONTAL
                | GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER);
        this.messageText.setLayoutData(textData);
    }

    /**
     * Set the layoutData for the title area. In most cases this will be a copy
     * of the layoutData used in setMessageLayoutData.
     * 
     * @param layoutData the layoutData for the title
     * @see #setMessageLayoutData(Object)
     */
    public void setTitleLayoutData(Object layoutData)
    {
        this.titleLabel.setLayoutData(layoutData);
    }

    /**
     * Set the layoutData for the messageArea. In most cases this will be a copy
     * of the layoutData used in setTitleLayoutData.
     * 
     * @param layoutData the layoutData for the message area composite.
     * @see #setTitleLayoutData(Object)
     */
    public void setMessageLayoutData(Object layoutData)
    {
        this.messageComposite.setLayoutData(layoutData);
    }

    /**
     * Show the title.
     * 
     * @param titleMessage String for the titke
     * @param titleImage Image or <code>null</code>
     */
    public void showTitle(String titleMessage, Image titleImage)
    {
        this.titleLabel.setImage(titleImage);
        this.titleLabel.setText(titleMessage);
        this.restoreTitle();
        return;
    }

    /**
     * Enables the title and disable the message text and image.
     */
    public void restoreTitle()
    {
        this.titleLabel.setVisible(true);
        this.messageComposite.setVisible(false);
        this.lastMessageText = null;
        this.lastMessageType = IMessageProvider.NONE;
    }

    /**
     * Show the new message in the message text and update the image. Base the
     * background color on whether or not there are errors.
     * 
     * @param newMessage The new value for the message
     * @param newType One of the IMessageProvider constants. If newType is
     *            IMessageProvider.NONE show the title.
     * @see org.eclipse.jface.dialogs.IMessageProvider
     */
    public void updateText(String newMessage, int newType)
    {
        Image newImage = null;
        switch (newType)
        {
            case IMessageProvider.NONE :
                if (newMessage == null)
                {
                    this.restoreTitle();
                }
                else
                {
                    this.showTitle(newMessage, null);
                }
                return;
            case IMessageProvider.INFORMATION :
                newImage = JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_INFO);
                break;
            case IMessageProvider.WARNING :
                newImage = JFaceResources
                        .getImage(Dialog.DLG_IMG_MESSAGE_WARNING);
                break;
            case IMessageProvider.ERROR :
                newImage = JFaceResources
                        .getImage(Dialog.DLG_IMG_MESSAGE_ERROR);
                break;
        }

        this.messageComposite.setVisible(true);
        this.titleLabel.setVisible(false);
        // Any more updates required?
        // If the message text equals the tooltip (i.e. non-shortened text is
        // the same)
        // and shortened text is the same (i.e. not a resize)
        // and the image is the same then nothing to do
        String shortText = Dialog.shortenText(newMessage, messageText);
        if (newMessage.equals(messageText.getToolTipText())
                && newImage == messageImageLabel.getImage()
                && shortText.equals(messageText.getText()))
        {
            return;
        }
        this.messageImageLabel.setImage(newImage);
        this.messageText.setText(Dialog.shortenText(newMessage, messageText));
        this.messageText.setToolTipText(newMessage);
        this.lastMessageText = newMessage;
    }

    /**
     * Clears the error message. Restore the previously displayed message if
     * there is one, if not restore the title label.
     */
    public void clearErrorMessage()
    {
        if (lastMessageText == null)
        {
            this.restoreTitle();
        }
        else
        {
            this.updateText(lastMessageText, lastMessageType);
        }
    }
}
