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
package org.geotools.data;

import org.geotools.feature.FeatureType;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * A Default FIDReader.  Just auto-increments an index.   May be sufficient for
 * files, representing rows in a file.  For jdbc datasources a
 * ResultSetFIDReader should be used.
 *
 * @author Chris Holmes
 * @version $Id: DefaultFIDReader.java,v 1.2 2003/11/04 00:28:49 cholmesny Exp $
 */
public class DefaultFIDReader implements FIDReader {
    protected static final String CLOSE_MESG = "Close has already been called"
        + " on this FIDReader";

    /** The logger for the jdbc module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.data.jdbc");

    /** A flag to track the status of the result set. */
    protected boolean isClosed = false;
    protected int index = 0;
    protected String typeName;

    public DefaultFIDReader(String typeName) {
        this.typeName = typeName;
    }

    public DefaultFIDReader(FeatureType featureType) {
        this.typeName = featureType.getTypeName();
    }

    /**
     * Release any resources associated with this reader
     */
    public void close() {
        this.isClosed = true;
    }

    /**
     * Does another set of attributes exist in this reader?
     *
     * @return <code>true</code> if more attributes exist
     *
     * @throws IOException If closed
     */
    public boolean hasNext() throws IOException {
        if (isClosed) {
            throw new IOException(CLOSE_MESG);
        }

        return index < Integer.MAX_VALUE;
    }

    /**
     * Read the attribute at the given index.
     *
     * @return Attribute at index
     *
     * @throws IOException If closed
     */
    public String next() throws IOException {
        if (isClosed) {
            throw new IOException(CLOSE_MESG);
        }

        index++;
        LOGGER.finer("reading fid " + index);

        return typeName + "." + index;
    }
}
