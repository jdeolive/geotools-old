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
import org.geotools.filter.Expression;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;


/**
 * A wrapper that puts in the same container a full expression editor and a simpler, more user
 * friendly editor, with a toggle button to let the user choose if use  one or the other.
 *
 * @author wolf
 */
public class ExpressionEditorWrapper extends ExpressionEditor {
    private DefaultExpressionEditor expressionEditor;
    private ExpressionEditor simpleEditor;
    private ExpressionEditor currentEditor;
    private JToggleButton btnChoose;
    private boolean expertMode;

    public ExpressionEditorWrapper(ExpressionEditor simpleExpressionEditor, FeatureType ft,
        boolean expertMode) {
        setLayout(new BorderLayout());
        btnChoose = new JToggleButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/ComposeMail16.gif")));
        add(simpleExpressionEditor);
        add(btnChoose, BorderLayout.EAST);
        btnChoose.setSelected(false);
        btnChoose.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    toggleEditor();
                }
            });
        this.expertMode = expertMode;
        this.simpleEditor = simpleExpressionEditor;
        this.expressionEditor = new DefaultExpressionEditor(ft);
        this.currentEditor = simpleExpressionEditor;
        btnChoose.setVisible(expertMode);
        btnChoose.setPreferredSize(FormUtils.getButtonDimension());
        btnChoose.setMinimumSize(FormUtils.getButtonDimension());
    }

    private void toggleEditor() {
        Expression exp = currentEditor.getExpression();
        ExpressionEditor newEditor = null;

        if (currentEditor == simpleEditor) {
            newEditor = expressionEditor;
        } else {
            if ((exp != null) && !simpleEditor.canEdit(exp)) {
                int result = JOptionPane.showConfirmDialog(this,
                        "The simple editor cannot fully manage the current expression, if you choose ok a simpler version will be created. Want to proceeed?",
                        "Expression editor", JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.NO_OPTION) {
                    btnChoose.setSelected(true);

                    return;
                }
            }

            newEditor = simpleEditor;
        }

        setCurrentEditor(newEditor, exp);
    }

    private void setCurrentEditor(ExpressionEditor newEditor, Expression exp) {
        newEditor.setExpression(exp);

        if (newEditor != currentEditor) {
            remove(currentEditor);
            add(newEditor, BorderLayout.CENTER);
            currentEditor = newEditor;
            revalidate();
            repaint();
        }
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#getExpression()
     */
    public Expression getExpression() {
        return currentEditor.getExpression();
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#setExpression(org.geotools.filter.Expression)
     */
    public void setExpression(Expression expression) {
        if (simpleEditor.canEdit(expression)) {
            setCurrentEditor(simpleEditor, expression);
            btnChoose.setSelected(false);
        } else {
            setCurrentEditor(expressionEditor, expression);
            btnChoose.setSelected(true);
        }
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.ExpressionEditor#canEdit(org.geotools.filter.Expression)
     */
    public boolean canEdit(Expression expression) {
        return true;
    }
    
    
    /**
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        simpleEditor.setEnabled(enabled);
        expressionEditor.setEnabled(enabled);
        btnChoose.setEnabled(enabled);
    }

}

