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

/**
 * Class CoordinateFloat.java is responsible for
 * 
 * <p>
 * Created: Thu Mar 27 15:11:13 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */
public class CoordinateFloat implements Coordinate {
    protected float[][] coordinates;

    /**
     * Creates a new CoordinateFloat object.
     *
     * @param coords DOCUMENT ME!
     */
    public CoordinateFloat(float[][] coords) {
        coordinates = coords;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < coordinates.length; i++) {
            sb.append("(");

            for (int j = 0; j < coordinates[i].length; j++) {
                if (j > 0) {
                    sb.append(", ");
                }

                // end of if (j > 0)
                sb.append(coordinates[i][j]);
            }

            // end of for (int j = 0; j < coordinates[i].length; j++)
            sb.append(")");
        }

        // end of for (int i = 0; i < coordinates.length; i++)
        return sb.toString();
    }
}


// CoordinateFloat
