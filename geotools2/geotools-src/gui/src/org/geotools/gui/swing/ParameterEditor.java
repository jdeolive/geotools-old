/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
 */
package org.geotools.gui.swing;

// J2SE dependencies
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.Format;

import javax.swing.JList; // For javadoc
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JFormattedTextField;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.OperationNode;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.PerspectiveTransform;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.RegistryElementDescriptor;

// Geotools dependencies
import org.geotools.pt.Angle;
import org.geotools.pt.AngleFormat;
import org.geotools.resources.XMath;
import org.geotools.resources.Utilities;
import org.geotools.resources.gui.Resources;
import org.geotools.resources.gui.ResourceKeys;


/**
 * An editor for arbitrary parameter object. The parameter value can be any {@link Object}.
 * The editor content will changes according the parameter class. For example, the content
 * will be a {@link KernelEditor} if the parameter is an instance of {@link KernelJAI}.
 * Currently supported parameter type includes:
 *
 * <ul>
 *   <li>Individual {@linkplain String string}, {@linkplain Number number}, {@linkplain Date date}
 *       or {@linkplain Angle angle}.</li>
 *   <li>Table of any primitive type (<code>int[]</code>, <code>float[]</code>, etc.).</li>
 *   <li>Matrix of any primitive type (<code>int[][]</code>, <code>float[][]</code>, etc.).</li>
 *   <li>JAI {@linkplain LookupTableJAI lookup table}, which are display in tabular format.</li>
 *   <li>{@linkplain AffineTransform Affine transform} and {@linkplain PerspectiveTransform
 *       perspective transform}, which are display like a matrix.</li>
 *   <li>Convolution {@linkplain KernelJAI kernel}, which are display in a {@link KernelEditor}.</li>
 * </ul>
 *
 * @version $Id: ParameterEditor.java,v 1.4 2003/08/07 16:03:24 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see KernelEditor
 * @see ImageProperties
 * @see OperationTreeBrowser
 *
 * @task TODO: This class do not yet support the edition of parameter value.
 *             We will allow that in a future version. This work is already
 *             partially done with the 'editable' boolean value.
 */
public class ParameterEditor extends JPanel {
    /** Key for {@link String} node.    */  private static final String STRING  = "String";
    /** Key for {@link Number} node.    */  private static final String NUMBER  = "Number";
    /** Key for {@link Angle} node.     */  private static final String ANGLE   = "Angle";
    /** Key for {@link Date} node.      */  private static final String DATE    = "Date";
    /** Key for {@link KernelJAI} node. */  private static final String KERNEL  = "Kernel";
    /** Key for any kind of table node. */  private static final String TABLE   = "Table";
    /** Key for unrecognized types.     */  private static final String DEFAULT = "Default";

    /**
     * The set of {@linkplain Component component} editors created up to date.
     * Keys are {@link String} and values are {@link Component} objects.
     */
    private final Map editors = new HashMap();

    /**
     * The properties panel for parameters. The content for this panel
     * depends on the selected item, but usually includes the following:
     * <ul>
     *   <li>A {@link JTextField} for simple parameters (numbers, string, etc.)</li>
     *   <li>A {@link JList} for enumerated parameters.</li>
     *   <li>A {@link JTable} for any kind of array parameter and {@link LookupTableJAI}.</li>
     *   <li>A {@link KernelEditor} for {@link KernelJAI} parameters.</li>
     * </ul>
     */
    private final Container cards = new JPanel(new CardLayout());

    /**
     * The label for parameter or image description.
     * Usually displayed on top of parameter editor.
     */
    private final JLabel description = new JLabel(" ", JLabel.CENTER);

    /**
     * The current value in the process of being edited. This object is usually an instance of
     * {@link Number}, {@link KernelJAI}, {@link LookupTableJAI} or some other parameter object.
     *
     * @see #setParameterValue
     */
    private Object value;

    /**
     * The editor widget currently in use.
     *
     * @see #setParameterValue
     * @see #getEditor
     */
    private Component editor;

    /**
     * The editor model currently in use. This is often the model used by the editor widget.
     */
    private Editor model;

    /**
     * <code>true</code> if this widget is editable.
     *
     * @task TODO: This
     */
    private static final boolean editable = false;

