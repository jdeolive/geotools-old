package org.geotools.data.property;

import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;

import com.vividsolutions.jts.geom.Envelope;

public class PropertyFeatureSource extends AbstractFeatureLocking {
    String typeName;
    FeatureType featureType;
    PropertyDataStore store;
    
    long cacheTimestamp = 0;
    Envelope cacheBounds = null;
    int cacheCount = -1;
    
    PropertyFeatureSource( PropertyDataStore propertyDataStore, String typeName ) throws IOException{
        this.store = propertyDataStore;
        this.typeName = typeName;
        this.featureType = store.getSchema( typeName );
        store.listenerManager.addFeatureListener( this, new FeatureListener(){
            public void changed(FeatureEvent featureEvent) {
                if( cacheBounds != null ){
                    if( featureEvent.getEventType() == FeatureEvent.FEATURES_ADDED ){
                        cacheBounds.expandToInclude( featureEvent.getBounds() );
                    }
                    else {
                        cacheBounds = null;                                            
                    }                
                }
                cacheCount = -1;
            }
        });        
    }
    public DataStore getDataStore() {
        return store;
    }

    public void addFeatureListener(FeatureListener listener) {
        store.listenerManager.addFeatureListener(this, listener);
    }

    public void removeFeatureListener(
        FeatureListener listener) {
        store.listenerManager.removeFeatureListener(this, listener);
    }

    public FeatureType getSchema() {
        return featureType;
    }
    
    public int getCount(Query query) {
        if( query == Query.ALL && getTransaction() == Transaction.AUTO_COMMIT ){
            File file = new File( store.directory, typeName+".properties" );            
            if( cacheCount != -1 && file.lastModified() == cacheTimestamp){
                return cacheCount;
            }
            cacheCount = countFile( file );
            cacheTimestamp = file.lastModified();
            return cacheCount;
        }
        return -1;
    }
    private int countFile(File file){
        try {
            LineNumberReader reader = new LineNumberReader( new FileReader( file ) );
            while( reader.readLine() != null);                    
            return reader.getLineNumber() -1;   
        }
        catch( IOException e){
            return -1;
        }
    }
    public Envelope getBounds() {
        File file = new File( store.directory, typeName+".properties" );                
        if( cacheBounds != null && file.lastModified() == cacheTimestamp ){            
            // we have the cache
            return cacheBounds;
        }
        try {
            // calculate and store in cache                    
            cacheBounds = getFeatures().getBounds();
            cacheTimestamp = file.lastModified();            
            return cacheBounds;
        } catch (IOException e) {            
        }
        // bounds are unavailable!
        return null;
    }

}
