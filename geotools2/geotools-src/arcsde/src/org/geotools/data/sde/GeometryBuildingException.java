package org.geotools.data.sde;

/**
 * Exception that can be thrown if an error occurs while creating
 * a <code>Geometryy</code> from a <code>SeShape</code> or
 * viceversa
 * @author Gabriel Roldán
 * @version 0.1
 */

public class GeometryBuildingException extends Exception {

  public GeometryBuildingException(String msg)
  {
    this(msg, null);
  }

  public GeometryBuildingException(String msg, Throwable cause)
  {
    super(msg, cause);
  }
}