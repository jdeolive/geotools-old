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
import java.util.Arrays;
import java.util.logging.Logger;


/**
 * Encodes a filter into a SQL WHERE statement for postgis.  This class adds
 * the ability to turn geometry filters into sql statements if they are
 * bboxes.
 *
 * @author Chris Holmes, TOPP
 *
 * @task TODO: integrated with SQLEncoderPostgisGeos.java, as there no  real
 *       reason to have two different classes.  We just need to do testing to
 *       make sure both handle everything.  At the very least have the geos
 *       one extend more intelligently.
 */
public class SQLEncoderPostgis extends SQLEncoder
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");

    /** To write geometry so postgis can read it. */
    private static WKTWriter wkt = new WKTWriter();

    /**
     * The filters that this encoder can processed. (Note this value shadows
     * private capabilities in superclass)
     */
    private FilterCapabilities capabilities = new FilterCapabilities();

    /**
     * The srid of the schema, so the bbox conforms.  Could be better to have
     * it in the bbox filter itself, but this works for now.
     */
    private int srid;

    /** The geometry attribute to use if none is specified. */
    private String defaultGeom;
    private boolean useGeos;
    private String fidColumn;

    /**
     * Empty constructor TODO: rethink empty constructor, as BBOXes _need_ an
     * SRID, must make client set it somehow.  Maybe detect when encode is
     * called?
     */
    public SQLEncoderPostgis() {
        capabilities.addType(AbstractFilter.LOGIC_OR);
        capabilities.addType(AbstractFilter.LOGIC_AND);
        capabilities.addType(AbstractFilter.LOGIC_NOT);
        capabilities.addType(AbstractFilter.COMPARE_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_NOT_EQUALS);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN);
        capabilities.addType(AbstractFilter.COMPARE_LESS_THAN_EQUAL);
        capabilities.addType(AbstractFilter.COMPARE_GREATER_THAN_EQUAL);
        capabilities.addType(AbstractFilter.NULL);
        capabilities.addType(AbstractFilter.BETWEEN);
        capabilities.addType((short) 12345);
        capabilities.addType((short) -12345);
        capabilities.addType(AbstractFilter.GEOMETRY_BBOX);
        capabilities.addType(AbstractFilter.FID);
    }

    public SQLEncoderPostgis(boolean looseBbox) {
        this();

        if (!looseBbox) {
            this.useGeos = true;
        }
    }

    /**
     * Constructor with srid.
     *
     * @param srid spatial reference id to encode geometries with.
     */
    public SQLEncoderPostgis(int srid) {
        this(true);
        this.srid = srid;
    }

    /**
     * Capabilities of this encoder.
     *
     * @return
     *
     * @see org.geotools.filter.SQLEncoder#getCapabilities()
     */
    public FilterCapabilities getCapabilities() {
        return capabilities;
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
     * Sets the fid column to use for fid filters.  This _must_ be set if fid
     * filters are used.
     *
     * @param fidColumnName DOCUMENT ME!
     *
     * @task TODO: put in constructor.
     * @task TODO: if set to null don't report in capabilities.
     */
    public void setFidColumn(String fidColumnName) {
        this.fidColumn = fidColumnName;
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
     * This should never be called. This can only happen if a subclass of
     * AbstractFilter failes to implement its own version of
     * accept(FilterVisitor);
     *
     * @param filter The filter to visit
     *
     * @throws RuntimeException for IO Encoding problems.
     *
     * @task REVISIT: I don't think Filter.NONE and Filter.ALL should be
     *       handled here.  They should have their own methods, but they don't
     *       have interfaces, so I don't know if that's possible.
     */
    public void visit(Filter filter) {
        try {
            //HACK: 12345 are Filter.NONE and Filter.ALL, they
            //should have some better names though.
            if (filter.getFilterType() == 12345) {
                out.write("TRUE");
            } else if (filter.getFilterType() == -12345) {
                out.write("FALSE");
            }
            LOGGER.warning("exporting unknown filter type:"+filter.getClass().getName());
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("io error while writing", ioe);
        }
    }

    /**
     * Encodes an FidFilter.
     *
     * @param filter
     *
     * @task TODO: We're getting problems when the fidColumn is an int but  the
     *       fidFilter passes in something that is not an int.  Ideally we
     *       should figure out if the fidColumn type matches that passed in
     *       but for now we are going to make an assumption based on the fact
     *       that all fids returned by postgis will be of form rail.342.  So
     *       if we don't have a '.' in the fid, we will just pass -1 for the
     *       column name, which should gaurantee it won't match anything.  The
     *       one place where this _could_ mess us up is if users pass in
     *       something like 'road.rail3'.  But all should really use fids that
     *       they are returned, and this is really just a fix to pass the cite
     *       tests.  We can revisit once we figure out fid encoding in
     *       general.
     *
     * @see org.geotools.filter.SQLEncoder#visit(org.geotools.filter.FidFilter)
     */
    public void visit(FidFilter filter) {
        String[] fids = filter.getFids();
        LOGGER.finer("Exporting FID=" + Arrays.asList(fids));

        for (int i = 0; i < fids.length; i++) {
            try {
                out.write(fidColumn);
                out.write(" = '");

                int pos;

                if ((pos = fids[i].indexOf('.')) != -1) {
                    out.write(fids[i].substring(pos + 1));
                } else {
                    //not what we're expecting, postgis always returns in 
                    //rail.3 form, so just write out something that will not
                    //match but that will also parse (not causing errors).
                    out.write("-1");

                    //out.write(fids[i]);
                }

                out.write("'");

                if (i < (fids.length - 1)) {
                    out.write(" OR ");
                }
            } catch (java.io.IOException e) {
                LOGGER.warning("IO Error exporting FID Filter.");
            }
        }
    }

    /**
     * Turns a geometry filter into the postgis sql bbox statement.
     *
     * @param filter the geometry filter to be encoded.
     *
     * @throws RuntimeException for IO exception (need a better error)
     */
    public void visit(GeometryFilter filter) throws RuntimeException {
        LOGGER.finer("exporting GeometryFilter");

        if (filter.getFilterType() == AbstractFilter.GEOMETRY_BBOX) {
            DefaultExpression left = (DefaultExpression) filter.getLeftGeometry();
            DefaultExpression right = (DefaultExpression) filter
                .getRightGeometry();

            try {
                if (useGeos) {
                    out.write("NOT disjoint(");
                }

                if (left == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    left.accept(this);
                }

                if (useGeos) {
                    out.write(", ");
                } else {
                    out.write(" && ");
                }

                if (right == null) {
                    out.write("\"" + defaultGeom + "\"");
                } else {
                    right.accept(this);
                }

                if (useGeos) {
                    out.write(")");
                }
            } catch (java.io.IOException ioe) {
                LOGGER.warning("Unable to export filter" + ioe);
                throw new RuntimeException("io error while writing", ioe);
            }
        } else {
            LOGGER.warning("exporting unknown filter type, only bbox supported");
            throw new RuntimeException("Only BBox is currently supported");
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
        LOGGER.finer("exporting LiteralExpression");

        try {
            if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY) {
                Geometry bbox = (Geometry) expression.getLiteral();
                String geomText = wkt.write(bbox);
                out.write("GeometryFromText('" + geomText + "', " + srid + ")");
            } else {
                super.visit(expression);
            }
        } catch (java.io.IOException ioe) {
            LOGGER.warning("Unable to export expresion" + ioe);
            throw new RuntimeException("io error while writing", ioe);
        }
    }
}
