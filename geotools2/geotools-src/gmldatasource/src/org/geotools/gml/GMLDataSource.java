/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:03
 */

package org.geotools.gml;
import org.geotools.datasource.*;
import java.net.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;
/**
 *
 * @author  ian
 */
public class GMLDataSource implements org.geotools.datasource.DataSource{
    boolean stopped=false;
    URL source;
    GMLReader gmlr;
    String[] columnNames = new String[0];
    /** Creates a new instance of GMLReader */
    public GMLDataSource(URL src) {
        source=src;
    }
    
    /** gets the Column names (used by FeatureTable) for this DataSource
     */
    public String[] getColumnNames() {
        if(gmlr != null){
    
        }
        return null;
    }
    
    /** Loads Feature rows for the given Extent from the datasource
     */
    public List load(Extent ex) throws DataSourceException {
       
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(source.openStream()));

            gmlr = new GMLReader(in);
            GeometryCollection gc = gmlr.read();
            return new ArrayList();
        }catch(IOException e){
            throw new DataSourceException("exception in GMLDataSource.load: "+e);
        }
        
    }
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        stopped=true;
        gmlr.stopLoading();
    }
    
    /** Saves the given features to the datasource
     */
    public void save(List features) throws DataSourceException {
    }
    
}
