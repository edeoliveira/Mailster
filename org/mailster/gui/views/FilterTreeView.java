package org.mailster.gui.views;

import java.util.List;

import javax.mail.Flags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.MailsterSWT;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.widgets.DropDownListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.DisposableMap;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

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
 * FilterTreeView.java - A tree that filters mails by destination host.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.19 $, $Date: 2009/04/05 12:06:05 $
 */
public class FilterTreeView
	extends TreeView
{
	private static final Logger LOG = LoggerFactory.getLogger(FilterTreeView.class);
	private static final String DELETED_TREEITEM_LABEL = Messages.getString("MailsterSWT.treeView.trash.label"); //$NON-NLS-1$
	private static final String CHECKED_TREEITEM_LABEL = Messages.getString("MailsterSWT.treeView.flaggedMail.label"); //$NON-NLS-1$
	private static final Image FOLDER_IMAGE = SWTHelper.loadImage("folder.gif"); //$NON-NLS-1$
	
	private TreeItem deletedMailsTreeItem;
	private TreeItem checkedMailsTreeItem;

	private ToolItem clearQueueToolItem;

	private MenuItem exportAsMailItem;
	private MenuItem exportAsMailBoxItem;

	private HostMatcherEditor editor;

	/**
	 * The count of messages last time a event occurred. Prevents from multiple unnecessary updates.
	 */
	private long lastCallCount = 0;

	private class HostMatcherEditor
		extends AbstractMatcherEditor<StoredSmtpMessage>
		implements SelectionListener
	{
		private class HostMatcher
			implements Matcher<StoredSmtpMessage>
		{
			private TreeItem selectedItem;

			public HostMatcher()
			{
			}

			public void setSelectedItem(TreeItem selectedItem)
			{
				this.selectedItem = selectedItem;
			}

			public boolean matches(StoredSmtpMessage msg)
			{
				if (selectedItem == root)
					return !msg.getFlags().contains(Flags.Flag.FLAGGED);
				else if (selectedItem == deletedMailsTreeItem)
					return msg.getFlags().contains(Flags.Flag.FLAGGED);
				else if (selectedItem == checkedMailsTreeItem)
					return !msg.getFlags().contains(Flags.Flag.FLAGGED) 
							&& msg.isChecked();
				else
					return !msg.getFlags().contains(Flags.Flag.FLAGGED)
							&& ((String) selectedItem.getData()).equals(msg.getMessageHost());
			}
		}

		private Tree mailBoxTree;
		private final HostMatcher matcher = new HostMatcher();

		public HostMatcherEditor(Tree mailBoxTree)
		{
			this.mailBoxTree = mailBoxTree;
			mailBoxTree.addSelectionListener(this);
		}

		public void filter()
		{
			final TreeItem[] selected = mailBoxTree.getSelection();
			if (selected == null || selected.length == 0)
				mailBoxTree.setSelection(root);
			else
			{
				matcher.setSelectedItem(selected[0]);
				fireChanged(matcher);
			}
		}

		public void widgetDefaultSelected(SelectionEvent event)
		{
			filter();
		}

		public void widgetSelected(SelectionEvent event)
		{
			filter();
		}
	}

	/**
	 * This class provides the "drop down" functionality for the import/export button.
	 */
	class ImportExportDropDownMenu
		extends DropDownListener
	{
		private MenuItem importAsMailItem;
		private MenuItem importAsMailBoxItem;

		public ImportExportDropDownMenu(ToolItem dropdown)
		{
			super(dropdown);
			buildMenu();
		}

		public void buildMenu()
		{
			Image importImage = SWTHelper.decorateImage(SWTHelper.loadImage("folder.gif"), SWTHelper.loadImage("incoming.gif"), //$NON-NLS-1$ //$NON-NLS-2$ 
					SWT.BOTTOM | SWT.LEFT);
			Image exportImage = SWTHelper.decorateImage(SWTHelper.loadImage("folder_closed.gif"), SWTHelper //$NON-NLS-1$
					.loadImage("outgoing.gif"), SWT.BOTTOM | SWT.LEFT); //$NON-NLS-1$

			dropdown.setEnabled(true);
			dropdown.setToolTipText(Messages.getString("FilterTreeview.toggle.importExportTooltip")); //$NON-NLS-1$

			importAsMailItem = new MenuItem(menu, SWT.NONE);
			importAsMailItem.setText(Messages.getString("FilterTreeview.toggle.importAsMailTooltip")); //$NON-NLS-1$
			importAsMailItem.setImage(importImage);

			menu.setDefaultItem(importAsMailItem);

			importAsMailBoxItem = new MenuItem(menu, SWT.NONE);
			importAsMailBoxItem.setText(Messages.getString("FilterTreeview.toggle.importAsMailBoxTooltip")); //$NON-NLS-1$
			importAsMailBoxItem.setImage(importImage);

			exportAsMailItem = new MenuItem(menu, SWT.NONE);
			exportAsMailItem.setText(Messages.getString("FilterTreeview.toggle.exportAsMailTooltip")); //$NON-NLS-1$
			exportAsMailItem.setImage(exportImage);
			exportAsMailItem.setEnabled(false);

			exportAsMailBoxItem = new MenuItem(menu, SWT.NONE);
			exportAsMailBoxItem.setText(Messages.getString("FilterTreeview.toggle.exportAsMailBoxTooltip")); //$NON-NLS-1$
			exportAsMailBoxItem.setImage(exportImage);
			exportAsMailBoxItem.setEnabled(false);

			SelectionAdapter adapter = new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt)
				{
					doMenuAction((MenuItem) evt.getSource());
				}
			};

			importAsMailItem.addSelectionListener(adapter);
			importAsMailBoxItem.addSelectionListener(adapter);
			exportAsMailItem.addSelectionListener(adapter);
			exportAsMailBoxItem.addSelectionListener(adapter);
		}

		private void doMenuAction(MenuItem item)
		{
			menu.setDefaultItem(item);

			if (item == importAsMailItem)
				ImportExportUtilities.importFromEmailFile();
			else if (item == importAsMailBoxItem)
				ImportExportUtilities.importFromMbox();
			else if (item == exportAsMailItem)
				ImportExportUtilities.exportAsEmailFile();
			else if (item == exportAsMailBoxItem)
				ImportExportUtilities.exportAsMbox();
		}

		public void buttonPushed()
		{
			doMenuAction(menu.getDefaultItem());
		}
	}

	public FilterTreeView(Composite parent, boolean enableToolbar)
	{
		super(parent, enableToolbar);

		tree.setBackground(MailsterSWT.BGCOLOR);
		final Font NORMAL_FONT = SWTHelper.createFont(new FontData("Segoe UI", 8, SWT.NONE));
		tree.setFont(NORMAL_FONT);

		collapseAllItem.setEnabled(true);
		expandAllItem.setEnabled(true);

		root = new TreeItem(tree, SWT.NONE);
		root.setImage(SWTHelper.loadImage("forum16.png")); //$NON-NLS-1$
		root.setText(Messages.getString("MailsterSWT.treeView.root.label")); //$NON-NLS-1$

		checkedMailsTreeItem = new TreeItem(root, SWT.NONE);
		checkedMailsTreeItem.setImage(SWTHelper.loadImage("flag16.png")); //$NON-NLS-1$
		checkedMailsTreeItem.setText(CHECKED_TREEITEM_LABEL);
		checkedMailsTreeItem.setForeground(SWTHelper.createColor(12, 97, 232));
		checkedMailsTreeItem.setData(CHECKED_TREEITEM_LABEL);

		deletedMailsTreeItem = new TreeItem(root, SWT.NONE);
		deletedMailsTreeItem.setImage(SWTHelper.loadImage("clearArchive16.png")); //$NON-NLS-1$
		deletedMailsTreeItem.setText(DELETED_TREEITEM_LABEL);
		deletedMailsTreeItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		deletedMailsTreeItem.setData(DELETED_TREEITEM_LABEL);

		root.setExpanded(true);
		tree.setSelection(root);
		final Font bold = SWTHelper.makeBoldFont(tree.getFont());

		tree.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event e)
			{
				TreeItem item = (TreeItem) e.item;
				if (item.getText().indexOf('(') > 0)
					item.setFont(bold);
				else
					item.setFont(tree.getFont());
			}
		});
		
		setupDragAndDrop();
    }
    
    private void setupDragAndDrop()
    {
    	DropTarget target = new DropTarget(tree, DND.DROP_MOVE);
     	target.setTransfer(new Transfer[] {FileTransfer.getInstance(), TextTransfer.getInstance()});
    	target.addDropListener(new DropTargetAdapter() {
			/**
    		@Override
			public void dragEnter(DropTargetEvent evt)
			{
				if (tree.getSelection()[0] != deletedMailsTreeItem)
					evt.detail = DND.DROP_NONE;
			}

			@Override
			public void dragOver(DropTargetEvent evt)
			{
				if (tree.getSelection()[0] == deletedMailsTreeItem)
				{
					if (evt.item == root)
						evt.detail = DND.DROP_MOVE;
					else
						evt.detail = DND.DROP_NONE;
				}
			}**/

			@Override
			public void drop(DropTargetEvent evt)
			{
				/**
				if (TextTransfer.getInstance().isSupportedType(evt.currentDataType))
				{
					TreeItem item = (TreeItem) evt.item;
					System.out.println("source ="+tree.getSelection()[0].getText()); 
					System.out.println("Data dropped: "+evt.data);
					System.out.println("|-target="+item.getText());
				}
				else**/
				{
					if (FileTransfer.getInstance().isSupportedType(evt.currentDataType))
					{
						String[] files = (String[]) evt.data;
						for (String file : files)
						{
							if (file.toLowerCase().endsWith(".eml"))
								ImportExportUtilities.importFromEmailFile(file);
							else if (file.toLowerCase().endsWith(".mbx"))
								ImportExportUtilities.importFromMbox(file);
						}
					}					
				}
			}
		});
    }

	protected void customizeToolbar(ToolBar toolBar)
	{
		final ToolItem refreshToolItem = new ToolItem(toolBar, SWT.PUSH);
		refreshToolItem.setImage(SWTHelper.loadImage("refresh.gif")); //$NON-NLS-1$
		refreshToolItem.setToolTipText(Messages.getString("MailsterSWT.refreshQueue.tooltip")); //$NON-NLS-1$   

		clearQueueToolItem = new ToolItem(toolBar, SWT.PUSH);
		clearQueueToolItem.setImage(SWTHelper.loadImage("closeall.gif")); //$NON-NLS-1$
		clearQueueToolItem.setToolTipText(Messages.getString("MailsterSWT.clearQueue.tooltip")); //$NON-NLS-1$
		clearQueueToolItem.setEnabled(false);

		final ToolItem importExportToolItem = SWTHelper.createToolItem(toolBar, SWT.FLAT | SWT.DROP_DOWN,
				"", Messages.getString("MailView.import.export.tooltip"), "importExport.gif", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		ImportExportDropDownMenu dd = new ImportExportDropDownMenu(importExportToolItem);
		importExportToolItem.addSelectionListener(dd);

		new ToolItem(toolBar, SWT.SEPARATOR);
 
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				if (e.widget == refreshToolItem)
					MailsterSWT.getInstance().getSMTPService().refreshEmailQueue(false);
				else if (e.widget == clearQueueToolItem)
					MailsterSWT.getInstance().getSMTPService().clearQueue();
			}
		};

		refreshToolItem.addSelectionListener(selectionAdapter);
		clearQueueToolItem.addSelectionListener(selectionAdapter);
	}
	
	public void updateMessageCounts(EventList<StoredSmtpMessage> eventList)
	{
		LOG.debug("Call to updateMessagesCounts()");

		String filter;
		List<StoredSmtpMessage> l;
		DisposableMap<String, List<StoredSmtpMessage>> map;

		eventList.getReadWriteLock().readLock().lock();
		try
		{
			lastCallCount = eventList.size();
			map = GlazedLists.syncEventListToMultiMap(
					eventList, 
					new FunctionList.Function<StoredSmtpMessage, String>() {
						public String evaluate(StoredSmtpMessage msg)
						{
							if (msg.getFlags().contains(Flags.Flag.FLAGGED))
								return DELETED_TREEITEM_LABEL;
							else
								return msg.getMessageHost();
						}
					});
		} finally
		{
			eventList.getReadWriteLock().readLock().unlock();
		}

		tree.setRedraw(false);
		for (TreeItem child : root.getItems())
		{
			if (child == checkedMailsTreeItem)
				continue;
			else
			{
				filter = (String) child.getData();
				l = map == null ? null : map.get(filter);
			}

			long count = l == null ? 0 : l.size();

			if (count > 0)
			{
				StringBuilder countLabel = new StringBuilder(filter);

				count = 0;
				eventList.getReadWriteLock().readLock().lock();
				try
				{
					for (StoredSmtpMessage msg : l)
					{
						// We only count unread messages
						if (!msg.isSeen())
							count++;
					}
				} finally
				{
					eventList.getReadWriteLock().readLock().unlock();
				}

				if (count > 0)
					countLabel.append(" (").append(count).append(')');
				
				child.setText(countLabel.toString());
			}
			else if (l == null && !DELETED_TREEITEM_LABEL.equals(filter))
			{
				if (!findInFolder(CHECKED_TREEITEM_LABEL, eventList, map, filter) &&
						!findInFolder(DELETED_TREEITEM_LABEL, eventList, map, filter))
					child.dispose();
			}
			else
				child.setText(filter);
		}
		tree.setRedraw(true);
		map.dispose();
	}

	private boolean findInFolder(String folderLabel, 
			EventList<StoredSmtpMessage> eventList, 
			DisposableMap<String, List<StoredSmtpMessage>> map,
			String host)
	{
		eventList.getReadWriteLock().readLock().lock();
		try
		{
			List<StoredSmtpMessage> folder = map.get(folderLabel);
			if (folder != null)
			{
				for (StoredSmtpMessage msg : folder)
				{
					if (host.equals(msg.getMessageHost()))
						return true;
				}
			}
		}
		finally
		{
			eventList.getReadWriteLock().readLock().unlock();
		}
		
		return false;
	}
	
	public void installListeners(EventList<StoredSmtpMessage> filterList, final EventList<StoredSmtpMessage> baseList)
	{
		filterList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
			public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
			{
				baseList.getReadWriteLock().readLock().lock();
				try
				{
					if (!listChanges.isReordering() && lastCallCount != baseList.size())
						updateMessageCounts(baseList);
				} finally
				{
					baseList.getReadWriteLock().readLock().unlock();
				}
			}
		});

		baseList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
			public void listChanged(ListEvent<StoredSmtpMessage> evt)
			{
				boolean notEmpty = !baseList.isEmpty();

				clearQueueToolItem.setEnabled(notEmpty);
				exportAsMailItem.setEnabled(notEmpty);
				exportAsMailBoxItem.setEnabled(notEmpty);
			}
		});
	}

	private void addNodeIfNewHost(String host)
	{
		for (TreeItem child : root.getItems())
		{
			if (((String) child.getData()).equals(host))
				return;
		}

		try
		{
			TreeItem item = new TreeItem(root, SWT.NONE, 0);
			item.setImage(FOLDER_IMAGE);
			item.setText(host);
			item.setData(host);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public FilterList<StoredSmtpMessage> buildFilterList(final EventList<StoredSmtpMessage> eventList)
	{
		eventList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
			public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
			{
				if (listChanges.isReordering())
					return;
				
				tree.setRedraw(false);
				while (listChanges.next())
				{
					if (listChanges.getType() == ListEvent.INSERT || listChanges.getType() == ListEvent.UPDATE)
					{
						String host = eventList.get(listChanges.getIndex()).getMessageHost();
						addNodeIfNewHost(host);
					}
				}

				root.setExpanded(true);
				tree.setRedraw(true);
			}
		});

		eventList.getReadWriteLock().readLock().lock();
		try
		{
			editor = new HostMatcherEditor(tree);
			return new FilterList<StoredSmtpMessage>(eventList, editor);
		} finally
		{
			eventList.getReadWriteLock().readLock().unlock();
		}
	}

	public void filter()
	{
		if (editor != null)
			editor.filter();
	}

	public boolean isTrashFolderSelected()
	{
		TreeItem[] sel = tree.getSelection();
		return sel != null && sel.length > 0 && sel[0] == deletedMailsTreeItem;
	}
}