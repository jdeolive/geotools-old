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

import org.geotools.feature.AttributeType;
import java.io.IOException;


/**
 * Attribute Reader that joins.
 *
 * @author Ian Schneider
 * @version $Id: JoiningAttributeReader.java,v 1.2 2003/11/04 00:28:50 cholmesny Exp $
 */
public class JoiningAttributeReader implements AttributeReader {
    private AttributeReader[] readers;
    private int[] index;
    private AttributeType[] metaData;

    /**
     * Creates a new instance of JoiningAttributeReader
     *
     * @param readers Readers to join
     */
    public JoiningAttributeReader(AttributeReader[] readers) {
        this.readers = readers;

        this.metaData = joinMetaData(readers);
    }

    private AttributeType[] joinMetaData(AttributeReader[] readers) {
        int total = 0;
        index = new int[readers.length];

        for (int i = 0, ii = readers.length; i < ii; i++) {
            index[i] = total;
            total += readers[i].getAttributeCount();
        }

        AttributeType[] md = new AttributeType[total];
        int idx = 0;

        for (int i = 0, ii = readers.length; i < ii; i++) {
            for (int j = 0, jj = readers[i].getAttributeCount(); j < jj; j++) {
                md[idx] = readers[i].getAttributeType(j);
                idx++;
            }
        }

        return md;
    }

    public void close() throws IOException {
        IOException dse = null;

        for (int i = 0, ii = readers.length; i < ii; i++) {
            try {
                readers[i].close();
            } catch (IOException e) {
                dse = e;
            }
        }

        if (dse != null) {
            throw dse;
        }
    }

    public boolean hasNext() throws IOException {
        for (int i = 0, ii = readers.length; i < ii; i++) {
            if (readers[i].hasNext()) {
                return true;
            }
        }

        return false;
    }

    public void next() throws IOException {
        for (int i = 0, ii = readers.length; i < ii; i++) {
            if (readers[i].hasNext()) {
                readers[i].next();
            }
        }
    }

    public Object read(int idx) throws IOException {
        AttributeReader reader = null;

        for (int i = index.length - 1; i >= 0; i--) {
            if (idx >= index[i]) {
                idx -= index[i];
                reader = readers[i];

                break;
            }
        }

        if (reader == null) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }

        return reader.read(idx);
    }

    public int getAttributeCount() {
        return metaData.length;
    }

    public AttributeType getAttributeType(int i) {
        return metaData[i];
    }
}
