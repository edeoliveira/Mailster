package com.dumbster.smtp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.mailster.util.MailUtilities;

public class SmtpHeaders
{
    public final static String HEADER_TO = "To";
    public final static String HEADER_DATE = "Date";
    public final static String HEADER_SUBJECT = "Subject";
    public final static String HEADER_MESSAGE_ID = "Message-ID";
    public final static String HEADER_CONTENT_TYPE = "Content-Type";
    public final static String HEADER_ENCODING = "Content-Transfer-Encoding";

    /** Headers: Map of List of String hashed on header name. */
    private Map<String, SmtpHeader> headers = new HashMap<String, SmtpHeader>(
	    10);

    private String lastHeaderName;

    public class SmtpHeader
    {
	private String name;

	private List<String> values;

	public SmtpHeader(String name, List<String> values)
	{
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
	    StringBuffer sb = new StringBuffer(getName());
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

    public SmtpHeaders()
    {}

    /**
         * Get the value(s) associated with the given header name.
         * 
         * @param name
         *                header name
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
         * @param name
         *                header name
         * @return first value associated with the header name
         */
    public String getHeaderValue(String name)
    {
	SmtpHeader h = headers.get(name);
	if (h == null || h.getValues() == null || h.getValues().get(0) == null)
	    return "";
	else
	    return h.getValues().get(0);
    }

    /**
         * Adds a header to the Map.
         */
    public void addHeader(String name, String value)
    {
	List<String> vals = new ArrayList<String>(1);
	if (name.equals(HEADER_SUBJECT))
	    value = MailUtilities.decodeText(value);
	vals.add(value);

	headers.put(name, new SmtpHeader(name, vals));
    }

    /**
         * Adds a header to the Map.
         */
    public void addHeaderLine(String line)
    {
	if (line != null && !line.equals(""))
	{
	    int pos = line.indexOf(':');
	    int termPos = line.indexOf('=');

	    if (pos >= 0 && (termPos == -1 || pos < termPos))
	    {
		String name = line.substring(0, pos).trim();
		lastHeaderName = name;
		String values = MailUtilities.decodeText(line
			.substring(pos + 1).trim());

		StringTokenizer tk = new StringTokenizer(values, ";");
		List<String> vals = new ArrayList<String>(tk.countTokens());

		while (tk.hasMoreTokens())
		    vals.add(tk.nextToken().trim());

		headers.put(name, new SmtpHeader(name, vals));
	    }
	    else
	    {
		// Additional header value
		SmtpHeader h = headers.get(lastHeaderName);
		if (lastHeaderName.equals(HEADER_SUBJECT))
		    h.getValues().set(0, h.getValues().get(0) + MailUtilities.decodeText(line));
		else
		    h.getValues().add(line.trim());
	    }
	}
    }

    public String toString()
    {
	StringBuffer sb = new StringBuffer();
	for (Iterator i = headers.keySet().iterator(); i.hasNext();)
	{
	    String name = (String) i.next();
	    SmtpHeader hdr = headers.get(name);
	    sb.append(hdr);
	    if (i.hasNext())
		sb.append('\n');
	}
	return sb.toString();
    }
}
