/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.renderer;

// J2SE dependencies
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import junit.framework.*;

// Geotools dependencies
import org.geotools.resources.Geometry;
import org.geotools.renderer.ShapePanel;


/**
 * Performs a visual check of {@link Geometry} computations. Those computations are
 * an essential part of {@link org.geotools.renderer.Polygon} internal working.
 *
 * @version $Id: GeometryTest.java,v 1.3 2003/01/30 23:34:41 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class GeometryTest extends TestCase implements ShapePanel.Producer {
    /**
     * Constante identifiant la méthode à tester.
     */
    private int method;

    /**
     * Coordonnées des points de contrôles qui ont été explicitement spécifiés.
     */
    private static String[] args;

    /**
     * Default constructor.
     */
    public GeometryTest(String testName) {
        super(testName); 
    }

    /**
     * Run the suit from the command line.
     */
    public static void main(final String[] args) {
        GeometryTest.args = args;
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Returns the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(GeometryTest.class);
        return suite;
    }

    /**
     * Affiche dans différentes fenêtres des résultats de calculs géométriques.
     * Cet affichage sert uniquement à vérifier visuellement l'exactitude des
     * calculs géométriques. Les fenêtres comprendront:
     *
     * <ul>
     *   <li>Deux droites et leur point d'intersection ({@link #intersectionPoint intersectionPoint}).</li>
     *   <li>Une parabole avec ses points de contrôle  ({@link #fitParabol        fitParabol}).</li>
     *   <li>Un cercle passant par trois points        ({@link #fitCircle         fitCircle}).</li>
     * </ul>
     */
    public void testGeometry(){
        //step through the different methods
        for (int i=0; i<=2; i++) {
            GeometryTest test = new GeometryTest(null);
            test.method = i;
            ShapePanel.show(test);
        }
    }

    /**
     * Retourne une liste de points et de courbes à afficher.
     */
    public Object[] getShapes() {
        switch (method) {
            //////////////////////////////////////
            ////////                      ////////
            ////////    intersectPoint    ////////
            ////////                      ////////
            //////////////////////////////////////
            default: {
                final Point2D[] points = ShapePanel.getPoints(4,args); args=null;
                final Line2D a = new Line2D.Double(points[0], points[1]);
                final Line2D b = new Line2D.Double(points[2], points[3]);
                return new Object[] {
                    a,
                    b,
                    Geometry.intersectionPoint(a,b)
                };
            }
            //////////////////////////////////
            ////////                  ////////
            ////////    fitParabol    ////////
            ////////                  ////////
            //////////////////////////////////
            case 1: {
                Point2D[] points;
                QuadCurve2D curve;
                do {
                    points = ShapePanel.getPoints(3,args); args=null;
                    curve  = Geometry.fitParabol(points[0], points[1], points[2], Geometry.PARALLEL);
                } while (curve == null);
                return new Object[] {
                    curve,
                    points[1]
                };
                /*
                 * Note: on peut obtenir les coordonnées
                 * d'un point arbitraire sur la courbe par:
                 *
                 *  x = 0.5*(ctrlx + 0.5*(x1+x2))
                 *  y = 0.5*(ctrly + 0.5*(y1+y2))
                 *
                 * Pour une courbe cubique, ce serait:
                 *
                 *  x = 0.25*(1.5*(ctrlx1+ctrlx2) + 0.5*(x1+x2));
                 *  y = 0.25*(1.5*(ctrly1+ctrly2) + 0.5*(y1+y2));
                 */
            }
            /////////////////////////////////
            ////////                 ////////
            ////////    fitCircle    ////////
            ////////                 ////////
            /////////////////////////////////
            case 2: {
                final Point2D[] points=ShapePanel.getPoints(3,args); args=null;
                return new Object[] {
                    Geometry.fitCircle(points[0], points[1], points[2]),
                                       points[0], points[1], points[2]
                };
            }
        }
    }
}
