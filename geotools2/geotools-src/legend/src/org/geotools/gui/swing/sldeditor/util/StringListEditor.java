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
 * StringListEditor.java
 *
 * Created on 7 dicembre 2003, 12.27
 */
package org.geotools.gui.swing.sldeditor.util;

import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.geotools.filter.Expression;
import org.geotools.gui.swing.sldeditor.*;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class StringListEditor extends JComponent implements SLDEditor {
    JComboBox combo;

    /**
     * Creates a new instance of StringListEditor
     *
     * @param values DOCUMENT ME!
     */
    public StringListEditor(String[] values) {
        this.setLayout(new GridBagLayout());
        combo = new JComboBox(values);
        combo.setEditable(false);
        combo.setMinimumSize(FormUtils.getComboDimension());
        if (combo.getPreferredSize().width < combo.getMinimumSize().width) {
            combo.setPreferredSize(combo.getMinimumSize());
        }

        FormUtils.addSingleRowWestComponent(this, 0, combo);
    }

    public void setExpression(Expression e) {
        combo.setSelectedItem(e.toString());
    }

    public Expression getExpression() {
        return styleBuilder.literalExpression((String) combo.getSelectedItem());
    }

    public static void main(String[] args) {
        FormUtils.show(new StringListEditor(new String[] { "one", "two", "three" }));
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        combo.setEnabled(enabled);
    }
}
