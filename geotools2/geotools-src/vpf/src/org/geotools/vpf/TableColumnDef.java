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
 * TableColumnDef.java
 *
 *
 * Created: Thu Jan 02 23:11:27 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableColumnDef 
  implements DataTypesDefinition
{

  protected String name = null;
  protected char type = '-';
  protected int elementsNumber = 0;
  protected char keyType = '-';
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

  /**
   * Gets the value of name
   *
   * @return the value of name
   */
  public String getName() 
  {
    return this.name;
  }

//   /**
//    * Sets the value of name
//    *
//    * @param argName Value to assign to this.name
//    */
//   public void setName(String argName)
//   {
//     this.name = argName;
//   }

  /**
   * Gets the value of type
   *
   * @return the value of type
   */
  public char getType() 
  {
    return this.type;
  }

//   /**
//    * Sets the value of type
//    *
//    * @param argType Value to assign to this.type
//    */
//   public void setType(char argType)
//   {
//     this.type = argType;
//   }

  /**
   * Gets the value of elementsNumber
   *
   * @return the value of elementsNumber
   */
  public int getElementsNumber() 
  {
    return this.elementsNumber;
  }

//   /**
//    * Sets the value of elementsNumber
//    *
//    * @param argElementsNumber Value to assign to this.elementsNumber
//    */
//   public void setElementsNumber(int argElementsNumber)
//   {
//     this.elementsNumber = argElementsNumber;
//   }

  /**
   * Gets the value of keyType
   *
   * @return the value of keyType
   */
  public char getKeyType() 
  {
    return this.keyType;
  }

//   /**
//    * Sets the value of keyType
//    *
//    * @param argKeyType Value to assign to this.keyType
//    */
//   public void setKeyType(char argKeyType)
//   {
//     this.keyType = argKeyType;
//   }

  /**
   * Gets the value of colDesc
   *
   * @return the value of colDesc
   */
  public String getColDesc() 
  {
    return this.colDesc;
  }

//   /**
//    * Sets the value of colDesc
//    *
//    * @param argColDesc Value to assign to this.colDesc
//    */
//   public void setColDesc(String argColDesc)
//   {
//     this.colDesc = argColDesc;
//   }

  /**
   * Gets the value of valDescTableName
   *
   * @return the value of valDescTableName
   */
  public String getValDescTableName() 
  {
    return this.valDescTableName;
  }

//   /**
//    * Sets the value of valDescTableName
//    *
//    * @param argValDescTableName Value to assign to this.valDescTableName
//    */
//   public void setValDescTableName(String argValDescTableName)
//   {
//     this.valDescTableName = argValDescTableName;
//   }

  /**
   * Gets the value of thematicIdx
   *
   * @return the value of thematicIdx
   */
  public String getThematicIdx() 
  {
    return this.thematicIdx;
  }

//   /**
//    * Sets the value of thematicIdx
//    *
//    * @param argThematicIdx Value to assign to this.thematicIdx
//    */
//   public void setThematicIdx(String argThematicIdx)
//   {
//     this.thematicIdx = argThematicIdx;
//   }

  /**
   * Gets the value of narrTable
   *
   * @return the value of narrTable
   */
  public String getNarrTable() 
  {
    return this.narrTable;
  }

//   /**
//    * Sets the value of narrTable
//    *
//    * @param argNarrTable Value to assign to this.narrTable
//    */
//   public void setNarrTable(String argNarrTable)
//   {
//     this.narrTable = argNarrTable;
//   }
  
} // TableColumnDef
