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

import java.util.ArrayList;
import java.util.List;

import org.geotools.gui.swing.map.map2d.event.RenderingStrategyEvent;
import org.geotools.gui.swing.map.map2d.handler.NavigationHandler;
import org.geotools.gui.swing.map.map2d.listener.Map2DNavigationListener;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.gui.swing.map.map2d.event.Map2DNavigationEvent;
import org.geotools.gui.swing.map.map2d.handler.DefaultPanHandler;
import org.geotools.gui.swing.map.map2d.strategy.RenderingStrategy;

/**
 * Default implementation of NavigableMap2D
 * @author Johann Sorel
 */
public class JDefaultNavigableMap2D extends JDefaultMap2D implements NavigableMap2D {

    private final List<Envelope> mapAreas = new ArrayList<Envelope>();
    private Envelope lastMapArea = null;
    private NavigationHandler navigationHandler = new DefaultPanHandler();

    /**
     * create a default JDefaultNavigableMap2D
     */
    public JDefaultNavigableMap2D() {
        super();
    }
    
    private void fireHandlerChanged(NavigationHandler oldhandler, NavigationHandler newhandler) {
        Map2DNavigationEvent mce = new Map2DNavigationEvent(this, oldhandler, newhandler);

        Map2DNavigationListener[] lst = getNavigableMap2DListeners();

        for (Map2DNavigationListener l : lst) {
            l.navigationHandlerChanged(mce);
        }

    }
    
    //----------------------Map2d override--------------------------------------
    @Override
    protected void mapContextChanged(RenderingStrategyEvent event) {
        super.mapContextChanged(event);        
        mapAreas.clear();
        lastMapArea = getRenderingStrategy().getMapArea();
    }

    @Override
    protected void mapAreaChanged(RenderingStrategyEvent event) {
        super.mapAreaChanged(event);

        while (mapAreas.size() > 10) {
            mapAreas.remove(0);
        }

        Envelope newMapArea = event.getMapArea();
        lastMapArea = newMapArea;

        if (!mapAreas.contains(newMapArea)) {
            mapAreas.add(newMapArea);
        } 

    }

    @Override
    public void setRenderingStrategy(RenderingStrategy stratege) {
        if (actionState == ACTION_STATE.NAVIGATE && navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }
        
        super.setRenderingStrategy(stratege);
        
        if (actionState == ACTION_STATE.NAVIGATE) {
            navigationHandler.install(this);
        }

    }
    
    @Override
    public void setActionState(ACTION_STATE state) {
                        
        if (state == ACTION_STATE.NAVIGATE && !navigationHandler.isInstalled()) {
            navigationHandler.install(this);
        } else if (state != ACTION_STATE.NAVIGATE && navigationHandler.isInstalled()) {
            navigationHandler.uninstall();
        }
        
        super.setActionState(state);

    }
    
    //-----------------------NAVIGABLEMAP2D-------------------------------------
        
    public void setNavigationHandler(NavigationHandler newHandler) {
        if (newHandler == null) {
            throw new NullPointerException();
        } else if (newHandler != navigationHandler) {

            NavigationHandler oldHandler = navigationHandler;
            
            if (navigationHandler.isInstalled()) {
                navigationHandler.uninstall();
            }

            navigationHandler = newHandler;

            if (actionState == ACTION_STATE.NAVIGATE) {
                navigationHandler.install(this);
            }

            fireHandlerChanged(oldHandler,newHandler);
        }
    }

    public NavigationHandler getNavigationHandler() {
        return navigationHandler;
    }

    public void previousMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index--;
            if (index >= 0) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void nextMapArea() {
        if (lastMapArea != null) {
            int index = mapAreas.indexOf(lastMapArea);

            index++;
            if (index < mapAreas.size()) {
                getRenderingStrategy().setMapArea(mapAreas.get(index));
            }
        }
    }

    public void addNavigableMap2DListener(Map2DNavigationListener listener) {
        MAP2DLISTENERS.add(Map2DNavigationListener.class, listener);
    }

    public void removeNavigableMap2DListener(Map2DNavigationListener listener) {
        MAP2DLISTENERS.remove(Map2DNavigationListener.class, listener);
    }

    public Map2DNavigationListener[] getNavigableMap2DListeners() {
        return MAP2DLISTENERS.getListeners(Map2DNavigationListener.class);
    }
}
