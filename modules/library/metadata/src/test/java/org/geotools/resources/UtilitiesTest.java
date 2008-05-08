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
package org.geotools.resources;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests the {@link Utilities} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class UtilitiesTest {
    /**
     * Tests {@link Utilities#equals}.
     */
    @Test
    public void testEquals() {
        assertTrue (Utilities.equals(null, null));
        assertFalse(Utilities.equals(null, ""  ));
        assertFalse(Utilities.equals(""  , null));
        assertTrue (Utilities.equals(""  , ""  ));
        assertFalse(Utilities.equals(" " , ""  ));
    }

    /**
     * Tests {@link Utilities#spaces}.
     */
    @Test
    public void testSpaces() {
        assertEquals("",         Utilities.spaces(0));
        assertEquals(" ",        Utilities.spaces(1));
        assertEquals("        ", Utilities.spaces(8));
    }
}
