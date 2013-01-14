package org.mailster.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.mailster.core.mail.SmtpHeaders;
import org.mailster.core.mail.SmtpHeadersInterface;
import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.mail.SmtpMessagePart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * MailUtilities.java - Various methods to help in message handling, parsing,
 * formatting, writting etc...
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.19 $, $Date: 2009/03/22 21:56:40 $
 */
public class MailUtilities
{
    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(MailUtilities.class);
    
    /**
     * Maximum SMTP communication line width (RFC 822 sets it to "72 or 73
     * characters" so we give some extra characters just to ensure not loosing
     * characters).
     */
    public final static int MAX_LINE_LENGTH = 80;

    /**
     * Temporary directory name used when saving temporary data. Value is set to
     * the value of the 'java.io.tmpdir' system property.
     * 
     * @see java.lang.System#getProperty(String)
     */
    public final static String tempDirectory = System
            .getProperty("java.io.tmpdir");

    /**
     * Returns the value of the header <code>headerName</code>. If the parameter
     * <code>decode</code> is set to true the the value is decoded. If value is
     * null, returns "" unless required header is of the following types :
     * <p>
     * Date : returns a RFC 822 compliant string of the current date and time<br>
     * Subject : returns the "{No subject}" string<br>
     * <p>
     * This is a convenience method for GUI programs to avoid returning a null
     * value for headers that are not commonly expected to be null but can be
     * according to RFCs.
     * <p>
     * 
     * @param headers all the headers in which to search
     * @param headerName the name of the header
     * @param decode if true the header value is decoded
     * @return the non null header value
     */
    public static String getNonNullHeaderValue(SmtpHeadersInterface headers,
            String headerName, boolean decode)
    {
        if (headers == null || headerName == null)
            return "";

        String s = headers.getHeaderValue(headerName);
        if (s == null)
        {
            if (SmtpHeadersInterface.DATE.equals(headerName))
            {
            	synchronized(DateUtilities.RFC822_FORMATTER)
            	{
            		return DateUtilities.RFC822_FORMATTER.format(new Date());
            	}
            }
            else if (SmtpHeadersInterface.SUBJECT.equals(headerName))
                return "{No subject}";

            return "";
        }
        else
        {
        	if (decode)
        	{
        		if (SmtpHeadersInterface.SUBJECT.equals(headerName))
        		{
        			StringBuilder sb = new StringBuilder();
        			for (String val : headers.getHeaderValues(headerName))
        			{
						try 
        				{
							sb.append(MimeUtility.decodeText(val));
						} 
						catch (UnsupportedEncodingException e) {}
        			}
        			return sb.toString();
        		}
        		else
        			return decodeHeaderValue(s);
        	}
        	else
        		return s;
        }
    }