    /**
     * Construct an initially empty parameter editor.
     */
    public ParameterEditor() {
        super(new BorderLayout());
        add(description, BorderLayout.NORTH );
        add(cards,       BorderLayout.CENTER);
        setPreferredSize(new Dimension(400,250));
    }

    /**
     * Returns the parameter value currently edited, or <code>null</code> if none.
     */
    public Object getParameterValue() {
        return (model!=null) ? model.getValue() : value;
    }

    /**
     * Set the value to edit. The editor content will be updated according the value type.
     * For example if the value is an instance of {@link KernelJAI}, then the editor content
     * will be changed to a {@link KernelEditor}.
     *
     * @param value The value to edit. This object is usually an instance of {@link Number},
     *              {@link KernelJAI}, {@link LookupTableJAI} or some other parameter object.
     */
    public void setParameterValue(final Object value) {
        final Object oldValue = this.value;
        if (!Utilities.equals(value, oldValue)) {
            this.value = value;
            updateEditor();
            firePropertyChange("value", oldValue, value);
        }
    }

    /**
     * Returns the description currently shown, or <code>null</code> if none.
     */
    public String getDescription() {
        String text = description.getText();
        if (text != null) {
            text = text.trim();
            if (text.length() == 0) {
                text = null;
            }
        }
        return text;
    }

    /**
     * Set the description string to write on top of the editor.
     * This is usually a short description of the paramter being edited.
     */
    public void setDescription(String description) {
        if (description==null || description.length()==0) {
            description = " ";
        }
        this.description.setText(description);
        if (model != null) {
            model.setValueRange(null,null);
        }
    }

    /**
     * Convenience method for setting the parameter description from an operation node.
     *
     * @param operation The operation node for the current parameter.
     * @param index The parameter index, or <code>-1</code> if unknow.
     */
    final void setDescription(final OperationNode operation, final int index) {
        String description = null;
        Class  type        = null;
        Range  range       = null;
        if (operation != null) {
            final String name, mode;
            final RegistryElementDescriptor element;
            final ParameterListDescriptor param;
            name    = operation.getOperationName();
            mode    = operation.getRegistryModeName();
            element = operation.getRegistry().getDescriptor(mode, name);
            param   = element.getParameterListDescriptor(mode);
            /*
             * If a parameter is specified, gets the parameter type and its range of valid
             * values.
             */
            if (index>=0 && index<param.getNumParameters()) {
                type  = param.getParamClasses()[index];
                range = param.getParamValueRange(param.getParamNames()[index]);
            }
            /*
             * If the descriptor is an operation, gets the localized operation
             * description or the parameter description.
             */
            if (element instanceof OperationDescriptor) {
                final String key;
                final OperationDescriptor descriptor = (OperationDescriptor) element;
                final ResourceBundle resources = descriptor.getResourceBundle(getLocale());
                if (index >= 0) {
                    key = "arg"+index+"Desc";
                } else {
                    key = "Description";
                }
                try {
                    description = resources.getString(key);
                } catch (MissingResourceException ignore) {
                    // No description for this parameter. Try a global description.
                    try {
                        description = resources.getString("Description");
                    } catch (MissingResourceException exception) {
                        /*
                         * No description at all for this operation. Not a big deal;
                         * just left the description empty. Log the exception with a
                         * low level, since this warning is not really important. The
                         * level is slightly higher than in 'RegisteredOperationBrowser'
                         * since we have tried the global operation description as well.
                         */
                        Logger.getLogger("org.geotools.gui.swing").log(Level.FINE,
                                         exception.getLocalizedMessage(), exception);
                    }
                }
            }
        }
        setDescription(description);
        if (model != null) {
            model.setValueRange(type, range);
        }
    }

    /**
     * Returns the component used for editing the parameter. The component class depends on the
     * class of the value set by the last call to {@link #setParameterValue}. The editor may be
     * an instance of {@link KernelEditor}, {@link JTable}, {@link JTextField}, {@link JList} or
     * any other suitable component.
     *
     * @return The editor, or <code>null</code> if no value has been set.
     */
    public Component getEditor() {
        return editor;
    }

