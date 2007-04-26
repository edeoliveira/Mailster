package org.mailster.gui.glazedlists;

import org.eclipse.swt.widgets.TableItem;

import ca.odell.glazedlists.gui.TableFormat;

public interface ExtendedTableFormat<E> extends TableFormat<E>
{
    public void setupItem(TableItem item, E value, int realIndex);
}
