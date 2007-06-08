package org.mailster.gui.views;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.StyledLabel;
import org.mailster.smtp.SmtpHeadersInterface;
import org.mailster.smtp.SmtpMessage;
import org.mailster.util.MailUtilities;

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
 * HeadersView.java - A view that creates a mail header.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version %I%, %G%
 */
public class HeadersView
{
    private static Font headerFont;
    private boolean minimized = false;
    private StyledLabel headersLabel;
    private Composite composite;

    private String fullText = "";
    private String resumeText = "";
    
    static
    {
        FontData fontData = SWTHelper.SYSTEM_FONT.getFontData()[0];
        headerFont = new Font(Display.getDefault(), new FontData(
                fontData.getName(), 7, fontData.getStyle()));
    }
    
    public HeadersView(Composite parent, SmtpMessage msg)
    {
        createView(parent, msg);
    }    
    
    private static String formatEmailList(SmtpHeadersInterface headers,
            String headerName)
    {
        return formatEmailList(Arrays.asList(headers.getHeaderValues(headerName)));
    }
    
    private static String formatEmailList(List<String> list)
    {
        StringBuffer sb = new StringBuffer();        

        for (String s : list)
        {
            if (sb.length()>0)
                sb.append(';');
            if (s != null)
                sb.append("<a>").append(s.trim()).append("</a>");
        }
        
        return sb.toString();
    }    
    
    private static void removeMatchesFromList(List<String> recipients, 
            SmtpMessage msg, String headerName)
    {
        String[] values = msg.getHeaders().getHeaderValues(headerName);
        for (String s : values)
        {
            if (s != null)
            {
                s = s.substring(s.indexOf('<'),s.indexOf('>')+1);
                recipients.remove(s);
            }
        }
    }
    
    private static String formatBccList(SmtpMessage msg)
    {
        List<String> recipients = new ArrayList<String>(msg.getRecipients().size());
        for (String s : msg.getRecipients())
            recipients.add(s);
        
        removeMatchesFromList(recipients, msg, SmtpHeadersInterface.TO);
        removeMatchesFromList(recipients, msg, SmtpHeadersInterface.CC);
        
        return formatEmailList(recipients);
    }
    
    public Composite getComposite()
    {
        return composite;
    }
    
    private void computeTextStrings(SmtpMessage msg)
    {
        StringBuffer sb = new StringBuffer();
        SmtpHeadersInterface headers = msg.getHeaders();
        
        String date = MailsterSWT.df.format(new Date());
        
        try
        {
            date = MailsterSWT.df.format(MailUtilities.rfc822DateFormatter
                    .parse(MailUtilities.getNonNullHeaderValue(headers, SmtpHeadersInterface.DATE)));
        }
        catch (ParseException pex) {}
        
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
    
    public void createView(Composite parent, SmtpMessage msg)
    {
        final Color startColor = SWTHelper.createColor(243, 245, 248);
        final Color endColor = SWTHelper.createColor(179, 192, 206);
        composite = new Composite(parent, SWT.BORDER);
        composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridLayout g = new GridLayout(2, false);
        g.marginHeight = 0;
        g.marginWidth = 4;
        g.horizontalSpacing = 2;
        g.verticalSpacing = 0;
        composite.setLayout(g);
        composite.addListener (SWT.Resize, new Listener () {
            public void handleEvent (Event event) {
                Image oldImage = composite.getBackgroundImage();
                Display display = SWTHelper.getDisplay();
                Rectangle rect = composite.getClientArea ();
                Image newImage = new Image (display, Math.max (1, rect.width), 1);  
                GC gc = new GC (newImage);
                gc.setForeground (startColor);
                gc.setBackground (endColor);
                gc.fillGradientRectangle (rect.x, rect.y, rect.width, 1, false);
                gc.dispose ();
                composite.setBackgroundImage (newImage);
                if (oldImage != null) oldImage.dispose ();
                oldImage = newImage;
            }
        });

        final Image minimizedImage = SWTHelper.loadImage("plus.gif"); //$NON-NLS-1$
        final Image expandedImage = SWTHelper.loadImage("minus.gif"); //$NON-NLS-1$

        final Label image = new Label(composite, 0);
        image.setImage(expandedImage);
        image.setAlignment(SWT.TOP);
        image.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent arg0)
            {
                minimized = !minimized;
                image.setImage(minimized ? minimizedImage : expandedImage);

                if (minimized)
                    headersLabel.setText(resumeText);
                else
                    headersLabel.setText(fullText);

                composite.getParent().layout(true);
            }
        });

        computeTextStrings(msg);
        
        GridData data = new GridData();
        data.verticalAlignment = GridData.BEGINNING;
        data.grabExcessVerticalSpace = true;
        image.setLayoutData(data);

        headersLabel = new StyledLabel(composite, 0);
        headersLabel.setText(fullText);
        
        headersLabel.setFont(headerFont);
        data = new GridData();
        data.horizontalAlignment = GridData.BEGINNING;
        data.grabExcessHorizontalSpace = true;
        headersLabel.setLayoutData(data);
    }
}
