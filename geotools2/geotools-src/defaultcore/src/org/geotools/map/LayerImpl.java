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

import org.geotools.styling.Style;
import org.geotools.data.DataSource;
import org.geotools.feature.Feature;

/**
 * Layer is an aggregation of both a FeatureCollection and Style.
 * @version $Id: LayerImpl.java,v 1.1 2003/02/23 11:29:57 camerons Exp $
 * @author  Cameron Shorter
 * @task REVISIT: This class maybe should contain CoordinateSystem, which
 * could either be set externally, or derived from one of its features.
 */

public class LayerImpl implements Layer {

    /**
     * Specify the DataSource which provides the features for this layer.
     */
    private DataSource dataSource;
    
    /**
     * Specify the style for this layer.
     */
    private Style style;

    /**
     * Specify whether this layer is visable or not.  Defaults to TRUE on
     * initialisation.
     */
    private boolean visability=true;
    
    /**
     * The title of this layer for use in Legend and similar.
     */
    private String title;
    
    /** Creates a new instance of DefaultLayer.
     * @param dataSource The dataSource to query in order to get features for
     * this layer.
     * @param style The style to use when rendering features associated with
     * this layer.*/
    public LayerImpl(
            DataSource dataSource,
            Style style
            ) {
        this.dataSource=dataSource;
        this.style=style;
        visability=true;
    }

    /**
     * Get the dataSource for this layer.  If dataSource has not
     * been set yet, then null is returned.
     */
    public DataSource getDataSource() {
        if (dataSource==null){
            return null;
        }else{
            return dataSource;
        }
    }
    
    /**
     * Get the style for this layer.  If style has not been set, then null is
     * returned.
     * @return The style (SLD).
     */
    public Style getStyle() {
        if (style==null){
            return null;
        }else{
            return style;
        }
    }
    
    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     * @param visable Set the layer visable if TRUE.
     */
    public void setVisability(boolean visability) {
        this.visability=visability;
    }
    
    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.  Visibility defaults to TRUE on initialisation.
     * @param visable Set the layer visable if TRUE.
     */
    public boolean getVisability() {
        return visability;
    }

    /** Get the title of this layer.  If title has not been defined then an
     * empty string is returned.
     * @return The title of this layer.
     */ 
    public String getTitle() {
        if (title==null){
            return new String("");
        }else{
            return title;
        }
    }

    /** Set the title of this layer.
     * @title The title of this layer.
     */ 
    public void setTitle(String title) {
        this.title = title;
    }

    /** Return the title of this layer.  If no title has been defined, then
     * the class name is returned.
     */
    public String toString() {
        if (title==null){
            return super.toString();
        }else{
            return title;
        }
    }
}
