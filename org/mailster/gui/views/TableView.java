package org.mailster.gui.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.mail.Flags;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
import org.mailster.gui.glazedlists.BatchEventList;
import org.mailster.gui.glazedlists.SmtpMessageTableFormat;
import org.mailster.gui.glazedlists.SmtpMessageTableItemConfigurer;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.pop3.mailbox.StoredSmtpMessage;

import ca.odell.glazedlists.AbstractEventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swt.EventTableViewer;
import ca.odell.glazedlists.swt.GlazedListsSWT;
import ca.odell.glazedlists.swt.TableComparatorChooser;
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
	private final static int SWT_BACKSPACE_KEYCODE = 8;
	
    private EventTableViewer<StoredSmtpMessage> msgTableViewer;
    private Table table;
    
    private TableColumn to;
    private TableColumn subject;
    private TableColumn date;
    
    private AbstractEventList<StoredSmtpMessage> eventList;
    private SortedList<StoredSmtpMessage> dataList;

    public TableView(Composite parent, MailView mailView, FilterTreeView treeView, Text filterTextField)
    {
        createTable(parent, mailView, treeView, filterTextField);
        treeView.installListeners(this);
    }
    
    public boolean isTableDisposed()
    {
    	return table == null || table.isDisposed();
    }

    public EventList<StoredSmtpMessage> getSelection()
    {
    	return msgTableViewer.getSelected();
    }
    
	public void setSelection(StoredSmtpMessage stored)
    {
		if (msgTableViewer.getSourceList().contains(stored))
		{
			msgTableViewer.getTogglingSelected().clear();
			msgTableViewer.getTogglingSelected().add(stored);
			table.showSelection();
		}
    }
    
    protected AbstractEventList<StoredSmtpMessage> getEventList()
    {
        return eventList;
    }

    protected AbstractEventList<StoredSmtpMessage> getTableList()
    {
        return dataList;
    }
    
    protected void clearQueue(FilterTreeView treeView)
    {
    	eventList.getReadWriteLock().writeLock().lock();
    	try
    	{
    		eventList.clear();
    		treeView.updateMessagesCounts(eventList);
    	}
    	finally
    	{
    		eventList.getReadWriteLock().writeLock().unlock();
    	}
    }
    
    private void createTable(Composite parent, final MailView mailView, 
            final FilterTreeView treeView, Text filterTextField)
    {
        final Composite tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 2));
        
        /*DebugList<StoredSmtpMessage> tmp = new DebugList<StoredSmtpMessage>();
        tmp.setLockCheckingEnabled(true);*/
        
        eventList = new BasicEventList<StoredSmtpMessage>();
        eventList.getReadWriteLock().readLock().lock();
        final BatchEventList<StoredSmtpMessage, StoredSmtpMessage> batchList;
        try
        {
        	batchList = new BatchEventList<StoredSmtpMessage, StoredSmtpMessage>(GlazedListsSWT.swtThreadProxyList(eventList, Display.getDefault()));        	
        }
        finally
        {
        	eventList.getReadWriteLock().readLock().unlock();
        }
        
        table = new Table(tableComposite, SWT.VIRTUAL | SWT.BORDER
                | SWT.FULL_SELECTION | SWT.MULTI);
        
        final FilterList<StoredSmtpMessage> treeFilteredList = treeView.getFilterList(batchList);
        
        String[] filterProperties = new String[] { "message.to", "message.subject" };  //$NON-NLS-1$  //$NON-NLS-2$
        TextFilterator<StoredSmtpMessage> filterator = GlazedLists
                .textFilterator(filterProperties);
        TextWidgetMatcherEditor<StoredSmtpMessage> matcher = new TextWidgetMatcherEditor<StoredSmtpMessage>(
                filterTextField, filterator);
        
        final FilterList<StoredSmtpMessage> filterList = new FilterList<StoredSmtpMessage>(
                treeFilteredList, matcher);
        
        dataList = new SortedList<StoredSmtpMessage>(filterList, new Comparator<StoredSmtpMessage>() {
            public int compare(StoredSmtpMessage row0, StoredSmtpMessage row1)
            {
                return 0;
            }
        });
        
        treeView.addMessageCounter(dataList, eventList);
        
        SmtpMessageTableFormat tf = new SmtpMessageTableFormat();
        
        msgTableViewer = new EventTableViewer<StoredSmtpMessage>(dataList, table, tf);
        msgTableViewer.setTableItemConfigurer(new SmtpMessageTableItemConfigurer());

        table.addListener(SWT.MouseDown, new Listener() 
        {
			public void handleEvent(Event event) 
			{
				Rectangle clientArea = table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = table.getTopIndex();
				while (index < table.getItemCount()) 
				{
					boolean visible = false;
					final TableItem item = table.getItem(index);
					for (int i = 0; i < table.getColumnCount(); i++) 
					{
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt) && i == 3) 
						{
					        StoredSmtpMessage stored = (StoredSmtpMessage) item.getData() ;
					        stored.setChecked(!stored.isChecked());
					        eventList.getReadWriteLock().writeLock().lock();
					        try
					        {
					        	eventList.set(eventList.indexOf(stored), stored);
					        }
					        finally
					        {
					        	eventList.getReadWriteLock().writeLock().unlock();
					        }
					        treeView.updateMessagesCounts(eventList);
							return;
						}
						if (!visible && rect.intersects(clientArea)) 
							visible = true;
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
        
        TableComparatorChooser.install(msgTableViewer, dataList, false);

        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setItemCount(0);

        TableColumn attachments = table.getColumn(0);
        attachments.setResizable(false);
        attachments.setMoveable(true);
        attachments.setWidth(28);
        attachments.setImage(SWTHelper.loadImage("attach.gif")); //$NON-NLS-1$
        attachments.setAlignment(SWT.RIGHT);        
        
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
        flagColumn.setAlignment(SWT.RIGHT);        
        //flagColumn.setImage(SWTHelper.loadGrayImage("flag16.png")); //$NON-NLS-1$
        
        date = table.getColumn(4);
        date.setResizable(true);
        date.setMoveable(true);
        date.setWidth(100);
        date.setAlignment(SWT.RIGHT);

        table.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event)
            {
            	/*
                 * If you wish to paint the selection beyond the end of a
                 * column, you must change the clipping region.
                 */
                Rectangle area = table.getClientArea();
                int columnCount = table.getColumnCount();
                if (event.index == columnCount - 1 || event.index == 0)
                {
                	int width = area.x + area.width - event.x;
                    if (width > 0)
                        event.width = width;
                }
            }
        });
        
        final Color selectionBackground = SWTHelper.createColor(144, 187, 248);
        final Color selectionForeground = SWTHelper.createColor(12, 97, 232);        
        final Color tableRowColor = SWTHelper.createColor(243, 245, 248);
        
        table.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event)
            {
                boolean selected = (event.detail & SWT.SELECTED) != 0;
                GC gc = event.gc;
                Rectangle area = table.getClientArea();
                
                Color foreground = gc.getForeground();
                Color background = gc.getBackground();
                if (selected)
                {
                	gc.setForeground(selectionForeground);
                	gc.setBackground(selectionBackground);
                    gc.fillRectangle(0, event.y, area.width, event.height);
                }
                else
                {
                	TableItem item = (TableItem) event.item;
                    gc.setForeground(item.getForeground());
                    
                    if (table.indexOf(item) % 2 == 1)
                    	gc.setBackground(tableRowColor);
                    else
                    	gc.setBackground(table.getBackground());
                    
                    gc.fillRectangle(0, event.y, area.width, event.height);
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
                    
                    if (msgTableViewer.getSelected().size() == 1)
                    {
                    	StoredSmtpMessage stored = msgTableViewer.getSelected().get(0);
                        if (e.type == SWT.DefaultSelection)
                        {
                        	stored.setSeen();
                        	eventList.getReadWriteLock().writeLock().lock();
                        	try
                        	{
                        		eventList.set(eventList.indexOf(stored), stored);
                        	}
                        	finally
                        	{
                        		eventList.getReadWriteLock().writeLock().unlock();
                        	}
                            
                            mailView.createMailTab(stored);
                            treeView.updateMessagesCounts(eventList);
                        }
                        else if (e.type == SWT.Selection && mailView.isSynced())
                            mailView.selectMailTab(stored);
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
            		msgTableViewer.getTogglingDeselected().clear();
                    return;
            	}
            	
                if (e.keyCode == ' ' || e.keyCode == SWT_BACKSPACE_KEYCODE)
                {   
                	boolean checked = e.keyCode == ' ';
                	batchList.getReadWriteLock().writeLock().lock();                	
                	try
                	{
                		batchList.beginBatch();
	                	for (StoredSmtpMessage stored : msgTableViewer.getSelected())
	                	{
	                       	stored.setChecked(checked);                        
	                       	batchList.set(batchList.indexOf(stored), stored);
	                    }	                	
	                	batchList.commitBatch();
                	}
                	finally
                	{                		
                		batchList.getReadWriteLock().writeLock().unlock();
                	}
                	treeView.updateMessagesCounts(eventList);
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
    	                    	List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>(msgTableViewer.getSelected().size());
    	                    	for (StoredSmtpMessage stored : msgTableViewer.getSelected())
    	                    	{
    	                    		l.add(stored);
    	                    		mailView.getSMTPService().getPop3Service().removeMessage(stored);
    	                    		if (mailView.isSynced())
    	                    			mailView.closeTab(stored.getMessageId());
    	                    	}
    	                    	dataList.getReadWriteLock().writeLock().lock();
    	                    	try
    	                    	{
    	                    		dataList.removeAll(l);
    	                    	}
    	                    	finally
    	                    	{
    	                    		dataList.getReadWriteLock().writeLock().unlock();
    	                    	}
                        	}
                        	catch (Exception ex)
                        	{
                        		ex.printStackTrace();
                        	}
                        }
                    }
                    else
                    {
                    	batchList.getReadWriteLock().writeLock().lock();                	
                    	try
                    	{
                    		batchList.beginBatch();                    		
                    		for (StoredSmtpMessage stored : msgTableViewer.getSelected())
	                        {
	                            if (!stored.getFlags().contains(Flags.Flag.FLAGGED))
	                            {
	                            	stored.getFlags().add(Flags.Flag.FLAGGED);
	                            	batchList.set(batchList.indexOf(stored), stored);
	                            }
	                        }
    	                	batchList.commitBatch();
    	                	treeView.filter();
                    	}
                    	finally
                    	{                		
                    		batchList.getReadWriteLock().writeLock().unlock();
                    	}
                    }
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
    
    protected void updateTableColumnsWidth()
    {
        Rectangle area = table.getClientArea();
        Point size = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        int scroll = size.y > area.height + table.getHeaderHeight()
                && table.getVerticalBar() != null ? table.getVerticalBar()
                .getSize().x : 0;

        int w = (area.width
                - table.getColumn(0).getWidth() - table.getColumn(3).getWidth() - scroll)
                / (table.getColumnCount() - 2);

        for (int i = 1, max = table.getColumnCount(); i < max; i++)
        {
            if (i != 3 && i < max - 1)
                table.getColumn(i).setWidth((int) (w * 1.2));
            if (i == max - 1)
            {
            	int width = area.width - 1;
            	
            	for (int j = 0, jmax = table.getColumnCount()-1; j < jmax; j++)
            		width -= table.getColumn(j).getWidth();
            	
            	table.getColumn(max-1).setWidth(width);
            }
        }
    }
}