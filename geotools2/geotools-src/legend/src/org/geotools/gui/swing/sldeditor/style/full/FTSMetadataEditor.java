/*
 * Created on 15-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.style.full;

import javax.swing.JLabel;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.FeatureTypeChooser;
import org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.FeatureTypeStyle;

/**
 * @author wolf
 */
public class FTSMetadataEditor extends BasicMetadataEditor {
    
    private FeatureTypeChooser ftEditor;

    private JLabel lblFeatureType;

    public FTSMetadataEditor(FeatureTypeStyle fts, FeatureType ft) {
        metadataLabel.setText("Feature type style metadata");
        
        
        lblFeatureType = new JLabel("Feature type");
        ftEditor = PropertyEditorFactory.createPropertyEditorFactory().createFeatureTypeChooser(ft);
        int lastRow = getLastRow();
        FormUtils.addRowInGBL(this, lastRow + 1, 0, FormUtils.getTitleLabel("Feature type"));
        FormUtils.addRowInGBL(this, lastRow + 2, 0, lblFeatureType, ftEditor);
        
        txtName.setText(toText(fts.getName()));
        txtTitle.setText(toText(fts.getTitle()));
        txaAbstract.setText(toText(fts.getAbstract()));
    }
    
    public void fillFeatureTypeStyle(FeatureTypeStyle fts) {
        fts.setName(txtName.getText());
        fts.setTitle(txtTitle.getText());
        fts.setAbstract(txaAbstract.getText());
        fts.setFeatureTypeName(ftEditor.getFeatureTypeName());
    }
}
