package org.mailster.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.glazedlists.SmtpMessageTableFormat;
import org.mailster.gui.glazedlists.swt.EventTableViewer;
import org.mailster.gui.glazedlists.swt.TableComparatorChooser;
import org.mailster.smtp.SmtpHeadersInterface;
import org.mailster.smtp.SmtpMessage;
import org.mailster.smtp.SmtpMessagePart;
import org.mailster.util.MailUtilities;

import ca.odell.glazedlists.AbstractEventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

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
 * MailView.java - Enter your Comment HERE.
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
                menuItem.setImage(main.getSWTHelper().loadImage("mail.gif")); //$NON-NLS-1$

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
                        fileName = main.getDefaultOutputDirectory()
                                + File.separator + fileName;
                        saveAllAttachments(
                                new MenuItem[] { (MenuItem) event.widget },
                                main.getDefaultOutputDirectory());
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
                        .getDefaultOutputDirectory());
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
                openedMailsIds.remove(((SmtpMessage) event.item.getData())
                        .getMessageID());
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

    private SortedList<SmtpMessage> dataList;
    private ArrayList<String> openedMailsIds = new ArrayList<String>();

    private TableColumn to;
    private TableColumn subject;
    private TableColumn date;
    private CTabFolder folder;
    private Table table;
    private Composite parent;
    private MailsterSWT main;

    private MCTabFolder2Listener folderLayoutListener;
    private DropdownSelectionListener dropdownListener;

    private ToolItem toggleView;

    private Color[] tabGradient;
    private boolean forcedMozillaBrowserUse = false;

    public MailView(Composite parent, MailsterSWT main)
    {
        this.parent = parent;
        this.main = main;

        attachedFilesImage = main.getSWTHelper().loadImage("attach.gif"); //$NON-NLS-1$
        mailImage = main.getSWTHelper().loadImage("mail_into.gif"); //$NON-NLS-1$
        homeImage = main.getSWTHelper().loadImage("home.gif"); //$NON-NLS-1$
        rawViewImage = main.getSWTHelper().loadImage("rawView.gif"); //$NON-NLS-1$
        browserImage = main.getSWTHelper().loadImage("mail.gif"); //$NON-NLS-1$

        tabGradient = main.getSWTHelper().getGradientColors(5,
                new Color(Display.getCurrent(), 0, 84, 227),
                new Color(Display.getCurrent(), 61, 149, 255));

        createView(main.getFilterTextField());
    }

    private void createView(Text filterTextField)
    {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        SashForm sashForm = new SashForm(parent, SWT.NONE);
        sashForm.setOrientation(SWT.VERTICAL);
        sashForm.setLayoutData(gridData);
        createTable(sashForm, filterTextField);
        createTabFolder(sashForm);
        sashForm.setWeights(new int[] { 30, 70 });
    }

    private Comparable getFieldValue(SmtpMessage msg, TableColumn selected)
    {
        if (selected == to)
            return msg.getHeaderValue(SmtpHeadersInterface.TO).toLowerCase();
        else if (selected == subject)
            return msg.getSubject().toLowerCase();
        else if (selected == date)
        {
            try
            {
                return MailUtilities.rfc822DateFormatter.parse(msg.getDate());
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                return null;
            }
        }
        else
            return new Boolean(
                    msg.getInternalParts().getAttachedFiles().length > 0);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private int compareTo(SmtpMessage row0, SmtpMessage row1,
            TableColumn selected)
    {
        if (row0 == null && row1 == null)
            return 0;
        else if (row0 == null)
            return -1;
        else if (row1 == null)
            return 1;
        else
            return getFieldValue(row0, selected).compareTo(
                    getFieldValue(row1, selected));
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

    private void createMailTab(SmtpMessage msg)
    {
        if (msg == null)
            return;
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

                HeadersView h = new HeadersView(browserComposite, main
                        .getSWTHelper(), msg);
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
            rawView.setText(msg.getRawMessage());

            item.setData(msg);
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

    private void selectMailTab(SmtpMessage msg)
    {
        if (msg == null)
            return;
        String id = msg.getMessageID();
        if (openedMailsIds.contains(id))
        {
            for (int i = 0, max = folder.getItemCount(); i < max; i++)
            {
                if (folder.getItems()[i].getData() != null
                        && id.equals(((SmtpMessage) folder.getItems()[i]
                                .getData()).getMessageID()))
                {
                    folder.setSelection(folder.getItems()[i]);
                    updateFolderToolbar(msg.getInternalParts());
                    setMailViewMode();
                    return;
                }
            }
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

    @SuppressWarnings("deprecation")
    private void createTable(Composite parent, Text filterTextField)
    {
        final Composite tableComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        layout.horizontalSpacing = 2;
        layout.verticalSpacing = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        tableComposite.setLayout(layout);

        final BasicEventList<SmtpMessage> eventList = new BasicEventList<SmtpMessage>();
        String[] filterProperties = new String[] { "to", "subject" };
        TextFilterator<SmtpMessage> filterator = GlazedLists
                .textFilterator(filterProperties);
        TextWidgetMatcherEditor matcher = new TextWidgetMatcherEditor(
                filterTextField, filterator);

        @SuppressWarnings("unchecked")
        final FilterList<SmtpMessage> filterList = new FilterList<SmtpMessage>(
                eventList, matcher);

        dataList = new SortedList<SmtpMessage>(filterList,
                new Comparator<SmtpMessage>() {
                    public int compare(SmtpMessage row0, SmtpMessage row1)
                    {
                        try
                        {
                            return compareTo(row0, row1, table.getSortColumn());
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                            return 0;
                        }
                    }
                });

        table = new Table(tableComposite, SWT.VIRTUAL | SWT.BORDER
                | SWT.FULL_SELECTION);

        SmtpMessageTableFormat tf = new SmtpMessageTableFormat(main
                .getSWTHelper());
        EventTableViewer<SmtpMessage> msgTableViewer = new EventTableViewer<SmtpMessage>(
                dataList, table, tf);
        TableComparatorChooser.install(msgTableViewer, dataList, false);

        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setItemCount(0);

        TableColumn attachments = table.getColumn(0);
        attachments.setResizable(false);
        attachments.setMoveable(true);
        attachments.setWidth(20);

        to = table.getColumn(1);
        to.setResizable(true);
        to.setMoveable(true);
        to.setWidth(100);

        subject = table.getColumn(2);
        subject.setResizable(true);
        subject.setMoveable(true);
        subject.setWidth(100);

        date = table.getColumn(3);
        date.setResizable(true);
        date.setMoveable(true);
        date.setWidth(100);
        date.setAlignment(SWT.RIGHT);

        Listener tableListener = new Listener() {
            public void handleEvent(Event e)
            {
                try
                {
                    if (e.type == SWT.DefaultSelection)
                        createMailTab((SmtpMessage) table.getSelection()[0]
                                .getData());
                    else if (e.type == SWT.Selection)
                        selectMailTab((SmtpMessage) table.getSelection()[0]
                                .getData());
                }
                catch (SWTError ex)
                {
                    main.log(ex.getMessage());
                }
            }
        };

        table.addListener(SWT.Selection, tableListener);
        table.addListener(SWT.DefaultSelection, tableListener);

        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                updateTableColumnsWidth();
            }
        });

        // Add sort indicator
        Listener sortListener = new Listener() {
            public void handleEvent(Event e)
            {
                // determine new sort column and direction
                TableColumn sortColumn = table.getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = table.getSortDirection();
                if (sortColumn == currentColumn)
                {
                    dir = dir == SWT.UP ? SWT.DOWN : (dir == SWT.DOWN
                            ? SWT.NONE
                            : SWT.UP);
                }
                else
                {
                    table.setSortColumn(currentColumn);
                    dir = SWT.UP;
                }
                table.setSortDirection(dir);
            }
        };
        to.addListener(SWT.Selection, sortListener);
        subject.addListener(SWT.Selection, sortListener);
        date.addListener(SWT.Selection, sortListener);
        table.setSortColumn(to);
        table.setSortDirection(SWT.NONE);

        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        table.setLayoutData(gd);

        final Label countLabel = new Label(tableComposite, SWT.NONE);
        countLabel.setText("<0/0>"); //$NON-NLS-1$
        filterList.addListEventListener(new ListEventListener<SmtpMessage>() {
            public void listChanged(ListEvent<SmtpMessage> arg0)
            {
                countLabel.setText("<" + filterList.size() + "/"
                        + eventList.size() + ">");
                if (filterList.size() < eventList.size())
                    countLabel.setForeground(main.getSWTHelper().createColor(0,
                            127, 0));
                else
                    countLabel.setForeground(Display.getDefault()
                            .getSystemColor(SWT.COLOR_BLACK));
                tableComposite.layout(true);
            }
        });

        gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.END;
        countLabel.setLayoutData(gd);
    }

    private void updateTableColumnsWidth()
    {
        Rectangle area = table.getClientArea();
        Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int scroll = size.y > area.height + table.getHeaderHeight()
                && table.getVerticalBar() != null ? table.getVerticalBar()
                .getSize().x : 0;

        int w = (table.getSize().x
                - (table.getBorderWidth() * (table.getColumnCount() - 1))
                - table.getColumn(0).getWidth() - scroll)
                / (table.getColumnCount() - 1);

        for (int i = 1, max = table.getColumnCount(); i < max; i++)
            table.getColumn(i).setWidth(
                    i < max - 1 ? (int) (w * 1.2) : (int) (w * 0.6));
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
                    main
                            .log(Messages
                                    .getString("MailView.saving.attached.file.log1") + p.getFileName() + Messages.getString("MailView.saving.attached.file.log2") //$NON-NLS-1$ //$NON-NLS-2$
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
        dialog.setFilterPath(main.getDefaultOutputDirectory());
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

        if (folder.getSelection() == null)
            return;

        SmtpMessage msg = (SmtpMessage) folder.getSelection().getData();

        if (msg == null)
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
                    updateFolderToolbar(((SmtpMessage) event.item.getData())
                            .getInternalParts());
                    setMailViewMode();
                }
            }
        });

        ToolBar rightToolbar = new ToolBar(folder, SWT.FILL | SWT.FLAT);
        rightToolbar.setLayoutData(new FillLayout());
        ToolItem home = main
                .getSWTHelper()
                .createToolItem(
                        rightToolbar,
                        SWT.PUSH,
                        "", //$NON-NLS-1$
                        Messages.getString("MailView.home.page.tooltip"), "home.gif", false); //$NON-NLS-1$ //$NON-NLS-2$

        folderLayoutListener = new MCTabFolder2Listener(sash, folder);
        folder.addCTabFolder2Listener(folderLayoutListener);

        home.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                showURL("http://sourceforge.net/projects/mailster/", false);
            }
        });

        toggleView = main
                .getSWTHelper()
                .createToolItem(
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

        ToolItem item = main
                .getSWTHelper()
                .createToolItem(
                        rightToolbar,
                        SWT.FLAT | SWT.DROP_DOWN,
                        "", Messages.getString("MailView.attach.tooltip"), "attach.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        new ToolItem(rightToolbar, SWT.SEPARATOR);
        dropdownListener = new DropdownSelectionListener(item);
        item.addSelectionListener(dropdownListener);
        
        folder.setTopRight(rightToolbar);
        folder.setSelectionBackground(tabGradient,
                new int[] { 10, 20, 30, 40 }, true);
        folder.setSelectionForeground(Display.getCurrent().getSystemColor(
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
        return table;
    }

    public AbstractEventList<SmtpMessage> getDataList()
    {
        return dataList;
    }

    public void closeAllTabs()
    {
        for (int i = folder.getItemCount() - 1; i >= 0; i--)
        {
            CTabItem item = folder.getItems()[i];

            if (item.getData() != null)
                openedMailsIds.remove(((SmtpMessage) item.getData())
                        .getMessageID());

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
}
