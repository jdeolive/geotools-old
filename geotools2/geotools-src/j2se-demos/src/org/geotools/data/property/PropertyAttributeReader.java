package org.geotools.data.property;

import java.io.IOException;

import java.io.*;
import java.util.NoSuchElementException;

import org.geotools.data.*;
import org.geotools.feature.*;
/**
 * Simple AttributeReader that works against Java properties files.
 * <p>
 * This AttributeReader is part of the geotools2 DataStore tutorial, and
 * should be considered a Toy.
 * </p>
 * <p>
 * The content of this file should start with a the property "_" with the
 * value being the typeSpec describing the featureType. Thereafter each line
 * will should have a FeatureID as the property and the attribtues
 * as the value separated by | characters.
 * </p> 
 * <pre><code>
 * _=id:Integer|name:String|geom:Geometry
 * fid1=1|Jody|<i>well known text</i>
 * fid2=2|Brent|<i>well known text</i>
 * fid3=3|Dave|<i>well known text</i>
 * </code></pre>
 * @author jgarnett
 */
public class PropertyAttributeReader implements AttributeReader {
    BufferedReader reader;
    FeatureType type;
    String line;
    String next;
    
    public PropertyAttributeReader( File file ) throws IOException {
        String typeName = typeName( file );
        String namespace = namespace( file );    
        reader = new BufferedReader( new FileReader( file )  );
        line = reader.readLine();                        
        if( line == null || !line.startsWith("_=")){
            throw new IOException( typeName+" schema not available" );            
        }
        String typeSpec = line.substring(2);
        try {            
            type = DataUtilities.createType( namespace+typeName, typeSpec );
            
        } catch (SchemaException e) {
            throw new DataSourceException( typeName+" schema not available", e );
        }        
        line = null;
        next = null;        
    }
    private static String typeName( File file ){
        String name = file.getName();
        int split = name.lastIndexOf('.');
        return split == -1 ? name : name.substring(0, split );        
    }
    private static String namespace( File file ){
        File parent = file.getParentFile();
        return parent == null ? "" : parent.getName()+".";
    }
    public int getAttributeCount() {
        return type.getAttributeCount();
    }        
    public AttributeType getAttributeType(int index)
        throws ArrayIndexOutOfBoundsException {
        return type.getAttributeType( index );
        
    }
    public void close() throws IOException {
        reader.close();
        reader = null;
    }
    public boolean hasNext() throws IOException {        
        if( next != null){
            return true;
        }
        next = reader.readLine();
        return next != null;
    }
    public void next() throws IOException {
        if( hasNext() ){
            line = next;
            next = null;
        }
        else {
            throw new NoSuchElementException();        
        }
    }
    public String getFeatureID(){
        if( line == null ){
            return null;
        }
        int split = line.indexOf('=');
        if( split == -1){
            return null;
        }
        return line.substring( 0, split );        
    }
    public Object read(int index)
        throws IOException, ArrayIndexOutOfBoundsException {
        if( line == null ){
            throw new IOException( "No content available - did you remeber to call next?" );            
        }
        int split = line.indexOf('=');
        String fid = line.substring( 0, split );
        String text[] = line.substring( split+1 ).split("\\|");
        return type.getAttributeType( index ).parse( text[ index ] );
    }
}