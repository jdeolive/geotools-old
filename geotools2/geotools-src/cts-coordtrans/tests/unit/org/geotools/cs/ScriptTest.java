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
package org.geotools.cs;

// Text parsing and formating
import java.text.ParsePosition;
import java.text.ParseException;
import java.util.StringTokenizer;

// Input/output
import java.io.IOException;
import java.io.PrintWriter;
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
import org.geotools.ct.*;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Arguments;


/**
 * Run the suite of OpenGIS tests. A text file ({@link #TEST_FILE}) is provided. This
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
 * @version $Id: ScriptTest.java,v 1.2 2002/10/25 16:32:18 ianturton Exp $
 * @author Yann Cézard
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class ScriptTest extends TestCase {
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
     * The math transformation factory to use for the test.
     */
    private MathTransformFactory mtFactory;

    /**
     * The list of object defined in the {@link #TEST_FILE} file.  Keys are
     * {@link String} objects, while values are {@link CoordinateSystem} or
     * {@link MathTransform} objects.
     */
    private Map definitions;

    /**
     * Source and target coordinate systems for the test currently executed.
     * Those fields are updated many times by {@link #runInstruction}.
     */
    private CoordinateSystem sourceCS, targetCS;

    /**
     * Source and target coordinate points for the test currently executed.
     * Those fields are updated many times by {@link #runInstruction}.
     */
    private CoordinatePoint sourcePT, targetPT;

    /**
     * Tolerance numbers for the test currently executed.
     * Thise field is updated many times by {@link #runInstruction}.
     */
    private double[] tolerance;

    /**
     * Number of test run and passed. Used for displaying
     * a report after once the test is finished.
     */
    private int testRun, testPassed;

    /**
     * If non-null display error messages to this writer instead of throwing
     * {@link AssertionFailedError}. This is used for debugging only.
     */
    private PrintWriter out;
    
    /**
     * Constructs a test case with the given name.
     */
    public ScriptTest(final String name) {
        super(name);
    }
    
    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(ScriptTest.class);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();        
        csFactory    = CoordinateSystemFactory.getDefault();
        ctFactory    = CoordinateTransformationFactory.getDefault();
        mtFactory    = MathTransformFactory.getDefault();
        definitions  = new HashMap();
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
            assertEquals("Mismatch for ordinate "+i+" (zero-based):",
                         expected.getOrdinate(i), actual.getOrdinate(i),
                         tolerance[Math.min(i, lastToleranceIndex)]);
        }
    }

    /**
     * Returns a coordinate system for the specified name. The coordinate system
     * must has been previously defined with a call to {@link #addDefinition}.
     */
    private CoordinateSystem getCoordinateSystem(final String name) throws FactoryException {
        final Object cs = definitions.get(name);
        if (cs instanceof CoordinateSystem) {
            return (CoordinateSystem) cs;
        }
        throw new FactoryException("No coordinate system defined for \""+name+"\".");
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
        final Object cs;
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
            CoordinateSystem head = getCoordinateSystem(st.nextToken().trim());
            CoordinateSystem tail = getCoordinateSystem(st.nextToken().trim());
            cs = csFactory.createCompoundCoordinateSystem(csname, head, tail);
        }
        else if (value.regionMatches(true, 0, FITTED_CS, 0, FITTED_CS.length())) {
            System.out.println("FITTED_CS not yet implemented.");
            return true;
        }
        else if (value.regionMatches(true, 0, PARAM_MT, 0, PARAM_MT.length())) {
            cs = mtFactory.createFromWKT(value);
            if (true) {
                assertEquals("MathTransform.equals(...) failed", cs, cs);
                final MathTransform check = mtFactory.createFromWKT(cs.toString());
                assertEquals("WKT formating produces a different result.", check, cs);
            }
        }
        else {
            cs = csFactory.createFromWKT(value);
            if (true) {
                assertEquals("CoordinateSystem.equals(...) failed", cs, cs);
                final CoordinateSystem check = csFactory.createFromWKT(cs.toString());
                assertEquals("WKT formating produces a different result.", check, cs);
            }
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
     * @throws FactoryException if the instruction can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    private void runInstruction(final String text) throws FactoryException, TransformException {
        final StringTokenizer st = new StringTokenizer(text, "=");
        if (st.countTokens() != 2) {
            throw new FactoryException("Illegal instruction: "+text);
        }
        final String name  = st.nextToken().trim().toLowerCase();
        final String value = st.nextToken().trim();
        if (name.equals("cs_source")) {
            sourceCS = getCoordinateSystem(value);
            return;
        }
        if (name.equals("cs_target")) {
            targetCS = getCoordinateSystem(value);
            return;
        }
        if (name.equals("test_tolerance")) {
            tolerance = parseVector(value);
            return;
        }
        if (name.equals("pt_source")) {
            sourcePT = new CoordinatePoint(parseVector(value));
            return;
        }
        if (!name.equals("pt_target")) {
            throw new FactoryException("Unknow instruction: "+name);
        }
        targetPT = new CoordinatePoint(parseVector(value));
        /*
         * The "pt_target" instruction trig the test.
         */
        CoordinatePoint    computed = null;
        CoordinateTransformation tr = null;
        try {
            testRun++;
            tr = ctFactory.createFromCoordinateSystems(sourceCS, targetCS);
            computed = tr.getMathTransform().transform(sourcePT, computed);
            assertEquals(targetPT, computed, tolerance);
            testPassed++;
        } catch (TransformException exception) {
            if (out == null) {
                throw exception;
            }
            out.println("----TRANSFORMATION FAILED-------------------------------------------------------");
            out.println(exception);
            out.println();
        } catch (AssertionFailedError error) {
            if (out == null) {
                throw error;
            }
            out.println("----TEST FAILED-----------------------------------------------------------------");
            out.println("cs_source : " + sourceCS);
            out.println("cs_target : " + targetCS);
            out.println("pt_source = " + sourcePT);
            out.println("pt_target = " + targetPT);
            out.println("computed  = " + computed);
            out.println();
        }
    }

    /**
     * Run the {@link #TEST_FILE}.
     *
     * @throws IOException If {@link #TEST_FILE} can't be read.
     * @throws FactoryException if a line can't be parsed.
     * @throws TransformException if the transformation can't be run.
     */
    public void testOpenGIS() throws IOException, FactoryException  {
        final BufferedReader reader;
        if (true) {
            InputStream in = getClass().getClassLoader().getResourceAsStream(TEST_FILE);
            if (in == null) {
                 String dataFolder;
                
                    //then we are being run by maven
                    dataFolder = System.getProperty("basedir");
                    dataFolder+="/tests/unit/" + TEST_FILE;
                in = new java.io.FileInputStream(dataFolder);
                //throw new FileNotFoundException(TEST_FILE);
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
            try{
                runInstruction(line);
            } catch (TransformException te){
                if(out != null){
                    out.println(line + "\n\t threw a TransformationException - " + te);
                }
            }
        }
        reader.close();
        if (out != null) {
            out.print("Test passed: ");
            out.print((int) (100*testPassed/testRun));
            out.println('%');
            out.flush();
        }
    }   

    /**
     * Run the test from the command line.
     *
     * @param  args The command-line arguments.
     * @throws Exception if a test failed.
     */
    public static void main(final String[] args) throws Exception {
        final Arguments arguments = new Arguments(args);
        final ScriptTest test = new ScriptTest(null);
        test.out = arguments.out;
        test.setUp();
        test.testOpenGIS();
    }
}
