/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.geotools.vpf;

/**
 * Class CoordinateDouble.java is responsible for 
 *
 * <p>
 * Created: Thu Mar 27 15:06:52 2003
 * </p>
 * @unittest on
 * @unittest_code "Put test code below"
 *  return true;
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class CoordinateDouble implements Coordinate {

  protected double[][] coordinates;
  
  public CoordinateDouble(double[][] coords) 
  {
    coordinates = coords;
  }
  
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < coordinates.length; i++)
    {
      sb.append("(");
      for (int j = 0; j < coordinates[i].length; j++)
      {
        if (j > 0)
        {
          sb.append(", ");
        } // end of if (j > 0)
        sb.append(coordinates[i][j]);
      } // end of for (int j = 0; j < coordinates[i].length; j++)
      sb.append(")");
    } // end of for (int i = 0; i < coordinates.length; i++)
    return sb.toString();
  }
  
}// CoordinateDouble
