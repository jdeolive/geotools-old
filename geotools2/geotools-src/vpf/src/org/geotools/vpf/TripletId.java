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
 * Class TripletId.java is responsible for 
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class TripletId 
{

  byte[] rawData = null;

  public TripletId(byte[] data) 
  {
    rawData = data;
  }

  public String toString()
  {
    return rawData == null ? "NULL" : new String(rawData, 1, rawData.length-1);
  }

  public static int calculateDataSize(byte definition)
  {
    int[] pieces = new int[3];
    pieces[0] = (definition >> 2) & 3;
    pieces[1] = (definition >> 4) & 3;
    pieces[2] = (definition >> 6) & 3;
    int size = 0;
    for (int i = 0; i < pieces.length; i++)
    {
      switch (pieces[i])
      {
        case 0:
          break;
        case 1:
          size++;
          break;
        case 2:
          size += 2;
          break;
        case 3:
          size += 4;
          break;
        default:
          System.out.println("Tripled id size decoding error");
          System.out.println("tripled definition: "+definition);
          System.out.println("piece 0: "+pieces[0]);
          System.out.println("piece 1: "+pieces[1]);
          System.out.println("piece 2: "+pieces[2]);
          break;
      } // end of switch (pieces[i])
    } // end of for (int i = 0; i < pieces.length; i++)
    return size;
  }
  
}// TripletId
