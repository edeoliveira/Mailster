/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package org.mailster.gui.glazedlists.swt;

// Java list utility classes
import java.util.ArrayList;
import java.util.List;

// JFace packages
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

// SWT packages
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

// Core GlazedList packages
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swt.GlazedListsSWT;

/**
 * A TableViewerManager is a decorator that binds a JFace TableViewer to an
 * EventList
 *
 * @see org.eclipse.jface.viewers.TableViewer
 *
 * This class is not thread-safe.  It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a proof-of-concept and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:compulinkltd@...">Bruce Alspaugh</a>
 * @author <a href="mailto:tuler@...">Danilo Tuler</a>
 * @author <a href="mailto:jplemieux@...">James Lemieux</a>
 * @author <a href="mailto:jesse@...">Jesse Wilson</a>
 */
@SuppressWarnings("unchecked")
public class TableViewerManager {
        /** the heavyweight TableViewer **/
        private TableViewer tableViewer;
       
        /** the list being displayed in the TableViewer **/
        private EventList sourceList;
       
        /** Specifies how to render table headers and sort */
        private TableFormat tableFormat;
       
        /** ContentProvider installed in the TableViewer */
        private IStructuredContentProvider contentProvider = null;
       
        /** LabelProvider installed in the TableViewer */
        private ITableLabelProvider labelProvider = null;
       
        /** Is the selection being changed? */
        private boolean selectionInProgress = false;
       
        /** Maintains the TableViewer selection as an EventList */
        private ListSelection listSelection = null;
       
        /** Watches for changes in the selection and updates the ListSelection */
        private ISelectionChangedListener selectionChangedListener = null;
       
        /** Watches to see when the underlying SWT Table is disposed */
        private DisposeListener disposeListener = null;
               
        /**
         * Creates a new TableViewerManager that binds an EventList to a JFace
         * TableViewer.  Use the provided EventTableContentProvider and
         * EventTableLabelProvider
         */
        public TableViewerManager(TableViewer aTableViewer, EventList aSourceList,
                        EventTableContentProvider aContentProvider,
                        EventTableLabelProvider aLabelProvider) {
                tableViewer = aTableViewer;
                sourceList = aSourceList;
                tableFormat = aLabelProvider.getTableFormat();
               
                createColumns(tableViewer.getTable());
                tableViewer.setContentProvider(aContentProvider);
                tableViewer.setLabelProvider(aLabelProvider);
               
                // finish the rest of the initialization
                init();
        }
               
        /**
         * Creates a new TableViewerManager that binds an EventList to a JFace
         * TableViewer.  The table is formatted according to the provided TableFormat.
         *
         * @param aTableViewer the TableViewer to display
         * @param aSourceList the EventList to display
         * @param aTableFormat the TableFormat to use
         */
        public TableViewerManager(TableViewer aTableViewer, EventList aSourceList,
                        TableFormat aTableFormat) {
                tableViewer = aTableViewer;
                sourceList = aSourceList;
                tableFormat = aTableFormat;
               
                createColumns(tableViewer.getTable());
                contentProvider = new EventTableContentProvider();
                tableViewer.setContentProvider(contentProvider);
               
                labelProvider = new EventTableLabelProvider(tableFormat);
                tableViewer.setLabelProvider(labelProvider);
               
                tableViewer.setInput(sourceList);
               
                // finish the rest of the initialization
                init();
        }
       
        /**
         * Creates a new TableViewerManager that binds an EventList to a JFace
         * TableViewer.  The table is formatted with an automatically generated
         * TableFormat that does not support in-cell editing.
         *
         * @param aTableViewer the TableViewer to display
         * @param aSourceList the EventList to display
         * @param aPropertyNames the names of the JavaBean properties
         * @param aColumnLabels the column names displayed to the user
         */
        public TableViewerManager(TableViewer aTableViewer, EventList aSourceList,
                        String[] aPropertyNames, String[] aColumnLabels) {
                this(aTableViewer, aSourceList,
                        GlazedLists.tableFormat(aPropertyNames, aColumnLabels));
        }
       
        /**
         * Creates a new TableViewerManager that binds an EventList to a JFace
         * TableViewer.  The table is formatted with an automatically generated
         * TableFormat that supports in-cell editing if the developer installs
         * appropriate CellEditors in the TableViewer by calling
         * TableViewrManager.getTableViewer().setCellEditors(CellEditor[] editors)
         *
         * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=87733
         * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=151295
         *
         * @param aTableViewer the TableViewer to display
         * @param aSourceList the EventList to display
         * @param aPropertyNames the names of the JavaBean properties
         * @param aColumnLabels the column names displayed to the user
         * @param aEditable indicates whether the column is editable
         */
        public TableViewerManager(TableViewer aTableViewer, EventList aSourceList,
                        String[] aPropertyNames, String[] aColumnLabels, boolean[] aEditable) {
                this(aTableViewer, aSourceList,
                        GlazedLists.tableFormat(aPropertyNames, aColumnLabels, aEditable));
        }
       
