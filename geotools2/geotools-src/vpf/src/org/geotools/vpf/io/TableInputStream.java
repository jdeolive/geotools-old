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

package org.geotools.vpf.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import org.geotools.vpf.TableColumnDef;
import org.geotools.vpf.TableHeader;
import org.geotools.vpf.TableRow;
import org.geotools.vpf.RowField;
import org.geotools.vpf.VPFDate;
import org.geotools.vpf.util.DataUtils;
import org.geotools.vpf.exc.VPFHeaderFormatException;
import org.geotools.vpf.exc.VPFRowDataException;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;

/**
 * Class <code>TableInputStream</code> implements
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version $Id: TableInputStream.java,v 1.14 2003/03/26 15:19:53 kobit Exp $
 */
public class TableInputStream extends VPFInputStream
  implements FileConstants, DataTypesDefinition
{

  /**
   * Variable constant <code>AHEAD_BUFFER_SIZE</code> keeps value of 
   * number of records to read ahead and keep in cache to improve further
   * access to data.
   */
  public static final int AHEAD_BUFFER_SIZE = 0;

  public TableInputStream(String file)
    throws IOException
  {
	super(file);
  }

  public TableInputStream(String file, char byteOrder)
    throws IOException
  {
	super(file, byteOrder);
  }
  
  public VPFHeader readHeader()
    throws IOException
  {
    byte[] fourBytes = new byte[4];
    int res = input.read(fourBytes);
    char order = readChar();
    if (order == LITTLE_ENDIAN_ORDER)
    {
      fourBytes = DataUtils.toBigEndian(fourBytes);
    } // end of if (order == LITTLE_ENDIAN_ORDER)
    int length = DataUtils.decodeInt(fourBytes);
    char ctrl = readChar();
    if (ctrl != VPF_RECORD_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String description = readString(""+VPF_RECORD_SEPARATOR);
    String narrativeTable = readString(""+VPF_RECORD_SEPARATOR);
    ArrayList colDefs = new ArrayList();
    TableColumnDef colDef = readColumnDef();
    while (colDef != null)
    {
	  //	  System.out.println(colDef.toString());
      colDefs.add(colDef);
      ctrl = readChar();
      if (ctrl != VPF_FIELD_SEPARATOR)
      {
        throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                           " file definition.");
      } // end of if (ctrl != VPF_RECORD_SEPARATOR)
      colDef = readColumnDef();
    } // end of while (colDef != null)
    if (colDefs.size() == 0)
    {
      colDefs = null;
    } // end of if (colDefs.size() == 0)
    return new TableHeader(length, order, description,
						   narrativeTable, colDefs);
  }

  private TableColumnDef readColumnDef()
    throws IOException, NumberFormatException
  {
    char ctrl = readChar();
    if (ctrl == VPF_RECORD_SEPARATOR)
    {
      return null;
    } // end of if (ctrl == VPF_RECORD_SEPARATOR)
    String name = ctrl + readString("=");
    char type = readChar();
    ctrl = readChar();
    if (ctrl != VPF_ELEMENT_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String elemStr = readString(""+VPF_ELEMENT_SEPARATOR);
    if (elemStr.equals("*"))
    {
      elemStr = "-1";
    } // end of if (elemStr.equals("*"))
    int elements = Integer.parseInt(elemStr);
    char key = readChar();
    ctrl = readChar();
    if (ctrl != VPF_ELEMENT_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String colDesc =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String descTableName =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String indexFile =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    String narrTable =
	  readString(""+VPF_ELEMENT_SEPARATOR+VPF_FIELD_SEPARATOR);
    return new TableColumnDef(name, type, elements, key, colDesc,
                              descTableName, indexFile, narrTable);
  }

  public VPFRow readRow() throws IOException
  {
    //    condeb("Current file position: "+input.getFilePointer());
	List rowsDef = ((TableHeader)header).getColumnDefs();
    RowField[] fieldsArr = new RowField[rowsDef.size()];
    HashMap fieldsMap = new HashMap();
	for (int i = 0; i < rowsDef.size(); i++) {
	  TableColumnDef tcd = (TableColumnDef)rowsDef.get(i);
      //      condeb("Reading field: "+tcd.getName()+", columnSize="+tcd.getColumnSize());
      Object value = null;
      if (tcd.getColumnSize() < 0)
      {
        value = readVariableSizeData(tcd.getType());
      } // end of if (tcd.getColumnSize() <= 0)
      else
      {
        value = readFixedSizeData(tcd.getType(), tcd.getElementsNumber());
      } // end of if (tcd.getColumnSize() <= 0) else
      //  	  Object value = DataUtils.decodeData(bytes, tcd.getType());
      //      condeb("Read value="+value.toString());
      RowField field = new RowField(value, tcd.getType());
      fieldsArr[i] = field;
      fieldsMap.put(tcd.getName(), field);
	} // end of for (int i = 0; i < rowsDefs.size(); i++)
    return new TableRow(fieldsArr, fieldsMap);
  }

  public int tableSize()
  {
    return -1;
  }

  public static void main(String[] args)
    throws IOException
  {
    if (args.length != 1)
    {
      System.out.println("Put valid file name as parameter.");
      System.exit(1);
    } // end of if (args.length <> 1)
    TableInputStream testInput = new TableInputStream(args[0]);
    TableHeader testHeader = (TableHeader)testInput.getHeader();
    System.out.println(testHeader.toString());
	List fieldDefs = testHeader.getColumnDefs();
    TableRow row = (TableRow)testInput.readRow();
    int counter = 0;
    while (row != null)
    {
	  for (int i = 0; i < fieldDefs.size(); i++) {
		TableColumnDef tcd = (TableColumnDef)fieldDefs.get(i);
		System.out.println(tcd.getName()+"="+row.get(i).toString());
	  } // end of for (int i = 0; i < fieldDefs.size(); i++)
      row = (TableRow)testInput.readRow();
    } // end of while (row != null)
  } // end of main()
  
} // TableInputStream
