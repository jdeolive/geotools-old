/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.firstdraft.util;

import java.net.URI;
import java.util.Random;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.referencing.crs.DefaultEngineeringCRS;


public class Generator {
    private static final FeatureType type = createFeatureType();
    private static final GeometryFactory gfact = new GeometryFactory();
    private static final Random rand = new Random();
    private static final FilterFactory filterFactory = new FilterFactoryImpl();
    private final double xrange;
    private final double yrange;

    public Generator(double xrange, double yrange) {
        this.xrange = xrange;
        this.yrange = yrange;
    }

    public static FeatureType createFeatureType() {
        FeatureTypeBuilder builder = FeatureTypeBuilder.newInstance("test");
        GeometricAttributeType geom = new GeometricAttributeType("geom", Geometry.class, true,
                null, DefaultEngineeringCRS.GENERIC_2D, Filter.INCLUDE);
        AttributeType dummydata = DefaultAttributeTypeFactory.newAttributeType("dummydata",
                String.class);
        long time = System.currentTimeMillis();
        builder.addType(geom);
        builder.addType(dummydata);
        builder.setDefaultGeometry(geom);
        builder.setNamespace(URI.create("testStore"));

        try {
            FeatureType type = builder.getFeatureType();
            time = System.currentTimeMillis() - time;
            System.out.println(time);

            return type;
        } catch (SchemaException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    private static LineString createRectangle(double x1, double y1, double x2, double y2) {
        double x_min = (x1 < x2) ? x1 : x2;
        double y_min = (y1 < y2) ? y1 : y2;
        double x_max = (x1 < x2) ? x2 : x1;
        double y_max = (y1 < y2) ? y2 : y1;
        Coordinate[] coords = new Coordinate[5];
        coords[0] = new Coordinate(x_min, y_min);
        coords[1] = new Coordinate(x_max, y_min);
        coords[2] = new Coordinate(x_max, y_max);
        coords[3] = new Coordinate(x_min, y_max);
        coords[4] = coords[0];

        CoordinateSequence cs = new CoordinateArraySequence(coords);

        return new LineString(cs, gfact);
    }

    public Feature createFeature(int i) {
        Geometry g = createRectangle(xrange * rand.nextDouble(), yrange * rand.nextDouble(),
                xrange * rand.nextDouble(), yrange * rand.nextDouble());
        String dummydata = "Id: " + i;
        Feature f = null;

        try {
            f = type.create(new Object[] { g, dummydata });

            return f;
        } catch (IllegalAttributeException e) {
            throw (RuntimeException) new RuntimeException().initCause(e);
        }
    }

    public static Coordinate pickRandomPoint(Coordinate center, double xrange, double yrange) {
        double x = (center.x - (xrange / 2)) + (xrange * rand.nextDouble());
        double y = (center.y - (yrange / 2)) + (yrange * rand.nextDouble());

        return new Coordinate(x, y);
    }

    public static Query createBboxQuery(Coordinate center, double xrange, double yrange) {
        double x_min = center.x - (xrange / 2);
        double x_max = center.x + (xrange / 2);
        double y_min = center.y - (yrange / 2);
        double y_max = center.y + (yrange / 2);
        Filter bb = filterFactory.bbox(type.getPrimaryGeometry().getLocalName(), x_min, y_min,
                x_max, y_max, type.getPrimaryGeometry().getCoordinateSystem().toString());

        return new DefaultQuery(type.getTypeName(), bb);
    }

    public FeatureType getFeatureType() {
        return type;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage : Generator number_of_data");
            System.exit(0);
        }

        int numberOfObjects = Integer.parseInt(args[0]);
        Generator gen = new Generator(1000, 1000);

        for (int i = 0; i < numberOfObjects; i++) {
            Feature f = gen.createFeature(i);
            System.out.println(f);

            Coordinate c = pickRandomPoint(new Coordinate(500, 500), 900, 900);
            Query q = createBboxQuery(c, 100, 100);
            System.out.println(q);
        }
    }
}
