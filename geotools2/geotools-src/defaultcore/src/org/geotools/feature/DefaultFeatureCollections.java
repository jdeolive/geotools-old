/*
 * DefaultFeatureCollections.java
 *
 * Created on July 17, 2003, 9:25 AM
 */

package org.geotools.feature;

/**
 *
 * @author  Ian Schneider
 */
public class DefaultFeatureCollections extends FeatureCollections {
  
  /** Creates a new instance of DefaultFeatureCollections */
  public DefaultFeatureCollections() {
  }
  
  protected FeatureCollection createCollection() {
    return new DefaultFeatureCollection();
  }
  
}
