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

import java.io.InputStream;
import java.io.IOException;
import org.geotools.vpf.ifc.VPFHeader;
import org.geotools.vpf.ifc.VPFRow;

/**
 * VariableIndexInputStream.java
 *
 *
 * Created: Mon Feb 24 22:23:58 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexInputStream.java,v 1.2 2003/03/11 22:35:47 kobit Exp $
 */
public class VariableIndexInputStream extends VPFInputStream 
{

  protected int entriesNumber = 0;
  protected int vpfHeaderLen = 0;

  public VariableIndexInputStream(String file, char byteOrder)
	throws IOException
  {
	super(file, byteOrder);
  }
  
  public int tableSize()
  {
	return entriesNumber;
  }

  public VPFHeader readHeader() throws IOException
  {
	return null;
  }

  public VPFRow readRow() throws IOException
  {
	return null;
  }

  public VPFRow readRow(int index) throws IOException
  {
	return null;
  }

} // VariableIndexInputStream