    /**
     * Returns the editor for the given name. If an editor is found, it will be bring
     * on top of the card layout (i.e. will become the visible editor). Otherwise, this
     * method returns <code>null</code>.
     *
     * @param  name The editor name. Should be one of {@link #NUMBER}, {@link #KERNEL} and
     *         similar constants.
     * @return The editor, or <code>null</code>.
     */
    private Component getEditor(final String name) {
        final Component panel = (Component) editors.get(name);
        ((CardLayout) cards.getLayout()).show(cards, name);
        return panel;
    }

    /**
     * Add the specified editor. No editor must exists for the specified name prior to this
     * call. The editor will be bring on top of the card layout (i.e. will become the visible
     * panel).
     *
     * @param  name The editor name. Should be one of {@link #NUMBER}, {@link #KERNEL} and
     *         similar constants.
     * @param  editor The editor.
     * @param  scroll <code>true</code> if the editor should be wrapped into a {@link JScrollPane}
     *         prior its addition to the container.
     */
    private void addEditor(final String name, Component editor, final boolean scroll) {
        if (editors.put(name, editor) != null) {
            throw new IllegalStateException(name); // Should not happen.
        }
        if (scroll) {
            editor = new JScrollPane(editor);
        }
        cards.add(editor, name);
        ((CardLayout) cards.getLayout()).show(cards, name);
    }

