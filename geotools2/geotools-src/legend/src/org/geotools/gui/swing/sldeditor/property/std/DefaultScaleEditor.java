/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * Created on 20-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.property.std;

import org.geotools.gui.swing.sldeditor.property.ScaleEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JList;


/**
 * A JComboBox designed to edit scales allowing only proper values to be inserted
 *
 * @author aaime
 */
public class DefaultScaleEditor extends ScaleEditor {
    private static double[] defaultScales = new double[] {
            1000, 5000, 10000, 20000, 30000, 40000, 50000, 100000, 200000, 300000, 400000, 500000,
            1000000, 2000000, 3000000, 4000000, 5000000, 10000000
        };
    private static NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private JComboBox cmbScale;

    public DefaultScaleEditor() {
        this(defaultScales);
    }

    public DefaultScaleEditor(double[] scalesList) {
        cmbScale = new JComboBox();
        cmbScale.setEditor(new DoubleComboBoxEditor());
        cmbScale.setRenderer(new NumberListCellRenderer());
        cmbScale.setEditable(true);

        Double[] scales = new Double[defaultScales.length];

        for (int i = 0; i < scales.length; i++) {
            scales[i] = new Double(defaultScales[i]);
        }

        cmbScale.setModel(new DefaultComboBoxModel(scales));

        this.setLayout(new BorderLayout());
        this.add(cmbScale);
    }

    public void setScaleDenominator(double scale) {
        cmbScale.setSelectedItem(new Double(scale));
    }

    public double getScaleDenominator() {
        return ((Number) cmbScale.getSelectedItem()).doubleValue();
    }

    /**
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        cmbScale.setEnabled(enabled);
    }

    /**
     * @see java.awt.Component#isEnabled()
     */
    public boolean isEnabled() {
        return cmbScale.isEnabled();
    }

    /**
     * An extended ListCellRenderer that properly formats numbers according to the current locale
     *
     * @author aaime
     */
    public static class NumberListCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Number) {
                setText(numberFormat.format(((Number) value).doubleValue()));
            }

            return this;
        }
    }

    /**
     * An editor that allows only numbers to be inputed into the combo box
     *
     * @author wolf
     */
    public static class DoubleComboBoxEditor implements ComboBoxEditor, FocusListener {
        protected JFormattedTextField editor;
        private Object oldValue;

        public DoubleComboBoxEditor() {
            editor = new JFormattedTextField(numberFormat);
            editor.setValue(new Double(1000));
            editor.setInputVerifier(new ScaleVerifier());
        }

        public Component getEditorComponent() {
            return editor;
        }

        /**
         * Sets the item that should be edited.
         *
         * @param anObject the displayed value of the editor
         */
        public void setItem(Object anObject) {
            if (anObject instanceof Number) {
                editor.setValue(new Double(((Number) anObject).doubleValue()));

                oldValue = anObject;
            }
        }

        public Object getItem() {
            Object newValue = editor.getValue();

            if (oldValue != null) {
                // The original value is not a string. Should return the value in it's
                // original type.
                if (oldValue.equals(newValue)) {
                    return oldValue;
                }
            }

            return newValue;
        }

        public void selectAll() {
            editor.selectAll();
            editor.requestFocus();
        }

        // This used to do something but now it doesn't.  It couldn't be
        // removed because it would be an API change to do so.
        public void focusGained(FocusEvent e) {
        }

        // This used to do something but now it doesn't.  It couldn't be
        // removed because it would be an API change to do so.
        public void focusLost(FocusEvent e) {
        }

        public void addActionListener(ActionListener l) {
            editor.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            editor.removeActionListener(l);
        }

        /**
         * A subclass of DoubleComboBoxEditor that implements UIResource. BasicComboBoxEditor
         * doesn't implement UIResource directly so that applications can safely override the
         * cellRenderer property with BasicListCellRenderer subclasses.
         * 
         * <p>
         * <strong>Warning:</strong> Serialized objects of this class will not be compatible with
         * future Swing releases. The current serialization support is appropriate for short term
         * storage or RMI between applications running the same version of Swing.  As of 1.4,
         * support for long term storage of all JavaBeans<sup><font size="-2">TM</font></sup> has
         * been added to the <code>java.beans</code> package. Please see {@link
         * java.beans.XMLEncoder}.
         * </p>
         */
        public static class UIResource extends DoubleComboBoxEditor
            implements javax.swing.plaf.UIResource {
        }

        public class ScaleVerifier extends InputVerifier {
            public boolean verify(JComponent input) {
                if (input instanceof JFormattedTextField) {
                    JFormattedTextField ftf = (JFormattedTextField) input;
                    AbstractFormatter formatter = ftf.getFormatter();

                    if (formatter != null) {
                        String text = ftf.getText();

                        try {
                            Number value = (Number) formatter.stringToValue(text);

                            return value.doubleValue() > 0;
                        } catch (ParseException pe) {
                            return false;
                        }
                    }
                }

                return true;
            }

            public boolean shouldYieldFocus(JComponent input) {
                return verify(input);
            }
        }
    }
}
