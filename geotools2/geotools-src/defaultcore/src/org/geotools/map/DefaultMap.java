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

import java.util.Hashtable;
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;
import org.opengis.cs.*;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @version $Id: DefaultMap.java,v 1.4 2002/06/04 16:49:54 loxnard Exp $
 * @author James Macgill, CCG
 */
public class DefaultMap implements org.geotools.map.Map {
    
    private Hashtable tables = new Hashtable();
    
    /** Creates a new instance of DefaultMap */
    public DefaultMap() {
    }

    public void addFeatureTable(FeatureCollection ft, Style style) {
        tables.put(ft,style);
    }
    
    public void render(Renderer renderer, Envelope envelope) {
        java.util.Enumeration layers = tables.keys();
        while(layers.hasMoreElements()){
            FeatureCollection ft = (FeatureCollection)layers.nextElement();
            Style style = (Style)tables.get(ft);
            try{
                Feature[] features = ft.getFeatures(new EnvelopeExtent(envelope));//TODO: this could be a bottle neck
                renderer.render(features,envelope,style);
            }
            catch(DataSourceException dse){
                //HACK: should deal with this exception properly
                //HACK: or ensure that there is a method in feature table we can call without fear
                System.err.println(dse);
            }
            
        }
    }
    
    public void setCoordinateSystem(CS_CoordinateSystem cs) {
    }
    
}
