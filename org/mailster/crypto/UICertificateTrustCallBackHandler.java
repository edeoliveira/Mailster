package org.mailster.crypto;

import java.security.cert.Certificate;
import java.util.concurrent.Semaphore;

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
 * See&nbsp; <a href="http://mailster.sourceforge.net" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * UICertificateTrustCallBackHandler.java - This interface allows a UI component
 * to ask to the user if he wants to trust the peer certificate.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public abstract class UICertificateTrustCallBackHandler
    implements Runnable
{
    public final static int ACCEPT_CERTIFICATE_CHAIN            = 1;
    public final static int TEMPORARY_ACCEPT_CERTIFICATE_CHAIN  = 2;
    public final static int REJECT_CERTIFICATE_CHAIN            = 4;

    protected int returnCode;
    protected boolean cancelled;
    protected Semaphore sem;
    protected Certificate[] chain;
    
    public int getReturnCode()
    {
        return returnCode;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }    
    
    public void setCallBackParameters(Semaphore sem, Certificate[] chain)
    {
        this.sem = sem;
        this.chain = chain;
        sem.acquireUninterruptibly();
    }
}
