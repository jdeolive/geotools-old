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

import org.geotools.vpf.ifc.VPFHeader;

/**
 * VariableIndexHeader.java
 *
 *
 * Created: Tue Mar 11 23:41:57 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VariableIndexHeader.java,v 1.2 2003/03/16 22:59:39 kobit Exp $
 */
public class VariableIndexHeader implements VPFHeader {

  public static final int VARIABLE_INDEX_HEADER_LENGTH = 8;
  
  protected int entriesNumber = 0;
  protected int vpfHeaderLen = 0;

  public VariableIndexHeader(int entriesNumber, int vpfHeaderLen)
  {
	this.entriesNumber = entriesNumber;
	this.vpfHeaderLen = vpfHeaderLen;
  } // VariableIndexHeader constructor

  public int getLength()
  {
	return VARIABLE_INDEX_HEADER_LENGTH;
  }
  
  /**
   * Gets the value of <code>entriesNumber</code>
   *
   * @return the value of <code>entriesNumber</code>
   */
  public int getEntriesNumber() 
  {
	return this.entriesNumber;
  }

//   /**
//    * Sets the value of entriesNumber
//    *
//    * @param argEntriesNumber Value to assign to this.entriesNumber
//    */
//   public void setEntriesNumber(int argEntriesNumber)
//   {
// 	this.entriesNumber = argEntriesNumber;
//   }

  /**
   * Gets the value of <code>vpfHeaderLen</code>
   *
   * @return the value of <code>vpfHeaderLen</code>
   */
  public int getVpfHeaderLen() 
  {
	return this.vpfHeaderLen;
  }

//   /**
//    * Sets the value of vpfHeaderLen
//    *
//    * @param argVpfHeaderLen Value to assign to this.vpfHeaderLen
//    */
//   public void setVpfHeaderLen(int argVpfHeaderLen)
//   {
// 	this.vpfHeaderLen = argVpfHeaderLen;
//   }
  
} // VariableIndexHeader
