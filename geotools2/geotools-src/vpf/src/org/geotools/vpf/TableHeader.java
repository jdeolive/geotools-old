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
import org.geotools.vpf.ifc.VPFHeader;
import java.util.List;
import java.util.Collections;

/**
 * This class contains definition of VPF standard table header
 * according to specification found in:
 * "Interface Standard for Vector Product Format." Objects of this
 * type are immutable.
 * 
 *
 * Created: Thu Jan 02 22:50:59 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableHeader
  implements VPFHeader, DataTypesDefinition
{

  /**
   * Variable <code>length</code> keeps value of length of ASCII header
   * string (i.e., the remaining information after this field)
   */
  protected int headerLength = -0;
  /**
   * Variable <code>byteOrder</code> keeps value of 
   * byte order in which table is written:
   * <ul>
   * <li><b>L</b> - least-significant-first</li>
   * <li><b>M</b> - most-significant-first</li>
   * </ul>
   */
  protected char byteOrder = LEAST_SIGNIF_FIRST;
  /**
   * Variable <code>description</code> keeps value of text
   * description of the table's contents.
   */
  protected String description = null;
  /**
   * Variable <code>narrativeTable</code> keeps value of 
   * an optional narrative file which contains miscellaneous
   * information about the table.
   */
  protected String narrativeTable = null;
  /**
   * Variable <code>columnDefs</code> keeps value of list of
   * all column definitions found in table header. This list keeps
   * objects of type <code>TableColumnDef</code> class.
   *
   */
  protected List columnDefs = null;
  
  /**
   * Creates a new <code>TableHeader</code> instance.
   *
   * @param length an <code>int</code> value of table header length.
   * @param byteOrder a <code>char</code> value byte order used in table file.
   * @param description a <code>String</code> value text description of found
   * in header of this table.
   * @param narrativeTable a <code>String</code> value file name of narrative
   * table.
   * @param columnDefs a <code>List</code> value of all column definitions for
   * this table.
   */
  public TableHeader(int length, char byteOrder, String description,
                     String narrativeTable, List columnDefs)
  {
    this.headerLength = length;
    this.byteOrder = byteOrder;
    this.description = description;
    this.narrativeTable = narrativeTable;
    this.columnDefs = columnDefs;
  }

  /**
   * Method <code>toString</code> returns content of all fields
   * values. Used only for test and debug purpose.
   *
   * @return a <code>String</code> value
   */
  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append(" length="+headerLength+"\n");
    buff.append(" byteOrder="+byteOrder+"\n");
    buff.append(" description="+description+"\n");
    buff.append(" narrativeTable="+narrativeTable+"\n");
    buff.append(" columnDefs:");
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
      buff.append("\n");
    } // end of if (columnDefs == null) else
    return buff.toString();
  }

  /**
   * Gets the value of full length of ASCII header string
   * including <code>headerLength</code> field.
   * @return the value of headerLength
   */
  public int getLength() 
  {
    return this.headerLength+4;
  }

  /**
   * Method <code><code>getRecordSize</code></code> is used to return
   * size in bytes of records stored in this table. If table keeps variable
   * length records <code>-1</code> should be returned.
   *
   * @return an <code><code>int</code></code> value
   */
  public int getRecordSize()
  {
    int size = 0;
    for (int i = 0; i < columnDefs.size(); i++)
    {
      TableColumnDef colDef = (TableColumnDef)columnDefs.get(i);
//       System.out.println("Column def no. "+i+" column size: "+
//                          colDef.getColumnSize());
      if (colDef.getColumnSize() < 0)
      {
        return -1;
      } // end of if (colDef.getColumnSize() <= 0)
      else
      {
        size += colDef.getColumnSize();
      } // end of if (colDef.getColumnSize() <= 0) else
    } // end of for (int i = 0; i < columnDefs.size(); i++)
    return size;
  }
  
  /**
   * Gets the value of byteOrder variable.
   * Byte order in which table is written:
   * <ul>
   * <li><b>L</b> - least-significant-first</li>
   * <li><b>M</b> - most-significant-first</li>
   * </ul>
   * @return the value of byteOrder
   */
  public char getByteOrder() 
  {
    return this.byteOrder;
  }

  /**
   * Gets the value of the description of table content.
   *
   * @return the value of description
   */
  public String getDescription() 
  {
    return this.description;
  }

  /**
   * Gets the value of narrativeTable variable file name.
   *
   * @return the value of narrativeTable
   */
  public String getNarrativeTable() 
  {
    return this.narrativeTable;
  }

  /**
   * Gets the value of columnDefs variable keeping
   * definitions of all columns in this table.
   * @return the value of columnDefs
   */
  public List getColumnDefs() 
  {
    return Collections.unmodifiableList(this.columnDefs);
  }

} // TableHeader
