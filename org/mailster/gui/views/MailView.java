package org.mailster.gui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.server.MailsterSmtpService;
import org.mailster.smtp.SmtpMessage;
import org.mailster.smtp.SmtpMessagePart;
import org.mailster.util.StreamWriterUtilities;

import ca.odell.glazedlists.AbstractEventList;

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
 * MailView.java - Handles mail tabs.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class MailView
{
    /**
     * This class provides the "drop down" functionality for our attached file
     * dropdown.
     */
    class DropdownSelectionListener extends SelectionAdapter
    {
        private ToolItem dropdown;

        private Menu menu;

        /**
         * Constructs a DropdownSelectionListener
         * 
         * @param dropdown the dropdown this listener belongs to
         */
        public DropdownSelectionListener(ToolItem dropdown)
        {
            this.dropdown = dropdown;
            clearMenu();
        }

        /**
         * Adds an item to the dropdown list
         * 
         * @param item the item to add
         */
        public void add(SmtpMessagePart part)
        {
            dropdown.setEnabled(true);
            MenuItem menuItem = new MenuItem(menu, SWT.NONE);
            String fileName = part.getFileName();
            menuItem.setText(fileName);

            if (fileName.lastIndexOf('.') != -1)
            {
                Program p = Program.findProgram(fileName.substring(fileName
                        .lastIndexOf('.')));
                if (p != null)
                {
                    ImageData data = p.getImageData();
                    if (data != null)
                        menuItem.setImage(new Image(menu.getDisplay(), data));
                }
            }
            else if (part.getContentType().startsWith("message")) //$NON-NLS-1$
                menuItem.setImage(SWTHelper.loadImage("mail.gif")); //$NON-NLS-1$

            menuItem.setData(part);
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event)
                {
                    String fileName = ((SmtpMessagePart) event.widget.getData())
                            .getFileName();
                    Program p = Program.findProgram(fileName.substring(fileName
                            .lastIndexOf('.')));
                    if (p != null)
                    {
                        fileName = main.getSMTPService().getDefaultOutputDirectory()
                                + File.separator + fileName;
                        saveAllAttachments(
                                new MenuItem[] { (MenuItem) event.widget },
                                main.getSMTPService().getDefaultOutputDirectory());
                        main
                                .log(Messages
                                        .getString("MailView.execute.file") + fileName + " ..."); //$NON-NLS-1$
                        p.execute(fileName);
                    }
                    else
                        saveAttachedFile((SmtpMessagePart) event.widget
                                .getData());
                }
            });
        }

        public final void clearMenu()
        {
            menu = new Menu(dropdown.getParent().getShell());
            dropdown.setEnabled(false);
        }

        /**
         * Called when either the button itself or the dropdown arrow is clicked
         * 
         * @param event the event that trigged this call
         */
        public void widgetSelected(SelectionEvent event)
        {
            // If arrow is clicked then show the list
            if (event.detail == SWT.ARROW)
            {
                // Determine where to put the dropdown list
                ToolItem item = (ToolItem) event.widget;
                Rectangle rect = item.getBounds();
                Point pt = item.getParent()
                        .toDisplay(new Point(rect.x, rect.y));
                menu.setLocation(pt.x, pt.y + rect.height);
                menu.setVisible(true);
            }
            else
            {
                // Button has been pushed so take appropriate action
                saveAllAttachments(menu.getItems(), main
                        .getSMTPService().getDefaultOutputDirectory());
            }
        }
    }

    class MCTabFolder2Listener extends CTabFolder2Adapter
    {
        private int percents = 0;
        private CTabFolder folder;
        private SashForm sash;

        public MCTabFolder2Listener(SashForm sash, CTabFolder folder)
        {
            this.sash = sash;
            this.folder = folder;
        }

        private void computePercents()
        {
            int h = sash.getSize().y - sash.SASH_WIDTH;
            percents = Math.round((float) (folder.getSize().y * 100)
                    / (float) h);
        }

        public void minimize(CTabFolderEvent event)
        {
            if (!folder.getMinimized())
            {
                if (!folder.getMaximized())
                    computePercents();
                folder.setMaximized(false);
                folder.setMinimized(true);
            }

            int p = Math.round((float) (folder.getTabHeight() * 100)
                    / (float) (sash.getSize().y - sash.SASH_WIDTH)) + 1;
            sash.setWeights(new int[] { 100 - p, p });
        }

        public void maximize(CTabFolderEvent event)
        {
            if (!folder.getMaximized())
            {
                if (!folder.getMinimized())
                    computePercents();
                folder.setMinimized(false);
                folder.setMaximized(true);
            }
            sash.setWeights(new int[] { 0, 100 });
        }

        public void restore(CTabFolderEvent event)
        {
            folder.setMinimized(false);
            folder.setMaximized(false);
            sash.setWeights(new int[] { 100 - percents, percents });
        }

        public void close(CTabFolderEvent event)
        {
            dropdownListener.clearMenu();

            if (event.item.getData() != null)
                openedMailsIds.remove(((StoredSmtpMessage) event.item.getData())
                        .getMessage().getMessageID());
            else
                restore(event);
        }
    }

    public final static String DEFAULT_PREFERRED_CONTENT = "text/html"; //$NON-NLS-1$
    
    private String preferredContentType = DEFAULT_PREFERRED_CONTENT;

    public final Image attachedFilesImage;
    public final Image mailImage;
    public final Image homeImage;

    public final Image rawViewImage;
    public final Image browserImage;

    private ArrayList<String> openedMailsIds = new ArrayList<String>();

    private TableView tableView;
    private CTabFolder folder;    
    private Composite parent;
    private MailsterSWT main;

    private MCTabFolder2Listener folderLayoutListener;
    private DropdownSelectionListener dropdownListener;

    private ToolItem toggleView;

    private Color[] tabGradient;
    private boolean forcedMozillaBrowserUse = false;
    
    public MailView(Composite parent, FilterTreeView treeView, MailsterSWT main)
    {
        this.parent = parent;
        this.main = main;

        attachedFilesImage = SWTHelper.loadImage("attach.gif"); //$NON-NLS-1$
        mailImage = SWTHelper.loadImage("mail_into.gif"); //$NON-NLS-1$
        homeImage = SWTHelper.loadImage("home.gif"); //$NON-NLS-1$
        rawViewImage = SWTHelper.loadImage("rawView.gif"); //$NON-NLS-1$
        browserImage = SWTHelper.loadImage("mail.gif"); //$NON-NLS-1$

        tabGradient = SWTHelper.getGradientColors(5,
                new Color(SWTHelper.getDisplay(), 0, 84, 227),
                new Color(SWTHelper.getDisplay(), 61, 149, 255));

        createView(treeView, main.getFilterTextField());        
    }

    private void createView(FilterTreeView treeView, Text filterTextField)
    {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        SashForm sashForm = new SashForm(parent, SWT.NONE);
        sashForm.setOrientation(SWT.VERTICAL);
        sashForm.setLayoutData(gridData);
        tableView = new TableView(sashForm, this, treeView, filterTextField);        
        createTabFolder(sashForm);
        sashForm.setWeights(new int[] { 30, 70 });
    }

    private Text createRawMailView(Composite parent)
    {
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.verticalAlignment = GridData.BEGINNING;
        gridData.grabExcessHorizontalSpace = true;
        Text rawMailView = new Text(parent, SWT.MULTI | SWT.V_SCROLL
                | SWT.BORDER | SWT.WRAP);
        rawMailView.setLayoutData(gridData);
        rawMailView.setEditable(false);

        return rawMailView;
    }

    private Browser createBrowser(Composite parent)
    {
        if (isForcedMozillaBrowserUse())
            return new Browser(parent, SWT.BORDER | SWT.MOZILLA);
        else
            return new Browser(parent, SWT.BORDER);
    }

    protected void createMailTab(StoredSmtpMessage stored)
    {
        if (stored == null)
            return;
        
        SmtpMessage msg = stored.getMessage();
        String id = msg.getMessageID();
        if (!openedMailsIds.contains(id))
        {
            final CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(msg.getSubject());
            item.setImage(mailImage);

            final Composite itemComposite = new Composite(folder, SWT.NONE);
            final StackLayout layout = new StackLayout();
            itemComposite.setLayout(layout);
            item.setControl(itemComposite);

            Text rawView = createRawMailView(itemComposite);
            Browser _browser = null;

            try
            {
                Composite browserComposite = new Composite(itemComposite, 0);
                GridLayout g = new GridLayout();
                g.marginHeight = 0;
                g.marginWidth = 0;
                g.horizontalSpacing = 0;
                g.verticalSpacing = 0;
                browserComposite.setLayout(g);

                HeadersView h = new HeadersView(browserComposite, msg);
                GridData data = new GridData();
                data.grabExcessHorizontalSpace = true;
                data.horizontalAlignment = GridData.FILL;
                data.verticalAlignment = GridData.BEGINNING;
                h.getComposite().setLayoutData(data);

                _browser = createBrowser(browserComposite);
                data = new GridData();
                data.verticalAlignment = GridData.FILL;
                data.horizontalAlignment = GridData.FILL;
                data.grabExcessVerticalSpace = true;
                data.grabExcessHorizontalSpace = true;
                _browser.setLayoutData(data);

                layout.topControl = browserComposite;
                browserComposite.layout(true);
            }
            catch (SWTError swt)
            {
                main.log(swt.getMessage());
                item.dispose();
                return;
            }

            _browser.setText(msg.getContent(preferredContentType));
        	_browser.addProgressListener(new ProgressAdapter() {
                public void completed(ProgressEvent event)
                {
                	// Dynamically add the javascript highlighting code
                    executeJavaScript("var script = document.createElement('script');\r\n" +
		                                "script.type = 'text/javascript';\r\n" +
		                                "script.src = 'file:///"+StreamWriterUtilities.USER_DIR+"/js/highlight_mailster.js';\r\n" +
		                                "document.getElementsByTagName('head')[0].appendChild(script);");
                }
            });
            rawView.setText(msg.getRawMessage());

            item.setData(stored);
            openedMailsIds.add(id);
            folder.setSelection(item);
            updateFolderToolbar(msg.getInternalParts());
            setMailViewMode();

            final Browser browser = _browser;
            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    browser.dispose();
                }
            });
        }
    }

    protected void selectMailTab(SmtpMessage msg)
    {
        if (msg == null)
            return;
        String id = msg.getMessageID();
        if (openedMailsIds.contains(id))
        {
            for (int i = 0, max = folder.getItemCount(); i < max; i++)
            {
                if (folder.getItems()[i].getData() != null
                        && id.equals(((StoredSmtpMessage) folder.getItems()[i]
                                .getData()).getMessage().getMessageID()))
                {
                    folder.setSelection(folder.getItems()[i]);
                    updateFolderToolbar(msg.getInternalParts());
                    setMailViewMode();
                    return;
                }
            }
        }
    }

    public void executeJavaScript(String script)
    {
        for (int i = 0, max = folder.getItemCount(); i < max; i++)
        {
        	Composite c = (Composite) folder.getItem(i).getControl();
        	Browser browser = (Browser) ((Composite)c.getChildren()[1]).getChildren()[1];
        
        	boolean result = browser.execute(script);
        	
        	// Script may fail or may not be supported on certain platforms.
        	if (!result)
                main.log("Script failed to execute on tab ["+folder.getItem(i).getText()+"]");
        }
    }

    private void updateFolderToolbar(final SmtpMessagePart current)
    {
        dropdownListener.clearMenu();
        if (current != null)
        {
            SmtpMessagePart[] files = current.getAttachedFiles();

            for (int i = 0, max = files.length; i < max; i++)
                dropdownListener.add(files[i]);
        }
    }

    private void saveAllAttachments(MenuItem[] items, String dir)
    {
        for (int i = 0, max = items.length; i < max; i++)
        {
            SmtpMessagePart p = (SmtpMessagePart) items[i].getData();
            String fileName = dir + File.separator + p.getFileName();

            if (fileName != null)
            {
                try
                {
                    FileOutputStream f = new FileOutputStream(
                            new File(fileName));
                    p.write(f);
                    f.flush();
                    f.close();
                    main.log(Messages.getString("MailView.saving.attached.file.log1") 
                                + p.getFileName() + Messages.getString("MailView.saving.attached.file.log2") //$NON-NLS-1$ //$NON-NLS-2$
                                + dir);
                }
                catch (Exception e)
                {
                    main.log(e.toString());
                }
            }
        }
    }

    private void saveAttachedFile(SmtpMessagePart part)
    {
        FileDialog dialog = new FileDialog(parent.getShell(), SWT.SAVE);
        dialog.setFilterNames(new String[] {
                Messages.getString("MailView.attached.files.ext"), //$NON-NLS-1$
                Messages.getString("MailView.all.files.ext") }); //$NON-NLS-1$
        dialog.setFilterExtensions(new String[] { "*.eml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setFilterPath(main.getSMTPService().getDefaultOutputDirectory());
        dialog.setFileName(part.getFileName());
        dialog.setText(Messages.getString("MailView.dialog.title")); //$NON-NLS-1$
        String fileName = dialog.open();
        if (fileName != null)
        {
            try
            {
                FileOutputStream f = new FileOutputStream(new File(fileName));
                part.write(f);
                f.flush();
                f.close();
                main
                        .log(Messages
                                .getString("MailView.saving.attached.file.log") + fileName); //$NON-NLS-1$
            }
            catch (Exception e)
            {
                main.log(e.toString());
            }
        }
    }

    private void setMailViewMode()
    {
        toggleView.setHotImage(toggleView.getSelection()
                ? rawViewImage
                : browserImage);
        toggleView.setToolTipText(toggleView.getSelection()
                ? Messages.getString("MailView.toggle.toInterpretedViewTooltip")
                : Messages.getString("MailView.toggle.toRawViewTooltip"));

        if (folder.getSelection() == null ||
        		folder.getSelection().getData() == null)
            return;

        Composite c = (Composite) folder.getSelection().getControl();
        StackLayout layout = (StackLayout) c.getLayout();
        if (toggleView.getSelection())
            layout.topControl = c.getChildren()[0];
        else
            layout.topControl = c.getChildren()[1];

        c.layout();
    }

    private void createTabFolder(final SashForm sash)
    {
        folder = new CTabFolder(sash, SWT.BORDER);

        folder.setSimple(false);
        folder.setMinimizeVisible(true);
        folder.setMaximizeVisible(true);
        folder.setUnselectedImageVisible(false);
        folder.setUnselectedCloseVisible(false);

        folder.setTabHeight(22);

        folder.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                update(event);
                if (folder.getMaximized())
                    folderLayoutListener.restore(null);
                else
                    folderLayoutListener.maximize(null);
            }

            public void widgetSelected(SelectionEvent event)
            {
                update(event);
            }

            public void update(SelectionEvent event)
            {
                if (event.item != null && event.item.getData() != null)
                {
                    updateFolderToolbar(((StoredSmtpMessage) event.item.getData())
                            .getMessage().getInternalParts());
                    setMailViewMode();
                }
            }
        });

        ToolBar rightToolbar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
        rightToolbar.setLayoutData(new FillLayout());
        ToolItem home = SWTHelper.createToolItem(
                        rightToolbar,
                        SWT.PUSH,
                        "", //$NON-NLS-1$
                        Messages.getString("MailView.home.page.tooltip"), "home.gif", false); //$NON-NLS-1$ //$NON-NLS-2$

        folderLayoutListener = new MCTabFolder2Listener(sash, folder);
        folder.addCTabFolder2Listener(folderLayoutListener);

        home.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                showURL(MailsterSWT.MAILSTER_HOMEPAGE, false);
            }
        });

        new ToolItem(rightToolbar, SWT.SEPARATOR);
        
        toggleView = SWTHelper.createToolItem(
                        rightToolbar,
                        SWT.CHECK,
                        "", //$NON-NLS-1$
                        Messages.getString("MailView.toggle.toRawViewTooltip"), "mail.gif", true); //$NON-NLS-1$ //$NON-NLS-2$

        toggleView.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setMailViewMode();
            }
        });

        ToolItem item = SWTHelper.createToolItem(
                        rightToolbar,
                        SWT.FLAT | SWT.DROP_DOWN,
                        "", Messages.getString("MailView.attach.tooltip"), "attach.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        ToolItem collapseAll = SWTHelper.createToolItem(
                        rightToolbar,
                        SWT.PUSH,
                        "", Messages.getString("MailView.collapseall.tooltip")+" (Ctrl+Shift+F4)", "collapseall.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        
        collapseAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                closeAllTabs();
            }
        });
        
        new ToolItem(rightToolbar, SWT.SEPARATOR);
        dropdownListener = new DropdownSelectionListener(item);
        item.addSelectionListener(dropdownListener);
        
        folder.setTopRight(rightToolbar);
        folder.setSelectionBackground(tabGradient,
                new int[] { 10, 20, 30, 40 }, true);
        folder.setSelectionForeground(SWTHelper.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
    }

    public void showURL(String url, boolean setURLAsTitle)
    {
        CTabItem item = null;
        try
        {
            item = new CTabItem(folder, SWT.CLOSE);
            if (setURLAsTitle)
                item.setText(url);
            else
                item.setText(Messages.getString("MailView.tabitem.title")); //$NON-NLS-1$
            item.setImage(homeImage);

            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            gridData.verticalAlignment = GridData.FILL;

            final Browser b = createBrowser(folder);
            b.setUrl(url); //$NON-NLS-1$
            b.setLayoutData(gridData);
            item.setControl(b);
            folder.setSelection(item);
            folderLayoutListener.maximize(null);

            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e)
                {
                    b.dispose();
                }
            });
        }
        catch (SWTError swt)
        {
            main.log(swt.getMessage());
            item.dispose();
            return;
        }
    }

    public Table getTable()
    {
        return tableView.getTable();
    }
    
    public void refreshTable()
    {
        tableView.refreshTable();
    }    

    public AbstractEventList<StoredSmtpMessage> getDataList()
    {
        return tableView.getDataList();
    }
    
    public void clearDataList()
    {
        tableView.clearDataList();
    }

    public void closeAllTabs()
    {
        for (int i = folder.getItemCount() - 1; i >= 0; i--)
        {
            CTabItem item = folder.getItems()[i];

            if (item.getData() != null)
                openedMailsIds.remove(((StoredSmtpMessage) item.getData())
                        .getMessage().getMessageID());

            item.dispose();
        }

        if (folder.getMaximized() || folder.getMinimized())
            folderLayoutListener.restore(null);
        dropdownListener.clearMenu();
    }

    public String getPreferredContentType()
    {
        return preferredContentType;
    }

    public void setPreferredContentType(String preferredContentType)
    {
        this.preferredContentType = preferredContentType;
    }

    public boolean isForcedMozillaBrowserUse()
    {
        return forcedMozillaBrowserUse;
    }

    public void setForcedMozillaBrowserUse(boolean forcedMozillaBrowserUse)
    {
        this.forcedMozillaBrowserUse = forcedMozillaBrowserUse;
    }
    
    protected void log(String message)
    {
        main.log(message);
    }
    
    protected MailsterSmtpService getSMTPService()
    {
        return main.getSMTPService();
    }
}