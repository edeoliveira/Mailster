package org.mailster.gui.crypto;

import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.misc.NetscapeRevocationURL;
import org.bouncycastle.asn1.misc.VerisignCzagExtension;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMESigned;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.mailster.crypto.CertificateUtilities;
import org.mailster.crypto.CertificateUtilities.DigestAlgorithm;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.DialogUtils;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.util.DateUtilities;

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
 * CertificateDialog.java - A dialog that shows informations about certificates.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class CertificateDialog extends Dialog 
{   
    static 
    {
        if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * The message that contains the certificate to display.
     */
    private MimeMessage message;
    
    /**
     * The certificate chain to display.
     */
    private Certificate[] chain;  
    
    /**
     * <code>true</true> if validation messages should be shown.
     */
    private boolean showValidationMessages;
    
    /**
     * Creates a new <code>CertificateDialog</code> instance.
     * 
     * @param shell the parent shell
     */
    public CertificateDialog(Shell shell, MimeMessage msg) 
    {
        super(shell);
        this.message = msg;
    }
    
    /**
     * Creates a new <code>CertificateDialog</code> instance.
     * 
     * @param shell the parent shell
     */
    public CertificateDialog(Shell shell, Certificate[] chain) 
    {
        super(shell);
        this.chain = chain;
    }    
    
    /**
     * Configures the <code>Shell</code> representing this
     * <code>CertificateDialog</code>.
     * 
     * @param shell the<code>Shell</code> to configure
     */
    protected void configureShell(Shell shell) 
    {             
        super.configureShell(shell);
        shell.setText(Messages.getString("MailsterSWT.dialog.certificate.title")); //$NON-NLS-1$
        shell.setImage(SWTHelper.loadImage("smime.gif"));
        shell.setSize(520, 600);
        DialogUtils.centerShellOnParentShell(shell);     
    }
    
    /**
     * Creates the contents of this <code>CertificateDialog</code>.
     * 
     * @param parent the parent <code>Composite</code> in which to embed the
     * created contents
     */
    protected Control createContents(Composite parent) 
    {
        // Create the top level composite for the dialog
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);        
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));        
        applyDialogFont(composite);
        
        // Initialize the dialog units
        this.initializeDialogUnits(composite);
        
        // Create the dialog area and button bar
        dialogArea = createDialogArea(composite);
        buttonBar = createButtonBar(composite);
        
        return composite;
    }    
    
    /**
     * Adds the buttons to this <code>CertificateDialog</code>'s button bar.
     * 
     * @param parent the button bar <code>Composite</code>
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) 
    {
        Button okButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.getString("MailsterSWT.dialog.about.ok.label"), true); //$NON-NLS-1$
        okButton.forceFocus();
        setButtonLayoutData(okButton);        
    }    
    
    /**
     * Creates and returns the contents of the upper part of this
     * <code>CertificateDialog</code> (above the button bar).
     * 
     * @param parent the parent <code>Composite</code> to contain 
     * the dialog area
     * @return the dialog area control
     */
    protected Control createDialogArea(Composite parent) 
    {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        CTabFolder folder = new CTabFolder(dialogArea, SWT.BORDER);
        folder.setSimple(false);
        folder.setMRUVisible(false);
        Color[] tabGradient = SWTHelper.getGradientColors(5,
                new Color(SWTHelper.getDisplay(), 0, 84, 227),
                new Color(SWTHelper.getDisplay(), 61, 149, 255));
        folder.setSelectionBackground(tabGradient,
                new int[] { 10, 20, 30, 40 }, true);
        folder.setSelectionForeground(SWTHelper.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));
        
        folder.setLayoutData(LayoutUtils.createGridData(GridData.FILL, GridData.FILL, 
                        true, true, 1, 1));
        folder.setBackground(SWTHelper.createColor(244, 243, 238));
        
        CTabItem item = new CTabItem(folder, SWT.NONE);
        createGeneralTabItem(folder, item);
        folder.setSelection(item);
        
        item = new CTabItem(folder, SWT.NONE);
        createDetailTabItem(folder, item);
        
        // Create a container for the separator
        Composite separatorContainer = new Composite(parent, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        separatorContainer.setLayoutData(gd);
        separatorContainer.setLayout(
                LayoutUtils.createGridLayout(1, false, 0, 0, 0, 0, 0, 0, 5, 5));

        // Create the Separator
        Label separator = new Label(separatorContainer, SWT.SEPARATOR
                | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));                       
        
        return dialogArea;                
    }
    
    private void addCertificateFieldLabel(Composite composite, String label, String value)
    {
        Label lbl = new Label(composite, SWT.WRAP);
        lbl.setText(label);
        lbl.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.BEGINNING, false, false, 1, 1));
        
        lbl = new Label(composite, SWT.WRAP);
        if (value == null || "".equals(value))
            lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.fieldNotPresent")); //$NON-NLS-1$
        else
            lbl.setText(value);
        lbl.setLayoutData(LayoutUtils.createGridData(GridData.BEGINNING, 
        		GridData.BEGINNING, false, false, 1, 1));
    }
    
    private X509Certificate getSubjectCertificate()
    {
        if (message != null)
        {
            try
            {
                SMIMESigned signed = new SMIMESigned((MimeMultipart)message.getContent());
                Iterator<?> it = signed.getSignerInfos().getSigners().iterator();
                CertStore certsAndCRLs = signed.getCertificatesAndCRLs("Collection", "BC");
                
                while (it.hasNext()) 
                {
                    SignerInformation signer = (SignerInformation) it.next();
                    return (X509Certificate) certsAndCRLs.getCertificates(signer.getSID()).iterator().next();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
            return (X509Certificate) chain[0];
        
        return null;
    }
    
    private Object[] reverseArray(Object[] array)
    {
        if (array == null || array.length < 2)
            return array;
        
        for(int i=0,max= array.length / 2;i<max;i++)
        {
            int end = array.length-(i+1);
            Object obj = array[end];
            array[end] = array[i];
            array[i] = obj;
        }
        
        return array;
    }
    
    private void parseCertificateChains(TreeItem root)
    {
        if (message != null)
        {
        	root.setText(Messages.getString("MailsterSWT.dialog.certificate.signers")); //$NON-NLS-1$
            
        	try
            {
                SMIMESigned signed = new SMIMESigned((MimeMultipart) message.getContent());
                Iterator<?> it = signed.getSignerInfos().getSigners().iterator();
                CertStore certsAndCRLs = signed.getCertificatesAndCRLs("Collection", "BC");
                
                while (it.hasNext()) 
                {
                    SignerInformation signer = (SignerInformation) it.next();
                    Collection<? extends Certificate> l = certsAndCRLs.getCertificates(signer.getSID());
                    
                    generateCertificateChainTree(root, l.toArray(new X509Certificate[l.size()]));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
        	root.setText(Messages.getString("MailsterSWT.dialog.certificate.peer")); //$NON-NLS-1$
        	
	        X509Certificate[] c = new X509Certificate[chain.length];
	        for (int i=0,max=chain.length;i<max;i++)
	            c[i] = (X509Certificate) chain[i];
	        
	        generateCertificateChainTree(root, (X509Certificate[]) reverseArray(c));
        }
    }
    
    private void createGeneralTabItem(CTabFolder folder, CTabItem item)
    {
        item.setText(Messages.getString("MailsterSWT.dialog.certificate.generalTab")); //$NON-NLS-1$
        item.setImage(SWTHelper.loadImage("smime.gif"));
        
        Composite composite = new Composite(folder, SWT.BORDER);
        composite.setLayout(
                LayoutUtils.createGridLayout(2, false, 5, 5, 0, 0, 0, 0, 4, 20));
        composite.setBackground(SWTHelper.createColor(244, 243, 238));
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        
        item.setControl(composite);
        
        Label lbl = new Label(composite, SWT.WRAP);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.issuedTo")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        
        X509Certificate cert = getSubjectCertificate();
        String dn = cert.getSubjectDN().getName();
        
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.CN"), //$NON-NLS-1$ 
                CertificateUtilities.getField("CN", dn));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.O"), //$NON-NLS-1$ 
                CertificateUtilities.getField("O", dn));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.OU"), //$NON-NLS-1$ 
                CertificateUtilities.getField("OU", dn));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.serialNumber"), //$NON-NLS-1$ 
        		CertificateUtilities.byteArrayToString(cert.getSerialNumber().toByteArray(), false));

        lbl = new Label(composite, SWT.WRAP);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.issuedBy")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        
        dn = cert.getIssuerDN().getName();
        
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.CN"), //$NON-NLS-1$ 
                CertificateUtilities.getField("CN", dn));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.O"), //$NON-NLS-1$ 
                CertificateUtilities.getField("O", dn));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.OU"), //$NON-NLS-1$ 
                CertificateUtilities.getField("OU", dn));
        
        lbl = new Label(composite, SWT.WRAP);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.validity")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.issuedOn"), //$NON-NLS-1$ 
                DateUtilities.df.format(cert.getNotBefore()));
        addCertificateFieldLabel(composite, Messages.getString("MailsterSWT.dialog.certificate.expiresOn"), //$NON-NLS-1$ 
                DateUtilities.df.format(cert.getNotAfter()));
        
        lbl = new Label(composite, SWT.WRAP);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.fingerprints")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        
        try
        {
            addCertificateFieldLabel(composite, 
            		Messages.getString("MailsterSWT.dialog.certificate.fingerprint.sha1"), //$NON-NLS-1$ 
                    CertificateUtilities.toFingerprint(cert, DigestAlgorithm.SHA1));
            addCertificateFieldLabel(composite, 
            		Messages.getString("MailsterSWT.dialog.certificate.fingerprint.md5"), //$NON-NLS-1$ 
                    CertificateUtilities.toFingerprint(cert, DigestAlgorithm.MD5));
        }
        catch (Exception e) {}
        
        if (showValidationMessages)
        {
	        Label resultLabel = new Label(composite, SWT.WRAP);
	        resultLabel.setText(Messages.getString("MailsterSWT.dialog.certificate.validation")); //$NON-NLS-1$
	        resultLabel.setFont(SWTHelper.SYSTEM_FONT_BOLD);
	        resultLabel.setLayoutData(LayoutUtils.createGridData(
	                GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));
	        
	        Text results = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.VERTICAL);
	        results.setEditable(false);
	        results.setLayoutData(LayoutUtils.createGridData(
	                GridData.FILL, GridData.FILL, false, true, 2, 1));
	        results.setText("");
        }
    }
    
    private void createDetailTabItem(CTabFolder folder, CTabItem item)
    {
        item.setText(Messages.getString("MailsterSWT.dialog.certificate.detailsTab")); //$NON-NLS-1$
        item.setImage(SWTHelper.loadImage("hierarchy.gif"));
        
        Composite composite = new Composite(folder, SWT.BORDER);
        composite.setLayout(
                LayoutUtils.createGridLayout(1, false, 5, 5, 0, 0, 0, 0, 4, 0));
        composite.setBackground(SWTHelper.createColor(244, 243, 238));
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        item.setControl(composite);
        
        Label lbl = new Label(composite, SWT.WRAP);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.hierarchy")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, false, false, 1, 1));
        
        Tree certificateTree = new Tree(composite, SWT.BORDER);
        certificateTree.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL_VERTICAL, true, false, 1, 1, SWT.DEFAULT, 80));
        
        lbl = new Label(composite, SWT.NONE);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.fields")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, false, false, 1, 1));
        
        Tree valuesTree = new Tree(composite, SWT.BORDER);        
        valuesTree.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 1, 2));
        
        lbl = new Label(composite, SWT.NONE);
        lbl.setText(Messages.getString("MailsterSWT.dialog.certificate.fieldValue")); //$NON-NLS-1$
        lbl.setFont(SWTHelper.SYSTEM_FONT_BOLD);
        lbl.setLayoutData(LayoutUtils.createGridData(
                GridData.BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, false, false, 1, 1));
        
        Text valueText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.VERTICAL);
        valueText.setEditable(false);
        
        valueText.setFont(SWTHelper.createFont(new FontData("Courier New", 8, SWT.NONE)));
        valueText.setBackground(SWTHelper.createColor(235, 235, 228));
        valueText.setLayoutData(LayoutUtils.createGridData(
                GridData.FILL, GridData.FILL, true, true, 1, 1));
        
        // Generate data
        try
        {
            generateCertificateChainTree(certificateTree, valuesTree, valueText);
            TreeItem root = certificateTree.getItem(0);
            if (root != null)
                certificateTree.setSelection(root);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } 
    }
    
    private void generateCertificateChainTree(TreeItem root, Certificate[] certs)
    {
        TreeItem last = root;
        Display display = SWTHelper.getDisplay();
        Color success = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        Color failure = display.getSystemColor(SWT.COLOR_RED);
        
        for (Certificate cert : certs) 
        {
            TreeItem current = new TreeItem(last, SWT.NONE);
            if (cert instanceof X509Certificate)
            {
            	current.setText(CertificateUtilities.getCN(((X509Certificate)cert).getSubjectDN().getName()));
                try
                {
                    ((X509Certificate)cert).checkValidity();
                    current.setImage(SWTHelper.loadImage("button_ok.png"));
                    current.setForeground(success);
                }
                catch (Exception ex)
                {
                    current.setImage(SWTHelper.loadImage("button_cancel.png"));
                    current.setForeground(failure);
                }
            }
            else
            	current.setText(cert.toString());
            
            current.setData(cert);
            last = current;
        }
    }
    
    private void generateCertificateChainTree(Tree tree, final Tree details, final Text valueText)
        throws Exception
    {
        TreeItem root = new TreeItem(tree, SWT.NONE);        
        root.setImage(SWTHelper.loadImage("hierarchy.gif"));
        tree.setTopItem(root);
        
        parseCertificateChains(root);
        
        SWTHelper.expandAll(tree);
        tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Certificate cert = (Certificate) event.item.getData();
				generateCertificateStructureTree(details, valueText, cert);
				valueText.setText("");
			}
		});
    }
    
    private TreeItem generateNode(TreeItem parent, String childName, Object data)
    {
        TreeItem node = new TreeItem(parent, SWT.NONE);
        node.setText(childName);
        node.setData(data);
        
        return node;
    }
    
    private void generateExtensionNode(TreeItem parent, X509Certificate cert, X509Extensions extensions, String oid)
    {
        DERObjectIdentifier derOID = new DERObjectIdentifier(oid);        
        X509Extension ext = extensions.getExtension(derOID);
        
        if (ext.getValue() == null)
            return;
            
        byte[] octs = ext.getValue().getOctets();
        ASN1InputStream dIn = new ASN1InputStream(octs);
        StringBuilder buf = new StringBuilder();
        
        try
        {
            if (ext.isCritical())
                buf.append(Messages.getString("MailsterSWT.dialog.certificate.criticalExt")); //$NON-NLS-1$
            else
                buf.append(Messages.getString("MailsterSWT.dialog.certificate.nonCriticalExt")); //$NON-NLS-1$
            
            if (derOID.equals(X509Extensions.BasicConstraints))
            {
                BasicConstraints bc = new BasicConstraints((ASN1Sequence)dIn.readObject());                
                if (bc.isCA())
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.BasicConstraints.isCA")); //$NON-NLS-1$
                else
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.BasicConstraints.notCA")); //$NON-NLS-1$
                
                buf.append(Messages.getString("MailsterSWT.dialog.certificate.BasicConstraints.maxIntermediateCA")); //$NON-NLS-1$
                
                if (bc.getPathLenConstraint() == null || 
                        bc.getPathLenConstraint().intValue() == Integer.MAX_VALUE)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.BasicConstraints.unlimited")); //$NON-NLS-1$
                else
                    buf.append(bc.getPathLenConstraint()).append('\n');
                
                generateNode(parent, Messages.getString(oid), buf);
            }
            else
            if (derOID.equals(X509Extensions.KeyUsage))
            {
                KeyUsage us = new KeyUsage((DERBitString)dIn.readObject());
                if ((us.intValue() & KeyUsage.digitalSignature) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.digitalSignature")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.nonRepudiation) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.nonRepudiation")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.keyEncipherment) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.keyEncipherment")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.dataEncipherment) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.dataEncipherment")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.keyAgreement) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.keyAgreement")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.keyCertSign) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.keyCertSign")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.cRLSign) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.cRLSign")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.encipherOnly) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.encipherOnly")); //$NON-NLS-1$
                if ((us.intValue() & KeyUsage.decipherOnly) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.KeyUsage.decipherOnly")); //$NON-NLS-1$
                    
                generateNode(parent, Messages.getString(oid), buf);
            }
            else
            if (derOID.equals(X509Extensions.SubjectKeyIdentifier))
            {                        
                SubjectKeyIdentifier id = new SubjectKeyIdentifier((DEROctetString)dIn.readObject());
                generateNode(parent, Messages.getString(oid), 
                        buf.toString()+CertificateUtilities.byteArrayToString(id.getKeyIdentifier()));
            }
            else
            if (derOID.equals(X509Extensions.AuthorityKeyIdentifier))
            {                        
                AuthorityKeyIdentifier id = new AuthorityKeyIdentifier((ASN1Sequence)dIn.readObject());
                generateNode(parent, Messages.getString(oid), 
                        buf.toString()+id.getAuthorityCertSerialNumber());
            }
            else 
            if (derOID.equals(MiscObjectIdentifiers.netscapeRevocationURL))
            {
                buf.append(new NetscapeRevocationURL((DERIA5String)dIn.readObject())).append("\n");
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else 
            if (derOID.equals(MiscObjectIdentifiers.verisignCzagExtension))
            {
                buf.append(new VerisignCzagExtension((DERIA5String)dIn.readObject())).append("\n");
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else 
            if (derOID.equals(X509Extensions.CRLNumber))
            {
                buf.append((DERInteger)dIn.readObject()).append("\n");
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else 
            if (derOID.equals(X509Extensions.ReasonCode))
            {
                ReasonFlags rf = new ReasonFlags((DERBitString)dIn.readObject());
                
                if ((rf.intValue() & ReasonFlags.unused) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.unused")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.keyCompromise) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.keyCompromise")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.cACompromise) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.cACompromise")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.affiliationChanged) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.affiliationChanged")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.superseded) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.superseded")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.cessationOfOperation) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.cessationOfOperation")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.certificateHold) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.certificateHold")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.privilegeWithdrawn) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.privilegeWithdrawn")); //$NON-NLS-1$
                if ((rf.intValue() & ReasonFlags.aACompromise) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ReasonCode.aACompromise")); //$NON-NLS-1$
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else
            if (derOID.equals(MiscObjectIdentifiers.netscapeCertType))
            {
                NetscapeCertType type = new NetscapeCertType((DERBitString)dIn.readObject());
                
                if ((type.intValue() & NetscapeCertType.sslClient) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.sslClient")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.sslServer) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.sslServer")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.smime) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.smime")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.objectSigning) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.objectSigning")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.reserved) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.reserved")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.sslCA) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.sslCA")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.smimeCA) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.smimeCA")); //$NON-NLS-1$
                if ((type.intValue() & NetscapeCertType.objectSigningCA) > 0)
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.NetscapeCertType.objectSigningCA")); //$NON-NLS-1$
                
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else
            if (derOID.equals(X509Extensions.ExtendedKeyUsage))
            {
                ExtendedKeyUsage eku = new ExtendedKeyUsage((ASN1Sequence)dIn.readObject());
                if (eku.hasKeyPurposeId(KeyPurposeId.anyExtendedKeyUsage))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.anyExtendedKeyUsage")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_clientAuth))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_clientAuth")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_codeSigning))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_codeSigning")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_emailProtection))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_emailProtection")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_ipsecEndSystem))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_ipsecEndSystem")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_ipsecTunnel))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_ipsecTunnel")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_ipsecUser))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_ipsecUser")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_OCSPSigning))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_OCSPSigning")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_serverAuth))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_serverAuth")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_smartcardlogon))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_smartcardlogon")); //$NON-NLS-1$
                if (eku.hasKeyPurposeId(KeyPurposeId.id_kp_timeStamping))
                    buf.append(Messages.getString("MailsterSWT.dialog.certificate.ExtendedKeyUsage.id_kp_timeStamping")); //$NON-NLS-1$
                
                generateNode(parent, Messages.getString(oid), buf.toString());
            }
            else
                generateNode(parent, MessageFormat.format(
                        Messages.getString("MailsterSWT.dialog.certificate.objectIdentifier"), //$NON-NLS-1$ 
                        new Object[] {oid.replace('.', ' ')}), 
                        CertificateUtilities.byteArrayToString((cert.getExtensionValue(oid))));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    private String formatDate(Date d)
    {
    	return DateUtilities.df.format(d)+"\n("+DateUtilities.gmt.format(d)+")";
    }
    
    private void generateCertificateStructureTree(Tree tree, final Text valueText, Certificate selected)
    {
    	tree.removeAll();
    	if (selected == null)
    		return;
    	
    	if (selected instanceof X509Certificate)
    	{
	        X509Certificate cert = (X509Certificate) selected;        
	        TreeItem root = new TreeItem(tree, SWT.NONE);
	        root.setText(cert.getSubjectDN().getName());
	        tree.setTopItem(root);
	    
	        TreeItem c = generateNode(root, Messages.getString("MailsterSWT.dialog.certificate.certificate"), null); //$NON-NLS-1$
	        
            String version = Messages.getString("MailsterSWT.dialog.certificate.version"); //$NON-NLS-1$
	        generateNode(c, version, version+" "+cert.getVersion());
	        generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.serialNumber"), //$NON-NLS-1$ 
	        		CertificateUtilities.byteArrayToString(cert.getSerialNumber().toByteArray(), false));
	        generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.certSigAlg"), //$NON-NLS-1$ 
                    cert.getSigAlgName());
	        generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.issuer"), //$NON-NLS-1$ 
                    CertificateUtilities.x500PrincipalToString(cert.getIssuerDN()));
	        
	        TreeItem validity = generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.validity"), null); //$NON-NLS-1$
	        generateNode(validity, Messages.getString("MailsterSWT.dialog.certificate.notBefore"), //$NON-NLS-1$ 
                    formatDate(cert.getNotBefore()));	        
	        generateNode(validity, Messages.getString("MailsterSWT.dialog.certificate.notAfter"), //$NON-NLS-1$ 
                    formatDate(cert.getNotAfter()));
	        
	        generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.subject"), //$NON-NLS-1$ 
                    CertificateUtilities.x500PrincipalToString(cert.getSubjectDN()));
            
            TreeItem pk = generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.subjectPKInfo"), null); //$NON-NLS-1$
            generateNode(pk, Messages.getString("MailsterSWT.dialog.certificate.subjectPKAlgorithm"), //$NON-NLS-1$ 
                    cert.getPublicKey().getAlgorithm());
            generateNode(pk, Messages.getString("MailsterSWT.dialog.certificate.subjectPublicKey"), //$NON-NLS-1$ 
                    CertificateUtilities.byteArrayToString(cert.getPublicKey().getEncoded()));

            TreeItem extItem = generateNode(c, Messages.getString("MailsterSWT.dialog.certificate.extensions"), null); //$NON-NLS-1$

            try
            {
                X509Extensions exts = CertificateUtilities.getExtensions(cert);
                if (cert.getCriticalExtensionOIDs() != null)
                    for(String oid : cert.getCriticalExtensionOIDs())
                        generateExtensionNode(extItem, cert, exts, oid);

                if (cert.getNonCriticalExtensionOIDs() != null)
                    for(String oid : cert.getNonCriticalExtensionOIDs())
                        generateExtensionNode(extItem, cert, exts, oid);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            generateNode(root, Messages.getString("MailsterSWT.dialog.certificate.certSigningAlgorithm"), //$NON-NLS-1$ 
                    cert.getSigAlgName());
	        
	        generateNode(root, Messages.getString("MailsterSWT.dialog.certificate.certSignature"), //$NON-NLS-1$
	                CertificateUtilities.byteArrayToString(cert.getSignature()));
    	}
    	else
    	{
    		TreeItem root = new TreeItem(tree, SWT.NONE);
	        root.setText(selected.toString());
	        tree.setTopItem(root);
	        
	        generateNode(root, Messages.getString("MailsterSWT.dialog.certificate.type"), //$NON-NLS-1$ 
                    selected.getType());
	        generateNode(root, Messages.getString("MailsterSWT.dialog.certificate.publicKey"), //$NON-NLS-1$ 
	        		CertificateUtilities.byteArrayToString(selected.getPublicKey().getEncoded()));	        
    	}
    	
    	SWTHelper.expandAll(tree);
        
        tree.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Object data = event.item.getData();
				valueText.setText(data == null ? "" : data.toString()); //$NON-NLS-1$
			}
		});
    }    
}