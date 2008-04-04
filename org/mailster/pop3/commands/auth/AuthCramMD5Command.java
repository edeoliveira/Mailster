package org.mailster.pop3.commands.auth;

import org.mailster.util.md5.MD5;

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
 * AuthCramMD5Command.java - The POP3 AUTH CRAM-MD5 command (see RFCs 1734 & 2195).
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class AuthCramMD5Command extends AuthCramCommand
{
    private static MD5 md5 = new MD5();

    /**
     * The HMAC_MD5, according to RFC 2104, transform looks like :
     *
     * MD5(K XOR opad, MD5(K XOR ipad, text))
     *
     * where K is a n byte key, ipad is the byte 0x36 repeated 64 times
     * opad is the byte 0x5c repeated 64 times
     * 
     * @param text is the data being protected
     * @param secret is the secret key
     * 
     * @return the hex string representation of the HMAC_MD5 transformation
     */
    public String hmacHash(byte[] text, byte[] secret)
    {    	
    	return hmacMD5(text, secret);
    }
    
    public static String hmacMD5(byte[] text, byte[] secret)
	{
        byte[] k_ipad = new byte[64];
        byte[] k_opad = new byte[64];
        
        byte[] key;
        
        // If key is longer than 64 bytes then reset it to key = MD5(key)
        if (secret.length > 64) 
        {
            synchronized(md5)
            {
                md5.Init();
                md5.Update(secret);
                key = md5.Final();
            }
        }
        else
            key = secret;

        // Stores key in pads and XOR it with ipad and opad values
        for(int i=0;i<64;i++) 
        {
            if (i<key.length) 
            {
            	k_ipad[i] = (byte) (key[i] ^ 0x36);
            	k_opad[i] = (byte)(key[i] ^ 0x5c);
            }
            else 
            {
            	k_ipad[i] = 0x36;
            	k_opad[i] = 0x5c;            	
            }          
        }

        synchronized(md5)
        {
            // Performs inner MD5
            md5.Init();
            md5.Update(k_ipad);
            md5.Update(text);
            byte[] mid_buffer = md5.Final();
            
            // Performs outer MD5
            md5.Init();
            md5.Update(k_opad);
            md5.Update(mid_buffer);
            
            return md5.asHex();
        }
    }
}