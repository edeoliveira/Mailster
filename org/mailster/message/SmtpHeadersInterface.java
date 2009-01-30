package org.mailster.message;

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
 * SmtpHeadersInterface.java - Enter your Comment HERE.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public interface SmtpHeadersInterface
{
    public final static String TO = "To";
    public final static String DATE = "Date";
    public final static String SUBJECT = "Subject";
    public final static String CONTENT_TYPE = "Content-Type";
    public static final String RETURN_PATH = "Return-Path";
    public static final String RECEIVED = "Received";
    public static final String MESSAGE_ID = "Message-ID";
    public static final String RESENT_DATE = "Resent-Date";
    public static final String RESENT_FROM = "Resent-From";
    public static final String FROM = "From";
    public static final String REPLY_TO = "Reply-To";
    public static final String CC = "Cc";
    public static final String IN_REPLY_TO = "In-Reply-To";
    public static final String RESENT_MESSAGE_ID = "Resent-Message-ID";
    public static final String ERRORS_TO = "Errors-To";
    public static final String MIME_VERSION = "Mime-Version";
    public static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String STATUS = "Status";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_ID = "Content-ID";

    // Optional parameters
    public static final String FILENAME_PARAMETER = "filename";
    public static final String FORMAT_PARAMETER = "format";
    public static final String CHARSET_PARAMETER = "charset";
    public final static String BOUNDARY_PARAMETER = "boundary";

    // Content types
    public static final String TEXT_HTML_CONTENT_TYPE 	= "text/html";
    public static final String TEXT_PLAIN_CONTENT_TYPE 	= "text/plain";
    
    /**
     * Get the string representation of the headers
     * 
     * @return string representation of the headers
     */
    public String toString();

    /**
     * Get the value(s) associated with the given header name.
     * 
     * @param name header name
     * @return value(s) associated with the header name
     */
    public String[] getHeaderValues(String name);

    /**
     * Get the first value associated with a given header name.
     * 
     * @param name header name
     * @return first value associated with the header name
     */
    public String getHeaderValue(String name);

    /**
     * Adds a header.
     * 
     * @param name header name
     * @param value header value
     */
    public void addHeader(String name, String value);

    /**
     * Adds a header.
     * 
     * @param line parsed line
     */
    public void addHeaderLine(String line);
}
