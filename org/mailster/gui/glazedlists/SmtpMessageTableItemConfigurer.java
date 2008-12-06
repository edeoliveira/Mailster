package org.mailster.gui.glazedlists;

import java.util.Date;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;
import org.mailster.gui.SWTHelper;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.util.DateUtilities;

import ca.odell.glazedlists.swt.TableItemConfigurer;

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
 * SmtpMessageTableItemConfigurer.java - Decorates the table items.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmtpMessageTableItemConfigurer
	implements TableItemConfigurer<StoredSmtpMessage> 
{
    private static Image attachedFilesImage;
    private static Image flaggedImage;
    private static Image unflaggedImage;
    
    public SmtpMessageTableItemConfigurer() 
    {    	
        if (attachedFilesImage == null)
        {
        	attachedFilesImage = SWTHelper.loadImage("attach.gif"); //$NON-NLS-1$
        	flaggedImage = SWTHelper.loadImage("flag16.png"); //$NON-NLS-1$
        	unflaggedImage = SWTHelper.loadImage("no_flag.gif"); //$NON-NLS-1$
        }
    }
   
	public void configure(TableItem item, StoredSmtpMessage rowValue, Object columnValue, int row, int column) 
	{
		if (column == SmtpMessageTableFormat.FLAG_COLUMN || 
				column == SmtpMessageTableFormat.ATTACHMENT_COLUMN)
			item.setImage(column, getColumnImage(rowValue, column));

		if (column == SmtpMessageTableFormat.DATE_COLUMN)
		{
			Date d = (Date) columnValue;
			
            if (DateUtilities.isCurrentDay(d))
            	item.setText(column, DateUtilities.hourDateFormat.format(d));
            else
            	item.setText(column, DateUtilities.df.format(d));
		}
		else
			item.setText(column, 
					columnValue == null || !(columnValue instanceof String)? "" : (String) columnValue);
		
		item.setFont(getFont(rowValue));
		/*if (rowValue.isPassivated())
			item.setForeground(item.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA));*/
				
		item.setData(rowValue);
	}
	
    public Image getColumnImage(StoredSmtpMessage stored, int column) 
    {
    	if (stored == null)
    		return null;
    	
    	if (column == SmtpMessageTableFormat.ATTACHMENT_COLUMN 
                && stored.getAttachedFilesCount() > 0)
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
    
    public Font getFont(StoredSmtpMessage stored) 
    {
    	if (stored != null && !stored.isSeen())
                return SWTHelper.SYSTEM_FONT_BOLD;
        
        return SWTHelper.SYSTEM_FONT;
    }
}
