/*
 * ShapefileDataSource.java
 *
 * Created on March 4, 2002, 1:48 PM
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
 *
 * @author  jamesm
 */
public class ShapefileDataSource implements org.geotools.data.DataSource {
    Shapefile shapefile;
    /** Creates a new instance of ShapefileDataSource */
    public ShapefileDataSource(Shapefile shapefile) {
        this.shapefile = shapefile;
    }
    
    /** gets the Column names (used by FeatureTable) for this DataSource
     */
    public String[] getColumnNames() {
        return new String[]{"Geometry"};
    }
    
    /** Loads Feature rows for the given Extent from the datasource
     */
    public void importFeatures(FeatureCollection ft,Extent ex) throws DataSourceException {
        if(ex instanceof EnvelopeExtent){
            try{
                GeometryCollection shapes = shapefile.read(new GeometryFactory());
                List features = new ArrayList();
                EnvelopeExtent ee = (EnvelopeExtent)ex;
                Envelope bounds = ee.getBounds();
                Geometry typical = shapes.getGeometryN(0);
                AttributeType geometryAttribute = new AttributeTypeDefault(Shapefile.getShapeTypeDescription(Shapefile.getShapeType(typical)), Geometry.class);
                
                FeatureType shapefileType = new FeatureTypeFlat(geometryAttribute);
                System.out.println("schema is "+shapefileType);
                FeatureFactory fac = new FeatureFactory(shapefileType);
                int count = shapes.getNumGeometries();
                //Feature[] features = new Feature[count];
                for(int i=0;i<count;i++){
                    //Feature feat = new FlatFeature();
                    
                    Object [] row = new Object[1];
                    row[0] = (Geometry)shapes.getGeometryN(i);
                    System.out.println("adding geometry"+row[0]);
                    Feature feature = fac.create(row);
                    if(ex.containsFeature(feature)){
                        ft.addFeatures(new Feature[]{feature});
                    }
                }
            }
            catch(IOException ioe){
                throw new DataSourceException("IO Exception loading data : "+ioe.getMessage());
            }
            catch(ShapefileException se){
                throw new DataSourceException("Shapefile Exception loading data : "+se.getMessage());
            }
            catch(TopologyException te){
                throw new DataSourceException("Topology Exception loading data : "+te.getMessage());
            }
            catch(IllegalFeatureException ife){
                throw new DataSourceException("Illigal Feature Exception loading data : "+ife.getMessage());
            }
            
            
        }
        
    }
    
    /** Saves the given features to the datasource
     * TODO: write the export code
     */
    public void exportFeatures(FeatureCollection ft,Extent ex) throws DataSourceException {
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
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        //can't sorry
    }
    
    /** gets the extent of this data source using the default speed of
     * this datasource as set by the implementer.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return new EnvelopeExtent(shapefile.getBounds());
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return getExtent();
    }
    
}
