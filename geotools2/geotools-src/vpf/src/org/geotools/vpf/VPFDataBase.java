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

package org.geotools.vpf;

import org.geotools.vpf.io.TableInputStream;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFLibraryIfc;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Class <code>VPFDataBase</code> is responsible for 
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataBase.java,v 1.3 2003/04/04 14:33:03 kobit Exp $
 */
public class VPFDataBase implements FileConstants {
    TableRow dataBaseInfo = null;
    TableRow[] libraries = null;

    /**
     * Creates a new <code><code>VPFDataBase</code></code> instance.
     *
     * @param directory a <code><code>File</code></code> value
     * @exception IOException if an error occurs
     */
    public VPFDataBase(File directory) throws IOException {
        // read data base header info
        String vpfTableName =
            new File(directory, DATABASE_HEADER_TABLE).toString();
        TableInputStream vpfTable = new TableInputStream(vpfTableName);
        dataBaseInfo = (TableRow) vpfTable.readRow();
        vpfTable.close();

        // read libraries info
        vpfTableName = new File(directory, LIBRARY_ATTTIBUTE_TABLE).toString();
        vpfTable = new TableInputStream(vpfTableName);

        ArrayList al = new ArrayList();
        TableRow tableRow = (TableRow) vpfTable.readRow();

        while (tableRow != null) {
            al.add(tableRow);
            tableRow = (TableRow) vpfTable.readRow();
        }

        vpfTable.close();
        libraries = (TableRow[]) al.toArray(new TableRow[al.size()]);
    }

    public double getMinX() {
        double xmin = libraries[0].get(VPFLibraryIfc.FIELD_XMIN).getAsDouble();
        for (int i = 1; i < libraries.length; i++) {
            double temp =
                libraries[i].get(VPFLibraryIfc.FIELD_XMIN).getAsDouble();
            xmin = Math.min(xmin, temp);
        }
        return xmin;
    }

    public double getMinY() {
        double ymin = libraries[0].get(VPFLibraryIfc.FIELD_YMIN).getAsDouble();
        for (int i = 1; i < libraries.length; i++) {
            double temp =
                libraries[i].get(VPFLibraryIfc.FIELD_YMIN).getAsDouble();
            ymin = Math.min(ymin, temp);
        }
        return ymin;
    }
    
    public double getMaxX() {
        double xmax = libraries[0].get(VPFLibraryIfc.FIELD_XMAX).getAsDouble();
        for (int i = 1; i < libraries.length; i++) {
            double temp =
                libraries[i].get(VPFLibraryIfc.FIELD_XMAX).getAsDouble();
            xmax = Math.min(xmax, temp);
        }
        return xmax;
    }

    public double getMaxY() {
        double ymax = libraries[0].get(VPFLibraryIfc.FIELD_YMAX).getAsDouble();
        for (int i = 1; i < libraries.length; i++) {
            double temp =
                libraries[i].get(VPFLibraryIfc.FIELD_YMAX).getAsDouble();
            ymax = Math.min(ymax, temp);
        }
        return ymax;
    }

    public static void main(String[] args) {
         
    }
    
}


// VPFDataBase
