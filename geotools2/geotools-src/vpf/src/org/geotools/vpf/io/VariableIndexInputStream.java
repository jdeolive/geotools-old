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

/**
 * VariableIndexInputStream.java Created: Mon Feb 24 22:23:58 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexInputStream.java,v 1.8 2003/05/19 20:59:38 kobit Exp $
 */
public class VariableIndexInputStream extends VPFInputStream {

    /**
     * Creates a new <code>VariableIndexInputStream</code> instance.
     *
     * @param file a <code>String</code> value
     * @param byteOrder a <code>char</code> value
     * @exception IOException if an error occurs
     */
    public VariableIndexInputStream(String file, char byteOrder)
        throws IOException {
        super(file, byteOrder);
    }

    /**
     * Describe <code>tableSize</code> method here.
     *
     * @return an <code>int</code> value
     */
    public int tableSize() {
        return ((VariableIndexHeader) getHeader()).getEntriesNumber();
    }

    /**
     * Describe <code>readHeader</code> method here.
     *
     * @return a <code>VPFHeader</code> value
     * @exception IOException if an error occurs
     */
    public VPFHeader readHeader() throws IOException {
        return new VariableIndexHeader(
            readInteger(),
            readInteger()
        );
    }

    /**
     * Describe <code>readRow</code> method here.
     *
     * @return a <code>VPFRow</code> value
     * @exception IOException if an error occurs
     */
    public VPFRow readRow() throws IOException {
        return new VariableIndexRow(
            readInteger(),
            readInteger()
        );
    }
}


// VariableIndexInputStream
