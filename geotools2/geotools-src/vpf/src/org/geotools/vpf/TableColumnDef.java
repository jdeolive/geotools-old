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

import org.geotools.vpf.ifc.DataTypesDefinition;

/**
 * This class contains definition of VPF standard table column definition
 * according to specification found in:
 * "Interface Standard for Vector Product Format." Objects of this
 * type are immutable.
 *
 *
 * Created: Thu Jan 02 23:11:27 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableColumnDef implements DataTypesDefinition
{

  protected String name = null;
  protected char type = CHAR_NULL_VALUE;
  protected int elementsNumber = 0;
  protected char keyType = CHAR_NULL_VALUE;
  protected String colDesc = null;
  protected String valDescTableName = null;
  protected String thematicIdx = null;
  protected String narrTable = null;

  public TableColumnDef(String name, char type, int elementsNumber,
                        char keyType, String colDesc, String valDescTableName,
                        String thematicIdx, String narrTable)
  {
    this.name = name;
    this.type = type;
    this.elementsNumber = elementsNumber;
    this.keyType = keyType;
    this.colDesc = colDesc;
    this.valDescTableName = valDescTableName;
    this.thematicIdx = thematicIdx;
    this.narrTable = narrTable;
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer(" ["+getClass().getName());
    buff.append(" (name="+name+")");
    buff.append(" (type="+type+")");
    buff.append(" (elementsNumber="+elementsNumber+")");
    buff.append(" (keyType="+keyType+")");
    buff.append(" (colDesc="+colDesc+")");
    buff.append(" (valDescTableName="+valDescTableName+")");
    buff.append(" (thematicIdx="+thematicIdx+")");
    buff.append(" (narrTable="+narrTable+")");
    buff.append("]");
    return buff.toString();
  }

  /**
   * Gets the value of name
   *
   * @return the value of name
   */
  public String getName() 
  {
    return this.name;
  }

  /**
   * Gets the value of type
   *
   * @return the value of type
   */
  public char getType() 
  {
    return this.type;
  }

  /**
   * Gets the value of elementsNumber
   *
   * @return the value of elementsNumber
   */
  public int getElementsNumber() 
  {
    return this.elementsNumber;
  }

  /**
   * Gets the value of keyType
   *
   * @return the value of keyType
   */
  public char getKeyType() 
  {
    return this.keyType;
  }

  /**
   * Gets the value of colDesc
   *
   * @return the value of colDesc
   */
  public String getColDesc() 
  {
    return this.colDesc;
  }

  /**
   * Gets the value of valDescTableName
   *
   * @return the value of valDescTableName
   */
  public String getValDescTableName() 
  {
    return this.valDescTableName;
  }

  /**
   * Gets the value of thematicIdx
   *
   * @return the value of thematicIdx
   */
  public String getThematicIdx() 
  {
    return this.thematicIdx;
  }

  /**
   * Gets the value of narrTable
   *
   * @return the value of narrTable
   */
  public String getNarrTable() 
  {
    return this.narrTable;
  }

  public int getColumnSize()
  {
	int size = -1;
	switch (type) {
	  case DATA_TEXT:
	  case DATA_LEVEL1_TEXT:
	  case DATA_LEVEL2_TEXT:
	  case DATA_LEVEL3_TEXT:
		size = elementsNumber;
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
	  case DATA_TRIPLED_ID:
		size = DATA_TRIPLED_ID_LEN;
	  default:
		break;
	} // end of switch (type)
	return size;
  }

  public boolean isNumeric()
  {
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
	  case DATA_TRIPLED_ID:
		return true;
	  default:
		return false;
	} // end of switch (type)
  }

} // TableColumnDef
