package org.geotools.data.property;

import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;

public class PropertyFeatureSource extends AbstractFeatureLocking {
    String typeName;
    FeatureType featureType;
    PropertyDataStore store;
    
    PropertyFeatureSource( PropertyDataStore propertyDataStore, String typeName ) throws IOException{
        this.store = propertyDataStore;
        this.typeName = typeName;
        this.featureType = store.getSchema( typeName );
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
            return countFile( file );
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
}
