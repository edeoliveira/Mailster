package org.mailster.gui;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.mailster.gui.prefs.utils.LanguageResource;
import org.mailster.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @version $Revision$, $Date$
 */
public class Messages
{
    private static final Logger log = LoggerFactory.getLogger(Messages.class);
    
    /** 
     * The default base name of the language resources, a fully qualified class 
     * name.
     */
    private final static String DEFAULT_RESOURCE_BASE = 
        "org.mailster.gui.resources.messages";
    
    /**
     * Stores the single <code>LanguageResource</code> objects in a sorted
     * <code>TreeMap</code> data structure
     */
    private final static TreeMap<String, LanguageResource> LANGUAGE_RESOURCES;
    
    /**
     * <p>
     * Static initialization method that tests for each <code>Locale</code>
     * available to the used vm if there is an existing and valid
     * <code>ResourceBundle</code> for this specific <code>Locale</code>s
     * language. If so the <code>ResourceBundle</code> is being loaded and
     * wrapped in a <code>LanguageResource</code> object.
     * </p>
     * 
     * @since 0.3.0
     */
    static 
    {
        LANGUAGE_RESOURCES = new TreeMap<String, LanguageResource>();

        for (Locale l : Locale.getAvailableLocales()) 
        {
            try 
            {
                ResourceBundle bundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_BASE, l);
                String lang = bundle.getLocale().getLanguage();
                String country = bundle.getLocale().getCountry();
                
                if (!StringUtilities.isEmpty(country))
                    lang=lang+"_"+country;
                
                if (LANGUAGE_RESOURCES.get(lang) == null)
                    LANGUAGE_RESOURCES.put(lang, new LanguageResource(bundle));
                
            } 
            catch (Exception e) 
            {               
                // No language file available for this specific locale 
                log.debug("No language file available for this specific locale - {}", l);
            }
        }
    }
    
	/**
	 * The current locale. Defaults to Locale.ENGLISH.
	 */
	private static Locale locale = Locale.ENGLISH;

    /**
     * The default resource bundle is the one corresponding to local 
     * <code>Locale</code>.
     */
    private static ResourceBundle defaultBundle = 
    	ResourceBundle.getBundle(DEFAULT_RESOURCE_BASE, locale);

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
    			.getBundle(DEFAULT_RESOURCE_BASE, locale);
		
			if (bundle == null || !bundle.getLocale().equals(locale))
				bundle = defaultBundle;
    	}
		else
			bundle = defaultBundle;
    }
    
    /**
     * Gets all available <code>LanguageResource</code>s
     * 
     * @return an array of <code>LanguageResource</code> objects holding
     * all available resources
     */
    public static LanguageResource[] getAvailableLanguageResources() 
    {
        return ((LanguageResource[])
                LANGUAGE_RESOURCES.values().toArray(new LanguageResource[0]));
    }
    
    /**
     * Gets the <code>LanguageResource</code> mapped to the specified ISO 639
     * two letter language code (e.g. "de" for german, "en" for english)
     * 
     * @param language the ISO 639 twoe letter language code for which
     * to get the mapped <code>LanguageResource</code>
     *
     * @return the <code>LanguageResource</code> mapped to the specified ISO 639
     * two letter language code (e.g. "de" for german, "en" for english);
     * <code>null</code> if no such <code>LanguageResource</code> is available
     * for the specified language
     */
    public static LanguageResource getLanguageResource(String language) 
    {
        return ((LanguageResource) LANGUAGE_RESOURCES.get(language));
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
        			return defaultBundle.getString(key);
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
		locale = l;
		setupResourceBundle();
	}
}
