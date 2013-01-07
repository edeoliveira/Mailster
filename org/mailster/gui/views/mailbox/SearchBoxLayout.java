package org.mailster.gui.views.mailbox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster Web Site</a>
 * <br>
 * ---
 * <p>
 * MailsterSWT.java - The main Mailster class.
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.23 $, $Date: 2009/04/05 12:13:27 $
 */
public class SearchBoxLayout
	extends Layout
{
	// fixed margin and spacing
	public static final int MARGIN = 4;
	public static final int SPACING = 0;

	private Text t;
	private Canvas canvas;
	Point[] sizes;
	int maxWidth, totalHeight;

	private void initialize(Control children[])
	{
		maxWidth = 0;
		totalHeight = 0;
		sizes = new Point[children.length];
		for (int i = 0; i < children.length; i++)
		{
			sizes[i] = children[i].getSize();
			if (sizes[i] == null)
				sizes[i] = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			maxWidth = Math.max(maxWidth, sizes[i].x);
			totalHeight = Math.max(totalHeight, sizes[i].y);
		}
	}

	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache)
	{
		Control children[] = composite.getChildren();
		if (flushCache || sizes == null || sizes.length != children.length)
		{
			initialize(children);
		}
		int width = wHint, height = hHint;
		if (wHint == SWT.DEFAULT)
			width = maxWidth;
		if (hHint == SWT.DEFAULT)
			height = totalHeight;
		return new Point(width + (2 * MARGIN) + SPACING, height + (2 * MARGIN));
	}

	protected void layout(Composite composite, boolean flushCache)
	{
		if (t == null || flushCache)
		{
			Control[] ctrls = composite.getChildren();
			for (Control c : ctrls)
			{
				if (c instanceof Text)
					t = (Text) c;
				else
					canvas = (Canvas) c;
			}
		}

		Point size = composite.getSize();
		Point tmp = canvas.getSize();
		Point pt = new Point(size.x - tmp.x - SPACING - (2 * MARGIN), tmp.y);

		int h = t.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
		if (h % 2 == 1)
			h += 1;
		t.setBounds(MARGIN, MARGIN + (tmp.y - h) / 2, pt.x, h);
		canvas.setLocation(pt.x + SPACING + MARGIN, MARGIN);
	}
}
