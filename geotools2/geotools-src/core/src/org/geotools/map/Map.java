/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.map;

import org.geotools.renderer.Renderer;
import org.geotools.feature.FeatureCollection;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * Holds a set of FeatureCollections together with styles and a single output
 * coordinate system.
 *
 * @version $Id: Map.java,v 1.11 2003/03/08 07:40:00 camerons Exp $
 * @author James Macgill, CCG
 * @deprecated Use Context instead.
 */
public interface Map {
    
    /**
     * Sets the coordinate system to be used when outputting the features in
     * this map.
     * The features being added to it may well have a variety of different
     * coordinate systems.
     * @param cs The single coordinate system that all features will be
     *           converted to for output.
     */
    void setCoordinateSystem(CS_CoordinateSystem cs);
    
    /**
     * Adds a collection of features to this map together with a style
     * specification.  The features need not have the same coordinate system
     * as the one specified for this map, though it is strongly recommended
     * that all features within the collection share a single coordinate
     * system.
     * @task REVISIT: Confirm if single cs should be enforced.
     * @task TODO: Rename this method addFeatureCollection.
     *
     * @param fc The collection of features to add.
     * @param style The style to apply to these features.
     */
    void addFeatureTable(FeatureCollection fc, Style style);
    
    void removeFeatureTable(FeatureCollection fc);
    
    /**
     * Displays or outputs the portion of the map that falls within a 
     * specified envelope using the provided renderer.
     * @param renderer The renderer which will produce the output.
     * @param envelope The portion of the map to be rendered.
     */
    void render(Renderer renderer, Envelope envelope);
    
}

