package org.geotools.vpf.ifc;

/**
 * DataTypesDefinition.java
 *
 *
 * Created: Thu Jan 02 17:26:02 2003
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version $Id: DataTypesDefinition.java,v 1.1 2003/01/18 13:31:12 kobit Exp $
 */

public interface DataTypesDefinition 
{

  // Byte order codes
  /**
   * <code>LEAST_SIGNIF_FIRST</code> stores code for indicator
   * of byte order <code>least-significant-byte-first</code> used
   * during creating table. 
   */
  public static final char LEAST_SIGNIF_FIRST = 'L';
  /**
   * <code>MOST_SIGNIF_FIRST</code> stores code for indicator
   * of byte order <code>most-significant-byte-first</code> used
   * during creating table. 
   */
  public static final char MOST_SIGNIF_FIRST = 'M';
  
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
  
}// DataTypesDefinition
