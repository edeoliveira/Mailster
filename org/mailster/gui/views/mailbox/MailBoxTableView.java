package org.mailster.gui.views.mailbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.mailster.MailsterSWT;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.FilterTreeView;
import org.mailster.gui.views.ImportExportUtilities;
import org.mailster.util.DateUtilities;
import org.mailster.util.DateUtilities.DateFormatterEnum;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * MailBoxView.java - The mailbox table view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.27 $, $Date: 2009/05/18 22:14:26 $
 */
public class MailBoxTableView
	implements MailBoxTableInterface
{
	private MailBoxTableViewFormat fmt = new MailBoxTableViewFormat();

	private TableViewer _viewer;
	private Table _table;

	private EventList<StoredSmtpMessage> _eventList;
	private SortedList<StoredSmtpMessage> _sortedList;
	private FilterList<StoredSmtpMessage> _filterList;

	public MailBoxTableView(Composite parent, Text filterText)
	{
		createTableViewer(parent, filterText);
	}

	public FilterList<StoredSmtpMessage> getFilterList()
	{
		return _filterList;
	}

	private void createTableColumns()
	{
		TableColumn attachments = new TableColumn(_table, SWT.NONE);
		attachments.setResizable(false);
		attachments.setMoveable(true);
		attachments.setWidth(28);
		attachments.setImage(SWTHelper.loadImage("attach.gif")); //$NON-NLS-1$
		attachments.setAlignment(SWT.RIGHT);

		TableColumn to = new TableColumn(_table, SWT.NONE);
		to.setResizable(true);
		to.setMoveable(true);
		to.setWidth(100);
        to.setText(Messages.getString("MailView.column.to")); //$NON-NLS-1$
		to.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e)
			{
				sortColumn(1);
			}
		});

		TableColumn subject = new TableColumn(_table, SWT.NONE);
		subject.setResizable(true);
		subject.setMoveable(true);
		subject.setWidth(100);
        subject.setText(Messages.getString("MailView.column.subject")); //$NON-NLS-1$
		subject.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e)
			{
				sortColumn(2);
			}
		});

		TableColumn flagColumn = new TableColumn(_table, SWT.NONE);
		flagColumn.setResizable(false);
		flagColumn.setMoveable(true);
		flagColumn.setWidth(18);
		flagColumn.setAlignment(SWT.RIGHT);
		//flagColumn.setImage(SWTHelper.loadGrayImage("flag16.png")); //$NON-NLS-1$

		TableColumn date = new TableColumn(_table, SWT.NONE);
		date.setResizable(true);
		date.setMoveable(true);
		date.setWidth(100);
		date.setAlignment(SWT.RIGHT);
        date.setText(Messages.getString("MailView.column.date")); //$NON-NLS-1$
		date.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e)
			{
				sortColumn(4);
			}
		});
	}
	
	public void createTableViewer(Composite parent, Text filterText)
	{
		final FilterTreeView treeView = MailsterSWT.getInstance().getFilterTreeView();
		final Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 2));
		
		// TODO to see if SWT.VIRTUAL does not cause any trouble any more
		_viewer = new TableViewer(tableComposite, SWT.NONE | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
		_viewer.setLabelProvider(new GlazedLabelProvider());
		_viewer.setContentProvider(new GlazedContentProvider());
		_viewer.setUseHashlookup(true);
		
		_table = _viewer.getTable();
		_table.setHeaderVisible(true);
		_table.setLinesVisible(false);

		createTableColumns();
		
		final int lastColIndex = _table.getColumnCount() - 1;
		final Color colBG = SWTHelper.createColor(144, 187, 248);
		final Color tableRowColor = SWTHelper.createColor(243, 245, 248);

		_table.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event)
			{
				GC gc = event.gc;
				Rectangle area = _table.getClientArea();
				Color background = gc.getBackground();

				// event.detail &= ~SWT.HOT;
				if ((event.detail & SWT.SELECTED) != 0)
				{
					// If you wish to paint the selection beyond the end of
					// last column, you must change the clipping region.
					if (event.index == lastColIndex)
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
					Rectangle rect = event.getBounds();
					gc.setBackground(colBG);
					gc.fillRectangle(0, rect.y, area.width, rect.height);
					
					event.detail &= ~SWT.SELECTED;
				}
				else
				{
					TableItem item = (TableItem) event.item;

					if (_table.indexOf(item) % 2 != 0)
						gc.setBackground(tableRowColor);
					else
						gc.setBackground(_table.getBackground());

					gc.fillRectangle(0, event.y, area.width, event.height);
				}
				
				// restore colors for subsequent drawing
				gc.setBackground(background);
			}
		});

		_table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event)
			{
				// If you wish to paint the selection beyond the end of a
				// column, you must change the clipping region.
				Rectangle area = _table.getClientArea();
				if (event.index == lastColIndex)
				{
					int width = area.x + area.width - event.x;
					if (width > 0)
						event.width = width;
				}
			}
		});

		_table.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e)
			{
				updateTableColumnsWidth();
			}
		});

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		_table.setLayoutData(gd);
		
		final Label countLabel = new Label(tableComposite, SWT.NONE);
		countLabel.setText("<0/0>"); //$NON-NLS-1$
		final Color c = SWTHelper.createColor(0, 127, 0);

		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.END;
		countLabel.setLayoutData(gd);
		
		//DebugList<StoredSmtpMessage> tmp = new DebugList<StoredSmtpMessage>();
		//tmp.setLockCheckingEnabled(true);
		BasicEventList<StoredSmtpMessage> tmp = new BasicEventList<StoredSmtpMessage>();		

		// hook up everything with glazed lists
		_eventList = tmp;// new BasicEventList<StoredSmtpMessage>();
		_eventList.getReadWriteLock().readLock().lock();
		try
		{
			_sortedList = new SortedList<StoredSmtpMessage>(_eventList, null);
		} finally
		{
			_eventList.getReadWriteLock().readLock().unlock();
		}

		final FilterList<StoredSmtpMessage> treeFilteredList = treeView.buildFilterList(_sortedList);

		String[] filterProperties = new String[] {"messageFrom", "messageTo", "messageSubject"}; //$NON-NLS-1$  //$NON-NLS-2$
		TextFilterator<StoredSmtpMessage> filterator = GlazedLists.textFilterator(filterProperties);
		TextWidgetMatcherEditor<StoredSmtpMessage> matcher = new TextWidgetMatcherEditor<StoredSmtpMessage>(filterText,
				filterator);

		_filterList = new FilterList<StoredSmtpMessage>(treeFilteredList, matcher);
		_filterList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
			public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
			{
				try
				{
					int curr = _filterList.size();
					int total = _eventList.size();
					countLabel.setText("<" + curr + "/" + total + ">");	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (curr == 0 && total > 0)
						countLabel.setForeground(_table.getDisplay().getSystemColor(SWT.COLOR_RED));
					else if (curr < total)
						countLabel.setForeground(c);
					else
						countLabel.setForeground(_table.getDisplay().getSystemColor(SWT.COLOR_BLACK));
					tableComposite.layout();

					_table.setRedraw(false);

					// get the list before looping, otherwise it won't be the same list as it's
					// modified continuously
					final List<StoredSmtpMessage> changeList = new ArrayList<StoredSmtpMessage>(listChanges.getSourceList());

					while (listChanges.next())
					{
						int sourceIndex = listChanges.getIndex();
						int changeType = listChanges.getType();

						switch (changeType)
						{
							case ListEvent.DELETE :
								// note the remove of the object fetched from the event list here,
								// we need to remove by index which the viewer does not support
								// and we're removing from the raw list, not the filtered list
								StoredSmtpMessage o = _eventList.get(sourceIndex);
								_viewer.refresh(o, true);
							break;
							case ListEvent.INSERT :
								StoredSmtpMessage obj = changeList.get(sourceIndex);
								_viewer.insert(obj, sourceIndex);
							break;
							case ListEvent.UPDATE :
							break;
						}
					}
				} catch (Exception err)
				{
					err.printStackTrace();
				} finally
				{
					// most important, we update the table size after the update
					_viewer.setItemCount(_filterList.size());

					_table.setRedraw(true);

					// we could do detailed refreshes, but this isn't much of a performance hit
					_viewer.refresh(true);
				}
			}
		});

		// populate initial table
		_eventList.getReadWriteLock().readLock().lock();
		try
		{
			_viewer.setInput(_eventList);
			_viewer.setItemCount(_eventList.size());
		} finally
		{
			_eventList.getReadWriteLock().readLock().unlock();
		}

		// the mail item check listener
		_table.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event)
			{
				Rectangle clientArea = _table.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = _table.getTopIndex();
				while (index < _table.getItemCount())
				{
					boolean visible = false;
					final TableItem item = _table.getItem(index);
					for (int i = 0; i < _table.getColumnCount(); i++)
					{
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt) && i == 3)
						{
							StoredSmtpMessage stored = (StoredSmtpMessage) item.getData();
							stored.setChecked(!stored.isChecked());
							_viewer.refresh(stored, true);
							treeView.updateMessageCounts(_eventList);
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
		
		_table.setSortColumn(_table.getColumn(MailBoxTableViewFormat.DATE_COLUMN));
		_table.setSortDirection(SWT.DOWN);
		
		treeView.installListeners(_filterList, _eventList);
		setupFileDrop(_table);
	}

	public static void setupFileDrop(Control ctrl)
	{
		DropTarget dt = new DropTarget(ctrl, DND.DROP_DEFAULT | DND.DROP_MOVE);
		dt.setTransfer(new Transfer[] {FileTransfer.getInstance()});
		dt.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event)
			{
				FileTransfer ft = FileTransfer.getInstance();
				if (ft.isSupportedType(event.currentDataType))
				{
					String[] files = (String[]) event.data;
					for (String file : files)
					{
						if (file.toLowerCase().endsWith(".eml"))
							ImportExportUtilities.importFromEmailFile(file);
						else if (file.toLowerCase().endsWith(".mbx"))
							ImportExportUtilities.importFromMbox(file);
					}
				}
			}
		});
	}
	
	private void sortColumn(int col)
	{
		int dir = SWT.UP;
		int current = _table.getSortDirection();
		TableColumn tc = _table.getColumn(col);
		if (_table.getSortColumn() == tc)
		{
			dir = current == SWT.UP ? SWT.DOWN : (current == SWT.DOWN ? SWT.NONE : SWT.UP);
		}

		_table.setSortColumn(tc);
		_table.setSortDirection(dir);

		// now tell the sorted list we've updated
		_sortedList.getReadWriteLock().writeLock().lock();
		try
		{
			_sortedList.setComparator(new GlazedSortComparator(col, dir));
		} finally
		{
			_sortedList.getReadWriteLock().writeLock().unlock();
		}
	}

	protected void updateTableColumnsWidth()
	{
		Rectangle area = _table.getClientArea();
		int scroll = _table.getVerticalBar().isVisible() ? _table.getVerticalBar().getSize().x : 0;

		int nbColumns = _table.getColumnCount();
		int w = (area.width - _table.getColumn(0).getWidth() - _table.getColumn(3).getWidth() - scroll) / (nbColumns - 2);

		int max = nbColumns - 1;
		for (int i = 1; i < max; i++)
		{
			if (i != 3)
				_table.getColumn(i).setWidth((int) (w * 1.2));
		}

		int width = area.width - 1;

		for (int j = 0; j < max; j++)
			width -= _table.getColumn(j).getWidth();

		_table.getColumn(max).setWidth(width);
	}

	class GlazedSortComparator
		implements Comparator<StoredSmtpMessage>
	{
		private int _col;
		private int _direction;

		public GlazedSortComparator(int col, int direction)
		{
			_col = col;
			_direction = direction;
		}

		public int compare(StoredSmtpMessage o1, StoredSmtpMessage o2)
		{
			int ret = 0;
			if (fmt.getColumnValue(o1, _col) instanceof Date)
				ret = ((Date) fmt.getColumnValue(o1, _col)).compareTo((Date) fmt.getColumnValue(o2, _col));
			else
				ret = ((String) fmt.getColumnValue(o1, _col)).compareTo((String)fmt.getColumnValue(o2, _col));
			
			if (_direction == SWT.DOWN)
				return -ret;
			else
				return ret;
		}
	}

	class GlazedContentProvider
		implements IStructuredContentProvider
	{
		public Object[] getElements(Object inputElement)
		{
			return _filterList.toArray();
		}

		public void dispose()
		{
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
		}
	}

	class GlazedLabelProvider
		implements ITableLabelProvider, ITableFontProvider
	{
		public void addListener(ILabelProviderListener listener)
		{
		}

		public void removeListener(ILabelProviderListener listener)
		{
		}		

		public void dispose()
		{
		}

		public boolean isLabelProperty(Object element, String property)
		{
			return false;
		}

		public Image getColumnImage(Object element, int columnIndex)
		{
			return fmt.getColumnImage(element, columnIndex);
		}

		public String getColumnText(Object element, int columnIndex)
		{
			Object columnValue = fmt.getColumnValue((StoredSmtpMessage) element, columnIndex);
			if (columnIndex == MailBoxTableViewFormat.DATE_COLUMN)
			{
				Date d = (Date) columnValue;
				
	            if (DateUtilities.isCurrentDay(d))
            		return DateUtilities.format(DateFormatterEnum.HOUR, d);
	            else
            		return DateUtilities.format(DateFormatterEnum.DF, d);
			}
			else
				return columnValue == null || !(columnValue instanceof String)? 
							"" : (String) columnValue;
		}

		public Font getFont(Object element, int index)
		{
			StoredSmtpMessage msg = (StoredSmtpMessage) element;
			
	    	if (msg != null && !msg.isSeen())
				return MailBoxView.BOLD_FONT;
	    	else
	    		return MailBoxView.NORMAL_FONT;
		}
	}

	EventList<StoredSmtpMessage> getEventList()
	{
		return _eventList;
	}

	EventList<StoredSmtpMessage> getTableList()
	{
		return _filterList;
	}

	public void addTableListener(int eventType, Listener listener)
	{
		_table.addListener(eventType, listener);
	}

	@SuppressWarnings("unchecked")
	public List<StoredSmtpMessage> getSelection()
	{
		return new ArrayList<StoredSmtpMessage>((List<StoredSmtpMessage>) ((IStructuredSelection) _viewer.getSelection())
				.toList());
	}

	public Shell getShell()
	{
		return _table.getShell();
	}

	public void removeTableListener(int eventType, Listener listener)
	{
		_table.removeListener(eventType, listener);
	}

	public void selectAll()
	{
		_table.selectAll();
	}

	public void setSelection(List<StoredSmtpMessage> selected)
	{
		List<StoredSmtpMessage> tmp = new ArrayList<StoredSmtpMessage>();
		for (StoredSmtpMessage msg : selected)
		{
			if (msg != null)
				tmp.add(msg);
		}
		
		if (tmp.size() == 0)
			_viewer.setSelection(null);
		else
		{
			ISelection sel = new StructuredSelection(tmp);
			_viewer.setSelection(sel, true);
		}
	}

	public void setTableRedraw(boolean redraw)
	{
		_table.setRedraw(redraw);
	}

	public void refreshViewer(Object elt, boolean updateLabels)
	{
		_viewer.refresh(elt, updateLabels);
	}
	
	public boolean focus()
	{
		return _table.forceFocus();
	}
}