        /**
         * Obtain the TableViewer that is being managed.
         * @see org.eclipse.jface.viewers.TableViewer
         *
         * @return TableViewer
         */
        public TableViewer getTableViewer() {
                return tableViewer;
        }
       
        /**
         * Obtain the TableFormat that specifies the column names and labels
         * @see ca.odell.glazedlists.gui.TableFormat
         *
         * @return TableFormat
         */
        public TableFormat getTableFormat() {
                return tableFormat;
        }
       
        /**
         * Return the ListSelection
         *
         * @see ca.odell.glazedlists.ListSelection
         * @return ListSelection
         */
        public ListSelection getListSelection() {
                return listSelection;
        }
                       
   /**
     * The EventTableContentProvider listens for changes in the
     * EventList and updates the Viewer accordingly.  Relies on the swtSource
     * to dispatch the change events to the SWT thread.
     */
        public static class EventTableContentProvider implements
                IStructuredContentProvider, ListEventListener {
       
                private EventList swtSource = null;
                private TableViewer tableViewer = null;
               
                // Return an array of beans (rows) stored in the EventList
                public Object[] getElements(Object aInputElements) {
                        EventList sourceList = (EventList)aInputElements;
                        sourceList.getReadWriteLock().readLock().lock();
                        try {
                                return sourceList.toArray();
                        } finally {
                                sourceList.getReadWriteLock().readLock().unlock();
                        }
                }
               
                /**
                 * When TableViewer.setInput(Object model) is called to set the EventList
                 * the TableViewer watches, this method is called.  The input must
                 * always be an EventList.  It stops listening for changes to the old
                 * EventList (if any), and starts listening for
                 * changes to the new one (if any).
                 *
                 * @param Viewer TableViewer that had setInput called on it
                 * @param aOldInput the old EventList
                 * @param aNewInput the new EventList
                 */
				public void inputChanged(Viewer aViewer, Object aOldInput, Object aNewInput) {
                        // update viewer
                        tableViewer = (TableViewer)aViewer;
                       
                        // if not same input
                        if (aOldInput != aNewInput) {
                               
                                // remove old listener
                                if (aOldInput != null) {
                                        swtSource.removeListEventListener(this);
                                }
                               
                                // add new listener
                                if (aNewInput != null) {
                                        EventList eventList = (EventList)aNewInput;
                                        swtSource = GlazedListsSWT.swtThreadProxyList(eventList,
                                                tableViewer.getControl().getDisplay());
                                        swtSource.addListEventListener(this);
                                }
                        }
                }
               
        /**
         * Refresh the TableViewer when the EventList changes
         */
        public void listChanged(ListEvent aListEvent) {
        tableViewer.getControl().setRedraw(false);
        try {
                while (aListEvent.next()) {
                    int index = aListEvent.getIndex();
                    switch (aListEvent.getType()) {
                        case ListEvent.INSERT:
                            Object inserted = swtSource.get(index);
                            tableViewer.insert(inserted, index);
                            break;
                        case ListEvent.DELETE:
                            Object deleted = tableViewer.getElementAt(index);
                            tableViewer.remove(deleted);
                            break;
                        case ListEvent.UPDATE:
                            Object updated = swtSource.get(index);
                            tableViewer.update(updated, null);
                            break;
                    }
                }
        } finally {
        tableViewer.getTable().setRedraw(true);
        }
        }
       
                public void dispose() { }
        }

        /**
         * The EventTableLabelProvider specifies the contents, formatting, and
         * decorations in the cells of the TableViewer
         */
        public static class EventTableLabelProvider implements ITableLabelProvider {
                private List<ILabelProviderListener> listeners;
                private TableFormat tableFormat;
               
                public EventTableLabelProvider(TableFormat aTableFormat) {
                        listeners = new ArrayList<ILabelProviderListener>();
                        tableFormat = aTableFormat;
                }
               
                public TableFormat getTableFormat() {
                        return tableFormat;
                }
               
				public String getColumnText(Object aElement, int aColumnIndex) {
                        Object cellValue = tableFormat.getColumnValue(aElement, aColumnIndex);
                        if (cellValue != null)
                                return cellValue.toString();
                       
                        return "";
                }
               
                public Image getColumnImage(Object aElement, int aColumnIndex) {
                        return null;
                }
               
                public boolean isLabelProperty(Object aElement, String aProperty) {
                        return true;
                }
               
                public void addListener(ILabelProviderListener aListener) {
                        if (!listeners.contains(aListener)) {
                                listeners.add(aListener);
                        }
                }
               
                public void removeListener(ILabelProviderListener aListener) {
                        listeners.remove(aListener);
                }
                               
                public void dispose() { }
        }
       
