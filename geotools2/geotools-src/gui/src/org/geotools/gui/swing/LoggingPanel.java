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

// Swing dependencies
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.EventListenerList;

// AWT
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.BorderLayout;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

// Collections
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

// Resources
import org.geotools.resources.XArray;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * A panel displaying logging messages.
 *
 * This panel is initially set to listen to messages of level {@link Level#CONFIG} or higher.
 * This level can be changed with <code>{@link #getHandler}.setLevel(level)</code>.
 *
 * @version $Id: LoggingPanel.java,v 1.1 2002/08/30 18:41:11 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class LoggingPanel extends JPanel {
    /**
     * The model for this component.
     */
    private final Model model = new Model();

    /**
     * The table for displaying logging messages.
     */
    private final JTable table = new JTable(model);

    /**
     * The levels for colors enumerated in <code>levelColors</code>.
     * This array must be in increasing order.
     */
    private int[] levelValues = new int[0];

    /**
     * The color to use for displaying logging messages. Color at index <var>i</var>
     * will be used for level <code>levelValues[i/2]</code> or greater.
     */
    private final List levelColors = new ArrayList();

    /**
     * Construct a logging panel. This panel is not registered to any logger.
     * In order to register this panel, the following code must be invoked:
     *
     * <blockquote><pre>
     * aLogger.{@link Logger#addHandler addHandler}({@link #getHandler});
     * </pre></blockquote>
     */
    public LoggingPanel() {
        super(new BorderLayout());
        table.setShowGrid(false);
        table.setCellSelectionEnabled(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        final TableColumnModel columns = table.getColumnModel();
        columns.getColumn(0).setPreferredWidth(80);
        columns.getColumn(1).setPreferredWidth(200);
        add(new JScrollPane(table), BorderLayout.CENTER);

        setLevelColor(Level.INFO,    new Color(0, 128, 0), null);
        setLevelColor(Level.WARNING, Color.RED,           null);
        setLevelColor(Level.SEVERE,  Color.WHITE,    Color.RED);
    }

    /**
     * Construct a logging panel and register it to the specified logger.
     *
     * @param logger The logger to listen to, or <code>null</code> for the root logger.
     */
    public LoggingPanel(Logger logger) {
        this();
        if (logger == null) {
            logger = Logger.getLogger("");
        }
        logger.addHandler(getHandler());
    }

    /**
     * Construct a logging panel and register it to the specified logger.
     *
     * @param logger The logger name to listen to, or <code>null</code> for the root logger.
     */
    public LoggingPanel(final String logger) {
        this(Logger.getLogger(logger!=null ? logger : ""));
    }

    /**
     * Returns the logging handler.
     */
    public Handler getHandler() {
        return model;
    }

    /**
     * Set the color for message of the specified level. Message of level <code>level</code>
     * or greater will be painted using the specified foreground and background colors.
     *
     * @param level       The minimal level to set color for.
     * @param foreground  The foreground color, or <code>null</code> for the default color.
     * @param background  The background color, or <code>null</code> for the default color.
     */
    public void setLevelColor(final Level level, final Color foreground, final Color background) {
        final int value = level.intValue();
        int i = Arrays.binarySearch(levelValues, value);
        if (i >= 0) {
            i *= 2;
            levelColors.set(i+0, foreground);
            levelColors.set(i+1, background);
        } else {
            i = ~i;
            levelValues = XArray.insert(levelValues, i, 1);
            levelValues[i] = value;
            i *= 2;
            levelColors.add(i+0, foreground);
            levelColors.add(i+1, background);
        }
        assert levelValues.length*2 == levelColors.size();
    }

    /**
     * Returns the foreground color for the specified level.
     *
     * @param  level The level to get foreground.
     * @return The foreground color for the specified level.
     */
    public Color getForeground(final Level level) {
        final Color color = getForeground(level.intValue());
        return (color!=null) ? color : getForeground();
    }

    /**
     * Returns the foreground color for the specified level.
     *
     * @param  level The level to get foreground.
     * @return The foreground color for the specified level.
     */
    public Color getBackground(final Level level) {
        final Color color = getBackground(level.intValue());
        return (color!=null) ? color : getBackground();
    }

    /**
     * Returns the foreground color for the specified level.
     *
     * @param  level The level to get foreground.
     * @return The foreground color for the specified level.
     */
    final Color getForeground(final int level) {
        int i = Arrays.binarySearch(levelValues, level);
        if (i < 0) {
            i = ~i;
            if (i >= levelValues.length) {
                return getForeground();
            }
        }
        return (Color) levelColors.get(i*2);
    }

    /**
     * Returns the background color for the specified level.
     *
     * @param  level The level to get background.
     * @return The background color for the specified level.
     */
    final Color getBackground(final int level) {
        int i = Arrays.binarySearch(levelValues, level);
        if (i < 0) {
            i = ~i;
            if (i >= levelValues.length) {
                return getBackground();
            }
        }
        return (Color) levelColors.get(i*2 + 1);
    }

    /**
     * The model to use for storing logging data.
     */
    private final class Model extends Handler implements TableModel {
        /**
         * The list of cell's contents in a row major fashion.
         */
        private final List records = new ArrayList();

        /**
         * Record's levels for each row. Used for selecting a rendering color.
         */
        private int[] levels = new int[12];

        /**
         * Width of the "message" column. This width will never dimunish.
         * It will only go greater if longer message are logged.
         */
        private int width = 20;

        /**
         * Construct the handler.
         */
        public Model() {
            setLevel(Level.CONFIG);
            setFormatter(new SimpleFormatter());
        }

        /**
         * Publish a {@link LogRecord}.
         */
        public synchronized void publish(final LogRecord record) {
            final int        row = getRowCount();
            final Level    level = record.getLevel();
            final String message = getFormatter().formatMessage(record);
            final int     length = message.length();

            // Store the level
            if (row >= levels.length) {
                levels = XArray.resize(levels, row + Math.min(row, 512));
            }
            levels[row] = level.intValue();

            // Store the logging message
            records.add(level.getLocalizedName());
            records.add(message);
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    fireTableChanged(new TableModelEvent(Model.this, row, row,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
                    if (length > width) {
                        width = length;
                        final int column = table.convertColumnIndexToView(getColumnCount()-1);
                        if (column >= 0) {
                            table.getColumnModel().getColumn(column).setPreferredWidth(length*6);
                        }
                    }
                }
            });
        }

        /**
         * Returns the level for the specified row.
         */
        final synchronized int getLevel(final int row) {
            assert row < getRowCount();
            return levels[row];
        }

        /**
         * Returns the number of columns in the model.
         */
        public int getColumnCount() {
            return 2;
        }
        
        /**
         * Returns the number of rows in the model.
         */
        public synchronized int getRowCount() {
            final int size = records.size();
            final int columnCount = getColumnCount();
            assert (size % columnCount) == 0;
            return  size / columnCount;
        }

        /**
         * Returns the most specific superclass for all the cell values in the column.
         */
        public Class getColumnClass(int columnIndex) {
            return String.class;
        }
        
        /**
         * Returns the name of the column at <code>columnIndex</code>.
         */
        public String getColumnName(int columnIndex) {
            final int key;
            switch (columnIndex) {
                case 0:  key=ResourceKeys.LEVEL;   break;
                default: key=ResourceKeys.MESSAGE; break;
            }
            return Resources.getResources(getLocale()).getString(key);
        }

        /**
         * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
         */
        public synchronized Object getValueAt(int rowIndex, int columnIndex) {
            return records.get(rowIndex*getColumnCount() + columnIndex);
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

    /**
     * Display cell contents. This class is used for changing
     * the cell's color according the log record level.
     */
    private final class CellRenderer extends DefaultTableCellRenderer {
        /**
         * Default color for the foreground.
         */
        private Color foreground;

        /**
         * Default color for the background.
         */
        private Color background;

        /**
         * Construct a new cell renderer.
         */
        public CellRenderer() {
            foreground = super.getForeground();
            background = super.getBackground();
        }

        /**
         * Set the foreground color.
         */
        public void setForeground(final Color foreground) {
            super.setForeground(this.foreground=foreground);
        }

        /**
         * Set the background colior
         */
        public void setBackground(final Color background) {
            super.setBackground(this.background=background);
        }

        /**
         * Returns the component to use for painting the cell.
         */
        public Component getTableCellRendererComponent(final JTable  table,
                                                       final Object  value,
                                                       final boolean isSelected,
                                                       final boolean hasFocus,
                                                       final int row, final int column)
        {
            Color foreground = this.foreground;
            Color background = this.background;
            if (column == 0) {
                foreground = Color.GRAY;
            } else if (row >= 0) {
                final TableModel candidate = table.getModel();
                if (candidate instanceof Model) {
                    final Model model = (Model) candidate;
                    final int   level = model.getLevel(row);
                    Color color;
                    color=LoggingPanel.this.getForeground(level); if (color!=null) foreground=color;
                    color=LoggingPanel.this.getBackground(level); if (color!=null) background=color;
                }
            }
            super.setBackground(background);
            super.setForeground(foreground);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    /**
     * Temporary method for testing this widget (will be removed soon).
     */
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new LoggingPanel("org.geotools"));
        frame.pack();
        frame.show();
        for (int i=0; i<50; i++) {
            Thread.currentThread().sleep(500);
            Logger.getLogger("org.geotools").info("Test #"+i+org.geotools.resources.Utilities.spaces(i)+"bof");
            if (i==15) {
               Logger.getLogger("org.geotools").warning("Test");
            }
            if (i==20) {
               Logger.getLogger("org.geotools").severe("Test");
            }
        }
    }
}
