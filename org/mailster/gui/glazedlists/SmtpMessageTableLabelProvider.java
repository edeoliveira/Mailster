package org.mailster.gui.glazedlists;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.glazedlists.swt.TableViewerManager.EventTableLabelProvider;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SmtpMessage;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

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
 * SmtpMessageTableLabelProvider.java - Decorates the table rows.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class SmtpMessageTableLabelProvider extends EventTableLabelProvider
	implements IFontProvider, IColorProvider 
{
    private static Image attachedFilesImage, flaggedImage, unflaggedImage;
    private static Color tableRowColor;

	/** 
     * the list being displayed in the TableViewer
     */
    private EventList sourceList;
    
    public SmtpMessageTableLabelProvider(EventList sourceList, TableFormat aTableFormat) 
    {
    	super(aTableFormat);
    	this.sourceList = sourceList;
        
        if (attachedFilesImage == null)
        {
        	attachedFilesImage = SWTHelper.loadImage("attach.gif"); //$NON-NLS-1$
        	flaggedImage = SWTHelper.loadImage("public_co.gif"); //$NON-NLS-1$
        	unflaggedImage = SWTHelper.loadGrayImage("public_co.gif"); //$NON-NLS-1$
            
        	tableRowColor = SWTHelper.createColor(243, 245, 248); 
        }
    }
   
	public String getColumnText(Object aElement, int aColumnIndex) 
	{
		if (aColumnIndex == SmtpMessageTableFormat.FLAG_COLUMN)
			return "";
		else
			return super.getColumnText(aElement, aColumnIndex);
	}
    
    public Image getColumnImage(Object obj, int column) 
    {
    	if (obj == null)
    		return null;
    	
    	StoredSmtpMessage stored = (StoredSmtpMessage) obj;
    	
    	if (stored == null)
    		return null;
    	
    	SmtpMessage msg = stored.getMessage();
    	
    	if (column == SmtpMessageTableFormat.ATTACHMENT_COLUMN 
                && msg.getInternalParts().getAttachedFiles().length > 0)
    		return attachedFilesImage;
    	else
		if (column == SmtpMessageTableFormat.FLAG_COLUMN)
		{
			if (stored.isChecked())
				return flaggedImage;
			else
				return unflaggedImage;
		}
    	
		return null;
    }
   
    public void dispose() 
    {
    }
    
    public Font getFont(Object element) 
    {
    	if (element != null)
    	{
            StoredSmtpMessage msg = (StoredSmtpMessage) element;
            if (!msg.isSeen())
                return SWTHelper.SYSTEM_FONT_BOLD;
        }
        
        return SWTHelper.SYSTEM_FONT;
    }

	public Color getBackground(Object element) 
	{
        if (element != null && sourceList.indexOf(element) % 2 == 1)
            return tableRowColor;
        
        return null;
	}

	public Color getForeground(Object element) 
	{
		return null;
	}  
}
