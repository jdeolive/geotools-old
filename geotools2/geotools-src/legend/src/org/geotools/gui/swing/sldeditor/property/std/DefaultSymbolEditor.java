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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.geotools.gui.swing.sldeditor.property.MarkEditor;
import org.geotools.gui.swing.sldeditor.property.SymbolEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Symbol;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultSymbolEditor extends SymbolEditor {
    private Symbol symbol;
    JLabel lblSymbolType;
    JComboBox cmbSymbolType;
    MarkEditor markEditor;
    ExternalGraphicEditor egxEditor;

    /**
     * Creates a new instance of GraphicItemEditor
     */
    public DefaultSymbolEditor(FeatureType featureType) {
        this(featureType, styleBuilder.createGraphic().getSymbols()[0]);
    }

    public DefaultSymbolEditor(FeatureType featureType, Symbol symbol) {
        setLayout(new GridBagLayout());
        setOpaque(true);

        cmbSymbolType = new JComboBox(new String[] { "Mark", "External graphic" });
        cmbSymbolType.setSelectedIndex(0);
                cmbSymbolType.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        switchEditor();
                    }
                });
        markEditor = propertyEditorFactory.createMarkEditor(featureType);
        egxEditor = propertyEditorFactory.createExternalGraphicEditor();

        lblSymbolType = new JLabel("Symbol type");

        FormUtils.addRowInGBL(this, 0, 0, lblSymbolType, cmbSymbolType);
		FormUtils.addRowInGBL(this, 1, 0, new JLabel(""));
        
        setEditor(markEditor);
        setSymbol(symbol);
    }
    
    private void switchEditor() {
        if(cmbSymbolType.getSelectedIndex() == 0)
            setEditor(markEditor);
        else
            setEditor(egxEditor);

        Window parent = FormUtils.getWindowForComponent(this);
        parent.pack();
    }
    
    private void setEditor(JComponent component) {
        if(component == markEditor)
            remove(egxEditor);
        else
            remove(markEditor);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(component, gbc);
    }

    public void setSymbol(Symbol symbol) {
       this.symbol = symbol;
       
       if(this.symbol instanceof Mark) {
           markEditor.setMark((Mark) symbol);
           cmbSymbolType.setSelectedIndex(0);
       } else if(this.symbol instanceof ExternalGraphic) {
           egxEditor.setExternalGraphic((ExternalGraphic) symbol);
           cmbSymbolType.setSelectedIndex(1);
       }
    }

    public Symbol getSymbol() {
        if(cmbSymbolType.getSelectedIndex() == 0)
            return markEditor.getMark();
        else
            return egxEditor.getExternalGraphic();
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultSymbolEditor(null));
    }
}
