/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.firstdraft;

import java.util.Map;


/** An entry in FeatureCache. Holds access statistics associated with a Feature in the cache.
 * Interface copied from javax.cache.CacheEntry
 * @see http://jsr-107-interest.dev.java.net/javadoc/javax/cache/Cache.html
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public interface CacheEntry extends Map.Entry {
    long getCost();

    long getCreationTime();

    long getExpirationTime();

    int getHits();

    long getLastAccessTime();

    long getLastUpdateTime();

    long getVersion();

    boolean isValid();
}
