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
package org.geotools.filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

// J2SE dependencies 
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Exports a filter as a OGC XML Filter document.  This class is does not
 * generate namespace compliant xml, even though it does print gml prefixes.
 * It was also written before the 1.0 filter spec, so some of it may be not up
 * to date.
 *
 * @author James Macgill, PSU
 *
 * @task HACK: Logging errors, very bad!  We need a filter visitor exception,
 *       or have visit methods throw illegal filter exceptions, or io
 *       exceptions.
 * @task TODO: Support full header information for new XML file
 * @task REVISIT: make filter utils class so that other encoders (like sql). It
 *       could also be nice to refactor common code from gml producer, as
 *       there is basically a GeometryProducer there.
 * @task REVISIT: make namespace aware.
 */
public class XMLEncoder implements org.geotools.filter.FilterVisitor {
    /** The logger for the filter module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** Map of comparison types to sql representation */
    private static Map comparisions = new HashMap();

    /** Map of spatial types to sql representation */
    private static Map spatial = new HashMap();

    /** Map of logical types to sql representation */
    private static Map logical = new HashMap();

    /** Map of expression types to sql representation */
    private static Map expressions = new HashMap();

    static {
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS),
            "PropertyIsEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN),
            "PropertyIsGreaterThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),
            "PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN),
            "PropertyIsLessThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),
            "PropertyIsLessThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.LIKE), "PropertyIsLike");
        comparisions.put(new Integer(AbstractFilter.NULL), "PropertyIsNull");
        comparisions.put(new Integer(AbstractFilter.BETWEEN),
            "PropertyIsBetween");

        expressions.put(new Integer(DefaultExpression.MATH_ADD), "Add");
        expressions.put(new Integer(DefaultExpression.MATH_DIVIDE), "Div");
        expressions.put(new Integer(DefaultExpression.MATH_MULTIPLY), "Mul");
        expressions.put(new Integer(DefaultExpression.MATH_SUBTRACT), "Sub");
        expressions.put(new Integer(DefaultExpression.FUNCTION), "Function");

