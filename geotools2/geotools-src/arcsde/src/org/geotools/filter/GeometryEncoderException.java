package org.geotools.filter;

/**
 * Indicates a client class has attempted to encode a filter not supported by
 * the GeometryEncoderSDE being used.
 * @author Gabriel Roldán
 * @version 0.1
 */

public class GeometryEncoderException extends Exception {

  public GeometryEncoderException(String msg) {
    this(msg, null);
  }

  public GeometryEncoderException(String msg, Throwable cause) {
    super(msg, cause);
  }
}