/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.ct;

// Text parsing and formating
import java.text.ParsePosition;
import java.text.ParseException;
import java.util.StringTokenizer;

// Input/output
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

// Collections
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.AssertionFailedError;

// Geotools dependencies
import org.geotools.cs.*;
import org.geotools.pt.CoordinatePoint;


/**
 * Run the suite of OpenGIS. A text file ({@link #TEST_FILE}) is provided. This
 * file contains a list of source and target coordinates systems (in WKT), source
 * coordinate points and expected coordinate points after the transformation from
 * source CS to target CS. Running this test really test all the following classes:
 *
 * <ul>
 *   <li>{@link CoordinateSystemFactory} (especially the WKT parser)</li>
 *   <li>{@link CoordinateSystemAuthorityFactory} (especially the implementation for EPSG codes)</li>
 *   <li>{@link CoordinateTransformationFactory}</li>
 *   <li>Many {@link MathTransform} implementations.</li>
 * </ul>
 *
 * This is probably the most important test case for the whole CTS module.
 *
 * @version $Id: OpenGISTest.java,v 1.1 2002/10/07 22:49:55 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class OpenGISTest extends TestCase {
    /**
     * If <code>true</code>, display error messages instead of throwing
     * {@link AssertionFailedError}. This is used for debugging only.
     */
    private static final boolean DISPLAY_ONLY = true;

    /**
     * The test file to parse and execute.
     */
    private static final String TEST_FILE = "test-data/CT_TestScript.txt";

    /**
     * The coordinate system factory to use for the test.
     * This is also the class used for parsing WKT texts.
     */
    private CoordinateSystemFactory csFactory;

    /**
     * The coordinate transformation factory to use for the test.
     */
    private CoordinateTransformationFactory ctFactory;

    /**
     * The list of object defined in the {@link #TEST_FILE} file.  Keys are
     * {@link String} objects, while values are {@link CoordinateSystem} or
     * {@link MathTransform} objects.
     */
    private Map definitions;

    /**
     * The list of instructions. Example of instructions:
     * <pre>
     *   cs_source      = _Wgs84NE_
     *   cs_target      = _Wgs84SW_
     *   test_tolerance = 1e-6
     *   pt_source      = (1, 2)
     *   pt_target      = (-1, -2)
     * </pre>
     */
    private Map instructions;

    /**
     * Number of test run and passed. Used for displaying
     * a report after once the test is finished.
     */
    private int testRun, testPassed;
    
    /**
     * Constructs a test case with the given name.
     */
    public OpenGISTest(final String name) {
        super(name);
    }
    
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(OpenGISTest.class);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();        
        csFactory    = CoordinateSystemFactory.getDefault();
        ctFactory    = CoordinateTransformationFactory.getDefault();
        definitions  = new HashMap();
        instructions = new HashMap();
    }

    /**
     * Check if two coordinate points are equals, in the limits of the specified
     * tolerance vector.
     *
     * @param expected  The expected coordinate point.
     * @param actual    The actual coordinate point.
     * @param tolerance The tolerance vector. If this vector length is smaller than the number
     *                  of dimension of <code>actual</code>, then the last tolerance value will
     *                  be reused for all extra dimensions.
     * @throws AssertionFailedError If the actual point is not equals to the expected point.
     */
    private static void assertEquals(final CoordinatePoint expected,
                                     final CoordinatePoint actual,
                                     final double[]        tolerance)
        throws AssertionFailedError
    {
        final int dimension = actual.getDimension();
        final int lastToleranceIndex = tolerance.length-1;
        assertEquals("The coordinate point doesn't have the expected dimension",
                     expected.getDimension(), dimension);
        for (int i=0; i<dimension; i++) {
            assertEquals("Mismatch for (zero-based) ordinate "+i,
                         expected.getOrdinate(i), actual.getOrdinate(i),
                         tolerance[Math.min(i, lastToleranceIndex)]);
        }
    }

    /**
     * Parse a vector of values. Vectors are used for coordinate points.
     * Example:
     * <pre>
     * (46.69439222, 13.91405611, 41.21)
     * </pre>
     *
     * @param  text The vector to parse.
     * @return The vector as floating point numbers.
     * @throws NumberFormatException if a number can't be parsed.
     */
    private static double[] parseVector(String text) throws NumberFormatException {
        text = removeDelimitors(text, '(', ')');
        final StringTokenizer st = new StringTokenizer(text, ",");
        final double[]    values = new double[st.countTokens()];
        for (int i=0; i<values.length; i++) {
            values[i] = Double.parseDouble(st.nextToken());
        }
        return values;
    }

    /**
     * Check if the specified string start and end with the specified delimitors,
     * and returns the string without the delimitors.
     *
     * @param text  The string to check.
     * @param start The delimitor required at the string begining.
     * @param end   The delimitor required at the string end.
     */
    private static String removeDelimitors(String text, final char start, final char end) {
        text = text.trim();
        final int endPos = text.length()-1;
        if (endPos >= 1) {
            if (text.charAt(0)==start && text.charAt(endPos)==end) {
                text = text.substring(1, endPos).trim();
            }
        }
        return text;
    }

    /**
     * If the specified string start with <code>"set"</code>, then add its
     * value to the {@link #definitions} map and returns <code>true</code>.
     * Otherwise, returns <code>false</code>.
     *
     * @param  text The string to parse.
     * @return <code>true</code> if it was a definition string,
     *         or <code>false</code> otherwise.
     * @throws FactoryException if the string can't be parsed.
     */
    private boolean addDefinition(String text) throws FactoryException {
        /*
         * List of keywords processed in a special ways by this method.
         */
        final String SET       = "set";
        final String PARAM_MT  = "PARAM_MT";
        final String COMPD_CS  = "COMPD_CS";
        final String FITTED_CS = "FITTED_CS";
        /*
         * If the string is in the form "set name = value",
         * then separate the name and the value parts.
         */
        if (!text.regionMatches(true, 0, SET, 0, SET.length())) {
            return false;
        }
        text = text.substring(SET.length());
        StringTokenizer st = new StringTokenizer(text, "=");
        if (st.countTokens() != 2) {
            throw new FactoryException("String must be in the form \"name = value\".");
        }
        String name  = st.nextToken().trim();
        String value = st.nextToken().trim();
        final CoordinateSystem cs;
        /*
         * Checks if the value is a Compound Coordinate System.
         * Syntax: COMPD_CS["name", cs1name, cs2name]
         */
        if (value.regionMatches(true, 0, COMPD_CS, 0, COMPD_CS.length())) {
            value = removeDelimitors(value.substring(COMPD_CS.length()), '[', ']');
            st = new StringTokenizer(value, ",");
            if (st.countTokens() != 3) {
                throw new FactoryException("COMPD_CS must be in the form "+
                                           "COMPD_CS[\"name\", cs1name, cs2name]");
            }
            String csname = removeDelimitors(st.nextToken(), '"', '"');
            CoordinateSystem head = (CoordinateSystem) definitions.get(st.nextToken().trim());
            CoordinateSystem tail = (CoordinateSystem) definitions.get(st.nextToken().trim());
            cs = csFactory.createCompoundCoordinateSystem(csname, head, tail);
        }
        else if (value.regionMatches(true, 0, FITTED_CS, 0, FITTED_CS.length())) {
            System.out.println("FITTED_CS not yet implemented.");
            return true;
        }
        else if (value.regionMatches(true, 0, PARAM_MT, 0, PARAM_MT.length())) {
            System.out.println("PARAM_MT not yet implemented.");
            return true;
        }
        else {
            cs = csFactory.createFromWKT(value);
        }
        if (definitions.put(name, cs) != null) {
            throw new FactoryException("A value is already defined for \""+name+"\".");
        }
        return true;
    }

    /**
     * Run an instruction. Instruction may be any of the following lines
     * (values listed here are just examples):
     * <pre>
     *   cs_source      = _Wgs84NE_
     *   cs_target      = _Wgs84SW_
     *   test_tolerance = 1e-6
     *   pt_source      = (1, 2)
     *   pt_target      = (-1, -2)
     * </pre>
     *
     * The "<code>pt_target</code>" instruction trig the computation.
     *
     * @param  text The instruction to parse.
     * @throws TransformException if the transformation can't be run.
     */
    private void runInstruction(final String text) throws TransformException {
        final StringTokenizer st = new StringTokenizer(text, "=");
        if (st.countTokens() != 2) {
            throw new TransformException("Illegal instruction: "+text);
        }
        final String name  = st.nextToken().trim().toLowerCase();
        final String value = st.nextToken().trim();
        if (name.startsWith("cs_")) {
            instructions.put(name, definitions.get(value));
            return;
        }
        if (name.startsWith("test_")) {
            instructions.put(name, parseVector(value));
            return;
        }
        if (!name.startsWith("pt_")) {
            throw new TransformException("Unknow instruction: "+name);
        }
        instructions.put(name, new CoordinatePoint(parseVector(value)));
        if (!name.equals("pt_target")) {
            return;
        }
        /*
         * The "pt_target" instruction trig the test.
         */
        final CoordinateSystem sourceCS = (CoordinateSystem) instructions.get("cs_source");
        final CoordinateSystem targetCS = (CoordinateSystem) instructions.get("cs_target");
        final CoordinatePoint  sourcePT = (CoordinatePoint)  instructions.get("pt_source");
        final CoordinatePoint  targetPT = (CoordinatePoint)  instructions.get("pt_target");
        final double[]        tolerance = (double[]) instructions.get("test_tolerance");
        CoordinatePoint        computed = null;
        CoordinateTransformation     tr = null;
        try {
            testRun++;
            tr = ctFactory.createFromCoordinateSystems(sourceCS, targetCS);
            computed = tr.getMathTransform().transform(sourcePT, computed);
            assertEquals(targetPT, computed, tolerance);
            testPassed++;
        } catch (TransformException exception) {
            if (!DISPLAY_ONLY) {
                throw exception;
            }
            System.out.println("----TRANSFORMATION FAILED-------------------------------------------------------");
            System.out.println(exception);
            System.out.println();
        } catch (AssertionFailedError error) {
            if (!DISPLAY_ONLY) {
                throw error;
            }
            System.out.println("----TEST FAILED-----------------------------------------------------------------");
            System.out.println("cs_source : " + sourceCS);
            System.out.println("cs_target : " + targetCS);
            System.out.println("pt_source = " + sourcePT);
            System.out.println("pt_target = " + targetPT);
            System.out.println("computed  = " + computed);
            System.out.println();
        }
    }

    /**
     * Run the {@link #TEST_FILE}.
     *
     * @throws IOException If {@link #TEST_FILE} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    public void testOpenGIS() throws IOException, FactoryException, TransformException {
        final BufferedReader reader;
        if (true) {
            final InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_FILE);
            if (in == null) {
                throw new FileNotFoundException(TEST_FILE);
            }
            reader = new BufferedReader(new InputStreamReader(in));
        }
        String line;
        while ((line=reader.readLine()) != null) {             
            line = line.trim();
            if (line.length() == 0) {
                // Ignore empty lines.
                continue;
            }
            if (line.startsWith("//")) {
                // Ignore comment lines.
                continue;
            }
            if (addDefinition(line)) {
                // Definition line are processed by 'addDefinition'.
                continue;
            }
            runInstruction(line);
        }
        reader.close();
        System.out.print("Test passed: ");
        System.out.print((int) (100*testPassed/testRun));
        System.out.println('%');
    }   

    /**
     * Run the test from the command line.
     */
    public static void main(final String[] args) throws Exception {
        final OpenGISTest test = new OpenGISTest(null);
        test.setUp();
        test.testOpenGIS();
    }
}
