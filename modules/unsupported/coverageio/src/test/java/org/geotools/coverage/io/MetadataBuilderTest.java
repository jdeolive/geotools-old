/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.coverage.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;
import org.geotools.resources.Arguments;


/**
 * Tests the {@link MetadataBuilder} implementation.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class MetadataBuilderTest extends TestCase {
    /**
     * Set to a non-null value for printing some diagnostic message to the standard output.
     */
    private static PrintWriter out;

    /**
     * Run the suit from the command line. Run the test with the
     * "-print" option in order to print test to standard output.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        if (arguments.getFlag("-print")) {
            out = arguments.out;
        } else {
            out = new PrintWriter(new StringWriter());
        }
        arguments.getRemainingArguments(0);
        org.geotools.util.logging.Logging.GEOTOOLS.forceMonolineConsoleOutput();
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(MetadataBuilderTest.class);
    }

    /**
     * Constructs a test case with the given name.
     */
    public MetadataBuilderTest(final String name) {
        super(name);
    }

    /**
     * Tests the addition of alias.
     */
    public void testAlias() throws IOException {
        final MetadataBuilder parser = new MetadataBuilder();
        /*
         * Tests "add" operations.
         */
        parser.add("Alias 1", "Value 1");
        parser.add("Alias 2", "Value 2");
        try {
            parser.add("  alias  1", "Value X");
            fail(); // We should not get there.
        } catch (AmbiguousMetadataException exception) {
            // This is the expected exception.
            if (out != null) {
                out.println(exception);
            }
        }
        parser.add("Alias 1", "Value 1"); // Already defined
        parser.add("Alias 3", "Value 3");
        /*
         * Tests "addAlias" operations.
         */
        parser.addAlias(MetadataBuilder.X_RESOLUTION, "Alias 1");
        parser.addAlias(MetadataBuilder.Y_RESOLUTION, "Alias 2");
        parser.addAlias(MetadataBuilder.Y_RESOLUTION, "Alias 2bis");
        parser.addAlias(MetadataBuilder.X_RESOLUTION, "Alias 1bis");
        parser.addAlias(MetadataBuilder.X_RESOLUTION, "Alias 1"); // Already defined
        try {
            parser.addAlias(MetadataBuilder.X_RESOLUTION, "Alias 2");
            fail(); // We should not get there.
        } catch (AmbiguousMetadataException exception) {
            // This is the expected exception.
            if (out != null) {
                out.println(exception);
            }
        }
        parser.add("Alias 2bis", "Value 2");
        try {
            parser.add("Alias 1bis", "Value 2");
            fail(); // We should not get there.
        } catch (AmbiguousMetadataException exception) {
            // This is the expected exception.
            if (out != null) {
                out.println(exception);
            }
        }
        /*
         * Tests "get" operations.
         */
        assertEquals("Value 1", parser.get(MetadataBuilder.X_RESOLUTION));
        assertEquals("Value 2", parser.get(MetadataBuilder.Y_RESOLUTION));
        try {
            parser.get(MetadataBuilder.DATUM);
            fail(); // We should not get there.
        } catch (MissingMetadataException exception) {
            // This is the expected exception.
            if (out != null) {
                out.println(exception);
            }
        }
        /*
         * Tests "getAsDouble" and "getAsInt" operations.
         */
        parser.add("ULX", "40");
        parser.add("ULY", "12.5");
        parser.addAlias(MetadataBuilder.X_MINIMUM, "ULX");
        parser.addAlias(MetadataBuilder.Y_MAXIMUM, "ULY");
        assertEquals(40,   parser.getAsDouble(MetadataBuilder.X_MINIMUM), 0);
        assertEquals(12.5, parser.getAsDouble(MetadataBuilder.Y_MAXIMUM), 0);
        assertEquals(40,   parser.getAsInt   (MetadataBuilder.X_MINIMUM));
        try {
            parser.getAsInt(MetadataBuilder.Y_MAXIMUM);
            fail(); // We should not get there.
        } catch (MetadataException exception) {
            // This is the expected exception.
            if (out != null) {
                out.println(exception);
            }
        }
        if (out != null) {
            out.println();
            parser.listMetadata(out);
            out.flush();
        }
    }

    /**
     * Tests the formatting.
     */
    public void testFormat() throws IOException {
        final MetadataBuilder parser = new MetadataBuilder();
        /*
         * Do not add a COORDINATE_REFERENCE_SYSTEM property, because we want
         * to test the MetadataBuilder capability to create it from scratch.
         */
        parser.addAlias(MetadataBuilder.PROJECTION,    "Projection"  );
        parser.addAlias(MetadataBuilder.DATUM,         "Datum"       );
        parser.addAlias(MetadataBuilder.UNITS,         "Units"       );
        parser.addAlias(MetadataBuilder.X_MINIMUM,     "Upper left X");
        parser.addAlias(MetadataBuilder.Y_MAXIMUM,     "Upper left Y");
        parser.addAlias(MetadataBuilder.X_RESOLUTION,  "Resolution X");
        parser.addAlias(MetadataBuilder.Y_RESOLUTION,  "Resolution Y");
        parser.addAlias(MetadataBuilder.WIDTH,         "Width"       );
        parser.addAlias(MetadataBuilder.HEIGHT,        "Height"      );

        final GridCoverage coverage = GridCoverageExamples.getExample(0);
        parser.add(coverage);
        if (out != null) {
            out.println(parser);
            out.flush();
        }
        assertEquals(35.0, parser.getAsDouble(MetadataBuilder.X_MINIMUM),    1E-8);
        assertEquals( 5.0, parser.getAsDouble(MetadataBuilder.Y_MAXIMUM),    1E-8);
        assertEquals( 0.1, parser.getAsDouble(MetadataBuilder.X_RESOLUTION), 1E-8);
        assertEquals( 0.1, parser.getAsDouble(MetadataBuilder.Y_RESOLUTION), 1E-8);
        assertEquals( 450, parser.getAsInt   (MetadataBuilder.WIDTH));
        assertEquals( 460, parser.getAsInt   (MetadataBuilder.HEIGHT));

        final GridRange range = parser.getGridRange();
        assertEquals("Width",  450, range.getLength(0));
        assertEquals("Height", 460, range.getLength(1));

        final CoordinateReferenceSystem expectedCRS = coverage.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem  createdCRS =   parser.getCoordinateReferenceSystem();
        assertTrue   ("The test data changed!", expectedCRS instanceof GeographicCRS);
        assertTrue   ("Created wrong CRS type.", createdCRS instanceof GeographicCRS);
        assertNotSame("Not testing creation.",  expectedCRS, createdCRS);
        assertTrue   ("Created incompatible CRS.", CRS.equalsIgnoreMetadata(expectedCRS, createdCRS));

        parser.addAlias(MetadataBuilder.COORDINATE_REFERENCE_SYSTEM, "CRS");
        parser.add(coverage);
        assertSame("Should not create CRS anymore.", expectedCRS, parser.getCoordinateReferenceSystem());
    }
}
