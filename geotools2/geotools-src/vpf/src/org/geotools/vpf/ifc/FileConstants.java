package org.geotools.vpf.ifc;

/**
 * FileConstants.java
 *
 *
 * Created: Fri Dec 27 23:16:01 2002
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version $Id: FileConstants.java,v 1.1 2003/01/18 13:31:12 kobit Exp $
 */

public interface FileConstants 
{

  // Key types
  /**
   * <code>KEY_PRIMARY</code> stores code for primary key identification.
   */
  public static final char KEY_PRIMARY    = 'P';
  /**
   * <code>KEY_UNIQUE</code> stores code for unique key identification.
   */
  public static final char KEY_UNIQUE     = 'U';
  /**
   *  <code>KEY_NON_UNIQUE</code> stores code for non unique key identification.
   */
  public static final char KEY_NON_UNIQUE = 'N';

  // Column conditions
  /**
   * <code>COLUMN_OPTIONAL</code> stores code for optional column.
   */
  public static final String COLUMN_OPTIONAL    = "O";
  /**
   * <code>COLUMN_OPTIONAL_FP</code> stores code for optional feature
   * pointer column.
   */
  public static final String COLUMN_OPTIONAL_FP = "OF";
  /**
   * <code>COLUMN_MANDATORY</code> stores code for mandatory column.
   */
  public static final String COLUMN_MANDATORY   = "M";
  /**
   * <code>COLUMN_MANDATORY_AT_LEVEL_0</code> stores code for mandatory column
   * at topology level 0.
   */
  public static final String COLUMN_MANDATORY_AT_LEVEL_0 = "M0";
  /**
   * <code>COLUMN_MANDATORY_AT_LEVEL_1</code> stores code for mandatory column
   * at topology level 1.
   */
  public static final String COLUMN_MANDATORY_AT_LEVEL_1 = "M1";
  /**
   * <code>COLUMN_MANDATORY_AT_LEVEL_2</code> stores code for mandatory column
   * at topology level 2.
   */
  public static final String COLUMN_MANDATORY_AT_LEVEL_2 = "M2";
  /**
   * <code>COLUMN_MANDATORY_AT_LEVEL_3</code> stores code for mandatory column
   * at topology level 3.
   */
  public static final String COLUMN_MANDATORY_AT_LEVEL_3 = "M3";
  /**
   * <code>COLUMN_MANDATORY_IF_TILES</code> stores code for mandatory column
   * if tiles exists.
   */
  public static final String COLUMN_MANDATORY_IF_TILES   = "MT";

  // Table reserved names
  public static final String COVERAGE_ATTRIBUTE_TABLE = "cat";
  public static final String TABLE_CAT = COVERAGE_ATTRIBUTE_TABLE;
  public static final String CONNECTED_NODE_PRIMITIVE = "cnd";
  public static final String TABLE_CND = CONNECTED_NODE_PRIMITIVE;
  public static final String CONNECTED_NODE_SPATIAL_INDEX = "csi";
  public static final String TABLE_CSI = CONNECTED_NODE_SPATIAL_INDEX;
  public static final String DATABASE_HEADER_TABLE = "dht";
  public static final String TABLE_DHT = DATABASE_HEADER_TABLE;
  public static final String DATA_QUALITY_TABLE = "dqt";
  public static final String TABLE_DQT = DATA_QUALITY_TABLE;
  public static final String EDGE_BOUNDING_RECTANGLE = "ebr";
  public static final String TABLE_EBR = EDGE_BOUNDING_RECTANGLE;
  public static final String EDGE_PRIMITIVE = "edg";
  public static final String TABLE_EDG = EDGE_PRIMITIVE;
  public static final String ENTITY_NODE_PRIMITIVE = "end";
  public static final String TABLE_END = ENTITY_NODE_PRIMITIVE;
  public static final String EDGE_SPATIAL_INDEX = "esi";
  public static final String TABLE_ESI = EDGE_SPATIAL_INDEX;
  public static final String FACE_PRIMITIVE = "fac";
  public static final String TABLE_FAC = FACE_PRIMITIVE;
  public static final String FACE_BOUNDING_RECTANGLE = "fbr";
  public static final String TABLE_FBR = FACE_BOUNDING_RECTANGLE;
  public static final String FEATURE_CLASS_ATTRIBUTE_TABLE = "fca";
  public static final String TABLE_FCA = FEATURE_CLASS_ATTRIBUTE_TABLE;
  public static final String FEATURE_CLASS_SCHEMA_TABLE = "fcs";
  public static final String TABLE_FCS = FEATURE_CLASS_SCHEMA_TABLE;
  public static final String FACE_SPATIAL_INDEX = "fsi";
  public static final String TABLE_FSI = FACE_SPATIAL_INDEX;
  public static final String GEOGRAPHIC_REFERENCE_TABLE = "grt";
  public static final String TABLE_GRT = GEOGRAPHIC_REFERENCE_TABLE;
  public static final String LIBRARY_ATTTIBUTE_TABLE = "lat";
  public static final String TABLE_LAT = LIBRARY_ATTTIBUTE_TABLE;
  public static final String LIBRARY_HEADER_TABLE = "lht";
  public static final String TABLE_LHT = LIBRARY_HEADER_TABLE;
  public static final String ENTITY_NODE_SPATIAL_INDEX = "nsi";
  public static final String TABLE_NSI = ENTITY_NODE_SPATIAL_INDEX;
  public static final String RING_TABLE = "rng";
  public static final String TABLE_RNG = RING_TABLE;
  public static final String TEXT_PRIMITIVE = "txt";
  public static final String TABLE_TXT = TEXT_PRIMITIVE;
  public static final String TEXT_SPATIAL_INDEX = "tsi";
  public static final String TABLE_TSI = TEXT_SPATIAL_INDEX;
  public static final String CHARACTER_VALUE_DESCRIPTION_TABLE = "char.vdt";
  public static final String TABLE_CHAR = CHARACTER_VALUE_DESCRIPTION_TABLE;
  public static final String INTEGER_VALUE_DESCRIPTION_TABLE = "int.vdt";
  public static final String TABLE_INT = INTEGER_VALUE_DESCRIPTION_TABLE;

