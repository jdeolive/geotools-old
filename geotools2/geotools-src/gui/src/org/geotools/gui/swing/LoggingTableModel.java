/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gui.swing;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

// Table model
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.EventListenerList;
import java.awt.EventQueue;

// Collections
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

// Formatting
import java.util.Date;
import java.text.DateFormat;

// Resources
import org.geotools.resources.XArray;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * A logging {@link Handler} storing {@link LogRecords} as a {@link TableModel}.
 * This model is used by {@link LoggingPanel} for displaying logging messages in
 * a {@link javax.swing.JTable}.
 *
 * @version $Id: LoggingTableModel.java,v 1.2 2002/09/01 22:47:03 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LoggingTableModel extends Handler implements TableModel {
    /**
     * Resource keys for column names.
     */
    private static final int[] COLUMN_NAMES = new int[] {
        ResourceKeys.LOGGER,
        ResourceKeys.CLASS,
        ResourceKeys.METHOD,
        ResourceKeys.TIME_OF_DAY,
        ResourceKeys.LEVEL,
        ResourceKeys.MESSAGE
    };

    /**
     * The last {@link LogRecord}s stored. This array will grows as needed up to
     * {@link #capacity}. Once the maximal capacity is reached, early records
     * are discarted.
     */
    private LogRecord[] records = new LogRecord[16];

    /**
     * The maximum amount of records that can be stored in this logging panel.
     * If more than {@link #capacity} messages are logged, early messages will
     * be discarted.
     */
    private int capacity = 500;

    /**
     * The total number of logging messages published by this panel. This number may be
     * greater than the amount of {@link LogRecord} actually memorized, since early records
     * may have been discarted. The slot in <code>records</code> where to write the next
     * message can be computed by <code>recordCount % capacity</code>.
     */
    private int recordCount;

    /**
     * String representations of latest required records. Keys are {@link LogRecord} objects
     * and values are <code>String[]</code>. This is a cache for faster rendering.
     */
    private final Map cache = new LinkedHashMap() {
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() >= Math.min(capacity, 80);
        }
    };

    /**
     * The list of registered listeners.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * The format to use for formatting time.
     */
    private final DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

    /**
     * Construct the handler.
     */
    public LoggingTableModel() {
        setLevel(Level.CONFIG);
        setFormatter(new SimpleFormatter());
    }

    /**
     * Returns the capacity. This is the maximum number of {@link LogRecord}s this handler
     * can memorize. If more messages are logged, then the oldiest messages will be discarted.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Set the capacity. This is the maximum number of {@link LogRecord}s this handler can
     * memorize. If more messages are logged, then the oldiest messages will be discarted.
     */
    public synchronized void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    /**
     * Publish a {@link LogRecord}. If the maximal capacity has been reached,
     * the oldiest record will be discarted.
     */
    public synchronized void publish(final LogRecord record) {
	if (!isLoggable(record)) {
	    return;
	}
        final int nextSlot = recordCount % capacity;
        if (nextSlot >= records.length) {
            records = (LogRecord[]) XArray.resize(records, Math.min(records.length*2, capacity));
        }
        records[nextSlot] = record;
        final TableModelEvent event;
        if (++recordCount <= capacity) {
            event = new TableModelEvent(this, nextSlot, nextSlot,
                                        TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        } else {
            event = new TableModelEvent(this, 0, capacity-1,
                                        TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        }
        //
        // Notify all listeners that a record has been added.
        //
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                fireTableChanged(event);
            }
        });
    }

    /**
     * Returns the log record for the specified row.
     *
     * @param row The row in the table. This is the visible row,
     *            not the record number from the first record.
     */
    public synchronized LogRecord getLogRecord(int row) {
        assert row < getRowCount();
        if (recordCount > capacity) {
            row += (recordCount % capacity);
            row %= capacity;
        }
        return records[row];
    }

    /**
     * Returns the number of columns in the model.
     */
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    /**
     * Returns the number of rows in the model.
     */
    public synchronized int getRowCount() {
        return Math.min(recordCount, capacity);
    }

    /**
     * Returns the most specific superclass for all the cell values in the column.
     */
    public Class getColumnClass(final int columnIndex) {
        return String.class;
    }

    /**
     * Returns the name of the column at <code>columnIndex</code>.
     */
    public String getColumnName(final int columnIndex) {
        return Resources.format(COLUMN_NAMES[columnIndex]);
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
     */
    public synchronized Object getValueAt(final int rowIndex, final int columnIndex) {
        final LogRecord record = getLogRecord(rowIndex);
        String[] row = (String[]) cache.get(record);
        if (row == null) {
            row = new String[getColumnCount()];
            row[0] = record.getLoggerName();
            row[1] = record.getSourceClassName();
            row[2] = record.getSourceMethodName();
            row[3] = dateFormat.format(new Date(record.getMillis()));
            row[4] = record.getLevel().getLocalizedName();
            row[5] = getFormatter().formatMessage(record);
            cache.put(record, row);
        }
        return row[columnIndex];
    }

    /**
     * Do nothing since cells are not editable.
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    /**
     * Returns <code>false</code> since cells are not editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * Adds a listener that is notified each time a change to the data model occurs.
     */
    public void addTableModelListener(final TableModelListener listener) {
        listenerList.add(TableModelListener.class, listener);
    }

    /**
     * Removes a listener from the list that is notified each time a change occurs.
     */
    public void removeTableModelListener(final TableModelListener listener) {
        listenerList.remove(TableModelListener.class, listener);
    }

    /**
     * Forwards the given notification event to all {@link TableModelListeners}.
     */
    private void fireTableChanged(final TableModelEvent event) {
        final Object[] listeners = listenerList.getListenerList();
        for (int i=listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TableModelListener.class) {
                ((TableModelListener)listeners[i+1]).tableChanged(event);
            }
        }
    }

    /**
     * Flush any buffered output.
     */
    public void flush() {
    }

    /**
     * Close the <code>Handler</code> and free all associated resources.
     */
    public void close() {
    }
}
