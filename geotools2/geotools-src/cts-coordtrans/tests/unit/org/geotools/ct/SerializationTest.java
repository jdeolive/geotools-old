/*
 * Geotools - OpenSource mapping toolkit
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.ct;

// J2SE and Geotools dependencies
import java.io.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test serialization of a {@link CoordinateTransformation} class.
 *
 * @author Vadim Semenov
 * @author Martin Desruisseaux
 */
public class SerializationTest extends TestCase {
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(SerializationTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public SerializationTest(final String name) {
        super(name);
    }

    /**
     * Tests the serialization of a {@link CoordinateTransformation} object.
     */
    public void testCoordinateTransformation() throws Exception {
        CoordinateSystemAuthorityFactory csf = CoordinateSystemEPSGFactory.getDefault();
        CoordinateSystem cs1 = csf.createCoordinateSystem( "4326" );
        CoordinateSystem cs2 = csf.createCoordinateSystem( "4322" );
        CoordinateTransformationFactory ctf = CoordinateTransformationFactory.getDefault();
        CoordinateTransformation ct = ctf.createFromCoordinateSystems( cs1, cs2 );
        ObjectOutputStream so = new ObjectOutputStream( new ByteArrayOutputStream() );
        so.writeObject(ct);
        so.flush();
        so.close();
    }
}
