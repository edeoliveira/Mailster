package org.mailster.gui.glazedlists;

import java.text.ParseException;
import java.util.Date;

import org.eclipse.swt.widgets.TableItem;
import org.mailster.gui.Messages;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SmtpMessage;
import org.mailster.util.DateUtilities;

import ca.odell.glazedlists.gui.WritableTableFormat;

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
 * SmtpMessageTableFormat.java - Defines columns order and values.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmtpMessageTableFormat
    implements ExtendedTableFormat<StoredSmtpMessage>, WritableTableFormat<StoredSmtpMessage>
{
    private String toHeader;
    private String subjectHeader;
    private String dateHeader;
    
    public final static int ATTACHMENT_COLUMN   = 0;
    public final static int TO_COLUMN           = 1;
    public final static int SUBJECT_COLUMN      = 2;
    public final static int FLAG_COLUMN         = 3;
    public final static int DATE_COLUMN         = 4;
    
    public SmtpMessageTableFormat()
    {
        init();
    }

    private void init()
    {
        toHeader = Messages.getString("MailView.column.to"); //$NON-NLS-1$
        subjectHeader = Messages.getString("MailView.column.subject"); //$NON-NLS-1$
        dateHeader = Messages.getString("MailView.column.date"); //$NON-NLS-1$
    }

    public int getColumnCount()
    {
        return 5;
    }

    public String getColumnName(int column)
    {
        if (column == ATTACHMENT_COLUMN)
            return ""; //$NON-NLS-1$
        else if (column == TO_COLUMN)
            return toHeader;
        else if (column == SUBJECT_COLUMN)
            return subjectHeader;
        else if (column == FLAG_COLUMN)
            return ""; //$NON-NLS-1$
        else if (column == DATE_COLUMN)
            return dateHeader;

        throw new IllegalStateException();
    }

    public Object getColumnValue(StoredSmtpMessage stored, int column)
    {
    	if (stored == null)
    		return "";
    	
    	SmtpMessage msg = stored.getMessage();
    	
        if (column == ATTACHMENT_COLUMN)
            return ""; //$NON-NLS-1$
        else if (column == TO_COLUMN)
            return msg.getTo();
        else if (column == SUBJECT_COLUMN)
            return msg.getSubject();
        else if (column == FLAG_COLUMN)
            return stored.isChecked();
        else if (column == DATE_COLUMN)
        {
			try 
			{
				Date d = DateUtilities.rfc822DateFormatter.parse(stored.getMessage().getDate());
				
	            if ((int) (d.getTime() / 8.64E7) == (int) (((new Date())
	                    .getTime()) / 8.64E7))
	                // same day
	            	return DateUtilities.hourDateFormat.format(d);
	            	
	            return DateUtilities.df.format(d);
			} 
			catch (ParseException e) 
			{
				return "-";
			}
        }

        throw new IllegalStateException();
    }

    public void setupItem(final TableItem item, StoredSmtpMessage msg, final int realIndex)
    {
        item.setData(msg);        
    }

	public boolean isEditable(StoredSmtpMessage msg, int column) 
	{
		return column == FLAG_COLUMN;
	}

	public StoredSmtpMessage setColumnValue(StoredSmtpMessage msg, Object obj, int column) 
	{
		if (column == FLAG_COLUMN)
			msg.setChecked(((Boolean) obj).booleanValue());
		
		return msg;
	}
}
