package com.dumbster.smtp;

import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.mail.internet.InternetHeaders;

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
 * SmtpInternetHeaders.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class SmtpInternetHeaders extends InternetHeaders
        implements
            SmtpHeadersInterface
{
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        Enumeration e = (Enumeration) getAllHeaderLines();
        while (e.hasMoreElements())
        {
            sb.append((String) e.nextElement());
            if (e.hasMoreElements())
                sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * Get the value(s) associated with the given header name.
     * 
     * @param name header name
     * @return value(s) associated with the header name
     */
    public String[] getHeaderValues(String name)
    {
        String vals = super.getHeader(name, ";");
        StringTokenizer str = new StringTokenizer(vals, ";\n");
        String[] result = new String[str.countTokens()];

        for (int i = 0, max = result.length; i < max; i++)
            result[ i ] = str.nextToken().trim();

        return result;
    }

    /**
     * Get the first value associated with a given header name.
     * 
     * @param name header name
     * @return first value associated with the header name
     */
    public String getHeaderValue(String name)
    {
        String result = super.getHeader(name, null);
        if (result == null)
            return "";
        else
            return result;
    }
}
