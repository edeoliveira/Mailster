package org.mailster.gui.views.mailbox;

/*
 * (c) Copyright IBM Corp. 2000, 2001. All Rights Reserved
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TypedListener;
import org.mailster.core.pop3.mailbox.StoredSmtpMessage;
import org.mailster.gui.SWTHelper;
import org.mailster.util.DateUtilities;

/**
 * A TableTree is a selectable user interface object that displays a hierarchy of items, and issues
 * notification when an item is selected. A TableTree may be single or multi select.
 * <p>
 * The item children that may be added to instances of this class must be of type
 * <code>TableTreeItem</code>.
 * </p>
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it does not make sense to
 * add <code>Control</code> children to it, or set a layout on it.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>SINGLE, MULTI, CHECK, FULL_SELECTION
 * <dt><b>Events:</b>
 * <dd>Selection, DefaultSelection, Collapse, Expand
 * </dl>
 */
public class TableTree
	extends Composite
{
	static final TableTreeItem[] EMPTY_ITEMS = new TableTreeItem[0];

	static final int IMAGE_SIZE = 16;
	static final int IMAGE_MARGIN = 5;
	static final int IMAGE_ICON_SIZE = 6;

	Table table;
	TableTreeItem[] items = EMPTY_ITEMS;
	Image plusImage, minusImage;

	/*
	 * TableTreeItems are not treated as children but rather as items. When the TableTree is
	 * disposed, all children are disposed because TableTree inherits this behaviour from Composite.
	 * The items must be disposed separately. Because TableTree is not part of the
	 * org.eclipse.swt.widgets package, the method releaseWidget can not be overriden (this is how
	 * items are disposed of in Table and Tree). Instead, the items are disposed of in response to
	 * the dispose event on the TableTree. The "inDispose" flag is used to distinguish between
	 * disposing one TableTreeItem (e.g. when removing an entry from the TableTree) and disposing
	 * the entire TableTree.
	 */
	boolean inDispose = false;

	/**
	 * Creates a new instance of the widget.
	 * 
	 * @param parent
	 *            a composite widget
	 * @param style
	 *            the bitwise OR'ing of widget styles
	 */
	public TableTree(Composite parent, int style)
	{
		super(parent, SWT.NONE);
		table = new Table(this, style);
		setBackground(table.getBackground());
		setForeground(table.getForeground());
		setFont(table.getFont());
		createImages();

		Listener listener = new Listener() {
			public void handleEvent(Event e)
			{
				switch (e.type)
				{
					case SWT.MouseDown :
						onMouseDown(e);
					break;
					case SWT.Selection :
					case SWT.DefaultSelection :
						onSelection(e);
					break;
					case SWT.Dispose :
						onDispose();
					break;
					case SWT.Resize :
						onResize();
					break;
					case SWT.FocusIn :
						table.setFocus();
					break;
				}
			}
		};

		table.addListener(SWT.MouseDown, listener);
		table.addListener(SWT.Selection, listener);
		table.addListener(SWT.DefaultSelection, listener);
		addListener(SWT.Dispose, listener);
		addListener(SWT.Resize, listener);
		addListener(SWT.FocusIn, listener);

		configureCustomPaint();
	}

	public void setRedraw(boolean redraw)
	{
		super.setRedraw(redraw);
		table.setRedraw(redraw);
	}

	private void configureCustomPaint()
	{
		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event)
			{
				event.height = 40;
				int w = table.getColumn(event.index).getWidth();
				event.width = w < 40 ? 40 : w;
				Rectangle area = table.getClientArea();
				w = area.width - area.x;
				if (event.width > w)
					event.width = w;
			}
		});

		// TODO clean 
		final Color SELECTED_NODE_BG_COLOR = SWTHelper.createColor(197, 206, 216);
		final Color SELECTED_BG_COLOR = SWTHelper.createColor(167, 205, 241);

		table.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event)
			{
				boolean selected = (event.detail & SWT.SELECTED) != 0;
				GC gc = event.gc;
				Rectangle area = table.getClientArea();
				TableItem item = (TableItem) event.item;
				TableTreeItem treeItem = (TableTreeItem) event.item.getData();

				Color foreground = gc.getForeground();
				Color background = gc.getBackground();
				if (selected)
				{
					if (treeItem.isNode())
						gc.setBackground(SELECTED_NODE_BG_COLOR);
					else
						gc.setBackground(SELECTED_BG_COLOR);
					gc.fillRectangle(0, event.y, area.width, event.height);
				}
				else
				{
					gc.setForeground(item.getForeground());
					gc.setBackground(table.getBackground());
					gc.fillRectangle(0, event.y, event.width, event.height);
				}
				gc.setForeground(foreground);
				gc.setBackground(background);
				if (selected)
					event.detail &= ~SWT.SELECTED;

				/* disable the native drawing of this item */
				event.detail &= ~SWT.FOREGROUND;
			}
		});

		table.setFont(MailBoxView.NORMAL_FONT);

		GC gc = new GC(table);
		gc.setFont(MailBoxView.NORMAL_FONT);
		final FontMetrics fm = gc.getFontMetrics();
		final int FONT_HEIGHT = fm.getHeight();
		final String POINTS = "...";
		final int POINTS_WIDTH = gc.textExtent(POINTS).x;
		final int AVERAGE_CHAR_WIDTH = fm.getAverageCharWidth();
		gc.dispose();

		final Image MAIL_IMAGE = SWTHelper.loadImage("mail.gif");
		final int MAIL_IMAGE_WIDTH = MAIL_IMAGE.getBounds().width;
		final int ATTACHMENTS_IMAGE_WIDTH = MailBoxView.ATTACHED_FILES_IMAGE.getBounds().width;
		final int IMAGE_MARGIN = 10;
		final int IMAGE_PLUS_MARGIN = MAIL_IMAGE_WIDTH + IMAGE_MARGIN;
		final Color GROUP_COLOR = SWTHelper.createColor(54, 54, 54);

		final SimpleDateFormat SDF = new SimpleDateFormat("EEE dd/MM");
		final SimpleDateFormat SIMPLE_SDF = new SimpleDateFormat("HH:mm");
		final int[] DASH_LINE = new int[] {2, 2};

		final int TEXT_MARGIN = 3;
		final int HANDLE_WIDTH = IMAGE_SIZE + TEXT_MARGIN;

		table.addListener(SWT.PaintItem, new Listener() {
			public void handleEvent(Event event)
			{
				GC gc = event.gc;
				int column = event.index;
				TableItem item = (TableItem) event.item;
				TableTreeItem treeItem = (TableTreeItem) event.item.getData();
				StoredSmtpMessage msg = ((MailBoxItem) treeItem.getData()).getMessage();
				Image image = item.getImage(column);
				int x = event.x;
				int w = table.getColumn(event.index).getWidth();

				Rectangle r = item.getBounds();
				gc.setForeground(SWTHelper.getColor(SWT.COLOR_GRAY));
				if (treeItem.isNode())
					gc.setLineDash(DASH_LINE);

				gc.drawLine(0, r.y + r.height - 1, w, r.y + r.height - 1);

				if (treeItem.isNode())
				{
					gc.setLineStyle(SWT.LINE_SOLID);
					gc.setForeground(GROUP_COLOR);
					int y = event.y + event.height - FONT_HEIGHT - 2;
					if (image != null)
						gc.drawImage(image, x, y);
					x += HANDLE_WIDTH;
					String s = item.getText(column);
					if (w < (x + gc.textExtent(s).x))
					{
						int pos = (w - HANDLE_WIDTH - POINTS_WIDTH) / AVERAGE_CHAR_WIDTH;
						if (pos > 0)
							s = s.substring(0, pos + 1) + POINTS;
						else
							s = null;
					}
					if (s != null)
						gc.drawString(s, x, y, true);
				}
				else
				{
					gc.setForeground(SWTHelper.getColor(SWT.COLOR_BLACK));
					x += event.width;
					int y = event.y + 2;
					gc.drawImage(MAIL_IMAGE, 4, y);

					y += 2;

					if (!msg.isSeen())
						gc.setFont(MailBoxView.BOLD_FONT);

					Date d = msg.getMessageDate();
					String tmp = SDF.format(msg.getMessageDate());
					if (DateUtilities.isCurrentDay(d))
						tmp = SIMPLE_SDF.format(msg.getMessageDate());

					int tmpWidth = gc.textExtent(tmp).x;
					int dateX = w - tmpWidth - IMAGE_MARGIN;

					String t = msg.getMessageFrom();
					if (gc.textExtent(t).x > dateX - IMAGE_PLUS_MARGIN - 2)
					{
						int pos = (dateX - POINTS_WIDTH * 3 - IMAGE_PLUS_MARGIN) / AVERAGE_CHAR_WIDTH;

						t = (pos > 0 ? t.substring(0, pos) : "") + POINTS;
					}
					gc.drawString(t, IMAGE_PLUS_MARGIN, y, true);
					gc.drawString(tmp, dateX, y, true);

					gc.setForeground(SWTHelper.getColor(SWT.COLOR_DARK_GRAY));
					gc.setFont(MailBoxView.NORMAL_FONT);
					t = item.getText();
					boolean attachments = msg.getAttachedFilesCount() > 0;
					y += gc.getFontMetrics().getHeight();
					
					if (attachments)
					{
						int maxWidth = dateX + tmpWidth;
						if (gc.textExtent(t).x > maxWidth - IMAGE_PLUS_MARGIN)
						{
							int pos = (maxWidth - POINTS_WIDTH - IMAGE_PLUS_MARGIN - 10) / AVERAGE_CHAR_WIDTH;
							t = (pos > 0 ? t.substring(0, pos) : "") + POINTS;
						}
						gc.drawImage(MailBoxView.ATTACHED_FILES_IMAGE, maxWidth - ATTACHMENTS_IMAGE_WIDTH + 5, y);
					}
					else
					{
						if ((IMAGE_PLUS_MARGIN + gc.textExtent(t).x) > r.width)
						{
							int pos = (w - POINTS_WIDTH - IMAGE_PLUS_MARGIN) / AVERAGE_CHAR_WIDTH;
							t = (pos > 0 ? t.substring(0, pos) : "") + POINTS;
						}
					}
					gc.drawString(t, IMAGE_PLUS_MARGIN, y, true);
				}
			}
		});
	}

	int addItem(TableTreeItem item, int index)
	{
		if (index < 0 || index > items.length)
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT);
		TableTreeItem[] newItems = new TableTreeItem[items.length + 1];
		System.arraycopy(items, 0, newItems, 0, index);
		newItems[index] = item;
		System.arraycopy(items, index, newItems, index + 1, items.length - index);
		items = newItems;

		/* Return the index in the table where this table should be inserted */
		if (index == items.length - 1)
			return table.getItemCount();
		else
			return table.indexOf(items[index + 1].tableItem);
	}

	/**
	 * Adds the listener to receive selection events.
	 * <p>
	 * 
	 * @param listener
	 *            the selection listener
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_NULL_ARGUMENT when listener is null
	 *                </ul>
	 */
	public void addSelectionListener(SelectionListener listener)
	{
		if (listener == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	/**
	 * Adds the listener to receive tree events.
	 * <p>
	 * 
	 * @param listener
	 *            the tree listener
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_NULL_ARGUMENT when listener is null
	 *                </ul>
	 */
	public void addTreeListener(TreeListener listener)
	{
		if (listener == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Expand, typedListener);
		addListener(SWT.Collapse, typedListener);
	}

	/**
	 * Computes the preferred size of the widget.
	 * <p>
	 * Calculate the preferred size of the widget based on the current contents. The hint arguments
	 * allow a specific client area width and/or height to be requested. The hints may be honored
	 * depending on the platform and the layout.
	 * 
	 * @param wHint
	 *            the width hint (can be SWT.DEFAULT)
	 * @param hHint
	 *            the height hint (can be SWT.DEFAULT)
	 * @return a point containing the preferred size of the widget including trim
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public Point computeSize(int wHint, int hHint)
	{
		return table.computeSize(wHint, hHint, true);
	}

	/**
	 * Computes the widget trim.
	 * <p>
	 * Trim is widget specific and may include scroll bars and menu bar in addition to other
	 * trimmings that are outside of the widget's client area.
	 * 
	 * @param x
	 *            the x location of the client area
	 * @param y
	 *            the y location of the client area
	 * @param width
	 *            the width of the client area
	 * @param height
	 *            the height of the client area
	 * @return a rectangle containing the trim of the widget.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public Rectangle computeTrim(int x, int y, int width, int height)
	{
		return table.computeTrim(x, y, width, height);
	}

	/**
	 * Deselects all items.
	 * <p>
	 * If an item is selected, it is deselected. If an item is not selected, it remains unselected.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed
	 *                </ul>
	 */
	public void deselectAll()
	{
		table.deselectAll();
	}

	/**
	 * Expands upward from the specified leaf item.
	 */
	void expandItem(TableTreeItem item)
	{
		if (item == null)
			return;
		expandItem(item.parentItem);
		item.setExpanded(true);
		Event event = new Event();
		event.item = item;
		notifyListeners(SWT.Expand, event);
	}

	public TableTreeItem insertAtRealIndex(int index, int style)
	{
		int idx = 0;
		for (int i = 0; i < items.length; i++)
		{
			int count = items[i].getItemCount();

			if (style == SWT.MULTI)
			{
				if (idx == index)
					return new TableTreeItem(this, style, i);
			}
			else
			{
				if (count + idx + 1 == index)
					return new TableTreeItem(items[i], style);
				if (count + idx >= index)
					return new TableTreeItem(items[i], style, index - idx - 1);
			}
			idx += count + 1;
		}
		if (index == idx)
		{
			if (style == SWT.NONE)
				return new TableTreeItem(items[items.length - 1], style);
			else
				return new TableTreeItem(this, style);
		}
		else
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT, "insertAtRealIndex(" + index + ")");
	}

	public TableTreeItem getByRealIndex(int index)
	{
		int idx = 0;
		for (int i = 0; i < items.length; i++)
		{
			if (index == idx)
				return items[i];
			else
			{
				int count = items[i].getItemCount();

				if (count + idx >= index)
					return items[i].getItems()[index - idx - 1];
				idx += count + 1;
			}
		}

		throw new SWTError(SWT.ERROR_INVALID_ARGUMENT, "getByRealIndex(" + index + ")");
	}

	public void removeByRealIndex(int index)
	{
		int idx = 0;
		for (int i = 0; i < items.length; i++)
		{
			int count = items[i].getItemCount();
			if (index == idx)
			{
				if (count > 0)
					items[i].getItems()[0].dispose();
				else
					items[i].dispose();
				return;
			}
			else
			{
				if (count + idx >= index)
				{
					items[i].getItems()[index - idx - 1].dispose();
					return;
				}
				idx += count + 1;
			}
		}

		throw new SWTError(SWT.ERROR_INVALID_ARGUMENT, "removeByRealIndex(" + index + ")");
	}

	/**
	 * Gets the number of items.
	 * <p>
	 * 
	 * @return the number of items in the widget
	 */
	public int getItemCount()
	{
		return items.length;
	}

	/**
	 * Gets the height of one item.
	 * <p>
	 * This operation will fail if the height of one item could not be queried from the OS.
	 * 
	 * @return the height of one item in the widget
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_CANNOT_GET_ITEM_HEIGHT when the operation fails
	 *                </ul>
	 */
	public int getItemHeight()
	{
		return table.getItemHeight();
	}

	/**
	 * Gets the items.
	 * <p>
	 * 
	 * @return the items in the widget
	 * 
	 */
	public TableTreeItem[] getItems()
	{
		TableTreeItem[] newItems = new TableTreeItem[items.length];
		System.arraycopy(items, 0, newItems, 0, items.length);
		return newItems;
	}

	/**
	 * Gets the selected items.
	 * <p>
	 * This operation will fail if the selected items cannot be queried from the OS.
	 * 
	 * @return the selected items in the widget
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_CANNOT_GET_SELECTION when the operation fails</li>
	 *                </ul>
	 */
	public TableTreeItem[] getSelectedItems()
	{
		TableItem[] selection = table.getSelection();
		TableTreeItem[] result = new TableTreeItem[selection.length];
		for (int i = 0; i < selection.length; i++)
		{
			result[i] = (TableTreeItem) selection[i].getData();
		}
		return result;
	}

	/**
	 * Returns the underlying Table control.
	 * 
	 * @return the underlying Table control
	 */
	public Table getTable()
	{
		return table;
	}

	private void createImages()
	{
		int midpoint = IMAGE_MARGIN + IMAGE_ICON_SIZE / 2;
		int pt = IMAGE_MARGIN + IMAGE_ICON_SIZE;

		Color foreground = getForeground();
		Color gray = SWTHelper.createColor(89, 89, 89);
		Color black = SWTHelper.getColor(SWT.COLOR_BLACK);
		Color background = getBackground();

		/* Plus image */
		PaletteData palette = new PaletteData(new RGB[] {foreground.getRGB(), background.getRGB(), gray.getRGB()});
		ImageData imageData = new ImageData(IMAGE_SIZE, IMAGE_SIZE, 2, palette);
		imageData.transparentPixel = 1;
		plusImage = new Image(SWTHelper.getDisplay(), imageData);
		GC gc = new GC(plusImage);
		gc.setBackground(background);
		gc.fillRectangle(0, 0, IMAGE_SIZE, IMAGE_SIZE);
		gc.setForeground(gray);
		gc.drawPolygon(new int[] {IMAGE_MARGIN, IMAGE_MARGIN - 2, IMAGE_MARGIN, pt, midpoint + 1, midpoint - 1});
		gc.dispose();

		/* Minus image */
		palette = new PaletteData(new RGB[] {foreground.getRGB(), background.getRGB(), gray.getRGB(), black.getRGB()});
		imageData = new ImageData(IMAGE_SIZE, IMAGE_SIZE, 2, palette);
		imageData.transparentPixel = 1;
		minusImage = new Image(SWTHelper.getDisplay(), imageData);
		gc = new GC(minusImage);
		gc.setBackground(background);
		gc.fillRectangle(0, 0, IMAGE_SIZE, IMAGE_SIZE);
		gc.setBackground(gray);
		gc.fillPolygon(new int[] {IMAGE_MARGIN, pt, pt, pt, pt, IMAGE_MARGIN});
		gc.setForeground(black);
		gc.drawPolygon(new int[] {IMAGE_MARGIN + 1, pt, pt, pt, pt, IMAGE_MARGIN + 1});
		gc.dispose();
	}

	Image getPlusImage()
	{
		return plusImage;
	}

	Image getMinusImage()
	{
		return minusImage;
	}

	void onDispose()
	{
		inDispose = true;
		for (int i = 0; i < items.length; i++)
		{
			items[i].dispose();
		}
		inDispose = false;
		if (plusImage != null)
			plusImage.dispose();
		if (minusImage != null)
			minusImage.dispose();
		plusImage = minusImage = null;
	}

	void onResize()
	{
		Rectangle area = getClientArea();
		table.setBounds(0, 0, area.width-1, area.height-1);
	}

	void onSelection(Event e)
	{
		Event event = new Event();
		TableItem tableItem = (TableItem) e.item;
		TableTreeItem item = getItem(tableItem);
		event.item = item;

		if (e.type == SWT.Selection && e.detail == SWT.CHECK && item != null)
		{
			event.detail = SWT.CHECK;
			item.checked = tableItem.getChecked();
		}
		notifyListeners(e.type, event);
	}

	public TableTreeItem getItem(int index)
	{
		TableItem item = table.getItem(index);
		if (item != null)
			return (TableTreeItem) item.getData();
		else
			return null;
	}

	public TableTreeItem getItem(Point point)
	{
		TableItem item = table.getItem(point);
		if (item != null)
			return (TableTreeItem) item.getData();
		else
			return null;
	}

	private TableTreeItem getItem(TableItem tableItem)
	{
		if (tableItem != null)
			return (TableTreeItem) tableItem.getData();
		else
			return null;
	}

	void onMouseDown(Event event)
	{
		/* If user clicked on the [+] or [-], expand or collapse the tree. */
		TableItem i = table.getItem(new Point(event.x, event.y));
		if (i == null)
			return;
		Rectangle rect = i.getImageBounds(0);
		if (rect != null && rect.contains(event.x, event.y))
		{
			TableTreeItem item = (TableTreeItem) i.getData();
			event = new Event();
			event.item = item;
			item.setExpanded(!item.isExpanded());
			if (item.isExpanded())
				notifyListeners(SWT.Expand, event);
			else
				notifyListeners(SWT.Collapse, event);
		}
	}

	/**
	 * Removes all items.
	 * <p>
	 * This operation will fail when an item could not be removed in the OS.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_ITEM_NOT_REMOVED when the operation fails
	 *                </ul>
	 */
	public void removeAll()
	{
		setRedraw(false);
		for (int i = items.length - 1; i >= 0; i--)
		{
			items[i].dispose();
		}
		items = EMPTY_ITEMS;
		setRedraw(true);
	}

	public void removeItem(TableTreeItem item)
	{
		int index = 0;
		while (index < items.length && items[index] != item)
			index++;
		if (index == items.length)
			return;

		TableTreeItem[] newItems = new TableTreeItem[items.length - 1];
		System.arraycopy(items, 0, newItems, 0, index);
		System.arraycopy(items, index + 1, newItems, index, items.length - index - 1);
		items = newItems;
	}

	/**
	 * Removes the listener.
	 * <p>
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_NULL_ARGUMENT when listener is null
	 *                </ul>
	 */
	public void removeSelectionListener(SelectionListener listener)
	{
		if (listener == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	/**
	 * Removes the listener.
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread
	 *                <li>ERROR_WIDGET_DISPOSED when the widget has been disposed
	 *                <li>ERROR_NULL_ARGUMENT when listener is null
	 *                </ul>
	 */
	public void removeTreeListener(TreeListener listener)
	{
		if (listener == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		removeListener(SWT.Expand, listener);
		removeListener(SWT.Collapse, listener);
	}

	/**
	 * Sets the widget background color.
	 * <p>
	 * When new color is null, the background reverts to the default system color for the widget.
	 * 
	 * @param color
	 *            the new color (or null)
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setBackground(Color color)
	{
		super.setBackground(color);
		table.setBackground(color);
	}

	/**
	 * Sets the enabled state.
	 * <p>
	 * A disabled widget is typically not selectable from the user interface and draws with an
	 * inactive or grayed look.
	 * 
	 * @param enabled
	 *            the new enabled state
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		table.setEnabled(enabled);
	}

	/**
	 * Sets the widget font.
	 * <p>
	 * When new font is null, the font reverts to the default system font for the widget.
	 * 
	 * @param font
	 *            the new font (or null)
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setFont(Font font)
	{
		super.setFont(font);
		table.setFont(font);
	}

	/**
	 * Gets the widget foreground color.
	 * <p>
	 * 
	 * @return the widget foreground color
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setForeground(Color color)
	{
		super.setForeground(color);
		table.setForeground(color);
	}

	/**
	 * Sets the pop up menu.
	 * <p>
	 * Every control has an optional pop up menu that is displayed when the user requests a popup
	 * menu for the control. The sequence of key strokes/button presses/button releases that is used
	 * to request a pop up menu is platform specific.
	 * 
	 * @param menu
	 *            the new pop up menu
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_MENU_NOT_POP_UP when the menu is not a POP_UP</li> <li>
	 *                ERROR_NO_COMMON_PARENT when the menu is not in the same widget tree</li>
	 *                </ul>
	 */
	public void setMenu(Menu menu)
	{
		super.setMenu(menu);
		table.setMenu(menu);
	}

	/**
	 * Sets the selection.
	 * <p>
	 * 
	 * @param items
	 *            new selection
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_NULL_ARGUMENT when items is null
	 *                </ul>
	 */
	public void setSelection(TableTreeItem[] items)
	{
		TableItem[] tableItems = new TableItem[items.length];
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == null)
				throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
			if (!items[i].isVisible())
				expandItem(items[i]);
			tableItems[i] = items[i].tableItem;
		}
		table.setSelection(tableItems);
	}

	/**
	 * Sets the tool tip text.
	 * <p>
	 * 
	 * @param string
	 *            the new tool tip text (or null)
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setToolTipText(String string)
	{
		super.setToolTipText(string);
		table.setToolTipText(string);
	}

	/**
	 * Shows the item.
	 * <p>
	 * 
	 * @param item
	 *            the item to be shown
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed <li>
	 *                ERROR_NULL_ARGUMENT when item is null
	 *                </ul>
	 */
	public void showItem(TableTreeItem item)
	{
		if (item == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		if (!item.isVisible())
			expandItem(item);
		TableItem tableItem = item.tableItem;
		table.showItem(tableItem);
	}

	/**
	 * Shows the selection.
	 * <p>
	 * If there is no selection or the selection is already visible, this method does nothing. If
	 * the selection is scrolled out of view, the top index of the widget is changed such that
	 * selection becomes visible.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed
	 *                </ul>
	 */
	public void showSelection()
	{
		table.showSelection();
	}

	/**
	 * Gets the number of selected items.
	 * <p>
	 * This operation will fail if the number of selected items cannot be queried from the OS.
	 * 
	 * @return the number of selected items in the widget
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_CANNOT_GET_COUNT when the operation fails</li>
	 *                </ul>
	 */
	public int getSelectionCount()
	{
		return table.getSelectionCount();
	}

	/**
	 * Selects all items.
	 * <p>
	 * If an item is not selected, it is selected. If an item is selected, it remains selected.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed
	 *                </ul>
	 */
	public void selectAll()
	{
		table.selectAll();
	}
}