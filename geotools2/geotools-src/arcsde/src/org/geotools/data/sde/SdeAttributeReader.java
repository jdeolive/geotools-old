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
package org.geotools.data.sde;

import org.geotools.data.AttributeReader;
import org.geotools.feature.AttributeType;
import java.io.IOException;
import com.esri.sde.sdk.client.*;

/**
 * Not used yet, since I'm thinking in a way for better streaming of attributes
 * on demand. I mean, if an attribute reader just holds all attributes in an
 * Object[], it is not really doing it's function. A significant performance
 * gain can be accomplished if attribute values does not get loaded id they're
 * not needed, since API users will never bo so complasient of specifying to
 * retrieve just the needed atts, i.e. for rendering, there're no reasson
 * to load all attributes that not affect the rendering process

 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeAttributeReader implements AttributeReader
{

    private SeRow sdeRow;

    /**
     * Creates a new SdeAttributeReader object.
     */
    public SdeAttributeReader(SeRow sdeRow)
    {
      this.sdeRow = sdeRow;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getAttributeCount()
    {
        return sdeRow.getNumColumns();
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public AttributeType getAttributeType(int i)
        throws ArrayIndexOutOfBoundsException
    {
        /**
         * @todo Implement this org.geotools.data.AttributeReader method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method getAttributeType() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public void close() throws IOException
    {
        /**
         * @todo Implement this org.geotools.data.AttributeReader method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method close() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public boolean hasNext() throws IOException
    {
        /**
         * @todo Implement this org.geotools.data.AttributeReader method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method hasNext() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public void next() throws IOException
    {
        /**
         * @todo Implement this org.geotools.data.AttributeReader method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method next() not yet implemented.");
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     * @throws java.lang.UnsupportedOperationException DOCUMENT ME!
     */
    public Object read(int i)
        throws IOException, ArrayIndexOutOfBoundsException
    {
        /**
         * @todo Implement this org.geotools.data.AttributeReader method
         */
        throw new java.lang.UnsupportedOperationException(
            "Method read() not yet implemented.");
    }
}
