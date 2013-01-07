package org.mailster.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.mail.SmtpMessageFactory;
import org.mailster.core.pop3.connection.AbstractPop3Connection;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;

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
 * StreamUtilities.java - Various methods to help reading and writing to streams.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.7 $, $Date: 2009/01/30 01:32:31 $
 */
public class StreamUtilities
{
    private final static String From_ = "From ";
    private final static LineDelimiter lineDelimiter = new LineDelimiter("\n");

    public static void write(String s, AbstractPop3Connection conn)
    {
        write(s, conn, -1);
    }

    public static void write(String s, AbstractPop3Connection conn, int numLines)
    {
        if (s == null)
            return;

        BufferedReader in = new BufferedReader(new StringReader(s));
        try
        {
            write(in, conn, numLines);
            in.close();
        }
        catch (IOException e)
        {
            // Nothing todo
        }
    }

    public static void write(BufferedReader in, AbstractPop3Connection conn)
            throws IOException
    {
        write(in, conn, -1);
    }

    public static void write(BufferedReader in, AbstractPop3Connection conn,
            int numLines) throws IOException
    {
        String line;
        int count = 0;
        boolean headerEnded = false;

        while ((line = in.readLine()) != null
                && (numLines == -1 || !headerEnded || count < numLines))
        {
            // Byte-stuff output
            if (line.length() > 0 && line.charAt(0) == '.')
                line = "." + line;

            conn.println(line);
            if (headerEnded)
                count++;
            else if (headerEnded = "".equals(line))
                continue;
        }
    }

    /**
     * A reader scans through a mbox file looking for From_ lines.
 	 * Any From_ line marks the beginning of a message.  The reader
     * should not attempt to take advantage of the fact that  every
     * From_ line (past the beginning of the file) is preceded by a
     * blank line.
	 *
     * Once the reader finds a message,  it  extracts  a  (possibly
     * corrupted)  envelope  sender  and  delivery  date out of the
     * From_ line.  It then reads until the next From_ line or  end
     * of  file,  whichever  comes  first.  It strips off the final
     * blank line and deletes  the  quoting  of  >From_  lines  and
     * >>From_ lines and so on.  The result is an RFC 822 message.
     * 
     * http://www.qmail.org/qmail-manual-html/man5/mbox.html
     */    
    public static List<SmtpMessage> readMessageFromMBoxRDFormat(BufferedReader in, Charset charset)
    {
    	SmtpMessageFactory factory = new SmtpMessageFactory(charset, lineDelimiter);
    	List<SmtpMessage> mails = new ArrayList<SmtpMessage>();
    	String charsetName = charset.displayName();
    	
    	try 
    	{
    		String line = null;
    		StringBuilder msg = null;
    		boolean skip = true;
    		
			while ((line = in.readLine()) != null)
			{
				if (line.startsWith(From_))
				{
					 if (skip)
						 skip = false;
					 else
					 {
						 msg.deleteCharAt(msg.length()-1);
						 mails.add(factory.asSmtpMessage(
							new ByteArrayInputStream(msg.toString().getBytes(charsetName)), null));
					 }
					 
					 msg = new StringBuilder();
				}
				else
				if (skip == true)
					continue;
				else
				{
					if (line.matches(">*From.*"))
					{
						int i=0;
						while(line.charAt(i) == '>')
							i+=1;
						
						line = line.substring(i);
					}
					
					msg.append(line).append('\n');
				}
			}
			
			// Add last message
			msg.deleteCharAt(msg.length()-1);
			mails.add(factory.asSmtpMessage(
					new ByteArrayInputStream(msg.toString().getBytes(charsetName)), null));
		} 
    	catch (Exception e) 
		{
			e.printStackTrace();
		}
    	
    	return mails;
    }
    
    /**
     * Here is how a program appends a message to an mbox file.
	 * 
     * It first creates a From_ line given the  message's  envelope
     * sender  and  the  current  date.   If the envelope sender is
     * empty (i.e., if this is a bounce message), the program  uses
     * MAILER-DAEMON  instead.   If  the  envelope  sender contains
     * spaces, tabs, or newlines, the program  replaces  them  with
     * hyphens.
	 *
     * The program then copies the message, applying >From  quoting
     * to  each  line.   >From  quoting  ensures that the resulting
     * lines are not From_ lines:  the program prepends a > to  any
     * From_ line, >From_ line, >>From_ line, >>>From_ line, etc.
	 *  
     * Finally the program appends a blank line to the message.  If
     * the  last  line of the message was a partial line, it writes
     * two newlines; otherwise it writes one.
     * 
     * http://www.qmail.org/qmail-manual-html/man5/mbox.html
     */
    public static void writeMessageToMBoxRDFormat(StoredSmtpMessage msg,
            PrintWriter out)
    {
        BufferedReader in = new BufferedReader(new StringReader(msg
                .getMessage().toString()));

        try
        {
            String envSender = msg.getMessageFrom();
            envSender = envSender == null || "".equals(envSender)
                    ? "MAILER-DAEMON"
                    : envSender.replaceAll("[ \t\r\n]", "-").trim();

            out.println(From_ + envSender + " "
                    + DateUtilities.formatAsFixedWidthAsctime(msg.getInternalDate()));

            String line;
            String last = null;
            while ((line = in.readLine()) != null)
            {
                // Quote
                int pos = line.indexOf("From");
                if (pos == 0 || (pos > 0 && line.matches(">*From.*")))
                    out.println('>' + line);
                else
                    out.println(line);
                last = line;
            }

            // Add extra blank line if needed
            if (last != null && !last.endsWith("\n"))
                out.println();

            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}