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
package org.geotools.vpf.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.geotools.vpf.VPFDate;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.exc.VPFDataFormatException;
import org.geotools.vpf.Coordinate2DFloat;
import org.geotools.vpf.Coordinate2DDouble;
import org.geotools.vpf.Coordinate3DFloat;
import org.geotools.vpf.Coordinate3DDouble;

/**
 * Class DataUtils.java is responsible for 
 *
 * <p>
 * Created: Wed Jan 29 10:06:37 2003
 * </p>
 * @version $Id: DataUtils.java,v 1.3 2003/02/05 08:44:50 kobit Exp $
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 */

public class DataUtils implements DataTypesDefinition
{
  
  public static byte[] toBigEndian(byte[] source)
  {
	byte[] result = new byte[source.length];
	for (int i = 0; i < source.length; i++)
	{
	  result[i] = source[source.length - (i+1)];
	} // end of for (int i = 0; i < source.length; i++)
	return result;
  }

  public static Object decodeData(byte[] bytes, char type)
  {
	Object result = null;
	switch (type) {
	  case DATA_TEXT:
	  case DATA_LEVEL1_TEXT:
	  case DATA_LEVEL2_TEXT:
	  case DATA_LEVEL3_TEXT:
		StringBuffer sb = new StringBuffer(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
		  sb.append((char)bytes[i]);
		} // end of for (int i = 0; i < bytes.length; i++)
        boolean isNull = false;
        for (int i = 0; i < STRING_NULL_VALUES.length; i++)
        {
          isNull |=
			sb.toString().trim().equalsIgnoreCase(STRING_NULL_VALUES[i]);
        } // end of for (int i = 0; i < STRING_NULL_VALUES.length; i++)
        if (isNull)
        {
          result = null;
        } // end of if (isNull)
        else
        {
          result = sb.toString();
        } // end of else
		break;
	  case DATA_SHORT_FLOAT:
		result = new Float(decodeFloat(bytes));
		break;
	  case DATA_LONG_FLOAT:
		result = new Double(decodeDouble(bytes));
		break;
	  case DATA_SHORT_INTEGER:
		result = new Short(decodeShort(bytes));
		break;
	  case DATA_LONG_INTEGER:
		result = new Integer(decodeInt(bytes));
		break;
	  case DATA_2_COORD_F:
        result = new Coordinate2DFloat();
		break;
	  case DATA_2_COORD_R:
        result = new Coordinate2DDouble();
		break;
	  case DATA_3_COORD_F:
        result = new Coordinate3DFloat();
		break;
	  case DATA_3_COORD_R:
        result = new Coordinate3DDouble();
		break;
	  case DATA_DATE_TIME:
		result = new VPFDate(bytes);
		break;
	  case DATA_NULL_FIELD:

		break;
	  case DATA_TRIPLED_ID:

	  default:
		break;
	} // end of switch (tcd.getType())
	return result;
  }

  public static short decodeShort(byte[] bytes)
  {
    short res = 0;
    int shift = 8;
    for (int i = 0; i < bytes.length && shift >= 0; i++)
    {
      res |= ((short)(bytes[i] & 0xff)) << shift;
      shift -= 8;
    }
    return res;
  }

  public static int decodeInt(byte[] bytes)
  {
    int res = 0;
    int shift = 24;
    for (int i = 0; i < bytes.length && shift >= 0; i++)
    {
      res |= (bytes[i] & 0xff) << shift;
      shift -= 8;
    }
    return res;
  }
  
  public static float decodeFloat(byte[] bytes)
  {
    int res = 0;
    int shift = 24;
    for (int i = 0; i < bytes.length && shift >= 0; i++)
    {
      res |= (bytes[i] & 0xff) << shift;
      shift -= 8;
    }
    return Float.intBitsToFloat(res);
  }
  
  public static double decodeDouble(byte[] bytes)
  {
    long res = 0;
    int shift = 56;
    for (int i = 0; i < bytes.length && shift >= 0; i++)
    {
      res |= ((long)(bytes[i] & 0xff)) << shift;
      shift -= 8;
    }
    return Double.longBitsToDouble(res);
  }
  
  public static int littleEndianToInt(byte[] fourBytes)
  {
    int res = 0;
    int limit = Math.min(fourBytes.length, 4);
    for (int i = 0; i < limit; i++)
    {
      res |= (fourBytes[i] & 0xFF) << (i*8);
    } // end of for (int i = 0; i < limit-1; i++)
    return res;
  }

  public static int unsigByteToInt(byte b)
  {
    return (int) b & 0xFF;
  }

}// DataUtils
