package org.mailster.smtp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.mailster.util.MailUtilities;

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
 * SmtpHeaders.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmtpHeaders implements SmtpHeadersInterface
{
    /** 
     * Headers: Map of List of String hashed on header name. 
     */
    private Map<String, SmtpHeader> headers = new HashMap<String, SmtpHeader>(
            10);
    private String lastHeaderName;

    public class SmtpHeader
    {
        private String name;

        private List<String> values;

        public SmtpHeader(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public List<String> getValues()
        {
            return values;
        }

        public void setValues(List<String> values)
        {
            this.values = values;
        }

        public String toString()
        {
        	StringBuilder sb = new StringBuilder(getName());
            sb.append(": ");
            Iterator<String> it = values.iterator();
            while (it.hasNext())
            {
                sb.append(it.next());
                if (it.hasNext())
                    sb.append("; ");
            }

            return sb.toString();
        }
    }

    public SmtpHeaders() {
    }

    /**
     * Get the value(s) associated with the given header name.
     * 
     * @param name header name
     * @return value(s) associated with the header name
     */
    public String[] getHeaderValues(String name)
    {
        SmtpHeader h = headers.get(name);
        if (h == null || h.getValues() == null)
            return new String[0];
        else
            return h.getValues().toArray(new String[h.getValues().size()]);
    }

    /**
     * Get the first value associated with a given header name.
     * 
     * @param name header name
     * @return first value associated with the header name
     */
    public String getHeaderValue(String name)
    {
        SmtpHeader h = headers.get(name);
        if (h == null || h.getValues() == null || h.getValues().get(0) == null)
            return null;
        else
            return h.getValues().get(0);
    }

    /**
     * Adds a header to the Map.
     */
    public void addHeader(String name, String value)
    {
        List<String> vals = new ArrayList<String>(1);
        if (SmtpHeadersInterface.SUBJECT.equals(name))
            vals.add(MailUtilities.decodeHeaderValue(value));
        else
            vals.add(value);

        headers.put(name, new SmtpHeader(name, vals));
    }

    /**
     * Adds a header to the Map.
     */
    public void addHeaderLine(String line)
    {
        if (line != null && !"".equals(line))
        {
            int pos = line.indexOf(':');
            int termPos = line.indexOf('=');

            if (pos >= 0 && (termPos == -1 || pos < termPos))
            {
                String name = line.substring(0, pos);
                lastHeaderName = name;
                List<String> vals = null;
                
                if (lastHeaderName.startsWith("X-"))
                {
                    // eXtended header so leave it intact 
                    vals = new ArrayList<String>(1);
                    // strips first blank character
                    vals.add(line.substring(pos + 2));
                }
                else
                {
                    String values = MailUtilities.decodeHeaderValue(line
                            .substring(pos + 2));
    
                    StringTokenizer tk = new StringTokenizer(values, ";");
                    vals = new ArrayList<String>(tk.countTokens());
    
                    while (tk.hasMoreTokens())
                        vals.add(tk.nextToken().trim());
                }

                headers.put(name, new SmtpHeader(name, vals));
            }
            else
            {
                // Additional header value
                SmtpHeader h = headers.get(lastHeaderName);
                if (lastHeaderName.startsWith("X-"))
                    h.getValues().add(line);
                else if (SmtpHeadersInterface.SUBJECT.equals(lastHeaderName))
                    h.getValues().set(
                            0,
                            h.getValues().get(0)
                                    + MailUtilities.decodeHeaderValue(line.trim()));
                else
                    h.getValues().add(line.trim());
            }
        }
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = headers.keySet().iterator(); i.hasNext();)
        {
            SmtpHeader hdr = headers.get(i.next());
            sb.append(hdr);
            if (i.hasNext())
                sb.append('\n');
        }
        return sb.toString();
    }
}
