package org.mailster.crypto;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.auth.x500.X500PrivateCredential;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
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
 * CertificateUtilities.java - A set of utilities methods to handle certificates.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class CertificateUtilities
{
	static
	{
		if (Security.getProvider("BC") == null)
            Security.addProvider(new BouncyCastleProvider());
	}
	
    public enum DigestAlgorithm 
    {
    	SHA1, MD5;

	    public String toString() 
	    {
	    	if (SHA1.equals(this))
	    		return "SHA1";
	    	else
	    	if (MD5.equals(this))
	    		return "MD5";
	    	else
	    		throw new AssertionError("Unknown algorithm : " + this);
	    }
    }
    
    /** 
     * Log object for this class. 
     */
    private static final Logger LOG = LoggerFactory.getLogger(CertificateUtilities.class);
    
    /**
     * A two years validity period in milliseconds.
     */
    public static final long DEFAULT_VALIDITY_PERIOD = 2L * 366L * 24L * 60L * 60L * 1000L;
    
    /**
     * Stores the serial number used to generate certificates.
     */
    private static long serial = 1;

    /**
     * Create a random <code>keySize</code> bit RSA key pair.
     */
    public static KeyPair generateRSAKeyPair(int keySize)
        throws Exception
    {
        KeyPairGenerator  kpGen = KeyPairGenerator.getInstance("RSA", "BC");    
        kpGen.initialize(keySize, new SecureRandom());
    
        return kpGen.generateKeyPair();
    }
    
    public static X509V3CertificateGenerator initCertificateGenerator(
            KeyPair pair, String issuerDN, String subjectDN, boolean isCA, long validityPeriod)
        throws Exception
    {   
        return initCertificateGenerator(
                pair, issuerDN, subjectDN, isCA, validityPeriod, null);
    }
    
    public static X509V3CertificateGenerator initCertificateGenerator(
            KeyPair pair, String issuerDN, String subjectDN, 
            boolean isCA, long validityPeriod, String signatureAlgorithm)
        throws Exception
    {   
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        setSerialNumberAndValidityPeriod(v3CertGen, isCA, validityPeriod);
        
        v3CertGen.setIssuerDN(new X509Name(true, X509Name.DefaultLookUp, issuerDN));
        v3CertGen.setSubjectDN(new X509Name(true, X509Name.DefaultLookUp, subjectDN));
        v3CertGen.setPublicKey(pair.getPublic());
        if (signatureAlgorithm != null)
            v3CertGen.setSignatureAlgorithm(signatureAlgorithm);
        else
            v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        
        return v3CertGen;
    }
    
    public static void setSerialNumberAndValidityPeriod(X509V3CertificateGenerator certGen, 
            boolean isRootCA, long validityPeriod)
    {
    	if (isRootCA)
    		certGen.setSerialNumber(BigInteger.ONE);
    	else
    		certGen.setSerialNumber(BigInteger.valueOf(++serial));
        
        long time = System.currentTimeMillis();
        time -= time % 86400000L;
        certGen.setNotBefore(new Date(time));
        certGen.setNotAfter(new Date(time + validityPeriod));
    }
    
    /**
     * Generate a CA Root certificate.
     */
    private static X509Certificate generateRootCert(String DN, KeyPair pair)
        throws Exception
    {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setIssuerDN(new X509Name(true, X509Name.DefaultLookUp, DN));
        certGen.setSubjectDN(new X509Name(true, X509Name.DefaultLookUp, DN));   
        
        setSerialNumberAndValidityPeriod(certGen, true, DEFAULT_VALIDITY_PERIOD);  

        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        
        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, 
                false, new AuthorityKeyIdentifier(
                        new GeneralNames(new GeneralName(new X509Name(true, X509Name.DefaultLookUp, DN))), 
                        BigInteger.ONE));
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, 
                false, new SubjectKeyIdentifierStructure(pair.getPublic()));
        
        certGen.addExtension(X509Extensions.BasicConstraints, 
                true, new BasicConstraints(true));
        certGen.addExtension(X509Extensions.KeyUsage, 
                true, new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign | KeyUsage.nonRepudiation));
        certGen.addExtension(MiscObjectIdentifiers.netscapeCertType, 
                false, new NetscapeCertType(NetscapeCertType.smimeCA | 
                        NetscapeCertType.sslCA | NetscapeCertType.objectSigning));
        
        return certGen.generate(pair.getPrivate(), "BC");
    }
    
    /**
     * Generate a sample V3 certificate to use as an intermediate or end entity 
     * certificate depending on the <code>isEndEntity</code> argument.
     */
    private static X509Certificate generateV3Certificate(String DN, boolean isEndEntity, 
            PublicKey entityKey, PrivateKey caKey, X509Certificate caCert)
        throws Exception
    {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();              
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setSubjectDN(new X509Name(true, X509Name.DefaultLookUp, DN));
        
        setSerialNumberAndValidityPeriod(certGen, false, DEFAULT_VALIDITY_PERIOD);
        
        certGen.setPublicKey(entityKey);
        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        
        certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, 
                false, new AuthorityKeyIdentifier(caCert.getEncoded(),
                        new GeneralNames(new GeneralName(
                        		new X509Name(true, X509Name.DefaultLookUp, caCert.getSubjectDN().getName()))), 
                        caCert.getSerialNumber()));
        
        certGen.addExtension(X509Extensions.SubjectKeyIdentifier, 
                false, new SubjectKeyIdentifierStructure(entityKey));
        
        if (isEndEntity)
        {
            certGen.addExtension(X509Extensions.BasicConstraints, 
                    true, new BasicConstraints(false));
            certGen.addExtension(X509Extensions.KeyUsage, 
                    true, new KeyUsage(KeyUsage.digitalSignature | 
                            KeyUsage.keyEncipherment));
        }
        else
        {
            certGen.addExtension(X509Extensions.BasicConstraints, 
                    true, new BasicConstraints(0));
            certGen.addExtension(X509Extensions.KeyUsage, 
                    true, new KeyUsage(KeyUsage.digitalSignature | 
                            KeyUsage.keyCertSign | KeyUsage.cRLSign));
        }
        return certGen.generate(caKey, "BC");
    }
    
    /**
     * Generate a X500PrivateCredential for the root entity.
     */
    public static X500PrivateCredential createRootCredential(String DN, String alias)
        throws Exception
    {
        KeyPair rootPair = generateRSAKeyPair(1024);
        X509Certificate rootCert = generateRootCert(DN, rootPair);
        
        return new X500PrivateCredential(rootCert, rootPair.getPrivate(), alias);
    }
    
    /**
     * Generate a X500PrivateCredential for the end or intermediate entity
     * depending on the <code>isEndEntity</code> argument.
     */
    public static X500PrivateCredential createEntityCredential(
        PrivateKey      caKey,
        X509Certificate caCert,
        String          alias,
        String          DN,
        boolean         isEndEntity)
        throws Exception
    {
        KeyPair interPair = generateRSAKeyPair(1024);
        X509Certificate cert = generateV3Certificate(
                DN, isEndEntity, interPair.getPublic(), caKey, caCert);
        
        return new X500PrivateCredential(cert, interPair.getPrivate(), alias);
    }
    
    public static X500PrivateCredential createIntermediateCredential(
            PrivateKey      caKey,
            X509Certificate caCert,
            String          rootDN,
            String          alias)
            throws Exception
    {
        return createEntityCredential(caKey, caCert, alias, "CN=Intermediate cert, "+rootDN, false);
    }
    
    public static X509Extensions getExtensions(X509Certificate cert) 
        throws Exception
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(cert.getEncoded());
        ASN1InputStream ais = new ASN1InputStream(bis);
        DERObject o = ais.readObject();
        X509CertificateStructure struct = X509CertificateStructure
                .getInstance(o);

        return struct.getTBSCertificate().getExtensions();
    }
    
    public static String toFingerprint(Certificate cer, DigestAlgorithm alg) 
    	throws NoSuchAlgorithmException, NoSuchProviderException, CertificateEncodingException        
    {
        MessageDigest digester = MessageDigest.getInstance(alg.toString(), "BC");
        byte[] bytes = digester.digest(cer.getEncoded());        
        return asHex(bytes, ":").toUpperCase();
    }
    
    public static String asHex(byte[] bytes)
    {
    	return asHex(bytes, null);
    }
    
    public static String asHex(byte[] bytes, String separator) 
	{
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < bytes.length; i++)
	    {
	        String code = Integer.toHexString(bytes[i] & 0xFF);
	        if ((bytes[i] & 0xFF) < 16)
	        	sb.append('0');
	        
	        sb.append(code);
	        
	        if (separator != null && i<bytes.length-1)
	        	sb.append(separator);
	    }
	    
	    return sb.toString();
	}
    
    public static String x500PrincipalToString(Principal p)
	{
    	if (p == null)
    		return "";
    	
	    StringBuilder sb = new StringBuilder(p.getName().length());
	    String[] array = StringUtilities.split(p.getName(), ",");
	    for (String s : array)
	    {
	    	if (s.indexOf('=') != -1)
	    	{
		    	String[] name = StringUtilities.split(s, "=");
		    	sb.append(name[0].trim());
		    	sb.append(" = ");
		    	sb.append(name[1].trim());
		    	sb.append('\n');
	    	}
	    	else
	    		sb.append(s);
	    }
	    
	    return sb.toString();
	}
    
    public static String byteArrayToString(byte[] b)
    {
    	return byteArrayToString(b, true);
    }
    
    public static String byteArrayToString(byte[] b, boolean outputHeader)
	{
    	if (b == null)
    		return "";
    	
	    StringBuilder sb = new StringBuilder();
	    
	    if (outputHeader)
    	{
    		sb.append("Taille : ").append(b.length).append(" octets / ");
    		sb.append(b.length*8).append(" bits.\n");
    	}
	    
	    for (int i = 0; i < b.length; i++)
	    {
	    	if (i != 0 && i % 16 == 0)
	    		sb.append('\n');

	    	String s = Integer.toHexString(b[i] & 0xFF).toUpperCase();
	    	if (s.length() == 1)	    		
	    		sb.append('0');	    		
	    	sb.append(s);	    		
	    	sb.append(' ');	    	
	    }
	    return sb.toString();
	}    
    
    public static X509Certificate loadCertificate(String certfile)
    {
        X509Certificate cert = null;
        try
        {
            InputStream in = new FileInputStream(certfile);
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            cert = (X509Certificate) cf.generateCertificate(in);
        }
        catch (Exception e)
        {
            System.err.println("Certificate file \"" + certfile
                    + "\" not found - classpath is : \n"
                    + System.getProperty("java.class.path"));
        }
        return cert;
    }

    /**
     *  This method writes a certificate to a file. If binary is false, the
     *  certificate is base64 encoded.
     */
    public static void exportCertificate(Certificate cert, String certfile, boolean binary)
        throws Exception
    {
        exportCertificate(cert, new FileOutputStream(certfile), binary);
    }
    
    /**
     *  This method writes a certificate to a file. If binary is false, the
     *  certificate is base64 encoded.
     */
    public static void exportCertificate(Certificate cert, FileOutputStream os, boolean binary)
        throws Exception
    {
        // Get the encoded form which is suitable for exporting
        byte[] buf = cert.getEncoded();

        if (binary) 
        {
            // Write in binary form
            os.write(buf);
        } 
        else 
        {
            // Write in text form
            Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            wr.write("-----BEGIN CERTIFICATE-----\n");
            wr.write(new sun.misc.BASE64Encoder().encode(buf));
            wr.write("\n-----END CERTIFICATE-----\n");
            wr.flush();
        }
        
        os.close();
    }
    
    protected static TrustAnchor loadTrustAnchor(String trustCertFileName)
		throws Exception
	{
		X509Certificate cert = CertificateUtilities.loadCertificate(trustCertFileName);
		
		if (cert != null)
		{
		    byte[] ncBytes = cert
		            .getExtensionValue(X509Extensions.NameConstraints.getId());
		
		    if (ncBytes != null)
		    {
		        ASN1Encodable extValue = X509ExtensionUtil
		                .fromExtensionValue(ncBytes);
		        return new TrustAnchor(cert, extValue.getDEREncoded());
		    }
		    
		    return new TrustAnchor(cert, null);
		}
		
		return null;
	}
    
    /**
     * Returns the X.500 distinguished name. It internally uses toString()
     * instead of the getName() method. 
     * 
     * For example, getName() gives me this:
     * 1.2.840.113549.1.9.1=#16166a756c6975736461766965734063756362632e636f6d 
     * whereas toString() gives me this: 
     * EMAILADDRESS=juliusdavies@cucbc.com
     * 
     * @param cert  a X.500 certificate.
     * @return the value of the Subject DN.
     */
    public static String getDN(X509Certificate cert)
    {
        return cert.getSubjectDN().toString();
    }
    
    /**
     * @see #getDN(X509Certificate cert)
     */
    public static String getDN(javax.security.cert.X509Certificate cert)
    {
        return cert.getSubjectDN().toString();
    }
    
    /**
     * @see #getCN(String dn)
     */    
    public static String getCN(X509Certificate cert)
    {
        return getCN(getDN(cert));
    }

    /**
     * Parses a X.500 distinguished name for the value of the 
     * "Common Name" field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to <code>RFC 2253</code>.
     *
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    public static String getCN(String dn) 
    {
        return getField("CN", dn);
    }
    
    /**
     * Parses a X.500 distinguished name for the value of the 
     * <code>field</code> field.
     * This is done a bit sloppy right now and should probably be done a bit
     * more according to <code>RFC 2253</code>.
     *
     * @param field  the field to retrieve from the X.500 distinguished name.
     * @param dn  a X.500 distinguished name.
     * @return the value of the "Common Name" field.
     */
    public static String getField(String field, String dn) 
    {
        int i = dn.indexOf(field+"=");
        if (i == -1)
            return null;

        // Get the remaining DN without CN=
        dn = dn.substring(i + field.length() + 1);  

        char[] dncs = dn.toCharArray();
        for (i = 0; i < dncs.length; i++) 
        {
            if (dncs[i] == ','  && i > 0 && dncs[i - 1] != '\\')
                break;
        }
        
        return dn.substring(0, i);
    }
    
    /**
     * Checks if the peer certificate name is identical to it's DNS name.
     *
     * @param socket a <code>SSLSocket</code> value
     * @exception SSLPeerUnverifiedException  If there are problems obtaining
     * the server certificates from the SSL session, or the server certificates 
     * does not have a "Common Name" or if it does not match.
     * 
     * @exception UnknownHostException  If we are not able to resolve
     * the SSL sessions returned server host name. 
     */
    public static void verifyHostname(SSLSocket socket) 
        throws SSLPeerUnverifiedException, UnknownHostException 
    {
        SSLSession session = socket.getSession();
        String hostname = session.getPeerHost();
        
        try 
        {
            InetAddress.getByName(hostname);
        } 
        catch (UnknownHostException uhe) 
        {
            throw new UnknownHostException(
                    "Could not resolve SSL session server hostname: " + hostname);
        }
        
        javax.security.cert.X509Certificate[] certs = session.getPeerCertificateChain();
        if (certs == null || certs.length == 0) 
            throw new SSLPeerUnverifiedException("No server certificates found!");
        
        //get the servers DN in its string representation
        javax.security.cert.X509Certificate x509 = (javax.security.cert.X509Certificate) certs[0];
        String dn = getDN(x509);

        //might be useful to print out all certificates we receive from the
        //server, in case one has to debug a problem with the installed certs.
        if (LOG.isDebugEnabled()) 
        {
            LOG.debug("Server certificate chain:");
            for (int i = 0; i < certs.length; i++)
                LOG.debug("X509Certificate[{}]={}", i, certs[i]);            
        }
        
        //get the common name from the first cert
        String cn = getCN(dn);
        if (cn == null)
            throw new SSLPeerUnverifiedException("Certificate doesn't contain CN: " + dn);
        
        // I'm okay with being case-insensitive when comparing the host we used
        // to establish the socket to the hostname in the certificate.
        // Don't trim the CN, though.
        cn = cn.toLowerCase();
        hostname = hostname.trim().toLowerCase();
        boolean doWildcard = false;
        
        if (cn.startsWith("*."))
        {
            // The CN better have at least two dots if it wants wildcard action,
            // but can't be [*.co.uk] or [*.co.jp] or [*.org.uk], etc...
            String withoutCountryCode = "";
            if (cn.length() >= 7 && cn.length() <= 9)
                withoutCountryCode = cn.substring(2, cn.length() - 2);

            doWildcard = cn.lastIndexOf('.') >= 0
                    && !"ac.".equals(withoutCountryCode)
                    && !"co.".equals(withoutCountryCode)
                    && !"com.".equals(withoutCountryCode)
                    && !"ed.".equals(withoutCountryCode)
                    && !"edu.".equals(withoutCountryCode)
                    && !"go.".equals(withoutCountryCode)
                    && !"gouv.".equals(withoutCountryCode)
                    && !"gov.".equals(withoutCountryCode)
                    && !"info.".equals(withoutCountryCode)
                    && !"lg.".equals(withoutCountryCode)
                    && !"ne.".equals(withoutCountryCode)
                    && !"net.".equals(withoutCountryCode)
                    && !"or.".equals(withoutCountryCode)
                    && !"org.".equals(withoutCountryCode);

            // The [*.co.uk] problem is an interesting one. Should we just
            // hope that CA's would never foolishly allow such a
            // certificate to happen?
        }

        boolean match;
        if (doWildcard)
            match = hostname.endsWith(cn.substring(1));
        else
            match = hostname.equals(cn);

        if (match) 
            LOG.debug("Target hostname valid : {}", cn);
        else 
            throw new SSLPeerUnverifiedException("HTTPS hostname invalid: <"
                    + hostname + "> != <" + cn + ">");
    }    
}
