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

import org.geotools.vpf.ifc.VPFRow;

/**
 * VariableIndexRow.java
 *
 *
 * Created: Sun Mar 16 23:28:11 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexRow.java,v 1.1 2003/03/16 23:01:02 kobit Exp $
 */
public class VariableIndexRow implements VPFRow {

  protected int offset = 0;
  protected int size = 0;
  
  public VariableIndexRow(int offset, int size)
  {
	this.offset = offset;
	this.size = size;
  } // VariableIndexRow constructor
  
  /**
   * Gets the value of offset
   *
   * @return the value of offset
   */
  public int getOffset() 
  {
	return this.offset;
  }

//   /**
//    * Sets the value of offset
//    *
//    * @param argOffset Value to assign to this.offset
//    */
//   public void setOffset(int argOffset)
//   {
// 	this.offset = argOffset;
//   }

  /**
   * Gets the value of size
   *
   * @return the value of size
   */
  public int getSize() 
  {
	return this.size;
  }

//   /**
//    * Sets the value of size
//    *
//    * @param argSize Value to assign to this.size
//    */
//   public void setSize(int argSize)
//   {
// 	this.size = argSize;
//   }

} // VariableIndexRow