    /**
     * Decodes a encoded header value. Returns null if <code>string</code> is
     * null. If decoding fails, it returns the original string.
     * <p>
     * All the encodings defined in RFC 2045 are supported here. They include
     * "base64", "quoted-printable", "7bit", "8bit", and "binary". In addition,
     * "uuencode" is also supported.
     * 
     * @see javax.mail.internet.MimeUtility#decodeText(String)
     * @param encodedHeaderValue the header value to decode
     * @return the decoded string
     */
    public static String decodeHeaderValue(String encodedHeaderValue)
    {
        try
        {
            if (encodedHeaderValue != null)
            {
                String str = encodedHeaderValue.trim();
                if (str.startsWith("=?") && str.endsWith("?="))
                    return MimeUtility.decodeText(str);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        catch (NoClassDefFoundError nex)
        {
            nex.printStackTrace();
        }

        return encodedHeaderValue;
    }

    /**
     * Decodes the string <code>encodedString</code> encoded as
     * <code>encoding</code> with the charset named <code>charsetName</code>.
     * If decoding fails, it returns the original string.
     * <p>
     * All the encodings defined in RFC 2045 are supported here. They include
     * "base64", "quoted-printable", "7bit", "8bit", and "binary". In addition,
     * "uuencode" is also supported.
     * 
     * @see javax.mail.internet.MimeUtility#decode(java.io.InputStream, java.lang.String)
     * @param encodedString the string to decode
     * @param charsetName the name of the charset
     * @param encoding the encoding used for the string
     * @return the decoded String
     */
    public static String decode(String encodedString, String charsetName,
            String encoding)
    {
        try
        {
            String charset = charsetName != null ? MimeUtility
                    .javaCharset(charsetName) : null;
            byte[] b = charset == null
                    ? encodedString.getBytes()
                    : encodedString.getBytes(charset);
            InputStream is = MimeUtility.decode(new ByteArrayInputStream(b),
                    encoding);

            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[4096];
            int nb = 0;

            while ((nb = is.read(buffer)) >= 0)
                sb.append(new String(buffer, 0, nb));

            return sb.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return encodedString;
        }
        catch (NoClassDefFoundError nex)
        {
            nex.printStackTrace();
            return encodedString;
        }
    }

    /**
     * Saves the content of the sub parts of the given message part to the
     * system default temporary directory if they have a Content-ID header.
     * Method returns a HashMap where CID aka Content-ID is the key to a string
     * representing the URL of the corresponding local file.
     * 
     * @param part the message part containing relative files to save
     * @return a hashmap where CID is the key to a string representing the URL
     *         to a local file
     */
    public static HashMap<String, String> saveCIDFilesToTemporaryDirectory(
            SmtpMessagePart part)
    {
        HashMap<String, String> cids = new HashMap<String, String>(part
                .getParts().size());
        for (SmtpMessagePart subPart : part.getParts())
        {
            String cid = subPart.getContentId();
            if (cid != null && !"".equals(cid))
            {
                cid = cid.substring(1, cid.length() - 1);
                String fileName = subPart.getFileName();
                fileName = tempDirectory + (fileName == null ? cid : fileName);
                try
                {
                    FileOutputStream f = new FileOutputStream(
                            new File(fileName));
                    subPart.write(f);
                    f.flush();
                    f.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                cid = "cid:" + cid;
                cids.put(cid, "file:///"
                        + fileName.replace(File.separatorChar, '/'));
            }
        }

        return cids;
    }

    /**
     * Outputs the decoded content from the
     * <code>encodedString</quote> string to the <code>os</code> output stream.
     * 
     * @param os the output stream
     * @param encodedString the content to write 
     * @param charsetName the name of the charset
     * @param encoding the name of the encoding
     * @throws IOException
     * @throws MessagingException
     */
    public static void write(OutputStream os, String encodedString,
            String charsetName, String encoding) throws IOException,
            MessagingException
    {
        String charset = charsetName != null ? MimeUtility
                .javaCharset(charsetName) : null;
        byte[] b = charset == null ? encodedString.getBytes() : encodedString
                .getBytes(charset);
        InputStream is = MimeUtility.decode(new ByteArrayInputStream(b),
                encoding);

        byte[] buffer = new byte[4096];
        int nb = 0;

        while ((nb = is.read(buffer)) >= 0)
            os.write(buffer, 0, nb);
    }

    /**
     * Returns the quote level of the format flowed message line
     * <code>line</code>.
     * 
     * @param line the text line
     * @return the quote level
     */
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

    /**
     * Computes and returns the HTML color string (#xxyyzz) associated with the
     * quote level <code>level</code>.
     * 
     * @param level the quote level
     * @return the HTML color string
     */
    private static String getFlowedMarkerHMTLColor(int level)
    {
        String r = Integer.toHexString((80 + level * 32) % 256);
        String v = Integer.toHexString(Math.max(
                Math.abs(256 - level * 35) % 256, 40));
        String b = Integer.toHexString((50 + level * 64) % 256);

        return "#" + r + v + b;
    }

    /**
     * Returns a decorated version of <code>body</code> compliant with RFC
     * 2646 when interpreted in a browser.
     * 
     * @param body the body of the message
     * @return the decorated body
     */
    public static String formatFlowed(String body)
    {
    	StringBuilder sb = new StringBuilder(body.replace("\n", "<br>"));
    	StringBuilder result = new StringBuilder(
                "<div style='font: 8pt sans-serif'>");
        int level = 0;
        int pos = 0;

        do
        {
            pos = sb.indexOf("<br>");
            String line = pos == -1 ? sb.toString() : sb.substring(0, pos + 4);
            int lineLevel = getLineLevel(line.trim());
            if (lineLevel > level)
            {
                level++;
                String color = getFlowedMarkerHMTLColor(level);
                result.append(
                        "<div style='font: 8pt sans-serif;border-right: 2px ")
                        .append(color).append(" solid;border-left: 2px ")
                        .append(color).append(" solid;padding: 5px'>");
            }
            else if (lineLevel < level)
            {
                level--;
                result.append("</div>");
            }

            result.append(line.substring(lineLevel)).append('\n');
            if (pos >= 0)
                sb.delete(0, pos + 4);
        }
        while (pos >= 0);

        result.append("</div>");
        return result.toString();
    }

    /**
     * Returns the value of a specific parameter named
     * <code>parameterName</code> which is a sub value of the header
     * <code>name</code> from the headers <code>headers</code>.
     * 
     * @param headers the headers
     * @param name the header name
     * @param parameterName the parameter name
     * @return the value as a string
     */
    public static String getHeaderParameterValue(SmtpHeadersInterface headers,
            String name, String parameterName)
    {
        if (headers == null)
            return null;

        String[] vals = headers.getHeaderValues(name);
        if (vals != null && vals.length > 0)
        {
            String pattern = parameterName + "=";

            for (int i = 0, max = vals.length; i < max; i++)
            {
                if (vals[i].startsWith(pattern))
                {
                    String param = vals[i].substring(pattern.length());
                    if (param.charAt(0) == '\"')
                        return param.substring(1, param.length() - 1);

                    return param;
                }
            }
        }

        return null;
    }

    /**
     * Returns the string that delimits sub parts of a MIME multipart message.
     * This string is the value of the boundary parameter which is a sub value
     * of the Content-Type header from the headers <code>headers</code>.
     * 
     * @param headers the headers of the part
     * @return the boundary string
     */
    private static String getPartBoundary(SmtpHeadersInterface headers)
    {
        return getHeaderParameterValue(headers,
                SmtpHeadersInterface.CONTENT_TYPE,
                SmtpHeadersInterface.BOUNDARY_PARAMETER);
    }

    /**
     * Returns true if headers <code>headers</code> represent a multipart
     * content.
     * 
     * @param headers the headers
     * @return true if part content is of type multipart
     */
    public static boolean isMultiPart(SmtpHeadersInterface headers)
    {
        String contentType = headers
                .getHeaderValue(SmtpHeadersInterface.CONTENT_TYPE);
        return contentType != null && contentType.startsWith("multipart");
    }

    /**
     * Returns true if headers <code>headers</code> represent a message
     * content.
     * 
     * @param headers the headers
     * @return true if part content is of type message
     */
    private static boolean isMessagePart(SmtpHeadersInterface headers)
    {
        String contentType = headers
                .getHeaderValue(SmtpHeadersInterface.CONTENT_TYPE);
        return contentType != null && contentType.startsWith("message");
    }

    /**
     * Returns a SmtpMessagePart from parsing <code>reader</code>. If
     * <code>readHeader</code> is true then data read from <code>reader</code>
     * should contain the headers of the part (always the case except for a
     * simple single part message where headers are set within the message
     * headers).
     * 
     * @param reader the mail body reader stream
     * @param parentHeaders the headers of the parent part
     * @param readHeader true if method should read headers from the reader
     * @return a part representing the reader contents
     * @throws IOException
     */
    private static SmtpMessagePart handleBodyPart(BufferedReader reader,
            SmtpHeadersInterface parentHeaders, boolean readHeader)
            throws IOException
    {
        boolean isHeader = readHeader;
        int lineNum = 0;
        SmtpHeadersInterface headers = new SmtpHeaders();
        SmtpMessagePart mPart = new SmtpMessagePart();

        LOG.debug("[DEBUG] --- BODY PART START ---");
        if (isHeader)
            LOG.debug("[DEBUG] --- Header start ---");

        while (reader.ready())
        {
            reader.mark(MAX_LINE_LENGTH);
            String line = reader.readLine();

            if (isHeader)
            {
                if ("".equals(line))
                {
                    isHeader = false;
                    LOG.debug("[DEBUG] --- Header end ---");

                    mPart.setHeaders(headers);
                    if (isMultiPart(headers))
                    {
                        mPart.setBoundary(getPartBoundary(headers));
                        mPart.setParts(handleMultiPart(reader, headers));
                        LOG.debug("[DEBUG] --- BODY PART END ---");
                        return mPart;
                    }
                    if (isMessagePart(headers))
                    {
                        handleRFC822MessagePart(reader, mPart,
                                getPartBoundary(parentHeaders));
                        LOG.debug("[DEBUG] --- BODY PART END ---");
                        return mPart;
                    }
                }
                else
                    headers.addHeaderLine(line);

                LOG.debug(line);
            }
            else if (line == null || line.startsWith("--"))
            {
                reader.reset();
                break;
            }
            else
            {
            	if (lineNum == 1)
            	{
                	lineNum++;
                	mPart.appendToBody("\n");
            	}

            	if (!"".equals(line))
            		mPart.appendToBody(line);
            	
            	if (!readHeader && lineNum == 0)
            		lineNum++;
            	else
            		mPart.appendToBody("\n");
            }
        }

        if (LOG.isDebugEnabled())
        	LOG.debug(mPart.getBody().toString());
        
        LOG.debug("[DEBUG] --- BODY PART END ---");
        return mPart;
    }

    /**
     * Reads an embedded RFC 822 message and writes it to the <code>part</code>
     * body's part.
     * 
     * @param reader the reader stream
     * @param part the target part
     * @param boundary the boundary of the part
     * @throws IOException
     */
    private static void handleRFC822MessagePart(BufferedReader reader,
            SmtpMessagePart part, String boundary) throws IOException
    {
        String partEndBoundary = "--" + boundary + "--";

        while (reader.ready())
        {
            String line = reader.readLine();

            if (line == null || line.startsWith(partEndBoundary))
                break;
            else
            {
                part.appendToBody(line);
                part.appendToBody("\n");
            }
        }

        if (LOG.isDebugEnabled())
        	LOG.debug(part.getBody().toString());
    }

    /**
     * Returns a SmtpMessagePart's list from parsing <code>reader</code>.
     * 
     * @param reader the mail body reader stream
     * @param parentHeaders the headers of the parent part
     * @return the list of parts
     * @throws IOException
     */
    private static List<SmtpMessagePart> handleMultiPart(BufferedReader reader,
            SmtpHeadersInterface parentHeaders) throws IOException
    {
        List<SmtpMessagePart> mParts = new ArrayList<SmtpMessagePart>();
        String boundary = getPartBoundary(parentHeaders);
        String end = "--" + boundary + "--";
        StringBuilder bodyPart = new StringBuilder();

        while (reader.ready())
        {
            String line = reader.readLine();
            
            if (line == null)
        	break;
            
            if (boundary == null && line.startsWith("--"))
            {
                boundary = line;
                end = boundary + "--";
            }
            
            if (boundary != null && line.equals(end)) {
                bodyPart.append(line).append('\n');
                break;
            }
            
            bodyPart.append(line).append('\n');
        }

        BufferedReader mpReader = new BufferedReader(new StringReader(bodyPart
                .toString()));
        LOG.debug("[DEBUG] --- MULTI PART START (boundary {}) --- ", boundary);

        int i = 1;
        String line = null;
        do
        {
            while (mpReader.ready())
            {
                line = mpReader.readLine();
                if (line == null || line.indexOf(boundary) >= 0)
                    break;
                LOG.debug(line);
            }

            if (line == null || line.equals(end))
                break;

            LOG.debug("[DEBUG] --- MULTI PART {} (boundary={}) START ---", i, boundary);
            mParts.add(handleBodyPart(mpReader, parentHeaders, true));
            LOG.debug("[DEBUG] --- MULTI PART {} (boundary={}) END ---", i, boundary);
            i++;
        }
        while (mpReader.ready());

        LOG.debug("[DEBUG] --- MULTI PART END (boundary {}) ---", boundary);
        return mParts;
    }

    /**
     * Parses <code>msg</code> and returns a SmtpMessagePart representing the
     * contents of the message.
     * 
     * @param msg the message to be parsed
     * @param body the body of the message
     * @return the object representation of the message's body
     */
    public static SmtpMessagePart parseInternalParts(SmtpMessage msg, String body)
    {
        LOG.debug("[DEBUG] --- MAIL ---");
        if (LOG.isDebugEnabled())
        	LOG.debug(body);
        LOG.debug("[DEBUG] --- END MAIL ---\n\n");

        SmtpMessagePart mPart = null;

        try
        {
            BufferedReader reader = new BufferedReader(new StringReader(body));
            LOG.debug("[DEBUG] --- MAIL BODY PARSING START ---");

            if (isMultiPart(msg.getHeaders()))
            {
                mPart = new SmtpMessagePart();
                mPart.setBoundary(getPartBoundary(msg.getHeaders()));
                mPart.setParts(handleMultiPart(reader, msg.getHeaders()));
            }
            else
                mPart = handleBodyPart(reader, msg.getHeaders(), false);

            mPart.setHeaders(msg.getHeaders());
            LOG.debug("[DEBUG] --- MAIL BODY PARSING END ---");
        }
        catch (Exception ex)
        {
        	LOG.debug("[EXCEPTION] --- !!! PARSING FAILED EXCEPTION !!! ---", ex);
            mPart = new SmtpMessagePart();
            mPart.setHeaders(msg.getHeaders());
            mPart.setBody(body);
        }
        LOG.debug("*************************************************************");
        if (LOG.isDebugEnabled())
        	LOG.debug(mPart.toString());
        return mPart;
    }

    /**
     * Returns an escaped HTML string.
     * 
     * @param string the string to escape
     * @return the escaped string
     */
    public static String escapeHTMLString(String string)
    {
    	StringBuilder sb = new StringBuilder(string.length());
        boolean lastWasBlankChar = false; // true if last char was blank

        for (int i = 0, len = string.length(); i < len; i++)
        {
            char c = string.charAt(i);
            if (c == ' ')
            {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar)
                    sb.append("&nbsp;");
                else
                    sb.append(' ');
                lastWasBlankChar = true;
            }
            else
            {
                lastWasBlankChar = false;

                // HTML Special Chars
                if (c == '"')
                    sb.append("&quot;");
                else if (c == '&')
                    sb.append("&amp;");
                else if (c == '<')
                    sb.append("&lt;");
                else if (c == '>')
                    sb.append("&gt;");
                else if (c == '\n')
                    // Handle Newline
                    sb.append("&lt;br/&gt;");
                else
                {
                    int ci = 0xffff & c;
                    if (ci < 160)
                        sb.append(c);
                    else
                        // Not 7 Bit use the unicode system
                        sb.append("&#").append(new Integer(ci).toString())
                                .append(';');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Writes the stream to the specified directory and filename.
     * 
     * @param directory the directory to write to
     * @param fileName the image file name
     * @param bin the inputstream to read from
     * @return true if write succeeded
     */
    public static boolean outputStreamToFile(String directory, String fileName,
            InputStream bin)
    {
        try
        {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(new File(directory + fileName)));
            byte[] buffer = new byte[4096];

            int nb;
            while ((nb = bin.read(buffer)) > 0)
                bos.write(buffer, 0, nb);

            bos.flush();
            bos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    
    private static List<String> extractEmails(String... list)
    {
    	List<String> l = new ArrayList<String>();
    	for (String s : list)
		{
			if (s != null && s.length() > 0)
			{
				if (s.indexOf(',') >= 0)
				{
					StringTokenizer tk = new StringTokenizer(s, ",");

					while (tk.hasMoreTokens())
					{
						String email = tk.nextToken().trim();
						if (!"".equals(email))
							l.add(email);
					}
				}
				else
					l.add(s.trim());
			}
		}
    	
    	return l;
    }
    
	public static String formatEmailList(String[] list)
	{
		StringBuilder sb = new StringBuilder();
		List<String> tmp = extractEmails(list);
		for (String s : tmp)
		{
			if (sb.length() > 0)
				sb.append(';');

			sb.append(s);
		}

		return sb.toString();
	}

	public static String formatEmailList(SmtpHeadersInterface headers, String headerName)
	{
		return formatEmailList(headers.getHeaderValues(headerName));
	}

	public static void removeMatchesFromList(List<String> recipients, SmtpMessage msg, String headerName)
	{
		if (recipients.size() <= 0)
			return;

		List<String> values = extractEmails(msg.getHeaders().getHeaderValues(headerName));

		// Detect recipients formating.
		boolean withDelimiters = recipients.get(0).contains("<");

		for (String s : values)
		{
			if (s != null)
			{
				int pos = s.indexOf('<');

				try
				{
					if (withDelimiters)
					{
						if (pos == -1)
							s = "<" + s.trim() + ">";
						else
							s = s.substring(pos, s.indexOf('>') + 1);

						for (String str : recipients)
						{
							if (str != null && str.contains(s))
							{
								recipients.remove(str);
								break;
							}
						}
					}
					else
					{
						if (pos != -1)
							s = s.substring(pos + 1, s.indexOf('>'));
						else
							s = s.trim();

						recipients.remove(s);
					}
				} catch (Exception ex)
				{
					// Just to prevent malformed email addresses
					LOG.debug("Malformed email adress", ex);
				}
			}
		}
	}

	public static String formatBccList(SmtpMessage msg)
	{
		List<String> recipients = extractEmails(msg.getRecipients().toArray(new String[msg.getRecipients().size()]));

		removeMatchesFromList(recipients, msg, SmtpHeadersInterface.TO);
		removeMatchesFromList(recipients, msg, SmtpHeadersInterface.CC);

		return formatEmailList(recipients.toArray(new String[recipients.size()]));
	}    
}
