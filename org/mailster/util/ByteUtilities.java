package org.mailster.util;

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
 * MailUtilities.java - Various methods to help manipulate bytes.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class ByteUtilities 
{   
    /**
     * Returns the integer represented by 4 bytes in network byte order.
     */
    public static int networkByteOrderToInt(byte[] buf, int start, int count) 
    {
    	if (count > 4)
    		throw new IllegalArgumentException("Cannot handle more than 4 bytes");

    	int result = 0;

        for (int i = 0; i < count; i++) 
        {
        	result <<= 8;
        	result |= ((int)buf[start+i] & 0xff);
        }
        
        return result;
    }

    /**
     * Encodes an integer into 4 bytes in network byte order in the buffer
     * supplied.
     */
    public static void intToNetworkByteOrder(int num, byte[] buf, int start, int count) 
    {
    	if (count > 4)
    		throw new IllegalArgumentException("Cannot handle more than 4 bytes");
    
    	for (int i = count-1; i >= 0; i--) 
    	{
    		buf[start+i] = (byte)(num & 0xff);
    		num >>>= 8;
    	}
    }
}
