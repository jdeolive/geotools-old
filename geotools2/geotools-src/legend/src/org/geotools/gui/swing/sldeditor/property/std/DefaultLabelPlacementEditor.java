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
 * GraphicItemEditor.java
 *
 * Created on 8 dicembre 2003, 10.46
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.LabelPlacementEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultLabelPlacementEditor extends LabelPlacementEditor {
    LabelPlacement labelPlacement;
    JCheckBox chkUseLabelPlacement;
    JComboBox cmbPlacementType;
    DefaultPointPlacementEditor ppEditor;
    DefaultLinePlacementEditor lpEditor;

    /**
     * Creates a new instance of LabelPlacementEditor
     */
    public DefaultLabelPlacementEditor(FeatureType featureType) {
        this(featureType, styleBuilder.createPointPlacement());
    }

    public DefaultLabelPlacementEditor(FeatureType featureType, LabelPlacement labelPlacement) {
         setLayout(new GridBagLayout());

        cmbPlacementType = new JComboBox(new String[] { "Point placement", "Line placement" });
        cmbPlacementType.setSelectedIndex(0);
                cmbPlacementType.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        switchEditor();
                    }
                });
        ppEditor = new DefaultPointPlacementEditor(featureType);
        lpEditor = new DefaultLinePlacementEditor(featureType);

        chkUseLabelPlacement = new JCheckBox("Label placement");

        FormUtils.addRowInGBL(this, 0, 0, chkUseLabelPlacement, cmbPlacementType);
        
        setEditor(ppEditor);
        setLabelPlacement(labelPlacement);
    }
    
    private void switchEditor() {
        if(cmbPlacementType.getSelectedIndex() == 0)
            setEditor(lpEditor);
        else
            setEditor(ppEditor);

        Window parent = FormUtils.getWindowForComponent(this);
        parent.pack();
    }
    
    private void setEditor(JComponent component) {
        if(component == ppEditor)
            remove(ppEditor);
        else
            remove(lpEditor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        add(component, gbc);
    }

    public void setLabelPlacement(LabelPlacement labelPlacement) {
       this.labelPlacement = labelPlacement;
       
       if(labelPlacement instanceof PointPlacement) {
           ppEditor.setPointPlacement((PointPlacement) labelPlacement);
           cmbPlacementType.setSelectedIndex(0);
       } else if(labelPlacement instanceof LinePlacement) {
           lpEditor.setLinePlacement((LinePlacement) labelPlacement);
           cmbPlacementType.setSelectedIndex(1);
       }
    }

    public LabelPlacement getLabelPlacement() {
        if(cmbPlacementType.getSelectedIndex() == 0)
            return ppEditor.getPointPlacement();
        else
            return lpEditor.getLinePlacement();
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultLabelPlacementEditor(null));
    }
}
