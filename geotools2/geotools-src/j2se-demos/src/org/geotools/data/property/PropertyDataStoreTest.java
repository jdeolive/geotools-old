package org.geotools.data.property;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;

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
        assertEquals( "road", type.getTypeName() );
        assertEquals( "propertyTestData", type.getNamespace() );
        assertEquals( 2, type.getAttributeCount() );
        
        AttributeType id = type.getAttributeType(0);        
        AttributeType name = type.getAttributeType(1);
        
        assertEquals( "id", id.getName() );
        assertEquals( "class java.lang.Integer", id.getType().toString() );
                
        assertEquals( "name", name.getName() );
        assertEquals( "class java.lang.String", name.getType().toString() );                        
    }
    public void testGetFeaturesFeatureTypeFilterTransaction1() throws Exception {
        FeatureType type = store.getSchema( "road" );
        Query roadQuery = new DefaultQuery("road");
        FeatureReader reader = store.getFeatureReader( roadQuery, Transaction.AUTO_COMMIT );
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
        
        Filter filter;
        
        filter = FilterFactory.createFilterFactory().createFidFilter("fid1");
        reader = store.getFeatureReader( new DefaultQuery("road", filter ), Transaction.AUTO_COMMIT );
        assertEquals( 1, count( reader ) );
        
        Transaction transaction = new DefaultTransaction();
        reader = store.getFeatureReader( roadQuery, transaction );
        assertEquals( 4, count( reader ));
        
        reader = store.getFeatureReader( roadQuery, transaction );
        List list = new ArrayList();
        try {
            while( reader.hasNext() ){
                list.add( reader.next().getID() );
            }
        }
        finally {
            reader.close();
        }
        assertEquals( "[fid1, fid2, fid3, fid4]", list.toString() );        
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
    private int count( FeatureReader reader ) throws Exception {
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
    private int count( String typeName ) throws Exception {
        return count( store.getFeatureReader( typeName ) );                
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
    public void testGetFeatureSource() throws Exception {
        FeatureSource road = store.getFeatureSource( "road" );
        FeatureResults features = road.getFeatures();
        FeatureReader reader = features.reader();
        List list = new ArrayList();
        try {
            while( reader.hasNext() ){
                list.add( reader.next().getID() );                
            }
        } finally {
            reader.close();
        }
        assertEquals( "[fid1, fid2, fid3, fid4]", list.toString() );
        assertEquals( 4, road.getCount(Query.ALL) );
        assertEquals( null, road.getBounds(Query.ALL) );
        assertEquals( 4, features.getCount() );
        assertTrue( features.getBounds().isNull() );
        assertEquals( 4, features.collection().size() );
                
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

    public void testTransaction() throws Exception {
        Transaction t1 = new DefaultTransaction();
        Transaction t2 = new DefaultTransaction();
    
        FeatureType type = store.getSchema( "road" );
        FeatureStore road = (FeatureStore) store.getFeatureSource("road");
        FeatureStore road1 = (FeatureStore) store.getFeatureSource("road");
        FeatureStore road2 = (FeatureStore) store.getFeatureSource("road");
    
        road1.setTransaction( t1 );
        road2.setTransaction( t2 );

        Filter filter1 = FilterFactory.createFilterFactory().createFidFilter("fid1");
        Filter filter2 = FilterFactory.createFilterFactory().createFidFilter("fid2");        
        
        Feature feature =
            type.create( new Object[]{ new Integer(5), "chris"}, "fid5" );
            
        assertEquals( 4, road.getFeatures().getCount() );
        assertEquals( 4, road1.getFeatures().getCount() );
        assertEquals( 4, road2.getFeatures().getCount() );
                
        road1.removeFeatures( filter1 ); // road1 removes fid1 on t1
        assertEquals( 4, road.getFeatures().getCount() );
        assertEquals( 3, road1.getFeatures().getCount() );
        assertEquals( 4, road2.getFeatures().getCount() );               
        
        FeatureReader reader = DataUtilities.reader( new Feature[]{ feature, });
        road2.addFeatures( reader ); // road2 adds fid5 on t2
    
        assertEquals( 4, road.getFeatures().getCount() );
        assertEquals( 3, road1.getFeatures().getCount() );
        assertEquals( 5, road2.getFeatures().getCount() );        
            
        t1.commit();
        assertEquals( 3, road.getFeatures().getCount() );
        assertEquals( 3, road1.getFeatures().getCount() );
        assertEquals( 4, road2.getFeatures().getCount() );                
            
        t2.commit();
        assertEquals( 4, road.getFeatures().getCount() );
        assertEquals( 4, road1.getFeatures().getCount() );
        assertEquals( 4, road2.getFeatures().getCount() );
    }
}
