package org.mailster.gui.views.mailbox;

/*
 * (c) Copyright IBM Corp. 2000, 2001. All Rights Reserved
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * A MailBoxTableTreeItem is a selectable user interface object that represents an item in a hierarchy of
 * items in a MailBoxTableTree.
 */
public class MailBoxTableTreeItem
	extends Item
{
	static final String[] EMPTY_TEXTS = new String[0];
	static final Image[] EMPTY_IMAGES = new Image[0];

	TableItem tableItem;
	MailBoxTableTree parent;
	MailBoxTableTreeItem parentItem;
	MailBoxTableTreeItem[] items = MailBoxTableTree.EMPTY_ITEMS;
	String[] texts = EMPTY_TEXTS;
	Image[] images = EMPTY_IMAGES;
	Object data;
	boolean expanded;
	boolean checked;

	/**
	 * Create a new instance of a root item.
	 * 
	 * @param parent
	 *            the MailBoxTableTree that contains this root item
	 * @param style
	 *            the bitwise OR'ing of widget styles
	 */
	public MailBoxTableTreeItem(MailBoxTableTree parent, int style)
	{
		this(parent, style, parent.getItemCount());
	}

	/**
	 * Create a new instance of a root item in the position indicated by the specified index.
	 * 
	 * @param parent
	 *            the MailBoxTableTree that contains this root item
	 * @param style
	 *            the bitwise OR'ing of widget styles
	 * @param index
	 *            specifies the position of this item in the MailBoxTableTree relative to other root items
	 */
	public MailBoxTableTreeItem(MailBoxTableTree parent, int style, int index)
	{
		this(parent, null, style, index);
	}

	/**
	 * Create a new instance of a sub item.
	 * 
	 * @param parent
	 *            this item's parent in the hierarchy of MailBoxTableTree items
	 * @param style
	 *            the bitwise OR'ing of widget styles
	 */
	public MailBoxTableTreeItem(MailBoxTableTreeItem parent, int style)
	{
		this(parent, style, parent.getItemCount());
	}

	/**
	 * Create a new instance of a sub item in the position indicated by the specified index.
	 * 
	 * @param parent
	 *            this item's parent in the hierarchy of MailBoxTableTree items
	 * @param style
	 *            the bitwise OR'ing of widget styles
	 * @param index
	 *            specifies the position of this item in the MailBoxTableTree relative to other children of
	 *            the same parent
	 */
	public MailBoxTableTreeItem(MailBoxTableTreeItem parent, int style, int index)
	{
		this(parent.getParent(), parent, style, index);
	}

	MailBoxTableTreeItem(MailBoxTableTree parent, MailBoxTableTreeItem parentItem, int style, int index)
	{
		super(parent, style);
		this.parent = parent;
		this.parentItem = parentItem;
		if (parentItem == null)
		{
			// Root items are visible immediately
			int tableIndex = parent.addItem(this, index);
			tableItem = new TableItem(parent.getTable(), style, tableIndex);
			tableItem.setData(this);
			addCheck();
			if (isNode())
			{
				Image image = expanded ? parent.getMinusImage() : parent.getPlusImage();
				if (parentItem == null)
					tableItem.setImage(0, image);
			}
		}
		else
		{
			parentItem.addItem(this, index);
		}
	}

	boolean isNode()
	{
		return (getStyle() & SWT.MULTI) > 0;
	}

	void addCheck()
	{
		Table table = parent.getTable();
		if ((table.getStyle() & SWT.CHECK) == 0)
			return;
		tableItem.setChecked(checked);
	}

	void addItem(MailBoxTableTreeItem item, int index)
	{
		if (item == null)
			throw new SWTError(SWT.ERROR_NULL_ARGUMENT);
		if (index < 0 || index > items.length)
			throw new SWTError(SWT.ERROR_INVALID_ARGUMENT, index + "");

		/* Put the item in the items list */
		MailBoxTableTreeItem[] newItems = new MailBoxTableTreeItem[items.length + 1];
		System.arraycopy(items, 0, newItems, 0, index);
		newItems[index] = item;
		System.arraycopy(items, index, newItems, index + 1, items.length - index);
		items = newItems;
		if (expanded)
			item.setVisible(true);
	}

	/**
	 * Gets the widget bounds at the specified index.
	 * <p>
	 * 
	 * @return the widget bounds at the specified index
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public Rectangle getBounds(int index)
	{
		if (tableItem != null)
			return tableItem.getBounds(index);
		else
			return new Rectangle(0, 0, 0, 0);
	}

	/**
	 * Gets the checked state.
	 * <p>
	 * 
	 * @return the item checked state.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public boolean getChecked()
	{
		if (tableItem == null)
			return checked;
		return tableItem.getChecked();
	}

	/**
	 * Gets the Display.
	 * <p>
	 * This method gets the Display that is associated with the widget.
	 * 
	 * @return the widget data
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public Display getDisplay()
	{
		if (parent == null)
			throw new SWTError(SWT.ERROR_WIDGET_DISPOSED);
		return parent.getDisplay();
	}

	/**
	 * Gets the expanded state of the widget.
	 * <p>
	 * 
	 * @return a boolean that is the expanded state of the widget
	 */
	public boolean isExpanded()
	{
		return expanded;
	}

	/**
	 * Gets the first image.
	 * <p>
	 * The image in column 0 is reserved for the [+] and [-] images of the tree, therefore
	 * getImage(0) will return null.
	 * 
	 * @return the image at index 0
	 */
	public Image getImage()
	{
		return getImage(0);
	}

	/**
	 * Gets the image at the specified index.
	 * <p>
	 * Indexing is zero based. The image can be null. The image in column 0 is reserved for the [+]
	 * and [-] images of the tree, therefore getImage(0) will return null. Return null if the index
	 * is out of range.
	 * 
	 * @param index
	 *            the index of the image
	 * @return the image at the specified index or null
	 */
	public Image getImage(int index)
	{
		if (0 < index && index < images.length)
			return images[index];
		return null;
	}

	int getIndent()
	{
		if (parentItem == null)
			return 0;
		return parentItem.getIndent() + 1;
	}

	/**
	 * Gets the number of sub items.
	 * <p>
	 * 
	 * @return the number of sub items
	 */
	public int getItemCount()
	{
		return items.length;
	}

	/**
	 * Gets the sub items.
	 * <p>
	 * 
	 * @return the sub items
	 */
	public MailBoxTableTreeItem[] getItems()
	{
		MailBoxTableTreeItem[] newItems = new MailBoxTableTreeItem[items.length];
		System.arraycopy(items, 0, newItems, 0, items.length);
		return newItems;
	}

	MailBoxTableTreeItem getItem(TableItem tableItem)
	{
		if (tableItem == null)
			return null;
		if (this.tableItem == tableItem)
			return this;
		for (int i = 0; i < items.length; i++)
		{
			MailBoxTableTreeItem item = items[i].getItem(tableItem);
			if (item != null)
				return item;
		}
		return null;
	}

	/**
	 * Gets the parent.
	 * <p>
	 * 
	 * @return the parent
	 */
	public MailBoxTableTree getParent()
	{
		return parent;
	}

	/**
	 * Gets the parent item.
	 * <p>
	 * 
	 * @return the parent item.
	 */
	public MailBoxTableTreeItem getParentItem()
	{
		return parentItem;
	}

	/**
	 * Gets the first item text.
	 * <p>
	 * 
	 * @return the item text at index 0, which can be null
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_CANNOT_GET_TEXT when the operation fails</li>
	 *                </ul>
	 */
	public String getText()
	{
		return getText(0);
	}

	/**
	 * Gets the item text at the specified index.
	 * <p>
	 * Indexing is zero based.
	 * 
	 * This operation will fail when the index is out of range or an item could not be queried from
	 * the OS.
	 * 
	 * @param index
	 *            the index of the item
	 * @return the item text at the specified index, which can be null
	 */
	public String getText(int index)
	{
		if (0 <= index && index < texts.length)
			return texts[index];
		return null;
	}

	public Object getData()
	{
		return data;
	}

	boolean isVisible()
	{
		return tableItem != null;
	}

	/**
	 * Gets the index of the specified item.
	 * 
	 * <p>
	 * The widget is searched starting at 0 until an item is found that is equal to the search item.
	 * If no item is found, -1 is returned. Indexing is zero based. This index is relative to the
	 * parent only.
	 * 
	 * @param item
	 *            the search item
	 * @return the index of the item or -1 if the item is not found
	 * 
	 */
	public int indexOf(MailBoxTableTreeItem item)
	{
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == item)
				return i;
		}
		return -1;
	}

	int expandedIndexOf(MailBoxTableTreeItem item)
	{
		int index = 0;
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == item)
				return index;
			if (items[i].expanded)
				index += items[i].visibleChildrenCount();
			index++;
		}
		return -1;
	}

	int visibleChildrenCount()
	{
		int count = 0;
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].isVisible())
				count += 1 + items[i].visibleChildrenCount();
		}
		return count;
	}

	public void dispose()
	{
		for (int i = items.length - 1; i >= 0; i--)
		{
			items[i].dispose();
		}
		super.dispose();
		if (!parent.inDispose)
		{
			if (parentItem != null)
			{
				parentItem.removeItem(this);
			}
			else
			{
				parent.removeItem(this);
			}

			if (tableItem != null)
				tableItem.dispose();
		}
		items = null;
		parentItem = null;
		parent = null;
		images = null;
		texts = null;
		data = null;
		tableItem = null;
	}

	void removeItem(MailBoxTableTreeItem item)
	{
		int index = 0;
		while (index < items.length && items[index] != item)
			index++;
		if (index == items.length)
			return;

		MailBoxTableTreeItem[] newItems = new MailBoxTableTreeItem[items.length - 1];
		System.arraycopy(items, 0, newItems, 0, index);
		System.arraycopy(items, index + 1, newItems, index, items.length - index - 1);
		items = newItems;
	}

	/**
	 * Sets the checked state.
	 * <p>
	 * 
	 * @param checked
	 *            the new checked state.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setChecked(boolean checked)
	{
		if (tableItem != null)
		{
			tableItem.setChecked(checked);
		}
		this.checked = checked;
	}

	/**
	 * Sets the expanded state.
	 * <p>
	 * 
	 * @param expanded
	 *            the new expanded state.
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setExpanded(boolean expanded)
	{
		if (!isNode()|| this.expanded == expanded)
			return;
		this.expanded = expanded;
		if (tableItem == null)
			return;
		parent.setRedraw(false);
		for (int i = 0; i < items.length; i++)
		{
			items[i].setVisible(expanded);
		}
		Image image = expanded ? parent.getMinusImage() : parent.getPlusImage();
		tableItem.setImage(0, image);
		parent.setRedraw(true);
	}

	/**
	 * Sets the image at an index.
	 * <p>
	 * The image can be null. The image in column 0 is reserved for the [+] and [-] images of the
	 * tree, therefore do nothing if index is 0.
	 * 
	 * @param image
	 *            the new image or null
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
	 *                </ul>
	 */
	public void setImage(int index, Image image)
	{
		int columnCount = Math.max(parent.getTable().getColumnCount(), 1);
		if (index <= 0 || index >= columnCount)
			return;
		if (images.length < columnCount)
		{
			Image[] newImages = new Image[columnCount];
			System.arraycopy(images, 0, newImages, 0, images.length);
			images = newImages;
		}
		images[index] = image;
		if (tableItem != null)
			tableItem.setImage(index, image);
	}

	/**
	 * Sets the first image.
	 * <p>
	 * The image can be null. The image in column 0 is reserved for the [+] and [-] images of the
	 * tree, therefore do nothing.
	 * 
	 * @param image
	 *            the new image or null
	 */
	public void setImage(Image image)
	{
		setImage(0, image);
	}

	/**
	 * Sets the widget text.
	 * <p>
	 * 
	 * The widget text for an item is the label of the item or the label of the text specified by a
	 * column number.
	 * 
	 * @param index
	 *            the column number
	 * @param text
	 *            the new text
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_NULL_ARGUMENT when string is null</li>
	 *                </ul>
	 */
	public void setText(int index, String text)
	{
		int columnCount = Math.max(parent.getTable().getColumnCount(), 1);
		if (index < 0 || index >= columnCount)
			return;
		if (texts.length < columnCount)
		{
			String[] newTexts = new String[columnCount];
			System.arraycopy(texts, 0, newTexts, 0, texts.length);
			texts = newTexts;
		}
		texts[index] = text;
		if (tableItem != null)
			tableItem.setText(index, text);
	}

	public void setData(Object data)
	{
		this.data = data;
	}

	/**
	 * Sets the widget text.
	 * <p>
	 * 
	 * The widget text for an item is the label of the item or the label of the text specified by a
	 * column number.
	 * 
	 * @param index
	 *            the column number
	 * @param text
	 *            the new text
	 * 
	 * @exception SWTError
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li> <li>
	 *                ERROR_WIDGET_DISPOSED when the widget has been disposed</li> <li>
	 *                ERROR_NULL_ARGUMENT when string is null</li>
	 *                </ul>
	 */
	public void setText(String string)
	{
		setText(0, string);
	}

	void setVisible(boolean show)
	{
		if (parentItem == null)
			return; // this is a root and can not be toggled between visible and hidden

		if (isVisible() == show)
			return;

		if (show)
		{
			// parentItem must already be visible
			if (!parentItem.isVisible())
				return;

			// create underlying table item and set data in table item to stored data
			Table table = parent.getTable();
			int parentIndex = table.indexOf(parentItem.tableItem);
			int index = parentItem.expandedIndexOf(this) + parentIndex + 1;
			if (index < 0)
				return;
			tableItem = new TableItem(table, getStyle(), index);
			tableItem.setData(this);
			addCheck();

			// restore fields to item
			// ignore any images in the first column
			int columnCount = Math.max(table.getColumnCount(), 1);
			for (int i = 0; i < columnCount; i++)
			{
				if (i < texts.length && texts[i] != null)
					setText(i, texts[i]);
				if (i < images.length && images[i] != null)
					setImage(i, images[i]);
			}

			// display the children and the appropriate [+]/[-] symbol as
			// required
			if (items.length != 0)
			{
				if (expanded)
				{
					tableItem.setImage(0, parent.getMinusImage());
					for (int i = 0, length = items.length; i < length; i++)
						items[i].setVisible(true);
				}
				else
					tableItem.setImage(0, parent.getPlusImage());
			}
		}
		else
		{

			for (int i = 0, length = items.length; i < length; i++)
				items[i].setVisible(false);

			// remove row from table
			tableItem.dispose();
			tableItem = null;
		}
	}
}
