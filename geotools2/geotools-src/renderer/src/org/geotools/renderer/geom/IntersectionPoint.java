/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
package org.geotools.renderer.geom;

// J2SE dependencies
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;


/**
 * Coordonnée associée à une intersection entre deux lignes. Cette classe est réservée à un usage
 * interne afin de déterminer de quelle façon on doit refermer les formes géométriques des îles et
 * des continents. Le point mémorisé par cette classe proviendra de l'intersection de deux lignes:
 * un des bords de la carte (généralement un des 4 côtés d'un rectangle, mais ça pourrait être une
 * autre forme géométrique) avec une ligne passant par les deux premiers ou les deux derniers points
 * du traît de côte. Appellons la première ligne (celle du bord de la carte) "<code>line</code>".
 * Cette classe mémorisera au passage le produit scalaire entre un vecteur passant le premier point
 * de <code>line</code> et le point d'intersection avec un vecteur passant par le premier et dernier
 * point de <code>line</code>. Ce produit scalaire peut être vu comme une sorte de mesure de la
 * distance entre le début de <code>line</code> et le point d'intersection.
 *
 * @version $Id: IntersectionPoint.java,v 1.1 2003/02/04 12:30:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class IntersectionPoint extends Point2D.Double implements Comparable {
    /**
     * Numéro du bord sur lequel a été trouvée ce point. Cette information est laissé
     * à la discrétion du programmeur, qui y mettra les informations qu'il souhaite.
     * Ce numéro peut être utile pour aider à retrouver plus tard la ligne sur laquelle
     * fut trouvée le point d'intersection.
     */
    int border;

    /**
     * Distance au carré entre le point d'intersection et le point qui en était le plus
     * proche. Cette information n'est pas utilisée par cette classe, sauf dans la méthode
     * {@link #toString}. Elle utile à des fins de déboguage, mais aussi pour choisir quel
     * point d'intersection supprimé s'il y en a trop. On supprimera le point qui se trouve
     * le plus loin de sa bordure.
     */
    double minDistanceSq = java.lang.Double.NaN;

    /**
     * Produit scalaire entre la ligne sur laquelle fut trouvée le point d'intersection
     * et un vecteur allant du début de cette ligne jusqu'au point d'intersection.
     */
    double scalarProduct;

    /**
     * Segment de traît de côte auquel appartient la ligne avec
     * laquelle on a calculé un point d'intersection.
     */
    Polygon path;

    /**
     * Indique si le point d'intersection fut calculé à partir des deux premiers ou deux derniers
     * points du traît de côte. Si <code>append</code> a la valeur <code>true</code>, cela
     * signifiera que l'intersection fut calculée à partir des deux derniers points du trait
     * de côte. Refermer la forme géométrique de l'île ou du continent impliquera donc que l'on
     * ajoute des points à la fin du trait de côte ("append"), en opposition au fait d'ajouter
     * des points au début du trait de côte ("prepend").
     */
    boolean append;

    /**
     * Système de coordonnées de ce point. Cette information n'est utilisée que par la méthode
     * {@link #toString}, afin de pouvoir écrire une coordonnées en latitudes et longitudes.
     */
    CoordinateSystem coordinateSystem;

    /**
     * Construit un point initialisé
     * à la position (0,0).
     */
    public IntersectionPoint() {
    }

    /**
     * Construit un point initialisé
     * à la position spécifiée.
     */
    public IntersectionPoint(final Point2D point) {
        super(point.getX(), point.getY());
    }

    /**
     * Mémorise dans cet objet la position du point spécifié. Le produit scalaire
     * de ce point avec la ligne <code>line</code> sera aussi calculé et placé dans
     * le champs {@link #scalarProduct}.
     *
     * @param point  Coordonnées de l'intersection.
     * @param line   Coordonnées de la ligne sur laquelle l'intersection <code>point</code> fut
     *               trouvée.
     * @param border Numéro de la ligne <code>line</code>. Cette information sera mémorisée dans
     *               le champs {@link #border} et est laissée à la discretion du programmeur.
     *               Il est suggéré d'utiliser un numéro unique pour chaque ligne <code>line</code>,
     *               et qui croissent dans le même ordre que les lignes <code>line</code> sont
     *               balayées.
     */
    final void setLocation(final Point2D point, final Line2D.Double line, final int border) {
        super.setLocation(point);
        final double dx = line.x2-line.x1;
        final double dy = line.y2-line.y1;
        scalarProduct = ((x-line.x1)*dx+(y-line.y1)*dy) / Math.sqrt(dx*dx + dy*dy);
        this.border = border;
    }

    /**
     * Compare ce point avec un autre. Cette comparaison n'implique que
     * la position de ces points sur un certain segment. Elle permettra
     * de classer les points dans l'ordre des aiguilles d'une montre, ou
     * dans l'ordre inverse selon la façon dont {@link PathIterator} est
     * implémentée.
     *
     * @param o Autre point d'intersection avec lequel comparer celui-ci.
     * @return -1, 0 ou +1 selon que ce point précède, égale ou suit le
     *         point <code>o</code> dans un certain sens (généralement le
     *         sens des aiguilles d'une montre).
     */
    public int compareTo(final IntersectionPoint pt) {
        if (border < pt.border) return -1;
        if (border > pt.border) return +1;
        if (scalarProduct < pt.scalarProduct) return -1;
        if (scalarProduct > pt.scalarProduct) return +1;
        return 0;
    }

    /**
     * Compare ce point avec un autre. Cette comparaison n'implique que
     * la position de ces points sur un certain segment. Elle permettra
     * de classer les points dans l'ordre des aiguilles d'une montre, ou
     * dans l'ordre inverse selon la façon dont {@link PathIterator} est
     * implémentée.
     *
     * @param o Autre point d'intersection avec lequel comparer celui-ci.
     * @return -1, 0 ou +1 selon que ce point précède, égale ou suit le
     *         point <code>o</code> dans un certain sens (généralement le
     *         sens des aiguilles d'une montre).
     */
    public int compareTo(Object o) {
        return compareTo((IntersectionPoint) o);
    }

    /**
     * Indique si ce point d'intersection est identique au point <code>o</code>.
     * Cette méthode est définie pour être cohérente avec {@link #compareTo}, mais
     * n'est pas utilisée.
     *
     * @return <code>true</code> si ce point d'intersection est le même que <code>o</code>.
     */
    public boolean equals(final Object o) {
        if (o instanceof IntersectionPoint) {
            return compareTo((IntersectionPoint) o) == 0;
        } else {
            return false;
        }
    }

    /**
     * Retourne un code à peu près unique pour ce point d'intersection,
     * basé sur le produit scalaire et le numéro de la ligne. Ce code
     * sera cohérent avec la méthode {@link #equals}.
     *
     * @return Un numéro à peu près unique pour ce point d'intersection.
     */
    public int hashCode() {
        final long bits = java.lang.Double.doubleToLongBits(scalarProduct);
        return border ^ (int)bits ^ (int)(bits >>> 32);
    }

    /**
     * Renvoie une représentation sous forme de chaîne de caractères
     * de ce point d'intersection (à des fins de déboguage seulement).
     *
     * @return Chaîne de caractères représentant ce point d'intersection.
     */
    public String toString() {
        final CoordinateSystem WGS84 = GeographicCoordinateSystem.WGS84;
        final StringBuffer buffer = new StringBuffer("IntersectionPoint[");
        if (coordinateSystem != null) {
            try {
                CoordinatePoint coord = new CoordinatePoint(this);
                coord = CoordinateTransformationFactory.getDefault()
                                            .createFromCoordinateSystems(coordinateSystem, WGS84)
                                            .getMathTransform().transform(coord, coord);
                buffer.append(coord);
            } catch (TransformException exception) {
                buffer.append("error");
            }
        } else {
            buffer.append((float) x);
            buffer.append(' ');
            buffer.append((float) y);
        }
        buffer.append(']');
        if (!java.lang.Double.isNaN(minDistanceSq)) {
            buffer.append(" at ");
            buffer.append((float) Math.sqrt(minDistanceSq));
        }
        if (coordinateSystem != null) {
            buffer.append(' ');
            buffer.append(coordinateSystem.getUnits(0));
        }
        buffer.append(" from #");
        buffer.append(border);
        buffer.append(" (");
        buffer.append((float) scalarProduct);
        buffer.append(')');
        return buffer.toString();
    }
}
