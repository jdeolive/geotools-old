/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
/*
 * MapLayerTest.java
 *
 * Created on 29 novembre 2003, 11.24
 */
package org.geotools.map;

import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListener;



/**
 * Unit tests for the DefaultMapLayer class
 *
 * @author wolf
 */
public class MapLayerTest extends TestCase {
    private ArrayList events = null;
    private int layerChangedCount;
    private int layerShownCount;
    private int layerHiddenCount;
    private MapLayerListener layerListener = new MapLayerListener() {
            public void layerChanged(MapLayerEvent event) {
                layerChangedCount++;
                events.add(event);
            }

            public void layerShown(MapLayerEvent event) {
                layerShownCount++;
                events.add(event);
            }

            public void layerHidden(MapLayerEvent event) {
                layerHiddenCount++;
                events.add(event);
            }
        };

    /** Test suite for this test case */
    private TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public MapLayerTest(String testName) {
        super(testName);
    }

    /**
     * Main for test runner.
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Required suite builder.
     *
     * @return A test suite for this unit test.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(MapLayerTest.class);

        return suite;
    }

    public void setUp() {
        clearEventTracking();
    }
    
    private void clearEventTracking() {
        events = new ArrayList(0);
        layerChangedCount = 0;
        layerShownCount = 0;
        layerHiddenCount = 0;
    }
    
    public void testNullConstructor() {
        boolean exceptionThrown = false;
        try {
            MapLayer layer = new DefaultMapLayer((FeatureSource) null, null, null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        
        assertTrue("Null exception should be thrown with null arguments", exceptionThrown);
        
        exceptionThrown = false;
        try {
            MapLayer layer = new DefaultMapLayer((FeatureSource) null, null, null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        
        assertTrue("Null exception should be thrown with null arguments", exceptionThrown);
    }
    
    public void testNullSets() throws Exception {
        DefaultMapLayer layer = TestUtils.buildLayer(0, "style", "");
        
        boolean exceptionThrown = false;
        try {
            layer.setStyle(null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        
        assertTrue("Null exception should be thrown with null arguments", exceptionThrown);
        
        exceptionThrown = false;
        try {
            layer.setTitle(null);
        } catch (NullPointerException npe) {
            exceptionThrown = true;
        }
        
        assertTrue("Null exception should be thrown with null arguments", exceptionThrown);
    }
    
    public void testEvents() throws Exception {
        FeatureCollection fc = TestUtils.buildFeatureCollection(10);
        DefaultMapLayer layer = new DefaultMapLayer(DataUtilities.source(fc), TestUtils.buildStyle("style"), "");
        
        layer.addMapLayerListener(layerListener);
        
        String newTitle = "Title";
        layer.setTitle(newTitle);
        assertEquals(1, layerChangedCount);
        assertEquals(0, layerHiddenCount);
        assertEquals(0, layerShownCount);
        assertEquals(newTitle, layer.getTitle());
        MapLayerEvent event = (MapLayerEvent) events.get(0);
        assertEquals(MapLayerEvent.METADATA_CHANGED, event.getReason());
        
        clearEventTracking();
        
        layer.setVisible(false);
        assertEquals(0, layerChangedCount);
        assertEquals(1, layerHiddenCount);
        assertEquals(0, layerShownCount);
        assertEquals(false, layer.isVisible());
        event = (MapLayerEvent) events.get(0);
        assertEquals(MapLayerEvent.VISIBILITY_CHANGED, event.getReason());
        
        clearEventTracking();
        
        layer.setVisible(true);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerHiddenCount);
        assertEquals(1, layerShownCount);
        assertEquals(true, layer.isVisible());
        event = (MapLayerEvent) events.get(0);
        assertEquals(MapLayerEvent.VISIBILITY_CHANGED, event.getReason());
        
        clearEventTracking();
        
        fc.addAll(TestUtils.buildFeatureCollection(20));
        assertEquals(1, layerChangedCount);
        assertEquals(0, layerHiddenCount);
        assertEquals(0, layerShownCount);
        event = (MapLayerEvent) events.get(0);
        assertEquals(MapLayerEvent.DATA_CHANGED, event.getReason());
        
        clearEventTracking();
        
        layer.setStyle(TestUtils.buildStyle("style2"));
        assertEquals(1, layerChangedCount);
        assertEquals(0, layerHiddenCount);
        assertEquals(0, layerShownCount);
        event = (MapLayerEvent) events.get(0);
        assertEquals(MapLayerEvent.STYLE_CHANGED, event.getReason());
    }   
    
    

}
