package org.mailster.gui.views.mailbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mailster.MailsterSWT;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.utils.LayoutUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

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
 * MailBoxTableTreeView.java - The new mailbox treeTable tree view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.27 $, $Date: 2009/05/18 22:14:26 $
 */
public class MailBoxTableTreeView
	implements MailBoxTableInterface
{
	private final ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
	
	private TreeList<MailBoxItem> treeList;
	private	MailBoxTableTree mailBoxTableTree;
	private Table treeTable;
	
	private static class StoredSmtpMessage2MailBoxItemFunction
		implements FunctionList.Function<StoredSmtpMessage, MailBoxItem>
	{
		public MailBoxItem evaluate(StoredSmtpMessage value)
		{
			return new MailBoxItem(value);
		}
	}

	private static class MailBoxTreeExpansionModel
		implements TreeList.ExpansionModel<MailBoxItem>
	{
		public boolean isExpanded(MailBoxItem element, List<MailBoxItem> path)
		{
			return true;
		}

		public void setExpanded(MailBoxItem element, List<MailBoxItem> path, boolean expanded)
		{
		}
	}
	
	private static class MailBoxTreeFormat
		implements TreeList.Format<MailBoxItem>
	{
		static final Comparator<MailBoxItem> c0 = new Comparator<MailBoxItem>() {
			public int compare(MailBoxItem o1, MailBoxItem o2)
			{
				long cat1 = o1.getCategory();
				long cat2 = o2.getCategory();

				if (cat1 > cat2)
					return -1;
				else if (cat1 < cat2)
					return 1;
				else
					return 0;
			}
		};

		static final Comparator<MailBoxItem> c1 = new Comparator<MailBoxItem>() {
			public int compare(MailBoxItem o1, MailBoxItem o2)
			{
				long cat1 = o1.getMessage().getMessageDate().getTime();
				long cat2 = o2.getMessage().getMessageDate().getTime();

				if (cat1 > cat2)
					return -1;
				else if (cat1 < cat2)
					return 1;
				else
					return 0;
			}
		};

		public void getPath(List<MailBoxItem> path, MailBoxItem element)
		{
			path.add(new MailBoxItem(element.getCategoryLabel()));
			path.add(element);
		}

		public boolean allowsChildren(MailBoxItem element)
		{
			return true;
		}
		
		public Comparator<? extends MailBoxItem> getComparator(int id)
		{
			if (id == 0)
				return c0;
			else
				return c1;
		}
	}
	
	public MailBoxTableTreeView(final Composite parent, EventList<StoredSmtpMessage> tableList)
	{
		createTableTree(parent, tableList);
	}
	
	private void createTableTree(Composite tableTreeComposite, EventList<StoredSmtpMessage> tableList)
	{
		mailBoxTableTree = new MailBoxTableTree(tableTreeComposite, SWT.NONE | SWT.FULL_SELECTION | SWT.MULTI);
		treeTable = mailBoxTableTree.getTable();
		treeTable.setHeaderVisible(true);
		MailBoxView.setupFileDrop(mailBoxTableTree);

		tableTreeComposite.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));
		mailBoxTableTree.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		treeTable.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				resizeTableTreeColumn(treeTable, false);
			}
		});

		TableColumn column = new TableColumn(treeTable, SWT.NONE, 0);
		column.setText("Mailbox");
		column.setResizable(false);
		column.setMoveable(false);

		final FunctionList<StoredSmtpMessage, MailBoxItem> fl = 
			new FunctionList<StoredSmtpMessage, MailBoxItem>(tableList, 
					new StoredSmtpMessage2MailBoxItemFunction());

		treeList = new TreeList<MailBoxItem>(fl, new MailBoxTreeFormat(),
				new MailBoxTreeExpansionModel());
		
		svc.scheduleAtFixedRate(new Runnable() {
			public void run()
			{
				MailBoxItem.computeCategories();
				treeTable.getDisplay().syncExec(new Runnable() {
					public void run()
					{
						treeList.getReadWriteLock().writeLock().lock();
						try
						{
							List<MailBoxItem> items = new ArrayList<MailBoxItem>();
							ListIterator<MailBoxItem> l = treeList.listIterator();
							while (l.hasNext())
							{
								MailBoxItem item = l.next();
								if (!item.isRoot() && item.resetCategory())
								{
									items.add(item);
								}
							}
							treeList.removeAll(items);
							treeList.addAll(items);
						} finally
						{
							treeList.getReadWriteLock().writeLock().unlock();
						}
					}
				});
			}
		}, ((MailBoxItem.tomorrow + 1) - System.currentTimeMillis()), 24L * 60L * 60L * 1000L, TimeUnit.MILLISECONDS);

		treeList.addListEventListener(new ListEventListener<MailBoxItem>() {
			public void listChanged(final ListEvent<MailBoxItem> listChanges)
			{
				treeTable.setRedraw(false);
				int countSelected = treeTable.getSelectionCount();

				// get the list before looping, otherwise
				// it won't be the same list as it's modified continuously
				final List<MailBoxItem> changeList = new ArrayList<MailBoxItem>(listChanges.getSourceList());

				while (listChanges.next())
				{
					int sourceIndex = listChanges.getIndex();
					int changeType = listChanges.getType();

					switch (changeType)
					{
						case ListEvent.DELETE :
							mailBoxTableTree.removeByRealIndex(sourceIndex);
						break;
						case ListEvent.INSERT :
							MailBoxItem m = changeList.get(sourceIndex);
							MailBoxTableTreeItem item = null;
							if (m.isRoot())
							{
								item = mailBoxTableTree.insertAtRealIndex(sourceIndex, SWT.MULTI);
								item.setExpanded(true);
							}
							else
								item = mailBoxTableTree.insertAtRealIndex(sourceIndex, SWT.NONE);
							item.setText(m.toString());
							item.setData(m);
						break;
						case ListEvent.UPDATE :
							m = changeList.get(sourceIndex);
							item = mailBoxTableTree.getByRealIndex(sourceIndex);
							item.setText(m.toString());
							item.setData(m);
						break;
					}
				}

				if (countSelected != 0 && treeTable.getSelectionCount() == 0)
					MailsterSWT.getInstance().getMultiView().switchTopControl(false);

				treeTable.setRedraw(true);
				resizeTableTreeColumn(treeTable, isVerticalBarNeeded(treeTable));
			}
		});		
	}
	
	private void resizeTableTreeColumn(Table table, boolean isVerticalBarNeeded)
	{
		int w = table.getBounds().width - (table.getBorderWidth() * 2);
		if (isVerticalBarNeeded || table.getVerticalBar().isVisible())
			w -= table.getVerticalBar().getSize().x;

		table.getColumn(0).setWidth(w);
	}
	
	private boolean isVerticalBarNeeded(Table table)
	{
		Rectangle rect = table.getClientArea();
		int itemHeight = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();
		int visibleCount = (rect.height - headerHeight + itemHeight - 1) / itemHeight;

		return table.getItemCount() >= visibleCount;
	}
	
	public void selectAll()
	{
		MailBoxTableTreeItem[] roots = mailBoxTableTree.getItems();
		for (MailBoxTableTreeItem item : roots)
			item.setExpanded(true);
		TableItem[] selectedItems = new TableItem[treeTable.getItemCount() - roots.length];
		int pos = 0;
		for (MailBoxTableTreeItem root : roots)
		{
			for (MailBoxTableTreeItem item : root.getItems())
				selectedItems[pos++] = item.tableItem;
		}

		treeTable.setSelection(selectedItems);
	}

	public void setSelection(List<StoredSmtpMessage> selected)
	{
		int[] sel = new int[selected.size()];
		int i=0;
		for (StoredSmtpMessage msg : selected)
			sel[i++] = treeList.indexOf(new MailBoxItem(msg));
		
		treeTable.setSelection(sel);
	}

	public List<StoredSmtpMessage> getSelection()
	{
		List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>(treeTable.getSelectionCount());
		for (int idx : treeTable.getSelectionIndices())
			l.add(treeList.get(idx).getMessage());

		return l;
	}
	
	public void addTableListener(int eventType, Listener listener)
	{
		treeTable.addListener(eventType, listener);
	}

	public void removeTableListener(int eventType, Listener listener)
	{
		treeTable.removeListener(eventType, listener);
	}

	public void setTableRedraw(boolean redraw)
	{
		treeTable.setRedraw(redraw);
	}

	public void refreshViewer(Object elt, boolean updateLabels)
	{
		// Nothing to do
	}

	public boolean focus()
	{
		return treeTable.forceFocus();
	}

	@Override
	public Shell getShell()
	{
		return mailBoxTableTree.getShell();
	}	
}
