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
package org.geotools.gui.swing.sldeditor.symbolizer.std;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.geotools.gui.swing.sldeditor.util.SymbolizerUtils;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;


class DefaultSymbolizerChooserDialog extends SymbolizerChooserDialog {
    JRadioButton rbtPoint;
    JRadioButton rbtPolygon;
    JRadioButton rbtLine;
    JRadioButton rbtText;
    JRadioButton rbtRaster;
    JLabel label;
    boolean exitOk = false;

    public DefaultSymbolizerChooserDialog(Component parent, FeatureType ft) {
        super(JOptionPane.getFrameForComponent(parent), true);
        setTitle("Symbolizer list");

        label = new JLabel("Choose a symbolizer type:");
        rbtPoint = new JRadioButton("Point symbolizer");
        rbtLine = new JRadioButton("Line symbolizer");
        rbtPolygon = new JRadioButton("Polygon symbolizer");
        rbtText = new JRadioButton("Text symbolizer");
        rbtRaster = new JRadioButton("Raster symbolizer");

        Insets insets = rbtPoint.getBorder().getBorderInsets(rbtPoint);
        Border border = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom,
                insets.right);
        label.setBorder(border);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rbtLine);
        buttonGroup.add(rbtPoint);
        buttonGroup.add(rbtPolygon);
        buttonGroup.add(rbtText);
        buttonGroup.add(rbtRaster);

        JPanel pnlCommands = new JPanel();
        pnlCommands.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));

        JButton btnOk = new JButton("Ok");
        JButton btnCancel = new JButton("Cancel");
        pnlCommands.add(btnOk);
        pnlCommands.add(btnCancel);

        btnOk.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    dispose();
                    exitOk = true;
                }
            });
        btnCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    dispose();
                    exitOk = false;
                }
            });

        Container c = getContentPane();
        c.setLayout(new GridLayout(7, 1));
        c.add(label);
        c.add(rbtPoint);
        c.add(rbtLine);
        c.add(rbtPolygon);
        c.add(rbtText);
        c.add(rbtRaster);
        c.add(pnlCommands);

        setResizable(false);
        setDefaultSelection(ft);
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ft
     */
    private void setDefaultSelection(FeatureType ft) {
        Symbolizer symbolizer = SymbolizerUtils.getDefaultSymbolizer(ft);

        if (symbolizer instanceof LineSymbolizer) {
            rbtLine.setSelected(true);
        } else if (symbolizer instanceof PointSymbolizer) {
            rbtPoint.setSelected(true);
        } else if (symbolizer instanceof PolygonSymbolizer) {
            rbtPolygon.setSelected(true);
        } else if (symbolizer instanceof TextSymbolizer) {
            rbtText.setSelected(true);
        } else if (symbolizer instanceof RasterSymbolizer) {
            rbtRaster.setSelected(true);
        }
    }

    public int getSelectionCode() {
        if (rbtPoint.isSelected()) {
            return DefaultSymbolizerChooserDialog.POINT;
        } else if (rbtLine.isSelected()) {
            return DefaultSymbolizerChooserDialog.LINE;
        } else if (rbtText.isSelected()) {
            return DefaultSymbolizerChooserDialog.POLYGON;
        } else {
            return DefaultSymbolizerChooserDialog.RASTER;
        }
    }

    public boolean exitOk() {
        return exitOk;
    }
}
