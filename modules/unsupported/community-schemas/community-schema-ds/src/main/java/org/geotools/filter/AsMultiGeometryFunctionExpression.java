package org.geotools.filter;

import org.opengis.feature.Attribute;
import org.opengis.filter.expression.Expression;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Function which wraps an instance of geometry in its associatd multi geometry
 * type.
 * 
 * <p>
 * <ul>
 * <li>Point -> MultiPoint
 * <li>LineString -> MultiLineString
 * <li>Polygon -> MultiPolygon
 * </ul>
 * <br>
 * <br>
 * This function takes a single argument expression which must evaluate to an
 * instanceof {@link com.vividsolutions.jts.geom.Geometry}.
 * </p>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 * 
 */
public class AsMultiGeometryFunctionExpression extends FunctionExpressionImpl {
    public AsMultiGeometryFunctionExpression() {
        super("asMultiGeometry");
    }

    public int getArgCount() {
        return 1;
    }

    public Object evaluate(Object obj) {
        if (!(obj instanceof Attribute)) {
            return null;
        }
        Attribute att = (Attribute) obj;
        org.opengis.filter.expression.Expression arg = (Expression) getParameters()
                .get(0);
        Object value = arg.evaluate(att);

        if (value != null) {
            if (value instanceof Geometry) {
                if (value instanceof GeometryCollection) {
                    return value;
                }

                return wrap((Geometry) value);
            } else {
                throw new IllegalArgumentException(
                        "function argument did not evaluate to "
                                + Geometry.class);
            }
        }

        return null;
    }

    private GeometryCollection wrap(Geometry geometry) {
        if (geometry instanceof Point) {
            return geometry.getFactory().createMultiPoint(
                    new Point[] { (Point) geometry });
        } else if (geometry instanceof LineString) {
            return geometry.getFactory().createMultiLineString(
                    new LineString[] { (LineString) geometry });
        } else if (geometry instanceof Polygon) {
            return geometry.getFactory().createMultiPolygon(
                    new Polygon[] { (Polygon) geometry });

        }

        throw new IllegalArgumentException(
                "Unable to create multi geometry from " + geometry);

    }

}
