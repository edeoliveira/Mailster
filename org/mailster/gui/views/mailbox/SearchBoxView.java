package org.mailster.gui.views.mailbox;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.mailster.gui.SWTHelper;

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
public class SearchBoxView
	extends Composite
{
	private static final Image SEARCH_IMAGE = SWTHelper.loadImage("icon_search.png");
	private static final Image CANCEL_IMAGE = SWTHelper.loadImage("icon_cancel.png");
	private static final Image CANCEL_IMAGE_GRAY = SWTHelper.loadGrayImage("icon_cancel.png");
	private static final Color BORDER = SWTHelper.createColor(165, 172, 181);
	private static final Color SHADOW = SWTHelper.createColor(240, 240, 240);

	private Text text;
	private Canvas c;
	private boolean empty = true;
	private boolean mouseDown = false;
	private Image img = SEARCH_IMAGE;

	private void clear()
	{
		text.setText("");
		setFocus();
		redrawCanvas();
	}

	private void redrawCanvas()
	{
		empty = text.getText().length() == 0;
		img = empty ? SEARCH_IMAGE : CANCEL_IMAGE_GRAY;
		c.redraw();
	}

	public SearchBoxView(Composite parent, int style)
	{
		super(parent, style);
		setLayout(new SearchBoxLayout());

		text = new Text(this, SWT.NONE);
		text.setMessage("Rechercher");
		text.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_IBEAM));
		c = new Canvas(this, SWT.NONE);
		c.moveAbove(text);
		c.setBounds(SEARCH_IMAGE.getBounds());
		c.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event)
			{
				if (mouseDown)
					event.gc.drawImage(CANCEL_IMAGE, 2, 2);
				else
					event.gc.drawImage(img, 0, 0);

				if (empty)
				{
					event.gc.setForeground(SHADOW);
					Rectangle rc = c.getBounds();
					event.gc.drawLine(0, 0, rc.width, 0);
					event.gc.drawLine(0, rc.height - 1, rc.width, rc.height - 1);
				}
				else
				{
					event.gc.setForeground(BORDER);
					event.gc.drawLine(0, 0, 0, event.height);
				}
			}
		});

		c.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent event)
			{
				if (empty)
					return;
				mouseDown = false;
				img = CANCEL_IMAGE_GRAY;
				c.redraw();
			}

			public void mouseEnter(MouseEvent event)
			{
				if (empty)
					return;
				if ((event.stateMask & SWT.BUTTON1) != 0)
					mouseDown = true;
				img = CANCEL_IMAGE;
				c.redraw();
			}
		});

		c.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent event)
			{
				if (empty)
					return;
				mouseDown = false;
				Point pt = c.getSize();
				if (event.x >= 0 && event.x < pt.x && event.y >= 0 && event.y < pt.y)
					clear();
			}

			public void mouseDown(MouseEvent event)
			{
				if (empty)
					return;
				mouseDown = true;
				redrawCanvas();
			}
		});

		text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event)
			{
				if (event.keyCode == SWT.ESC)
					clear();
				else
					redrawCanvas();
			}
		});

		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event)
			{
				event.gc.setForeground(BORDER);
				Rectangle rt = text.getBounds();
				Rectangle rc = c.getBounds();
				event.gc.drawRectangle(rt.x - 1, rc.y - 1, rt.width + rc.width + 1, rc.height + 1);
			}
		});
	}

	public Text getText()
	{
		return text;
	}
}
