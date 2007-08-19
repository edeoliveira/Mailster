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
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
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
 * @version $Revision$, $Date$
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
    
    public void create()
    {
        super.create();        
        getShell().setSize(getShell().computeSize(540, SWT.DEFAULT, true));
        DialogUtils.centerShellOnParentShell(getShell());
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
        DialogUtils.centerShellOnScreen(shell);
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
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);        
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));        
        applyDialogFont(composite);
        
        // Initialize the dialog units
        this.initializeDialogUnits(composite);
        
        // Create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        buttonBar = createButtonBar(composite);
        
        return composite;
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
        Button okButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("MailsterSWT.dialog.about.ok.label"), true);
        okButton.forceFocus();
        setButtonLayoutData(okButton);        
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
        GridLayout layout = (GridLayout) dialogArea.getLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        
        Label infoLabel = new Label(dialogArea, SWT.LEFT | SWT.WRAP);
        infoLabel.setText(MessageFormat.format(                
                Messages.getString("MailsterSWT.dialog.about.info.label"),
                new Object[] { ConfigurationManager.MAILSTER_VERSION_NB }));
        infoLabel.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.CENTER, 
                true, false, 2, 1));
                
        Link infoLink = new Link(dialogArea, SWT.NONE);       
        infoLink.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        infoLink.setText("<a>"+ConfigurationManager.MAILSTER_HOMEPAGE+"</a>");
        infoLink.setToolTipText(Messages.getString("MailsterSWT.dialog.about.link.tooltip"));
        infoLink.addListener (SWT.Selection, new Listener () {
			public void handleEvent(Event event) {
                close();
				mailView.showURL(ConfigurationManager.MAILSTER_HOMEPAGE, false);
			}
		});
        infoLink.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.CENTER, true, false, 2, 1));
        
        Label minaLogo = new Label(dialogArea, SWT.NONE);
        minaLogo.setImage(SWTHelper.loadImage("mina.png"));
        minaLogo.setLayoutData(LayoutUtils.createGridData(
                        GridData.END, GridData.CENTER, true, false, 1, 1));
        
        Label gnuLogo = new Label(dialogArea, SWT.NONE);
        gnuLogo.setImage(SWTHelper.loadImage("gnu.png"));
        gnuLogo.setLayoutData(LayoutUtils.createGridData(
                GridData.END, GridData.CENTER, false, false, 1, 1));
        
        // Create a container for the separator
        Composite separatorContainer = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        separatorContainer.setLayoutData(gd);
        separatorContainer.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 5, 5));

        // Create the Separator
        Label separator = new Label(separatorContainer, SWT.SEPARATOR
                | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));                       
        
        return dialogArea;                
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
        //titleContainer.setBackgroundMode(SWT.INHERIT_FORCE);
        titleContainer.setBackground(SWTHelper.createColor(79, 129, 191));
        
        titleContainer.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));
        titleContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Label bannerLabel = new Label(titleContainer, SWT.LEFT);
        bannerLabel.setBackground(
                Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
        bannerLabel.setImage(SWTHelper.loadImage("banner.gif"));                     
        
        // Create the Separator
        Label separator = new Label(titleContainer, SWT.SEPARATOR
                | SWT.HORIZONTAL);
        GridData layoutData = new GridData();
        layoutData.grabExcessHorizontalSpace = true;
        layoutData.horizontalAlignment = GridData.FILL;
        separator.setLayoutData(layoutData);
        
        return titleContainer;
    }
}