    /**
     * Update the editor according the current {@link #value}. If a suitable editors already
     * exists for the value class, it will be reused. Otherwise, a new editor will be created
     * on the fly.
     *
     * The {@link #editor} field will be set to the component used for editing the parameter.
     * This component may be an instance of {@link KernelEditor}, {@link JTable},
     * {@link JTextField}, {@link JList} or any other suitable component.
     *
     * The {@link #model} field will be set to the model used by the editor widget.
     */
    private void updateEditor() {
        Object value = this.value;
        /*
         * In the special case where the value is an array with only one element, extract
         * the element and use a specialized editor as if the element wasn't in an array.
         */
        while (value!=null && value.getClass().isArray() && Array.getLength(value)==1) {
            value = Array.get(value, 0);
        }
        /*
         * String  ---  Uses a JTextField editor.
         */
        if (value instanceof String) {
            Singleton editor = (Singleton) getEditor(STRING);
            if (editor == null) {
                editor = new Singleton(null);
                addEditor(STRING, editor, false);
            }
            editor.setValue(value);
            this.editor = editor.field;
            this.model  = editor;
            return;
        }
        /*
         * Number  ---  Uses a JFormattedTextField editor.
         */
        if (value instanceof Number) {
            Singleton editor = (Singleton) getEditor(NUMBER);
            if (editor == null) {
                editor = new Singleton(NumberFormat.getInstance(getLocale()));
                addEditor(NUMBER, editor, false);
            }
            editor.setValue(value);
            this.editor = editor.field;
            this.model  = editor;
            return;
        }
        /*
         * Date  ---  Uses a JFormattedTextField editor.
         */
        if (value instanceof Date) {
            Singleton editor = (Singleton) getEditor(DATE);
            if (editor == null) {
                editor = new Singleton(DateFormat.getDateTimeInstance(
                                       DateFormat.LONG, DateFormat.LONG, getLocale()));
                addEditor(DATE, editor, false);
            }
            editor.setValue(value);
            this.editor = editor.field;
            this.model  = editor;
            return;
        }
        /*
         * Angle  ---  Uses a JFormattedTextField editor.
         */
        if (value instanceof Angle) {
            Singleton editor = (Singleton) getEditor(ANGLE);
            if (editor == null) {
                editor = new Singleton(new AngleFormat("D°MM.mm'", getLocale()));
                addEditor(ANGLE, editor, false);
            }
            editor.setValue(value);
            this.editor = editor.field;
            this.model  = editor;
            return;
        }
        /*
         * AffineTransform  ---  convert to a matrix for processing by the general matrix case.
         */
        if (value instanceof AffineTransform) {
            final AffineTransform transform = (AffineTransform) value;
            value = new double[][] {
                {transform.getScaleX(), transform.getShearX(), transform.getTranslateX()},
                {transform.getShearY(), transform.getScaleY(), transform.getTranslateY()},
                {0, 0, 1}
            };
        }
        /*
         * PerspectiveTransform  ---  convert to a matrix for processing by the general matrix case.
         */
        if (value instanceof PerspectiveTransform) {
            final double[][] matrix = new double[][] {
                new double[3],
                new double[3],
                new double[3]
            };
            ((PerspectiveTransform) value).getMatrix(matrix);
            value = matrix;
        }
        /*
         * Any table or matrix  ---  use a JTable editor.
         */
        if (value != null) {
            final Class elementClass = value.getClass().getComponentType();
            if (elementClass != null) {
                final TableModel model;
                if (elementClass.isArray()) {
                    model = new Matrix((Object[]) value);
                } else {
                    model = new Table(new Object[] {value}, 0, false);
                }
                JTable editor = (JTable) getEditor(TABLE);
                if (editor == null) {
                    addEditor(TABLE, editor=new JTable(model), true);
                } else {
                    editor.setModel(model);
                }
                this.editor = editor;
                this.model  = (Editor) model;
                return;
            }
        }
        /*
         * LookupTableJAI  ---  Uses a JTable editor.
         */
        if (value instanceof LookupTableJAI) {
            final LookupTableJAI table = (LookupTableJAI) value;
            final Object[] data;
            boolean unsigned = false;
            switch (table.getDataType()) {
                case DataBuffer.TYPE_BYTE:   data=table.getByteData(); unsigned=true; break;
                case DataBuffer.TYPE_USHORT: unsigned=true; // Fall through
                case DataBuffer.TYPE_SHORT:  data=table.getShortData();  break;
                case DataBuffer.TYPE_INT:    data=table.getIntData();    break;
                case DataBuffer.TYPE_FLOAT:  data=table.getFloatData();  break;
                case DataBuffer.TYPE_DOUBLE: data=table.getDoubleData(); break;
                default: this.editor=null; this.model=null; return;
            }
            final Table model = new Table(data, table.getOffset(), unsigned);
            JTable editor = (JTable) getEditor(TABLE);
            if (editor == null) {
                addEditor(TABLE, editor=new JTable(model), true);
            } else {
                editor.setModel(model);
            }
            this.editor = editor;
            this.model  = model;
            return;
        }
        /*
         * KernelJAI  ---  Uses a KernelEditor.
         */
        if (value instanceof KernelJAI) {
            KernelEditor editor = (KernelEditor) getEditor(KERNEL);
            if (editor == null) {
                editor = new KernelEditor();
                editor.addDefaultKernels();
                addEditor(KERNEL, editor, false);
            }
            editor.setKernel((KernelJAI) value);
            this.editor = editor;
            this.model  = null; // TODO: Set the editor.
            return;
        }
        /*
         * Default case  ---  Uses a JTextArea
         */
        JTextArea editor = (JTextArea) getEditor(DEFAULT);
        if (editor == null) {
            addEditor(DEFAULT, editor=new JTextArea(), true);
            editor.setEditable(false);
        }
        editor.setText(String.valueOf(value));
        this.editor = editor;
        this.model  = null; // TODO: Set the editor.
    }

    /**
     * The interface for editor capable to returns the edited value.
     *
     * @version $Id: ParameterEditor.java,v 1.4 2003/08/07 16:03:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     *
     * @task TODO: This interface should have a 'setEditable(boolean)' method.
     */
    private static interface Editor {
        /**
         * Returns the edited value.
         */
        public abstract Object getValue();

        /**
         * Set the type and the range of valid values.
         */
        public abstract void setValueRange(final Class type, final Range range);
    }

    /**
     * An editor panel for editing a single value. The value if usually an instance of
     * {@link Number}, {@link Date}, {@link Angle} or {@link String}.
     *
     * @version $Id: ParameterEditor.java,v 1.4 2003/08/07 16:03:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     *
     * @task TODO: This editor should use <code>JSpinner</code>, but we need to gets
     *             the minimum and maximum values first since spinner needs bounds.
     */
    private static final class Singleton extends JPanel implements Editor {
        /**
         * The data type.
         */
        private final JLabel type = new JLabel();

