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
import org.geotools.filter.*;
import com.vividsolutions.jts.geom.*;

/**
 * @version $Id: ShapefileDataSource.java,v 1.13 2002/07/24 14:50:30 jmacgill Exp $
 * @author James Macgill, CCG
 * @task TODO: add support for reading dbf file
 * @task TODO: add support for the optional spatial index files to improve
 *             loading of sub regions
 */
public class ShapefileDataSource implements org.geotools.data.DataSource {
    /**
     * The shapefile object that this datasouce connects to
     **/
    private Shapefile shapefile;
    
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
    
    /** Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     * @task TODO: Implement addFeatures method
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox() {
        return shapefile.getBounds();
    }
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Envelope getBbox(boolean speed) {
        return getBbox();
    }
    
    /** 
     * Loads features from the datasource into the returned collection, based
     * on the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
        FeatureCollectionDefault fc = new FeatureCollectionDefault();
        getFeatures(fc, filter);
        return fc;
    }
    
    /** 
     * Loads features from the datasource into the passed collection, based
     * on the passed filter.  Note that all data sources must support this
     * method at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {
        try {
            GeometryCollection shapes = shapefile.read(new GeometryFactory());
            List features = new ArrayList();
            Geometry typical = shapes.getGeometryN(0);
            AttributeType geometryAttribute = new AttributeTypeDefault(Shapefile.getShapeTypeDescription(Shapefile.getShapeType(typical)), Geometry.class);
            
            FeatureType shapefileType = new FeatureTypeFlat(geometryAttribute);
            System.out.println("schema is " + shapefileType);
            FeatureFactory fac = new FeatureFactory(shapefileType);
            int count = shapes.getNumGeometries();
            //Feature[] features = new Feature[count];
            for (int i = 0; i < count; i++){
                Object [] row = new Object[1];
                row[0] = (Geometry) shapes.getGeometryN(i);
                System.out.println("adding geometry" + row[0]);
                Feature feature = fac.create(row);
                if (filter.contains(feature)){
                    collection.addFeatures(new Feature[]{feature});
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
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     * @task TODO: Implement support for modification of features (single attribute)
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     * @task TODO: Implement support for modification of feature (multi attribute)
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
        throw new DataSourceException("Modification of features is not yet supported by this datasource");
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     * @task TODO: Implement support for removal of features
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
        throw new DataSourceException("Removal of features is not yet supported by this datasource");
    }
    
}
