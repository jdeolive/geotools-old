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

package org.geotools.vpf.io;

import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;
import java.io.IOException;
import java.io.InputStream;


/**
 * SpatialIndexInputStream.java Created: Mon Feb 24 22:25:15 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: SpatialIndexInputStream.java,v 1.5 2003/04/04 09:15:48 kobit Exp $
 */
public class SpatialIndexInputStream extends VPFInputStream {
    /** Variable constant <code>SPATIAL_INDEX_ROW_SIZE</code> keeps value of */
    public static final long SPATIAL_INDEX_ROW_SIZE = 8;

    /**
     * Creates a new SpatialIndexInputStream object.
     *
     * @param file DOCUMENT ME!
     * @param byteOrder DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public SpatialIndexInputStream(
        String file,
        char byteOrder
    ) throws IOException {
        super(file, byteOrder);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int tableSize() {
        return -1;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFHeader readHeader() throws IOException {
        return new SpatialIndexHeader(
            readInteger(),
            readFloat(),
            readFloat(),
            readFloat(),
            readFloat(),
            readInteger()
        );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFRow readRow() throws IOException {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param pos DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void setPosition(long pos) throws IOException {
        seek(SPATIAL_INDEX_ROW_SIZE * pos);
    }
}


// SpatialIndexInputStream
