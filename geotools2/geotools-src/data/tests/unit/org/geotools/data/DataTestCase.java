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

import junit.framework.TestCase;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * A set of constructs and utility methods used to test the data module.
 * <p>
 * By isolating a commong set of Features,FeatureTypes and Filters
 * we are able to reduce the amount of overhead in setting up new
 * tests.
 * </p>
 * <p>
 * We have also special cased assert( Geometry, Geometry ) to work around
 * Geometry.equals( Object ) not working as expected.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class DataTestCase extends TestCase {
    protected GeometryFactory gf;
    protected FeatureType roadType; // road: id,geom,name
    protected FeatureType subRoadType; // road: id,geom
    protected Feature[] roadFeatures;
    protected Envelope roadBounds;
    protected Envelope rd12Bounds;    
    protected Filter rd1Filter;
    protected Filter rd2Filter;
    protected Filter rd12Filter;
    protected Feature newRoad;
    protected FeatureType riverType; // river: id, geom, river, flow
    protected FeatureType subRiverType; // river: river, flow     
    protected Feature[] riverFeatures;
    protected Envelope riverBounds;
    protected Filter rv1Filter;
    protected Feature newRiver;

    /**
     * Constructor for DataUtilitiesTest.
     *
     * @param arg0
     */
    public DataTestCase(String arg0) {
        super(arg0);        
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        String namespace = getName();
        roadType = DataUtilities.createType(namespace+".road",
                "id:0,geom:LineString,name:String");
        subRoadType = DataUtilities.createType(namespace+"road",
                "id:0,geom:LineString");
        gf = new GeometryFactory();

        roadFeatures = new Feature[3];

        //           3,2
        //  2,2 +-----+-----+ 4,2
        //     /     rd1     \
        // 1,1+               +5,1
        roadFeatures[0] = roadType.create(new Object[] {
                    new Integer(1), line(new int[] { 1, 1, 2, 2, 4, 2, 5, 1 }),
                    "r1",
                }, "road.rd1");

        //       + 3,4
        //       + 3,3
        //  rd2  + 3,2
        //       |
        //    3,0+
        roadFeatures[1] = roadType.create(new Object[] {
                    new Integer(2), line(new int[] { 3, 0, 3, 2, 3, 3, 3, 4 }),
                    "r2"
                }, "road.rd2");

        //     rd3     + 5,3
        //            / 
        //  3,2 +----+ 4,2
        roadFeatures[2] = roadType.create(new Object[] {
                    new Integer(3), line(new int[] { 3, 2, 4, 2, 5, 3 }), "r3"
                }, "road.rd3");
        roadBounds = new Envelope(1, 5, 0, 4);
        FilterFactory factory = FilterFactory.createFilterFactory();
        rd1Filter = factory.createFidFilter("road.rd1");
        rd2Filter = factory.createFidFilter("road.rd2");

        FidFilter create = factory.createFidFilter();
        create.addFid("road.rd1");
        create.addFid("road.rd2");
        
        rd12Filter = create;
        
        rd12Bounds = new Envelope();
        rd12Bounds.expandToInclude(roadFeatures[0].getBounds());
        rd12Bounds.expandToInclude(roadFeatures[1].getBounds());        
        //   + 2,3
        //  / rd4
        // + 1,2
        newRoad = roadType.create(new Object[] {
                    new Integer(4), line(new int[] { 1, 2, 2, 3 }), "r4"
                }, "road.rd4");

        riverType = DataUtilities.createType(namespace+".river",
                "id:0,geom:MultiLineString,river:String,flow:0.0");
        subRiverType = DataUtilities.createType(namespace+".river",
                "river:String,flow:0.0");
        gf = new GeometryFactory();
        riverFeatures = new Feature[2];

        //       9,7     13,7
        //        +------+
        //  5,5  /
        //  +---+ rv1
        //   7,5 \
        //    9,3 +----+ 11,3
        riverFeatures[0] = riverType.create(new Object[] {
                    new Integer(1),
                    lines(new int[][] {
                            { 5, 5, 7, 4 },
                            { 7, 5, 9, 7, 13, 7 },
                            { 7, 5, 9, 3, 11, 3 }
                        }), "rv1", new Double(4.5)
                }, "river.rv1");

        //         + 6,10    
        //        /
        //    rv2+ 4,8
        //       |
        //   4,6 +
        riverFeatures[1] = riverType.create(new Object[] {
                    new Integer(2),
                    lines(new int[][] {
                            { 4, 6, 4, 8, 6, 10 }
                        }), "rv2", new Double(3.0)
                }, "river.rv2");
        riverBounds = new Envelope(4, 13, 3, 10);
        rv1Filter = FilterFactory.createFilterFactory().createFidFilter("river.rv1");

        //  9,5   11,5   
        //   +-----+
        //     rv3  \ 
        //           + 13,3
        //                     
        newRiver = riverType.create(new Object[] {
                    new Integer(3),
                    lines(new int[][] {
                            { 9, 5, 11, 5, 13, 3 }
                        }), "rv3", new Double(1.5)
                }, "river.rv3");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        gf = null;
        roadType = null;
        subRoadType = null;
        roadFeatures = null;
        roadBounds = null;
        rd1Filter = null;
        rd2Filter = null;
        newRoad = null;
        riverType = null;
        subRiverType = null;     
        riverFeatures = null;
        riverBounds = null;
        rv1Filter = null;
        newRiver = null;                    
    }
    
    public LineString line(int[] xy) {
        Coordinate[] coords = new Coordinate[xy.length / 2];

        for (int i = 0; i < xy.length; i += 2) {
            coords[i / 2] = new Coordinate(xy[i], xy[i + 1]);
        }

        return gf.createLineString(coords);
    }

    public MultiLineString lines(int[][] xy) {
        LineString[] lines = new LineString[xy.length];

        for (int i = 0; i < xy.length; i++) {
            lines[i] = line(xy[i]);
        }

        return gf.createMultiLineString(lines);
    }
    //  need to special case Geometry
    protected void assertEquals(Geometry expected, Geometry actual) {
        if (expected == actual) {
            return;
        }
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected.equals(actual));
    }
    protected void assertEquals(String message, Geometry expected, Geometry actual) {
        if (expected == actual) {
            return;
        }
        assertNotNull(message, expected);
        assertNotNull(message, actual);
        assertTrue(message, expected.equals(actual));
    }           
}
