/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005-2006 Feed'n Read Development Team                        *
 * http://sourceforge.net/fnr                                                  *
 *                                                                             *
 * All rights reserved.                                                        *
 *                                                                             *
 * This program and the accompanying materials are made available under the    *
 * terms of the Common Public License v1.0 which accompanies this distribution,*
 * and is available at                                                         *
 * http://www.eclipse.org/legal/cpl-v10.html                                   *
 *                                                                             *
 * A copy is found in the file cpl-v10.html and important notices to the       *
 * license from the team is found in the textfile LICENSE.txt distributed      *
 * in this package.                                                            *
 *                                                                             *
 * This copyright notice MUST APPEAR in all copies of the file.                *
 *                                                                             *
 * Contributors:                                                               *
 *    Feed'n Read - initial API and implementation                             *
 *                  (smachhau@users.sourceforge.net)                           *
 *******************************************************************************/
package org.mailster.gui.prefs.widgets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.mailster.util.StringUtilities;

/**
 * Generic <code>ViewerSorter</code> extension for <code>Viewer</code>
 * instances using <code>ITableContentProvider</code> implementations.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 */
public class TableViewerSorter extends ViewerSorter
{
    /**
     * The column that the sorting is done by
     */
    private int sortingColumn;

    /**
     * <code>true</code> indicates ascending (default), <code>false</code>
     * descending sort order
     */
    private boolean ascending = true;

    /**
     * The <code>Viewer</code> that the sorting is done for
     */
    private Viewer viewer;

    /**
     * The <code>ITableContentProvider</code> used to query the underlying
     * model
     */
    private ITableContentProvider contentProvider;

    /**
     * Creates a new <code>TableViewerSorter</code> instance linked to the
     * specified <code>Viewer</code>.
     * 
     * @param viewer the <code>Viewer</code> to link this
     *            <code>TableViewerSorter</code> to
     */
    public TableViewerSorter(Viewer viewer,
            ITableContentProvider contentProvider)
    {
        this.viewer = viewer;
        this.contentProvider = contentProvider;
    }

    /**
     * Gets the column index by which the sorting is done.
     * 
     * @return the column index by which the sorting is done
     * @see #getSortingColumn()
     */
    public int getSortingColumn()
    {
        return (this.sortingColumn);
    }

    /**
     * Sets the column index by which the sorting is to be done.
     * 
     * @param columnIndex the column index by which the sorting is to be done
     * @see #getSortingColumn()
     */
    public void setSortingColumn(int columnIndex)
    {
        this.sortingColumn = columnIndex;
    }

    /**
     * Gets the sort order; <code>true<Code> indicates ascending,
     * <code>false</code> descending sort order.
     *
     * @return <code>true<Code> for ascending, <code>false</code> for descending
     * sort order
     * 
     * @see #setAscending(boolean)
     */
    public boolean isAscending()
    {
        return (this.ascending);
    }

    /**
     * Sets the sort order to be used; <code>true<Code> indicates ascending,
     * <code>false</code> descending sort order.
     *
     * @param ascending <code>true<Code> for ascending, <code>false</code> for
     * descending sort order
     * 
     * @see #isAscending()
     */
    public void setAscending(boolean ascending)
    {
        this.ascending = ascending;
    }

    /**
     * Sorts the underlying model data and refreshes the associated
     * <code>TableViewer</code> to reflect the new sorting.
     */
    public void sort()
    {
        this.viewer.refresh();
    }

    /**
     * Returns a negative, zero, or positive number depending on whether the
     * first element is less than, equal to, or greater than the second element.
     * 
     * @param viewer the viewer
     * @param e1 the first element
     * @param e2 the second element
     * @return a negative number if the first element is less than the second
     *         element; the value <code>0</code> if the first element is equal
     *         to the second element; and a positive number if the first element
     *         is greater than the second element
     */
    @SuppressWarnings("unchecked")
    public int compare(Viewer viewer, Object e1, Object e2)
    {
        /* Evaluate element categories first */
        int category1 = this.category(e1);
        int category2 = this.category(e2);
        if (category1 != category2)
        {
            return (category1 - category2);
        }

        /*
         * Get the value of the first argument for the current sorting column
         * and prevent null values.
         */
        Object value1 = this.contentProvider.getColumnValue(e1, this
                .getSortingColumn());

        /*
         * Get the value of the second argument for the current sorting column
         * and prevent null values.
         */
        Object value2 = this.contentProvider.getColumnValue(e2, this
                .getSortingColumn());

        if (value1 instanceof String && value2 instanceof String)
        {
            /* Prevent null values */
            if (value1 == null)
            {
                value1 = StringUtilities.EMPTY_STRING;
            }
            if (value2 == null)
            {
                value2 = StringUtilities.EMPTY_STRING;
            }

            /* Compare two String objects with the internal Collator */
            return (this.isAscending()
                    ? this.getComparator().compare(value1, value2)
                    : (-this.getComparator().compare(value1, value2)));
        }
        else
        {
            if (value1 == null && value2 == null)
            {
                /* Consider both values to be equal. */
                return (0);
            }
            else if (value1 != null && value2 == null)
            {
                /*
                 * Always consider value1 as the non null value greater than
                 * value2 as the null value. The sort order is ignored in this
                 * case.
                 */
                return (-1);
            }
            else if (value1 == null && value2 != null)
            {
                /*
                 * Always consider value2 as the non null value greater than
                 * value1 as the null value. The sort order is ignored in this
                 * case.
                 */
                return (1);
            }
            else if (value1 instanceof Comparable
                    && value2 instanceof Comparable)
            {
                /*
                 * Compare value1 and value2 based on the Comparable
                 * compareTo(Object) method.
                 */
                return (this.isAscending() ? ((Comparable) value1)
                        .compareTo(value2) : -((Comparable) value1)
                        .compareTo(value2));
            }
            else
            {
                /*
                 * Convert both Objects to String objects and make use of the
                 * internal Collator
                 */
                return (this.isAscending() ? this.getComparator().compare(value1,
                        value2) : (-this.getComparator().compare(value1, value2)));
            }
        }
    }
}
