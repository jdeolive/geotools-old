/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.feature;

import org.geotools.factory.Factory;
import org.geotools.factory.FactoryFinder;

/**
 * A utility class for working with FeatureCollections.
 * Provides a mechanism for obtaining a FeatureCollection instance.
 * @author  Ian Schneider
 */
public abstract class FeatureCollections implements Factory {

  /**
   * Holds a reference to a FeatureCollections implementation once
   * one has been requested for the first time using instance().
   */
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
  
  /**
   * create a new FeatureCollection using the current default factory.
   * @return A FeatureCollection instance.
   */
  public static FeatureCollection newCollection() {
    return instance().createCollection(); 
  }
  /**
   * Subclasses must implement this to return a new FeatureCollection object.
   * @return A new FeatureCollection
   */
  protected abstract FeatureCollection createCollection();
  
}
