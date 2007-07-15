package org.mailster.gui.views;

import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
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
public class FilterTreeView
{
    private final static Logger log = LoggerFactory.getLogger(FilterTreeView.class);
    
    private TreeItem deletedMailsTreeItem;
    private TreeItem root;    
    private Tree tree;
    
    private HostMatcherEditor editor;
    
    private static String deletedTreeItemLabel = 
    	Messages.getString("MailsterSWT.treeView.trash.label"); //$NON-NLS-1$
    
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
            private final TreeItem selectedItem;
    
            public HostMatcher(TreeItem selectedItem) 
            {
                this.selectedItem = selectedItem;
            }
    
            public boolean matches(StoredSmtpMessage msg) 
            {
                if (selectedItem == deletedMailsTreeItem)
                    return msg.getFlags().contains(Flags.Flag.FLAGGED);
                else
                    return !msg.getFlags().contains(Flags.Flag.FLAGGED) &&
				                ((String)selectedItem.getData()).
				                	equals(getEmailHost(msg.getMessage().getTo()));
            }
        }
        
        private Tree mailBoxTree;
    
        public HostMatcherEditor(Tree mailBoxTree) 
        {
            this.mailBoxTree = mailBoxTree;
            mailBoxTree.addSelectionListener(this);            
        }
    
        public void filter() 
        {
            final TreeItem[] selected = mailBoxTree.getSelection();
            if (selected == null || selected.length == 0 || selected[0] == root)
                this.fireMatchAll();
            else
                this.fireChanged(new HostMatcher(selected[0]));
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
        public String evaluate(StoredSmtpMessage msg) 
        {
            // We only count unread messages
            if (msg.isSeen())
                return "";
            
            if (msg.getFlags().contains(Flags.Flag.FLAGGED))
                return deletedTreeItemLabel;
            else
                return getEmailHost(msg.getMessage().getTo());
        }
    }
    
    public FilterTreeView(Composite parent)
    {
        tree = new Tree(parent, SWT.FLAT | SWT.BORDER);
        tree.setLinesVisible(false);
        
        root = new TreeItem(tree, SWT.NONE);
        root.setImage(SWTHelper.loadImage("forum16.png")); //$NON-NLS-1$
        root.setText(Messages.getString("MailsterSWT.treeView.root.label")); //$NON-NLS-1$
        
        deletedMailsTreeItem = new TreeItem(root, SWT.NONE);
        deletedMailsTreeItem.setImage(SWTHelper.loadImage("clearArchive16.png")); //$NON-NLS-1$
        deletedMailsTreeItem.setText(deletedTreeItemLabel);
        deletedMailsTreeItem.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        deletedMailsTreeItem.setData(deletedTreeItemLabel);
        root.setExpanded(true);
    }
    
    public static String getEmailHost(String email)
    {
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
    	Map<Comparable<String>, List<StoredSmtpMessage>> messageMap = 
        	GlazedLists.syncEventListToMultiMap(eventList, new FilterGroupFunction());
        
        for (TreeItem child : root.getItems())
        {
        	String filterHost = (String)child.getData();
        	List<StoredSmtpMessage> l = messageMap.get(filterHost);
        	long count = l == null ? 0 : l.size();
        	
        	String countLabel = count == 0 ? "" : " ("+count+")";
        	child.setText(filterHost+countLabel);
        }
    }
	
	public void addMessageCounter(AbstractEventList<StoredSmtpMessage> eventList,
			final AbstractEventList<StoredSmtpMessage> baseList)
	{
		eventList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
            public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
            {
            	synchronized(baseList)
            	{
            		if (!listChanges.isReordering() && lastCallCount != baseList.size())
            			updateMessagesCounts(baseList);
            	}
            }
        });
	}
	
    public FilterList<StoredSmtpMessage> getFilterList(
    		final AbstractEventList<StoredSmtpMessage> eventList, Table table)
    {
        eventList.addListEventListener(new ListEventListener<StoredSmtpMessage>() {
            public void listChanged(ListEvent<StoredSmtpMessage> listChanges)
            {
                while (listChanges.next()) 
                {
                    if (listChanges.getType() != ListEvent.INSERT)
                        continue;
                    
                    String host = getEmailHost(eventList.get(listChanges.getIndex()).getMessage().getTo());
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
                            TreeItem item = new TreeItem(root, 0, SWT.NONE);
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
                
                root.setExpanded(true);
            }
        });
        
        editor = new HostMatcherEditor(tree);
        return new FilterList<StoredSmtpMessage>(eventList, editor);
    }
    
    public void setLayoutData(Object layoutData) 
    {
        tree.setLayoutData(layoutData);
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
}
