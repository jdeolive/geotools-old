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
package org.geotools.vpf.exc;

import java.io.IOException;



/**
 * VPFDataException.java
 *
 *
 * Created: Mon Mar 03 21:32:32 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: VPFDataException.java,v 1.1 2003/03/03 20:40:30 kobit Exp $
 */
public class VPFDataException extends IOException 
{
  public VPFDataException()
  {
	super();
  } // VPFDataException constructor

  public VPFDataException(String msg)
  {
	super(msg);
  }
  
} // VPFDataException
