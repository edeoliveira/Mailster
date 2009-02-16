package org.mailster.gui.views;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.DropDownListener;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.dialogs.ErrorDialog;
import org.mailster.message.SmtpMessage;
import org.mailster.message.SmtpMessageFactory;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.server.MailsterConstants;
import org.mailster.server.MailsterSmtpService;
import org.mailster.util.StreamUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.AbstractEventList;
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
 * FilterTreeView.java - A tree that filters mails by destination host.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class FilterTreeView extends TreeView
{
    private final static Logger log = LoggerFactory.getLogger(FilterTreeView.class);
    
    private TreeItem deletedMailsTreeItem;
    private TreeItem checkedMailsTreeItem;

    private ToolItem clearQueueToolItem;
    
    private static String deletedTreeItemLabel = 
    	Messages.getString("MailsterSWT.treeView.trash.label"); //$NON-NLS-1$

    private static String checkedTreeItemLabel = 
    	Messages.getString("MailsterSWT.treeView.flaggedMail.label"); //$NON-NLS-1$

    private MenuItem exportAsMailItem;
    private MenuItem exportAsMailBoxItem; 
    
    private HostMatcherEditor editor;
    
    /**
     * The count of messages last time a event occured. Prevents from multiple
     * unnecessary updates.
     */
	private long lastCallCount=0;
    
    private class HostMatcherEditor extends AbstractMatcherEditor<StoredSmtpMessage>
        implements SelectionListener
    {
        private class HostMatcher implements Matcher<StoredSmtpMessage> 
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
            	else
                if (selectedItem == deletedMailsTreeItem)
                    return msg.getFlags().contains(Flags.Flag.FLAGGED);
                else
                if (selectedItem == checkedMailsTreeItem)
                    return msg.isChecked();
                else
                    return !msg.getFlags().contains(Flags.Flag.FLAGGED)
				                && ((String)selectedItem.getData()).
				                	equals(getEmailHost(msg.getMessageTo()));
            }
        }
        
        private Tree mailBoxTree;
        private HostMatcher matcher = new HostMatcher();
    
        public HostMatcherEditor(Tree mailBoxTree) 
        {
            this.mailBoxTree = mailBoxTree;
            mailBoxTree.addSelectionListener(this);            
        }
    
        public void filter() 
        {
            final TreeItem[] selected = mailBoxTree.getSelection();
            if (selected == null || selected.length == 0)
            {
            	this.mailBoxTree.setSelection(root);
            	matcher.setSelectedItem(root);
            }
            else
            	matcher.setSelectedItem(selected[0]);
            
            this.fireChanged(matcher);
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
    
    private static class FilterGroupFunction 
        implements FunctionList.Function<StoredSmtpMessage, String> 
    {
    	public FilterGroupFunction()
    	{
    	}
    	
        public String evaluate(StoredSmtpMessage msg) 
        {
            // We only count unread messages
            if (msg.isSeen())
                return "";
            
            if (msg.getFlags().contains(Flags.Flag.FLAGGED))
                return deletedTreeItemLabel;
            else
                return getEmailHost(msg.getMessageTo());
        }
    }
    
    /**
     * This class provides the "drop down" functionality for the import/export
     * button.
     */
    class ImportExportDropDownMenu extends DropDownListener
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
        	Image importImage = SWTHelper.decorateImage(SWTHelper.loadImage("folder.gif"), SWTHelper.loadImage("incoming.gif"), SWT.BOTTOM | SWT.LEFT);
        	Image exportImage = SWTHelper.decorateImage(SWTHelper.loadImage("folder_closed.gif"), SWTHelper.loadImage("outgoing.gif"), SWT.BOTTOM | SWT.LEFT);
        	
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
			{
				String fileName = getPath(true, false, false, SWT.OPEN);
				importFromEmailFile(fileName);
			}
			else
			if (item == importAsMailBoxItem)
			{
				String fileName = getPath(true, true, false, SWT.OPEN);
				importFromMbox(fileName);
			}
			else
			if (item == exportAsMailItem)
			{
				String fileName = getPath(false, false, true,SWT.SAVE);
				exportAsEmailFile(fileName);
			}
			else
			if (item == exportAsMailBoxItem)
			{
				String fileName = getPath(false, true, false, SWT.SAVE);
				exportAsMbox(fileName);
			}        	
        }
        
        private EventList<StoredSmtpMessage> getEmailSelection()
        {
    		EventList<StoredSmtpMessage> mails = 
    			MailsterSWT.getInstance().getMailView().getTableView().getSelection();
    		
    		if (mails.size() == 0)
    			mails = MailsterSWT.getInstance().getMailView().getTableView().getTableList();

    		return mails;
        }

        private void importFromEmailFile(String fileName)
        {
        	if (fileName == null)
        		return;
        	
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
            			"Exception occured", "Failed importing email file : "+fileName, new Status(IStatus.ERROR, "Mailster", "Unable to import file", e), IStatus.ERROR);
            	dlg.open();
            }
        }
        
        private void importFromMbox(String fileName)
        {
        	if (fileName == null)
        		return;
        	
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
        
        private void exportAsEmailFile(String path)
        {
        	EventList<StoredSmtpMessage> mails = getEmailSelection();
        	
        	if (path == null || mails == null || mails.size() == 0)
        		return;
        	
    		for (StoredSmtpMessage msg : mails)
    		{
    			PrintWriter out = null;
    			
    			try
                {
    				out = new PrintWriter(new FileWriter(path+
    						msg.getMessageId().substring(1,msg.getMessageId().length()-2)+".eml", false));
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
        
        private void exportAsMbox(String fileName)
        {
        	EventList<StoredSmtpMessage> mails = getEmailSelection();
        	
        	if (fileName == null || mails == null || mails.size() == 0)
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
        
        private String getPath(boolean importMode, boolean mboxMode, boolean isDirectoryMode, int dialogMode)
        {
        	Dialog d =  null;
        	
        	if (isDirectoryMode)
        		d = new DirectoryDialog(dropdown.getParent().getShell(), dialogMode);
        	else
        		d = new FileDialog(dropdown.getParent().getShell(), dialogMode);

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

        public void buttonPushed()
        {
        	doMenuAction(menu.getDefaultItem());
        }
    }
    
	private Map<String, List<StoredSmtpMessage>> messageTreeMap;
	
	private FilterList<StoredSmtpMessage> checkedList;    
    
    public FilterTreeView(Composite parent, boolean enableToolbar)
    {
    	super(parent, enableToolbar);
    	
        collapseAllItem.setEnabled(true);
        expandAllItem.setEnabled(true);
        
        root = new TreeItem(tree, SWT.NONE);
        root.setImage(SWTHelper.loadImage("forum16.png")); //$NON-NLS-1$
        root.setText(Messages.getString("MailsterSWT.treeView.root.label")); //$NON-NLS-1$
        
        checkedMailsTreeItem = new TreeItem(root, SWT.NONE);
        checkedMailsTreeItem.setImage(SWTHelper.loadImage("flag16.png")); //$NON-NLS-1$
        checkedMailsTreeItem.setText(checkedTreeItemLabel);
        checkedMailsTreeItem.setForeground(SWTHelper.createColor(12, 97, 232));
        checkedMailsTreeItem.setData(checkedTreeItemLabel);
        
        deletedMailsTreeItem = new TreeItem(root, SWT.NONE);
        deletedMailsTreeItem.setImage(SWTHelper.loadImage("clearArchive16.png")); //$NON-NLS-1$
        deletedMailsTreeItem.setText(deletedTreeItemLabel);
        deletedMailsTreeItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        deletedMailsTreeItem.setData(deletedTreeItemLabel);
        
        root.setExpanded(true);
        tree.setSelection(root);
    }
    
    protected  void customizeToolbar(ToolBar toolBar)
    {
        final ToolItem refreshToolItem = new ToolItem(toolBar, SWT.PUSH);
        refreshToolItem.setImage(SWTHelper.loadImage("refresh.gif")); //$NON-NLS-1$
        refreshToolItem.setToolTipText(Messages
                .getString("MailsterSWT.refreshQueue.tooltip")); //$NON-NLS-1$   
        
        clearQueueToolItem = new ToolItem(toolBar, SWT.PUSH);
        clearQueueToolItem.setImage(SWTHelper.loadImage("closeall.gif")); //$NON-NLS-1$
        clearQueueToolItem.setToolTipText(Messages
                .getString("MailsterSWT.clearQueue.tooltip")); //$NON-NLS-1$
        clearQueueToolItem.setEnabled(false);
        
        final ToolItem importExportToolItem = 
        	SWTHelper.createToolItem(toolBar,
					                SWT.FLAT | SWT.DROP_DOWN,
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
    
    /**
     * Retrieves the host of the first email in the TO header.
     */
    public static String getEmailHost(String email)
    {
    	if (email == null)
    		return Messages.getString("MailsterSWT.treeView.localNetwork.label");
    	
    	email = email.toLowerCase();
    	if (email.indexOf(',')>=0)
    		email = email.substring(0, email.indexOf(','));
    	
    	int pos = email.lastIndexOf("@"); //$NON-NLS-1$
    	if (pos>=0)
    	{
            pos++;
    		int end=email.indexOf(">"); //$NON-NLS-1$
    		return end < 0 ? email.substring(pos) : email.substring(pos, end);
    	}
    	else 
    		return Messages.getString("MailsterSWT.treeView.localNetwork.label");
    }
	
    public void updateMessagesCounts(AbstractEventList<StoredSmtpMessage> eventList)
    {
    	lastCallCount = eventList.size();
    	log.debug("Call to updateMessagesCounts()");
        
    	String filterHost;
    	List<StoredSmtpMessage> l;
    	
        for (TreeItem child : root.getItems())
        {
        	if (child == checkedMailsTreeItem)
        	{
        		filterHost = checkedTreeItemLabel;
        		l = checkedList;
        	}
        	else
        	{
	        	filterHost = (String)child.getData();
	        	l = messageTreeMap == null ? null : messageTreeMap.get(filterHost);	        	
        	}
        	
        	long count = l == null ? 0 : l.size();
        	StringBuilder countLabel = new StringBuilder(filterHost);
	        if (count > 0)
	        {
	        	countLabel.append(" (").append(count);
		        if (child == checkedMailsTreeItem)
		        	countLabel.append('/').append(lastCallCount);
		        countLabel.append(')');
	        }
	        
	        child.setText(countLabel.toString());
        }
    }
	
	public void addMessageCounter(AbstractEventList<StoredSmtpMessage> eventList,
			final AbstractEventList<StoredSmtpMessage> baseList)
	{
		eventList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
            public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
            {
            	baseList.getReadWriteLock().readLock().lock();
				try
				{
					if (!listChanges.isReordering() && lastCallCount != baseList.size())
						updateMessagesCounts(baseList);
				}
				finally
				{
					baseList.getReadWriteLock().readLock().unlock();
				}					
            }
        });
		
		baseList.getReadWriteLock().writeLock().lock();
        try
        {
        	messageTreeMap = GlazedLists.syncEventListToMultiMap(baseList, new FilterGroupFunction());
        	
        	checkedList = new FilterList<StoredSmtpMessage>(baseList, new Matcher<StoredSmtpMessage>() {
    			public boolean matches(StoredSmtpMessage stored) 
    			{
   					return stored.isChecked();
    			}
    		});
        }
        finally
        {
        	baseList.getReadWriteLock().writeLock().unlock();
        }        	
	}
	
	private void addNodeIfNewHost(String host)
	{
		boolean found = false;
        for (TreeItem child : root.getItems())
        {
        	if (((String)child.getData()).equals(host))
        	{
        		found = true;
            	break;
            }
        }
        
        if (!found)
        {                        
            try
            {
                TreeItem item = new TreeItem(root, SWT.NONE, 0);
                item.setImage(SWTHelper.loadImage("folder.gif")); //$NON-NLS-1$
                item.setText(host);
                item.setData(host);
            }
            catch (Exception e)
            {
                e.printStackTrace();                        
            }
        }
	}
	
    public FilterList<StoredSmtpMessage> getFilterList(
    		final AbstractEventList<StoredSmtpMessage> eventList)
    {
        eventList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
            public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
            {
                while (listChanges.next()) 
                {
                    if (listChanges.getType() != ListEvent.INSERT)
                        continue;
                    
                    String host = getEmailHost(eventList.get(listChanges.getIndex()).getMessageTo());
                   	addNodeIfNewHost(host);
                }
                
                root.setExpanded(true);
            }
        });
        
        eventList.getReadWriteLock().readLock().lock();
        try
        {
        	editor = new HostMatcherEditor(tree);
        	return new FilterList<StoredSmtpMessage>(eventList, editor);
        }
        finally
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
        return sel != null && sel.length>0 && sel[0] == deletedMailsTreeItem;
    }
    
    protected void installListeners(final TableView view)
    {
        view.getEventList().addListEventListener(new ListEventListener<StoredSmtpMessage>() {
        		public void listChanged(ListEvent<StoredSmtpMessage> evt) 
        		{
        			boolean notEmpty = !view.getEventList().isEmpty();
        			
        			clearQueueToolItem.setEnabled(notEmpty);
        			exportAsMailItem.setEnabled(notEmpty);
        			exportAsMailBoxItem.setEnabled(notEmpty);
        		}
		});
    }
}
