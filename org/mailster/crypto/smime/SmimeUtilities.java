package org.mailster.crypto.smime;

import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

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
 * SmimeUtilities.java - A set of utilities methods to S/MIME functions.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class SmimeUtilities
{
    
    /**
     * Build a path using the given root as the trust anchor, and the passed
     * in end constraints and certificate store.
     * <p>
     * Note: the path is built with revocation checking turned off.
     */
    private static PKIXCertPathBuilderResult buildPath(X509Certificate rootCert,
                                                      X509CertSelector endConstraints,
                                                      CertStore certsAndCRLs)
        throws Exception
    {
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX", "BC");
        PKIXBuilderParameters buildParams = new PKIXBuilderParameters(
                Collections.singleton(new TrustAnchor(rootCert, null)), endConstraints);
        
        buildParams.addCertStore(certsAndCRLs);
        buildParams.setRevocationEnabled(false);
        
        return (PKIXCertPathBuilderResult)builder.build(buildParams);
    }
    
    /**
     * Return a boolean array representing a <code>KeyUsage</code> 
     * with the digitalSignature bit set.
     */
    private static boolean[] getKeyUsageForSignature()
    {
        boolean[] val = new boolean[9];
        val[0] = true;
        return val;
    }
    
    /**
     * Take a CMS SignedData message and a trust anchor and determine if
     * the message is signed with a valid signature from a end entity
     * certificate recognized by the trust anchor rootCert.
     */
    public static boolean isValid(CMSSignedData signedData,
                                  X509Certificate rootCert)
        throws Exception
    {
        CertStore certsAndCRLs = signedData.getCertificatesAndCRLs("Collection", "BC");
        SignerInformationStore signers = signedData.getSignerInfos();
        Iterator<?> it = signers.getSigners().iterator();

        if (it.hasNext())
        {
            SignerInformation signer = (SignerInformation)it.next();
            X509CertSelector signerConstraints = signer.getSID();
            
            signerConstraints.setKeyUsage(getKeyUsageForSignature());            
            PKIXCertPathBuilderResult result = buildPath(rootCert, signer.getSID(), certsAndCRLs);

            return signer.verify(result.getPublicKey(), "BC");
        }
        
        return false;
    }
}
