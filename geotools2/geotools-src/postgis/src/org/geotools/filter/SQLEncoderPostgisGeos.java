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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import java.util.logging.Logger;


/**
 * Encodes a filter into a SQL WHERE statement for postgis.  With geos
 * installed.  This should be redone, probably integrated with the postgis
 * stuff, but that whole hierarchy should be redone, since the capabilities
 * stuff is a bit wacky.  This should only be used on versions of postgis
 * installed with GEOS support, to handle all the advanced spatial queries.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: SQLEncoderPostgisGeos.java,v 1.2 2003/12/03 22:49:54 cholmesny Exp $
 */
public class SQLEncoderPostgisGeos extends SQLEncoderPostgis
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /** To write geometry so postgis can read it. */
    private static WKTWriter wkt = new WKTWriter();

    /**
     * The filters that this encoder can processed. (Note this value shadows
     * private capabils in superclass)
     */
    private FilterCapabilities capabils = new FilterCapabilities();

    /**
     * The srid of the schema, so the bbox conforms.  Could be better to have
     * it in the bbox filter itself, but this works for now.
     */
    private int srid;

    /** The geometry attribute to use if none is specified. */
    private String defaultGeom;

    /**
     * Empty constructor TODO: rethink empty constructor, as BBOXes _need_ an
     * SRID, must make client set it somehow.  Maybe detect when encode is
     * called?
     */
    public SQLEncoderPostgisGeos() {
        capabils.addType(AbstractFilter.LOGIC_OR);
        capabils.addType(AbstractFilter.LOGIC_AND);
        capabils.addType(AbstractFilter.LOGIC_NOT);
        capabils.addType(AbstractFilter.COMPARE_EQUALS);
        capabils.addType(AbstractFilter.COMPARE_NOT_EQUALS);
        capabils.addType(AbstractFilter.COMPARE_LESS_THAN);
        capabils.addType(AbstractFilter.COMPARE_GREATER_THAN);
        capabils.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
        capabils.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
        capabils.addType(AbstractFilter.NULL);
        capabils.addType(AbstractFilter.BETWEEN);
        capabils.addType((short) 12345);
        capabils.addType((short) -12345);
        capabils.addType(AbstractFilter.GEOMETRY_BBOX);
        capabils.addType(AbstractFilter.GEOMETRY_EQUALS);
        capabils.addType(AbstractFilter.GEOMETRY_DISJOINT);
        capabils.addType(AbstractFilter.GEOMETRY_INTERSECTS);
        capabils.addType(AbstractFilter.GEOMETRY_CROSSES);
        capabils.addType(AbstractFilter.GEOMETRY_WITHIN);
        capabils.addType(AbstractFilter.GEOMETRY_CONTAINS);
        capabils.addType(AbstractFilter.GEOMETRY_OVERLAPS);
        capabils.addType(AbstractFilter.GEOMETRY_TOUCHES);
    }

    /**
     * Constructor with srid.
     *
     * @param srid spatial reference id to encode geometries with.
     */
    public SQLEncoderPostgisGeos(int srid) {
        this();
        this.srid = srid;
    }

    /**
     * Capabils of this encoder.
     *
     * @return
     *
     * @see org.geotools.filter.SQLEncoder#getCapabils()
     */
    public FilterCapabilities getCapabilities() {
        return capabils;
    }

    /**
     * Sets a spatial reference system ESPG number, so that the geometry can be
     * properly encoded for postgis.  If geotools starts actually creating
     * geometries with valid srids then this method will no longer be needed.
     *
     * @param srid the integer code for the EPSG spatial reference system.
     */
    public void setSRID(int srid) {
        this.srid = srid;
    }

    /**
     * Sets the default geometry, so that filters with null for one of their
     * expressions can assume that the default geometry is intended.
     *
     * @param name the name of the default geometry Attribute.
     *
     * @task REVISIT: pass in a featureType so that geometries can figure out
     *       their own default geometry?
     */
    public void setDefaultGeometry(String name) {
        //Do we really want clients to be using malformed filters?  
        //I mean, this is a useful method for unit tests, but shouldn't 
        //fully formed filters usually be used?  Though I guess adding 
        //the option wouldn't hurt. -ch
        this.defaultGeom = name;
    }

    /**
     * Turns a geometry filter into the postgis sql bbox statement.
     *
     * @param filter the geometry filter to be encoded.
     *
     * @throws RuntimeException for IO exception (need a better error)
     */
    public void visit(GeometryFilter filter) throws RuntimeException {
        log.finer("exporting GeometryFilter");

        short filterType = filter.getFilterType();
        DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
        DefaultExpression right = (DefaultExpression) filter.getRightGeometry();

        // Figure out if we need to constrain this query with the && constraint.
        int literalGeometryCount = 0;

        if ((left != null)
                && (left.getType() == DefaultExpression.LITERAL_GEOMETRY)) {
            literalGeometryCount++;
        }

        if ((right != null)
                && (right.getType() == DefaultExpression.LITERAL_GEOMETRY)) {
            literalGeometryCount++;
        }

        boolean constrainBBOX = (literalGeometryCount == 1);

        try {
            out.write("(");

            String closingParenthesis = ")";

            if (filterType == AbstractFilter.GEOMETRY_EQUALS) {
                out.write("equals");
            } else if (filterType == AbstractFilter.GEOMETRY_DISJOINT) {
                out.write("NOT (intersects");
                closingParenthesis += ")";
            } else if (filterType == AbstractFilter.GEOMETRY_INTERSECTS) {
                out.write("intersects");
            } else if (filterType == AbstractFilter.GEOMETRY_CROSSES) {
                out.write("crosses");
            } else if (filterType == AbstractFilter.GEOMETRY_WITHIN) {
                out.write("within");
            } else if (filterType == AbstractFilter.GEOMETRY_CONTAINS) {
                out.write("contains");
            } else if (filterType == AbstractFilter.GEOMETRY_OVERLAPS) {
                out.write("overlaps");
            } else if (filterType == AbstractFilter.GEOMETRY_BBOX) {
                out.write("intersects");
            } else if (filterType == AbstractFilter.GEOMETRY_TOUCHES) {
                out.write("touches");
            } else {
                //this will choke on beyond and dwithin
                throw new RuntimeException("does not support filter type "
                    + filterType);
            }

            out.write("(");

            if (left == null) {
                out.write("\"" + defaultGeom + "\"");
            } else {
                left.accept(this);
            }

            out.write(", ");

            if (right == null) {
                out.write("\"" + defaultGeom + "\"");
            } else {
                right.accept(this);
            }

            out.write(")");

            if (constrainBBOX) {
                out.write(" AND ");

                if (left == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    left.accept(this);
                }

                out.write(" && ");

                if (right == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    right.accept(this);
                }
            }

            out.write(closingParenthesis);
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export filter" + ioe);
            throw new RuntimeException("io error while writing", ioe);
        }
    }

    /**
     * Checks to see if the literal is a geometry, and encodes it if it  is, if
     * not just sends to the parent class.
     *
     * @param expression the expression to visit and encode.
     *
     * @throws RuntimeException for IO exception (need a better error)
     */
    public void visit(LiteralExpression expression) throws RuntimeException {
        log.finer("exporting LiteralExpression");

        try {
            if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY) {
                Geometry bbox = (Geometry) expression.getLiteral();
                String geomText = wkt.write(bbox);
                out.write("GeometryFromText('" + geomText + "', " + srid + ")");
            } else {
                super.visit(expression);
            }
        } catch (java.io.IOException ioe) {
            log.warning("Unable to export expresion" + ioe);
            throw new RuntimeException("io error while writing", ioe);
        }
    }
}