        /**
         * The minimum allowed value.
         */
        private final JLabel minimum = new JLabel();

        /**
         * The maximum allowed value.
         */
        private final JLabel maximum = new JLabel();

        /**
         * The text field for editing the value.
         */
        private final JTextField field;

        /**
         * Construct an editor for value using the specified format.
         */
        public Singleton(final Format format) {
            super(new GridBagLayout());
            if (format != null) {
                field = new JFormattedTextField(format);
            } else {
                field = new JTextField();
            }
            field.setEditable(editable);
            final Resources resources = Resources.getResources(getLocale());
            final GridBagConstraints c = new GridBagConstraints();
            c.gridx=0; c.gridwidth=1; c.insets.left=9; c.fill=c.HORIZONTAL;
            c.gridy=0; add(new JLabel(resources.getLabel(ResourceKeys.TYPE   )), c);
            c.gridy++; add(new JLabel(resources.getLabel(ResourceKeys.MINIMUM)), c);
            c.gridy++; add(new JLabel(resources.getLabel(ResourceKeys.MAXIMUM)), c);
            c.gridy++; add(new JLabel(resources.getLabel(ResourceKeys.VALUE  )), c);
            c.gridx=1; c.weightx=1; c.insets.right=9;
            c.gridy=0; add(type,    c);
            c.gridy++; add(minimum, c);
            c.gridy++; add(maximum, c);
            c.gridy++; add(field,   c);
        }

        /**
         * Set the value to be edited.
         */
        public void setValue(final Object value) {
            if (field instanceof JFormattedTextField) {
                ((JFormattedTextField) field).setValue(value);
            } else {
                field.setText(String.valueOf(value));
            }
        }

        /**
         * Returns the edited value.
         */
        public Object getValue() {
            if (field instanceof JFormattedTextField) {
                return ((JFormattedTextField) field).getValue();
            } else {
                return field.getText();
            }
        }

        /**
         * Set the type and the range of valid values.
         */
        public void setValueRange(Class classe, final Range range) {
            String type    = null;
            String minimum = null;
            String maximum = null;
            if (classe != null) {
                while (classe.isArray()) {
                    classe = classe.getComponentType();
                }
                classe = XMath.primitiveToWrapper(classe);
                boolean isInteger = false;
                if (XMath.isReal(classe) || (isInteger=XMath.isInteger(classe))==true) {
                    type = Resources.format(isInteger ? ResourceKeys.SIGNED_INTEGER_$1
                                                      : ResourceKeys.REAL_NUMBER_$1,
                                            new Integer(XMath.getBitCount(classe)));
                } else {
                    type = Utilities.getShortName(classe);
                }
            }
            if (range != null) {
                minimum = format(range.getMinValue());
                maximum = format(range.getMaxValue());
            }
            this.type   .setText(type);
            this.minimum.setText(minimum);
            this.maximum.setText(maximum);
        }

        /**
         * Format the given value.
         */
        private String format(final Comparable value) {
            if (value == null) {
                return null;
            }
            if (field instanceof JFormattedTextField) try {
                return ((JFormattedTextField) field).getFormatter().valueToString(value);
            } catch (ParseException exception) {
                // Value can't be formatted. Fall back on the 'toString()' method, which
                // is okay since this string is used for informative purpose only.
            }
            return value.toString();
        }
    }

    /**
     * Table model for table parameters (including {@link LookupTableJAI}.
     * Instance of this class are created by {@link #updateEditor} when first needed.
     *
     * @version $Id: ParameterEditor.java,v 1.4 2003/08/07 16:03:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class Table extends AbstractTableModel implements Editor {
        /**
         * The table (usually an instance of <code>double[][]</code>).
         */
        private final Object[] table;

        /**
         * The offset parameter (a {@link LookupTableJAI} property).
         */
        private final int offset;

        /**
         * <code>true</code> if the table values are unsigned.
         */
        private final boolean unsigned;

        /**
         * Construct a model for the given table.
         *
         * @param table    The table (usually an instance of <code>double[][]</code>).
         * @param offset   The offset parameter (a {@link LookupTableJAI} property).
         * @param unsigned <code>true</code> if the table values are unsigned.
         */
        public Table(final Object[] table, final int offset, final boolean unsigned) {
            this.table    = table;
            this.offset   = offset;
            this.unsigned = unsigned;
        }

