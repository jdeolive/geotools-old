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

import java.io.IOException;

/** - Added hasNext to support the FeatureWriter API.
 *  - Changed order of writer parameters to match Collections, JDBC API.
 *  - Added IOExceptions on all methods.
 *  - Do we want AttributeWriters to know about the schema and perform
 *    validation??
 *  
 *
 * @version $Id: AttributeWriter.java,v 1.2 2003/12/01 22:00:37 cholmesny Exp $
 * @author  Ian Schneider
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public interface AttributeWriter {
    /**
     * The number of attributes this reader can read, i.e the length of a row.
     */
    int getAttributeCount();

    /**
     * Retrieve the AttributeType at the given index.
     */
    org.geotools.feature.AttributeType getAttributeType(int i) throws ArrayIndexOutOfBoundsException;
        
    /**
     * Advance the AttributeWriter, all calls to write will correspond to the
     * same set of attributes until next is called again.
     */
    void next() throws IOException;
    
    /**
     * Write the given attribute value at the position indicated.
     * Implementations can choose to immediately flush the write or buffer it.
     */
    void write(int position, Object attribute) throws IOException;
    
    void close() throws IOException;
    
    /** Query whether there are other rows in the attribute writer.
     * 
     * @return
     * @throws IOException
     * @see FeatureWriter#hasNext()
     */
    boolean hasNext() throws IOException;
    
}
