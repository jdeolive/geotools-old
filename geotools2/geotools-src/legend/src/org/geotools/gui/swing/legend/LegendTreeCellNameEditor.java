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
 * LegendTreeCellNameEditor.java
 *
 * Created on 11 July 2003, 01:58
 */
package org.geotools.gui.swing.legend;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.swing.AbstractCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;

import org.geotools.gui.swing.Legend;


/**
 * A TreeCellEditor to edit every legend node name by trible click, since
 * double click will open a node.
 *
 * @author jianhuij
 */
public class LegendTreeCellNameEditor extends AbstractCellEditor
    implements TreeCellEditor, ActionListener {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend");

    /** userObject from a LegendTreeCell */
    private Object value;

    /** The editor for editing name of cell or the title of the legend note */
    private JTextField textField;

    /** how many clicks needs to start editing */
    private int clickCountToStart = 3;

    /** parent legend */
    private Legend legend;

    public LegendTreeCellNameEditor(Legend legend) {
        this.legend = legend;
        textField = new JTextField();
        clickCountToStart = 3;
        textField.addActionListener(this);
    }

    /**
     * Returns a reference to the editor component.
     *
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return textField;
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value,
        boolean isSelected, boolean expanded, boolean leaf, int row) {
        setValue(((DefaultMutableTreeNode) value).getUserObject());

        return textField;
    }

    public void setValue(Object value) {
        this.value = value;
        textField.setText((value != null) ? value.toString() : "");
    }

    /**
     * Returns true if <code>anEvent</code> is <b>not</b> a
     * <code>MouseEvent</code>.  Otherwise, it returns true if the necessary
     * number of clicks have occurred, and returns false otherwise.
     *
     * @param anEvent the event
     *
     * @return true  if cell is ready for editing, false otherwise
     *
     * @see #setClickCountToStart
     * @see #shouldSelectCell
     */
    public boolean isCellEditable(EventObject anEvent) {
        boolean editable = true;
        if (anEvent instanceof MouseEvent) {
            editable = ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
        }
        return editable;
    }

    public Object getCellEditorValue() {
        if (value instanceof LegendRootNodeInfo) {
            ((LegendRootNodeInfo) value).getMapContext().setTitle(textField.getText());
        } else if (value instanceof LegendLayerNodeInfo) {
            ((LegendLayerNodeInfo) value).getMapLayer().getStyle().setTitle(textField.getText());
        } else if (value instanceof LegendRuleNodeInfo) {
            ((LegendRuleNodeInfo) value).getRule().setTitle(textField.getText());
        }

        //legend.invalidate();
        //legend.repaint();
        //legend.contextChanged();
        //legend.layerListChanged( new java.util.EventObject(this) );
        return value;
    }

    /**
     * Specifies the number of clicks needed to start editing.
     *
     * @param count an int specifying the number of clicks needed to start
     *        editing
     *
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(int count) {
        clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     *
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
        return clickCountToStart;
    }

    /**
     * Returns true to indicate that the editing cell may be selected.
     *
     * @param anEvent the event
     *
     * @return true
     *
     * @see #isCellEditable
     */
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    /**
     * Returns true to indicate that editing has begun.
     *
     * @param anEvent the event
     *
     * @return DOCUMENT ME!
     */
    public boolean startCellEditing(EventObject anEvent) {
        return true;
    }

    /**
     * Stops editing and returns true to indicate that editing has stopped.
     * This method calls <code>fireEditingStopped</code>.
     *
     * @return true
     */
    public boolean stopCellEditing() {
        fireEditingStopped();
        legend.contextChanged();

        return true;
    }

    /**
     * Cancels editing.  This method calls <code>fireEditingCanceled</code>.
     */
    public void cancelCellEditing() {
        fireEditingCanceled();
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        stopCellEditing();
    }
}
