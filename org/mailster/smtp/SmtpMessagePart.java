package org.mailster.smtp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

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
 * SmtpMessagePart.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmtpMessagePart
{
    private SmtpHeadersInterface headers = new SmtpHeaders();
    private List<SmtpMessagePart> parts = new ArrayList<SmtpMessagePart>(1);
    private Map<String, String> cids;
    private StringBuilder body = new StringBuilder();
    private SmtpMessagePart[] attachedFiles;
    private SmtpMessagePart parentPart;

    private String boundary = "";

    public SmtpMessagePart() {
    }

    public StringBuilder getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = new StringBuilder(body);
    }

    public String getBoundary()
    {
        return boundary;
    }

    public void setBoundary(String boundary)
    {
        this.boundary = boundary;
    }

    public List<SmtpMessagePart> getParts()
    {
        return parts;
    }

    public void setParts(List<SmtpMessagePart> parts)
    {
        this.parts = parts;
        for (SmtpMessagePart part : parts)
            part.setParentPart(this);
    }

    public String getContentType()
    {
        return headers.getHeaderValue(SmtpHeadersInterface.CONTENT_TYPE);
    }

    public String getEncoding()
    {
        String encoding = headers
                .getHeaderValue(SmtpHeadersInterface.CONTENT_TRANSFER_ENCODING);
        if (encoding == null)
            // By RFC 2045 '7bit' encoding is defined as default
            return "7bit";
        else
            return encoding;
    }
    
    public String getCharset()
    {
        String charset = MailUtilities.getHeaderParameterValue(headers, SmtpHeadersInterface.CONTENT_TYPE,
                    SmtpHeadersInterface.CHARSET_PARAMETER);
        if (charset == null)
            // By RFC 2045 us-ascii charset is defined as default
            return "US-ASCII";
        else
            return charset;
    }    

    public SmtpHeadersInterface getHeaders()
    {
        return headers;
    }

    public void setHeaders(SmtpHeadersInterface headers)
    {
        this.headers = headers;
    }

    public void addSubPart(SmtpMessagePart part)
    {
        parts.add(part);
    }

    public boolean isMultiPart()
    {
        return parts.size() >= 1;
    }

    public void appendToBody(String s)
    {
        body.append(s);
    }

    public String toString()
    {
        return toString(true);
    }

    public String toString(boolean includeHeaders)
    {
        StringBuilder partOutput = new StringBuilder();
        if (includeHeaders)
            partOutput.append(headers).append("\n\n");

        if (isMultiPart())
        {
            Iterator<SmtpMessagePart> it = parts.iterator();
            while (it.hasNext())
            {
                if (!"".equals(boundary))
                    partOutput.append("--").append(boundary).append('\n');
                partOutput.append(it.next());
            }
            if (!"".equals(boundary))
                partOutput.append("--").append(boundary).append("--\n");
        }
        else
            partOutput.append(body);

        return partOutput.toString();
    }

    public String getDecodedBody()
    {
        String body = getBody().toString().trim();

        String format = MailUtilities.getHeaderParameterValue(getHeaders(),
                SmtpHeadersInterface.CONTENT_TYPE,
                SmtpHeadersInterface.FORMAT_PARAMETER);
        if (format != null && "flowed".equals(format))
            body = MailUtilities.formatFlowed(body);
        else
            body = MailUtilities.decode(body, getCharset(), getEncoding());

        return body;
    }

    public void write(OutputStream os) throws IOException, MessagingException
    {
        MailUtilities.write(os, getBody().toString(), getCharset(), getEncoding());
    }

    private String getMultiPartRelatedContent(SmtpMessagePart parent, String preferredContentType)
    {
    	Iterator<SmtpMessagePart> it = parts.iterator();
    	SmtpMessagePart part = null;

        while (it.hasNext()
                && (part == null || !part.getContentType().startsWith(
                		preferredContentType)))
            part = it.next();

        String content = part.getDecodedBody();

        cids = MailUtilities.saveCIDFilesToTemporaryDirectory(this);
        for (String cid : cids.keySet())
        	content = content.replaceAll(cid, cids.get(cid));

        return content;
    }
    
    protected String getPreferredContent(String preferredContentType)
    {
        String content = null;
        if (isMultiPart())
        {
            Iterator<SmtpMessagePart> it = parts.iterator();

            if (getContentType().startsWith("multipart/related"))
                return getMultiPartRelatedContent(this, preferredContentType);
            else
            {
                while (it.hasNext())
                {
                    SmtpMessagePart part = it.next();
                    String cType = part.getContentType();

                    if (cType.startsWith("text/plain")
                            || cType.startsWith("text/html"))
                    {
                        // get content
                        String body = part.getDecodedBody();

                        if (cType.startsWith(preferredContentType))
                            return body;
                        else
                        if (content == null)
                            content = body;
                    }
                    else if (cType.startsWith("multipart") && content == null)
                    	content = part.getPreferredContent(preferredContentType);
                }

                return content;
            }
        }

        return getDecodedBody();
    }

    public String getFileName()
    {
        return MailUtilities.getHeaderParameterValue(getHeaders(),
                SmtpHeadersInterface.CONTENT_DISPOSITION,
                SmtpHeadersInterface.FILENAME_PARAMETER);
    }

    public String getContentId()
    {
        return getHeaders().getHeaderValue(SmtpHeadersInterface.CONTENT_ID);
    }

    public SmtpMessagePart[] getAttachedFiles()
    {
        if (attachedFiles == null)
        {
            ArrayList<SmtpMessagePart> l = new ArrayList<SmtpMessagePart>();
            if (getParts() == null)
                return new SmtpMessagePart[0];

            Iterator<SmtpMessagePart> it = getParts().iterator();
            while (it.hasNext())
            {
                SmtpMessagePart part = it.next();
                if (part.getFileName() != null)
                    l.add(part);
            }
            attachedFiles = l.toArray(new SmtpMessagePart[l.size()]);
        }

        return attachedFiles;
    }

    public SmtpMessagePart getParentPart()
    {
        return parentPart;
    }

    public void setParentPart(SmtpMessagePart parentPart)
    {
        this.parentPart = parentPart;
    }
}
