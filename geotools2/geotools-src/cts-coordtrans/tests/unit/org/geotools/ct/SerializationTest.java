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

// J2SE dependencies
import java.io.*;
import java.sql.*;
import java.util.logging.*;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.ct.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test serialization of a {@link CoordinateTransformation} class.
 *
 * @version $Id: SerializationTest.java,v 1.4 2003/01/23 23:58:47 desruisseaux Exp $
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
        try {
            CoordinateSystemAuthorityFactory csf = CoordinateSystemEPSGFactory.getDefault();
            CoordinateSystem cs1 = csf.createCoordinateSystem( "4326" );
            CoordinateSystem cs2 = csf.createCoordinateSystem( "4322" );
            CoordinateTransformationFactory ctf = CoordinateTransformationFactory.getDefault();
            CoordinateTransformation ct = ctf.createFromCoordinateSystems( cs1, cs2 );
            ObjectOutputStream so = new ObjectOutputStream( new ByteArrayOutputStream() );
            so.writeObject(ct);
            so.flush();
            so.close();
        } catch (SQLException exception) {
            LogRecord record = new LogRecord(Level.WARNING,
                    "Can't connect to the EPSG database. "+
                    "Ignoring, since it is not the purpose of this test.");
            record.setSourceClassName ("SerializationTest");
            record.setSourceMethodName("testCoordinateTransformation");
            record.setThrown(exception);
            Logger.getLogger("org.geotools.ct").log(record);
        }
    }

    /**
     * Tests the serialization of many {@link CoordinateTransformation} objects.
     *
     * @task TODO: Enable this test later.
     */
    public static void testCoordinateTransformations() throws Exception {
        if (true) {
            // Disable this test for now.
            return;
        }
        final String cs1_name  = "4326";
        final int cs2_ranges[] = {4326,  4326,
                                  4322,  4322,
                                  4269,  4269,
                                  4267,  4267,
                                  4230,  4230,
                                 32601, 32661,
                                 32701, 32761,
                                  2759,  2930};

        CoordinateSystem cs1=null, cs2=null;
        for (int irange=0; irange<cs2_ranges.length; irange+=2) {
            int range_start = cs2_ranges[irange  ];
            int range_end   = cs2_ranges[irange+1];
            for (int isystem2=range_start; isystem2<=range_end; isystem2++) {
                try {
                    CoordinateSystemAuthorityFactory csf = CoordinateSystemEPSGFactory.getDefault();
                    CoordinateTransformationFactory  ctf = CoordinateTransformationFactory.getDefault();
                    if (cs1 == null) {
                        System.out.println("Create base CoordinateSystem "+cs1_name);
                        cs1 = csf.createCoordinateSystem(cs1_name);
                    }
                    String cs2_name = Integer.toString(isystem2);
                    System.out.println("Create CoordinateSystem "+cs2_name);
                    cs2 = csf.createCoordinateSystem(cs2_name);

                    System.out.println("Create CoordinateTransform "+cs1_name+" -> "+cs2_name);
                    CoordinateTransformation ct = ctf.createFromCoordinateSystems(cs1, cs2);

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    ObjectOutputStream so = new ObjectOutputStream(buffer);
                    so.writeObject(ct);
                    so.close();

                    ObjectInputStream si = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
                    Object obj = si.readObject();
                    si.close();

                    assertEquals("Serialized object not the same", ct, obj);
                } catch (Exception e) {
                    // Do not throw the exception for now, since
                    // this test is not yet fully successful.
                    e.printStackTrace(System.out);
                }
            }
        }
    }
}
