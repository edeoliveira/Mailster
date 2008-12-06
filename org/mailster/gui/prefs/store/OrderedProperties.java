package org.mailster.gui.prefs.store;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

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
 * OrderedProperties.java - Saves properties in a ordered and clean way to an
 * output file.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Author$ / $Date$
 */
public class OrderedProperties extends Properties 
{
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = -5382947507500989305L;

	public OrderedProperties() 
	{
		super();
	}

	public OrderedProperties(Properties p) 
	{
		super(p);
	}

	/**
	 * Writes the key/value pairs to the given output stream, in a format
	 * suitable for <code>load</code>.<br>
	 * 
	 * If header is not null, this method writes a comment containing the header
	 * as first line to the stream. The next line (or first line if header is
	 * null) contains a comment with the current date. Afterwards the key/value
	 * pairs are written to the stream in the following format.<br>
	 * 
	 * Each line has the form <code>key = value</code>. Newlines, Returns and
	 * tabs are written as <code>\n,\t,\r</code> resp. The characters
	 * <code>\, !, #, =</code> and <code>:</code> are preceeded by a
	 * backslash. Spaces are preceded with a backslash, if and only if they are
	 * at the beginning of the key. Characters that are not in the ascii range
	 * 33 to 127 are written in the <code>\</code><code>u</code>xxxx Form.<br>
	 * 
	 * Following the listing, the output stream is flushed but left open.
	 * 
	 * @param out
	 *            the output stream
	 * @param header
	 *            the header written in the first line, may be null
	 * @throws ClassCastException
	 *             if this property contains any key or value that isn't a
	 *             string
	 * @throws IOException
	 *             if writing to the stream fails
	 * @throws NullPointerException
	 *             if out is null
	 * @since 1.2
	 */
	@SuppressWarnings("unchecked")
	public void store(OutputStream out, String header) throws IOException 
	{
		// The spec says that the file must be encoded using ISO-8859-1.
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,
				"ISO-8859-1"));
		if (header != null)
			writer.println("#" + header);
		writer.println("#" + Calendar.getInstance().getTime()+"\n");

		TreeSet sortedSet = new TreeSet(this.keySet());
		Iterator iter = sortedSet.iterator();
		StringBuilder s = new StringBuilder(); // Reuse the same buffer.
		String lastKey = null;

		while (iter.hasNext()) 
		{
			String key = (String) iter.next();
			if(lastKey != null)
			{
				int pos = lastKey.indexOf(".");
				String currentKey = key.indexOf(".") == -1 ? 
						key : key.substring(0, key.indexOf(".")); 
				if (pos == -1 || !currentKey.equals(lastKey.substring(0, pos)))
					writer.println();
			}
			formatForOutput(key, s, true);
			s.append('=');
			formatForOutput(getProperty(key), s, false);
			writer.println(s);
			lastKey = key;
		}

		writer.flush();
	}

	/**
	 * Formats a key or value for output in a properties file. See store for a
	 * description of the format.
	 * 
	 * @param str
	 *            the string to format
	 * @param buffer
	 *            the buffer to add it to
	 * @param key
	 *            true if all ' ' must be escaped for the key, false if only
	 *            leading spaces must be escaped for the value
	 * @see #store(OutputStream, String)
	 */
	private void formatForOutput(String str, StringBuilder buffer, boolean key) 
	{
		if (key) 
		{
			buffer.setLength(0);
			buffer.ensureCapacity(str.length());
		} 
		else
			buffer.ensureCapacity(buffer.length() + str.length());
		
		boolean head = true;
		int size = str.length();
		for (int i = 0; i < size; i++) 
		{
			char c = str.charAt(i);
			switch (c) 
			{
				case '\n':
					buffer.append("\\n");
					break;
				case '\r':
					buffer.append("\\r");
					break;
				case '\t':
					buffer.append("\\t");
					break;
				case ' ':
					buffer.append(head ? "\\ " : " ");
					break;
				case '\\':
				case '!':
				case '#':
				case '=':
				case ':':
					buffer.append('\\').append(c);
					break;
				default:
					if (c < ' ' || c > '~') 
					{
						String hex = Integer.toHexString(c);
						buffer.append("\\u0000".substring(0, 6 - hex.length()));
						buffer.append(hex);
					} 
					else
						buffer.append(c);
			}
			if (c != ' ')
				head = key;
		}
	}
}