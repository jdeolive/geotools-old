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
import java.io.DataInputStream;
import java.util.LinkedList;
import java.util.List;
import org.geotools.vpf.TableHeader;
import org.geotools.vpf.TableColumnDef;
import org.geotools.vpf.TableRow;
import org.geotools.vpf.exc.VPFHeaderFormatException;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.DataTypesDefinition;

/**
 * TableInputStream.java
 *
 *
 * Created: Thu Jan 02 22:32:27 2003
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableInputStream extends InputStream
  implements FileConstants, DataTypesDefinition
{
  public static final int AHEAD_BUFFER_SIZE = 0;
  
  protected DataInputStream input = null;
  protected TableHeader header = null;
  protected List rowsReadAhead = new LinkedList();
  
  public TableInputStream(File file)
    throws IOException
  {
    input = new DataInputStream(new FileInputStream(file));
    readHeader();
  }

  public TableInputStream(FileDescriptor fdObj)
    throws IOException
  {
    input = new DataInputStream(new FileInputStream(fdObj));
    readHeader();
  }

  public TableInputStream(String file)
    throws IOException
  {
    input = new DataInputStream(new FileInputStream(file));
    readHeader();
  }

  public TableHeader getHeader()
  {
    return header;
  }

  protected void readHeader()
    throws IOException
  {
    byte[] fourBytes = new byte[4];
    int res = input.read(fourBytes);
    char order = readChar();
    int length = 0;
    if (order == LITTLE_ENDIAN_ORDER)
    {
      length = littleEndianToInt(fourBytes);
    } // end of if (order == LITTLE_ENDIAN_ORDER)
    else
    {
      length = bigEndianToInt(fourBytes);
    } // end of if (order == LITTLE_ENDIAN_ORDER) else
    char ctrl = readChar();
    if (ctrl != VPF_RECORD_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String description = readString(""+VPF_RECORD_SEPARATOR);
    String narrativeTable = readString(""+VPF_RECORD_SEPARATOR);
    LinkedList colDefs = new LinkedList();
    TableColumnDef colDef = readColumnDef();
    while (colDef != null)
    {
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
    header = new TableHeader(length, order, description,
                             narrativeTable, colDefs);
  }

  public TableColumnDef readColumnDef()
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
    int elements = Integer.parseInt(readString(""+VPF_ELEMENT_SEPARATOR));
    char key = readChar();
    ctrl = readChar();
    if (ctrl != VPF_ELEMENT_SEPARATOR)
    {
      throw new VPFHeaderFormatException("Header format does not fit VPF"+
                                         " file definition.");
    } // end of if (ctrl != VPF_RECORD_SEPARATOR)
    String colDesc = readString(""+VPF_ELEMENT_SEPARATOR);
    String descTableName = readString(""+VPF_ELEMENT_SEPARATOR);
    String indexFile = readString(""+VPF_ELEMENT_SEPARATOR);
    String narrTable = readString(""+VPF_ELEMENT_SEPARATOR);
    return new TableColumnDef(name, type, elements, key, colDesc,
                              descTableName, indexFile, narrTable);
  }

  public TableRow readRow()
  {
    return null;
  }

  public int readRows(TableRow[] rows)
  {
    return 0;
  }

  public int read()
  {
    return -1;
  }

  public int availableRows()
  {
    return rowsReadAhead.size();
  }

  protected char readChar()
    throws IOException
  {
    return (char)input.readByte();
  }

  protected String readString(String terminators)
    throws IOException
  {
    StringBuffer text = new StringBuffer();
    char ctrl = readChar();
    while (terminators.indexOf(ctrl) == -1)
    {
      text.append(ctrl);
      ctrl = readChar();
    } // end of while (terminators.indexOf(ctrl) != -1)
    if (text.toString().equals(STRING_NULL_VALUE))
    {
      return null;
    } // end of if (text.equals("null"))
    else
    {
      return text.toString();
    } // end of if (text.equals("null")) else
  }

  public static int littleEndianToInt(byte[] fourBytes)
  {
    int res = 0;
    int limit = Math.min(fourBytes.length, 4);
    for (int i = 0; i < limit; i++)
    {
      res += unsigByteToInt(fourBytes[i]) << (i*8);
    } // end of for (int i = 0; i < limit; i++)
    return res;
  }

  public static int bigEndianToInt(byte[] fourBytes)
  {
    int res = 0;
    int limit = Math.min(fourBytes.length, 4);
    for (int i = 0; i < limit; i++)
    {
      res += unsigByteToInt(fourBytes[i]) << ((limit-(i+1))*8);
    } // end of for (int i = 0; i < limit; i++)
    return res;
  }

  public static int unsigByteToInt(byte b)
  {
    return (int) b & 0xFF;
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
    TableHeader testHeader = testInput.getHeader();
    System.out.println(testHeader.toString());
  } // end of main()
  
} // TableInputStream
