/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005 Feed'n Read Development Team                             *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/

package org.mailster.gui.prefs.utils;

import java.text.MessageFormat;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.mailster.gui.Messages;
import org.mailster.util.StringUtilities;

/**
 * Models a container for localized language specific resources.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class LanguageResource implements Comparable<LanguageResource>
{
    /**
     * Predefined <code>String</code> constant for unknown keys which are not
     * part of the <code>LanguageResource</code>
     */
    public final static String KEY_NOT_FOUND = "Key not found";

    /**
     * Predefined key to access the name of the translator of a
     * <code>LanguageResource</code>
     */
    private final static String TRANSLATOR_KEY = "translator";

    /**
     * Predefined key to access the date of translation of a
     * <code>LanguageResource</code>
     */
    private final static String TRANSLATION_DATE = "translation_date";
    
    /**
     * Predefined key to access the translation version of a
     * <code>LanguageResource</code>
     */
    private final static String TRANSLATION_VERSION = "translation_version";

    /**
     * The <code>ResourceBundle</code> associated to the
     * <code>LanguageResource</code>
     */
    private ResourceBundle bundle;

    /**
     * Creates a new <code>LanguageResource</code> which works on the
     * specified <code>ResourceBundle</code>, i.e. extracts the messages from
     * it.
     * 
     * @param bundle the associated <code>ResourceBundle</code>
     */
    public LanguageResource(ResourceBundle bundle)
    {
        if (bundle != null)
            this.bundle = bundle;
        else
            throw new IllegalArgumentException(
                    "No valid ResourceBundle specified");
    }

    /**
     * Returns the lowercase ISO 639 code of this <code>LanguageResource</code>,
     * e.g. "en" for English. If a country code is available then it will be 
     * postfixed to the ISO code e.g. "pt_br" for Brazil.
     * 
     * @return the country string or the lowercase ISO 639 code
     */
    public String getFullISOLanguage()
    {
        String country=bundle.getLocale().getCountry();
        
        if (StringUtilities.isEmpty(country))
            return getISOLanguage();
        else
            return getISOLanguage()+"_"+country;
    }
    
    /**
     * Gets the lowercase ISO 639 code of this <code>LanguageResource</code>,
     * e.g. "en" for English.
     * 
     * @return the lowercase ISO 639 code of this <code>LanguageResource</code>
     */
    public String getISOLanguage()
    {
        return bundle.getLocale().getLanguage();
    }
    
    /**
     * Gets the version of this <code>LanguageResource</code>,
     * e.g. the version of the application with which the resource is compliant.
     * 
     * @return the version of this <code>LanguageResource</code>
     */
    public String getVersion()
    {
    	try
        {
            return bundle.getString(TRANSLATION_VERSION);
        }
        catch (MissingResourceException mex)
        {
            return "-";
        }
    }

    /**
     * Gets the full language identifier of this <code>LanguageResource</code>,
     * e.g. "English". If possible, the name returned will be localized
     * according to the current default <code>LanguageResource</code>. For
     * example, if the <code>LanguageResource</code> is fr and the default
     * <code>LanguageResource</code> is en, getLanguage(LanguageResource) will
     * return "French"; if the <code>LanguageResource</code> is en and the
     * default <code>LanguageResource</code> is fr,
     * getLanguage(LanguageResource) will return "anglais".
     * 
     * @return the full language identifier of this
     *         <code>LanguageResource</code>
     * @see #getLanguage(LanguageResource)
     */
    public String getLanguage()
    {
        String language = bundle.getLocale().getDisplayLanguage();

        return (language == null || language.length() == 0 ? Locale
                .getDefault().getDisplayLanguage() : language);
    }

    /**
     * Gets the full language identifier of this <code>LanguageResource</code>,
     * e.g. "English". If possible, the name returned will be localized
     * according to <code>resource</code>. For example, if the
     * <code>LanguageResource</code> is fr and <code>resource</code> is en,
     * getLanguage(LanguageResource) will return "French"; if the
     * <code>LanguageResource</code> is en and <code>resource</code> is fr,
     * getLanguage(LanguageResource) will return "anglais".
     * 
     * @param resource the <code>LanguageResource</code> to use for getting
     *            the language
     * @return the full language identifier of this
     *         <code>LanguageResource</code>
     * @see #getLanguage()
     */
    public String getLanguage(LanguageResource resource)
    {
        if (resource != null)
        {
            String language = bundle.getLocale().getDisplayLanguage(
                    Messages.getLocale());

            return (language == null || language.length() == 0 ? Locale
                    .getDefault().getDisplayLanguage() : language);
        }
        else
            return getLanguage();
    }

    /**
     * Gets the name of the translator of this <code>LanguageResource</code>.
     * 
     * @return the name of the translator of this <code>LanguageResource</code>
     */
    public String getTranslator()
    {
        try
        {
            return bundle.getString(TRANSLATOR_KEY);
        }
        catch (MissingResourceException mex)
        {
            return "-";
        }
    }

    /**
     * Gets the date of translation of this <code>LanguageResource</code>.
     * 
     * @return the the date of translation of this <code>LanguageResource</code>
     */
    public String getTranslationDate()
    {
        try
        {
            return bundle.getString(TRANSLATION_DATE);
        }
        catch (MissingResourceException mex)
        {
            return "-";
        }
    }

    /**
     * Gets a localized <code>String</code> from this
     * <code>LanguageResource</code> matching the specified <code>key</code>.
     * 
     * @param key the key of the localized <code>String</code> to access
     * @return the localized <code>String</code>; {@link #KEY_NOT_FOUND} if
     *         the key is not part of this <code>LanguageResource</code>
     */
    public String getString(String key)
    {
        try
        {
            return bundle.getString(key);
        }
        catch (MissingResourceException mre)
        {
            return KEY_NOT_FOUND;
        }
    }

    /**
     * Gets a localized formatted <code>String</code> from this
     * <code>LanguageResource</code> matching the specified <code>key</code>.
     * 
     * @param key the key of the localized <code>String</code> to access
     * @param arguments a list of parameters to use for formatting
     * @return the localized formatted <code>String</code>;
     *         {@link #KEY_NOT_FOUND} if the key is not part of this
     *         <code>LanguageResource</code>
     */
    public String getFormattedString(String key, Object[] arguments)
    {
        return MessageFormat.format(getString(key), arguments);
    }

    /**
     * Converts this <code>LanguageResource</code> to a human readable
     * representation.
     * 
     * @return the language of this <code>LanguageResource</code> as returned
     *         by the {@link #getLanguage()} method
     *         </p>
     */
    public String toString()
    {
        return getLanguage();
    }

    /**
     * Tests if this <code>LanguageResource</code> equals the specified
     * <code>obj</code> which also has to be a <code>LanguageResource</code>.
     * Two <code>LanguageResources</code> are considered equal if both have
     * the same language as returned by the {@link #getLanguage()} method.
     * 
     * @param obj the <code>Object</code> to test for equality
     * @return <code>true</code> if this <code>LanguageResource</code>
     *         equals the specified <code>obj</code>; <code>false</code>
     *         otherwise
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof LanguageResource)
            return getFullISOLanguage().equals(((LanguageResource) obj)
                    .getFullISOLanguage());
        else
            return false;
    }
    

    @Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getFullISOLanguage().hashCode();
		return result;
	}

    /**
     * Compares this <code>LanguageResource</code> with the specified object
     * for order. Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the specified object.
     * <p>
     * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt>
     * designates the mathematical <i>signum</i> function, which is defined to
     * return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt> according to
     * whether the value of <i>expression</i> is negative, zero or positive.
     * 
     * @param obj the <code>Object</code> to be compared.
     * @return a negative integer, zero, or a positive integer as this
     *         <code>LanguageResource</code> is less than, equal to, or
     *         greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *             from being compared to this Object.
     */
    public int compareTo(LanguageResource obj)
    {
        if (obj != null)
            return getLanguage().compareTo(obj.getLanguage());
        else
            return -1;
    }
}
