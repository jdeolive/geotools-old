/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;

import java.util.Iterator;


/**
 * @author Simone Giannecchini
 *
 */
public class ImageMosaicJDBCServiceTest extends TestCase {
    /**
     *
     */
    public ImageMosaicJDBCServiceTest() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(ImageMosaicJDBCServiceTest.class);
    }

    public void testIsAvailable() {
        //		String[] f = ImageIO.getReaderFormatNames();
        //		for ( int i = 0; i < f.length;i++  ) 
        //			System.out.println("Format "+f[i]);
        Iterator<GridFormatFactorySpi> list = GridFormatFinder.getAvailableFormats()
                                                              .iterator();
        boolean found = false;

        while (list.hasNext()) {
            final GridFormatFactorySpi fac = list.next();

            if (fac instanceof ImageMosaicJDBCFormatFactory) {
                found = true;

                break;
            }
        }

        assertTrue("ImageMosaicJDBCFormatFactorySpi not registered", found);
    }
}
