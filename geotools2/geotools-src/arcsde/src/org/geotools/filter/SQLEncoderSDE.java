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

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import com.esri.sde.sdk.client.*;

/**
 * Encodes a filter into a SQL WHERE statement for arcsde.
 * <p>
 * Although not all filters support is coded yet, the strategy to filtering
 * queries for ArcSDE datasources is separated in two parts, the SQL where
 * clause construction, provided here and the 
 * spatial filters (or spatial constraints, in SDE vocabulary) provided
 * by <code>GeometryEncoderSDE</code>; mirroring the java SDE api approach
 * for easy programing
 * </p>
 *
 * @author Chris Holmes, TOPP
 * @author Gabriel Roldán
 */
public class SQLEncoderSDE extends SQLEncoder
    implements org.geotools.filter.FilterVisitor {
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    /**
     * The filters that this encoder can processed with default capabilities:
     * AbstractFilter.LOGIC_OR AbstractFilter.LOGIC_AND
     * AbstractFilter.LOGIC_NOT AbstractFilter.COMPARE_EQUALS
     * AbstractFilter.COMPARE_NOT_EQUALS AbstractFilter.COMPARE_LESS_THAN
     * AbstractFilter.COMPARE_GREATER_THAN
     * AbstractFilter.COMPARE_LESS_THAN_EQUAL
     * AbstractFilter.COMPARE_GREATER_THAN_EQUAL AbstractFilter.NULL
     * AbstractFilter.BETWEEN
     */
    private static FilterCapabilities capabilities = SQLEncoder.getCapabilities();

    static {
        capabilities.addType(AbstractFilter.LIKE);
    }

    /** DOCUMENT ME! */
    private List sdeSpatialFilters = new ArrayList();

    /** DOCUMENT ME! */
    private SeLayer sdeLayer;

    /**
     */
    public SQLEncoderSDE() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param layer DOCUMENT ME!
     */
    public void setLayer(SeLayer layer) {
        this.sdeLayer = layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static FilterCapabilities getCapabilities() {
        return capabilities; //maybe clone?  Make immutable somehow
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalStateException DOCUMENT ME!
     */
    private String getLayerName() {
        if (sdeLayer == null) {
            throw new IllegalStateException("SDE layer name has not been set");
        }

        return sdeLayer.getName();
    }

    /**
     * overriden just to avoid the "WHERE" keyword
     *
     * @param out DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void encode(Writer out, Filter filter) throws SQLEncoderException {
        if (capabilities.fullySupports(filter)) {
            this.out = out;
            filter.accept(this);
        } else {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

/**
 *@task TODO: look forward for Like filter implementation or contribute in doing so.
 * 
     public void visit(LikeFilter filter) throws UnsupportedOperationException {
        log.finer("exporting like filter");

        try {
            String pattern = filter.getPattern();
            String wildCard = filter.getWildcardMulti();
            pattern = pattern.replaceAll(wildCard, "%");
            ((DefaultExpression) filter.getValue()).accept(this);
            out.write(" LIKE ");
            out.write("'" + pattern + "'");
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage(), ioe);
        }
    }
*/
}
