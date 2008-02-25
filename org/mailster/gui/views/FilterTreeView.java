package org.mailster.gui.views;

import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.AbstractEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * ---<br>
 * Mailster (C) 2007 De Oliveira Edouard
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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
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
        			clearQueueToolItem.setEnabled(!view.getEventList().isEmpty());
        		}
		});
    }
}
