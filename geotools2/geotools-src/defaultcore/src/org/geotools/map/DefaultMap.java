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

package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;
import org.opengis.cs.*;
import java.util.Hashtable;
import java.util.List;

//logging
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author James Macgill, CCG
 * @version $Id: DefaultMap.java,v 1.14 2003/07/17 07:09:53 ianschneider Exp $
 *
 * @deprecated Use ContextImpl instead.
 */
public class DefaultMap implements org.geotools.map.Map {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.defaultcore");
    private Hashtable tables = new Hashtable();
    private List tableOrder = new ArrayList();

    /**
     * DOCUMENT ME!
     *
     * @param ft the featureCollection to draw
     * @param style the Style to use in drawing the object
     */
    public void addFeatureTable(
        FeatureCollection ft,
        Style style
    ) {
        if(ft == null) throw new IllegalArgumentException("Feature Collection can not be null in DefaultMap.addFeatureTable");
        if(style == null) throw new IllegalArgumentException("Style can not be null in DefaultMap.addFeatureTable");
        tables.put(ft, style);
        tableOrder.add(ft);
    }

    /**
     * DOCUMENT ME!
     *
     * @param fc DOCUMENT ME!
     */
    public void removeFeatureTable(FeatureCollection fc) {
        tables.remove(fc);
        tableOrder.remove(fc);
    }

    /**
     * renders the portion of the map conteined within a specified reagion
     * using a supplied renderer.
     *
     * @param renderer The renderer which will draw the map.
     * @param envelope The region to draw
     *
     * @task TODO: Look at performace implication of calling FeatureCollection
     *       each time
     * @task HACK: DataSourceExceptions cought but not processed
     */
    public void render(
        Renderer renderer,
        Envelope envelope
    ) {
        java.util.Iterator layers = tableOrder.iterator();
        
        while (layers.hasNext()) {
            FeatureCollection ft = (FeatureCollection) layers.next();
            Style style = (Style) tables.get(ft);



                renderer.render( (Feature[]) ft.toArray(new Feature[ft.size()]), envelope, style);
            
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cs DOCUMENT ME!
     */
    public void setCoordinateSystem(CS_CoordinateSystem cs) {}
}
