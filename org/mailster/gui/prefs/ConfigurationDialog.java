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

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.prefs.pages.EnclosuresConfigurationPage;
import org.mailster.gui.prefs.pages.GeneralConfigurationPage;
import org.mailster.gui.prefs.pages.LanguageConfigurationPage;
import org.mailster.gui.prefs.pages.POP3ConfigurationPage;
import org.mailster.gui.prefs.pages.ProtocolsConfigurationPage;
import org.mailster.gui.prefs.pages.SMTPConfigurationPage;
import org.mailster.gui.prefs.pages.TrayConfigurationPage;
import org.mailster.gui.prefs.widgets.DialogMessageArea;
import org.mailster.gui.prefs.widgets.ExtendedPreferenceNode;
import org.mailster.gui.utils.DialogUtils;

/**
 * <code>Dialog</code> to manage the application configuration on end-user
 * side.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 *         This file has been used and modified.
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class ConfigurationDialog extends PreferenceDialog
{
    /**
     * The customized <code>DialogMessageArea</code>
     */
    private DialogMessageArea messageArea;

    /**
     * The last successful <code>IPreferenceNode</code> visited
     */
    private IPreferenceNode lastSuccessfulNode;

    /**
     * Creates a new <code>ConfigurationDialog</code> under the control of the
     * given <code>PreferenceManager</code>.
     * 
     * @param parentShell the parent <code>Shell</code>l
     * @param manager the <code>PreferenceManager</code> which controls this
     *            <code>ConfigurationDialog</code>
     */
    public ConfigurationDialog(Shell parentShell, PreferenceManager manager)
    {
        super(parentShell, manager);
    }
    
    public void create()
    {
        super.create();        
        getShell().setSize(getShell().computeSize(540, SWT.DEFAULT, true));
    }    

    /**
     * <p>
     * Sets the message for this dialog with an indication of what type of
     * message it is.
     * </p>
     * <p>
     * The valid message types are one of <code>NONE</code>,
     * <code>INFORMATION</code>,<code>WARNING</code>, or
     * <code>ERROR</code>.
     * </p>
     * <p>
     * Note that for backward compatibility, a message of type
     * <code>ERROR</code> is different than an error message (set using
     * <code>setErrorMessage</code>). An error message overrides the current
     * message until the error message is cleared. This method replaces the
     * current message and does not affect the error message.
     * </p>
     * 
     * @param newMessage the message, or <code>null</code> to clear the
     *            message
     * @param newType the message type
     * @see org.eclipse.jface.preference.PreferenceDialog#setMessage(String,
     *      int)
     */
    public void setMessage(String newMessage, int newType)
    {
        this.messageArea.updateText(newMessage, newType);
    }

    /**
     * Display the given error message. The currently displayed message is saved
     * and will be redisplayed when the error message is set to
     * <code>null</code>.
     * 
     * @param newErrorMessage the errorMessage to display or <code>null</code>
     */
    public void setErrorMessage(String newErrorMessage)
    {
        if (newErrorMessage == null)
        {
            this.messageArea.clearErrorMessage();
        }
        else
        {
            this.messageArea
                    .updateText(newErrorMessage, IMessageProvider.ERROR);
        }
    }

    /**
     * Updates the message.
     * 
     * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
     */
    public void updateMessage()
    {
        String message = null;
        String errorMessage = null;
        boolean showingError = false;
        IPreferencePage currentPage = this.getCurrentPage();

        if (currentPage != null)
        {
            message = currentPage.getMessage();
            errorMessage = currentPage.getErrorMessage();
        }
        int messageType = IMessageProvider.NONE;
        if (message != null && currentPage instanceof IMessageProvider)
        {
            messageType = ((IMessageProvider) currentPage).getMessageType();
        }

        if (errorMessage == null)
        {
            if (showingError)
            {
                // we were previously showing an error
                showingError = false;
            }
        }
        else
        {
            message = errorMessage;
            messageType = IMessageProvider.ERROR;
            if (!showingError)
            {
                // we were not previously showing an error
                showingError = true;
            }
        }
        this.messageArea.updateText(message, messageType);
    }

    /**
     * Updates the title.
     * 
     * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
     */
    public void updateTitle()
    {
        IPreferencePage currentPage = this.getCurrentPage();
        if (currentPage == null)
        {
            return;
        }
        this.messageArea.showTitle(currentPage.getTitle(), currentPage
                .getImage());
    }

    /**
     * Updates the buttons.
     * 
     * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
     */
    public void updateButtons()
    {
        Button okButton = this.getButton(IDialogConstants.OK_ID);
        okButton.setEnabled(this.isCurrentPageValid());
    }

    /**
     * Add the listeners to the tree viewer.
     * 
     * @param viewer
     * @since 3.1
     */
    protected void addListeners(final TreeViewer viewer)
    {
        viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
            private void handleError()
            {
                try
                {
                    // remove the listener temporarily so that the events caused
                    // by the error handling dont further cause error handling
                    // to occur.
                    viewer.removePostSelectionChangedListener(this);
                    showPageFlippingAbortError();
                    selectLastVisitedPageAgain();
                    clearLastSelectedNode();
                }
                finally
                {

                    viewer.addPostSelectionChangedListener(this);
                }
            }

            public void selectionChanged(SelectionChangedEvent event)
            {
                Object selection = getSingleSelection(event.getSelection());
                if (selection instanceof IPreferenceNode)
                {
                    if (!isCurrentPageValid())
                    {
                        handleError();
                    }
                    else if (!showPage((IPreferenceNode) selection))
                    {
                        // Page flipping wasn't successful
                        handleError();
                    }
                    else
                    {
                        // Everything went well
                        lastSuccessfulNode = (IPreferenceNode) selection;
                    }
                }
            }
        });
        ((Tree) viewer.getControl())
                .addSelectionListener(new SelectionAdapter() {
                    public void widgetDefaultSelected(final SelectionEvent event)
                    {
                        ISelection selection = viewer.getSelection();
                        if (selection.isEmpty())
                            return;
                        IPreferenceNode singleSelection = getSingleSelection(selection);
                        boolean expanded = viewer
                                .getExpandedState(singleSelection);
                        viewer.setExpandedState(singleSelection, !expanded);
                    }
                });
        // Register help listener on the tree to use context sensitive help
        viewer.getControl().addHelpListener(new HelpListener() {
            public void helpRequested(HelpEvent event)
            {
                // call perform help on the current page
                if (getCurrentPage() != null)
                {
                    getCurrentPage().performHelp();
                }
            }
        });
    }

    /**
     * The preference dialog implementation of this <code>Dialog</code>
     * framework method sends <code>performOk</code> to all pages of the
     * preference dialog, then calls <code>handleSave</code> on this dialog to
     * save any state, and then calls <code>close</code> to close this dialog.
     */
    protected void okPressed()
    {
        SafeRunnable.run(new SafeRunnable() {
            private boolean errorOccurred;

            /**
             * @see org.eclipse.core.runtime.ISafeRunnable#run()
             */
            public void run()
            {
                // getButton(IDialogConstants.OK_ID).setEnabled(false);
                errorOccurred = false;
                boolean hasFailedOK = false;
                try
                {
                    PreferenceManager preferenceManager = getPreferenceManager();
                    // Notify all the pages and give them a chance to abort
                    Iterator<?> nodes = preferenceManager.getElements(
                            PreferenceManager.PRE_ORDER).iterator();
                    while (nodes.hasNext())
                    {
                        IPreferenceNode node = (IPreferenceNode) nodes.next();
                        IPreferencePage page = node.getPage();
                        if (page != null)
                        {
                            if (!page.performOk())
                            {
                                hasFailedOK = true;
                                return;
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    handleException(e);
                }
                finally
                {
                    // Don't bother closing if the OK failed
                    if (hasFailedOK)
                    {
                        return;
                    }
                    if (!errorOccurred)
                    {
                        // Give subclasses the choice to save the state of the
                        // preference pages.
                        handleSave();
                    }

                    close();
                }
            }

            /**
             * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
             */
            public void handleException(Throwable e)
            {
                errorOccurred = true;
                Policy.getLog().log(
                        new Status(IStatus.ERROR, Policy.JFACE, 0,
                                e.toString(), e));

                clearLastSelectedNode();
                String message = JFaceResources
                        .getString("SafeRunnable.errorMessage"); //$NON-NLS-1$
                MessageDialog.openError(getShell(), JFaceResources
                        .getString("Error"), message); //$NON-NLS-1$
            }
        });
    }

    /**
     * Creates the wizard's title area.
     * 
     * @param parent the SWT parent for the title area composite
     * @return the created title area composite
     */
    protected Composite createTitleArea(Composite parent)
    {
        /*
         * Create the title area which will contai a title, message, and image.
         */
        int margins = 2;
        Composite titleArea = new Composite(parent, SWT.NONE);
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = margins;
        titleArea.setLayout(layout);

        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.verticalAlignment = SWT.TOP;
        titleArea.setLayoutData(layoutData);

        /* Message label */
        this.messageArea = new DialogMessageArea();
        this.messageArea.createContents(titleArea);

        titleArea.addControlListener(new ControlAdapter() {
            /**
             * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
             */
            public void controlResized(ControlEvent e)
            {
                updateMessage();
            }
        });

        final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event)
            {
                if (JFaceResources.BANNER_FONT.equals(event.getProperty()))
                    updateMessage();
                if (JFaceResources.DIALOG_FONT.equals(event.getProperty()))
                {
                    updateMessage();
                    Font dialogFont = JFaceResources.getDialogFont();
                    updateTreeFont(dialogFont);
                    Control[] children = ((Composite) buttonBar).getChildren();
                    for (int i = 0; i < children.length; i++)
                        children[i].setFont(dialogFont);
                }
            }
        };

        titleArea.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent event)
            {
                JFaceResources.getFontRegistry().removeListener(fontListener);
            }
        });
        JFaceResources.getFontRegistry().addListener(fontListener);
        this.messageArea.setTitleLayoutData(this.createMessageAreaData());
        this.messageArea.setMessageLayoutData(this.createMessageAreaData());

        return (titleArea);
    }

    /**
     * Creates the buttons for the embedded hutton bar.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent)
    {
        Button okButton = null;
        Button cancelButton = null;

        if (Display.getDefault().getDismissalAlignment() == SWT.LEFT)
        {
            /* Default dismissal button (OK) has to be on the left side */

            // Create OK Button
            okButton = this.createButton(parent, IDialogConstants.OK_ID,
                    Messages.getString("okButton"), true);

            // Create Cancel Button
            cancelButton = this.createButton(parent,
                    IDialogConstants.CANCEL_ID, Messages
                            .getString("cancelButton"), false);
        }
        else
        {
            /* Default dismissal button (OK) has to be on the right side */

            // Create Cancel Button
            cancelButton = this.createButton(parent,
                    IDialogConstants.CANCEL_ID, Messages
                            .getString("cancelButton"), true);

            // Create OK Button
            okButton = this.createButton(parent, IDialogConstants.OK_ID,
                    Messages.getString("okButton"), false);
        }
        // Set Image for OK button and adjust layout
        this.setButtonLayoutData(okButton);

        // Set Image for cancel button and adjust layout
        this.setButtonLayoutData(cancelButton);
    }

    /**
     * Create the <code>Sash</code>with left control on the left. Note that
     * this method assumes <code>GridData</code> for the layout data of the
     * leftControl.
     * 
     * @param composite the <code>Composite</code> to embed the
     *            <code>Sash</code> into
     * @param leftControl the <code>Control</code> at the left side of the
     *            <code>Sash</code>
     * @return Sash the created <code>Sash</code>
     */
    protected Sash createSash(final Composite composite,
            final Control leftControl)
    {
        final Sash sash = new Sash(composite, SWT.VERTICAL);
        sash.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        sash.setBackground(composite.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        sash.addListener(SWT.Selection, new Listener() 
        {
            public void handleEvent(Event event)
            {
                if (event.detail == SWT.DRAG)
                    return;

                int shift = event.x - sash.getBounds().x;
                GridData data = (GridData) leftControl.getLayoutData();
                int newWidthHint = data.widthHint + shift;
                int maxSize = getPageContainer().getSize().x / 2;
                if (newWidthHint > maxSize || newWidthHint < 70)
                {
                    event.doit = false;
                    return;
                }
                data.widthHint = newWidthHint;
                composite.layout(true);
            }
        });
        return (sash);
    }

    /**
     * Shows the preference page corresponding to the given preference node.
     * Does nothing if that page is already current. This implementation
     * prevents auto resizing.
     * 
     * @param node the preference node, or <code>null</code> if none
     * @return <code>true</code> if the page flip was successful;
     *         <code>false</code> if unsuccessful
     */
    protected boolean showPage(IPreferenceNode node)
    {
        IPreferencePage currentPage = this.getCurrentPage();
        final Composite pageContainer = this.getPageContainer();

        if (node == null)
            return false;

        // Create the page if nessessary
        if (node.getPage() == null)
            this.createPage(node);
        
        if (node.getPage() == null)
            return false;
        
        IPreferencePage newPage = this.getPage(node);
        if (newPage == currentPage)
            return true;
        
        if (currentPage != null && !currentPage.okToLeave())
            return (false);

        IPreferencePage oldPage = currentPage;
        this.setCurrentPage(newPage);
        currentPage = this.getCurrentPage();
        // Set the new page's container
        currentPage.setContainer(this);

        // Ensure that the page control has been created
        // (this allows lazy page control creation)
        final IPreferencePage curPage = currentPage;
        if (currentPage.getControl() == null)
        {
            final boolean[] failed = { false };
            SafeRunnable.run(new ISafeRunnable() {
                public void handleException(Throwable e)
                {
                    failed[0] = true;
                }

                public void run()
                {
                    createPageControl(curPage, pageContainer);
                }
            });
            if (failed[0])
                return false;
            // the page is responsible for ensuring the created control is
            // accessable via getControl.
            Assert.isNotNull(currentPage.getControl());
        }

        // Force calculation of the page's description label because
        // label can be wrapped.
        final Point[] size = new Point[1];
        final Point failed = new Point(-1, -1);
        SafeRunnable.run(new ISafeRunnable() 
        {
            public void handleException(Throwable e)
            {
                size[0] = failed;
            }

            public void run()
            {
                size[0] = curPage.computeSize();
            }
        });
        if (size[0].equals(failed))
            return false;
        
        // Do we need resizing. Computation not needed if the
        // first page is inserted since computing the dialog's
        // size is done by calling dialog.open().
        // Also prevent auto resize if the user has manually resized
        if (oldPage != null)
        {
            Rectangle rect = pageContainer.getClientArea();
            Point containerSize = new Point(rect.width, rect.height);
            // Set the size to be sure we use the result of computeSize
            currentPage.setSize(containerSize);
        }
        // Ensure that all other pages are invisible
        // (including ones that triggered an exception during
        // their creation).
        Control[] children = pageContainer.getChildren();
        Control currentControl = currentPage.getControl();
        for (int i = 0; i < children.length; i++)
        {
            if (children[i] != currentControl)
                children[i].setVisible(false);
        }
        // Make the new page visible
        currentPage.setVisible(true);
        if (oldPage != null)
            oldPage.setVisible(false);

        // update the dialog controls
        this.update();
        return true;
    }

    /**
     * Selects the page determined by <code>lastSuccessfulNode</code> in the
     * page hierarchy.
     */
    private void selectLastVisitedPageAgain()
    {
        if (lastSuccessfulNode == null)
        {
            return;
        }
        this.getTreeViewer().setSelection(
                new StructuredSelection(lastSuccessfulNode));
        this.getCurrentPage().setVisible(true);
    }

    /**
     * Clear the last selected node. This is so that we not chache the last
     * selection in case of an error.
     */
    private void clearLastSelectedNode()
    {
        this.setSelectedNodePreference(null);
    }

    /**
     * Shows an error indicating that the status of the currently selected page
     * is inavlid and has to be corrected.
     */
    private void showPageFlippingAbortError()
    {
        this.setErrorMessage(Messages.getString("pageAbortErrorMessage"));
    }

    /**
     * Create the layout data for the message area.
     * 
     * @return FormData for the message area.
     */
    private FormData createMessageAreaData()
    {
        FormData messageData = new FormData();
        messageData.top = new FormAttachment(0);
        messageData.bottom = new FormAttachment(100);
        messageData.right = new FormAttachment(100);
        messageData.left = new FormAttachment(0);

        return (messageData);
    }

    /**
     * Creates and shows the configuration dialog.
     * 
     * @param shell the shell to attach to
     */
    public static void run(Shell shell)
    {
        /* Create the preference manager */
        PreferenceManager mgr = new PreferenceManager();
        createPreferenceTree(mgr);

        /* Create and open the ConfigurationDialog */
        ConfigurationDialog dlg = new ConfigurationDialog(shell, mgr);

        dlg.setPreferenceStore(ConfigurationManager.CONFIG_STORE);
        dlg.create();
        dlg.getShell().setText(Messages.getString("configurationDialogTitle"));
        dlg.getShell().setImage(SWTHelper.loadImage("config.gif"));
        Point minSize = new Point(690, 480);
        dlg.getShell().setSize(minSize);
        dlg.setMinimumPageSize(minSize);
        dlg.getShell().setMinimumSize(minSize);
        DialogUtils.centerShellOnParentShell(dlg.getShell());
        dlg.getTreeViewer().expandAll();
        dlg.open();
    }

    /**
     * Creates the preference tree structure.
     * 
     * @param resource the <code>LanguageResource/code> to use for i18n
     * @param mgr the <code>PreferenceManager</code> that the <code>PreferenceNode</code>s
     * should be embedded in
     */
    private static void createPreferenceTree(PreferenceManager mgr)
    {
        /* Create the nodes representing the single preference pages */
        ExtendedPreferenceNode generalNode = new ExtendedPreferenceNode(
                ConfigurationManager.GENERAL_OPTIONS_KEY, Messages
                        .getString("generalConfigurationPageTitle"), SWTHelper
                        .getImageDescriptor("wizard/generalConfig16.png"),
                GeneralConfigurationPage.class.getName());

        ExtendedPreferenceNode languageNode = new ExtendedPreferenceNode(
                ConfigurationManager.LANGUAGE_OPTIONS_KEY, Messages
                        .getString("languageConfigurationPageTitle"), SWTHelper
                        .getImageDescriptor("wizard/languageConfig16.png"),
                LanguageConfigurationPage.class.getName());

        ExtendedPreferenceNode trayNode = new ExtendedPreferenceNode(
                ConfigurationManager.TRAY_OPTIONS_KEY, Messages
                        .getString("trayConfigurationPageTitle"), SWTHelper
                        .getImageDescriptor("wizard/trayConfig16.png"),
                TrayConfigurationPage.class.getName());

        ExtendedPreferenceNode smtpNode = new ExtendedPreferenceNode(
                ConfigurationManager.SMTP_OPTIONS_KEY, Messages
                        .getString("smtpConfigurationPageTitle"), SWTHelper
                        .getImageDescriptor("wizard/proxyConfig16.png"),
                SMTPConfigurationPage.class.getName());

        ExtendedPreferenceNode pop3Node = new ExtendedPreferenceNode(
                ConfigurationManager.POP3_OPTIONS_KEY, Messages
                        .getString("pop3ConfigurationPageTitle"), SWTHelper
                        .getImageDescriptor("wizard/proxyConfig16.png"),
                POP3ConfigurationPage.class.getName());

        ExtendedPreferenceNode connectionNode = new ExtendedPreferenceNode(
                ConfigurationManager.PROTOCOLS_OPTIONS_KEY, Messages
                        .getString("protocolsConfigurationPageTitle"),
                SWTHelper.getImageDescriptor("wizard/connectionConfig16.png"),
                ProtocolsConfigurationPage.class.getName());

        ExtendedPreferenceNode enclosuresNode = new ExtendedPreferenceNode(
                ConfigurationManager.ENCLOSURES_OPTIONS_KEY, Messages
                        .getString("enclosuresConfigurationPageTitle"),
                SWTHelper.getImageDescriptor("wizard/enclosureConfig16.png"),
                EnclosuresConfigurationPage.class.getName());

        /* Add the nodes to the PreferenceManager */
        mgr.addToRoot(generalNode);
        generalNode.add(trayNode);
        generalNode.add(languageNode);

        mgr.addToRoot(enclosuresNode);

        mgr.addToRoot(connectionNode);
        connectionNode.add(smtpNode);
        connectionNode.add(pop3Node);
    }
}
