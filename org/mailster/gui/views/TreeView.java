package org.mailster.gui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.LayoutUtils;

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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * TreeView.java - A class that abstracts a generic tree view.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class TreeView 
{
	protected TreeItem root;    
	protected Tree tree;
	
	protected ToolItem navigateUpItem;
	protected ToolItem collapseAllItem;
	protected ToolItem expandAllItem;
    
	protected TreeView()
	{		
	}
	
    public TreeView(Composite parent, boolean enableToolbar)
    {
    	createTreeToolBar(parent, enableToolbar);        
    }
    
    public void setLayoutData(Object layoutData) 
    {
        tree.setLayoutData(layoutData);
    }    
    
    protected abstract void customizeToolbar(ToolBar toolBar);
    
    protected void createTreeToolBar(Composite parent, boolean enableToolbar)
    {
    	ToolBar treeToolbar = null;
    	
    	if (enableToolbar)
    		treeToolbar = new ToolBar(parent, SWT.FILL | SWT.FLAT | SWT.RIGHT);
        
        tree = new Tree(parent, SWT.FLAT);
        tree.setLinesVisible(false);
        
        tree.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event evt) 
			{
				GC gc = evt.gc;
				Color old = gc.getForeground();
				gc.setForeground(tree.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
				gc.setLineWidth(tree.getBorderWidth());
				gc.drawLine(0, 0, tree.getSize().y, 0);
				gc.setForeground(old);
			}
		});
        
        if (!enableToolbar)
        	return;
        
        customizeToolbar(treeToolbar);
        
        navigateUpItem = SWTHelper.createToolItem(treeToolbar, SWT.FLAT | SWT.PUSH, "",
                Messages.getString("MailView.navigateUp.tooltip"), "up_nav.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
        
        new ToolItem(treeToolbar, SWT.SEPARATOR);
        
        collapseAllItem = SWTHelper.createToolItem(treeToolbar, SWT.FLAT | SWT.PUSH, "",
                Messages.getString("MailView.collapseAll.tooltip"), "collapseall.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
        expandAllItem = SWTHelper.createToolItem(treeToolbar, SWT.FLAT | SWT.PUSH, "",
                Messages.getString("MailView.expandAll.tooltip"), "expandall.gif", true); //$NON-NLS-1$ //$NON-NLS-2$
        treeToolbar.setLayoutData(LayoutUtils.createGridData(
                GridData.END, GridData.BEGINNING, true, false, 1, 1));
        
        navigateUpItem.setEnabled(false);
        collapseAllItem.setEnabled(false);
        expandAllItem.setEnabled(false);
        
        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
            	if (e.widget == navigateUpItem)
	            {
            		TreeItem parentItem = tree.getSelection()[0].getParentItem();
            		if (parentItem != null)
            			tree.setSelection(parentItem);
            	}
            	
                if (e.widget == collapseAllItem)
                	SWTHelper.collapseAll(tree);
                else if (e.widget == expandAllItem)
                	SWTHelper.expandAll(tree);

            	boolean treeHasASelectedNode = tree.getSelection() != null 
        		&& tree.getSelection().length == 1 && tree.getSelection()[0] != null;
        	
	        	boolean hasNodes = tree.getItems() != null && tree.getItems().length > 0;
	        	
	        	navigateUpItem.setEnabled(treeHasASelectedNode && tree.getSelection()[0] != root);
	            collapseAllItem.setEnabled(hasNodes);
	            expandAllItem.setEnabled(hasNodes);                
            }
        };
        
        navigateUpItem.addSelectionListener(selectionAdapter);
        collapseAllItem.addSelectionListener(selectionAdapter);
        expandAllItem.addSelectionListener(selectionAdapter);
        tree.addSelectionListener(selectionAdapter);
    }
}
