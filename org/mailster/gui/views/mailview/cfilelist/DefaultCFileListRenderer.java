package org.mailster.gui.views.mailview.cfilelist;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.mailster.gui.SWTHelper;

public class DefaultCFileListRenderer
	extends AbstractRenderer
{
	private static final Color SELECTION_COLOR = SWTHelper.createColor(167, 205, 240);
	private static final Color SELECTION_COLOR_NOFOCUS = SWTHelper.createColor(229, 229, 229);
	public static final int[] DASH_LINE = new int[] {2, 2};

	private int margin = 3;
	private int textMargin = 2;

	private CFileList parent;

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(GC gc, int wHint, int hHint, Object value)
	{
		CFileItem item = (CFileItem) value;

		if (item.getImage() == null)
			return new Point(wHint, gc.getFontMetrics().getHeight() + (2 * margin));

		Rectangle imageBounds = item.getImage().getBounds();
		int h = Math.max(imageBounds.height, gc.getFontMetrics().getHeight() + (2 * margin));

		if (h % 2 != 0)
			h++;

		int w = Math.max(wHint, gc.textExtent(item.getText()).x + imageBounds.width + (2 * margin) + (2 * textMargin));

		if (w % 2 != 0)
			w++;

		return new Point(w, h);
	}

	/**
	 * {@inheritDoc}
	 */
	public void paint(GC gc, Object value)
	{
		CFileItem item = (CFileItem) value;
		
		Image img = parent.getBackgroundImage();
		Rectangle r = getBounds();
		Rectangle b = img == null ? new Rectangle(0, 0, 0, 0) : img.getBounds();
		int h = Math.min(r.height, b.height);

		if (isFullRedraw())
			gc.setClipping((Rectangle) null);
		else
		{
			Region region = new Region();
			region.add(r.x, r.y, r.width, h);
			region.subtract(r.x + 2, r.y + 2, r.width - 4, h - 4);
			gc.setClipping(region);
		}

		if (isSelected())
		{
			if (isFocus())
				gc.setBackground(SELECTION_COLOR);
			else
				gc.setBackground(SELECTION_COLOR_NOFOCUS);

			gc.fillRectangle(r.x, r.y, r.width, r.height);
		}
		else
		{
			ScrolledComposite sc = (ScrolledComposite) parent.getParent();
			Point origin = sc.getOrigin();
			if (img != null)
			{
				if (origin.y <= r.y && r.y-origin.y < b.height)
				{
					int _h = r.y - origin.y + h > b.height ? b.height - (r.y - origin.y) : h;
					gc.drawImage(img, 0, r.y-origin.y, b.width, _h, r.x, r.y, r.width, _h);
				}
				else
					if (r.y < origin.y && r.y+r.height >= origin.y)
					{
						int _h = r.y+r.height - origin.y;
						gc.drawImage(img, 0, 0, b.width, _h, r.x, origin.y, r.width, _h);
					}
			}
			else
				gc.fillRectangle(r.x, r.y, r.width, r.height);
		}

		if ((isSelected() && isFocus()) || isHover())
		{
			gc.setLineDash(DASH_LINE);
			gc.drawRectangle(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
		}

		if (!isFullRedraw() && !isSelected())
			return;

		gc.drawImage(item.getImage(), r.x + margin, r.y + margin);
		Rectangle itemBounds = item.getImage().getBounds();

		gc.drawString(item.getText(), r.x + itemBounds.width + margin + textMargin, r.y + margin, true);
	}

	public void initialize(Control control)
	{
		this.parent = (CFileList) control;
	}
}
