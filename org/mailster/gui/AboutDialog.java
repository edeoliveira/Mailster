package org.mailster.gui;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.mailster.MailsterSWT;
import org.mailster.gui.views.MailView;

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
 * AboutDialog.java - A dialog that shows informations about Mailster.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class AboutDialog extends Dialog 
{   
    private MailView mailView;
    
    /**
     * Creates a new <code>AboutDialog</code> instance.
     * 
     * @param shell the parent shell
     */
    public AboutDialog(Shell shell, MailView mailView) 
    {
        super(shell);
        this.mailView = mailView;
    }
    
    
    /**
     * Configures the <code>Shell</code> representing this
     * <code>AboutDialog</code>.
     * 
     * @param shell the<code>Shell</code> to configure
     */
    protected void configureShell(Shell shell) 
    {             
        super.configureShell(shell);
        shell.setText(Messages.getString("MailsterSWT.dialog.about.title"));
        shell.setImage(SWTHelper.loadImage("about.gif"));
    }
    
    
    /**
     * Creates the contents of this <code>AboutDialog</code>.
     * 
     * @param parent the parent <code>Composite</code> in which to embed the
     * created contents
     */
    protected Control createContents(Composite parent) 
    {
        this.createTitleArea(parent);     
        
        // Create the top level composite for the dialog
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);        
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));        
        applyDialogFont(composite);
        
        // Initialize the dialog units
        this.initializeDialogUnits(composite);
        
        // Create the dialog area and button bar
        dialogArea = this.createDialogArea(composite);
        buttonBar = this.createButtonBar(composite);
        
        return (composite);
    }    
    
    /**
     * Adds the buttons to this <code>AboutDialog</code>'s button bar.
     * 
     * @param parent the button bar <code>Composite</code>
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) 
    {
        Button okButton = this.createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("MailsterSWT.dialog.about.ok.label"), true);
        okButton.setImage(SWTHelper.loadImage("button_ok.png"));
        okButton.setAlignment(SWT.RIGHT);
        okButton.forceFocus();
        this.setButtonLayoutData(okButton);        
    }    
    
    /**
     * Creates and returns the contents of the upper part of this
     * <code>AboutDialog</code> (above the button bar).
     * 
     * @param parent the parent <code>Composite</code> to contain 
     * the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) 
    {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        
        Label infoLabel = new Label(dialogArea, SWT.LEFT | SWT.WRAP);
        infoLabel.setText(MessageFormat.format(
                Messages.getString("MailsterSWT.dialog.about.info.label"),
                new Object[] { MailsterSWT.MAILSTER_VERSION_NB }));
        infoLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
                
        Link infoLink = new Link(dialogArea, SWT.NONE);       
        infoLink.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        infoLink.setText("<a>"+MailsterSWT.MAILSTER_HOMEPAGE+"</a>");
        infoLink.setToolTipText(Messages.getString("MailsterSWT.dialog.about.link.tooltip"));
        infoLink.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
                close();
				mailView.showURL(MailsterSWT.MAILSTER_HOMEPAGE, false);
			}
		});
        
        // Create a container for the separator to the buttons
        Composite separatorContainer = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        separatorContainer.setLayoutData(gd);
        GridLayout g = new GridLayout(1, false);
        g.marginWidth = 0;
        g.marginHeight = 0;
        g.verticalSpacing = 5;
        g.horizontalSpacing = 5;
        separatorContainer.setLayout(g);

        /* Create the Separator */
        Label separator = new Label(separatorContainer, SWT.SEPARATOR
                | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));                       
        
        return (dialogArea);                
    }
    
    
    /**
     * Creates this <code>AboutDialog</code>'s title area.
     * 
     * @param parent the SWT parent for the title area widgets
     * @return the title area <code>Control</code>
     */
    private Control createTitleArea(Composite parent) 
    {
        Composite titleContainer = new Composite(parent, SWT.NONE);
        titleContainer.setBackgroundMode(SWT.INHERIT_FORCE);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginBottom = 0;
        layout.verticalSpacing = 0;
        titleContainer.setLayout(layout);
        titleContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label bannerLabel = new Label(titleContainer, SWT.LEFT);
        bannerLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        bannerLabel.setImage(SWTHelper.loadImage("banner.gif"));                     
        
        // Create the Separator
        Label separator = new Label(titleContainer, SWT.SEPARATOR
                | SWT.HORIZONTAL);
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        separator.setLayoutData(layoutData);
        
        return (titleContainer);
    }
}