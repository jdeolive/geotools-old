/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:03
 */

package org.geotools.gml;
import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import java.net.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** GMLDataSource provides the interface between the gml reader and the
 * rest of geotools.
 *
 * @author ian
 * @version $Id: GMLDataSource.java,v 1.4 2002/03/12 12:51:42 ianturton Exp $
 */
public class GMLDataSource implements org.geotools.datasource.DataSource{
    boolean stopped=false;
    URL source;
    GMLReader gmlr;
    String[] columnNames = new String[0];
    /** Creates a new instance of GMLReader
     * @param src the url pointing to the resource
     */
    public GMLDataSource(URL src) {
        source=src;
    }
    
    /** gets the Column names (used by FeatureTable) for this DataSource
     * @return the names of the columns of the feature table
     */
    public String[] getColumnNames() {
        
        return new String[]{"Geometry"};
        
    }
    
    /** Loads Feature rows for the given Extent from the datasource
     * @param ex An extent to limit the data returned
     * @throws DataSourceException if any exceptions are generated reading the resource
     * @return List of features
     */
    public List load(Extent ex) throws DataSourceException {

        if(ex instanceof EnvelopeExtent){
            List features = new ArrayList();
            EnvelopeExtent ee = (EnvelopeExtent)ex;
            
            
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream()));
                
                gmlr = new GMLReader(in);
                GeometryCollection shapes = gmlr.read();
                int count = shapes.getNumGeometries();
                for(int i=0;i<count;i++){
                    Feature feat = new Feature();
                    feat.columnNames = getColumnNames();
                    Object [] row = new Object[1];
                    feat.row = row;
                    feat.row[0] = shapes.getGeometryN(i);
                    if(ee.containsFeature(feat)){
                        features.add(feat);
                    }
                }
                
            }
            catch(IOException ioe){
                throw new DataSourceException("IO Exception loading data : "+ioe.getMessage());
            }
            return features;
        }
        else{
            return null;
        }
        
    }
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        stopped=true;
        gmlr.stopLoading();
    }
    
    /** Saves the given features to the datasource
     * not currently implemented
     * @param features the features to be saved
     *
     * @throws DataSourceException if anything goes wrong in the write
     */
    public void save(List features) throws DataSourceException {
    }
    
}
