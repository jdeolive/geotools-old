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
 * SpatialIndexHeader.java
 *
 *
 * Created: Tue Mar 11 23:42:48 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: SpatialIndexHeader.java,v 1.3 2003/03/19 21:36:29 kobit Exp $
 */
public class SpatialIndexHeader implements VPFHeader {
  
  public static final int SPATIAL_INDEX_HEADER_LENGTH = 24;

  protected int numPrims = 0;
  protected float xmin = 0;
  protected float ymin = 0;
  protected float xmax = 0;
  protected float ymax = 0;
  protected int numNodes = 0;

  public SpatialIndexHeader(int numPrims, float xmin, float ymin,
							float xmax, float ymax, int numNodes)
  {
	this.numPrims = numPrims;
	this.xmin = xmin;
	this.ymin = ymin;
	this.xmax = xmax;
	this.ymax = ymax;
  } // SpatialIndexHeader constructor
  
  public int getLength()
  {
	return SPATIAL_INDEX_HEADER_LENGTH;
  }
  
  /**
   * Gets the value of numPrims
   *
   * @return the value of numPrims
   */
  public int getNumPrims() 
  {
	return this.numPrims;
  }

//   /**
//    * Sets the value of numPrims
//    *
//    * @param argNumPrims Value to assign to this.numPrims
//    */
//   public void setNumPrims(int argNumPrims)
//   {
// 	this.numPrims = argNumPrims;
//   }

  /**
   * Gets the value of xmin
   *
   * @return the value of xmin
   */
  public float getXmin() 
  {
	return this.xmin;
  }

//   /**
//    * Sets the value of xmin
//    *
//    * @param argXmin Value to assign to this.xmin
//    */
//   public void setXmin(float argXmin)
//   {
// 	this.xmin = argXmin;
//   }

  /**
   * Gets the value of ymin
   *
   * @return the value of ymin
   */
  public float getYmin() 
  {
	return this.ymin;
  }

//   /**
//    * Sets the value of ymin
//    *
//    * @param argYmin Value to assign to this.ymin
//    */
//   public void setYmin(float argYmin)
//   {
// 	this.ymin = argYmin;
//   }

  /**
   * Gets the value of xmax
   *
   * @return the value of xmax
   */
  public float getXmax() 
  {
	return this.xmax;
  }

//   /**
//    * Sets the value of xmax
//    *
//    * @param argXmax Value to assign to this.xmax
//    */
//   public void setXmax(float argXmax)
//   {
// 	this.xmax = argXmax;
//   }

  /**
   * Gets the value of ymax
   *
   * @return the value of ymax
   */
  public float getYmax() 
  {
	return this.ymax;
  }

//   /**
//    * Sets the value of ymax
//    *
//    * @param argYmax Value to assign to this.ymax
//    */
//   public void setYmax(float argYmax)
//   {
// 	this.ymax = argYmax;
//   }

  /**
   * Gets the value of numNodes
   *
   * @return the value of numNodes
   */
  public int getNumNodes() 
  {
	return this.numNodes;
  }

//   /**
//    * Sets the value of numNodes
//    *
//    * @param argNumNodes Value to assign to this.numNodes
//    */
//   public void setNumNodes(int argNumNodes)
//   {
// 	this.numNodes = argNumNodes;
//   }

} // SpatialIndexHeader
