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
package org.geotools.gui.swing.sldeditor.property.std;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.filter.Expression;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.parser.ParseException;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class ExpressionDialog extends JDialog implements SLDEditor {
    private static Logger LOGGER = Logger.getLogger("it.satanet.swing.gui.sldeditor");
    private Object lastException;
    private String lastInput;
    private JPanel contentPanel;
    private JPanel editingPanel;
    private JPanel commandPanel;
    private JButton btnOk;
    private JButton btnCancel;
    private JTextArea txaExpression;
    private JLabel lblOperators;
    private JLabel lblAttributes;
    private JComboBox cmbOperators;
    private JComboBox cmbAttributes;
    private FeatureType featureType;
    private boolean exitOk;

    public ExpressionDialog(Frame parent, boolean modal, FeatureType featureType) {
        super(parent, modal);
        this.featureType = featureType;
        init();
        setLocationRelativeTo(parent);
    }

    public ExpressionDialog(Dialog parent, boolean modal, FeatureType featureType) {
        super(parent, modal);
        this.featureType = featureType;
        init();
        setLocationRelativeTo(parent);
    }

    private void init() {
        String[] operators = new String[] { "+", "-", "*", "/", "<", "<=", "==", ">=", ">" };
        String[] attributes = getAttributesNamesFromType();

        // generate components
        cmbAttributes = new JComboBox(new DefaultComboBoxModel(attributes));
        cmbOperators = new JComboBox(new DefaultComboBoxModel(operators));
        lblAttributes = new JLabel("Attributes");
        lblOperators = new JLabel("Operators");
        txaExpression = new JTextArea();
        btnOk = new JButton("Ok");
        btnCancel = new JButton("Cancel");
        commandPanel = new JPanel();
        editingPanel = new JPanel();
        contentPanel = new JPanel();

        // setup command panel
        commandPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        commandPanel.add(btnOk);
        commandPanel.add(btnCancel);

        // setup editing panel
        editingPanel.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(editingPanel, 0, 0, lblAttributes, cmbAttributes);
        FormUtils.addRowInGBL(editingPanel, 0, 2, lblOperators, cmbOperators);
        FormUtils.addFiller(editingPanel, 1, 0, new JScrollPane(txaExpression));

        // setup the main panel
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(editingPanel);
        contentPanel.add(commandPanel, BorderLayout.SOUTH);
        setContentPane(contentPanel);
        setTitle("Style editor");
        pack();

        // setup event handlers
        btnOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitOk = true;
                    dispose();
                }
            });
        btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitOk = false;
                    dispose();
                }
            });

        ActionListener selectionPaster = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox combo = (JComboBox) e.getSource();
                    String selection = (String) combo.getSelectedItem();
                    txaExpression.insert(selection, txaExpression.getCaretPosition());
                }
            };

        cmbAttributes.addActionListener(selectionPaster);
        cmbOperators.addActionListener(selectionPaster);
        pack();
    }

    public boolean exitOk() {
        return exitOk;
    }

    private String[] getAttributesNamesFromType() {
        List attributes = new ArrayList();

        if(featureType == null)
            return new String[0];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            attributes.add(featureType.getAttributeType(i).getName());
        }

        return (String[]) attributes.toArray(new String[attributes.size()]);
    }

    public void setExpression(Expression e) {
        txaExpression.setText(e.toString());
    }

    public void setFilter(Filter f) {
        txaExpression.setText(f.toString());
    }

    /**
     * Returns the Expression typed by the user
     *
     * @return Returns either the Expression or the Filter parsed, or null if no parseable content
     *         is present
     */
    public Expression getExpression() {
        Expression result = null;
        lastInput = txaExpression.getText();

        try {
            result = (Expression) ExpressionBuilder.parse(lastInput);
            lastException = null;
        } catch (ParseException e) {
            lastException = e;

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ExpressionBuilder.getFormattedErrorMessage(e, lastInput));
            }

            result = null;
        } catch (ClassCastException e) {
            lastException = e;
            result = null;
        }

        return result;
    }

    /**
     * Returns the Expression typed by the user
     *
     * @return Returns either the Expression or the Filter parsed, or null if no parseable content
     *         is present
     */
    public Filter getFilter() {
        Filter result = null;
        lastInput = txaExpression.getText().trim();

        if (lastInput.equals("")) {
            return null;
        }

        try {
            result = (Filter) ExpressionBuilder.parse(lastInput);
            lastException = null;
        } catch (ParseException e) {
            lastException = e;

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ExpressionBuilder.getFormattedErrorMessage(e, lastInput));
            }

            result = null;
        } catch (ClassCastException e) {
            lastException = e;
            result = null;
        }

        return result;
    }
    
    public String getRawText() {
        return txaExpression.getText();
    }

    /**
     * Returns a formatted error message providing a better description of the problem encountered
     * while parsing the expression
     *
     * @return
     */
    public String getFormattedErrorMessage() {
        if (lastException == null) {
            return null;
        } else {
            if (lastException instanceof ParseException) {
                return ExpressionBuilder.getFormattedErrorMessage((ParseException) lastException,
                    lastInput);
            } else {
                return "Current input is a filter, not an expression";
            }
        }
    }

    public static void main(String[] args) throws Exception {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",
                com.vividsolutions.jts.geom.Polygon.class);
        AttributeType[] attributeTypes = new AttributeType[] {
                geom, AttributeTypeFactory.newAttributeType("name", String.class),
                AttributeTypeFactory.newAttributeType("population", Long.class)
            };

        FeatureType ft = DefaultFeatureTypeFactory.newFeatureType(attributeTypes, "demo", "",
                false, null, (GeometryAttributeType) geom);
        ExpressionDialog dialog = new ExpressionDialog((Frame) null, false, ft);
        dialog.show();
    }

    /**
     * @param string
     */
    public void setRawText(String string) {
        txaExpression.setText(string);        
    }
}
