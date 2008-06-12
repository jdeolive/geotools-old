/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources;

import java.io.*;
import java.util.Set;
import java.util.HashSet;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests the {@link Classes} static methods.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class ClassesTest {
    /**
     * Tests {@link Classes#mostSpecificClass} and {@link Classes#commonClass}.
     */
    @Test
    public void testCommonParent() {
        final Set<Object> types = new HashSet<Object>();

        assertTrue(types.add(new NotSerializableException()));
        assertEquals(NotSerializableException.class, Classes.commonClass     (types));
        assertEquals(NotSerializableException.class, Classes.specializedClass(types));

        assertTrue(types.add(new InvalidObjectException(null)));
        assertEquals(ObjectStreamException.class, Classes.commonClass     (types));
        assertEquals(ObjectStreamException.class, Classes.specializedClass(types));

        assertTrue(types.add(new FileNotFoundException()));
        assertEquals(IOException.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));

        assertTrue(types.add(new IOException()));
        assertEquals(IOException.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));

        assertTrue(types.add(new Exception()));
        assertEquals(  Exception.class, Classes.commonClass     (types));
        assertEquals(IOException.class, Classes.specializedClass(types));
    }

    /**
     * Tests {@link Classes#sameInterfaces}.
     */
    @Test
    @SuppressWarnings("unchecked") // We break consistency on purpose for one test.
    public void testSameInterfaces() {
        assertTrue (Classes.sameInterfaces(StringBuilder.class, String.class, CharSequence.class));
        assertTrue (Classes.sameInterfaces(StringBuilder.class, String.class, Serializable.class));
        assertFalse(Classes.sameInterfaces((Class)  File.class, String.class, CharSequence.class));
        assertTrue (Classes.sameInterfaces(         File.class, String.class, Serializable.class));
    }
}
