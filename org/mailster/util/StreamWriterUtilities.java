package org.mailster.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.mailster.pop3.connection.AbstractPop3Connection;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.smtp.SmtpHeadersInterface;

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
 * StreamWriterUtilities.java - Various methods to help writing to streams.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class StreamWriterUtilities
{
    public final static String USER_DIR = System.getProperty("user.dir").replace(File.separatorChar, '/'); //$NON-NLS-1$

    private final static String From_ = "From ";

    // ANSI C's asctime() format
    private final static SimpleDateFormat ascTimeFormatter = new SimpleDateFormat(
            "EEE MMM d HH:mm:ss yyyy", Locale.US);

    private static String formatAsFixedWidthAsctime(Date d)
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

    public static void writeMessageToMBoxRDFormat(StoredSmtpMessage msg,
            PrintWriter out)
    {
        BufferedReader in = new BufferedReader(new StringReader(msg
                .getMessage().getRawMessage()));

        try
        {
            String envSender = msg.getMessage().getHeaders().getHeaderValue(
                    SmtpHeadersInterface.FROM);
            envSender = envSender == null && "".equals(envSender)
                    ? "MAILER-DAEMON"
                    : envSender.replaceAll("[ \t\r\n]", "-").trim();

            out.println(From_ + envSender + " "
                    + formatAsFixedWidthAsctime(msg.getInternalDate()));

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
