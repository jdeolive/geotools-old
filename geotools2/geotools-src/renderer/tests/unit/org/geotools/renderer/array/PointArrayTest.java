/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
package org.geotools.renderer.array;

// J2SE dependencies
import java.util.Random;
import java.util.Arrays;
import org.geotools.resources.XArray;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Test the <code>org.geotools.renderer.array</code> package.
 *
 * @version $Id: PointArrayTest.java,v 1.4 2003/05/13 11:00:47 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PointArrayTest extends TestCase {
    /**
     * Set to <code>true</code> for printing some informations to the standard output.
     */
    private static boolean PRINT = false;

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        return new TestSuite(PointArrayTest.class);
    }
    
    /**
     * Constructs a test case with the given name.
     */
    public PointArrayTest(final String name) {
        super(name);
    }

    /**
     * Run the test.
     */
    public void testPointArray() {
        final Random random = new Random();
        float[] checkPoints = new float[2];
        PointArray   points = PointArray.getInstance(checkPoints);
        for (int i=0; i<2000; i++) {
            /*
             * Construit un tableau (de longueur paire) qui sera inséré au milieu
             * des points déjà existants. Ce tableau sera remplis avec des nombres
             * aléatoires pour distinguer les différentes coordonnées.
             */
            final float[] toMerge = new float[(int)(32*random.nextDouble()) & ~1];
            for (int j=0; j<toMerge.length; j++) {
                toMerge[j] = (float) (2000*random.nextDouble()-1000);
            }
            /*
             * Sélectionne au hasard une plage de valeurs à prendre en compte dans
             * le tableau <code>toMerge</code>. Les autres valeurs seront abandonnées.
             * Choisit aussi au hasard un point d'insertions pour les nouveaux points.
             */
            final int lower = (int)(toMerge.length*(0.49*random.nextDouble()    )) & ~1;
            final int upper = (int)(toMerge.length*(0.49*random.nextDouble()+0.5)) & ~1;
            final int index = (int)(checkPoints.length  *random.nextDouble()     ) & ~1;
            /*
             * Insère les nouveaux points dans le tableau 'checkPoints'. Ce tableau est
             * maintenu uniquement à des fins de comparaisons avec le "vrai" tableau de
             * points. Vérifie si les deux tableaux ont un contenu identique.
             */
            final boolean reverse = (random.nextDouble() > 0.5);
            points = points.insertAt(index/2, toMerge, lower, upper, reverse);
            if (reverse) {
                DynamicArray.reverse(toMerge, lower, upper);
            }
            checkPoints = XArray.insert(toMerge, lower, checkPoints, index, upper-lower);
            assertEquals(checkPoints.length, 2*points.count());
            assertTrue(Arrays.equals(points.toArray(), checkPoints));
        }
        /*
         * Prépare une liste d'index délimitant des plages de points à
         * l'intérieur du tableau. Ces plages seront extraites plusieurs
         * fois sur différentes versions du tableau de points.
         */
        final int index[]=new int[8];
        for (int i=0; i<index.length; i++) {
            index[i] = (int)(random.nextDouble()*points.count());
        }
        Arrays.sort(index);
        /*
         * Teste maintenant l'extraction d'un sous-tableau
         * ainsi que la compression des données.
         */
        for (int i=0; i<=3; i++) {
            switch (i) {
                case 0: {
                    break;
                }
                case 1: {
                    points = points.getFinal(false);
                    break;
                }
                case 2: {
                    checkPoints=points.toArray();
                    Arrays.sort(checkPoints);
                    points = PointArray.getInstance(checkPoints);
                    break;
                }
                case 3: {
                    points = points.getFinal(true);
                    break;
                }
            }
            if (PRINT) {
                System.out.println(points);
                for (int j=0; j<index.length; j++) {
                    System.out.print("    [");
                    System.out.print(index[j]);
                    System.out.print("..");
                    System.out.print(index[j+1]);
                    System.out.print("]: ");
                    System.out.println(points.subarray(index[j], index[++j]));
                }
                System.out.println();
            }
        }
        /*
         * Recherche l'écart maximal entre les
         * données compressées et les données réelles.
         */
        float dx=0, dy=0;
        final PointIterator iterator=points.iterator(0);
        for (int i=0; iterator.hasNext();) {
            assertTrue(i<checkPoints.length);
            // The compressed array may have less points than 'checkPoints'
            final float dxi = Math.abs(iterator.nextX()-checkPoints[i++]);
            final float dyi = Math.abs(iterator.nextY()-checkPoints[i++]);
            if (dxi > dx) dx=dxi;
            if (dyi > dy) dy=dyi;
        }
        if (PRINT) {
            System.out.print(dx);
            System.out.print(", ");
            System.out.println(dy);
        }
    }

    /**
     * Vérifie le bon fonctionnement de cette classe. Cette méthode peut être appelée
     * sans argument.  Les assertions doivent être activées (option <code>-ea</code>)
     * pour que la vérification soit effective. Cette méthode peut aussi être exécutée
     * sans les assertions pour tester les performances.
     */
    public static void main(final String[] args) {
        PRINT = true;
        new PointArrayTest(null).testPointArray();
    }
}
