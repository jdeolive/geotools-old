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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.MarkEditor;
import org.geotools.gui.swing.sldeditor.property.StrokeEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Mark;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultMarkEditor extends MarkEditor {
    Mark mark;
    String[] markNames;
    JLabel lblMarks;
    JComboBox cmbMarks;
    FillEditor fillEditor;
    StrokeEditor strokeEditor;
    JTabbedPane tbpGraphicProperties;

    /**
     * Creates a new instance of MarkEditor
     */
    public DefaultMarkEditor(FeatureType featureType) {
        this(featureType, styleBuilder.createGraphic().getMarks()[0]);
    }

    public DefaultMarkEditor(FeatureType featureType, Mark mark) {
        this.setLayout(new GridBagLayout());

        lblMarks = new JLabel("Well known mark");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = FormUtils.getDefaultInsets();
        add(lblMarks, gbc);

        markNames = styleBuilder.getWellKnownMarkNames();
        cmbMarks = new JComboBox(markNames);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = FormUtils.getDefaultInsets();
        add(cmbMarks, gbc);

        fillEditor = propertyEditorFactory.createFillEditor(featureType);
        fillEditor.setBorder(BorderFactory.createEmptyBorder());
        strokeEditor = propertyEditorFactory.createStrokeEditor(featureType);
        strokeEditor.setBorder(BorderFactory.createEmptyBorder());
        tbpGraphicProperties = new JTabbedPane();
        tbpGraphicProperties.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tbpGraphicProperties.add("Fill", fillEditor);
        tbpGraphicProperties.add("Stroke", strokeEditor);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = FormUtils.getDefaultInsets();
        add(tbpGraphicProperties, gbc);

        setMark(mark);
    }

    public void setMark(Mark mark) {
        String markName = mark.getWellKnownName().toString();
        for (int i = 0; i < markNames.length; i++) {
            if (markName.equalsIgnoreCase(markNames[i])) {
                cmbMarks.setSelectedIndex(i);

                break;
            }
        }

        if (cmbMarks.getSelectedIndex() == -1) {
            cmbMarks.setSelectedIndex(0);
        }

        fillEditor.setFill(mark.getFill());
        strokeEditor.setStroke(mark.getStroke());

        this.mark = mark;
    }

    public Mark getMark() {
        mark.setWellKnownName(styleBuilder.literalExpression((String) cmbMarks.getSelectedItem()));
        mark.setFill(fillEditor.getFill());
        mark.setStroke(strokeEditor.getStroke());

        return this.mark;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultMarkEditor(null));
    }
}
