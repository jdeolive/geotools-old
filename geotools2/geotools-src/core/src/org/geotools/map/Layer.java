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

import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;

/**
 * Layer is an aggregation of both a FeatureCollection and Style.
 * @version $Id: Layer.java,v 1.6 2003/08/03 03:28:15 seangeo Exp $
 * @author  Cameron Shorter
 */
public interface Layer {
    /**
     * Get the style for this layer.  If style has not been set, then null is
     * returned.
     * @return The style (SLD).
     */
    public Style getStyle();
    
    /**
     * Get the dataSource for this layer.  If dataSource has not
     * been set yet, then null is returned.
     *
     * Ammended (IanS) changed to FeatureCollection
     * 
     */
    public FeatureCollection getFeatures();
    
    /**
     * Specify whether this layer is visable on a MapPane or whether the layer
     * is hidden.
     * @param visable Set the layer visable if TRUE.
     */
    public void setVisability(boolean visable);

    /**
     * Determine whether this layer is visable on a MapPane or whether the layer
     * is hidden.
     * @return TRUE if the layer is visable.
     * @task There is a typo here, change name to getVisibility()
     */
    public boolean getVisability();

    /** Set the title of this layer.
     * @title The title of this layer.
     */ 
    public void setTitle(String title);
}