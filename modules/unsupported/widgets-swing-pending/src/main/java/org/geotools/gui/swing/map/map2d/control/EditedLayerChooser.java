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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.geotools.gui.swing.map.map2d.EditableMap2D;
import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.StreamingMap2D;
import org.geotools.gui.swing.map.map2d.event.RenderingStrategyEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEditionEvent;
import org.geotools.gui.swing.map.map2d.event.Map2DEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DEditionListener;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.misc.Render.LayerListRenderer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 *
 * @author johann sorel
 */
public class EditedLayerChooser extends JComboBox {

    private StreamingMap2D map = null;
    private MapContext editionContext = null;
    private MapLayer editionLayer = null;
    private ItemListener listListener = new ItemListener() {

        public void itemStateChanged(ItemEvent e) {

            if (map != null && map instanceof EditableMap2D) {
                int selected = getSelectedIndex();
                EditableMap2D editmap = (EditableMap2D) map;

                if (selected >= 1) {
                    editionLayer = editmap.getRenderingStrategy().getContext().getLayer(selected - 1);
                    editmap.setEditedMapLayer(editionLayer);
                } else {
                    editmap.setEditedMapLayer(null);
                    editionLayer = null;
                }
            }
        }
    };
    private Map2DEditionListener editmapListener = new Map2DEditionListener() {

        public void editedLayerChanged(Map2DEditionEvent event) {
            MapLayer layer = event.getEditedLayer();

            removeItemListener(listListener);
            if (layer != null && !editionLayer.equals(layer)) {
                editionLayer = layer;
                setSelectedItem(layer);
            }else if (layer == null && getSelectedIndex() != 0){
                setSelectedIndex(0);
            }
            addItemListener(listListener);
        }

        public void editionHandlerChanged(Map2DEditionEvent event) {
        }
    };
    
    private StrategyListener strategyListener = new StrategyListener() {

        public void setRendering(boolean rendering) {
        }

        public void mapAreaChanged(RenderingStrategyEvent event) {
        }

        public void mapContextChanged(RenderingStrategyEvent event) {
            if (editionContext != null) {
                editionContext.removeMapLayerListListener(contextListener);
            }

            editionContext = event.getContext();

            if (editionContext != null) {
                editionContext.addMapLayerListListener(contextListener);
            }

            initComboBox();
        }
    };
    private Map2DListener mapListener = new Map2DListener() {

        public void mapStrategyChanged(Map2DEvent mapEvent) {
            mapEvent.getPreviousStrategy().removeStrategyListener(strategyListener);
            mapEvent.getStrategy().addStrategyListener(strategyListener);
        }

        public void mapActionStateChanged(Map2DEvent mapEvent) {
        }
    };
    private MapLayerListListener contextListener = new MapLayerListListener() {

        public void layerAdded(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerRemoved(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerChanged(MapLayerListEvent event) {
            initComboBox();
        }

        public void layerMoved(MapLayerListEvent event) {
            initComboBox();
        }
    };

    public EditedLayerChooser() {
        setRenderer(new LayerListRenderer());
        initComboBox();
             
        setBorder(null);
        setOpaque(false);
        
    }

    private void initComboBox() {

        removeItemListener(listListener);
        removeAllItems();
        addItem("-");

        if (editionContext != null) {
            setEnabled(true);
            MapLayer[] layers = editionContext.getLayers();

            for (MapLayer layer : layers) {
                addItem(layer);
            }

        } else {
            setEnabled(false);
        }

        if (editionLayer != null) {
            setSelectedItem(editionLayer);
        }

        addItemListener(listListener);
    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(StreamingMap2D map2d) {

        if (map != null) {
            map.getRenderingStrategy().removeStrategyListener(strategyListener);
            map.removeMap2DListener(mapListener);

            if (map instanceof EditableMap2D) {
                ((EditableMap2D) map).removeEditableMap2DListener(editmapListener);
            }

        }

        if (map2d != null) {
            map = map2d;
            editionContext = map.getRenderingStrategy().getContext();            
            map.addMap2DListener(mapListener);
            map.getRenderingStrategy().addStrategyListener(strategyListener);

            if (map instanceof EditableMap2D) {
                editionLayer = ((EditableMap2D) map).getEditedMapLayer();
                ((EditableMap2D) map).addEditableMap2DListener(editmapListener);
                setEnabled(true);
            }else{
                setEnabled(false);
            }            
        } else {
            map = null;
            editionContext = null;
            editionLayer = null;
            setEnabled(false);
        }

        initComboBox();
    }

    
}
