package org.mailster.pop3.commands.auth;

import javax.security.sasl.SaslException;

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
 * AuthException.java - This class wraps an SaslException. The original message is 
 * logged to a SLF4J logger while a generic message is passed to the constructor. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AuthException extends SaslException
{
	private final static Logger log = LoggerFactory.getLogger(AuthException.class);
    private final static long serialVersionUID = -4012814883102061024L;

    public AuthException(String message)
    {
        super("Authentication process failed");
        log.debug(message);
    }
    
    public AuthException(String message, Throwable ex)
    {
        super("Authentication process failed", ex);
        log.debug(message);
    }
}

