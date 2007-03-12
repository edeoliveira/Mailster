package org.mailster.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.mailster.MailsterSWT;
import org.mailster.util.MailUtilities;

import com.dumbster.smtp.SmtpHeadersInterface;
import com.dumbster.smtp.SmtpMessage;
import com.dumbster.smtp.SmtpMessagePart;

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
    private Color[] tabGradient;

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
                    saveAttachedFile((SmtpMessagePart) event.widget.getData());
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
            // If they clicked the arrow, we show the list
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
                // They pushed the button so take appropriate action
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
                        .getHeaderValue(SmtpHeadersInterface.MESSAGE_ID));
            else
                restore(event);
        }
    }

    public final static String DEFAULT_PREFERRED_CONTENT = "text/html"; //$NON-NLS-1$

    public final Image attachedFilesImage;
    public final Image mailImage;
    public final Image homeImage;

    private ArrayList<SmtpMessage> tableData = new ArrayList<SmtpMessage>();
    private ArrayList<String> openedMailsIds = new ArrayList<String>();

    private TableColumn to;
    private TableColumn subject;
    private TableColumn date;
    private CTabFolder folder;
    private Table table;
    private Composite parent;
    private MailsterSWT main;

    private MCTabFolder2Listener folderLayoutListener;
    private ToolItem toggleView;

    private String preferredContentType = DEFAULT_PREFERRED_CONTENT;

    private DropdownSelectionListener dropdownListener;

    public MailView(Composite parent, MailsterSWT main)
    {
        this.parent = parent;
        this.main = main;

        attachedFilesImage = main.getSWTHelper().loadImage("attach.gif"); //$NON-NLS-1$
        mailImage = main.getSWTHelper().loadImage("mail_into.gif"); //$NON-NLS-1$
        homeImage = main.getSWTHelper().loadImage("home.gif"); //$NON-NLS-1$

        tabGradient = main.getSWTHelper().getGradientColors(5,
                new Color(Display.getCurrent(), 0, 84, 227),
                new Color(Display.getCurrent(), 61, 149, 255));

        createView();
    }

    private void createView()
    {
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;

        SashForm sashForm = new SashForm(parent, SWT.NONE);
        sashForm.setOrientation(SWT.VERTICAL);
        sashForm.setLayoutData(gridData);
        createTable(sashForm);
        createTabFolder(sashForm);
        sashForm.setWeights(new int[] { 20, 80 });
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
        return new Browser(parent, SWT.BORDER);
    }

    private void createMailTab(SmtpMessage msg)
    {
        String id = msg.getHeaderValue(SmtpHeadersInterface.MESSAGE_ID);
        if (!openedMailsIds.contains(id))
        {
            final CTabItem item = new CTabItem(folder, SWT.CLOSE);
            item.setText(msg.getSubject());
            item.setImage(mailImage);

            GridData gridData = new GridData();
            gridData.horizontalAlignment = GridData.FILL;
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            gridData.verticalAlignment = GridData.FILL;

            SashForm sash = new SashForm(folder, SWT.NONE);
            sash.setOrientation(SWT.VERTICAL);
            sash.setLayoutData(gridData);
            item.setControl(sash);
            Text hdr = createRawMailView(sash);
            Browser _browser = null;

            try
            {
                _browser = createBrowser(sash);
            }
            catch (SWTError swt)
            {
                main.log(swt.getMessage());
                item.dispose();
                return;
            }

            _browser.setText(msg.getContent(preferredContentType));
            hdr.setText(msg.getRawMessage());

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
        String id = msg.getHeaderValue(SmtpHeadersInterface.MESSAGE_ID);
        if (openedMailsIds.contains(id))
        {
            for (int i = 0, max = folder.getItemCount(); i < max; i++)
            {
                if (folder.getItems()[i].getData() != null
                        && id
                                .equals(((SmtpMessage) folder.getItems()[i]
                                        .getData())
                                        .getHeaderValue(SmtpHeadersInterface.MESSAGE_ID)))
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

    private void createTable(Composite parent)
    {
        table = new Table(parent, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setItemCount(0);
        TableColumn attachments = new TableColumn(table, SWT.NONE);
        attachments.setResizable(false);
        attachments.setMoveable(true);
        attachments.setWidth(20);
        to = new TableColumn(table, SWT.NONE);
        to.setText(Messages.getString("MailView.column.to")); //$NON-NLS-1$
        to.setResizable(true);
        to.setMoveable(true);
        to.setWidth(100);
        subject = new TableColumn(table, SWT.NONE);
        subject.setText(Messages.getString("MailView.column.subject")); //$NON-NLS-1$
        subject.setResizable(true);
        subject.setMoveable(true);
        subject.setWidth(100);
        date = new TableColumn(table, SWT.NONE);
        date.setText(Messages.getString("MailView.column.date")); //$NON-NLS-1$
        date.setResizable(true);
        date.setMoveable(true);
        date.setWidth(100);

        Listener tableListener = new Listener() {
            public void handleEvent(Event e)
            {
                if (e.type == SWT.SetData)
                {
                    TableItem item = (TableItem) e.item;
                    SmtpMessage msg = (SmtpMessage) tableData.get(table
                            .indexOf(item));
                    String date = msg.getDate();
                    try
                    {
                        date = MailsterSWT.df
                                .format(MailUtilities.rfc822DateFormatter
                                        .parse(date));
                    }
                    catch (ParseException ex)
                    {
                        ex.printStackTrace();
                    }
                    item.setText(new String[] {
                            "", //$NON-NLS-1$
                            msg.getHeaderValue(SmtpHeadersInterface.TO),
                            msg.getSubject(), date });
                    if (msg.getInternalParts().getAttachedFiles().length > 0)
                        item.setImage(attachedFilesImage);
                    item.setData(msg);
                }
                else if (e.type == SWT.DefaultSelection)
                    createMailTab((SmtpMessage) table.getSelection()[0]
                            .getData());
                else if (e.type == SWT.Selection)
                    selectMailTab((SmtpMessage) table.getSelection()[0]
                            .getData());
            }
        };

        table.addListener(SWT.Selection, tableListener);
        table.addListener(SWT.DefaultSelection, tableListener);
        table.addListener(SWT.SetData, tableListener);

        table.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e)
            {
                updateTableColumnsWidth();
            }
        });

        // Add sort indicator and sort data when column selected
        Listener sortListener = new Listener() {
            public void handleEvent(Event e)
            {
                // determine new sort column and direction
                TableColumn sortColumn = table.getSortColumn();
                final TableColumn currentColumn = (TableColumn) e.widget;
                int dir = table.getSortDirection();
                if (sortColumn == currentColumn)
                {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                }
                else
                {
                    table.setSortColumn(currentColumn);
                    dir = SWT.UP;
                }

                // sort the data based on column and direction
                final int direction = dir;
                Collections.sort(tableData, new Comparator<SmtpMessage>() {
                    public int compare(SmtpMessage row0, SmtpMessage row1)
                    {
                        try
                        {
                            int result = compareTo(row0, row1, currentColumn);
                            return direction == SWT.UP ? result : -result;
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                            return 0;
                        }
                    }
                });

                // update data displayed in table
                table.setSortDirection(dir);
                table.clearAll();
            }
        };
        to.addListener(SWT.Selection, sortListener);
        subject.addListener(SWT.Selection, sortListener);
        date.addListener(SWT.Selection, sortListener);
        table.setSortColumn(to);
        table.setSortDirection(SWT.UP);
    }

    private void updateTableColumnsWidth()
    {
        int w = (table.getSize().x
                - (table.getBorderWidth() * (table.getColumnCount() - 1)) - table
                .getColumn(0).getWidth())
                / (table.getColumnCount() - 1);

        for (int i = 1, max = table.getColumnCount(); i < max; i++)
            table.getColumn(i).setWidth(w);
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
        if (folder.getSelection() == null)
            return;

        SmtpMessage msg = (SmtpMessage) folder.getSelection().getData();

        if (msg == null)
            return;
        SashForm sash = ((SashForm) folder.getSelection().getControl());
        if (toggleView.getSelection())
            sash.setWeights(new int[] { 100, 0 });
        else
            sash.setWeights(new int[] { 0, 100 });
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

        ToolBar right = new ToolBar(folder, SWT.FILL | SWT.FLAT);
        right.setLayoutData(new FillLayout());
        ToolItem home = main
                .getSWTHelper()
                .createToolItem(
                        right,
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
                        right,
                        SWT.CHECK,
                        "", //$NON-NLS-1$
                        Messages.getString("MailView.toggle.tooltip"), "mail.gif", true); //$NON-NLS-1$ //$NON-NLS-2$

        toggleView.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                setMailViewMode();
            }
        });

        ToolItem item = main.getSWTHelper().createToolItem(right,
                SWT.FLAT | SWT.DROP_DOWN, "", "", "attach.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        new ToolItem(right, SWT.SEPARATOR);
        dropdownListener = new DropdownSelectionListener(item);
        item.addSelectionListener(dropdownListener);
        folder.setTopRight(right);

        Display display = Display.getCurrent();
        folder.setSelectionBackground(tabGradient,
                new int[] { 10, 20, 30, 40 }, true);
        folder.setSelectionForeground(display.getSystemColor(SWT.COLOR_WHITE));
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

    public ArrayList<SmtpMessage> getTableData()
    {
        return tableData;
    }

    public void closeAllTabs()
    {
        for (int i = folder.getItemCount() - 1; i >= 0; i--)
        {
            CTabItem item = folder.getItems()[i];

            if (item.getData() != null)
                openedMailsIds.remove(((SmtpMessage) item.getData())
                        .getHeaderValue(SmtpHeadersInterface.MESSAGE_ID));

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
}
