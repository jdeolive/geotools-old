package org.geotools.data.property;

import java.io.*;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

import junit.framework.TestCase;

/**
 * @author jgarnett
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PropertyDataStoreTest extends TestCase {
    PropertyDataStore store;
    /**
     * Constructor for SimpleDataStoreTest.
     * @param arg0
     */
    public PropertyDataStoreTest(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {
        File dir = new File(".", "propertyTestData" );
        dir.mkdir();
               
        File file = new File( dir ,"road.properties");
        if( file.exists()){
            file.delete();
        }        
        BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
        writer.write("_=id:Integer,name:String"); writer.newLine();
        writer.write("fid1=1|jody"); writer.newLine();
        writer.write("fid2=2|brent"); writer.newLine();
        writer.write("fid3=3|dave"); writer.newLine();
        writer.write("fid4=4|justin");
        writer.close();
        store = new PropertyDataStore( dir );
        super.setUp();
    }
    protected void tearDown() throws Exception {
        File dir = new File( "propertyTestData" );
        File list[]=dir.listFiles();
        for( int i=0; i<list.length;i++){
            list[i].delete();
        }
        dir.delete();
        super.tearDown();                
    }

    public void testGetTypeNames() {
        String names[] = store.getTypeNames();
        assertEquals( 1, names.length );
        assertEquals( "road", names[0] );                
    }

    public void testGetSchema() throws IOException {
        FeatureType type = store.getSchema( "road" );
        assertNotNull( type );
    }

    /*
     * Test for FeatureReader getFeatureReader(String)
     */
    public void testGetFeatureReaderString() throws NoSuchElementException, IOException, IllegalAttributeException {
        FeatureReader reader = store.getFeatureReader("road");
        int count = 0;
        try {
            while( reader.hasNext() ){
                reader.next();                
                count++;
            }
        }
        finally {
            reader.close();
        }
        assertEquals( 4, count );
    }
    private int count( String typeName ) throws Exception {
        FeatureReader reader = store.getFeatureReader( typeName );
        int count = 0;
        try {
            while( reader.hasNext() ){
                reader.next();
                count++;
            }
        }
        finally {
            reader.close();
        }
        return count;        
    }
    
    public void testWriterSkipThrough() throws Exception {
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
            store.getFeatureWriter("road");
            
        File in = writer.read;
        File out = writer.write;
        
        int count = 0;
        while( writer.hasNext() ){
            writer.next();
            count++;
        }
        assertEquals( 4, count );
        assertTrue( in.exists() );
        assertTrue( out.exists() );
        writer.close();
        assertTrue( in.exists() );        
        assertFalse( out.exists() );
        
        assertEquals( 4, count( "road" ) );
    }
    public void testWriterChangeName() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
            store.getFeatureWriter("road");
            
        int count = 0;
        while( writer.hasNext() ){
            Feature f = writer.next();
            f.setAttribute(1,"name "+(count+1));
            writer.write();
            count++;
        }                
        writer.close();        
        assertEquals( 4, count );
        assertEquals( 4, count( "road" ));                
    }
    public void testWriterChangeFirstName() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        f = writer.next();
        f.setAttribute(1,"changed");
        writer.write();
        writer.close();                           
        assertEquals( 4, count( "road" ));    
    }
    public void testWriterChangeLastName() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.next();
        writer.next();        
        f = writer.next();
        f.setAttribute(1,"changed");
        writer.write();
        writer.close();                           
        assertEquals( 4, count( "road" ));    
    }    
    public void testWriterChangeAppend() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.next();
        writer.next();
        writer.next();
        assertFalse( writer.hasNext() );
        f = writer.next();
        assertNotNull( f );
        f.setAttribute(0,new Integer(-1));        
        f.setAttribute(1,"new");
        writer.write();
        writer.close();
        assertEquals( 5, count( "road" ));    
    }
    public void testWriterChangeRemoveFirst() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.remove();
        writer.close();
        assertEquals( 3, count( "road" ));    
    }
    public void testWriterChangeRemoveLast() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.next();
        writer.next();
        writer.remove();
        writer.close();
        assertEquals( 3, count( "road" ));    
    }
    public void testWriterChangeRemoveAppend() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.next();
        writer.next();
        writer.next();        
                
        assertFalse( writer.hasNext() );
        f = writer.next();
        assertNotNull( f );
        f.setAttribute(0,new Integer(-1));        
        f.setAttribute(1,"new");
        writer.remove();
        writer.close();
        assertEquals( 4, count( "road" ));                    
    }
    public void testWriterChangeIgnoreAppend() throws Exception{
        PropertyFeatureWriter writer = (PropertyFeatureWriter)
                    store.getFeatureWriter("road");
        Feature f;
        writer.next();
        writer.next();
        writer.next();
        writer.next();
        assertFalse( writer.hasNext() );
        f = writer.next();
        assertNotNull( f );
        f.setAttribute(0,new Integer(-1));        
        f.setAttribute(1,"new");        
        writer.close();
        assertEquals( 4, count( "road" ));                    
    }
    private void dir( File file ){
        File dir;
        if( file.isDirectory() ){
            dir = file;
        }
        else{
            dir = file.getParentFile();
        }
        if( dir != null){
            String ls[] = dir.list();
            System.out.println( "Directory "+dir );
            for( int i=0; i<ls.length;i++){
                System.out.println( ls[i] );
            }
        }
    }
            
}
