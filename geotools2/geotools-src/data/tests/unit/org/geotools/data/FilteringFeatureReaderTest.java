/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;

/**
 * Test FilteredFeatureReader for conformance.
 * 
 * @author Jody Garnett, Refractions Research
 */
public class FilteringFeatureReaderTest extends DataTestCase {
    FeatureReader roadReader;
    FeatureReader riverReader;
    /**
     * Constructor for FilteringFeatureReaderTest.
     * @param arg0
     */
    public FilteringFeatureReaderTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        roadReader = DataUtilities.reader( roadFeatures );
        riverReader = DataUtilities.reader( riverFeatures );        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        roadReader.close();
        roadReader = null;
        riverReader.close();
        riverReader = null;
    }

    public void testFilteringFeatureReaderALL() throws IOException {
        FeatureReader reader;
        
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.ALL );
        try {
            assertFalse( reader.hasNext() );
        }
        finally {
            reader.close();
        }        
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.ALL );
        assertEquals( 0, count( reader ));
        
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.ALL );
        assertContents( new Feature[0], reader );                                                           
    }
    public void testFilteringFeatureReaderNONE() throws IOException {
        FeatureReader reader;        
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.NONE );
        try {
            assertTrue( reader.hasNext() );
        }
        finally {
            reader.close();
        }
        reader = DataUtilities.reader( roadFeatures );
        assertEquals( roadFeatures.length, count( reader ));
                
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.NONE );
        assertEquals( roadFeatures.length, count( reader ));
        
        reader = new FilteringFeatureReader(DataUtilities.reader( roadFeatures ), Filter.NONE );
        assertContents( roadFeatures, reader );                                            
    }
    void assertContents( Feature expected[], FeatureReader reader ) throws IOException {
        assertNotNull( reader );
        assertNotNull( expected );
        Feature feature;
        int count = 0;
        try {
            for( int i=0; i<expected.length;i++){
                assertTrue( reader.hasNext() );
                feature = reader.next();
                assertNotNull( feature );
                assertEquals( expected[i], feature );
                count++;
            }
            assertFalse( reader.hasNext() );
        } catch (NoSuchElementException e) {
            // bad dog!
            throw new DataSourceException("hasNext() lied to me at:"+count, e );
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("next() could not understand feature at:"+count, e );
        }        
        finally {
            reader.close();
        }                
    }
    public void testNext() {
    }

    public void testClose() {
    }

    public void testGetFeatureType() {
    }

    public void testHasNext() {
    }

}
