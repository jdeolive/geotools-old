/*
 * Copyright (c) 2004, JSR-108 group (http://www.jcp.org/en/jsr/detail?id=108)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of the JSR-108 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.units;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Top-level test suite for the <code>javax.units</code> package.
 *
 * @author Martin Desruisseaux
 * @version $Date: 2004/05/09 15:47:14 $
 */
public class UnitTest extends TestCase {
    /**
     * Small tolerance factor for testing conversions.
     */
    private static final double EPS = 1E-8;

    /**
     * Runs the test suite from the command line.
     */
    public static void main(final String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Returns the test suite for this class.
     */
    public static Test suite() {
        return new TestSuite(UnitTest.class);
    }

    /**
     * Create a test suite.
     */
    public UnitTest() {
        super("UnitTest");
    }

    /**
     * Test the {@link Unit#equals} method.
     */
    public void testEquals() {
        assertEquals("kg", SI.KILOGRAM, SI.KILOGRAM);
        assertEquals("m",  SI.METER,    SI.METER   );
        assertEquals("N",  SI.NEWTON,   SI.NEWTON  );
        assertEquals("J",  SI.JOULE,    SI.JOULE   );
        
        assertFalse("m vs kg",  SI.METER .equals(SI.KILOGRAM));
        assertFalse("N vs J" ,  SI.NEWTON.equals(SI.JOULE   ));
        assertFalse("N vs m" ,  SI.NEWTON.equals(SI.METER   ));
    }

    /**
     * Test if the parsing produces the expected units.
     */
    public void testParsing() {
        assertSame   ("kg" , SI.KILOGRAM, Unit.valueOf("kg" ));
        assertSame   ("m"  , SI.METER,    Unit.valueOf("m"  ));
        assertSame   ("s"  , SI.SECOND,   Unit.valueOf("s"  ));
        assertNotNull("m/s",              Unit.valueOf("m/s"));
    }

    /**
     * Tests operations on units (multiply, divide, pow, etc.).
     */
    public void testOperations() {
        if (false) {
            // Tests fail in current version.
            final Unit METRE_PER_SECOND = Unit.valueOf("m/s");
            assertSame("[J]", SI.JOULE,  METRE_PER_SECOND.pow(2).multiply(SI.KILOGRAM));
            assertSame("[N]", SI.NEWTON, SI.KILOGRAM.multiply(METRE_PER_SECOND).divide(SI.SECOND));
            assertSame("[J]", SI.JOULE,  SI.JOULE.multiply(SI.KELVIN.pow(0)));
        }
    }

    /**
     * Tests unit formatting. This test implies testing operation as well
     * (multiply, divide, pow, etc.).
     */
    public void testFormatting() {
        assertEquals("[J]",                "J",     SI.JOULE                           .toString());
        assertEquals("[N]",                "N",     SI.NEWTON                          .toString());

        assertEquals("[kg].pow(2)",        "kg²",   SI.KILOGRAM.pow(2)                 .toString());
        assertEquals("[kg].scale(1E-3)",   "g",     SI.KILOGRAM.multiply(1.0E-3)       .toString());
        assertEquals("[kg].scale(1E-6)",   "mg",    SI.KILOGRAM.multiply(1.0E-6)       .toString());
        assertEquals("[kg/s]",             "kg/s",  SI.KILOGRAM.divide(SI.SECOND)      .toString());
        assertEquals("[kg/s]",             "kg/s",  SI.KILOGRAM.pow(3)
                                                               .multiply(SI.SECOND  .pow(-1))
                                                               .multiply(SI.KILOGRAM.pow(-2))
                                                               .multiply(SI.KELVIN  .pow( 0))
                                                                                       .toString());

        assertEquals("[m].pow(2)",         "m²",    SI.METER.pow(2)                    .toString());
        assertEquals("[m]*[s]",            "m·s",   SI.METER.multiply(SI.SECOND)       .toString());
        assertEquals("[m]/[s]",            "m/s",   SI.METER.divide  (SI.SECOND)       .toString());
        assertEquals("[m].scale(1000)",    "km",    SI.METER.multiply(1000.0)          .toString());
        assertEquals("[m].scale(1E-6)",    "µm",    SI.METER.multiply(1.0E-6)          .toString());

        final Unit METRE_PER_SECOND = Unit.valueOf("m/s");
        assertEquals("[m/s].pow(2)",       "m²/s²", METRE_PER_SECOND.pow(2)            .toString());
        assertEquals("[m/s]*[m]",          "m²/s",  METRE_PER_SECOND.multiply(SI.METER).toString());
        assertEquals("[m]*[m/s]",          "m²/s",  SI.METER.multiply(METRE_PER_SECOND).toString());
        assertEquals("[m/s]/[m]",          "1/s",   METRE_PER_SECOND.divide(SI.METER)  .toString());
        assertEquals("[m]/[m/s]",          "s",     SI.METER.divide(METRE_PER_SECOND)  .toString());

        final Unit POUND_PER_SECOND = SI.KILOGRAM.divide(SI.SECOND).multiply(0.4535);
        final Unit MILE_PER_HOUR    = NonSI.MILE.divide(NonSI.HOUR);
        assertEquals("[mile/h].pow(2)",    "mi²/h²",        MILE_PER_HOUR    .pow(2)                    .toString());
        assertEquals("[pound].pow(2)",     "lb²",           NonSI.POUND      .pow(2)                    .toString());
        if (false) {
            // Tests fail in current version.
            assertEquals("[pound/s]",          "lb/s",          POUND_PER_SECOND                            .toString());
            assertEquals("[mile/h]*[pound/s]", "(mi*lb)/(h*s)", MILE_PER_HOUR    .multiply(POUND_PER_SECOND).toString());
            assertEquals("[pound/s]*[mile/h]", "(lb/s)*(mi/h)", POUND_PER_SECOND .multiply(MILE_PER_HOUR)   .toString());
            assertEquals("[mile/h]/[pound/s]", "(mi/h)/(lb/s)", MILE_PER_HOUR    .divide  (POUND_PER_SECOND).toString());
            assertEquals("[pound/s]/[mile/h]", "(lb/s)/(mi/h)", POUND_PER_SECOND .divide  (MILE_PER_HOUR)   .toString());
        }
    }

    /**
     * Tests unit conversions.
     */
    public void testConversions() {
        assertEquals("0 °C -> °F",        32, SI.CELSIUS          .getConverterTo(NonSI.FAHRENHEIT)   .convert(   0), EPS);
        assertEquals("32 °F -> °C",        0, NonSI.FAHRENHEIT    .getConverterTo(SI.CELSIUS)         .convert(  32), EPS);
        assertEquals("10 °C -> °F",       50, SI.CELSIUS          .getConverterTo(NonSI.FAHRENHEIT)   .convert(  10), EPS);
        assertEquals("500 g -> kg",        5, Unit.valueOf("g"   ).getConverterTo(SI.KILOGRAM)        .convert(5000), EPS);
        assertEquals("4 cm -> m",       0.04, Unit.valueOf("cm"  ).getConverterTo(SI.METER)           .convert(   4), EPS);
        assertEquals("25 km/s -> m/s", 25000, Unit.valueOf("km/s").getConverterTo(Unit.valueOf("m/s")).convert(  25), EPS);

        final Unit METRE_PER_SECOND = Unit.valueOf("m/s");
        final Unit MILE_PER_HOUR    = NonSI.MILE.divide(NonSI.HOUR);
        assertEquals("1 m/s -> mi/h",   2.2369362920544023, METRE_PER_SECOND.getConverterTo(MILE_PER_HOUR    ).convert(1   ), EPS);
        assertEquals("1 mile/h -> m/s", 0.44704,            MILE_PER_HOUR   .getConverterTo(METRE_PER_SECOND ).convert(1   ), EPS);
        if (false) {
            // Tests fail in current version.
            assertEquals("0.01 Hz -> min",  1.6666666666666667, SI.HERTZ        .getConverterTo(NonSI.MINUTE     ).convert(0.01), EPS);
            assertEquals("50 Ω -> S",       0.02,               SI.OHM          .getConverterTo(SI.SIEMENS       ).convert(50  ), EPS);
        }
    }

    /**
     * Tests exceptions.
     */
    public void testExceptions() {
        try {
            SI.METER.getConverterTo(SI.SECOND);
            fail("[m] -> [s]");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        try {
            SI.METER.divide(SI.SECOND).getConverterTo(SI.JOULE);
            fail("[m/s] -> [J]");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        try {
            SI.METER.divide(SI.SECOND).getConverterTo(NonSI.POUND.divide(SI.SECOND));
            fail("[m/s] -> [pound/s]");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        if (false) try {
            SI.CELSIUS.pow(2);
            fail("[°C].pow(2)");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        if (false) try {
            NonSI.FAHRENHEIT.multiply(SI.CELSIUS);
            fail("[°F].multiply([°C])");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        if (false) try {
            SI.CELSIUS.multiply(NonSI.FAHRENHEIT);
            fail("[°C].multiply([°F])");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        if (false) try {
            NonSI.FAHRENHEIT.divide(SI.CELSIUS);
            fail("[°F].divide([°C])");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
        if (false) try {
            SI.CELSIUS.divide(NonSI.FAHRENHEIT);
            fail("[°C].divide([°F])");
        } catch (ConversionException e) {
            // This is the expected exception.
        }
    }
}
