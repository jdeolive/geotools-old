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

package org.geotools.vpf.util;

import org.geotools.vpf.Coordinate2DDouble;
import org.geotools.vpf.Coordinate2DFloat;
import org.geotools.vpf.Coordinate3DDouble;
import org.geotools.vpf.Coordinate3DFloat;
import org.geotools.vpf.VPFDate;
import org.geotools.vpf.exc.VPFDataFormatException;
import org.geotools.vpf.ifc.DataTypesDefinition;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


/**
 * Class DataUtils.java is responsible for
 * 
 * <p>
 * Created: Wed Jan 29 10:06:37 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: DataUtils.java,v 1.9 2003/04/04 09:15:50 kobit Exp $
 */
public class DataUtils implements DataTypesDefinition {
    /**
     * DOCUMENT ME!
     *
     * @param source DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static byte[] toBigEndian(byte[] source) {
        byte[] result = new byte[source.length];

        for (int i = 0; i < source.length; i++) {
            result[i] = source[source.length - (i + 1)];
        }

        // end of for (int i = 0; i < source.length; i++)
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes DOCUMENT ME!
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Object decodeData(
        byte[] bytes,
        char type
    ) {
        Object result = null;

        switch (type) {
        case DATA_TEXT:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:

            StringBuffer sb = new StringBuffer(bytes.length);

            for (int i = 0; i < bytes.length; i++) {
                sb.append((char) bytes[i]);
            }

            // end of for (int i = 0; i < bytes.length; i++)
            boolean isNull = false;

            for (int i = 0; i < STRING_NULL_VALUES.length; i++) {
                isNull |= sb.toString().trim().equalsIgnoreCase(
                    STRING_NULL_VALUES[i]
                );
            }

            // end of for (int i = 0; i < STRING_NULL_VALUES.length; i++)
            if (isNull) {
                result = null;
            } // end of if (isNull)
            else {
                result = sb.toString();
            }

            // end of else
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

        case DATA_2_COORD_F: {
            float[][] coords = new float[bytes.length / DATA_2_COORD_F_LEN][2];
            byte[] floatData = new byte[DATA_SHORT_FLOAT_LEN];

            for (int i = 0; i < coords.length; i++) {
                copyArrays(floatData, bytes, i * DATA_2_COORD_F_LEN);
                coords[i][0] = decodeFloat(floatData);
                copyArrays(floatData, bytes, i * (DATA_2_COORD_F_LEN + 1));
                coords[i][1] = decodeFloat(floatData);
            }

            // end of for (int i = 0; i < coords.length; i++)
            result = new Coordinate2DFloat(coords);
        }

        break;

        case DATA_2_COORD_R: {
            double[][] coords =
                new double[bytes.length / DATA_2_COORD_R_LEN][2];
            byte[] doubleData = new byte[DATA_LONG_FLOAT_LEN];

            for (int i = 0; i < coords.length; i++) {
                copyArrays(doubleData, bytes, i * DATA_2_COORD_R_LEN);
                coords[i][0] = decodeDouble(doubleData);
                copyArrays(doubleData, bytes, i * (DATA_2_COORD_R_LEN + 1));
                coords[i][1] = decodeDouble(doubleData);
            }

            // end of for (int i = 0; i < coords.length; i++)
            result = new Coordinate2DDouble(coords);
        }

        break;

        case DATA_3_COORD_F: {
            float[][] coords = new float[bytes.length / DATA_3_COORD_F_LEN][3];
            byte[] floatData = new byte[DATA_SHORT_FLOAT_LEN];

            for (int i = 0; i < coords.length; i++) {
                copyArrays(floatData, bytes, i * DATA_3_COORD_F_LEN);
                coords[i][0] = decodeFloat(floatData);
                copyArrays(floatData, bytes, i * (DATA_3_COORD_F_LEN + 1));
                coords[i][1] = decodeFloat(floatData);
                copyArrays(floatData, bytes, i * (DATA_3_COORD_F_LEN + 2));
                coords[i][2] = decodeFloat(floatData);
            }

            // end of for (int i = 0; i < coords.length; i++)
            result = new Coordinate3DFloat(coords);
        }

        break;

        case DATA_3_COORD_R: {
            double[][] coords =
                new double[bytes.length / DATA_3_COORD_R_LEN][3];
            byte[] doubleData = new byte[DATA_LONG_FLOAT_LEN];

            for (int i = 0; i < coords.length; i++) {
                copyArrays(doubleData, bytes, i * DATA_3_COORD_R_LEN);
                coords[i][0] = decodeDouble(doubleData);
                copyArrays(doubleData, bytes, i * (DATA_3_COORD_R_LEN + 1));
                coords[i][1] = decodeDouble(doubleData);
                copyArrays(doubleData, bytes, i * (DATA_3_COORD_R_LEN + 2));
                coords[i][2] = decodeDouble(doubleData);
            }

            // end of for (int i = 0; i < coords.length; i++)
            result = new Coordinate3DDouble(coords);
        }

        break;

        case DATA_DATE_TIME:
            result = new VPFDate(bytes);

            break;

        case DATA_NULL_FIELD:
            break;

        case DATA_TRIPLET_ID:default:
            break;
        } // end of switch (tcd.getType())

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dest DOCUMENT ME!
     * @param source DOCUMENT ME!
     * @param fromIdx DOCUMENT ME!
     */
    public static void copyArrays(
        byte[] dest,
        byte[] source,
        int fromIdx
    ) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] = source[i + fromIdx];
        }

        // end of for (int i = 0; i < dest.length; i++)
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static short decodeShort(byte[] bytes) {
        short res = 0;
        int shift = 8;

        for (int i = 0; (i < bytes.length) && (shift >= 0); i++) {
            res |= (((short) (bytes[i] & 0xff)) << shift);
            shift -= 8;
        }

        return res;
    }

    //   public static int littleEndianToInt(byte[] fourBytes)
    //   {
    //     int res = 0;
    //     int limit = Math.min(fourBytes.length, 4);
    //     for (int i = 0; i < limit; i++)
    //     {
    //       res |= (fourBytes[i] & 0xFF) << (i*8);
    //     } // end of for (int i = 0; i < limit-1; i++)
    //     return res;
    //   }
    public static int decodeInt(byte[] bytes) {
        int res = 0;
        int shift = 24;

        for (int i = 0; (i < bytes.length) && (shift >= 0); i++) {
            res |= ((bytes[i] & 0xff) << shift);
            shift -= 8;
        }

        return res;
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static float decodeFloat(byte[] bytes) {
        int res = 0;
        int shift = 24;

        for (int i = 0; (i < bytes.length) && (shift >= 0); i++) {
            res |= ((bytes[i] & 0xff) << shift);
            shift -= 8;
        }

        return Float.intBitsToFloat(res);
    }

    /**
     * DOCUMENT ME!
     *
     * @param bytes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static double decodeDouble(byte[] bytes) {
        long res = 0;
        int shift = 56;

        for (int i = 0; (i < bytes.length) && (shift >= 0); i++) {
            res |= (((long) (bytes[i] & 0xff)) << shift);
            shift -= 8;
        }

        return Double.longBitsToDouble(res);
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int unsigByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int getDataTypeSize(char type) {
        int size = -1;

        switch (type) {
        case DATA_TEXT:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:
            size = 1;

            break;

        case DATA_SHORT_FLOAT:
            size = DATA_SHORT_FLOAT_LEN;

            break;

        case DATA_LONG_FLOAT:
            size = DATA_LONG_FLOAT_LEN;

            break;

        case DATA_SHORT_INTEGER:
            size = DATA_SHORT_INTEGER_LEN;

            break;

        case DATA_LONG_INTEGER:
            size = DATA_LONG_INTEGER_LEN;

            break;

        case DATA_2_COORD_F:
            size = DATA_2_COORD_F_LEN;

            break;

        case DATA_2_COORD_R:
            size = DATA_2_COORD_R_LEN;

            break;

        case DATA_3_COORD_F:
            size = DATA_3_COORD_F_LEN;

            break;

        case DATA_3_COORD_R:
            size = DATA_3_COORD_R_LEN;

            break;

        case DATA_DATE_TIME:
            size = DATA_DATE_TIME_LEN;

            break;

        case DATA_NULL_FIELD:
            size = DATA_NULL_FIELD_LEN;

            break;

        case DATA_TRIPLET_ID:
            size = DATA_TRIPLET_ID_LEN;

        default:
            break;
        } // end of switch (type)

        return size;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static boolean isNumeric(char type) {
        switch (type) {
        case DATA_TEXT:
        case DATA_LEVEL1_TEXT:
        case DATA_LEVEL2_TEXT:
        case DATA_LEVEL3_TEXT:
        case DATA_DATE_TIME:
        case DATA_NULL_FIELD:
            return false;

        case DATA_SHORT_FLOAT:
        case DATA_LONG_FLOAT:
        case DATA_SHORT_INTEGER:
        case DATA_LONG_INTEGER:
            return true;

        case DATA_2_COORD_F:
        case DATA_2_COORD_R:
        case DATA_3_COORD_F:
        case DATA_3_COORD_R:
        case DATA_TRIPLET_ID:
            return true;

        default:
            return false;
        } // end of switch (type)
    }
}


// DataUtils
