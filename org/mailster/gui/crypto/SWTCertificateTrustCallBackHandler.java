package org.mailster.gui.crypto;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.mailster.MailsterSWT;
import org.mailster.crypto.UICertificateTrustCallBackHandler;
import org.mailster.gui.SWTHelper;

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
 * SWTCertificateTrustCallBackHandler.java - SWT Implementation of the 
 * <code>UICertificateTrustCallBackHandler</code> interface.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SWTCertificateTrustCallBackHandler
        extends UICertificateTrustCallBackHandler
{
    public void run()
    {
        SWTHelper.getDisplay().asyncExec(new Runnable() 
        {
            public void run()
            {
                TrustDialog dialog = new TrustDialog(MailsterSWT.getInstance().getShell(), chain);
                cancelled = dialog.open() != IDialogConstants.OK_ID;
                returnCode = dialog.getChainAcceptationReturnCode();
                sem.release();
            }
        });
    }
}
