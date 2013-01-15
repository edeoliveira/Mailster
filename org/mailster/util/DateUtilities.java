package org.mailster.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
 * MailUtilities.java - Various methods to help in date formatting and handling.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.6 $, $Date: 2008/12/06 13:57:17 $
 */
public class DateUtilities 
{
	public enum DateFormatterEnum
	{
		ADF(ADF_FORMATTER), 
		DF(DF_FORMATTER), 
		GMT(GMT_FORMATTER), 
		HOUR(HOUR_FORMATTER), 
		RFC822(RFC822_FORMATTER), 
		ASCTIME(ASCTIME_FORMATTER);
		
		private SimpleDateFormat sdf;
		
		private DateFormatterEnum(SimpleDateFormat sdf)
		{
			this.sdf = sdf;
		}
		
		protected SimpleDateFormat getFormatter()
		{
			return sdf;
		}
	}
	
	/**
     * Advanced day & hour formatter.
     */
	private static final SimpleDateFormat ADF_FORMATTER = 
    	new SimpleDateFormat("EEE dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    
    /**
     * Simple day & hour formatter.
     */
	private static final SimpleDateFormat DF_FORMATTER = 
    	new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    
    /**
     * Simple GMT day & hour formatter.
     */
    private static final SimpleDateFormat GMT_FORMATTER = 
    	new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z"); //$NON-NLS-1$
    
	/**
     * Hour date formatter
     */
    private static final SimpleDateFormat HOUR_FORMATTER = 
    	new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
    
    /**
     * RFC 822 compliant date formatter
     */
    private static final SimpleDateFormat RFC822_FORMATTER = 
    	new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
    
    /**
     * ANSI C's asctime() formatter
     */
    private static final SimpleDateFormat ASCTIME_FORMATTER = 
    	new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
    
    static {
    	GMT_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    public static String format(DateFormatterEnum en, Date d)
    {
    	if (en == null)
    		throw new IllegalArgumentException("DateFormatterEnum argument can't be null");
    	
    	SimpleDateFormat sdf = en.getFormatter(); 
    	
    	synchronized(sdf)
    	{
    		return sdf.format(d);
    	}
    }
    
    public static Date parse(DateFormatterEnum en, String s) 
    	throws ParseException
    {
    	if (en == null)
    		throw new IllegalArgumentException("DateFormatterEnum argument can't be null");
    	
    	SimpleDateFormat sdf = en.getFormatter(); 
    	
    	synchronized(sdf)
    	{
    		return sdf.parse(s);
    	}
    }    

    public static String unsafeFormat(DateFormatterEnum en, Date d)
    {
    	if (en == null)
    		throw new IllegalArgumentException("DateFormatterEnum argument can't be null");
    	    	
    	return en.getFormatter().format(d);
    }
    
    public static Date unsafeParse(DateFormatterEnum en, String s) 
    	throws ParseException
    {
    	if (en == null)
    		throw new IllegalArgumentException("DateFormatterEnum argument can't be null");
    	
    	return en.getFormatter().parse(s);
    }

    /**
     * Format date as 24 chars wide ANSI C's asctime().
     * 
     * @param d the date to format
     * @return the formatted date
     */
    public static String formatAsFixedWidthAsctime(Date d)
    {
        String s = null;
        
        synchronized (ASCTIME_FORMATTER)
		{
        	s = ASCTIME_FORMATTER.format(d);	
		}

        // If day of Month value is 0..9 then output string will only be 23
        // chars wide so we pad it manually with a white space as specified by
        // RFC because SimpleDateFormat will pad it with zeroes and we have no
        // way to customize it ...
        if (s.length() == 23)
            return s.substring(0, 8) + " " + s.substring(8);

        return s;
    }
    
    /**
     * Return true if <code>d</code> represents a time value on the current
     * day.
     *  
     * @param d the date to compare with today
     * @return boolean true if <code>d</code> is bounded in the current day
     */
    public static boolean isCurrentDay(Date d)
    {
    	int offset = TimeZone.getDefault().getRawOffset();
    	long current = System.currentTimeMillis() + offset;
    	long compared = d.getTime() + offset;
    	
    	return ((int) (compared / 8.64E7)) == ((int) (current / 8.64E7));
    }
    
    /**
     * Return true if <code>d</code> represents a time value on the current
     * year.
     *  
     * @param d the date to compare with today
     * @return boolean true if <code>d</code> is bounded in the current year
     */
    public static boolean isCurrentYear(Date d)
    {
    	Calendar cal = Calendar.getInstance();
    	int y = cal.get(Calendar.YEAR);
    	cal.setTime(d);
    	
    	return cal.get(Calendar.YEAR) == y;
    }    
}
