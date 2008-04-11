package org.mailster.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.security.sasl.SaslException;

import org.mailster.pop3.commands.auth.AuthException;

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
 * StringUtilities.java - Various methods to handle strings.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class StringUtilities
{
    /** 
     * Constant for the empty <code>String</code>
     */
    public final static String EMPTY_STRING = "";
    
    /**
     * Tests if the given <code>String</code> is empty. A <code>String</code>
     * object is considered empty if it is either <code>null</code> or its
     * trimmed length is zero.
     * 
     * @param str the <code>String</code> to test
     * @return <code>true</code> if the given <code>String</code> is empty;
     * <code>false</code> otherwise
     */
    public static boolean isEmpty(String str) 
    {
        return (str == null || str.trim().length() == 0);
    }
    
    /**
     * Split a <code>String</code> into an array of <code>String[]</code>
     * using the space character as token.
     *  
     * @param s the string to split
     * @return the splitted string array
     */
    public static String[] split(String s)
    {
        return split(s, " ");   
    }
    
    /**
     * Split a <code>String</code> into an array of <code>String[]</code>.
     *  
     * @param s the string to split
     * @param token the character token
     * @return the splitted string array
     */
    public static String[] split(String s, String token)
    {
        if (s == null)
            return new String[0];
        
        StringTokenizer tokens = new StringTokenizer(s, token);
        String[] values = new String[tokens.countTokens()];
         
        for(int i=0; i<values.length; i++) 
            values[i] = tokens.nextToken();
        
        return values;
    }
    
    /**
     * Takes <code>value</code> and return its HEX value prefixed with zeros .
     *  
     * @param value the value to be converted
     * @param length the length to pad to.
     * @return a non-null String fixed width representing the HEX of value
     */
    public static String intToFixedLengthHex(int value, int length) 
    {
		String str = Integer.toHexString(value);
		StringBuilder paddedStr = new StringBuilder();
	
		if (str.length() < length) 
		{
		    for (int i = 0; i < length-str.length(); i ++)
		    	paddedStr.append('0');
		}
		paddedStr.append(str);
		
		return paddedStr.toString();
    }
    
    /**
     * Used to convert username-value, passwd or realm to 8859_1 encoding
     * if all chars in string are within the 8859_1 (Latin 1) encoding range.
     * 
     * @param str a non-null String
     * @param useUTF8 a boolean value set to <code>true</code> if UT8 is in use. 
     * @return a non-null String containing the correct character encoding
     * for username, paswd or realm.
     * @throws AuthException 
     */
    public static String stringTo_8859_1(String str, boolean useUTF8) 
    	throws AuthException
    {
		char[] buffer = str.toCharArray();
	
		try 
		{
		    if (useUTF8) 
		    {
		    	for( int i = 0; i< buffer.length; i++ ) 
		    	{
		    		if( buffer[i] > '\u00FF' )
		    			return str;
			    } 
		    }
	    
			return new String(str.getBytes("UTF8"), "8859_1");
		} 
	    catch (UnsupportedEncodingException e) 
	    {
	    	throw new AuthException("Cannot encode string in UTF8 or 8859-1 (Latin-1)");		
	    }
    }
    
    /**
     * Returns the occurence count of char <code>c</code> in a substring of the String  
     * <code>s</code> starting at position <code>start</code> and containing  
     * <code>length</code> chars. 
     * 
     * @param s the string to parse
     * @param c the character to search for
     * @return the occurence count
     */
    public static int occ(String s, char c, int start, int length)
    {    	
    	if (s==null)
    		return 0;
    	
    	int max = start+length;
    	if (start<0 || length<0 || max > s.length())
    		throw new IllegalArgumentException("Invalid bounds");
    
    	int count = 0;
    	for (int i=start;i<max;i++)
    		if (s.charAt(i) == c)
    			count++;
    			
    	return count;
    }
    
    /**
     * Parses digest-challenge string, extracting each token
     * and value(s)
     *
     * @param buf A non-null digest-challenge string.
     * @throws UnsupportedEncodingException 
     * @throws SaslException if the String cannot be parsed according to RFC 2831
     */
    public static HashMap<String, String> parseDirectives(byte[] buf) 
    	throws SaslException 
    {
		HashMap<String, String> map = new HashMap<String, String>();
		boolean gettingKey = true;
		boolean gettingQuotedValue = false;
		boolean expectSeparator = false;
		byte bch;

		ByteArrayOutputStream key = new ByteArrayOutputStream(10);
		ByteArrayOutputStream value = new ByteArrayOutputStream(10);

		int i = skipLws(buf, 0);
		while (i < buf.length) 
		{
			bch = buf[i];

			if (gettingKey) 
			{
				if (bch == ',') 
				{
					if (key.size() != 0)
						throw new SaslException("Directive key contains a ',':" + key);
					
					// Empty element, skip separator and lws
					i = skipLws(buf, i + 1);
				} 
				else 
				if (bch == '=') 
				{
					if (key.size() == 0)
						throw new SaslException("Empty directive key");

					gettingKey = false; // Termination of key
					i = skipLws(buf, i + 1); // Skip to next non whitespace

					// Check whether value is quoted
					if (i < buf.length) 
					{
						if (buf[i] == '"') 
						{
							gettingQuotedValue = true;
							++i; // Skip quote
						}
					} 
					else 
						throw new SaslException("Valueless directive found: " + key.toString());
				} 
				else 
				if (isLws(bch)) 
				{
					// LWS that occurs after key
					i = skipLws(buf, i + 1);

					// Expecting '='
					if (i < buf.length) 
					{
						if (buf[i] != '=')
							throw new SaslException("'=' expected after key: " + key.toString());
					} 
					else
						throw new SaslException("'=' expected after key: " + key.toString());
				} 
				else 
				{
					key.write(bch); // Append to key
					++i; // Advance
				}
			} 
			else 
			if (gettingQuotedValue) 
			{
				// Getting a quoted value
				if (bch == '\\') 
				{
					// quoted-pair = "\" CHAR ==> CHAR
					++i; // Skip escape
					if (i < buf.length) 
					{
						value.write(buf[i]);
						++i; // Advance
					} 
					else
						// Trailing escape in a quoted value
						throw new SaslException("Unmatched quote found for directive: "
																+ key.toString() + " with value: " + value.toString());
				} 
				else 
				if (bch == '"') 
				{
					// closing quote
					++i; // Skip closing quote
					gettingQuotedValue = false;
					expectSeparator = true;
				} 
				else 
				{
					value.write(bch);
					++i; // Advance
				}
			} 
			else 
			if (isLws(bch) || bch == ',') 
			{
				// Value terminated
				extractDirective(map, key.toString(), value.toString());
				key.reset();
				value.reset();
				gettingKey = true;
				gettingQuotedValue = expectSeparator = false;
				i = skipLws(buf, i + 1); // Skip separator and LWS
			} 
			else 
			if (expectSeparator) 
			{
				throw new SaslException("Expecting comma or linear whitespace after quoted string: \""
														+ value.toString() + "\"");
			} 
			else 
			{
				value.write(bch); // Unquoted value
				++i; // Advance
			}
		}

		if (gettingQuotedValue)
			throw new SaslException("Unmatched quote found for directive: "
													+ key.toString() + " with value: " + value.toString());

		// Get last pair
		if (key.size() > 0)
			extractDirective(map, key.toString(), value.toString());

		return map;
	}
    
    /**
     * Processes directive/value pairs from the digest-challenge and
     * fill out the provided map.
     * 
     * @param key A non-null String challenge token name.
     * @param value A non-null String token value.
     * @throws SaslException if either the key or the value is null or
     * if the key already has a value. 
     */
    private static void extractDirective(HashMap<String, String> map, String key, String value)
    	throws SaslException 
	{
    	if (map.get(key) != null)
    		throw new SaslException("Peer sent more than one " + key + " directive");
		else
			map.put(key, value);
	}

    /**
     * Is character a linear white space ?
     * LWS            = [CRLF] 1*( SP | HT )
     * Note that we're checking individual bytes instead of CRLF
     * 
     * @param b the byte to check
     * @return <code>true</code> if it's a linear white space
     */ 
    public static boolean isLws(byte b) 
    {
		switch (b) 
		{
			case 13:   // US-ASCII CR, carriage return
			case 10:   // US-ASCII LF, line feed
			case 32:   // US-ASCII SP, space
			case 9:     // US-ASCII HT, horizontal-tab
			    return true;
		}
		
		return false;
    }
    
    /**
     * Skip all linear white spaces
     */
    private static int skipLws(byte[] buf, int start) 
    {
    	int i;
    	
    	for (i = start; i < buf.length; i++) 
    	{
    		if (!isLws(buf[i]))
    			return i;
	    }

    	return i;
    }
    
    /**
     * <p>Replaces all occurrences of a String within another String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *)        = null
     * StringUtils.replace("", *, *)          = ""
     * StringUtils.replace("any", null, *)    = "any"
     * StringUtils.replace("any", *, null)    = "any"
     * StringUtils.replace("any", "", *)      = "any"
     * StringUtils.replace("aba", "a", null)  = "aba"
     * StringUtils.replace("aba", "a", "")    = "b"
     * StringUtils.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @see #replace(String text, String repl, String with, int max)
     * @param text  text to search and replace in, may be null
     * @param repl  the String to search for, may be null
     * @param with  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  <code>null</code> if null String input
     */
    public static String replace(String text, String repl, String with) {
        return replace(text, repl, with, -1);
    }

    /**
     * <p>Replaces a String with another String inside a larger String,
     * for the first <code>max</code> values of the search String.</p>
     *
     * <p>A <code>null</code> reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringUtils.replace(null, *, *, *)         = null
     * StringUtils.replace("", *, *, *)           = ""
     * StringUtils.replace("any", null, *, *)     = "any"
     * StringUtils.replace("any", *, null, *)     = "any"
     * StringUtils.replace("any", "", *, *)       = "any"
     * StringUtils.replace("any", *, *, 0)        = "any"
     * StringUtils.replace("abaa", "a", null, -1) = "abaa"
     * StringUtils.replace("abaa", "a", "", -1)   = "b"
     * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
     * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
     * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text  text to search and replace in, may be null
     * @param repl  the String to search for, may be null
     * @param with  the String to replace with, may be null
     * @param max  maximum number of values to replace, or <code>-1</code> if no maximum
     * @return the text with any replacements processed,
     *  <code>null</code> if null String input
     */
    public static String replace(String text, String repl, String with, int max) {
        if (isEmpty(text) || isEmpty(repl) || with == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(repl, start);
        if (end == -1) {
            return text;
        }
        int replLength = repl.length();
        int increase = with.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuffer buf = new StringBuffer(text.length() + increase);
        while (end != -1) {
            buf.append(text.substring(start, end)).append(with);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(repl, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }
    
    /**
     * <p>Strips whitespace from the start and end of a String.</p>
     *
     * <p>This is similar to {@link #trim(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.</p>
     *
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str  the String to remove whitespace from, may be null
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str) {
        return strip(str, null);
    }
    
    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.
     * Alternatively use {@link #strip(String)}.</p>
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str, String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }
    
    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A <code>null</code> input String returns <code>null</code>.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is <code>null</code>, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != -1)) {
                end--;
            }
        }
        return str.substring(0, end);
    }
}