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

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;

import com.vividsolutions.jts.geom.Geometry;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultGeometryChooser extends GeometryChooser {
    FeatureType type;
    JComboBox cmbGeomProperties;
    boolean hasGeometryProperties;
    Vector geomProperties;

    /**
     * Creates a new instance of GeometryChooser
     */
    public DefaultGeometryChooser() {
        this(null);
    }

    public DefaultGeometryChooser(FeatureType type) {
        setLayout(new BorderLayout());
        cmbGeomProperties = new JComboBox();
        add(cmbGeomProperties);

        geomProperties = getGeomProperties(type);

        if (type == null) {
            hasGeometryProperties = false;
            cmbGeomProperties.setEnabled(false);
        } else {
            cmbGeomProperties.setModel(new DefaultComboBoxModel(geomProperties));
            cmbGeomProperties.setSelectedItem(type.getDefaultGeometry().getName());
        }
    }

    private Vector getGeomProperties(FeatureType type) {
        Vector names = new Vector();

        if (type != null) {
            for (int i = 0; i < type.getAttributeCount(); i++) {
                AttributeType at = type.getAttributeType(i);
                if (Geometry.class.isAssignableFrom(at.getType())) {
                    names.add(at.getName());
                }
            }
        }

        return names;
    }

    public int getGeomPropertiesCount() {
        return geomProperties.size();
    }

    public String getSelectedName() {
        return (String) cmbGeomProperties.getSelectedItem();
    }

    public void setSelectedName(String name) {
        if (type == null) {
            return;
        }

        if (name == null) {
            cmbGeomProperties.setSelectedItem(type.getDefaultGeometry());
        } else {
            cmbGeomProperties.setSelectedItem(name);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        cmbGeomProperties.setEnabled(enabled);
    }
}
