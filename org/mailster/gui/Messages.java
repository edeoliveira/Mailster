package org.mailster.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * Messages.java - Handles localisation of UI.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class Messages
{
	/**
	 * The current locale. Defaults to Locale.ENGLISH.
	 */
	private static Locale locale = Locale.ENGLISH;

    /**
     * The default ENGLISH resource bundle.
     */
    private static ResourceBundle defaultBundle = 
    	ResourceBundle.getBundle("org.mailster.gui.resources.messages", locale);

    /**
     * The resource bundle.
     */
    private static ResourceBundle bundle = defaultBundle;
    
    private Messages()
    {
    }
    
    private static void setupResourceBundle()
    {
		if (locale != null)
		{
			bundle = ResourceBundle
    			.getBundle("org.mailster.gui.resources.messages", locale); //$NON-NLS-1$
		
			if (bundle == null)
				bundle = defaultBundle;
    	}
		else
			bundle = defaultBundle;
    }
    
    /**
     * Loads a string from the resource bundle file.
     * 
     * @param key the key to load the message string
     * @return the string or the key if string wasn't found
     */
    public static String getString(String key)
    {
        try
        {
        	String s = bundle.getString(key);
            if ("".equals(s))
            	s = defaultBundle.getString(key);
            
            return s;
        }
        catch (MissingResourceException e)
        {
        	if (bundle != defaultBundle)
        	{
        		try
        		{
        			defaultBundle.getString(key);
        		}
        		catch (MissingResourceException mex) {}
        	}
        	
        	return "[" + key + "]";
        }
    }

	public static Locale getLocale() 
	{
		return locale;
	}

	/**
	 * NOTE : Will not reload the already loaded strings.
	 */
	public static void setLocale(Locale l) 
	{
		Messages.locale = l;
		setupResourceBundle();
	}
}
