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
import java.util.List;

/**
 * TableHeader.java
 *
 *
 * Created: Thu Jan 02 22:50:59 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableHeader
  implements DataTypesDefinition
{

  protected int length = -0;
  protected char byteOrder = LEAST_SIGNIF_FIRST;
  protected String description = null;
  protected String narrativeTable = null;
  protected List columnDefs = null;
  
  public TableHeader(int length, char byteOrder, String description,
                     String narrativeTable, List columnDefs)
  {
    this.length = length;
    this.byteOrder = byteOrder;
    this.description = description;
    this.narrativeTable = narrativeTable;
    this.columnDefs = columnDefs;
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer(" ["+getClass().getName());
    buff.append(" (length="+length+")");
    buff.append(" (byteOrder="+byteOrder+")");
    buff.append(" (description="+description+")");
    buff.append(" (narrativeTable="+narrativeTable+")");
    buff.append(" (columnDefs=");
    if (columnDefs == null)
    {
      buff.append("null)");
    } // end of if (columnDefs == null)
    else
    {
      for (int i = 0; i < columnDefs.size(); i++)
      {
        buff.append("\n"+columnDefs.get(i).toString());
      } // end of for (int i = 0; i < columnDefs.size(); i++)
      buff.append("\n)");
    } // end of if (columnDefs == null) else
    buff.append("]");
    return buff.toString();
  }

  /**
   * Gets the value of length
   *
   * @return the value of length
   */
  public int getLength() 
  {
    return this.length;
  }

//   /**
//    * Sets the value of length
//    *
//    * @param argLength Value to assign to this.length
//    */
//   public void setLength(int argLength)
//   {
//     this.length = argLength;
//   }

  /**
   * Gets the value of byteOrder
   *
   * @return the value of byteOrder
   */
  public char getByteOrder() 
  {
    return this.byteOrder;
  }

//   /**
//    * Sets the value of byteOrder
//    *
//    * @param argByteOrder Value to assign to this.byteOrder
//    */
//   public void setByteOrder(char argByteOrder)
//   {
//     this.byteOrder = argByteOrder;
//   }

  /**
   * Gets the value of description
   *
   * @return the value of description
   */
  public String getDescription() 
  {
    return this.description;
  }

//   /**
//    * Sets the value of description
//    *
//    * @param argDescription Value to assign to this.description
//    */
//   public void setDescription(String argDescription)
//   {
//     this.description = argDescription;
//   }

  /**
   * Gets the value of narrativeTable
   *
   * @return the value of narrativeTable
   */
  public String getNarrativeTable() 
  {
    return this.narrativeTable;
  }

//   /**
//    * Sets the value of narrativeTable
//    *
//    * @param argNarrativeTable Value to assign to this.narrativeTable
//    */
//   public void setNarrativeTable(String argNarrativeTable)
//   {
//     this.narrativeTable = argNarrativeTable;
//   }

  /**
   * Gets the value of columnDefs
   *
   * @return the value of columnDefs
   */
  public List getColumnDefs() 
  {
    return this.columnDefs;
  }

//   /**
//    * Sets the value of columnDefs
//    *
//    * @param argColumnDefs Value to assign to this.columnDefs
//    */
//   public void setColumnDefs(List argColumnDefs)
//   {
//     this.columnDefs = argColumnDefs;
//   }
  
} // TableHeader
