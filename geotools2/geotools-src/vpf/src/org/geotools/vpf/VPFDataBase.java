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
import java.util.List;


/**
 * Class <code>VPFDataBase</code> is responsible for 
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataBase.java,v 1.4 2003/04/11 12:36:24 kobit Exp $
 */
public class VPFDataBase implements FileConstants {

    protected File directory = null;
    protected TableRow dataBaseInfo = null;
    protected TableRow[] libraries = null;
    protected TableRow[][] coverages = null;

    /**
     * Creates a new <code><code>VPFDataBase</code></code> instance.
     *
     * @param directory a <code><code>File</code></code> value
     * @exception IOException if an error occurs
     */
    public VPFDataBase(File directory) throws IOException {
        // read data base header info
        this.directory = directory;
        String vpfTableName =
            new File(directory, DATABASE_HEADER_TABLE).toString();
        TableInputStream vpfTable = new TableInputStream(vpfTableName);
        dataBaseInfo = (TableRow) vpfTable.readRow();
        vpfTable.close();

        // read libraries info
        vpfTableName = new File(directory, LIBRARY_ATTTIBUTE_TABLE).toString();
        vpfTable = new TableInputStream(vpfTableName);
        List list = vpfTable.readAllRows();
        vpfTable.close();
        libraries = (TableRow[]) list.toArray(new TableRow[list.size()]);
        coverages = new TableRow[libraries.length][];
        for (int i = 0; i < coverages.length; i++) {
            coverages[i] = null;
        }
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

    public TableRow[] getCoverages(int libId) throws IOException {
        if (libId < 0 && libId >= coverages.length) {
            return null;
        }
        if (coverages[libId] == null) {
            String libCover =
                libraries[libId].get(VPFLibraryIfc.FIELD_LIB_NAME).getAsString();
            String vpfTableName =
                new File(new File(directory, libCover),
                         COVERAGE_ATTRIBUTE_TABLE).toString();
            TableInputStream vpfTable = new TableInputStream(vpfTableName);
            List list = vpfTable.readAllRows();
            vpfTable.close();
            coverages[libId] =
                (TableRow[]) list.toArray(new TableRow[list.size()]);
        }
        return coverages[libId];
    }

    public static void main(String[] args) {
         
    }
    
}


// VPFDataBase
