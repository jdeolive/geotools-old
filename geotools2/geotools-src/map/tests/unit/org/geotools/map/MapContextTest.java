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
 * MapContextTest.java
 *
 * Created on 29 novembre 2003, 11.24
 */
package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.LocalCoordinateSystem;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class MapContextTest extends TestCase {
    private ArrayList events = null;
    private int boundsChangedCount;
    private int layerAddedCount;
    private int layerRemovedCount;
    private int layerChangedCount;
    private int layerMovedCount;
    private int propertyChangeCount;
    private MapBoundsListener boundsListener = new MapBoundsListener() {
            public void mapBoundsChanged(MapBoundsEvent event) {
                events.add(event);
                boundsChangedCount++;
            }
        };

    private MapLayerListListener layerListListener = new MapLayerListListener() {
            public void layerAdded(MapLayerListEvent event) {
                events.add(event);
                layerAddedCount++;
            }

            public void layerRemoved(MapLayerListEvent event) {
                events.add(event);
                layerRemovedCount++;
            }

            public void layerChanged(MapLayerListEvent event) {
                events.add(event);
                layerChangedCount++;
            }

            public void layerMoved(MapLayerListEvent event) {
                events.add(event);
                layerMovedCount++;
            }
        };

    private PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                events.add(evt);
                propertyChangeCount++;
            }
        };

    /** Test suite for this test case */
    private TestSuite suite = null;

    /**
     * Constructor with test name.
     *
     * @param testName DOCUMENT ME!
     */
    public MapContextTest(String testName) {
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
        TestSuite suite = new TestSuite(MapContextTest.class);

        return suite;
    }

    public void setUp() {
        clearEventTracking();
    }

    private void clearEventTracking() {
        events = new ArrayList(0);
        boundsChangedCount = 0;
        layerAddedCount = 0;
        layerRemovedCount = 0;
        layerChangedCount = 0;
        layerMovedCount = 0;
        propertyChangeCount = 0;
    }

    private DefaultMapContext buildMapContext() {
        DefaultMapContext context = new DefaultMapContext();
        context.addMapBoundsListener(boundsListener);
        context.addMapLayerListListener(layerListListener);
        context.addPropertyChangeListener(propertyChangeListener);

        return context;
    }

    public void testEmptyContext() {
        DefaultMapContext context = buildMapContext();

        assertEquals("", context.getTitle());
        assertEquals("", context.getContactInformation());
        assertEquals("", context.getAbstract());
        assertTrue(context.getKeywords().length == 0);
        assertEquals(0, context.getLayerCount());
        assertNull(context.getAreaOfInterest());
        assertEquals(LocalCoordinateSystem.PROMISCUOUS, context.getCoordinateReferenceSystem());
    }

    public void testLayerListEvents() throws Exception {
        DefaultMapContext context = buildMapContext();
        MapLayer layer1 = TestUtils.buildLayer(0.0, "style", "layer1");

        context.addLayer(layer1);
        assertEquals(1, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(0, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(0, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer1, ((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();

        FeatureCollection fc2 = TestUtils.buildFeatureCollection(10.0);
        context.addLayer(DataUtilities.source(fc2), TestUtils.buildStyle("style2"));

        DefaultMapLayer layer2 = (DefaultMapLayer) context.getLayer(1);
        assertEquals(2, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();

        FeatureCollection fc3 = TestUtils.buildFeatureCollection(20.0);
        context.addLayer(DataUtilities.source(fc3), TestUtils.buildStyle("style2"));

        DefaultMapLayer layer3 = (DefaultMapLayer) context.getLayer(2);
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer3, ((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();

        context.removeLayer(layer3);
        assertEquals(2, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(1, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer3, ((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();
        context.clearLayerList();
        assertEquals(0, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(1, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(0, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertNull(((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();
        context.addLayers(new MapLayer[] { layer1, layer2 });
        assertEquals(2, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(0, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertNull(((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();
        context.addLayers(new MapLayer[] { layer3 });
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer3, ((MapLayerListEvent) events.get(0)).getLayer());

        clearEventTracking();
        assertFalse(context.addLayer(layer2));
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);

        clearEventTracking();
        context.removeLayer(1);
        assertEquals(1, context.addLayers(new MapLayer[] { layer2, layer3 }));
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(1, layerAddedCount);
        assertEquals(1, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(2, ((MapLayerListEvent) events.get(1)).getFromIndex());
        assertEquals(2, ((MapLayerListEvent) events.get(1)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(1)).getLayer());

        try {
            context.removeLayer(0);
            context.addLayer(5, layer1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            context.removeLayer(5);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        context.clearLayerList();
        context.addLayers(new MapLayer[] { layer1, layer2, layer3 });
        clearEventTracking();
        context.moveLayer(0, 2);
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(1, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(0, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(2, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer1, ((MapLayerListEvent) events.get(0)).getLayer());
        assertEquals(layer2, context.getLayer(0));
        assertEquals(layer3, context.getLayer(1));
        assertEquals(layer1, context.getLayer(2));

        try {
            context.moveLayer(0, 10);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }

        context.clearLayerList();
        context.addLayers(new MapLayer[] { layer1, layer2, layer3 });
        clearEventTracking();
        layer2.setStyle(TestUtils.buildStyle("coolStyle"));
        layer2.setTitle("newTitle");
        layer2.setVisible(false);
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(3, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(0)).getLayer());
        assertEquals(MapLayerEvent.STYLE_CHANGED,
            ((MapLayerListEvent) events.get(0)).getMapLayerEvent().getReason());
        assertEquals(1, ((MapLayerListEvent) events.get(1)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(1)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(1)).getLayer());
        assertEquals(MapLayerEvent.METADATA_CHANGED,
            ((MapLayerListEvent) events.get(1)).getMapLayerEvent().getReason());
        assertEquals(1, ((MapLayerListEvent) events.get(2)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(2)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(2)).getLayer());
        assertEquals(MapLayerEvent.VISIBILITY_CHANGED,
            ((MapLayerListEvent) events.get(2)).getMapLayerEvent().getReason());

        clearEventTracking();
        fc2.addAll(TestUtils.buildFeatureCollection(50.0));
        assertEquals(3, context.getLayerCount());
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(1, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getFromIndex());
        assertEquals(1, ((MapLayerListEvent) events.get(0)).getToIndex());
        assertEquals(layer2, ((MapLayerListEvent) events.get(0)).getLayer());
        assertEquals(MapLayerEvent.DATA_CHANGED,
            ((MapLayerListEvent) events.get(0)).getMapLayerEvent().getReason());
    }

    public void testOtherProperties() {
        DefaultMapContext context = new DefaultMapContext(new MapLayer[0], "oldTitle",
                "oldAbstract", "oldCi", new String[] { "oldKeyword" });
        context.addMapBoundsListener(boundsListener);
        context.addMapLayerListListener(layerListListener);
        context.addPropertyChangeListener(propertyChangeListener);

        // test change events
        clearEventTracking();
        context.setTitle("newTitle");
        context.setAbstract("newAbstract");
        context.setContactInformation("newCi");
        context.setKeywords(new String[] { "newKeyword1", "newKeyword2" });
        assertEquals(0, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(4, propertyChangeCount);

        PropertyChangeEvent changeEvent = (PropertyChangeEvent) events.get(0);
        assertEquals("title", changeEvent.getPropertyName());
        assertEquals("oldTitle", changeEvent.getOldValue());
        assertEquals("newTitle", changeEvent.getNewValue());
        changeEvent = (PropertyChangeEvent) events.get(1);
        assertEquals("abstract", changeEvent.getPropertyName());
        assertEquals("oldAbstract", changeEvent.getOldValue());
        assertEquals("newAbstract", changeEvent.getNewValue());
        changeEvent = (PropertyChangeEvent) events.get(2);
        assertEquals("contactInformation", changeEvent.getPropertyName());
        assertEquals("oldCi", changeEvent.getOldValue());
        assertEquals("newCi", changeEvent.getNewValue());
        changeEvent = (PropertyChangeEvent) events.get(3);
        assertEquals("keywords", changeEvent.getPropertyName());
        assertEquals(1, ((String[]) changeEvent.getOldValue()).length);
        assertEquals(2, ((String[]) changeEvent.getNewValue()).length);

        try {
            context.setTitle(null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            context.setAbstract(null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            context.setContactInformation(null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            context.setKeywords(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testBoundsAndCRS() {
        DefaultMapContext context = buildMapContext();

        // test for null assignement
        try {
            context.setAreaOfInterest(null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            context.setAreaOfInterest(null, null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            context.setAreaOfInterest(new Envelope(0, 10, 0, 10), null);
            fail();
        } catch (NullPointerException e) {
        }

        // test events for simple area set
        clearEventTracking();

        Envelope env1 = new Envelope(0, 10, 0, 10);
        context.setAreaOfInterest(env1);
        assertEquals(1, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);

        MapBoundsEvent event = (MapBoundsEvent) events.get(0);
        assertNull(event.getOldAreaOfInterest());
        assertEquals(env1, context.getAreaOfInterest());
        assertEquals(event.getOldCoordinateReferenceSystem(), context.getCoordinateReferenceSystem());
        assertEquals(MapBoundsEvent.AREA_OF_INTEREST_MASK, event.getType());

        // test events for both area and coordinate system
        clearEventTracking();

        Envelope env2 = new Envelope(5, 15, 0, 10);
        context.setAreaOfInterest(env2, GeographicCoordinateSystem.WGS84);
        assertEquals(1, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        event = (MapBoundsEvent) events.get(0);
        assertEquals(env1, event.getOldAreaOfInterest());
        assertEquals(env2, context.getAreaOfInterest());
        assertEquals(LocalCoordinateSystem.PROMISCUOUS, event.getOldCoordinateReferenceSystem());
        assertEquals(GeographicCoordinateSystem.WGS84, context.getCoordinateReferenceSystem());
        assertEquals(MapBoundsEvent.AREA_OF_INTEREST_MASK | MapBoundsEvent.COORDINATE_SYSTEM_MASK,
            event.getType());

        // test for immutability
        env2.expandToInclude(50, 50);
        assertNotSame(env2, context.getAreaOfInterest());

        Envelope mapEnvelope = context.getAreaOfInterest();
        mapEnvelope.expandToInclude(100, 100);
        assertNotSame(mapEnvelope, context.getAreaOfInterest());

        // test transform
        Envelope env3 = new Envelope(5, 10, 5, 10);
        context.setAreaOfInterest(env3);

        clearEventTracking();

        AffineTransform tx = AffineTransform.getScaleInstance(2, 2);
        context.transform(tx);

        Envelope env4 = new Envelope(10, 20, 10, 20);
        assertEquals(env4, context.getAreaOfInterest());
        assertEquals(1, boundsChangedCount);
        assertEquals(0, layerAddedCount);
        assertEquals(0, layerRemovedCount);
        assertEquals(0, layerChangedCount);
        assertEquals(0, layerMovedCount);
        assertEquals(0, propertyChangeCount);
        event = (MapBoundsEvent) events.get(0);
        assertEquals(env3, event.getOldAreaOfInterest());
        assertEquals(MapBoundsEvent.AREA_OF_INTEREST_MASK, event.getType());
    }

    public void testLayerBounds() throws Exception {
        
        // TODO: fix DataUtilities generated FeatureSource to be a full
        // FeatureCollection wrapper
        
//        DefaultMapContext context = new DefaultMapContext();
//        Envelope testEnv1 = new Envelope(0, 10, 0, 10);
//        context.addLayer(TestUtils.buildLayer(0, "style", "layer1"));
//        assertEquals(testEnv1, context.getLayerBounds());
//
//        Envelope testEnv2 = new Envelope(0, 20, 0, 10);
//        context.addLayer(TestUtils.buildLayer(10, "style", "layer2"));
//        assertEquals(testEnv2, context.getLayerBounds());
//
//        Envelope testEnv3 = new Envelope(10, 20, 0, 10);
//        context.removeLayer(1);
//        assertEquals(testEnv3, context.getLayerBounds());
    }
}
