/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.feature;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * An IndexedFeatureCollection extends the functionality of FeatureCollection
 * by allowing FeatureIndex attachement.
 * @author  Ian Schneider
 * @source $URL$
 */
public interface IndexedFeatureCollection extends FeatureCollection<SimpleFeatureType, SimpleFeature> {
  
  /** Adds a FeatureIndex to this collection.
   * @param index The FeatureIndex to add.
   * @throws NullPointerException If the index is null.
   */  
  void addIndex(FeatureIndex index);
  
  /** Removes the given FeatureIndex from the collection.
   * @param index The FeatureIndex to remove.
   * @throws NullPointerException If the index is null.
   */  
  void removeIndex(FeatureIndex index);
  
  /** Removes all indices from this collection. */  
  void removeAllIndices();
  
  /** Look up an index by class.
   * @return The FeatureIndex or null, if none exists.
   */  
  FeatureIndex getIndex(Class index);
  
  /** Get an Iterator containing all of the indices in this collection.
   * @return An Iterator of the indices.
   */  
  java.util.Iterator indices();
  
}
