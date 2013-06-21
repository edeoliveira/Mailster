package org.mailster.gui.views.mailbox;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.mailster.MailsterSWT;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.Messages;
import org.mailster.gui.prefs.ConfigurationManager;
import org.mailster.gui.views.FilterTreeView;

import ca.odell.glazedlists.EventList;

public class MailBoxListener
	implements Listener
{
	private MailBoxTableInterface source;
	private MailBoxTableInterface target;

	private EventList<StoredSmtpMessage> srcList;

	public MailBoxListener(MailBoxTableInterface source, MailBoxTableInterface target, EventList<StoredSmtpMessage> srcList)
	{
		this.source = source;
		this.target = target;
		this.srcList = srcList;
		source.addTableListener(SWT.Selection, this);
		source.addTableListener(SWT.KeyDown, this);
	}

	public void switchListenerSource()
	{
		source.removeTableListener(SWT.Selection, this);
		source.removeTableListener(SWT.KeyDown, this);
		MailBoxTableInterface tmp = source;
		source = target;
		target = tmp;
		source.addTableListener(SWT.Selection, this);
		source.addTableListener(SWT.KeyDown, this);
	}

	public void handleEvent(Event e)
	{
		if (e.type == SWT.Selection)
			handleSelectionEvent(e);
		else
			handleKeyDownEvent(e);
	}
	
	private void setRedraw(boolean redraw)
	{
		source.setTableRedraw(redraw);
		target.setTableRedraw(redraw);
	}

	private void refreshViewers(Object elt, boolean updateLabels)
	{
		source.refreshViewer(elt, updateLabels);
		target.refreshViewer(elt, updateLabels);
	}
	
	public void handleKeyDownEvent(Event e)
	{
		MailsterSWT main = MailsterSWT.getInstance();
		FilterTreeView treeView = main.getFilterTreeView();
		
		if (((e.stateMask & SWT.CTRL) != 0) && e.keyCode == 'a')
		{
			target.selectAll();
			source.selectAll();
		}
		else if (e.keyCode == ' ')
		{
			setRedraw(false);
			for (StoredSmtpMessage stored : source.getSelection())
			{
				stored.setChecked(!stored.isChecked());
				refreshViewers(stored, true);
			}
			setRedraw(true);
			treeView.updateMessageCounts(srcList);
		}
		else if (e.keyCode == 'q')
		{
			setRedraw(false);
			boolean sp = (e.stateMask & SWT.SHIFT) != 0;
			for (StoredSmtpMessage stored : source.getSelection())
			{
				if (sp)
					stored.setNotSeen();
				else
					stored.setSeen();
				refreshViewers(stored, true);
			}
			setRedraw(true);
			treeView.updateMessageCounts(srcList);
		}
		else if (e.keyCode == SWT.DEL)
		{
			boolean trashSelected = treeView.isTrashFolderSelected();
			if (trashSelected && (e.stateMask & SWT.CTRL) != 0)
			{
				setRedraw(false);
				for (StoredSmtpMessage stored : source.getSelection())
				{
					stored.getFlags().remove(Flags.Flag.FLAGGED);
					refreshViewers(stored, true);
				}
				setRedraw(true);
				treeView.filter();
			}
			else if (trashSelected || (e.stateMask & SWT.SHIFT) != 0)
			{
				if (!ConfigurationManager.CONFIG_STORE.getBoolean(ConfigurationManager.ASK_ON_REMOVE_MAIL_KEY)
						|| MessageDialog.openConfirm(source.getShell(),
								Messages.getString("MailView.dialog.confirm.deleteMails"), Messages
										.getString("MailView.dialog.confirm.question")))
				{
					try
					{
						List<StoredSmtpMessage> sel = source.getSelection();
						List<StoredSmtpMessage> l = new ArrayList<StoredSmtpMessage>(sel.size());
						for (StoredSmtpMessage stored : sel)
						{
							l.add(stored);
							main.getSMTPService().getPop3Service().removeMessage(stored);
						}
						main.getMultiView().switchTopControl(false);

						srcList.getReadWriteLock().writeLock().lock();
						try
						{
							srcList.removeAll(l);
						} finally
						{
							srcList.getReadWriteLock().writeLock().unlock();
						}
					} catch (Exception ex)
					{
						main.getMultiView().log(ex.getMessage());
					}
				}
			}
			else if (!trashSelected)
			{
				setRedraw(false);
				for (StoredSmtpMessage stored : source.getSelection())
				{
					if (stored != null)
					{
						stored.getFlags().add(Flags.Flag.FLAGGED);
						refreshViewers(stored, true);
					}
				}
				setRedraw(true);
				treeView.filter();
			}
			treeView.updateMessageCounts(srcList);
		}
	}

	public void handleSelectionEvent(Event e)
	{
		if (e.detail == SWT.CHECK)
			return;

		MailsterSWT main = MailsterSWT.getInstance();
		List<StoredSmtpMessage> l = source.getSelection();

		try
		{
			if (l.size() == 1)
			{
				StoredSmtpMessage stored = l.get(0);
				if (stored != null)
				{
					if (!stored.isSeen())
					{
						stored.setSeen();
						refreshViewers(stored, true);
						main.getFilterTreeView().updateMessageCounts(srcList);
					}
	
					main.getMultiView().getMailView().setMail(stored);
					main.getMultiView().switchTopControl(true);
				}
				else
					main.getMultiView().switchTopControl(false);
			}
		} catch (SWTError ex)
		{
			main.getMultiView().log(ex.getMessage());
		}
		finally
		{
			if (l == null || l.size() <= 0)
				main.getMultiView().switchTopControl(false);
			
			target.setSelection(l);
			source.focus();
		}
	}
}