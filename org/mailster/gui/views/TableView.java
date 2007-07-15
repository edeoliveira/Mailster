package org.mailster.gui.views;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.mail.Flags;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.glazedlists.SmtpMessageTableFormat;
import org.mailster.gui.glazedlists.SmtpMessageTableLabelProvider;
import org.mailster.gui.glazedlists.swt.TableComparatorChooser;
import org.mailster.gui.glazedlists.swt.TableViewerManager;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SmtpHeadersInterface;
import org.mailster.smtp.SmtpMessage;
import org.mailster.util.DateUtilities;

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
 * TableView.java - Handles the mail table and backing data list.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class TableView
{
    private TableViewerManager msgTableViewer;
    private TableViewer viewer;
    private Table table;
    
    private TableColumn to;
    private TableColumn subject;
    private TableColumn date;
    
    private BasicEventList<StoredSmtpMessage> eventList;
    private SortedList<StoredSmtpMessage> dataList;

    public TableView(Composite parent, MailView mailView, FilterTreeView treeView, Text filterTextField)
    {
        createTable(parent, mailView, treeView, filterTextField);
    }
    
    public Table getTable()
    {
        return table;
    }
    
    public void refreshTable()
    {
        if (msgTableViewer != null)
            msgTableViewer.getTableViewer().refresh();
    }
    
    public AbstractEventList<StoredSmtpMessage> getDataList()
    {
        return dataList;
    }
    
    public void clearDataList()
    {
        eventList.clear();
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
                return DateUtilities.rfc822DateFormatter.parse(msg.getDate());
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

    @SuppressWarnings("unchecked")
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
    
    private void createTable(Composite parent, final MailView mailView, 
            final FilterTreeView treeView, Text filterTextField)
    {
        final Composite tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 2));

        eventList = new BasicEventList<StoredSmtpMessage>();        
        table = new Table(tableComposite, SWT.VIRTUAL | SWT.BORDER
                | SWT.FULL_SELECTION | SWT.MULTI);
        
        final FilterList<StoredSmtpMessage> treeFilteredList = treeView.getFilterList(eventList, table);
        
        String[] filterProperties = new String[] { "message.to", "message.subject" };  //$NON-NLS-1$  //$NON-NLS-2$
        TextFilterator<StoredSmtpMessage> filterator = GlazedLists
                .textFilterator(filterProperties);
        TextWidgetMatcherEditor matcher = new TextWidgetMatcherEditor(
                filterTextField, filterator);
        
        @SuppressWarnings("unchecked") //$NON-NLS-1$
        final FilterList<StoredSmtpMessage> filterList = new FilterList<StoredSmtpMessage>(
                treeFilteredList, matcher);
        
        dataList = new SortedList<StoredSmtpMessage>(filterList,
                new Comparator<StoredSmtpMessage>() {
                    public int compare(StoredSmtpMessage row0, StoredSmtpMessage row1)
                    {
                        try
                        {
                            return compareTo(row0.getMessage(), row1.getMessage(), table.getSortColumn());
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                            return 0;
                        }
                    }
                });
        
        treeView.addMessageCounter(dataList, eventList);
        
        SmtpMessageTableFormat tf = new SmtpMessageTableFormat();
        
        viewer = new TableViewer(table);        
        viewer.setCellEditors(
                new CellEditor[] {null, null, null, new CheckboxCellEditor(), null});

        msgTableViewer = new TableViewerManager(viewer, dataList, 
                new SmtpMessageTableLabelProvider(dataList, tf));
        
        TableComparatorChooser.install(msgTableViewer, dataList, false);

        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setItemCount(0);

        TableColumn attachments = table.getColumn(0);
        attachments.setResizable(false);
        attachments.setMoveable(true);
        attachments.setWidth(28);
        attachments.setImage(SWTHelper.loadImage("attach.gif")); //$NON-NLS-1$
        attachments.setAlignment(SWT.LEFT);
        
        to = table.getColumn(1);
        to.setResizable(true);
        to.setMoveable(true);
        to.setWidth(100);

        subject = table.getColumn(2);
        subject.setResizable(true);
        subject.setMoveable(true);
        subject.setWidth(100);

        TableColumn flagColumn = table.getColumn(3);
        flagColumn.setResizable(false);
        flagColumn.setMoveable(true);
        flagColumn.setWidth(18);
        flagColumn.setAlignment(SWT.CENTER);
        flagColumn.setText(" ");
        //flagColumn.setImage(SWTHelper.loadImage("public_co.gif")); //$NON-NLS-1$
        
        date = table.getColumn(4);
        date.setResizable(true);
        date.setMoveable(true);
        date.setWidth(100);
        date.setAlignment(SWT.RIGHT);

        table.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event)
            {
                boolean selected = (event.detail & SWT.SELECTED) != 0;
                GC gc = event.gc;
                Rectangle area = table.getClientArea();
                /*
                 * If you wish to paint the selection beyond the end of last
                 * column, you must change the clipping region.
                 */
                int columnCount = table.getColumnCount();
                if (event.index == columnCount - 1 || columnCount == 0)
                {
                    int width = area.x + area.width - event.x;
                    if (width > 0)
                    {
                        Region region = new Region();
                        gc.getClipping(region);
                        region.add(event.x, event.y, width, event.height);
                        gc.setClipping(region);
                        region.dispose();
                    }
                }
                if (selected)
                {
                    gc.setAdvanced(true);
                    if (gc.getAdvanced())
                        gc.setAlpha(127);
                }
                Rectangle rect = event.getBounds();
                Color foreground = gc.getForeground();
                Color background = gc.getBackground();
                Display display = Display.getDefault();
                if (selected)
                {
                    gc.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
                    gc.setBackground(display
                            .getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    gc.fillGradientRectangle(0, rect.y, area.width+100, rect.height, false);
                }
                else
                {
                    gc.setForeground(((TableItem) event.item).getForeground());
                    gc.setBackground(((TableItem) event.item).getBackground());
                    gc.fillRectangle(0, rect.y, area.width+100, rect.height);
                }
                gc.setForeground(foreground);
                gc.setBackground(background);
                if (selected)
                    event.detail &= ~SWT.SELECTED;
            }
        }); 
        
        Listener tableListener = new Listener() {
            public void handleEvent(Event e)
            {
                try
                {
                    if (e.type == SWT.Selection && e.detail == SWT.CHECK)
                        return;
                    
                    for (TableItem item : table.getSelection())
                    {
                        StoredSmtpMessage stored = (StoredSmtpMessage) item.getData();
                        if (e.type == SWT.DefaultSelection)
                        {
                            stored.setSeen();
                            msgTableViewer.getTableViewer().update(stored, null);
                            mailView.createMailTab(stored);
                            treeView.updateMessagesCounts(eventList);
                        }
                        else if (e.type == SWT.Selection)
                            mailView.selectMailTab(stored.getMessage());
                    }
                }
                catch (SWTError ex)
                {
                    mailView.log(ex.getMessage());
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
        
        table.addListener (SWT.KeyDown, new Listener() {
            public void handleEvent(Event e)
            {
            	if (((e.stateMask & SWT.CTRL) != 0) && e.keyCode == 'a')
            	{
                    viewer.setSelection(new StructuredSelection(getDataList()));
                    return;
            	}
            	
                if (e.keyCode == ' ')
                {                    
                	for (TableItem item : table.getSelection())
                    {
                		StoredSmtpMessage msg = ((StoredSmtpMessage)item.getData());
                        if ((e.stateMask & SWT.SHIFT) == 0)
                            msg.setChecked(true);
                        else
                            msg.setChecked(!msg.isChecked());
                        msgTableViewer.getTableViewer().update(msg, null);
                    }
                    return;
                }
                
                if (e.keyCode == SWT.DEL)
                {
                    if (treeView.isTrashFolderSelected() || (e.stateMask & SWT.SHIFT) != 0)
                    {
                        if (!ConfigurationManager.CONFIG_STORE.
                                getBoolean(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY) 
                            || MessageDialog.openConfirm(table.getShell(), 
                                    Messages.getString("MailView.dialog.confirm.deleteMails"), 
                                    Messages.getString("MailView.dialog.confirm.question")))
                        {
                        	try
                        	{
    	                    	List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>(table.getSelection().length);
    	                    	for (TableItem item : table.getSelection())
    	                    	{
    	                    		StoredSmtpMessage stored = (StoredSmtpMessage)item.getData();
    	                    		l.add(stored);
    	                    		mailView.getSMTPService().getPop3Service().removeMessage(stored);
    	                    	}
                                table.deselectAll();
    	                    	dataList.removeAll(l);                                
                        	}
                        	catch (Exception ex)
                        	{
                        		ex.printStackTrace();
                        	}
                        }
                    }
                    else
                    {
                        for (TableItem item : table.getSelection())
                        {
                            StoredSmtpMessage msg = ((StoredSmtpMessage)item.getData());
                            if (!msg.getFlags().contains(Flags.Flag.FLAGGED))
                                msg.getFlags().add(Flags.Flag.FLAGGED);
                        }
                    }
                    treeView.filter();
                    treeView.updateMessagesCounts(eventList);
                    return;
                }
            }
        });
        
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
        filterList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
            public void listChanged(ListEvent<StoredSmtpMessage> event)
            {
                countLabel.setText("<" + filterList.size() + "/" //$NON-NLS-1$  //$NON-NLS-2$
                        + eventList.size() + ">"); //$NON-NLS-1$
                if (filterList.size() == 0 && eventList.size() > 0)
                    countLabel.setForeground(Display.getDefault()
                            .getSystemColor(SWT.COLOR_RED));
                else
                if (filterList.size() < eventList.size())
                    countLabel.setForeground(SWTHelper.createColor(0,
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
                - table.getColumn(0).getWidth() - table.getColumn(3).getWidth() - scroll)
                / (table.getColumnCount() - 2);

        for (int i = 1, max = table.getColumnCount(); i < max; i++)
            if (i != 3)
                table.getColumn(i).setWidth(
                    i < max - 1 ? (int) (w * 1.2) : (int) (w * 0.6));
    }
}