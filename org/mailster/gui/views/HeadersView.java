package org.mailster.gui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.MimeMultipart;

import org.bouncycastle.mail.smime.SMIMESigned;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.mailster.MailsterSWT;
import org.mailster.crypto.MailsterKeyStoreFactory;
import org.mailster.crypto.smime.SmimeUtilities;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.StyledLabel;
import org.mailster.gui.utils.LayoutUtils;
import org.mailster.message.SmtpHeadersInterface;
import org.mailster.message.SmtpMessage;
import org.mailster.pop3.mailbox.StoredSmtpMessage;
import org.mailster.util.DateUtilities;
import org.mailster.util.MailUtilities;
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
 * HeadersView.java - A view that creates a mail header.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class HeadersView
{
    /** 
     * Log object for this class. 
     */
    private static final Logger log = LoggerFactory.getLogger(HeadersView.class);
    
    private final static Image minimizedImage = SWTHelper.loadImage("plus.gif"); //$NON-NLS-1$
    private final static Image expandedImage = SWTHelper.loadImage("minus.gif"); //$NON-NLS-1$
    private final static Image smimeOkImage = SWTHelper.loadImage("smime_ok.gif"); //$NON-NLS-1$
    private final static Image smimeNokImage = SWTHelper.loadImage("smime_nok.gif"); //$NON-NLS-1$
    
    private final static Color startColor = SWTHelper.createColor(243, 245, 248);
    private final static Color endColor = SWTHelper.createColor(179, 192, 206);

    private static Font headerFont;
    private boolean minimized = false;
    private StyledLabel headersLabel;
    private Composite composite;

    private String fullText = "";
    private String resumeText = "";
    private boolean isSigned;
    private boolean isSignatureValid;
    
    static
    {
        FontData fontData = SWTHelper.SYSTEM_FONT.getFontData()[0];
        headerFont = new Font(Display.getDefault(), new FontData(
                fontData.getName(), 7, fontData.getStyle()));
        
        // Preload KeyFactory to avoid latency.
        try {
        	MailsterKeyStoreFactory.getInstance();
        } catch (Exception ex) {
        	//TODO disable encryption code while factory fails to load
        }
    }
    
    public HeadersView(Composite parent, StoredSmtpMessage stored)
    {
        createView(parent, stored);
    }    
    
    private static String formatEmailList(SmtpHeadersInterface headers,
            String headerName)
    {
        return formatEmailList(Arrays.asList(headers.getHeaderValues(headerName)));
    }
    
    private static String formatEmailList(List<String> list)
    {
    	StringBuilder sb = new StringBuilder();        

        for (String s : list)
        {
            if (sb.length()>0)
                sb.append(';');

            if (s != null)
            {
            	if (s.indexOf(',')>=0)
            	{
                    StringTokenizer tk = new StringTokenizer(s, ",");
    
                    while (tk.hasMoreTokens())
                    {
                    	sb.append("<a>").append(tk.nextToken().trim()).append("</a>");
                    	if (tk.hasMoreTokens())
                    		sb.append(';');
                    }
            	}
            	else
            		sb.append("<a>").append(s.trim()).append("</a>");
            }
        }
        
        return sb.toString();
    }    
    
    private static void removeMatchesFromList(List<String> recipients, 
            SmtpMessage msg, String headerName)
    {
    	if (recipients.size() <=0)
    		return;
    	
        String[] values = msg.getHeaders().getHeaderValues(headerName);
        
        // Detect recipients formating.
        boolean withDelimiters = recipients.get(0).contains("<");
        
        for (String s : values)
        {
            if (s != null)
            {
            	int pos = s.indexOf('<');
            	
            	try
            	{
	            	if (withDelimiters)
	            	{
	            		if (pos == -1)
	            			s = "<"+s.trim()+">";
	            		else
	            			s = s.substring(pos,s.indexOf('>')+1);
	            		
	            		for (String str : recipients)
	            		{
	            			if (str != null && str.contains(s))
	            			{
	            					recipients.remove(str);
	            					break;
	            			}
	            		}
	            	}
	            	else
	            	{
		            	if (pos != -1)
		            		s = s.substring(pos+1,s.indexOf('>'));
		            	else
		            		s = s.trim();
		            	
		            	recipients.remove(s);
	            	}
            	}
            	catch (Exception ex)
            	{
            		// Just to prevent malformed email addresses
            		log.debug("Malformed email adress", ex);            		
            	}
            }
        }
    }
    
    private static String formatBccList(SmtpMessage msg)
    {
        List<String> recipients = new ArrayList<String>(msg.getRecipients());
        
        removeMatchesFromList(recipients, msg, SmtpHeadersInterface.TO);
        removeMatchesFromList(recipients, msg, SmtpHeadersInterface.CC);
        
        return formatEmailList(recipients);
    }
    
    public void setLayoutData(Object layoutData) 
    {
    	composite.setLayoutData(layoutData);
    }
    
    private void computeTextStrings(StoredSmtpMessage stored)
    {
    	StringBuilder sb = new StringBuilder();
    	SmtpMessage msg = stored.getMessage();
        SmtpHeadersInterface headers = msg.getHeaders();        
        String date = DateUtilities.df.format(stored.getMessageDate());
        
        sb.append("<b>").append(Messages.getString("MailView.column.subject")).append(" : ");
        sb.append(MailUtilities.getNonNullHeaderValue(headers, SmtpHeadersInterface.SUBJECT));
        sb.append("</b>");
        resumeText = sb.toString();
        
        sb.append("\n<b>").append(Messages.getString("MailView.column.date")).append(" : </b>");
        sb.append(date);
        sb.append("\n<b>").append(Messages.getString("MailView.column.from")).append(" : </b>");
        sb.append(formatEmailList(headers, SmtpHeadersInterface.FROM));
        sb.append("\n<b>").append(Messages.getString("MailView.column.to")).append(" : </b>");
        sb.append(formatEmailList(headers, SmtpHeadersInterface.TO));
        
        String list = formatEmailList(headers, SmtpHeadersInterface.CC);
        if (!"".equals(list))
        {
            sb.append("\n<b>").append(Messages.getString("MailView.column.cc")).append(" : </b>");
            sb.append(list);
        }
        
        list = formatBccList(msg);
        
        if (!"".equals(list))
        {
            sb.append("\n<b>").append(Messages.getString("MailView.column.bcc")).append(" : </b>");
            sb.append(list);
        }
        
        fullText = sb.toString();        
    }
    
    public final void createView(Composite parent, StoredSmtpMessage stored)
    {
        composite = new Composite(parent, SWT.BORDER);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        composite.setLayout(
                LayoutUtils.createGridLayout(3, false, 0, 4, 0, 1, 0, 0, 0, 2));
        
        final Label image = new Label(composite, SWT.NONE);
        image.setImage(expandedImage);
        image.setAlignment(SWT.CENTER);
        GridData data = new GridData();
        data.verticalAlignment = GridData.BEGINNING;
        data.grabExcessVerticalSpace = true;
        image.setLayoutData(data);
        
        computeTextStrings(stored);

        headersLabel = new StyledLabel(composite, SWT.WRAP);
        headersLabel.setText(fullText);
        
        headersLabel.setFont(headerFont);
        data = new GridData();
        data.horizontalAlignment = GridData.BEGINNING;
        data.verticalAlignment = GridData.BEGINNING;
        data.grabExcessHorizontalSpace = true;
        headersLabel.setLayoutData(data);
        
        final Label smime = new Label(composite, SWT.NONE);
        smime.setImage(null);
        smime.setAlignment(SWT.BOTTOM);
        data = new GridData();
        data.verticalAlignment = GridData.END;
        data.grabExcessVerticalSpace = true;
        smime.setLayoutData(data);
        
        SmtpMessage msg = stored.getMessage();        
    	isSigned = SmimeUtilities.isSignedMessage(msg);
    	
    	if (isSigned)
    	{	    		
			try 
			{
				isSignatureValid = SmimeUtilities.isValid(
						new SMIMESigned((MimeMultipart)msg.asMimeMessage().getContent()),
															MailsterKeyStoreFactory.getInstance().getRootCertificate());				
			}
			catch (Exception e) 
			{
				MailsterSWT.getInstance().log(e.getMessage());
			}
			
			if (isSignatureValid)
			{
    			smime.setImage(smimeOkImage);
    			smime.setToolTipText(Messages.getString("HeadersView.signatureOk.tooltip"));  //$NON-NLS-1$
			}
    		else
    		{
    			smime.setImage(smimeNokImage);
    			smime.setToolTipText(Messages.getString("HeadersView.signatureNotOk.tooltip"));  //$NON-NLS-1$
    		}
    	}
    	
        composite.addListener (SWT.Resize, new Listener () {
            public void handleEvent (Event event) 
            {
                Image oldImage = composite.getBackgroundImage();
                Rectangle rect = composite.getClientArea ();
                int width = Math.max (1, rect.width);
                Image newImage = new Image (composite.getDisplay(), width, 1);  
                GC gc = new GC (newImage);
                gc.setForeground (startColor);
                gc.setBackground (endColor);
                gc.fillGradientRectangle (rect.x, rect.y, width, 1, false);
                gc.dispose();
                composite.setBackgroundImage (newImage);
                if (oldImage != null) 
                	oldImage.dispose();
            }
        });
        
        image.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent evt)
            {
                minimized = !minimized;
                image.setImage(minimized ? minimizedImage : expandedImage);

                if (minimized)
                {
                    headersLabel.setText(resumeText);
                    if (isSignatureValid)
                    	smime.setImage(null);
                }
                else
                {
                    headersLabel.setText(fullText);
                    if (isSignatureValid)
    	    			smime.setImage(smimeOkImage);
    	    		else
    	    		if (isSigned)
    	    			smime.setImage(smimeNokImage);
                }
                
                composite.getParent().layout(true);
            }
        });
    }
}