        /**
         * Returns the number of rows in the table.
         */
        public int getRowCount() {
            int count = 0;
            for (int i=0; i<table.length; i++) {
                final int length = Array.getLength(table[i]);
                if (length > count) {
                    count = length;
                }
            }
            return count;
        }

        /**
         * Returns the number of columns in the model.
         */
        public int getColumnCount() {
            return Array.getLength(table) + 1;
        }

        /**
         * Returns the name of the column at the specified index.
         */
        public String getColumnName(final int index) {
            switch (index) {
                case 0:  return Resources.format(ResourceKeys.INDEX);
                default: return Resources.format(ResourceKeys.VALUE);
            }
        }

        /**
         * Returns the most specific superclass for all the cell values.
         */
        public Class getColumnClass(final int index) {
            if (index==0 || unsigned) {
                return Integer.class;
            }
            return XMath.primitiveToWrapper(table[index-1].getClass().getComponentType());
        }

        /**
         * Tells if the specified cell is editable.
         */
        public boolean isCellEditable(final int row, final int column) {
            return editable && column!=0;
        }

        /**
         * Returns the value at the specified index.
         */
        public Object getValueAt(final int row, final int column) {
            if (column == 0) {
                return new Integer(row + offset);
            }
            final Object array = table[column-1];
            if (unsigned) {
                return new Integer(Array.getInt(array, row) & 0x7FFFFFFF);
            }
            return Array.get(array, row);
        }

        /**
         * Set the value at the given index.
         */
        public void setValueAt(final Object value, final int row, final int column) {
            Array.set(table[column-1], row, value);
        }

        /**
         * Returns the edited value.
         */
        public Object getValue() {
            return table;
        }

        /**
         * Set the type and the range of valid values.
         * The default implementation does nothing.
         */
        public void setValueRange(final Class type, final Range range) {
        }
    }

    /**
     * Table model for matrix parameters. Instance of this class
     * are created by {@link #updateEditor} when first needed.
     *
     * @version $Id: ParameterEditor.java,v 1.4 2003/08/07 16:03:24 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class Matrix extends AbstractTableModel implements Editor {
        /**
         * The matrix (usually an instance of <code>double[][]</code>).
         */
        private final Object[] matrix;

        /**
         * Construct a model for the given matrix.
         *
         * @param matrix The matrix (usually an instance of <code>double[][]</code>).
         */
        public Matrix(final Object[] matrix) {
            this.matrix = matrix;
        }

        /**
         * Returns the number of rows in the matrix.
         */
        public int getRowCount() {
            return matrix.length;
        }

        /**
         * Returns the number of columns in the model. This is the length of the longest
         * row in the matrix.
         */
        public int getColumnCount() {
            int count = 0;
            for (int i=0; i<matrix.length; i++) {
                final int length = Array.getLength(matrix[i]);
                if (length > count) {
                    count = length;
                }
            }
            return count;
        }

        /**
         * Returns the name of the column at the specified index.
         */
        public String getColumnName(final int index) {
            return Integer.toString(index);
        }

        /**
         * Returns the most specific superclass for all the cell values.
         */
        public Class getColumnClass(final int index) {
            return XMath.primitiveToWrapper(matrix.getClass().getComponentType().getComponentType());
        }

        /**
         * Tells if the specified cell is editable.
         */
        public boolean isCellEditable(final int row, final int column) {
            return editable;
        }

        /**
         * Returns the value at the specified index.
         */
        public Object getValueAt(final int row, final int column) {
            final Object array = matrix[row];
            return (column < Array.getLength(array)) ? Array.get(array, column) : null;
        }

        /**
         * Set the value at the given index.
         */
        public void setValueAt(final Object value, final int row, final int column) {
            Array.set(matrix[row], column, value);
        }

        /**
         * Returns the edited value.
         */
        public Object getValue() {
            return matrix;
        }

        /**
         * Set the type and the range of valid values.
         * The default implementation does nothing.
         */
        public void setValueRange(final Class type, final Range range) {
        }
    }
}