  // Table reserved extensions
  public static final String AREA_BOUMDING_RECTANGLE_TABLE = ".abr";
  public static final String EXT_ABR = AREA_BOUMDING_RECTANGLE_TABLE;
  public static final String AREA_FEATURE_TABLE = ".aft";
  public static final String EXT_AFT = AREA_FEATURE_TABLE;
  public static final String AREA_JOIN_TABLE = ".ajt";
  public static final String EXT_AJT = AREA_JOIN_TABLE;
  public static final String AREA_THEMATIC_INDEX = ".ati";
  public static final String EXT_ATI = AREA_THEMATIC_INDEX;
  public static final String COMPLEX_BOUNDING_RECTANGLE_TABLE = ".cbr";
  public static final String EXT_CBR = COMPLEX_BOUNDING_RECTANGLE_TABLE;
  public static final String COMPLEX_FEATURE_TABLE = ".cft";
  public static final String EXT_CFT = COMPLEX_FEATURE_TABLE;
  public static final String COMPLEX_JOIN_TABLE = ".cjt";
  public static final String EXT_CJT = COMPLEX_JOIN_TABLE;
  public static final String COMPLEX_THEMATIC_INDEX = ".cti";
  public static final String EXT_CTI = COMPLEX_THEMATIC_INDEX;
  public static final String NARRATIVE_TABLE = ".doc";
  public static final String EXT_DOC = NARRATIVE_TABLE;
  public static final String DIAGNOSITC_POINT_TABLE = ".dpt";
  public static final String EXT_DPT = DIAGNOSITC_POINT_TABLE;
  public static final String FEATURE_INDEX_TABLE = ".fit";
  public static final String EXT_FIT = FEATURE_INDEX_TABLE;
  public static final String FEATURE_THEMATIC_INDEX = ".fti";
  public static final String EXT_FTI = FEATURE_THEMATIC_INDEX;
  public static final String JOIN_THEMATIC_INDEX = ".jti";
  public static final String EXT_JTI = JOIN_THEMATIC_INDEX;
  public static final String LINE_BOUNDING_RECTANGLE_TABLE = ".lbr";
  public static final String EXT_LBR = LINE_BOUNDING_RECTANGLE_TABLE;
  public static final String LINE_FEATURE_TABLE = ".lft";
  public static final String EXT_LFT = LINE_FEATURE_TABLE;
  public static final String LINE_JOIN_TABLE = ".ljt";
  public static final String EXT_LJT = LINE_JOIN_TABLE;
  public static final String LINE_THEMATIC_INDEX = ".lti";
  public static final String EXT_LTI = LINE_THEMATIC_INDEX;
  public static final String POINT_BOUNDING_RECTANGLE_TABLE = ".pbr";
  public static final String EXT_PBR = POINT_BOUNDING_RECTANGLE_TABLE;
  public static final String POINT_FEATURE_TABLE = ".pft";
  public static final String EXT_PFT = POINT_FEATURE_TABLE;
  public static final String POINT_JOIN_TABLE = ".pjt";
  public static final String EXT_PJT = POINT_JOIN_TABLE;
  public static final String POINT_THEMATIC_INDEX = ".pti";
  public static final String EXT_PTI = POINT_THEMATIC_INDEX;
  public static final String RELATED_ATTRIBUTE_TABLE = ".rat";
  public static final String EXT_RAT = RELATED_ATTRIBUTE_TABLE;
  public static final String REGISTRATION_POINT_TABLE = ".rpt";
  public static final String EXT_RPT = REGISTRATION_POINT_TABLE;
  public static final String TEXT_FEATURE_TABLE = ".tft";
  public static final String EXT_TFT = TEXT_FEATURE_TABLE;
  public static final String TEXT_THEMATIC_TABLE = ".tti";
  public static final String EXT_TTI = TEXT_THEMATIC_TABLE;

  // Reserved directory names
  public static final String LIBRARY_REFERENCE_COVERAGE = "libref";
  public static final String DIR_LIBREF = LIBRARY_REFERENCE_COVERAGE;
  public static final String DATA_QUALITY_COVERAGE = "dq";
  public static final String DIR_DQ = DATA_QUALITY_COVERAGE;
  public static final String TILE_REFERENCE_COVERAGE = "tileref";
  public static final String DIR_TILEREF = TILE_REFERENCE_COVERAGE;
  public static final String NAMES_REFERENCE_COVERAGE = "gazette";
  public static final String DIR_GAZETTE = NAMES_REFERENCE_COVERAGE;
  
}// FileConstants
