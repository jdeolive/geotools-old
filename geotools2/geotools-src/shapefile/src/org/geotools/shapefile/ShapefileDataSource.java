/*
 * ShapefileDataSource.java
 *
 * Created on March 4, 2002, 1:48 PM
 */

package org.geotools.shapefile;

import java.util.ArrayList;
import java.util.List;
import java.io.*;

import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import com.vividsolutions.jts.geom.*;

/**
 *
 * @author  jamesm
 */
public class ShapefileDataSource implements org.geotools.datasource.DataSource {
    Shapefile shapefile;
    /** Creates a new instance of ShapefileDataSource */
    public ShapefileDataSource(Shapefile shapefile) {
        
    }
    
    /** gets the Column names (used by FeatureTable) for this DataSource
     */
    public String[] getColumnNames() {
        return new String[]{"Geometry"};
    }
    
    /** Loads Feature rows for the given Extent from the datasource
     */
    public List load(Extent ex) throws DataSourceException {
        if(ex instanceof EnvelopeExtent){
            List features = new ArrayList();
            EnvelopeExtent ee = (EnvelopeExtent)ex;
            Envelope bounds = ee.getBounds();
            try{
                GeometryCollection shapes = shapefile.read(new GeometryFactory());
                int count = shapes.getNumGeometries();
                for(int i=0;i<count;i++){
                    Feature feat = new Feature();
                    feat.columnNames = getColumnNames();
                    Object [] row = new Object[1];
                    feat.row = row;
                    feat.row[0] = shapes.getGeometryN(i);
                    if(ex.containsFeature(feat)){
                        features.add(feat);
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
            
            return null;
        }
        else{
            return null;
        }
    }
    
    /** Saves the given features to the datasource
     */
    public void save(List features) throws DataSourceException {
        GeometryFactory fac = new GeometryFactory();
        GeometryCollection gc = fac.createGeometryCollection((GeometryCollection[])features.toArray(new Geometry[0]));
        try{
            shapefile.write(gc);
        }
        catch(Exception e){
            {
               throw new DataSourceException(e.getMessage()); 
            }
        }
            
    }
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        //can't sorry
    }
    
}
