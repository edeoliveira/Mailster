package org.mailster.gui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.mailster.MailsterSWT;
import org.mailster.gui.SWTHelper;
import org.mailster.gui.utils.GraphicsUtils;

public class CVerticalLabel
	extends Canvas
{
	private static final Rectangle NULL_BOUNDS = new Rectangle(0, 0, 0, 0);
	private static final Color[] BORDER_COLORS = new Color[] {SWTHelper.createColor(227, 230, 234),
			SWTHelper.createColor(222, 226, 230), SWTHelper.createColor(217, 220, 224), SWTHelper.createColor(211, 215, 218),
			SWTHelper.createColor(165, 172, 181)};

	private static final Color MOUSE_DOWN_BGCOLOR = SWTHelper.createColor(192, 199, 207);

	private static final Color[] MOUSE_DOWN_BORDER_COLORS = new Color[] {SWTHelper.createColor(179, 185, 193),
			SWTHelper.createColor(153, 162, 173)};

	private int margin = 6;

	private String text;

	private Image image;
	private Image textImage;
	private Image mouseDownTextImage;

	private boolean isHover;
	private boolean isMouseDown;
	private Point size;

	public CVerticalLabel(Composite parent, int style)
	{
		super(parent, style);
		setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		setBackground(MailsterSWT.BGCOLOR);

		addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e)
			{
				Rectangle r = getBounds();
				int x = margin;
				int y = margin + r.height - computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

				if (isMouseDown && isHover)
				{
					e.gc.setBackground(MOUSE_DOWN_BGCOLOR);
					e.gc.fillRectangle(0, 0, r.width - 1, r.height - 1);
				}

				if (textImage != null)
				{
					e.gc.drawImage(isMouseDown && isHover ? mouseDownTextImage : textImage, x, y);
					y += textImage.getBounds().height + margin;
				}
				else if (isHover && !isMouseDown)
				{
					e.gc.setBackground(BORDER_COLORS[1]);
					e.gc.fillRectangle(0, 0, r.width - 1, r.height - 1);
				}
				
				if (image != null)
					e.gc.drawImage(image, x, y);

				if (isHover && textImage != null)
				{
					Color[] colors = isMouseDown ? MOUSE_DOWN_BORDER_COLORS : BORDER_COLORS;

					for (int i = 0, max = colors.length; i < max; i++)
					{
						e.gc.setForeground(colors[max - i - 1]);
						int d = 1 + (i * 2);
						e.gc.drawRectangle(i, i, r.width - d, r.height - d);
					}
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e)
			{
				if (isHover)
				{
					Event ev = new Event();
					ev.item = CVerticalLabel.this;
					CVerticalLabel.this.notifyListeners(SWT.Selection, ev);
				}
				
				isMouseDown = false;
				redraw();
			}

			public void mouseDown(MouseEvent e)
			{
				isMouseDown = true;
				redraw();
			}
		});

		addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e)
			{
				isHover = true;
				redraw();
			}

			public void mouseExit(MouseEvent e)
			{
				isHover = false;
				redraw();
			}
		});

		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e)
			{
				boolean previousHoverValue = isHover;
				Rectangle r = getBounds();
				r.x = 0;
				r.y = 0;
				isHover = r.contains(e.x, e.y);

				if (previousHoverValue != isHover)
					redraw();
			}
		});
	}

	public void addSelectionListener(SelectionListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	public void removeSelectionListener(SelectionListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}
	
	protected void checkSubclass()
	{
		return;
	}

	private static Rectangle getSafeImageBounds(Image img)
	{
		if (img == null)
			return NULL_BOUNDS;
		else
			return img.getBounds();
	}

	public Point computeSize(int wHint, int hHint)
	{
		Rectangle rt = getSafeImageBounds(textImage);
		Rectangle ri = getSafeImageBounds(image);

		int w = Math.max(rt.width, ri.width) + 2 * margin;
		int h = rt.height + ri.height + 2 * margin;

		if (image != null && textImage != null)
			h += margin;

		size = new Point(w, Math.max(hHint, h));
		return size;
	}

	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		if (!changed && size != null)
			return size;
		else
			return computeSize(wHint, hHint);
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
		textImage = GraphicsUtils.createRotatedText(text, getFont(), getForeground(), getBackground(), SWT.UP);
		mouseDownTextImage = GraphicsUtils.createRotatedText(text, getFont(), getForeground(), MOUSE_DOWN_BGCOLOR, SWT.UP);
	}

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public void setMargin(int margin)
	{
		this.margin = margin;
	}
}
