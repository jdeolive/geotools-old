/*
 * Created on 28-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.FeatureTypeChooser;

/**
 * @author wolf
 */
public class DefaultFeatureTypeChooser extends FeatureTypeChooser {
    JComboBox cmbNames;
	String[] typeNames;
    
    public DefaultFeatureTypeChooser(FeatureType featureType) {
        typeNames = null;
        if(featureType != null) {
            FeatureType[] ancestors = featureType.getAncestors();
            List tmpNames = new ArrayList(ancestors.length + 1);
            
            String featureTypeName = featureType.getTypeName();
            tmpNames.add(featureTypeName);
            for (int i = 0; i < ancestors.length; i++) {
                String ancestorName = ancestors[i].getTypeName();
                if(ancestorName != null)
                	tmpNames.add(ancestorName);
    		}
            
            typeNames = (String[]) tmpNames.toArray(new String[tmpNames.size()]);
        } else {
           typeNames = new String[] {""};
        }
        cmbNames = new JComboBox(typeNames);
        cmbNames.setSelectedIndex(0);
        cmbNames.setEditable(true);
        setLayout(new BorderLayout());
        add(cmbNames);
    }
    
    public void setFeatureTypeName(String typeName) {
    	for (int i = 0; i < typeNames.length; i++) {
            if(typeNames[i].equalsIgnoreCase(typeName)) {
            	cmbNames.setSelectedIndex(i);
            }
        }
    }
    
    public String getFeatureTypeName() {
    	return (String) cmbNames.getSelectedItem();
    }
}
