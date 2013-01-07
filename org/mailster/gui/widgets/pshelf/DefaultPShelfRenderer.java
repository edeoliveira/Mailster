/*******************************************************************************
 * Copyright (c) 2006 Chris Gross. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: schtoo@schtoo.com(Chris Gross) - initial API and implementation
 ******************************************************************************/

package org.mailster.gui.widgets.pshelf;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.mailster.gui.SWTHelper;

/**
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT. THIS IS A PRE-RELEASE ALPHA VERSION.
 * USERS SHOULD EXPECT API CHANGES IN FUTURE VERSIONS.
 * </p>
 * 
 * @author cgross
 */
public class DefaultPShelfRenderer
	extends AbstractRenderer
{
	private int textMargin = 2;
	private int margin = 6;
	private PShelf parent;
	private int spacing = 4;

	private Color BORDER_COLOR = SWTHelper.createColor(161, 169, 179);
	private Color BORDER_SHADE1 = SWTHelper.createColor(229, 232, 237);
	private Color BORDER_SHADE2 = SWTHelper.createColor(217, 222, 227);
	private Color BG_COLOR = SWTHelper.createColor(197, 206, 216);

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(GC gc, int wHint, int hHint, Object value)
	{
		PShelfItem item = (PShelfItem) value;

		if (item.getImage() == null)
			return new Point(wHint, gc.getFontMetrics().getHeight() + (2 * (margin + textMargin)));

		int h = Math.max(item.getImage().getBounds().height, gc.getFontMetrics().getHeight() + (2 * textMargin)) + (2 * margin);

		if (h % 2 != 0)
			h++;

		return new Point(wHint, h);
	}

	/**
	 * {@inheritDoc}
	 */
	public void paint(GC gc, Object value)
	{
		PShelfItem item = (PShelfItem) value;
		Color fore = parent.getForeground();
		Rectangle r = getBounds();

		gc.setBackground(BG_COLOR);
		gc.fillRectangle(0, r.y, r.width, r.height);
		gc.setForeground(BORDER_COLOR);
		gc.drawRectangle(0, r.y, r.width - 1, 4);
		gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
		int shadeY = r.y + 1;
		gc.drawLine(1, shadeY, r.width - 2, shadeY);
		
		shadeY++;
		gc.setForeground(BORDER_SHADE1);
		gc.drawLine(1, shadeY, r.width - 2, shadeY);
		
		shadeY++;
		gc.setForeground(BORDER_SHADE2);
		gc.drawLine(1, shadeY, r.width - 2, shadeY);

		final int[] DASH_LINE = new int[] {2, 2};

		if (isSelected())
		{
			gc.setLineDash(DASH_LINE);
			gc.setForeground(BORDER_COLOR);
			gc.drawLine(1, r.y + r.height - 2, r.width - 2, r.y + r.height - 2);
			gc.setLineDash(null);
		}

		int x = 6;
		if (item.getImage() != null)
		{
			Rectangle b = item.getImage().getBounds();
			int y2 = (r.height - b.height) / 2;
			if ((r.height - b.height) % 2 != 0)
				y2++;

			gc.drawImage(item.getImage(), x, r.y + y2);

			x += b.width + spacing;
		}

		gc.setForeground(fore);

		int _y = r.height - gc.getFontMetrics().getHeight();
		int y2 = _y / 2;
		if (_y % 2 != 0)
			y2++;

		if (isHover() && !isSelected())
			gc.setForeground(SWTHelper.createColor(255, 201, 14));

		String text = getShortString(gc, item.getText(), r.width - x - 4);
		gc.drawString(text, x, r.y + y2, true);

		if (isFocus())
			gc.drawFocus(1, 1, r.width - 2, r.height - 1);
	}

	public void initialize(Control control)
	{
		this.parent = (PShelf) control;
	}

	private static String getShortString(GC gc, String t, int width)
	{
		if (t == null)
		{
			return null;
		}

		if (t.equals(""))
		{
			return "";
		}

		if (width >= gc.stringExtent(t).x)
		{
			return t;
		}

		int w = gc.stringExtent("...").x;
		String text = t;
		int l = text.length();
		int pivot = l / 2;
		int s = pivot;
		int e = pivot + 1;
		while (s >= 0 && e < l)
		{
			String s1 = text.substring(0, s);
			String s2 = text.substring(e, l);
			int l1 = gc.stringExtent(s1).x;
			int l2 = gc.stringExtent(s2).x;
			if (l1 + w + l2 < width)
			{
				text = s1 + "..." + s2;
				break;
			}
			s--;
			e++;
		}

		if (s == 0 || e == l)
		{
			text = text.substring(0, 1) + "..." + text.substring(l - 1, l);
		}

		return text;
	}
}
