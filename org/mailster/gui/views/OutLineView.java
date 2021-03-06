package org.mailster.gui.views;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.MailsterSWT;
import org.mailster.core.crypto.smime.SmimeUtilities;
import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.mail.SmtpMessagePart;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;

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
 * FilterTreeView.java - A tree that shows the ouline of a mail.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.9 $, $Date: 2009/01/30 01:32:30 $
 */
public class OutLineView
	extends TreeView
{
	private ToolItem checkSignatureItem;
	private boolean isSigned;

	public OutLineView(Composite parent, boolean enableToolbar)
	{
		super(parent, enableToolbar);
		setMessage(null);

		tree.setBackground(MailsterSWT.BGCOLOR);
		tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e)
			{
				TreeItem _item = (TreeItem) e.item;
				MailsterSWT.getInstance().getMultiView().getMailView().setRawText(
						((SmtpMessagePart) _item.getData()).toString());
			}
		});
	}

	protected void customizeToolbar(ToolBar toolBar)
	{
		checkSignatureItem = new ToolItem(toolBar, SWT.PUSH);
		checkSignatureItem.setImage(SWTHelper.loadImage("hierarchy.gif")); //$NON-NLS-1$
		checkSignatureItem.setToolTipText(Messages.getString("MailView.signingCertificates.show.tooltip")); //$NON-NLS-1$

		checkSignatureItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				MailsterSWT.getInstance().getMultiView().getMailView().showCertificateDialogBox();
			}
		});

		new ToolItem(toolBar, SWT.SEPARATOR);

		// TODO decryption
	}

	public void setMessage(SmtpMessage msg)
	{
		if (tree.isDisposed())
			return;

		tree.removeAll();

		if (msg != null)
		{
			String cType = msg.getInternalParts().getContentType();
			isSigned = SmimeUtilities.isSignedMessage(msg);

			root = new TreeItem(tree, SWT.NONE);
			root.setImage(SWTHelper.loadImage("mail.gif")); //$NON-NLS-1$

			if (cType == null)
				cType = Messages.getString("OutLineView.contentType.unknown"); //$NON-NLS-1$

			root.setText(cType);
			root.setData(msg.getInternalParts());

			createMailTreeItems(root, msg.getInternalParts());
			root.setExpanded(true);

			checkSignatureItem.setEnabled(isSigned);
			collapseAllItem.setEnabled(true);
			expandAllItem.setEnabled(true);
		}
		else
		{
			checkSignatureItem.setEnabled(false);
			collapseAllItem.setEnabled(false);
			expandAllItem.setEnabled(false);
		}

		navigateUpItem.setEnabled(false);
	}

	private void createMailTreeItems(TreeItem currentRoot, SmtpMessagePart part)
	{
		if (part != null && part.getParts() != null)
		{
			Iterator<SmtpMessagePart> it = part.getParts().iterator();
			while (it.hasNext())
			{
				SmtpMessagePart p = it.next();
				TreeItem item = new TreeItem(currentRoot, SWT.NONE);
				item.setText(p.getContentType());
				item.setData(p);

				if (SmimeUtilities.isEnvelopedData(p))
					item.setImage(SWTHelper.loadImage("smime.gif")); //$NON-NLS-1$
				else if (isSigned && "application/pkcs7-signature".equals(p.getContentType()))
					item.setImage(SWTHelper.loadImage("smime_sig.gif")); //$NON-NLS-1$
				else if (p.getFileName() != null && p.getFileName().lastIndexOf('.') > -1)
				{
					String fileName = p.getFileName();
					String ext = fileName.substring(fileName.lastIndexOf('.')); //$NON-NLS-1$
					Program program = Program.findProgram(ext);

					if (program != null && program.getImageData() != null)
						item.setImage(new Image(SWTHelper.getDisplay(), program.getImageData()));
					else
						item.setImage(SWTHelper.loadImage("attach.gif")); //$NON-NLS-1$
				}
				else
					item.setImage(SWTHelper.loadImage("part.gif")); //$NON-NLS-1$

				createMailTreeItems(item, p);
				item.setExpanded(true);
			}
		}
	}
}
