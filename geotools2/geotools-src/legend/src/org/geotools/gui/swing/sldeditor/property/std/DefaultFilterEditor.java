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

import org.geotools.feature.FeatureType;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.Filter;
import org.geotools.filter.parser.ParseException;
import org.geotools.gui.swing.sldeditor.property.FilterEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;


/**
 * A basic implementation
 *
 * @author wolf
 */
public class DefaultFilterEditor extends FilterEditor {
    /** The SLDEditor package logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.gui.swing.sldeditor");
    private Filter filter;
    private JTextField txtExpression;
    private Exception lastException;
    private String lastInput;
    private JButton btnWizard;
    private FeatureType featureType;

    public DefaultFilterEditor(FeatureType featureType) {
        this.featureType = featureType;
        setLayout(new BorderLayout());
        txtExpression = new JTextField();
        btnWizard = new JButton(new ImageIcon(getClass().getResource("/org/geotools/resources/wizard.gif")));
        btnWizard.setPreferredSize(FormUtils.getButtonDimension());
        add(txtExpression);
        add(btnWizard, BorderLayout.EAST);

        btnWizard.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    openDialog();
                }
            });
    }

    public DefaultFilterEditor(Filter f, FeatureType featureType) {
        this(featureType);
        setFilter(f);
    }

    public void openDialog() {
        ExpressionDialog dialog = null;
        Window w = FormUtils.getWindowForComponent(this);

        if (w instanceof Frame) {
            dialog = new ExpressionDialog((Frame) w, true, featureType);
        } else {
            dialog = new ExpressionDialog((Dialog) w, true, featureType);
        }

        dialog.setRawText(txtExpression.getText());
        dialog.show();

        if (dialog.exitOk()) {
            txtExpression.setText(dialog.getRawText());
        }
    }

    /**
     * Returns the Expression typed by the user
     *
     * @return Returns either the Expression or the Filter parsed, or null if no parseable content
     *         is present
     */
    public Filter getFilter() {
        Filter result = null;
        lastInput = txtExpression.getText().trim();

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
                return "Current input is an expression, not a filter";
            }
        }
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.basic.ExpressionEditor#setExpression
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
        txtExpression.setText(filter.toString());
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtExpression.setEnabled(enabled);
        btnWizard.setEnabled(enabled);
    }
}