        //more to come
        spatial.put(new Integer(AbstractFilter.GEOMETRY_EQUALS), "Equals");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT), "Disjoint");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),
            "Intersects");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES), "Touches");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CROSSES), "Crosses");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_WITHIN), "Within");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS), "Contains");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS), "Overlaps");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BEYOND), "Beyond");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BBOX), "BBOX");

        logical.put(new Integer(AbstractFilter.LOGIC_AND), "And");
        logical.put(new Integer(AbstractFilter.LOGIC_OR), "Or");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT), "Not");
    }

    /** To write the xml representations of filters to */
    private Writer out;

    /**
     * Constructor with writer to write filters to.
     *
     * @param out where to write the xml representation of filters.
     */
    public XMLEncoder(Writer out) {
        this.out = out;
    }

    /**
     * Creates a new instance of XMLEncoder
     *
     * @param out The writer to write to.
     * @param filter the filter to encode.
     */
    public XMLEncoder(Writer out, Filter filter) {
        this.out = out;

        try {
            encode(filter);
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Encodes the filter to the current writer.
     *
     * @param filter the filter to encode.
     *
     * @throws java.io.IOException if there are problems writing to out.
     */
    public void encode(Filter filter) throws java.io.IOException {
        out.write("<Filter>\n");
        filter.accept(this);
        out.write("</Filter>\n");
    }

    /**
     * Encodes the expression to the current writer.
     *
     * @param expression the expression to encode.
     */
    public void encode(Expression expression) {
        expression.accept(this);
    }

    /**
     * This should never be called. This can only happen if a subclass of
     * AbstractFilter failes to implement its own version of
     * accept(FilterVisitor);
     *
     * @param filter The filter to visit
     */
    public void visit(Filter filter) {
        LOGGER.warning("exporting unknown filter type");
    }

    /**
     * Writes the xml representation of a Between filter.
     *
     * @param filter the between filter to encode.
     */
    public void visit(BetweenFilter filter) {
        LOGGER.finer("exporting BetweenFilter");

        Expression left = (Expression) filter.getLeftValue();
        Expression right = (Expression) filter.getRightValue();
        Expression mid = (Expression) filter.getMiddleValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is "
            + comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            mid.accept(this);
            out.write("<LowerBoundary>\n");
            left.accept(this);
            out.write("</LowerBoundary>\n<UpperBoundary>\n");
            right.accept(this);
            out.write("</UpperBoundary>\n");
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a like filter.
     *
     * @param filter the like filter to encode.
     */
    public void visit(LikeFilter filter) {
        LOGGER.finer("exporting like filter");

        try {
            String wcm = filter.getWildcardMulti();
            String wcs = filter.getWildcardSingle();
            String esc = filter.getEscape();
            out.write("<PropertyIsLike wildCard=\"" + wcm + "\" singleChar=\""
                + wcs + "\" escape=\"" + esc + "\">\n");
            ((Expression) filter.getValue()).accept(this);
            out.write("<Literal>\n" + filter.getPattern() + "\n</Literal>\n");
            out.write("</PropertyIsLike>\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a logic filter.
     *
     * @param filter the logic filter to encode.
     */
    public void visit(LogicFilter filter) {
        LOGGER.finer("exporting LogicFilter");

        filter.getFilterType();

        String type = (String) logical.get(new Integer(filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");

            java.util.Iterator list = filter.getFilterIterator();

            while (list.hasNext()) {
                ((AbstractFilter) list.next()).accept(this);
            }

            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a compare filter.
     *
     * @param filter the compare filter to encode.
     */
    public void visit(CompareFilter filter) {
        LOGGER.finer("exporting ComparisonFilter");

        Expression left = filter.getLeftValue();
        Expression right = filter.getRightValue();
        LOGGER.finer("Filter type id is " + filter.getFilterType());
        LOGGER.finer("Filter type text is "
            + comparisions.get(new Integer(filter.getFilterType())));

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            LOGGER.fine("exporting left expression " + left);
            left.accept(this);
            LOGGER.fine("exporting right expression " + right);
            right.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a geometry filter.
     *
     * @param filter the geometry filter to encode.
     */
    public void visit(GeometryFilter filter) {
        LOGGER.finer("exporting GeometryFilter");

        Expression left = filter.getLeftGeometry();
        Expression right = filter.getRightGeometry();
        String type = (String) spatial.get(new Integer(filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            left.accept(this);
            right.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a null filter.
     *
     * @param filter the null filter to encode.
     */
    public void visit(NullFilter filter) {
        LOGGER.finer("exporting NullFilter");

        Expression expr = (Expression) filter.getNullCheckValue();

        String type = (String) comparisions.get(new Integer(
                    filter.getFilterType()));

        try {
            out.write("<" + type + ">\n");
            expr.accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export filter" + ioe);
        }
    }

    /**
     * Writes the xml representation of a fid filter.
     *
     * @param filter the fid filter to encode.
     */
    public void visit(FidFilter filter) {
        LOGGER.finer("exporting FidFilter");

        String[] fids = filter.getFids();

        for (int i = 0; i < fids.length; i++) {
            try {
                out.write("<FeatureId fid=\"" + fids[i] + "\"/>");
            } catch (java.io.IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
            }
        }
    }

    /**
     * Writes the xml representation of an Attribute expression.
     *
     * @param expression the attribute expression to encode.
     */
    public void visit(AttributeExpression expression) {
        LOGGER.finer("exporting ExpressionAttribute");

        try {
            out.write("<PropertyName>" + expression.getAttributePath()
                + "</PropertyName>\n");
        } catch (java.io.IOException ioe) {
            LOGGER.finer("Unable to export expresion: " + ioe);
        }
    }

    /**
     * This should never be called.  This can only happen if a subclass of
     * DefaultExpression fails to implement its own version of
     * accept(FilterVisitor);
     *
     * @param expression the expression to encode.
     */
    public void visit(Expression expression) {
        LOGGER.warning("exporting unknown (default) expression");
    }

    /**
     * Export the contents of a Literal Expresion
     *
     * @param expression the Literal to export
     *
     * @task TODO: Fully support GeometryExpressions so that they are writen as
     *       GML.
     */
    public void visit(LiteralExpression expression) {
        LOGGER.finer("exporting LiteralExpression");

        try {
            Object value = expression.getLiteral();

            if (Geometry.class.isAssignableFrom(value.getClass())) {
                GeometryEncoder encoder = new GeometryEncoder(out);
                encoder.encode((Geometry) value);
            } else {
                out.write("<Literal>" + value + "</Literal>\n");
            }
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion" + ioe);
        }
    }

    /**
     * Writes the xml representation of a  expression.
     *
     * @param expression the expression to encode.
     */
    public void visit(MathExpression expression) {
        LOGGER.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            out.write("<" + type + ">\n");
            ((Expression) expression.getLeftValue()).accept(this);
            ((Expression) expression.getRightValue()).accept(this);
            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }

    /**
     * Writes the xml representation of a  expression.
     *
     * @param expression the expression to encode.
     */
    public void visit(FunctionExpression expression) {
        LOGGER.finer("exporting Expression Math");

        String type = (String) expressions.get(new Integer(expression.getType()));

        try {
            out.write("<" + type + " name = " + expression.getName() + ">\n");

            Expression[] args = expression.getArgs();

            for (int i = 0; i < args.length; i++) {
                args[i].accept(this);
            }

            out.write("</" + type + ">\n");
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion: " + ioe);
        }
    }

    /**
     * Encodes geometries for filters.
     *
     * @task REVISIT: combine with gml producer code if possible (may not be,
     *       but it should be investigated).
     */
    static class GeometryEncoder {
        /** HACK!?!  The srs isn't always 4326, is it? */
        private String srs = "epsg:4326";

        /** The writer where the gml should print to. */
        private PrintWriter out;

        /**
         * Constructor with writer to write to.
         *
         * @param out where to write the geometry.
         */
        public GeometryEncoder(Writer out) {
            this.out = new PrintWriter(out);
        }

        /**
         * Constructor with writer to write to and srs.
         *
         * @param srs The spatial reference system id of the geometry.
         * @param out where to write the geometry.
         */
        public GeometryEncoder(String srs, Writer out) {
            this.out = new PrintWriter(out);
            this.srs = srs;
        }

        /**
         * Encodes the geometry, delegating to the appropriate sub method.
         *
         * @param geom the geometry to encode.
         */
        public void encode(Geometry geom) {
            Class geomType = geom.getClass();

            if (Point.class.isAssignableFrom(geomType)) {
                encode((Point) geom);
            } else if (LineString.class.isAssignableFrom(geomType)) {
                encode((LineString) geom);
            } else if (Polygon.class.isAssignableFrom(geomType)) {
                encode((Polygon) geom);
            } else if (MultiPoint.class.isAssignableFrom(geomType)) {
                encode((MultiPoint) geom);
            } else if (MultiLineString.class.isAssignableFrom(geomType)) {
                encode((MultiLineString) geom);
            } else if (MultiPolygon.class.isAssignableFrom(geomType)) {
                encode((MultiPolygon) geom);
            } else if (GeometryCollection.class.isAssignableFrom(geomType)) {
                encode((GeometryCollection) geom);
            }
        }

        /**
         * Encodes the coordinates to gml
         *
         * @param coords the coordinates to encode.
         */
        public void encode(Coordinate[] coords) {
            out.print("<gml:coordinates>");

            for (int i = 0; i < coords.length; i++) {
                out.print(coords[i].x + "," + coords[i].y);
                out.print((i < (coords.length - 1)) ? " " : "");
            }

            out.println("</gml:coordinates>");
        }

        /**
         * Encodes the point to gml
         *
         * @param point the point to encode.
         */
        public void encode(Point point) {
            out.println("<gml:Point srsName=\"" + srs + "\">");
            encode(point.getCoordinates());
            out.println("</gml:Point>");
        }

        /**
         * Encodes the LineString to gml
         *
         * @param line the LineString to encode.
         */
        public void encode(LineString line) {
            out.println("<gml:LineString srsName=\"" + srs + "\">");
            encode(line.getCoordinates());
            out.println("</gml:LineString>");
        }

        /**
         * Encodes the Polygon to gml
         *
         * @param polygon the Polygon to encode.
         */
        public void encode(Polygon polygon) {
            out.println("<gml:Polygon srsName=\"" + srs + "\">");
            out.println("<gml:outerBoundaryIs>");
            encode(polygon.getExteriorRing());
            out.println("</gml:outerBoundaryIs>");

            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                out.println("<gml:innerBoundaryIs>");
                encode(polygon.getInteriorRingN(i));
                out.println("</gml:innerBoundaryIs>");
            }

            out.println("</gml:Polygon>");
        }

        /**
         * Encodes the MultiPoint to gml
         *
         * @param mpoint the MultiPoint to encode.
         */
        public void encode(MultiPoint mpoint) {
            out.println("<gml:MultiPoint srsName=\"" + srs + "\">\n");

            for (int i = 0; i < mpoint.getNumGeometries(); i++) {
                encode((Point) mpoint.getGeometryN(i));
            }

            out.println("</gml:MultiPoint>\n");
        }

        /**
         * Encodes the MultiLineString to gml
         *
         * @param mline the MultiLineString to encode.
         */
        public void encode(MultiLineString mline) {
            out.println("<gml:MultiLineString srsName=\"" + srs + "\">\n");

            for (int i = 0; i < mline.getNumGeometries(); i++) {
                encode((LineString) mline.getGeometryN(i));
            }

            out.println("</gml:MultiLineString>\n");
        }

        /**
         * Encodes the MultiPolygon to gml
         *
         * @param mpolygon the MultiPolygon to encode.
         */
        public void encode(MultiPolygon mpolygon) {
            out.println("<gml:MultiPolygon srsName=\"" + srs + "\">\n");

            for (int i = 0; i < mpolygon.getNumGeometries(); i++) {
                encode((Polygon) mpolygon.getGeometryN(i));
            }

            out.println("</gml:MultiPolygon>\n");
        }

        /**
         * Encodes the Geometry Collection to gml
         *
         * @param geomcoll the collection to encode
         */
        public void encode(GeometryCollection geomcoll) {
            out.println("<gml:MultiGeometry srsName=\"" + srs + "\">\n");

            for (int i = 0; i < geomcoll.getNumGeometries(); i++) {
                encode(geomcoll.getGeometryN(i));
            }

            out.println("</gml:MultiGeometry>\n");
        }
    }
}
