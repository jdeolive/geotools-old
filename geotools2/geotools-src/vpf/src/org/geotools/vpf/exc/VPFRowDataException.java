package org.geotools.vpf.exc;

import java.io.IOException;

/**
 * VPFRowDataException.java
 *
 *
 * Created: Mon Jan 27 21:29:00 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version
 */

public class VPFRowDataException extends IOException {
  
  public VPFRowDataException()
  {
	super();
  }
  
  public VPFRowDataException(String msg) 
  {
    super(msg);
  }

}// VPFRowDataException
