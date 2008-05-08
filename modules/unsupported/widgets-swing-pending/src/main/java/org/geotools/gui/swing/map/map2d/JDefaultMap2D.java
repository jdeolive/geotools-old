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

/**
 * Default implementation of Map2D
 * @author Johann Sorel
 */
public class JDefaultMap2D extends javax.swing.JPanel implements 
        org.geotools.gui.swing.map.map2d.Map2D,
        org.geotools.map.event.MapLayerListListener,
        java.beans.PropertyChangeListener {

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
    
    private static final MapDecoration[] EMPTY_OVERLAYER_ARRAY = {};
    private final List<MapDecoration> userDecorations = new ArrayList<MapDecoration>();
    private final StrategyListener strategylisten = new StrategyListen();
    private final JLayeredPane mapDecorationPane = new JLayeredPane();
    private final JLayeredPane userDecorationPane = new JLayeredPane();
    private final JLayeredPane mainDecorationPane = new JLayeredPane();
    private int nextMapDecorationIndex = 1;
    private InformationDecoration informationDecoration = new DefaultInformationDecoration();
    private MapDecoration backDecoration = new ColorDecoration();

    /**
     * create a default JDefaultMap2D
     */
    public JDefaultMap2D() {
        this.THIS_MAP = this;
        init();        
    }
    
    private void init(){
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(150,150));
        mapDecorationPane.setLayout(new BufferLayout());
        userDecorationPane.setLayout(new BufferLayout());
        mainDecorationPane.setLayout(new BufferLayout());

        mainDecorationPane.add(informationDecoration.geComponent(), new Integer(3));
        mainDecorationPane.add(userDecorationPane, new Integer(2));
        mainDecorationPane.add(mapDecorationPane, new Integer(1));

        add(BorderLayout.CENTER, mainDecorationPane);

        renderingStrategy.addStrategyListener(strategylisten);
        renderingStrategy.getContext().addMapLayerListListener(this);
        renderingStrategy.getContext().addPropertyChangeListener(this);

        mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
        mapDecorationPane.revalidate();

        setBackground(Color.WHITE);
        setOpaque(true);
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

    protected void setRendering(boolean render) {
        informationDecoration.setPaintingIconVisible(render);
    }

    //----------------------Over/Sub/information layers-------------------------
    public void setInformationDecoration(InformationDecoration info) {
        if (info == null) {
            throw new NullPointerException("info decoration can't be null");
        }

        mainDecorationPane.remove(informationDecoration.geComponent());
        informationDecoration = info;
        mainDecorationPane.add(informationDecoration.geComponent(), new Integer(3));

        mainDecorationPane.revalidate();
        mainDecorationPane.repaint();
    }

    public InformationDecoration getInformationDecoration() {
        return informationDecoration;
    }

    public void setBackgroundDecoration(MapDecoration back) {

        if (back == null) {
            throw new NullPointerException("background decoration can't be null");
        }

        mainDecorationPane.remove(backDecoration.geComponent());
        backDecoration = back;
        mainDecorationPane.add(backDecoration.geComponent(), new Integer(0));

        mainDecorationPane.revalidate();
        mainDecorationPane.repaint();
    }

    public MapDecoration getBackgroundDecoration() {
        return backDecoration;
    }

    public void addDecoration(MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public void addDecoration(int index, MapDecoration deco) {

        if (deco != null && !userDecorations.contains(deco)) {
            deco.setMap2D(THIS_MAP);
            userDecorations.add(index, deco);
            userDecorationPane.add(deco.geComponent(), new Integer(userDecorations.indexOf(deco)));
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public int getDecorationIndex(MapDecoration deco) {
        return userDecorations.indexOf(deco);
    }

    public void removeDecoration(MapDecoration deco) {
        if (deco != null && userDecorations.contains(deco)) {
            deco.setMap2D(null);
            deco.dispose();
            userDecorations.remove(deco);
            userDecorationPane.remove(deco.geComponent());
            userDecorationPane.revalidate();
            userDecorationPane.repaint();
        }
    }

    public MapDecoration[] getDecorations() {
        return userDecorations.toArray(EMPTY_OVERLAYER_ARRAY);
    }

    /**
     * add a MapDecoration between the map and the user MapDecoration
     * those MapDecoration can not be removed because they are important
     * for edition/selection/navigation.
     * @param deco : MapDecoration to add
     */
    protected void addMapDecoration(MapDecoration deco) {
        mapDecorationPane.add(deco.geComponent(), new Integer(nextMapDecorationIndex));
        nextMapDecorationIndex++;
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
        mapDecorationPane.remove(renderingStrategy.getComponent());
        renderingStrategy.removeStrategyListener(strategylisten);
        renderingStrategy.dispose();

        //adding new strategy
        renderingStrategy = newStrategy;
        renderingStrategy.addStrategyListener(strategylisten);
        renderingStrategy.setContext(context);

        mapDecorationPane.add(renderingStrategy.getComponent(), new Integer(0));
        mapDecorationPane.revalidate();
        renderingStrategy.setMapArea(area);

        fireStrategyChanged(oldStrategy, newStrategy);

    }

    public RenderingStrategy getRenderingStrategy() {
        return renderingStrategy;
    }

    public JPanel getComponent() {
        return this;
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

