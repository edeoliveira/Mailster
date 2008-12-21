package org.mailster.gui.views;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.MailsterSWT;
import org.mailster.crypto.smime.SmimeUtilities;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.crypto.CertificateDialog;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SmtpMessage;
import org.mailster.smtp.SmtpMessagePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
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
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * FilterTreeView.java - A tree that shows the ouline of a mail.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class OutLineView extends TreeView
{
    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(OutLineView.class);
    
	private ToolItem checkSignatureItem;
	private boolean isSigned;
	
    public OutLineView(Composite parent, boolean enableToolbar)
    {
    	super(parent, enableToolbar);
        setMessage(null);
    	
        tree.addSelectionListener(new SelectionAdapter() {
  	      public void widgetSelected(SelectionEvent e) {
  	          TreeItem _item = (TreeItem) e.item;  	          
  	          Composite c = (Composite) MailsterSWT.getInstance().getMailView().
						getCTabFolder().getSelection().getControl();
  	          ((Text) c.getChildren()[1]).setText(((SmtpMessagePart)_item.getData()).toString());    	          
  	      }
    	});
    }

    protected  void customizeToolbar(ToolBar toolBar)
    {
        checkSignatureItem = new ToolItem(toolBar, SWT.PUSH);
        checkSignatureItem.setImage(SWTHelper.loadImage("hierarchy.gif")); //$NON-NLS-1$
        checkSignatureItem.setToolTipText(Messages
                .getString("MailView.signingCertificates.show.tooltip")); //$NON-NLS-1$

        checkSignatureItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event)
            {   
                try 
                {
                	CTabFolder folder = MailsterSWT.getInstance().getMailView().getCTabFolder();
                    if (folder != null && folder.getSelection().getData() != null)
                        (new CertificateDialog(tree.getShell(), 
                                ((StoredSmtpMessage) folder.getSelection().getData()).getMessage().asMimeMessage())).open();
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
        
        new ToolItem(toolBar, SWT.SEPARATOR);
        
        //TODO decryption
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
                else
                if (isSigned && "application/pkcs7-signature".equals(p.getContentType()))
                	item.setImage(SWTHelper.loadImage("smime_sig.gif")); //$NON-NLS-1$
                else
                if (p.getFileName()  != null && p.getFileName().lastIndexOf('.') > -1)
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
