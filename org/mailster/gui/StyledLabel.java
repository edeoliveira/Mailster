package org.mailster.gui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

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
 * StyledLabel.java - An improved SWT Label that handles automatic styling of
 * the label text. Currently only supports : <br>
 * \<a\>...\<\/a\> : show a text as a fake hyperlink<br>
 * \<b\>...\<\/b\> : set bold font<br>
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class StyledLabel extends StyledText
{
	public final static Color LINK_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	
    public StyledLabel(Composite c, int flags)
    {
        super(c, flags);
        setEditable(false);
        getCaret().setVisible (false);
    }

    public void setText(String s)
    {
        if (s.indexOf("<") != -1)
        {
            StringBuilder buffer = new StringBuilder(s);
            ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

            while (buffer.length() > 0)
            {
                int start = buffer.indexOf("<a>");
                int end = buffer.indexOf("</a>");
                int tmp = buffer.indexOf("<b>");

                if (start != -1 && end != -1
                        && start < end
                        && (tmp == -1 || start < tmp))
                {
                    buffer.delete(end, end + 4);
                    buffer.delete(start, start + 3);
                    StyleRange range = new StyleRange();
                    range.start = start;
                    range.length = end - start - 3;
                    range.foreground = LINK_COLOR;
                    range.underline = true;
                    ranges.add(range);
                }
                else
                {
                    start = tmp;
                    end = buffer.indexOf("</b>");

                    if (start != -1 && end != -1 && start < end)
                    {
                        buffer.delete(end, end + 4);
                        buffer.delete(start, start + 3);
                        StyleRange range = new StyleRange();
                        range.start = start;
                        range.length = end - start - 3;
                        range.fontStyle = SWT.BOLD;
                        ranges.add(range);
                    }
                    else
                        break;
                }
            }
            super.setText(buffer.toString());
            setStyleRanges(ranges.toArray(new StyleRange[ranges.size()]));
        }
        else
            super.setText(s);
    }

    protected void checkSubclass()
    {
        // Override SWT subclassing protection
        return;
    }
}
