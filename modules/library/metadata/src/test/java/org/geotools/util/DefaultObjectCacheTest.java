/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.util;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests the {@link DefaultObjectCache} with simple tests.
 *
 * @author Cory Horner
 */
public final class DefaultObjectCacheTest {
    /**
     * Tests with two values.
     */
    @Test
    public void testSimple() {
        Integer  key1 = 1;
        Integer  key2 = 2;
        String value1 = new String("value 1");
        String value2 = new String("value 2");

        ObjectCache cache = new DefaultObjectCache();
        assertNotNull(cache);
        assertEquals(null, cache.get(key1));

        cache.writeLock(key1);
        cache.put(key1, value1);
        cache.writeUnLock(key1);
        assertEquals(value1, cache.get(key1));
        assertEquals(null,   cache.get(key2));
    }
}
