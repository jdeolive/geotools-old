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

package org.geotools.vpf.ifc;

/**
 * DataTypesDefinition.java
 *
 *
 * Created: Thu Jan 02 17:26:02 2003
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version $Id: DataTypesDefinition.java,v 1.5 2003/01/28 15:15:10 kobit Exp $
 */

public interface DataTypesDefinition 
{

  // Byte order codes
  /**
   * <code>LEAST_SIGNIF_FIRST</code> stores code for indicator
   * of byte order <code>least-significant-byte-first</code> used
   * during creating table. It is little-endian byte order used
   * on Intel x86 based PCs but not in JVM.
   */
  public static final char LEAST_SIGNIF_FIRST = 'L';
  public static final char LITTLE_ENDIAN_ORDER = LEAST_SIGNIF_FIRST;
  /**
   * <code>MOST_SIGNIF_FIRST</code> stores code for indicator
   * of byte order <code>most-significant-byte-first</code> used
   * during creating table. It is big-endian byte order used on
   * Motorola CPU based machines and in JVM.
   */
  public static final char MOST_SIGNIF_FIRST = 'M';
  public static final char BIG_ENDIAN_ORDER = MOST_SIGNIF_FIRST;
  
  // Data type codes
  public static final char DATA_TEXT          = 'T';
  public static final char DATA_LEVEL1_TEXT   = 'L';
  public static final char DATA_LEVEL2_TEXT   = 'N';
  public static final char DATA_LEVEL3_TEXT   = 'M';
  public static final char DATA_SHORT_FLOAT   = 'F';
  public static final char DATA_LONG_FLOAT    = 'R';
  public static final char DATA_SHORT_INTEGER = 'S';
  public static final char DATA_LONG_INTEGER  = 'I';
  public static final char DATA_2_COORD_F     = 'C';
  public static final char DATA_2_COORD_R     = 'B';
  public static final char DATA_3_COORD_F     = 'Z';
  public static final char DATA_3_COORD_R     = 'Y';
  public static final char DATA_DATE_TIME     = 'D';
  public static final char DATA_NULL_FIELD    = 'X';
  public static final char DATA_TRIPLED_ID    = 'K';
  
  public static final int DATA_SHORT_FLOAT_LEN   = 4;
  public static final int DATA_LONG_FLOAT_LEN    = 8;
  public static final int DATA_SHORT_INTEGER_LEN = 2;
  public static final int DATA_LONG_INTEGER_LEN  = 4;
  public static final int DATA_DATE_TIME_LEN     = 20;
  public static final int DATA_2_COORD_F_LEN     = 8;
  public static final int DATA_2_COORD_R_LEN     = 16;
  public static final int DATA_3_COORD_F_LEN     = 12;
  public static final int DATA_3_COORD_R_LEN     = 24;
  public static final int DATA_TRIPLED_ID_LEN    = 1;
  public static final int DATA_NULL_FIELD_LEN    = 0;

  public static final String STRING_NULL_VALUE = "-";
  public static final String[] STRING_NULL_VALUES = {"-", "--", "N/A"};
  public static final char CHAR_NULL_VALUE = '-';

}// DataTypesDefinition
