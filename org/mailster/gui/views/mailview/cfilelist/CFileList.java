package org.mailster.gui.views.mailview.cfilelist;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;

public class CFileList
	extends Canvas
{
	private ArrayList<CFileItem> items = new ArrayList<CFileItem>();
	private ArrayList<Point> coords = new ArrayList<Point>();
	
	private AbstractRenderer renderer;
	private CFileItem selectedItem;
	private CFileItem focusItem;
	private CFileItem mouseDownItem;
	
	private CFileItem hoverItem;

	private int itemWidth = 0;
	private int itemHeight = 0;
	private int cols = 0;
	private int rows = 0;

	private boolean isFocused;
	
	private static int checkStyle(int style)
	{
		int mask = SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT | SWT.BORDER | SWT.SIMPLE;
		return (style & mask) | SWT.DOUBLE_BUFFERED;
	}

	/**
	 * Constructs a new instance of this class given its parent and a style value describing its
	 * behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class <code>SWT</code> which
	 * is applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing
	 * together (that is, using the <code>int</code> "|" operator) two or more of those
	 * <code>SWT</code> style constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of control to construct
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 *                </ul>
	 */
	public CFileList(Composite parent, int style)
	{
		super(parent, checkStyle(style));
		setRenderer(new DefaultCFileListRenderer());

		this.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e)
			{
				onPaint(e.gc);
			}
		});

		this.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event)
			{
				onResize();
			}
		});

		this.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e)
			{
				CFileItem item = getItem(new Point(e.x, e.y));
				if (item == null)
					return;
				if (item == mouseDownItem)
					selectItem(item);
				
				Event ev = new Event();
				ev.item = selectedItem;
				CFileList.this.notifyListeners(SWT.Selection, ev);
			}

			public void mouseDown(MouseEvent e)
			{
				mouseDownItem = getItem(new Point(e.x, e.y));
			}
		});

		this.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0)
			{
				onDispose();
			}
		});

		this.addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseExit(MouseEvent e)
			{
				CFileItem item = hoverItem;
				hoverItem = null;
				redrawItem(item);
			}
		});

		this.addMouseMoveListener(new MouseMoveListener() {
			
			Cursor hand = getDisplay().getSystemCursor(SWT.CURSOR_HAND);
			Cursor arrow = getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
				
			public void mouseMove(MouseEvent e)
			{
				CFileItem old = hoverItem;
				CFileItem item = getItem(new Point(e.x, e.y));

				if (item != old)
				{
					hoverItem = item;
					if (item != null)
					{
						setCursor(hand);
						setToolTipText(item.getToolTipText());
					}
					else
					{
						setCursor(arrow);
						setToolTipText(null);
					}
					
					redrawItem(false, old, item);
				}
			}
		});

		this.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e)
			{
				isFocused = false;
				if (focusItem != null)
					redrawItem(focusItem);
			}

			public void focusGained(FocusEvent arg0)
			{
				isFocused = true;
			}
		});
	}

	/**
	 * Sets the renderer.
	 * 
	 * @param renderer
	 *            the new renderer
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the renderer is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver or the renderer has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setRenderer(AbstractRenderer renderer)
	{
		checkWidget();

		if (renderer == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		if (renderer.isDisposed())
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);

		if (this.renderer != null)
			this.renderer.dispose();

		this.renderer = renderer;
		renderer.initialize(this);

		if (computeItemSize())
		{
			onResize();
			redraw();
		}
	}

	/**
	 * Returns the renderer.
	 * 
	 * @return the renderer
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public AbstractRenderer getRenderer()
	{
		checkWidget();
		return renderer;
	}

	private void computeGridSize(int width)
	{
		cols = itemWidth == 0 ? 1 : width / itemWidth;
		if (cols == 0)
			cols = 1;

		rows = items.size() / cols;
		if (rows == 0)
			rows = 1;
		
		//System.out.println("computeGridSize("+width+") -> #rows:"+rows+"#cols:"+cols);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(int wHint, int hHint, boolean changed)
	{
		checkWidget();

		Rectangle r = getClientArea();
		int width = wHint == SWT.DEFAULT ? r.width : wHint;

		Point pt = null;
		if (hHint != SWT.DEFAULT)
			pt = new Point(width, hHint);
		else
		{
			computeGridSize(width);
			pt = new Point(Math.max(cols * itemWidth, wHint), rows * itemHeight);
		}
		//System.out.println("computeSize("+wHint+","+hHint+","+changed+")="+pt);
		return pt;
	}

	private void onDispose()
	{
		renderer.dispose();
	}

	private void paintItem(GC gc, int index, CFileItem item, boolean all)
	{
		gc.setBackground(getBackground());
		gc.setForeground(getForeground());			
		
		Point pt = coords.get(index);
		renderer.setBounds(pt.x, pt.y, itemWidth, itemHeight);
		renderer.setSelected(item == selectedItem);
		renderer.setFocus(isFocused && focusItem == item);
		renderer.setHover(item == hoverItem);
		renderer.setFullRedraw(all);
		
		renderer.paint(gc, item);		
	}
	
	private void onPaint(GC gc)
	{
		gc.setAdvanced(true);
		if (gc.getAdvanced())
			gc.setTextAntialias(SWT.ON);

		int index = 0;
		for (Iterator<CFileItem> iter = items.iterator(); iter.hasNext();)
		{
			paintItem(gc, index++, iter.next(), true);
		}
	}

	private void redrawItem(CFileItem item)
	{
		redrawItem(true, item);
	}
	
	private void redrawItem(boolean all, CFileItem... itemArray)
	{
		GC gc = new GC(this);
		gc.setAdvanced(true);
		if (gc.getAdvanced())
			gc.setTextAntialias(SWT.ON);
		
		for(CFileItem item : itemArray)
		{
			int index = items.indexOf(item);
			if (index == -1)
				continue;

			paintItem(gc, index, item, all);
		}
		gc.dispose();
	}
	
	private void computeCoordinates()
	{
		coords.clear();

		int row = 0;
		int col = 0;
		for (Iterator<CFileItem> iter = items.iterator(); iter.hasNext();)
		{
			iter.next();
			coords.add(new Point(col * itemWidth, row * itemHeight));
			if (col + 1 == cols)
			{
				row++;
				col = 0;
			}
			else
				col++;
		}
	}

	void createItem(CFileItem item, int index)
	{
		if (index == -1)
			items.add(item);
		else
			items.add(index, item);

		computeItemSize();
		onResize();
	}

	public void removeAll()
	{
		Iterator<CFileItem> it = items.iterator();
		while(it.hasNext())
			it.next().internalDispose();

		items.clear();
		selectedItem = null;
		focusItem = null;
		
		computeItemSize();
		onResize();
	}
	
	void remove(CFileItem item)
	{
		if (selectedItem == item)
			selectedItem = null;
		if (focusItem == item)
			focusItem = null;

		items.remove(item);
		
		computeItemSize();
		onResize();
	}

	void onResize()
	{
		int wHint = getClientArea().width;
		//System.out.println("onResize() -> "+getClientArea()+" #items="+items.size()+" wHint="+wHint+" w/h: "+itemWidth+"/"+itemHeight);
		
		computeGridSize(wHint);
		computeCoordinates();
	}

	boolean computeItemSize()
	{
		int _itemWidth = itemWidth;
		int _itemHeight = itemHeight;
		
		GC gc = new GC(this);

		for (Iterator<CFileItem> iter = items.iterator(); iter.hasNext();)
		{
			CFileItem item = iter.next();
			Point pt = renderer.computeSize(gc, SWT.DEFAULT, SWT.DEFAULT, item);
			itemWidth = Math.max(pt.x, itemWidth);
			itemHeight = Math.max(pt.y, itemHeight);
		}
		
		//System.out.println("computeItemSize() -> #itemWidth:"+itemWidth+" #itemHeight:"+itemHeight);
		gc.dispose();
		
		return itemWidth != _itemWidth || itemHeight != _itemHeight;
	}

	/**
	 * Returns the item at the given location.
	 * 
	 * @param point
	 *            location
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver or the renderer has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public CFileItem getItem(Point pt)
	{
		checkWidget();

		if (pt == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		int col = pt.x / itemWidth;
		int row = pt.y / itemHeight;
		
		int idx = row*cols+col;
		if (col >= cols || row > rows || idx >= items.size())
			return null;
		
		return items.get(idx);
	}

	/**
	 * Sets the receiver's selection to the given item.
	 * 
	 * @param item
	 *            the item to select
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public void setSelection(CFileItem item)
	{
		checkWidget();

		if (item == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		if (!items.contains(item))
			return;

		if (selectedItem == item)
			return;

		selectItem(item);
	}

	private void selectItem(CFileItem item)
	{
		if (forceFocus())
			focusItem = item;
		
		if (item == selectedItem)
		{
			redrawItem(item);			
			return;
		}
		
		CFileItem oldSelected = selectedItem;
		selectedItem = item;

		redrawItem(item);			
		
		if (oldSelected != null)
			redrawItem(oldSelected);
	}

	/**
	 * Returns the <code>CFileItem</code> that is currently selected in the receiver.
	 * 
	 * @return the currently selected item
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public CFileItem getSelection()
	{
		checkWidget();
		return selectedItem;
	}

	/**
	 * Returns an array of <code>CFileItem</code>s which are the items in the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain its list of items, so
	 * modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the items in the receiver
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 */
	public CFileItem[] getItems()
	{
		checkWidget();
		return (CFileItem[]) items.toArray(new CFileItem[items.size()]);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the receiver's
	 * selection changes, by sending it one of the messages defined in the
	 * <code>SelectionListener</code> interface.
	 * <p>
	 * When <code>widgetSelected</code> is called, the item field of the event object is valid.
	 * <code>widgetDefaultSelected</code> is not called.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 * @see SelectionEvent
	 */
	public void addSelectionListener(SelectionListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified when the
	 * receiver's selection changes.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the receiver</li>
	 *                </ul>
	 * 
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(SelectionListener listener)
	{
		checkWidget();
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);

		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	public Rectangle getItemBounds(CFileItem item)
	{
		Point pt = coords.get(items.indexOf(item));
		return new Rectangle(pt.x, pt.y, itemWidth, itemHeight);
	}
}
