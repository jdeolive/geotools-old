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
 * SpatialIndexInputStream.java
 *
 *
 * Created: Mon Feb 24 22:25:15 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: SpatialIndexInputStream.java,v 1.4 2003/03/19 21:36:29 kobit Exp $
 */
public class SpatialIndexInputStream extends VPFInputStream 
{

  public static final long SPATIAL_INDEX_ROW_SIZE = 8;
  
  public SpatialIndexInputStream(String file, char byteOrder)
	throws IOException
  {
	super(file, byteOrder);
  }
  
  public int tableSize()
  {
	return -1;
  }

  public VPFHeader readHeader() throws IOException
  {
	return new SpatialIndexHeader(readInteger(), readFloat(), readFloat(),
								  readFloat(), readFloat(), readInteger());
  }

  public VPFRow readRow() throws IOException
  {
	return null;
  }

  public void setPosition(long pos) throws IOException
  {
	seek(SPATIAL_INDEX_ROW_SIZE*pos);
  }
  
} // SpatialIndexInputStream
