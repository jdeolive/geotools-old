/*
 * DefaultMap.java
 *
 * Created on April 12, 2002, 1:24 PM
 */

package org.geotools.map;

import java.util.Hashtable;
import org.geotools.datasource.*;
import org.geotools.featuretable.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;
import org.opengis.cs.*;

/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 *
 * @author jamesm
 */
public class DefaultMap implements org.geotools.map.Map {
    
    private Hashtable tables = new Hashtable();
    
    /** Creates a new instance of DefaultMap */
    public DefaultMap() {
    }

    public void addFeatureTable(FeatureTable ft, Style style) {
        tables.put(ft,style);
    }
    
    public void render(Renderer renderer, Extent extent) {
        java.util.Enumeration layers = tables.keys();
        while(layers.hasMoreElements()){
            FeatureTable ft = (FeatureTable)layers.nextElement();
            Style style = (Style)tables.get(ft);
            try{
                Feature[] features = ft.getFeatures(extent);//TODO: this could be a bottle neck
                renderer.render(features,extent,style);
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
