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
 * GraphicFillbtnGraphic.java
 *
 * Created on 10 dicembre 2003, 19.21
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.renderer.LegendIconMaker;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Symbolizer;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultGraphicFillEditor extends GraphicEditor {
    JButton btnGraphic;
    Graphic graphic;
    boolean polygonSymbolizer = false;
    Dimension iconDimension;
    FeatureType featureType;

    public DefaultGraphicFillEditor(Dimension d, boolean polygonSymbolizer, FeatureType featureType) {
        this(styleBuilder.createGraphic(), d, polygonSymbolizer, featureType);
    }

    /**
     * Creates a new instance of GraphicFillbtnGraphic
     *
     * @param g DOCUMENT ME!
     * @param d DOCUMENT ME!
     * @param polygonSymbolizer DOCUMENT ME!
     */
    public DefaultGraphicFillEditor(Graphic g, Dimension d, boolean polygonSymbolizer, FeatureType featureType) {
        this.featureType = featureType;
        this.polygonSymbolizer = polygonSymbolizer;

        this.iconDimension = d;
        this.setPreferredSize(iconDimension);
        this.setMinimumSize(iconDimension);

        btnGraphic = new JButton();
        btnGraphic.setPreferredSize(iconDimension);
        btnGraphic.setMinimumSize(iconDimension);
        btnGraphic.setText("");

        setLayout(new java.awt.GridBagLayout());
        this.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(btnGraphic, gridBagConstraints);

        // make it stay on the west side
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(new JLabel(), gridBagConstraints);

        setGraphic(g);

        btnGraphic.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    GraphicDialog gd = new GraphicDialog(DefaultGraphicFillEditor.this,
                            graphic);
                    gd.setTitle("Edit graphic fill");
                    gd.show();

                    if (gd.exitOk) {
                        setGraphic(gd.getGraphic());
                    }
                }
            });
    }

    public void setGraphic(Graphic graphic) {
        this.graphic = graphic;

        Symbolizer symbolizer = null;

        if (polygonSymbolizer) {
            Fill fill = styleBuilder.createFill();
            fill.setGraphicFill(graphic);
            symbolizer = styleBuilder.createPolygonSymbolizer(styleBuilder
                    .createStroke(), fill);
        } else {
            org.geotools.styling.Stroke stroke = styleBuilder.createStroke();
            stroke.setGraphicStroke(graphic);
            symbolizer = styleBuilder.createLineSymbolizer(stroke);
        }

        btnGraphic.setIcon(LegendIconMaker.makeLegendIcon(iconDimension.width,
                iconDimension.height, Color.WHITE,
                new Symbolizer[] { symbolizer }, null, true));
    }

    public Graphic getGraphic() {
        return this.graphic;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        btnGraphic.setEnabled(enabled);
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultGraphicFillEditor(new Dimension(50, 50), true, null));
    }

    private class GraphicDialog extends JDialog {
        private GraphicEditor graphicEditor;
        public boolean exitOk = false;

        public GraphicDialog(Component parent, Graphic g) {
            super(JOptionPane.getFrameForComponent(parent), true);

            JPanel content = new JPanel();
            JPanel command = new JPanel();
            JButton okbtnGraphic = new JButton("Ok");
            JButton cancelbtnGraphic = new JButton("Cancel");

            command.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));
            command.add(okbtnGraphic);
            command.add(cancelbtnGraphic);

            graphicEditor = propertyEditorFactory.createGraphicEditor(featureType);
            content.setLayout(new BorderLayout());
            content.add(graphicEditor);
            content.add(command, BorderLayout.SOUTH);

            okbtnGraphic.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        exitOk = true;
                        dispose();
                    }
                });
            cancelbtnGraphic.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        exitOk = false;
                        dispose();
                    }
                });

            setContentPane(content);
            pack();
            setLocationRelativeTo(parent);
        }

        public Graphic getGraphic() {
            return graphicEditor.getGraphic();
        }
    }
}
