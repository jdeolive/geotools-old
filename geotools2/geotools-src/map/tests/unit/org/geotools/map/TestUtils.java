/*
 * TestUtils.java
 *
 * Created on 29 novembre 2003, 20.15
 */

package org.geotools.map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.DataUtilities;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;

/**
 *
 * @author  wolf
 */
public class TestUtils {
    
    /** Creates a new instance of TestUtils */
    private TestUtils() {
    }
    
    /**
     * Builds and returns the features used to perform this test
     */
    public static FeatureCollection buildFeatureCollection(double xoffset)
        throws Exception {
        AttributeType[] types = new AttributeType[1];

        GeometryFactory geomFac = new GeometryFactory();

        types[0] = AttributeTypeFactory.newAttributeType("centre",
                Point.class);

        FeatureType pointType = FeatureTypeFactory.newFeatureType(types,
                "pointfeature");


        FeatureCollection fc = FeatureCollections
            .newCollection();
        fc.add(makeSamplePointFeature(geomFac, xoffset + 0.0, 0.0, pointType));
        fc.add(makeSamplePointFeature(geomFac, xoffset + 10.0, 10.0, pointType));

        return fc;
    }
    
    public static Feature makeSamplePointFeature(final GeometryFactory geomFac,
        double x, double y, FeatureType ft) throws IllegalAttributeException {
        Coordinate c = new Coordinate(x, y);
        Point point = geomFac.createPoint(c);

        return ft.create(new Object[] {point});
    }
    
    public static Style buildStyle(String styleName) {
        StyleFactory sf = StyleFactory.createStyleFactory();
        PointSymbolizer ps = sf.getDefaultPointSymbolizer();
        Rule r = sf.createRule();
        r.setSymbolizers(new Symbolizer[] {ps});
        
        Style s = sf.createStyle();
        s.setName(styleName);
        s.addFeatureTypeStyle(sf.createFeatureTypeStyle(new Rule[] {r}));
        
        return s;
    }
    
    public static DefaultMapLayer buildLayer(double offset, String styleName, String title) throws Exception {
        FeatureCollection fc = buildFeatureCollection(10);
        return new DefaultMapLayer(fc, buildStyle(styleName), title);
    }
    
}
