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

import java.io.PushbackInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import org.geotools.vpf.ifc.DataTypesDefinition;
import org.geotools.vpf.ifc.FileConstants;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;

/**
 * VPFInputStream.java
 *
 *
 * Created: Mon Feb 24 22:39:57 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFInputStream.java,v 1.1 2003/02/24 22:46:36 kobit Exp $
 */
public abstract class VPFInputStream extends InputStream 
  implements FileConstants, DataTypesDefinition
{

  protected List rowsReadAhead = new LinkedList();
  protected String streamFile = null;
  protected PushbackInputStream input = null;
  protected char byteOrder = LITTLE_ENDIAN_ORDER;
  
  protected VPFHeader header = null;
  
  public VPFInputStream(String file)
    throws IOException
  {
    this.streamFile = file;
    input = new PushbackInputStream(new FileInputStream(streamFile));
    header = readHeader();
  }

  public VPFInputStream(String file, char byteOrder)
    throws IOException
  {
	this.streamFile = file;
	this.byteOrder = byteOrder;
	input = new PushbackInputStream(new FileInputStream(streamFile));
    header = readHeader();
  } // VariableIndexInputStream constructor

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

  public abstract VPFHeader readHeader() throws IOException;

  public abstract VPFRow readRow() throws IOException;

  public abstract VPFRow readRow(int index) throws IOException;

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
    VPFRow row = readRow(fromIndex);
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
		input.unread(ctrl);
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

  public int availableRows()
  {
    return rowsReadAhead != null ? rowsReadAhead.size() : 0;
  }

  public int read()
  {
    return -1;
  }

  public void close() throws IOException
  {
    input.close();
    input = null;
  }
  
} // VPFInputStream
