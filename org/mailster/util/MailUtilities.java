package org.mailster.util;

import java.io.IOException;

import javax.mail.internet.MimeUtility;

import com.dumbster.smtp.SmtpHeaders;
import com.dumbster.smtp.SmtpMessage;

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
 * See&nbsp; <a href="http://mailster.sourceforge.org" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * MailUtilities.java - Enter your comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class MailUtilities
{
    public static String decodeText(String s)
    {
	try
	{
	    if (s != null)
	    {
		s = s.trim();
		if (s.startsWith("=?") && s.endsWith("?="))
			return MimeUtility.decodeText(s);
	    }
	}
	catch (Exception ex) {ex.printStackTrace();}
	catch (NoClassDefFoundError nex) {nex.printStackTrace();}

	return s;
    }
    
    private static int getLineLevel(String line)
    {
		int level = 0;
		if (line != null)
		{
		    while (level < line.length() && line.charAt(level) == '>')
			level++;
		}
	
		return level;
    }

    private static String getFlowedMarkerHMTLColor(int level)
    {
		String r = Integer.toHexString((80+level*32) % 256);	
		String v = Integer.toHexString(Math.max(Math.abs(256-level*35) % 256, 40));
		String b = Integer.toHexString((50+level*64) % 256);
		 
		return "#"+r+v+b;
    }
    
    public static String formatFlowed(String body) 
    {
		// RFC 2646 Format=flowed decoration
		StringBuffer sb = new StringBuffer(body.replace("\n", "<br>"));
		StringBuffer result = new StringBuffer(
				"<div style='font: 8pt sans-serif'>");
		int level = 0;

		while (sb.length() > 0) {
			int pos = sb.indexOf("<br>");
			if (pos >= 0) {
				String line = sb.substring(0, pos + 4);
				int lineLevel = getLineLevel(line);
				if (lineLevel > level) {
					level++;
					String color = getFlowedMarkerHMTLColor(level);
					result
							.append(
									"<div style='font: 8pt sans-serif;border-right: 2px ")
							.append(color).append(" solid;border-left: 2px ")
							.append(color).append(" solid;padding:5px'>");
				} else if (lineLevel < level) {
					level--;
					result.append("</div>");
				}

				result.append(line.substring(lineLevel)).append('\n');
				sb.delete(0, pos + 4);
			} else {
				result.append(sb);
				break;
			}
		}
		result.append("</div>");
		return result.toString();
	}
    
    public static String getHeaderParamValue(SmtpHeaders h, String name, String paramName)
    {
    	if (h == null)
    		return null;
    	
		String[] vals = h.getHeaderValues(name);
		if (vals != null)
		{
			String pattern = paramName + "=";
			
			for (int i=0,max=vals.length;i<max;i++)
			{			
			    if (vals[i].startsWith(pattern))
			    {
					String param = vals[i].substring(pattern.length());
					if (param.charAt(0) == '\"')
					    return param.substring(1, param.length()-1);
					
					return param;
			    }
			}
		}
		
		return null;
    }
    
    public static String outputBody(SmtpMessage msg)
    	throws IOException
    {
	String body = msg.getBody();
	String format = getHeaderParamValue(msg.getHeaders(), SmtpHeaders.HEADER_CONTENT_TYPE, "format");
	if (format != null && format.toLowerCase().equals("flowed"))
	    body = formatFlowed(body);
	
	return body;
    }
}
