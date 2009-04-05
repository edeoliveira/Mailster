package org.mailster.gui.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.dialogs.ErrorDialog;
import org.mailster.message.SmtpMessage;
import org.mailster.message.SmtpMessageFactory;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.server.MailsterConstants;
import org.mailster.server.MailsterSmtpService;
import org.mailster.util.StreamUtilities;

import ca.odell.glazedlists.EventList;

public class ImportExportUtilities 
{
    protected static void importFromEmailFile()
    {
		String fileName = getPath(true, false, false, SWT.OPEN);
    	
    	if (fileName != null)
    		importFromEmailFile(fileName);
    }    	

	public static void importFromEmailFile(String fileName)
    {
    	try
        {        		
    		FileInputStream in = new FileInputStream(fileName);
            
    		SmtpMessageFactory factory = 
    			new SmtpMessageFactory(MailsterConstants.DEFAULT_CHARSET, 
    					new LineDelimiter("\n"));
            
    		MailsterSmtpService smtp = MailsterSWT.getInstance().getSMTPService();
    		smtp.addReceivedEmail(factory.asSmtpMessage(in, null));
    		smtp.refreshEmailQueue(false);
            in.close();
        }
        catch (Exception e)
        {
        	ErrorDialog dlg = new ErrorDialog(MailsterSWT.getInstance().getShell(), 
        			"Exception occured", "Failed importing email file : "+fileName, 
        			new Status(IStatus.ERROR, "Mailster", "Unable to import file", e), IStatus.ERROR);
        	dlg.open();
        }
    }

    protected static void importFromMbox()
    {
		String fileName = getPath(true, true, false, SWT.OPEN);
    	
    	if (fileName != null)
    		importFromMbox(fileName);
    }    	
    
    public static void importFromMbox(String fileName)
    {
    	try
        {        		
    		BufferedReader in = new BufferedReader(new FileReader(fileName));
            
            List<SmtpMessage> mails = StreamUtilities.
            	readMessageFromMBoxRDFormat(in, MailsterConstants.DEFAULT_CHARSET);
            
            MailsterSmtpService smtp = MailsterSWT.getInstance().getSMTPService();
            smtp.addReceivedEmail(mails);
            smtp.refreshEmailQueue(false);
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static EventList<StoredSmtpMessage> getEmailSelection()
    {
		EventList<StoredSmtpMessage> mails = 
			MailsterSWT.getInstance().getMailView().getTableView().getSelection();
		
		if (mails == null || mails.size() == 0)
			mails = MailsterSWT.getInstance().getMailView().getTableView().getTableList();

		return mails;
    }    
    
    protected static void exportAsEmailFile()
    {
    	EventList<StoredSmtpMessage> mails = getEmailSelection();
    	String path = getPath(false, false, true, SWT.SAVE);
    	
    	if (path == null)
    		return;
    	
		for (StoredSmtpMessage msg : mails)
		{
			PrintWriter out = null;
			
			try
            {
    			out = new PrintWriter(new FileWriter(path+File.separatorChar+
    					msg.getMessageId().substring(1,msg.getMessageId().length()-1)+".eml", false));
				out.write(msg.getMessage().toString());                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }        	
            finally
            {
            	if (out != null)
            		out.close();
            }
		}
    }
    
    protected static void exportAsMbox()
    {
    	EventList<StoredSmtpMessage> mails = getEmailSelection();
		String fileName = getPath(false, true, false, SWT.SAVE);
    	
    	if (fileName == null)
    		return;
    	
    	try
        {        		
            PrintWriter out = new PrintWriter(new FileWriter(fileName, false));
            for (StoredSmtpMessage msg : mails)
            	StreamUtilities.writeMessageToMBoxRDFormat(msg, out);
            
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static String getPath(boolean importMode, boolean mboxMode, 
    		boolean isDirectoryMode, int dialogMode)
    {
    	Dialog d =  null;
    	
    	if (isDirectoryMode)
    		d = new DirectoryDialog(MailsterSWT.getInstance().getShell(), dialogMode);
    	else
    		d = new FileDialog(MailsterSWT.getInstance().getShell(), dialogMode);

        if (importMode)
        	d.setText(Messages.getString("FilterTreeview.import.dialog.title")); //$NON-NLS-1$
        else
        	d.setText(Messages.getString("FilterTreeview.export.dialog.title")); //$NON-NLS-1$
                	
    	if (isDirectoryMode)
    		return ((DirectoryDialog) d).open();
    	else
    	{
    		FileDialog dialog = (FileDialog) d;
    		
	        if (mboxMode)
	        {
		        dialog.setFilterNames(new String[] {
		                Messages.getString("FilterTreeview.mbox.files.ext"), //$NON-NLS-1$
		                Messages.getString("MailView.all.files.ext") }); //$NON-NLS-1$
		        
		        dialog.setFilterExtensions(new String[] { "*.mbx", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
	        }
		    else
		    {
		        dialog.setFilterNames(new String[] {
		                Messages.getString("FilterTreeview.mail.files.ext"), //$NON-NLS-1$
		                Messages.getString("MailView.all.files.ext") }); //$NON-NLS-1$
	        	
		        dialog.setFilterExtensions(new String[] { "*.eml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		    }
	        
	        dialog.setFilterPath(MailsterSWT.getInstance().getSMTPService().getOutputDirectory());
	        
	        return dialog.open();
    	}
    }    
}