        /**
         * Handle common initialization for private resources we do not expose
         */
		private void init() {
                /**
                 * Listener for programmatic changes to the ListSelection that syncs the
                 * TableViewer's IStructuredSelection accordingly.
                 */
                final class ListSelectionListener implements ListSelection.Listener {
                        public void selectionChanged(int changeStart, int changeEnd) {
                                if (!selectionInProgress && changeStart != -1) {
                                        selectionInProgress = true;
                                        tableViewer.setSelection(
                                                new StructuredSelection(listSelection.getSelected()));
                                        selectionInProgress = false;
                                }
                        }
                }
               
                /**
                 * Listener for user selection changes in the TableViewer that syncs
                 * the ListSelection accordingly.
                 */
                final class SelectionChangedListener implements ISelectionChangedListener {
                        public void selectionChanged(SelectionChangedEvent aEvent) {
                                if (!selectionInProgress) {
                                        IStructuredSelection sSel =
                                                (IStructuredSelection) aEvent.getSelection();
                                        selectionInProgress = true;
                                        listSelection.deselectAll();
                                        listSelection.select(sSel.toList());
                                        selectionInProgress = false;
                                }
                        }
                }

               
                /**
                 * The CellModifier supports in-cell editing if the TableFormat is
                 * writable, and the developer installs CellEditors by calling:
                 * TableViewerManager.getTableViewer().setCellEditors(CellEditor[] editors)
                 */
                @SuppressWarnings("unchecked")
                final class CellModifier implements ICellModifier {
                        private WritableTableFormat format = null;
                       
                        public CellModifier(WritableTableFormat aFormat) {
                                format = aFormat;
                        }

						public boolean canModify(Object aElement, String aProperty) {
                                int index = columnIndex(aProperty);
                                return index != -1 ? format.isEditable(aElement, index) : false;
                        }

						public Object getValue(Object aElement, String aProperty) {
                                return format.getColumnValue(aElement, columnIndex(aProperty));
                        }

						public void modify(Object aElement, String aProperty, Object aValue) {
                                if (aElement instanceof Item)
                                 aElement = ((Item) aElement).getData();
                               
                            format.setColumnValue(aElement, aValue, columnIndex(aProperty));
                                tableViewer.update(aElement, null);
                        }
                       
                        private int columnIndex(String aProperty) {
                                int column = -1;
                                for (int index = 0; index < format.getColumnCount(); index++)
                                        if (format.getColumnName(index).equals(aProperty))
                                                column = index;
                               
                                return column;
                        }
                }
               
                /**
                 * The DisposeTableListener is used to call our dispose() method
                 * when the underlying SWT Table is disposed()
                 */
                final class DisposeTableListener implements DisposeListener {
                        public void widgetDisposed(DisposeEvent aDisposeEvent) {
                                dispose();
                        }
                }
               
                // create list selection that maintains the selection as EventLists
                listSelection = new ListSelection(sourceList);
                listSelection.addSelectionListener(new ListSelectionListener());
               
                // create a selection changed listener
                selectionChangedListener = new SelectionChangedListener();
                tableViewer.addSelectionChangedListener(selectionChangedListener);
               
                // is the tableFormat writable so we can do in-cell editing?
                if (tableFormat instanceof WritableTableFormat) {
                        // enable the column properties
                        String[] properties = new String[tableFormat.getColumnCount()];
                        for (int index = 0; index < properties.length; index++)
                                properties[index] = tableFormat.getColumnName(index);
                        tableViewer.setColumnProperties(properties);
                       
                        // enable the CellModifier
                        tableViewer.setCellModifier(
                                new CellModifier((WritableTableFormat)tableFormat));
                }
               
                // call dispose() when the underlying SWT Table is disposed()
                disposeListener = new DisposeTableListener();
                tableViewer.getTable().addDisposeListener(disposeListener);
               
                // adjust column widths according to the table content
                for (TableColumn column : tableViewer.getTable().getColumns())
                        column.pack();
        }
       
        /**
         * Turn on the table header and install columns into the table based
         * on the TableFormat
         *
         * @param aTable the table to work on
         */
        private void createColumns(Table aTable) {
                aTable.setHeaderVisible(true);
                aTable.setLinesVisible(true);
               
        for (int c = 0; c < tableFormat.getColumnCount(); c++) {
            TableColumn column = new TableColumn(aTable, SWT.LEFT, c);
            column.setText(tableFormat.getColumnName(c));
            column.setMoveable(true);
        }
        }
       
        /**
         * Dispose of the TableViewerManagers resources.  It is an error
         * to call any methods on the TableViewerManager after it has been
         * disposed().
         */
        public void dispose() {
                if (tableViewer != null && disposeListener != null)
                        tableViewer.getTable().removeDisposeListener(disposeListener);
               
                if (tableViewer != null && selectionChangedListener != null)
                        tableViewer.removeSelectionChangedListener(selectionChangedListener);
               
                if (listSelection != null)
                        listSelection.dispose();
               
                if (labelProvider != null)
                        labelProvider.dispose();
               
                if (contentProvider != null)
                        contentProvider.dispose();
        }
} 