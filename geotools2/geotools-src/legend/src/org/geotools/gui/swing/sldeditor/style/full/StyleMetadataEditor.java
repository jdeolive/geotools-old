/*
 * Created on 15-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.style.full;

import org.geotools.styling.Style;

/**
 * @author wolf
 */
public class StyleMetadataEditor extends BasicMetadataEditor {
    
    public StyleMetadataEditor() {
        metadataLabel.setText("Style metadata");
    }
    
    public void setStyle(Style s) {
        txtName.setText(toText(s.getName()));
        txtTitle.setText(toText(s.getTitle()));
        txaAbstract.setText(toText(s.getAbstract()));
    }
    
    public void fillMetadata(Style s) {
        s.setName(txtName.getText());
        s.setTitle(txtTitle.getText());
        s.setAbstract(txaAbstract.getText());
    }
}
