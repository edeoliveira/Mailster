package org.mailster.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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
 * MailUtilities.java - Various methods to help in date formatting and handling.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class DateUtilities 
{
    /**
     * Simple day & hour formatter.
     */
    public final static SimpleDateFormat df = 
    	new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); //$NON-NLS-1$
    
    /**
     * Simple GMT day & hour formatter.
     */
    public final static SimpleDateFormat gmt = 
    	new SimpleDateFormat("dd/MM/yyyy HH:mm:ss z"); //$NON-NLS-1$
    
    static {
    	gmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
	/**
     * Hour date formatter
     */
    public final static SimpleDateFormat hourDateFormat = 
    	new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$
    
    /**
     * RFC 822 compliant date formatter
     */
    public final static SimpleDateFormat rfc822DateFormatter = 
    	new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
    
    /**
     * ANSI C's asctime() formatter
     */
    private final static SimpleDateFormat ascTimeFormatter = 
    	new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);

    /**
     * Format date as 24 chars wide ANSI C's asctime()
     * 
     * @param d the date to format
     * @return the formatted date
     */
    public static String formatAsFixedWidthAsctime(Date d)
    {
        String s = ascTimeFormatter.format(d);

        // If day of Month value is 0..9 then output string will only be 23
        // chars wide so we pad it manually with a white space as specified by
        // RFC because SimpleDateFormat will pad it with zeroes and we have no
        // way to customize it ...
        if (s.length() == 23)
            return s.substring(0, 8) + " " + s.substring(8);

        return s;
    }
}
