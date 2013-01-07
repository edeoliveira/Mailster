package org.mailster.gui.views.mailview.cfilelist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;

public class CFileItem
	extends Item
{
	private CFileList parent;
	private String toolTipText;
	
	/**
	 * Constructs a new instance of this class given its parent (which must be a <code>PShelf</code>
	 * ), a style value describing its behavior and appearance, and the index at which to place it
	 * in the items maintained by its parent.
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
	 * @param index
	 *            the zero-relative index to store the receiver in its parent
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of
	 *                elements in the parent (inclusive)</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created
	 *                the parent</li>
	 *                <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 *                </ul>
	 */
	public CFileItem(CFileList parent, int style, int index)
	{
		super(parent, style, index);

		if (index < 0 || index > parent.getItems().length)
			SWT.error(SWT.ERROR_INVALID_RANGE);

		construct(parent, index);
	}

	/**
	 * Constructs a new instance of this class given its parent (which must be a <code>PShelf</code>
	 * ) and a style value describing its behavior and appearance. The item is added to the end of
	 * the items maintained by its parent.
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
	public CFileItem(CFileList parent, int style)
	{
		super(parent, style);
		construct(parent, -1);
	}

	private void construct(CFileList parent, int index)
	{
		this.parent = parent;
		parent.createItem(this, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose()
	{
		checkWidget();
		parent.remove(this);
		super.dispose();
	}

	void internalDispose()
	{
		checkWidget();
		super.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setImage(Image image)
	{
		super.setImage(image);
		if (parent.computeItemSize())
		{
			parent.onResize();
			parent.redraw();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setText(String string)
	{
		super.setText(string);
		if (parent.computeItemSize())
		{
			parent.onResize();
			parent.redraw();
		}
	}
	
	String getToolTipText()
	{
		return toolTipText;
	}
	
	public void setToolTipText(String string)
	{
		toolTipText = string;
	}
	
	public Rectangle getBounds()
	{
		return parent.getItemBounds(this);
	}
}
