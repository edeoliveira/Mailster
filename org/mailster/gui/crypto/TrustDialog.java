package org.mailster.gui.crypto;

import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.mailster.crypto.CertificateUtilities;
import org.mailster.gui.Messages;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;

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
 * TrustDialog.java - A dialog that asks for trusting degree against a certificate.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class TrustDialog extends MessageDialog 
{   
    public final static int ACCEPT_CERTIFICATE_CHAIN            = 1;
    public final static int TEMPORARY_ACCEPT_CERTIFICATE_CHAIN  = 2;
    public final static int REJECT_CERTIFICATE_CHAIN            = 4;
    
    /**
     * The certificate chain to display.
     */
    private Certificate[] chain;  
    
    /**
     * The return code indicating if the certificate chain is accepted.
     */
    private int chainAcceptationReturnCode = TEMPORARY_ACCEPT_CERTIFICATE_CHAIN;
    
    /**
     * Creates a new <code>TrustDialog</code> instance.
     * 
     * @param shell the parent shell
     */
    public TrustDialog(Shell shell, Certificate[] chain) 
    {
        super(shell, Messages.getString("MailsterSWT.dialog.trust.title"), //$NON-NLS-1$ 
                null, buildMessage(chain), MessageDialog.WARNING, 
                new String[] {Messages.getString("MailsterSWT.dialog.about.ok.label"), //$NON-NLS-1$
            Messages.getString("MailsterSWT.dialog.about.cancel.label")}, 1); //$NON-NLS-1$
        setShellStyle(getShellStyle() & ~SWT.MENU);
        
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
        
        this.chain = chain;
    }    
    
    private static String buildMessage(Certificate[] chain)
    {
        String name = CertificateUtilities.getCN(((X509Certificate)chain[0]).getSubjectDN().getName());
        return MessageFormat.format(Messages.getString("MailsterSWT.dialog.trust.label"), //$NON-NLS-1$ 
        		name, name, name);
    }
    
    /**
     * Configures the <code>Shell</code> representing this
     * <code>TrustDialog</code>.
     * 
     * @param shell the<code>Shell</code> to configure
     */
    protected void configureShell(Shell shell) 
    {   
        super.configureShell(shell);
    }

    public void create()
    {
        super.create();        
        getShell().setSize(getShell().computeSize(500, SWT.DEFAULT, true));
        DialogUtils.centerShellOnParentShell(getShell());
    }
    
    protected Control createCustomArea(Composite parent) 
    {
        Composite composite = new Composite(parent, SWT.NONE);
        final Composite c = composite;
        composite.setLayout(LayoutUtils.createGridLayout(1, false, 0, 0, 4, 4, 40, 0, 4, 0));
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 500;
        composite.setLayoutData(gd);        
        
        final Button showCert = new Button(composite, SWT.PUSH);
        showCert.setText(Messages.getString("MailsterSWT.dialog.trust.showCert")); //$NON-NLS-1$
        showCert.forceFocus();
        showCert.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, GridData.BEGINNING, 
                false, false, 1, 1));
        
        final Button optionAccept = new Button (composite, SWT.RADIO);
        optionAccept.setText(Messages.getString("MailsterSWT.dialog.trust.accept")); //$NON-NLS-1$
        optionAccept.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, GridData.BEGINNING, 
                false, false, 1, 1));
        optionAccept.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt)
            {
                if (evt.widget == optionAccept)
                    chainAcceptationReturnCode = ACCEPT_CERTIFICATE_CHAIN;
            }
        });
        
        final Button optionTemp = new Button (composite, SWT.RADIO);
        optionTemp.setText (Messages.getString("MailsterSWT.dialog.trust.temporaryAccept")); //$NON-NLS-1$
        optionTemp.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, GridData.BEGINNING, 
                false, false, 1, 1));
        optionTemp.setSelection(true);
        
        final Button optionReject = new Button (composite, SWT.RADIO);
        optionReject.setText(Messages.getString("MailsterSWT.dialog.trust.reject")); //$NON-NLS-1$
        optionReject.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, GridData.BEGINNING, 
                false, false, 1, 1));
        
        SelectionAdapter selAdapter = new SelectionAdapter() 
        {
            public void widgetSelected(SelectionEvent evt)
            {
                if (evt.widget == showCert)
                    (new CertificateDialog(c.getShell(), chain)).open();
                else
                if (evt.widget == optionAccept)
                    chainAcceptationReturnCode = ACCEPT_CERTIFICATE_CHAIN;
                else
                if (evt.widget == optionTemp)
                    chainAcceptationReturnCode = TEMPORARY_ACCEPT_CERTIFICATE_CHAIN;
                else
                if (evt.widget == optionReject)
                    chainAcceptationReturnCode = REJECT_CERTIFICATE_CHAIN;
            }        
        };
        showCert.addSelectionListener(selAdapter);
        optionAccept.addSelectionListener(selAdapter);
        optionTemp.addSelectionListener(selAdapter);
        optionReject.addSelectionListener(selAdapter);
        
        return composite;
    }
    
    public int getChainAcceptationReturnCode()
    {
        return chainAcceptationReturnCode;
    }    
}