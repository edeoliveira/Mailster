package org.mailster.gui.views.mailbox;

import java.util.Comparator;
import java.util.Date;

import org.eclipse.swt.graphics.Image;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

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
 * MailBoxTableViewFormat.java - Defines columns order and values.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.8 $, $Date: 2009/03/21 01:15:15 $
 */
public class MailBoxTableViewFormat
    implements AdvancedTableFormat<StoredSmtpMessage>
{
	private static final Image ATTACHED_FILES_IMAGE = SWTHelper.loadImage("attach.gif"); //$NON-NLS-1$
	private static final Image FLAGGED_IMAGE = SWTHelper.loadImage("flag16.png"); //$NON-NLS-1$
	private static final Image UNFLAGGED_IMAGE = SWTHelper.loadImage("no_flag.gif"); //$NON-NLS-1$
	
    private String toHeader;
    private String subjectHeader;
    private String dateHeader;

	private static Comparator<String> stringComparator = new Comparator<String>() {
        public int compare(String s0, String s1)
        {
        	return s0.compareTo(s1);
        }
    };

    private static Comparator<Date> dateComparator = new Comparator<Date>() {
        public int compare(Date d0, Date d1)
        {
        	return d0.compareTo(d1);
        }
    };
    
    public final static int ATTACHMENT_COLUMN   = 0;
    public final static int TO_COLUMN           = 1;
    public final static int SUBJECT_COLUMN      = 2;
    public final static int FLAG_COLUMN         = 3;
    public final static int DATE_COLUMN         = 4;
    
    public MailBoxTableViewFormat()
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
        if (column == ATTACHMENT_COLUMN)
            return ""; //$NON-NLS-1$
        else if (column == TO_COLUMN)
            return stored.getMessageTo();
        else if (column == SUBJECT_COLUMN)
            return stored.getMessageSubject();
        else if (column == FLAG_COLUMN)
            return stored.isChecked();
        else if (column == DATE_COLUMN)
        	return stored.getMessageDate();

        throw new IllegalStateException();
    }

    public Image getColumnImage(Object element, int columnIndex)
	{
		if (element == null)
			return null;

		StoredSmtpMessage stored = (StoredSmtpMessage) element;
		if (columnIndex == ATTACHMENT_COLUMN && stored.getAttachedFilesCount() > 0)
			return ATTACHED_FILES_IMAGE;
		else if (columnIndex == FLAG_COLUMN)
		{
			if (stored.isChecked())
				return FLAGGED_IMAGE;
			else
				return UNFLAGGED_IMAGE;
		}

		return null;
	}
    
	@SuppressWarnings("unchecked")
	public Class getColumnClass(int column) 
	{
		return String.class;
	}

	@SuppressWarnings("unchecked")
	public Comparator getColumnComparator(int column) 
	{
		if (column == DATE_COLUMN)
			return dateComparator;
		else
		if (column == TO_COLUMN || column == SUBJECT_COLUMN)
			return stringComparator;
		
		return null;
	}
}