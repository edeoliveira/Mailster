package org.mailster.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolItem;

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
 * DropDownListener.java - Generic handler for dropdown buttons.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class DropDownListener 
	extends SelectionAdapter
{
    protected ToolItem dropdown;

    protected Menu menu;

    protected DropDownListener()
    {    	
    }
    
    /**
     * Constructs a DropDownListener
     * 
     * @param dropdown the dropdown this listener belongs to
     */
    public DropDownListener(ToolItem dropdown)
    {
        this.dropdown = dropdown;
        clear();
    }

    /**
     * Drops all items of the current dropdown by creating a new menu. 
     */
    public final void clear()
    {
        menu = new Menu(dropdown.getParent().getShell());
        dropdown.setEnabled(false);
    }

    /**
     * Sets the enabled state of the dropdown.
     */
    public void setEnabled(boolean enabled)
    {
    	dropdown.setEnabled(enabled);
    }
    
    /**
     * Called when either the button itself or the dropdown arrow is clicked
     * 
     * @param event the event that triggered this call
     */
    public void widgetSelected(SelectionEvent event)
    {
        // If arrow is clicked then show the list
        if (event.detail == SWT.ARROW)
        {
            // Find where to show the dropdown list
            ToolItem item = (ToolItem) event.widget;
            Rectangle rect = item.getBounds();
            Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
            menu.setLocation(pt.x, pt.y + rect.height);
            menu.setVisible(true);
        }
        else
        {
            // Button has been pushed so take appropriate action
        	buttonPushed();
        }
    }
    
    /**
     * Method is called when the button was clicked.
     */
    public abstract void buttonPushed();
}
