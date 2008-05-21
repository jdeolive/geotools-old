/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gui.swing.map.map2d;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import javax.swing.plaf.DimensionUIResource;
import org.geotools.gui.swing.map.map2d.decoration.DefaultInformationDecoration;
import org.geotools.gui.swing.map.map2d.decoration.MapDecoration;
import org.geotools.gui.swing.map.map2d.event.RenderingStrategyEvent;
import org.geotools.gui.swing.map.map2d.listener.Map2DListener;
import org.geotools.gui.swing.map.map2d.listener.StrategyListener;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;
import org.geotools.gui.swing.map.map2d.strategy.SingleBufferedImageStrategy;
import org.geotools.map.MapContext;
import org.geotools.gui.swing.map.map2d.decoration.ColorDecoration;
import org.geotools.gui.swing.map.map2d.decoration.InformationDecoration;
import org.geotools.gui.swing.map.map2d.event.Map2DEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;

/**
 * Default implementation of Map2D
 * @author Johann Sorel
 */
public class JDefaultMap2D extends AbstractMap2D implements StreamingMap2D, MapLayerListListener, PropertyChangeListener {

    /**
     * Action state of the map widget
     */
    protected ACTION_STATE actionState = ACTION_STATE.NONE;
    /**
     * EventListenerList to manage all possible Listeners
     */
    protected final EventListenerList MAP2DLISTENERS = new EventListenerList();
    /**
     * Map2D reference , same as "this" but needed to explicitly point to the 
     * map2d object when coding a private class
     */
    protected final JDefaultMap2D THIS_MAP;
    /**
     * Rendering Strategy of the map2d widget, should never be null
     */
    protected RenderingStrategy renderingStrategy = new SingleBufferedImageStrategy();
    
    private final StrategyListener strategylisten = new StrategyListen();

    /**
     * create a default JDefaultMap2D
     */
    public JDefaultMap2D() {
        super();
        this.THIS_MAP = this;
        setMapComponent(renderingStrategy.getComponent());
    }
    
    private void fireStrategyChanged(RenderingStrategy oldOne, RenderingStrategy newOne) {
        Map2DEvent mce = new Map2DEvent(this, actionState, oldOne, newOne);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapStrategyChanged(mce);
        }

    }

    private void fireActionStateChanged(ACTION_STATE oldone, ACTION_STATE newone) {
        Map2DEvent mce = new Map2DEvent(this, oldone, newone, renderingStrategy);

        Map2DListener[] lst = getMap2DListeners();

        for (Map2DListener l : lst) {
            l.mapActionStateChanged(mce);
        }

    }

    //----------------------Use as extend for subclasses------------------------
    protected void mapAreaChanged(RenderingStrategyEvent event) {

    }

    protected void mapContextChanged(RenderingStrategyEvent event) {
        event.getPreviousContext().removePropertyChangeListener(this);
        event.getContext().addPropertyChangeListener(this);
        
        event.getPreviousContext().removeMapLayerListListener(this);
        event.getContext().addMapLayerListListener(this);
    }

    public void propertyChange(PropertyChangeEvent arg0) {

    }


    //-----------------------------MAP2D----------------------------------------
    public void dispose(){
        renderingStrategy.getContext().removePropertyChangeListener(this);
        renderingStrategy.getContext().removeMapLayerListListener(this);
        renderingStrategy.dispose();
    }
    
    public void setActionState(ACTION_STATE newstate) {

        if (actionState != newstate) {
            ACTION_STATE oldstate = actionState;
            actionState = newstate;
            fireActionStateChanged(oldstate, newstate);
        }

    }

    public ACTION_STATE getActionState() {
        return actionState;
    }

    public void setRenderingStrategy(RenderingStrategy newStrategy) {

        if (newStrategy == null) {
            throw new NullPointerException();
        }

        RenderingStrategy oldStrategy = renderingStrategy;

        //removing old strategy
        MapContext context = renderingStrategy.getContext();
        Envelope area = renderingStrategy.getMapArea();
        renderingStrategy.removeStrategyListener(strategylisten);
        renderingStrategy.dispose();

        //adding new strategy
        renderingStrategy = newStrategy;
        renderingStrategy.addStrategyListener(strategylisten);
        renderingStrategy.setContext(context);
        
        setMapComponent(renderingStrategy.getComponent());
        
        renderingStrategy.setMapArea(area);

        fireStrategyChanged(oldStrategy, newStrategy);

    }

    public RenderingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public void addMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.add(Map2DListener.class, listener);
    }

    public void removeMap2DListener(Map2DListener listener) {
        MAP2DLISTENERS.remove(Map2DListener.class, listener);
    }

    public Map2DListener[] getMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DListener.class);
    }

    //---------------------- PRIVATE CLASSES------------------------------------    
    
    private class StrategyListen implements StrategyListener {

        public void setRendering(boolean rendering) {
            THIS_MAP.setRendering(rendering);
        }

        public void mapContextChanged(RenderingStrategyEvent event) {
            THIS_MAP.mapContextChanged(event);
        }

        public void mapAreaChanged(RenderingStrategyEvent event) {
            THIS_MAP.mapAreaChanged(event);
        }
    }

    //--------------------MapLayerListListener----------------------------------
    public void layerAdded(MapLayerListEvent event) {
    }

    public void layerRemoved(MapLayerListEvent event) {
    }

    public void layerChanged(MapLayerListEvent event) {
    }

    public void layerMoved(MapLayerListEvent event) {
    }
}

