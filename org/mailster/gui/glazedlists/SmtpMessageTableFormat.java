package org.mailster.gui.glazedlists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;
import org.mailster.MailsterSWT;
import org.mailster.gui.Messages;
import org.mailster.gui.SWTHelper;
import org.mailster.smtp.SmtpMessage;
import org.mailster.util.MailUtilities;

public class SmtpMessageTableFormat implements ExtendedTableFormat<SmtpMessage>
{
    public final static SimpleDateFormat hourDateFormat = new SimpleDateFormat(
            "HH:mm:ss"); //$NON-NLS-1$
    private String attachHeader, toHeader, subjectHeader, dateHeader;

    private Image attachedFilesImage;
    private Color tableRowColor;

    public SmtpMessageTableFormat(SWTHelper swtHelper)
    {
        init(swtHelper);
    }

    private void init(SWTHelper swtHelper)
    {
        attachHeader = ""; //$NON-NLS-1$
        toHeader = Messages.getString("MailView.column.to"); //$NON-NLS-1$
        subjectHeader = Messages.getString("MailView.column.subject"); //$NON-NLS-1$
        dateHeader = Messages.getString("MailView.column.date"); //$NON-NLS-1$
        attachedFilesImage = swtHelper.loadImage("attach.gif"); //$NON-NLS-1$
        tableRowColor = swtHelper.createColor(240, 240, 255);
    }

    public int getColumnCount()
    {
        return 4;
    }

    public String getColumnName(int column)
    {
        if (column == 0)
            return attachHeader;
        else if (column == 1)
            return toHeader;
        else if (column == 2)
            return subjectHeader;
        else if (column == 3)
            return dateHeader;

        throw new IllegalStateException();
    }

    public Object getColumnValue(SmtpMessage msg, int column)
    {
        if (column == 0)
            return ""; //$NON-NLS-1$
        else if (column == 1)
            return msg.getTo();
        else if (column == 2)
            return msg.getSubject();
        else if (column == 3)
        {
            String date = msg.getDate();

            try
            {
                Date d = MailUtilities.rfc822DateFormatter.parse(date);
                if ((int) (d.getTime() / 8.64E7) == (int) (((new Date())
                        .getTime()) / 8.64E7))
                    // same day
                    date = hourDateFormat.format(d);
                else
                    date = MailsterSWT.df.format(d);
            }
            catch (ParseException ex)
            {
                ex.printStackTrace();
            }

            return date;
        }

        throw new IllegalStateException();
    }

    public void setupItem(TableItem item, SmtpMessage msg, int realIndex)
    {
        //System.out.println(msg.getHeaderValue(SmtpInternetHeaders.TO)+" ; index = "+realIndex);
        item.setData(msg);
        if (msg.getInternalParts().getAttachedFiles().length > 0)
            item.setImage(attachedFilesImage);
        item.setData(msg);
        if (realIndex % 2 == 1)
            item.setBackground(tableRowColor);
    }
}
