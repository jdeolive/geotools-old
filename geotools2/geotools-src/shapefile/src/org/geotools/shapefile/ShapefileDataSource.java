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

package org.geotools.shapefile;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;
import com.vividsolutions.jts.geom.*;

/**
 * @version $Id: ShapefileDataSource.java,v 1.9 2002/07/12 14:58:50 loxnard Exp $
 * @author James Macgill, CCG
 */
public class ShapefileDataSource implements org.geotools.data.DataSource {
    Shapefile shapefile;
    /** Creates a new instance of ShapefileDataSource. */
    public ShapefileDataSource(Shapefile shapefile) {
        this.shapefile = shapefile;
    }
    
    /**
     * Gets the Column names (used by FeatureTable) for this DataSource.
     */
    public String[] getColumnNames() {
        return new String[]{"Geometry"};
    }
    
    /**
     * Loads Feature rows for the given Extent from the datasource.
     */
    public void importFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        if (ex instanceof EnvelopeExtent){
            try {
                GeometryCollection shapes = shapefile.read(new GeometryFactory());
                List features = new ArrayList();
                EnvelopeExtent ee = (EnvelopeExtent) ex;
                Envelope bounds = ee.getBounds();
                Geometry typical = shapes.getGeometryN(0);
                AttributeType geometryAttribute = new AttributeTypeDefault(Shapefile.getShapeTypeDescription(Shapefile.getShapeType(typical)), Geometry.class);
                
                FeatureType shapefileType = new FeatureTypeFlat(geometryAttribute);
                System.out.println("schema is " + shapefileType);
                FeatureFactory fac = new FeatureFactory(shapefileType);
                int count = shapes.getNumGeometries();
                //Feature[] features = new Feature[count];
                for(int i = 0; i < count; i++){
                    //Feature feat = new FlatFeature();
                    
                    Object [] row = new Object[1];
                    row[0] = (Geometry) shapes.getGeometryN(i);
                    System.out.println("adding geometry" + row[0]);
                    Feature feature = fac.create(row);
                    if (ex.containsFeature(feature)){
                        ft.addFeatures(new Feature[]{feature});
                    }
                }
            }
            catch (IOException ioe){
                throw new DataSourceException("IO Exception loading data : " + ioe.getMessage());
            }
            catch (ShapefileException se){
                throw new DataSourceException("Shapefile Exception loading data : " + se.getMessage());
            }
            catch (TopologyException te){
                throw new DataSourceException("Topology Exception loading data : " + te.getMessage());
            }
            catch (IllegalFeatureException ife){
                throw new DataSourceException("Illegal Feature Exception loading data : " + ife.getMessage());
            }
            
            
        }
        
    }
    
    /**
     * Saves the given features to the datasource.
     * TODO: write the export code.
     */
    public void exportFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        throw new DataSourceException("Exporting of shapefiles not yet supported");
       /* GeometryFactory fac = new GeometryFactory();
        GeometryCollection gc = fac.createGeometryCollection((GeometryCollection[])features.toArray(new Geometry[0]));
        try{
            shapefile.write(gc);
        }
        catch(Exception e){
            {
               throw new DataSourceException(e.getMessage());
            }
        }*/
        
    }
    
    /**
     * Stops this DataSource from loading.
     */
    public void stopLoading() {
        //can't sorry
    }
    
    /**
     * Gets the extent of this datasource using the default speed of
     * this datasource as set by the implementer.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return new EnvelopeExtent(shapefile.getBounds());
    }
    
    /**
     * Gets the extent of this datasource using the speed of
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return getExtent();
    }
    
}
