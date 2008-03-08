package org.mailster.server;

import java.io.File;
import java.nio.charset.Charset;

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
 * MailsterConstants.java - Interface purpose is to group all server constants.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public interface MailsterConstants 
{
	public static final String DEFAULT_CHARSET_NAME = "ISO-8859-1"; //$NON-NLS-1$
	
	public static final Charset DEFAULT_CHARSET = 
		Charset.forName(DEFAULT_CHARSET_NAME); //$NON-NLS-1$
	
    public final static String USER_DIR = 
    	System.getProperty("user.dir").replace(File.separatorChar, '/'); //$NON-NLS-1$ //$NON-NLS-2$
}
