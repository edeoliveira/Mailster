/*******************************************************************************
 * Copyright notice                                                            *
 *                                                                             *
 * Copyright (c) 2005 Feed'n Read Development Team                             *
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
package org.mailster.gui.utils;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

/**
 * Contains utility methods to create SWT <code>Layouts</code>.
 * 
 * @author <a href="mailto:smachhau@users.sourceforge.net">Sebastian Machhausen</a>
 * 
 * This file has been used and modified.
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 */
public class LayoutUtils
{
    /**
     * Creates a new <code>GridLayout</code> using the specified settings.
     * 
     * @param columns the number of cell columns
     * @param makeColumnsEqualWidth <code>true</code> to force all columns to
     *            have the same width; <code>false</code> if not to force sam
     *            width
     * @param marginHeight the number of pixels of vertical margin that will be
     *            placed along the top and bottom edges of the layout
     * @param marginWidth the number of pixels of horizontal margin that will be
     *            placed along the left and right edges of the layout
     * @param marginTop the number of pixels of vertical margin that will be
     *            placed along the top edge of the layout
     * @param marginBottom the number of pixels of vertical margin that will be
     *            placed along the bottom edge of the layout
     * @param marginLeft the number of pixels of horizontal margin that will be
     *            placed along the left edge of the layout
     * @param marginRight the number of pixels of horizontal margin that will be
     *            placed along the right edge of the layout
     * @param verticalSpacing the number of pixels between the bottom edge of
     *            one cell and the top edge of its neighbouring cell underneath
     * @param horizontalSpacing the number of pixels between the right edge of
     *            one cell and the left edge of its neighbouring cell to the
     *            right
     * @return a <code>GridLayout</code> instance according to the specified
     *         settings
     */
    public static GridLayout createGridLayout(int columns,
            boolean makeColumnsEqualWidth, int marginHeight, int marginWidth,
            int marginTop, int marginBottom, int marginLeft, int marginRight,
            int verticalSpacing, int horizontalSpacing)
    {
        GridLayout layout = new GridLayout(columns, makeColumnsEqualWidth);
        layout.marginHeight = marginHeight;
        layout.marginWidth = marginWidth;
        layout.marginTop = marginTop;
        layout.marginBottom = marginBottom;
        layout.marginLeft = marginLeft;
        layout.marginRight = marginRight;
        layout.verticalSpacing = verticalSpacing;
        layout.horizontalSpacing = horizontalSpacing;

        return (layout);
    }

    /**
     * Creates a new <code>GridData</code> object using the specified
     * settings.
     * 
     * @param horizontalAlignment specifies how controls will be positioned
     *            horizontally within a cell. Possible values are:
     *            <ul>
     *            <li>SWT.BEGINNING (or SWT.LEFT): Position the control at the
     *            left of the cell</li>
     *            <li>SWT.CENTER: Position the control in the horizontal center
     *            of the cell</li>
     *            <li>SWT.END (or SWT.RIGHT): Position the control at the right
     *            of the cell</li>
     *            <li>SWT.FILL: Resize the control to fill the cell
     *            horizontally</li>
     *            </ul>
     * @param verticalAlignment specifies how controls will be positioned
     *            vertically within a cell. Possible values are:
     *            <ul>
     *            <li>SWT.BEGINNING (or SWT.TOP): Position the control at the
     *            top of the cell</li>
     *            <li>SWT.CENTER: Position the control in the vertical center
     *            of the cell</li>
     *            <li>SWT.END (or SWT.BOTTOM): Position the control at the
     *            bottom of the cell</li>
     *            <li>SWT.FILL: Resize the control to fill the cell vertically</li>
     *            </ul>
     * @param grabExcessHorizontalSpace whether cell will be made wide enough to
     *            fit the remaining horizontal space
     * @param grabExcessVerticalSpace whether cell will be made high enough to
     *            fit the remaining vertical space
     * @param horizontalSpan the number of column cells that the control will
     *            take up
     * @param verticalSpan the number of row cells that the control will take up
     * @param heightHint the preferred height in pixels. This value is the hHint
     *            passed into Control.computeSize(int, int, boolean) to
     *            determine the preferred size of the control
     * @param widthHint the preferred width in pixels. This value is the wHint
     *            passed into Control.computeSize(int, int, boolean) to
     *            determine the preferred size of the control
     * @return a new <code>GridData</code> object according to the specified
     *         settings
     */
    public static GridData createGridData(int horizontalAlignment,
            int verticalAlignment, boolean grabExcessHorizontalSpace,
            boolean grabExcessVerticalSpace, int horizontalSpan,
            int verticalSpan, int heightHint, int widthHint)
    {
        GridData data = new GridData(horizontalAlignment, verticalAlignment, 
        		grabExcessHorizontalSpace, grabExcessVerticalSpace, 
        		horizontalSpan, verticalSpan);
        data.heightHint = heightHint;
        data.widthHint = widthHint;

        return (data);
    }

    /**
     * Creates a new <code>FillLayout</code> using the specified settings.
     * 
     * @param type specifies how controls will be positioned within the layout.
     *            Possible values are:
     *            <ul>
     *            <li>HORIZONTAL: Position the controls horizontally from left
     *            to right</li>
     *            <li>VERTICAL: Position the controls vertically from top to
     *            bottom</li>
     *            </ul>
     * @param marginHeight the number of pixels of vertical margin that will be
     *            placed along the top and bottom edges of the layout
     * @param marginWidth the number of pixels of horizontal margin that will be
     *            placed along the left and right edges of the layout
     * @param spacing the number of pixels between the edge of one cell and the
     *            edge of its neighbouring cell
     * @return a <code>FillLayout</code> instance according to the specified
     *         settings
     */
    public static FillLayout createFillLayout(int type, int marginHeight,
            int marginWidth, int spacing)
    {
        FillLayout layout = new FillLayout(type);
        layout.marginHeight = marginHeight;
        layout.marginWidth = marginWidth;
        layout.spacing = spacing;

        return layout;
    }
}
