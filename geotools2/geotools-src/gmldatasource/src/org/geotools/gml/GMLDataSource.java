/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:03
 */

package org.geotools.gml;
import org.geotools.datasource.*;
import org.geotools.datasource.extents.*;
import org.geotools.featuretable.*;
import java.net.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/** GMLDataSource provides the interface between the gml reader and the
 * rest of geotools.
 *
 * @author ian
 * @version $Id: GMLDataSource.java,v 1.6 2002/03/20 16:51:34 ianturton Exp $
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
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureTable ft, Extent ex) throws DataSourceException {
    

        if(ex instanceof EnvelopeExtent){
            List features = new ArrayList();
            EnvelopeExtent ee = (EnvelopeExtent)ex;
            
            
            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream()));
                
                gmlr = new GMLReader(in);
                GeometryCollection shapes = gmlr.read();
                int count = shapes.getNumGeometries();
                for(int i=0;i<count;i++){
                    Feature feat = new DefaultFeature();
                    feat.setAttributes(getColumnNames());
                    feat.setGeometry(shapes.getGeometryN(i));
                    if(ee.containsFeature(feat)){
                        features.add(feat);
                    }
                }
                
            }
            catch(IOException ioe){
                throw new DataSourceException("IO Exception loading data : "+ioe.getMessage());
            }
            catch(GMLException ge){
                throw new DataSourceException("GMLError"+ge.getMessage());
            }
            ft.addFeatures((Feature[])features.toArray(new Feature[0]));
            return ;
        }
        else{
            return;
        }
        
    }
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        stopped=true;
        gmlr.stopLoading();
    }
    
    /** Saves the given features to the datasource
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is not supported
     */
    public void exportFeatures(FeatureTable ft, Extent ex) throws DataSourceException {
    }
    
    
    
}
