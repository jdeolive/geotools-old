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

import org.geotools.gui.swing.map.map2d.Map2D;
import org.geotools.gui.swing.map.map2d.Map2D.ACTION_STATE;
import org.geotools.gui.swing.map.map2d.NavigableMap2D;
import org.geotools.gui.swing.map.map2d.handler.DefaultPanHandler;

/**
 *
 * @author johann sorel
 */
public class PanAction extends AbstractAction {

    private Map2D map = null;

    public void actionPerformed(ActionEvent arg0) {
        if (map != null && map instanceof NavigableMap2D) {
            ((NavigableMap2D) map).setNavigationHandler(new DefaultPanHandler());
            map.setActionState(ACTION_STATE.NAVIGATE);
        }
    }

    public Map2D getMap() {
        return map;
    }

    public void setMap(Map2D map) {
        this.map = map;
        setEnabled(map != null && map instanceof NavigableMap2D);
    }
}
