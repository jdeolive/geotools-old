package org.geotools.data.property;

import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;

public class PropertyDataStore extends AbstractDataStore {
    protected File directory;
    public PropertyDataStore(File dir) {
        if( !dir.isDirectory()){
            throw new IllegalArgumentException( dir +" is not a directory");
        }
        directory = dir;
    }
    public String[] getTypeNames() {
        FilenameFilter f;
        String list[] = directory.list( new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties");
            }
        });
        for( int i=0; i<list.length;i++){
            list[i] = list[i].substring(0, list[i].lastIndexOf('.'));
        }
        return list;
    }

    public FeatureType getSchema(String typeName) throws IOException {
        String typeSpec = property( typeName, "_");
        try {
            String namespace = directory.getName();
            return DataUtilities.createType( namespace+"."+typeName,typeSpec );
        } catch (SchemaException e) {
            e.printStackTrace();
            throw new DataSourceException( typeName+" schema not available", e);
        }
    }

    private String property( String typeName, String key ) throws IOException {
        File file = new File( directory, typeName+".properties");
        BufferedReader reader = new BufferedReader( new FileReader( file ) );
        try {        
            for( String line = reader.readLine(); line != null; line = reader.readLine()){
                if( line.startsWith( key+"=" )){
                    return line.substring( key.length()+1 );
                }
            }
        }
        finally {
            reader.close();            
        }        
        return null;        
    }
    protected FeatureReader getFeatureReader(String typeName) throws IOException {
        return new PropertyFeatureReader( directory, typeName );        
    }
    protected FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return new PropertyFeatureWriter( this, typeName );
    }
    public void createSchema(FeatureType featureType) throws IOException {
        String typeName = featureType.getTypeName();
        File file = new File( directory, typeName+".properties");
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write("_=");
        writer.write( DataUtilities.spec( featureType ) );
        writer.close();
    }
    //
    // Count Optimization
    //
    public FeatureSource getFeatureSource(final String typeName) throws IOException {
        final FeatureType featureType = getSchema(typeName);        
        return new AbstractFeatureLocking() {            
            public DataStore getDataStore() {
                return PropertyDataStore.this;
            }
    
            public void addFeatureListener(FeatureListener listener) {
                listenerManager.addFeatureListener(this, listener);
            }
    
            public void removeFeatureListener(
                FeatureListener listener) {
                listenerManager.removeFeatureListener(this, listener);
            }
    
            public FeatureType getSchema() {
                return featureType;
            }
            public int getCount(Query query) {
                if( query == Query.ALL && getTransaction() == Transaction.AUTO_COMMIT ){
                    return countFile( new File( directory, typeName+".properties") );
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
        };
    }        
}
