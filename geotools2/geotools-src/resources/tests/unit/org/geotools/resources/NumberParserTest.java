/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.resources;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link NumberParser} class.
 *
 * @author Ian Schneider
 */
public class NumberParserTest extends TestCase {
  /**
   * Returns the test suite.
   */
  public static Test suite() {
    return new TestSuite(NumberParserTest.class);
  }
  
  /**
   * Constructs a test case with the given name.
   */
  public NumberParserTest(String name) {
    super(name);
  }
  
  private void test1(String s) {
    assertEquals(Double.parseDouble(s), NumberParser.parseDouble(s),0); 
  }
  
  private void test(String s) {
    boolean javafail = false;
    boolean gtfail = false;
    double javadouble = 0;
    double gtdouble = 0;
    try {
      javadouble = Double.parseDouble(s);
    } catch (NumberFormatException nfe) {
      javafail = true;
    }
    try {
      gtdouble = NumberParser.parseDouble(s);
    } catch (NumberFormatException nfe) {
      gtfail = true;
    }
    if (javafail)
      assertTrue("gtfailure '" + s + "'", gtfail);
    if (gtfail)
      assertTrue("javafailure '" + s + "'", javafail);
    if (!javafail && !gtfail)
      assertEquals(javadouble,gtdouble, 0);
  }
  
  public void testValidDoubles() {
    test("4.275");
    test("0.123e5");
    test(".123e5");
    test("23e5");
    test("3e5");
    test("         123.456   ");
    test("         123e123   ");
    test("         123.456   ");
    test("10e");
    test("10.");
    test("10.e");
    test(".");
    
    // check garbage bytes robustness
    char[] zeros = new char [] {0,49,49,0};
    assertEquals( (double) 11,NumberParser.parseDouble(new String(zeros)), 0);
  }
  
  public void testInvalidDoubles() {
    test(""); 
    test("x");
    test("e");
    test("\b");
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  
}
