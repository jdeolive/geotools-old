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

package org.geotools.gml;


import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;

import org.xml.sax.helpers.XMLFilterImpl;


/**
 * 
 *
 * @author Darren Edmonds
 */
public class GMLReceiver extends XMLFilterImpl implements GMLHandlerFeature {
    
    /** */
    private FeatureCollection featureCollection;

    
    /**
     * Creates a new instance of GMLReceiver
     *
     * @param featureCollection sets the FeatureCollection
     */
    public GMLReceiver( FeatureCollection featureCollection ) {
        this.featureCollection = featureCollection;
    }
    
    
    /**
     * Receives an OGC feature and adds it into the collection
     *
     * @param feature the OGC feature
     */
    public void feature( Feature feature ) {
        featureCollection.add( feature );
    }
    
}