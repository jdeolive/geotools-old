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
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;
import org.opengis.cs.*;
import java.util.Hashtable;

//logging
import java.util.logging.Logger;


/**
 * DOCUMENT ME!
 *
 * @author James Macgill, CCG
 * @version $Id: DefaultMap.java,v 1.11 2003/05/16 21:10:19 jmacgill Exp $
 *
 * @deprecated Use ContextImpl instead.
 */
public class DefaultMap implements org.geotools.map.Map {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.defaultcore");
    private Hashtable tables = new Hashtable();


    /**
     * DOCUMENT ME!
     *
     * @param ft DOCUMENT ME!
     * @param style DOCUMENT ME!
     */
    public void addFeatureTable(
        FeatureCollection ft,
        Style style
    ) {
        tables.put(ft, style);
    }

    /**
     * DOCUMENT ME!
     *
     * @param fc DOCUMENT ME!
     */
    public void removeFeatureTable(FeatureCollection fc) {
        tables.remove(fc);
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
        java.util.Enumeration layers = tables.keys();

        while (layers.hasMoreElements()) {
            FeatureCollection ft = (FeatureCollection) layers.nextElement();
            Style style = (Style) tables.get(ft);

            try {
                LOGGER.finer("Envelope = " + envelope.toString());

                Feature[] features =
                    ft.getFeatures(new EnvelopeExtent(envelope)); //TODO: this could be a bottle neck
                LOGGER.finest(
                    "real renderer call " + features.length + " features"
                );
                renderer.render(features, envelope, style);
            } catch (DataSourceException dse) {
                //HACK: should deal with this exception properly
                //HACK: or ensure that there is a method in feature table we can call without fear
                System.err.println(dse);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cs DOCUMENT ME!
     */
    public void setCoordinateSystem(CS_CoordinateSystem cs) {}
}
