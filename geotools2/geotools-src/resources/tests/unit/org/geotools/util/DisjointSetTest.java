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
import java.util.*;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the {@link DisjointSet} class.
 *
 * @version $Id: DisjointSetTest.java,v 1.3 2003/08/06 17:30:17 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class DisjointSetTest extends TestCase {

    /**
     * Returns the test suite.
     */
     public static Test suite() {
         return new TestSuite(DisjointSetTest.class);
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
    public DisjointSetTest(final String name) {
        super(name);
    }

     /**
      * Test the set.
      */
     public void testDisjointSet() {
        DisjointSet t1 = new DisjointSet(true);
        DisjointSet t2 = new DisjointSet(t1);
        DisjointSet t3 = new DisjointSet(t2);

        assertNotNull(t1.getTrash());
        assertSame(t1.getTrash(), t2.getTrash());
        assertSame(t2.getTrash(), t3.getTrash());

        assertTrue(t1.add("alpha"));
        assertTrue(t2.add("bêta"));
        assertTrue(t3.add("gamma"));
        assertTrue(t2.add("delta"));
        assertTrue(t1.add("epsilon"));
        assertTrue(t2.add("alpha"));
        assertTrue(t2.remove("bêta"));

        assertEquals(Collections.singleton("epsilon"), t1);
        assertEquals(new HashSet(Arrays.asList(new String[] {"alpha","delta"})), t2);
        assertEquals(Collections.singleton("gamma"), t3);
        assertEquals(Collections.singleton("bêta"),  t1.getTrash());
    }
}
