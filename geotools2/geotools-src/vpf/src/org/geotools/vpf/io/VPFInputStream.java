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

import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;
import org.geotools.vpf.util.DataUtils;
import org.geotools.vpf.exc.VPFDataException;
import org.geotools.vpf.Coordinate2DFloat;

/**
 * VPFInputStream.java
 *
 *
 * Created: Mon Feb 24 22:39:57 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFInputStream.java,v 1.5 2003/03/24 16:38:24 kobit Exp $
 */
public abstract class VPFInputStream
  implements FileConstants, DataTypesDefinition
{

  protected List rowsReadAhead = new LinkedList();
  protected String streamFile = null;
  protected VPFInputStream variableIndex = null;
  protected RandomAccessFile input = null;
  protected char byteOrder = LITTLE_ENDIAN_ORDER;
  protected String accessMode = "r";
  
  protected VPFHeader header = null;
  
  public abstract VPFHeader readHeader() throws IOException;

  public abstract VPFRow readRow() throws IOException;

  public abstract int tableSize() throws IOException;

  public VPFInputStream(String file)
    throws IOException
  {
    this.streamFile = file;
    input = new RandomAccessFile(streamFile, accessMode);
    header = readHeader();
    System.out.println("("+streamFile+
                       ") header.getRecordSize()="+header.getRecordSize());
    if (header.getRecordSize() < 0)
    {
      variableIndex =
        new VariableIndexInputStream(getVariableIndexFileName(),
                                     getByteOrder());
    } // end of if (header.getRecordSize() == -1)
  }

  public VPFInputStream(String file, char byteOrder)
    throws IOException
  {
	this.streamFile = file;
	this.byteOrder = byteOrder;
    input = new RandomAccessFile(streamFile, accessMode);
    header = readHeader();
  } // VariableIndexInputStream constructor

  public String getVariableIndexFileName()
  {
    if (streamFile.equals("fcs"))
    {
      return "fcz";
    } // end of if (streamFile.equals("fcs"))
    else
    {
      return streamFile.substring(0, streamFile.length()-1)+"x";
    } // end of else
  }

  public VPFHeader getHeader()
  {
    return header;
  }

  /**
   * Get the ByteOrder value.
   * @return the ByteOrder value.
   */
  public char getByteOrder()
  {
	return byteOrder;
  }

  /**
   * Set the ByteOrder value.
   * @param newByteOrder The new ByteOrder value.
   */
  public void setByteOrder(char newByteOrder)
  {
	this.byteOrder = newByteOrder;
  }

  protected void unread(long bytes) throws IOException
  {
	input.seek(input.getFilePointer()-bytes);
  }

  protected void seek(long pos) throws IOException
  {
	input.seek(pos);
  }

  public void setPosition(long pos) throws IOException
  {
    if (header.getRecordSize() == -1)
    {
      VariableIndexRow varRow =
        (VariableIndexRow)variableIndex.readRow((int)pos);
      System.out.println("Variable index info:\noffset="+varRow.getOffset()+
                         "\nsize="+varRow.getSize());
      seek(varRow.getOffset());
    } // end of if (header.getRecordSize() == -1)
    else
    {
      seek(header.getLength()+(pos-1)*header.getRecordSize());
    } // end of if (header.getRecordSize() == -1) else
  }

  public VPFRow readRow(int index) throws IOException
  {
	setPosition(index);
	return readRow();
  }

  public int readRows(VPFRow[] rows) throws IOException
  {
    int counter = 0;
    VPFRow row = readRow();
    while (row != null && counter < rows.length)
    {
      rows[counter++] = row;
      row = readRow();
    } // end of while (row != null)

    return counter;
  }

  public int readRows(VPFRow[] rows, int fromIndex) throws IOException
  {
    int counter = 0;
	setPosition(fromIndex);
    VPFRow row = readRow();
    while (row != null && counter < rows.length)
    {
      rows[counter++] = row;
      row = readRow();
    } // end of while (row != null)

    return counter;
  }

  protected char readChar() throws IOException
  {
    return (char)input.read();
  }

  protected String readString(String terminators)
    throws IOException
  {
    StringBuffer text = new StringBuffer();
    char ctrl = readChar();
	if (terminators.indexOf(ctrl) != -1)
	{
	  if (ctrl == VPF_FIELD_SEPARATOR) {
		unread(1);
	  } // end of if (ctrl == VPF_RECORD_SEPARATOR)
	  return null;
	}
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

  protected Object readVariableSizeData(char dataType)
    throws IOException
  {
    int instances = readInteger();
    System.out.println("Variable length instances: "+instances);
    return readFixedSizeData(dataType, instances);
  }
  
  protected Object readFixedSizeData(char dataType, int instancesCount)
    throws IOException
  {
    Object result = null;
    switch (dataType)
    {
      case DATA_TEXT:
	  case DATA_LEVEL1_TEXT:
	  case DATA_LEVEL2_TEXT:
	  case DATA_LEVEL3_TEXT:
	  case DATA_SHORT_FLOAT:
	  case DATA_LONG_FLOAT:
	  case DATA_SHORT_INTEGER:
	  case DATA_LONG_INTEGER:
        byte[] dataBytes =
          new byte[instancesCount*DataUtils.getDataTypeSize(dataType)];
        input.read(dataBytes);
        if (DataUtils.isNumeric(dataType) && byteOrder == LITTLE_ENDIAN_ORDER)
        {
          dataBytes = DataUtils.toBigEndian(dataBytes);
        }
        result = DataUtils.decodeData(dataBytes, dataType);
        break;

      case DATA_2_COORD_F:
        float[][] data = new float[instancesCount][2];
        for (int i = 0; i < instancesCount; i++)
        {
          data[i][0] = readFloat();
          data[i][1] = readFloat();
        } // end of for (int i = 0; i < instancesCount; i++)
        result = new Coordinate2DFloat(data);
        break;
        
      default:
        
        break;
    } // end of switch (dataType)
    return result;
    //     byte[] result = new byte[bytesCount];
    //     int size = input.read(result);
    //     if (size != bytesCount)
    //     {
    //       throw new VPFRowDataException("Insuffitient data in stream: is "+size+
    //                                     " should be: "+tcd.getColumnSize());
    //     } // end of if (size != tcd.getColumnSize())
    //     if (numeric && getByteOrder() == LITTLE_ENDIAN_ORDER)
    //     {
    //       result = DataUtils.toBigEndian(result);
    //     } // end of if (numeric)
    //     return result;
  }

  protected byte[] readNumber(int cnt) throws IOException
  {
	byte[] dataBytes = new byte[cnt];
	int res = input.read(dataBytes);
	if (res == cnt)
	{
	  if (byteOrder == LITTLE_ENDIAN_ORDER)
	  {
		dataBytes = DataUtils.toBigEndian(dataBytes);
	  } // end of if (byteOrder == LITTLE_ENDIAN_ORDER)
	  return dataBytes;
	} // end of if (res == cnt)
	else
	{
	  throw new VPFDataException("Inssufficient bytes in input stream");
	} // end of if (res == cnt) else
  }

  protected short readShort() throws IOException
  {
	return DataUtils.decodeShort(readNumber(DATA_SHORT_INTEGER_LEN));
  }

  protected int readInteger() throws IOException
  {
	return DataUtils.decodeInt(readNumber(DATA_LONG_INTEGER_LEN));
  }

  protected float readFloat() throws IOException
  {
	return DataUtils.decodeFloat(readNumber(DATA_SHORT_FLOAT_LEN));
  }

  protected double readDouble() throws IOException
  {
	return DataUtils.decodeDouble(readNumber(DATA_LONG_FLOAT_LEN));
  }

  public int availableRows()
  {
    return rowsReadAhead != null ? rowsReadAhead.size() : 0;
  }

  public void close() throws IOException
  {
    input.close();
    input = null;
  }
  
} // VPFInputStream
