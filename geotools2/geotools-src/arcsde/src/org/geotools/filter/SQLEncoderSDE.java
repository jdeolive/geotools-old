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

import com.esri.sde.sdk.client.*;
import org.geotools.data.sde.SdeAdapter;
import java.io.Writer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Encodes a filter into a SQL WHERE statement for arcsde.
 *
 * <p>
 * Although not all filters support is coded yet, the strategy to filtering
 * queries for ArcSDE datasources is separated in two parts, the SQL where
 * clause construction, provided here and the  spatial filters (or spatial
 * constraints, in SDE vocabulary) provided by
 * <code>GeometryEncoderSDE</code>; mirroring the java SDE api approach for
 * easy programing
 * </p>
 *
 * @author Chris Holmes, TOPP
 * @author Gabriel Roldán
 */
public class SQLEncoderSDE extends SQLEncoder
    implements org.geotools.filter.FilterVisitor
{
    /** Standard java logger */
    private static Logger LOGGER = Logger.getLogger("org.geotools.filter");


    /** DOCUMENT ME! */
    private SeLayer sdeLayer;

    public SQLEncoderSDE()
    {
    }

    /**
     */
    public SQLEncoderSDE(SeLayer layer)
    {
        this.sdeLayer = layer;
    }

    /**
     * @deprecated remove when the old data api dissapear
     * @param layer DOCUMENT ME!
     */
    public void setLayer(SeLayer layer)
    {
        this.sdeLayer = layer;
    }


    /**
     * overriden just to avoid the "WHERE" keyword
     *
     * @param out DOCUMENT ME!
     * @param filter DOCUMENT ME!
     *
     * @throws SQLEncoderException DOCUMENT ME!
     */
    public void encode(Writer out, Filter filter) throws SQLEncoderException
    {

        if (getCapabilities().fullySupports(filter))
        {
            this.out = out;
            filter.accept(this);
        }
        else
        {
            throw new SQLEncoderException("Filter type not supported");
        }
    }

    /**
     * This only exists the fulfill the interface - unless There is a way of
     * determining the FID column in the database...
     *
     * @param filter the Fid Filter.
     *
     * @throws RuntimeException DOCUMENT ME!
     */
    public void visit(FidFilter filter)
    {
        long[] fids = SdeAdapter.getNumericFids(filter.getFids());
        int nFids = fids.length;

        if (nFids == 0)
            return;

        String fidField = sdeLayer.getSpatialColumn();

        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append(fidField + " IN(");

            for (int i = 0; i < nFids; i++)
            {
                sb.append(fids[i]);

                if (i < nFids - 1)
                    sb.append(", ");
            }

            sb.append(')');

            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.finer("added fid filter: " + sb.toString());

            out.write(sb.toString());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /*
    * @task TODO: look forward for Like filter implementation or contribute in
    *       doing so.  public void visit(LikeFilter filter) throws
    *       UnsupportedOperationException { log.finer("exporting like
    *       filter"); try { String pattern = filter.getPattern(); String
    *       wildCard = filter.getWildcardMulti(); pattern =
    *       pattern.replaceAll(wildCard, "%"); ((DefaultExpression)
    *       filter.getValue()).accept(this); out.write(" LIKE ");
    *       out.write("'" + pattern + "'"); } catch (java.io.IOException ioe)
    *       { throw new RuntimeException(ioe.getMessage(), ioe); }}
    */

}
