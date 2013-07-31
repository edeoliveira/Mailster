package org.mailster.gui.views.mailbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.mailster.MailsterSWT;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.SWTHelper;
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
 * MailBoxView.java - The new mailbox view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.27 $, $Date: 2009/05/18 22:14:26 $
 */
public class MailBoxView
	extends Composite
	implements MailBoxTableInterface
{
	public static final Font NORMAL_FONT = SWTHelper.createFont(new FontData("Segoe UI", 8, SWT.NONE));
	public static final Font BOLD_FONT = SWTHelper.makeBoldFont(NORMAL_FONT);
	public static final Image ATTACHED_FILES_IMAGE = SWTHelper.loadImage("attach.gif");

	private final ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
	
	private static final int MIN_WIDTH_FOR_MULTI_COLUMN = 380;

	private SearchBoxView sbView;
	private MailBoxTableView tableView;

	private MailBoxListener mboxListener;
	private TreeList<MailBoxItem> treeList;
	private	TableTree tableTree;
	private Table table;
	
	public MailBoxView(final Composite parent)
	{
		super(parent, SWT.NONE);
		createMailBoxView(parent);
	}

	private boolean isVerticalBarNeeded(Table table)
	{
		Rectangle rect = table.getClientArea();
		int itemHeight = table.getItemHeight();
		int headerHeight = table.getHeaderHeight();
		int visibleCount = (rect.height - headerHeight + itemHeight - 1) / itemHeight;

		return table.getItemCount() >= visibleCount;
	}

	private void resizeTableTreeColumn(Table table, boolean isVerticalBarNeeded)
	{
		int w = table.getBounds().width - (table.getBorderWidth() * 2);
		if (isVerticalBarNeeded || table.getVerticalBar().isVisible())
			w -= table.getVerticalBar().getSize().x;

		table.getColumn(0).setWidth(w);
	}

	private void createMailBoxView(final Composite parent)
	{
		parent.setLayout(new FillLayout());
		setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));

		sbView = new SearchBoxView(this, SWT.NONE);
		sbView.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Composite tComposite = new Composite(this, SWT.NONE);
		tComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		final StackLayout sl = new StackLayout();
		tComposite.setLayout(sl);

		final Composite tableViewComposite = createTableView(tComposite);
		final Composite tableTreeComposite = new Composite(tComposite, SWT.NONE);
		sl.topControl = tableTreeComposite;
		createTableTree(tableTreeComposite);

		parent.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				int w = parent.getSize().x;
				if (w > MIN_WIDTH_FOR_MULTI_COLUMN && sl.topControl != tableViewComposite) {
					sl.topControl = tableViewComposite;
					mboxListener.switchListenerSource();
					MailBoxView.this.layout();
				}
				else if (w <= MIN_WIDTH_FOR_MULTI_COLUMN && sl.topControl != tableTreeComposite) {
					sl.topControl = tableTreeComposite;
					mboxListener.switchListenerSource();
					MailBoxView.this.layout();					
				}
			}
		});
	}

	private void createTableTree(Composite tableTreeComposite)
	{
		tableTree = new TableTree(tableTreeComposite, SWT.NONE | SWT.FULL_SELECTION | SWT.MULTI);
		table = tableTree.getTable();
		table.setHeaderVisible(true);
		MailBoxTableView.setupFileDrop(tableTree);

		tableTreeComposite.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 0, 0));
		tableTree.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
		table.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				resizeTableTreeColumn(table, false);
			}
		});

		TableColumn column = new TableColumn(table, SWT.NONE, 0);
		column.setText("Mailbox");
		column.setResizable(false);
		column.setMoveable(false);

		final FunctionList<StoredSmtpMessage, MailBoxItem> fl = new FunctionList<StoredSmtpMessage, MailBoxItem>(tableView
				.getTableList(), new StoredSmtpMessage2MailBoxItemFunction());

		treeList = new TreeList<MailBoxItem>(fl, new MailBoxTreeFormat(),
				new MailBoxTreeExpansionModel());

		mboxListener = new MailBoxListener(this, tableView, tableView.getEventList());
		
		svc.scheduleAtFixedRate(new Runnable() {
			public void run()
			{
				MailBoxItem.computeCategories();
				table.getDisplay().syncExec(new Runnable() {
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
				table.setRedraw(false);
				int countSelected = table.getSelectionCount();

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
							tableTree.removeByRealIndex(sourceIndex);
						break;
						case ListEvent.INSERT :
							MailBoxItem m = changeList.get(sourceIndex);
							TableTreeItem item = null;
							if (m.isRoot())
							{
								item = tableTree.insertAtRealIndex(sourceIndex, SWT.MULTI);
								item.setExpanded(true);
							}
							else
								item = tableTree.insertAtRealIndex(sourceIndex, SWT.NONE);
							item.setText(m.toString());
							item.setData(m);
						break;
						case ListEvent.UPDATE :
							m = changeList.get(sourceIndex);
							item = tableTree.getByRealIndex(sourceIndex);
							item.setText(m.toString());
							item.setData(m);
						break;
					}
				}

				if (countSelected != 0 && table.getSelectionCount() == 0)
					MailsterSWT.getInstance().getMultiView().switchTopControl(false);

				table.setRedraw(true);
				resizeTableTreeColumn(table, isVerticalBarNeeded(table));
			}
		});		
	}
	
	private Composite createTableView(final Composite parent)
	{
		Composite mailTableComposite = new Composite(parent, SWT.NONE);
		mailTableComposite.setLayout(new FillLayout());

		tableView = new MailBoxTableView(mailTableComposite, sbView.getText());

		return mailTableComposite;
	}

	public List<StoredSmtpMessage> getExportSelection(boolean all)
	{
		List<StoredSmtpMessage> mails = null;
		
		if (!all)
			mails = tableView.getSelection();

		if (all || mails == null || mails.size() == 0) 
		{
			tableView.getTableList().getReadWriteLock().readLock().lock();
			try
			{
				mails = new ArrayList<StoredSmtpMessage>(tableView.getTableList());
			}
			finally
			{
				tableView.getTableList().getReadWriteLock().readLock().unlock();
			}
		}
		return mails;
	}

	public EventList<StoredSmtpMessage> getEventList()
	{
		return tableView.getEventList();
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

	public void selectAll()
	{
		TableTreeItem[] roots = tableTree.getItems();
		for (TableTreeItem item : roots)
			item.setExpanded(true);
		TableItem[] selectedItems = new TableItem[table.getItemCount() - roots.length];
		int pos = 0;
		for (TableTreeItem root : roots)
		{
			for (TableTreeItem item : root.getItems())
				selectedItems[pos++] = item.tableItem;
		}

		table.setSelection(selectedItems);
	}

	public void setSelection(List<StoredSmtpMessage> selected)
	{
		int[] sel = new int[selected.size()];
		int i=0;
		for (StoredSmtpMessage msg : selected)
			sel[i++] = treeList.indexOf(new MailBoxItem(msg));
		
		table.setSelection(sel);
	}

	public List<StoredSmtpMessage> getSelection()
	{
		List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>(table.getSelectionCount());
		for (int idx : table.getSelectionIndices())
			l.add(treeList.get(idx).getMessage());

		return l;
	}
	
	public void addTableListener(int eventType, Listener listener)
	{
		table.addListener(eventType, listener);
	}

	public void removeTableListener(int eventType, Listener listener)
	{
		table.removeListener(eventType, listener);
	}

	public void setTableRedraw(boolean redraw)
	{
		table.setRedraw(redraw);
	}

	public void refreshViewer(Object elt, boolean updateLabels)
	{
		// nothing todo
	}

	public boolean focus()
	{
		return table.forceFocus();
	}
}