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

import org.geotools.vpf.ifc.VPFRow;


/**
 * VariableIndexRow.java Created: Sun Mar 16 23:28:11 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexRow.java,v 1.2 2003/04/04 09:15:50 kobit Exp $
 */
public class VariableIndexRow implements VPFRow {
    protected int offset = 0;
    protected int size = 0;

    /**
     * Creates a new VariableIndexRow object.
     *
     * @param offset DOCUMENT ME!
     * @param size DOCUMENT ME!
     */
    public VariableIndexRow(
        int offset,
        int size
    ) {
        this.offset = offset;
        this.size = size;
    }

    // VariableIndexRow constructor

    /**
     * Gets the value of offset
     *
     * @return the value of offset
     */
    public int getOffset() {
        return this.offset;
    }

    //   /**
    //    * Sets the value of offset
    //    *
    //    * @param argOffset Value to assign to this.offset
    //    */
    //   public void setOffset(int argOffset)
    //   {
    // 	this.offset = argOffset;
    //   }

    /**
     * Gets the value of size
     *
     * @return the value of size
     */
    public int getSize() {
        return this.size;
    }

    //   /**
    //    * Sets the value of size
    //    *
    //    * @param argSize Value to assign to this.size
    //    */
    //   public void setSize(int argSize)
    //   {
    // 	this.size = argSize;
    //   }
}


// VariableIndexRow
