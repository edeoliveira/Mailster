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
	public final static Color LINK_COLOR = 
		Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	public final static Color SUBJECT_COLOR = SWTHelper.createColor(255, 128, 0);
	
    public StyledLabel(Composite c, int flags)
    {
        super(c, flags);
        setEditable(false);
        getCaret().setVisible(false);
    }

    private void setLink(ArrayList<StyleRange> ranges, int start, int length)
    {
        StyleRange range = new StyleRange();
        range.start = start;
        range.length = length;
        range.foreground = LINK_COLOR;
        range.underline = true;
        ranges.add(range);    	
    }

    private void setBold(ArrayList<StyleRange> ranges, int start, int length)
    {
    	StyleRange range = new StyleRange();
        range.start = start;
        range.length = length;
        range.fontStyle = SWT.BOLD;
        ranges.add(range);
    }

    private void setSubject(ArrayList<StyleRange> ranges, int start, int length)
    {
    	StyleRange range = new StyleRange();
        range.start = start;
        range.length = length;
        range.foreground = SUBJECT_COLOR;
        ranges.add(range);
    }
    
    public void setText(String s)
    {
        if (s.indexOf("[") != -1)
        {
            StringBuilder buffer = new StringBuilder(s);
            ArrayList<StyleRange> ranges = new ArrayList<StyleRange>();

            int open = 0; 
            do
            {
                open = buffer.indexOf("[");
                
                String tag = buffer.substring(open+1, open+3);
                if ("a]".equals(tag))
                {
                	int end = buffer.indexOf("[/a]");
                	if (end == -1)
                		break;
                    buffer.delete(end, end + 4);
                    buffer.delete(open, open + 3);
                    setLink(ranges, open, end - open - 3);            	
                }
                else
                if ("b]".equals(tag))
                {
                	int end = buffer.indexOf("[/b]");
                	if (end == -1)
                		break;
                    buffer.delete(end, end + 4);
                    buffer.delete(open, open + 3);
                    setBold(ranges, open, end - open - 3);            	                	
                }
                else
                if ("s]".equals(tag))
                {
                	int end = buffer.indexOf("[/s]");
                	if (end == -1)
                		break;
                    buffer.delete(end, end + 4);
                    buffer.delete(open, open + 3);
                    setSubject(ranges, open, end - open - 3);            	                	
                }
                else
                	break;
            } 
            while (open != -1);
            
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
