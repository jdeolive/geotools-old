/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.util;

// J2SE dependencies
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link WeakHashSet}. A standard {@link HashSet} object
 * is used for comparaison purpose.
 *
 * @version $Id: WeakHashSetTest.java,v 1.3 2003/08/06 17:30:17 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class WeakHashSetTest extends TestCase {
    /**
     * A random number generator.
     */
    private Random random;

    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(WeakHashSetTest.class);
     }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Constructs a test case with the given name.
     */
    public WeakHashSetTest(final String name) {
        super(name);
    }

    /**
     * Set up common objects used for all tests.
     */
    protected void setUp() throws Exception {
        super.setUp();
        random = new Random();
    }

    /**
     * Test the {@link WeakHashSet} using strong references.
     * The tested {@link WeakHashSet} should behave like a
     * standard {@link Set} object.
     */
    public void testStrongReferences() {
        for (int pass=0; pass<20; pass++) {
            final WeakHashSet weakSet = new WeakHashSet();
            final HashSet   strongSet = new HashSet();
            for (int i=0; i<1000; i++) {
                final Integer value = new Integer(random.nextInt(500));
                if (random.nextBoolean()) {
                    /*
                     * Test addition.
                     */
                    final boolean   weakModified = weakSet  .add(value);
                    final boolean strongModified = strongSet.add(value);
                    assertEquals("add", strongModified, weakModified);
                    if (strongModified) {
                        assertSame("get", value, weakSet.get(value));
                    } else {
                        assertEquals("get",  value, weakSet.get(value));
                    }
                } else {
                    /*
                     * Test remove
                     */
                    final boolean   weakModified = weakSet  .remove(value);
                    final boolean strongModified = strongSet.remove(value);
                    assertEquals("remove", strongModified, weakModified);
                    assertNull("get", weakSet.get(value));
                }
                assertEquals("contains", strongSet.contains(value), weakSet.contains(value));
                assertEquals("equals", strongSet, weakSet);
            }
        }
    }

    /**
     * Test the {@link WeakHashSet} using weak references.
     * In this test, we have to keep in mind than some elements
     * in <code>weakSet</code> may disaspear at any time!
     */
    public void testWeakReferences() throws InterruptedException {
        for (int pass=0; pass<2; pass++) {
            final WeakHashSet weakSet = new WeakHashSet();
            final HashSet   strongSet = new HashSet();
            for (int i=0; i<1000; i++) {
                final Integer value = new Integer(random.nextInt(500));
                if (random.nextBoolean()) {
                    /*
                     * Test addition.
                     */
                    final boolean   weakModified = weakSet  .add(value);
                    final boolean strongModified = strongSet.add(value);
                    if (weakModified) {
                        // If the element was not in the WeakHashSet (i.e. if the garbage
                        // collector has cleared it), then it must not been in HashSet neither
                        // (otherwise GC should not have cleared it).
                        assertTrue("add", strongModified);
                    } else {
                        assertTrue(value != weakSet.get(value));
                        if (strongModified) {
                            // If the element was already in the WeakHashSet but not in the
                            // HashSet, this is because GC has not cleared it yet. Replace it
                            // by 'value', because if we don't it may be cleared later and the
                            // "contains" test below will fails.
                            //
                            // Note: we don't test if 'remove' below returns 'true', because GC
                            //       may have already done its work since the few previous lines!
                            weakSet.remove(value);
                            assertTrue(weakSet.add(value));
                            assertSame(value, weakSet.get(value));
                        }
                    }
                } else {
                    /*
                     * Test remove
                     */
                    final boolean c = weakSet.contains(value);
                    if (strongSet.remove(value)) {
                        assertTrue("contains", c);
                    }
                }
                assertTrue("containsAll", weakSet.containsAll(strongSet));
            }
            // Do our best to lets GC finish its work.
            for (int i=0; i<5; i++) {
                Thread.sleep(100);
                System.gc();
            }
            assertTrue("equals", strongSet.equals(weakSet));
        }
    }
}
