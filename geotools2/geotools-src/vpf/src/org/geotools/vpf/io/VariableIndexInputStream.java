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
 * VariableIndexInputStream.java Created: Mon Feb 24 22:23:58 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexInputStream.java,v 1.7 2003/04/04 09:15:50 kobit Exp $
 */
public class VariableIndexInputStream extends VPFInputStream {
    /**
     * Creates a new VariableIndexInputStream object.
     *
     * @param file DOCUMENT ME!
     * @param byteOrder DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VariableIndexInputStream(
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
        return ((VariableIndexHeader) getHeader()).getEntriesNumber();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public VPFHeader readHeader() throws IOException {
        return new VariableIndexHeader(
            readInteger(),
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
        return new VariableIndexRow(
            readInteger(),
            readInteger()
        );
    }
}


// VariableIndexInputStream
