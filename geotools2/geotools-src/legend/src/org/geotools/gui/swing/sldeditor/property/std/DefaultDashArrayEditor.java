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
 * DashArrayEditor.java
 *
 * Created on 7 dicembre 2003, 16.21
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JComboBox;

import org.geotools.feature.Feature;
import org.geotools.gui.swing.sldeditor.property.DashArrayEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.renderer.LegendIconMaker;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Symbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultDashArrayEditor extends DashArrayEditor {
    private ArrayList dashArray;
    private Icon[] dashIcon;
    private Feature sample;
    private JComboBox cboDash;

    /**
     * Creates a new instance of DashArrayEditor
     */
    public DefaultDashArrayEditor() {
        setLayout(new GridBagLayout());
        cboDash = new JComboBox();
        FormUtils.addSingleRowWestComponent(this, 0, cboDash);

        dashArray = new ArrayList();

        for (int i = 0; i < 10; i++) {
            float[] a1 = new float[2];
            a1[0] = i + 1;
            a1[1] = 3;
            dashArray.add(a1);
        }

        initDashIcon();

        int height = cboDash.getPreferredSize().height;
        cboDash.setPreferredSize(new Dimension(FormUtils.getComboDimension().width, height));
    }

    public void setDashArray(float[] dash) {
        for (int i = 0; i < dashArray.size(); i++) {
            float[] currDash = (float[]) dashArray.get(i);
            if (Arrays.equals(dash, currDash)) {
                cboDash.setSelectedIndex(i);

                return;
            }
        }

        // not predefined, add
        dashArray.add(dash);
        initDashIcon();
        cboDash.setSelectedIndex(dashArray.size() - 1);
    }

    public float[] getDashArray() {
        return (float[]) dashArray.get(cboDash.getSelectedIndex());
    }

    private void initDashIcon() {
        if (dashArray != null) {
            dashIcon = new Icon[dashArray.size()];
            cboDash.removeAllItems();

            // cboDash.setRenderer(new DashComboBoxRenderer());
            for (int i = 0; i < dashArray.size(); i++) {
                LineSymbolizer l = styleFactory.getDefaultLineSymbolizer();
                l.setGeometryPropertyName(null);

                org.geotools.styling.Stroke s = styleBuilder.createStroke(Color.black, 2.0);
                s.setDashArray((float[]) dashArray.get(i));
                l.setStroke(s);
                dashIcon[i] = LegendIconMaker.makeLegendIcon(50, 20, this.getBackground(),
                        new Symbolizer[] { l }, null, true);
                cboDash.addItem(dashIcon[i]);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        cboDash.setEnabled(enabled);
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultDashArrayEditor());
    }
}
