/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
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
import static org.geotools.util.Utilities.*;


/**
 * Tests the {@link Utilities} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class UtilitiesTest {
    /**
     * Tests {@link Utilities#shallowEquals}.
     */
    @Test
    public void testEquals() {
        assertTrue (shallowEquals(null, null));
        assertFalse(shallowEquals(null, ""  ));
        assertFalse(shallowEquals(""  , null));
        assertTrue (shallowEquals(""  , ""  ));
        assertFalse(shallowEquals(" " , ""  ));
    }

    /**
     * Tests {@link Utilities#spaces}.
     */
    @Test
    public void testSpaces() {
        assertEquals("",         spaces(0));
        assertEquals(" ",        spaces(1));
        assertEquals("        ", spaces(8));
    }
}
