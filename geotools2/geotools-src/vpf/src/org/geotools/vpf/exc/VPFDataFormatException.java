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

import java.lang.Exception;



/**
 * Class VPFDataFormatException.java is responsible for 
 *
 * <p>
 * Created: Wed Jan 29 10:28:53 2003
 * </p>
 * @version $Id: VPFDataFormatException.java,v 1.1 2003/01/29 16:15:01 kobit Exp $
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 */

public class VPFDataFormatException extends RuntimeException 
{

  public VPFDataFormatException()
  {
    super();
  }
  
  public VPFDataFormatException(String message)
  {
    super(message);
  }
  
  public VPFDataFormatException(String message, Throwable cause)
  {
    super(message, cause);
  }
  
  public VPFDataFormatException(Throwable cause)
  {
    super(cause);
  }
  
}// VPFDataFormatException
