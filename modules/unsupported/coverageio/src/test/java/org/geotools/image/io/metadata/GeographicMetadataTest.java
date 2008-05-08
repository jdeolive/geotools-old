/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io.metadata;

import java.util.Arrays;
import java.util.Collection;
import javax.imageio.metadata.IIOMetadataFormat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Tests {@link GeographicMetadata}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicMetadataTest extends TestCase {
    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(GeographicMetadataTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public GeographicMetadataTest(final String name) {
        super(name);
    }

    /**
     * Tests the geographic metadata format.
     */
    public void testFormat() {
        if (true) {
            return;
            // TODO: this test doesn't seem to work with J2SE 1.4.
            //       Try again when we will be allowed to target J2SE 1.5.
        }
        final GeographicMetadata metadata = new GeographicMetadata();
        final Collection formats = Arrays.asList(metadata.getMetadataFormatNames());
        assertTrue(formats.contains(GeographicMetadataFormat.FORMAT_NAME));
        final IIOMetadataFormat format = metadata.getMetadataFormat(GeographicMetadataFormat.FORMAT_NAME);
        assertTrue(format instanceof GeographicMetadataFormat);

        final Collection crsChilds = Arrays.asList(format.getChildNames("CoordinateReferenceSystem"));
        assertTrue(crsChilds.contains("CoordinateSystem"));
        assertTrue(crsChilds.contains("Datum"));
        assertEquals(IIOMetadataFormat.DATATYPE_STRING, format.getAttributeDataType("Datum", "name"));
    }

    /**
     * Tests the setting of values in the metadata object.
     */
    public void testSetting() {
        final GeographicMetadata metadata = new GeographicMetadata();
        final ImageReferencing referencing = metadata.getReferencing();
        referencing.addAxis("latitude",  "north", "degrees");
        referencing.addAxis("longitude", "east",  "degrees");
        referencing.setCoordinateSystem("WGS84", "geographic");
        referencing.setDatum("WGS84");

        final String text = metadata.toString();
        assertTrue(text.indexOf("name=\"latitude\"" ) >= 0);
        assertTrue(text.indexOf("name=\"longitude\"") >= 0);
    }
}
