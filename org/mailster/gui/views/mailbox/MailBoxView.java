package org.mailster.gui.views.mailbox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.gui.views.ImportExportUtilities;

import ca.odell.glazedlists.EventList;

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
{
	public static final Font NORMAL_FONT = SWTHelper.createFont(new FontData("Segoe UI", 8, SWT.NONE));
	public static final Font BOLD_FONT = SWTHelper.makeBoldFont(NORMAL_FONT);
	public static final Image ATTACHED_FILES_IMAGE = SWTHelper.loadImage("attach.gif");

	protected static final int MIN_WIDTH_FOR_MULTI_COLUMN = 380;

	private SearchBoxView sbView;
	private MailBoxTableView tableView;
	private MailBoxTableTreeView tableTreeView;

	private MailBoxListener mboxListener;
	
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
	
	public MailBoxView(final Composite parent)
	{
		super(parent, SWT.NONE);
		createMailBoxView(parent);
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
		tableTreeView = new MailBoxTableTreeView(tableTreeComposite, tableView.getTableList());
		
		sl.topControl = tableTreeComposite;
		
		mboxListener = new MailBoxListener(tableTreeView, tableView, getEventList());

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

	public List<StoredSmtpMessage> getSelection()
	{
		return mboxListener.getSource().getSelection();
	}

	public void refreshViewer(Object elt, boolean updateLabels)
	{
		mboxListener.getSource().refreshViewer(elt, updateLabels);
	}
}