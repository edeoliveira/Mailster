package org.mailster.util;

import java.util.StringTokenizer;

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
 * StringUtilities.java - Various methods to handle strings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class StringUtilities
{
    /** 
     * Constant for the empty <code>String</code>
     */
    public final static String EMPTY_STRING = "";
    
    /**
     * Tests if the given <code>String</code> is empty. A <code>String</code>
     * object is considered empty if it is either <code>null</code> or its
     * trimmed length is zero.
     * 
     * @param str the <code>String</code> to test
     * @return <code>true</code> if the given <code>String</code> is empty;
     * <code>false</code> otherwise
     */
    public static boolean isEmpty(String str) 
    {
        return (str == null || str.trim().length() == 0);
    }
    
    /**
     * Split a <code>String</code> into an array of <code>String[]</code>
     * using the space character as token.
     *  
     * @param s the string to split
     * @return the splitted string array
     */
    public static String[] split(String s)
    {
        return split(s, " ");   
    }
    
    /**
     * Split a <code>String</code> into an array of <code>String[]</code>.
     *  
     * @param s the string to split
     * @param token the character token
     * @return the splitted string array
     */
    public static String[] split(String s, String token)
    {
        if (s == null)
            return null;
        
        StringTokenizer tokens = new StringTokenizer(s, token);
        String[] values = new String[tokens.countTokens()];
         
        for(int i=0; i<values.length; i++) 
            values[i] = tokens.nextToken();
        
        return values;
    }
}
