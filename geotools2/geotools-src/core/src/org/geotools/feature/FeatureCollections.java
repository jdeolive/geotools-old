/*
 * FeatureCollections.java
 *
 * Created on July 17, 2003, 9:23 AM
 */

package org.geotools.feature;

import org.geotools.factory.*;

/**
 *
 * @author  Ian Schneider
 */
public abstract class FeatureCollections implements Factory {
  
  private static FeatureCollections instance = null;
  
  private static FeatureCollections instance() {
    if (instance == null) {
      instance = (FeatureCollections) FactoryFinder.findFactory(
        "org.geotools.feature.FeatureCollections",
        "org.geotools.feature.DefaultFeatureCollections"
      );
    }
    return instance;
  }
  
  public static FeatureCollection newCollection() {
    return instance().createCollection(); 
  }
  
  protected abstract FeatureCollection createCollection();
  
}
