package org.geotools.data.shapefile;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;

import org.geotools.data.ServiceInfo;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.feature.FeatureTypes;

/**
 * ServiceInfo for ShapefileDataStore.
 * 
 * @author Jody Garnett (Refractions Reserach)
 */
public class ShapefileURLServiceInfo implements ServiceInfo {
    private final ShapefileDataStore shapefile;
    
    ShapefileURLServiceInfo(ShapefileDataStore shapefile) {
        this. shapefile = shapefile;
    }

    public URI getSchema() {
        return FeatureTypes.DEFAULT_NAMESPACE;
    }
    
    public Icon getIcon() {
        return null; // talk to Eclesia there is something in render
    }
    
    public URI getPublisher() {        
        return null; // current user? last person to modify the file
    }
    public String getDescription() {
        StringBuffer buf = new StringBuffer();
        buf.append( shapefile.getCurrentTypeName() );
        buf.append( " non local shapefile." );
        
        return buf.toString();
    }

    public String getTitle() {
        return shapefile.getCurrentTypeName();
    }
    
    public URI getSource() {
        String url = shapefile.shpFiles.get( ShpFileType.SHP );
        try {
            return new URI( url );
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public Set<String> getKeywords() {
        Set<String> words = new HashSet<String>();
        words.add( shapefile.getCurrentTypeName() );
        words.add( "shp" );
        words.add( "dbf" );
        words.add( "shx" );
        if( shapefile instanceof IndexedShapefileDataStore ){
            IndexedShapefileDataStore indexed = (IndexedShapefileDataStore) shapefile;
            if( indexed.indexUseable( ShpFileType.QIX ) ){
                words.add( "qix" );        
            }            
        }
        words.add( "shapefile" );
        
        return words;
    }
}
