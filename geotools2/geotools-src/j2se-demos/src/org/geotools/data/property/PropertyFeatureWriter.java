package org.geotools.data.property;

import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;

public class PropertyFeatureWriter implements FeatureWriter {
    File read;
    PropertyAttributeReader reader;
    
    File write;
    PropertyAttributeWriter writer;
    
    Feature feature = null;
    Feature live = null;        
    public PropertyFeatureWriter( File directory, String typeName ) throws IOException {
        read = new File( directory, typeName+".properties");
        write = File.createTempFile( typeName+System.currentTimeMillis(), null, directory );        
                
        reader = new PropertyAttributeReader( read );
        writer = new PropertyAttributeWriter( write, reader.type );
    }
    public FeatureType getFeatureType() {
        return reader.type;
    }
    public Feature next() throws IOException {
        if( writer == null ) {
            throw new IOException( "Writer has been closed" );
        }
        String fid = null;        
        try {
            if( hasNext() ){
                reader.next(); // grab next line
                
                FeatureType type = reader.type;
                
                fid = reader.getFeatureID();
                Object values[] = new Object[ reader.getAttributeCount() ];
                for( int i=0; i< reader.getAttributeCount(); i++){
                    values[i]=reader.read( i );
                }
                            
                feature = type.create( values, fid );
                live = type.duplicate( feature );
                return live;
            }
            else {
                FeatureType type = reader.type;
                
                fid = type.getTypeName()+"."+System.currentTimeMillis();
                Object values[] = DataUtilities.defaultValues( type );

                feature = null;                                            
                live = type.create( values, fid );
                return live;    
            }                    
        } catch (IllegalAttributeException e) {
            String message = "Problem creating feature "+(fid != null ? fid : "");
            throw new DataSourceException( message, e );
        }
    }
    public boolean hasNext() throws IOException {
        if( writer == null) {
            throw new IOException( "Writer has been closed" );
        }
        if( live != null && feature != null ){
            // we have returned something to the user,
            // and it has not been writen out or removed
            //
            // We don't bother checking content, as user would call
            // write if they wanted their changes recorded
            writer.next();
            writer.echoLine( reader.getLine() ); // echo unchanged                        
            feature = null;
            live = null;
        }
        return reader.hasNext();
    }
    
    public void close() throws IOException {
        if( writer == null ){
            throw new IOException( "writer already closed");            
        }
        // write out remaining contents from reader
        // if applicable
        while( reader.hasNext() ){
            reader.next(); // advance
            writer.next();             
            writer.echoLine( reader.getLine() ); // echo unchanged                        
        }
        writer.close();
        reader.close();        
        writer = null;
        reader = null;        
        read.delete();
        write.renameTo( read );
        read = null;
        write = null;                
    }    
    public void remove() throws IOException {
        if( live == null){
            throw new IOException( "No current feature to remove");
        }
        feature = null; 
        live = null; // prevent live and remove from being written out       
    }
    public void write() throws IOException {
        if( live == null){
            throw new IOException( "No current feature to write");            
        }
        if( live.equals( feature )){
            // no change - just echo line
            writer.next();
            writer.echoLine( reader.getLine() );
        }
        else {
            writer.next();
            writer.writeFeatureID( live.getID() );        
            for( int i=0; i<live.getNumberOfAttributes(); i++){
                writer.write( i, live.getAttribute( i ));
            }
        }
        feature = null;
        live = null;
    }    
}
