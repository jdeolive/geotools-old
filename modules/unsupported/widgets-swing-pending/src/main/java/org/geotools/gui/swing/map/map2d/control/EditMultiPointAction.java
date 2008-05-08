/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2007, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.gui.swing.map.map2d.control;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.geotools.data.FeatureStore;
import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.event.Map2DEditionEvent;
import org.geotools.gui.swing.map.map2d.handler.MultiPointCreationHandler;
import org.geotools.gui.swing.map.map2d.listener.Map2DEditionListener;
import org.geotools.map.MapLayer;

import com.vividsolutions.jts.geom.MultiPoint;

/**
 *
 * @author johann sorel
 */
public class EditMultiPointAction extends AbstractAction {

    private Map2D map = null;
    private Map2DEditionListener listener = new Map2DEditionListener() {


        public void editedLayerChanged(Map2DEditionEvent event) {
            checkLayer(event.getEditedLayer());
        }

        public void editionHandlerChanged(Map2DEditionEvent event) {
        }
    };

    protected void checkLayer(MapLayer editionLayer) {

        if (editionLayer != null) {

            if (editionLayer.getFeatureSource() instanceof FeatureStore) {
                
                Class jtsClass = null;
                jtsClass = editionLayer.getFeatureSource().getSchema().getDefaultGeometry().getType().getBinding();

                if (jtsClass != null && jtsClass.equals(MultiPoint.class)) {
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } else {
                setEnabled(false);
            }

        }else{
            setEnabled(false);
        }

    }

    public void actionPerformed(ActionEvent arg0) {
        if (map != null && map instanceof EditableMap2D) {
            ((EditableMap2D) map).setEditionHandler(new MultiPointCreationHandler());
        }

    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map) {

        if (this.map != null && this.map instanceof EditableMap2D) {
            ((EditableMap2D) this.map).removeEditableMap2DListener(listener);
        }
        this.map = map;

        if (this.map != null && this.map instanceof EditableMap2D) {
            ((EditableMap2D) this.map).addEditableMap2DListener(listener);
            checkLayer(((EditableMap2D) this.map).getEditedMapLayer());
        } else {
            setEnabled(false);
        }

    }
}
