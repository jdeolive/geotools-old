/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// Divers
import java.util.Iterator;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.util.Cloneable;


/**
 * Itérateur balayant les données d'un tableau {@link PointArray}. Cet itérateur est obtenu par
 * un appel à {@link PointArray#iterator}.  Le balayage se fait généralement par des appels aux
 * aux méthodes {@link #nextX} et {@link #nextY}, qui retournent les données sous forme de nombres
 * réels <code>float</code>. Toutefois, ces méthodes <g>doivent toujours</g> être appellées dans
 * cet ordre: {@link #nextX} d'abord, suivit de {@link #nextY}. Tout manquement à cette règle (par
 * exemple appeller {@link #nextX} deux fois de suite) peut produire des résultats erronées.
 *
 * @version $Id: PointIterator.java,v 1.5 2003/08/28 15:42:13 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @task TODO: Once J2SE 1.5 will be available, this class should implements
 *             Iterator<Point2D> and method next() should returns Point2D.
 *             Method clone() should returns PointIterator.
 */
public abstract class PointIterator implements Iterator, Cloneable {
    /**
     * Constructeur par défaut.
     */
    protected PointIterator() {
    }

    /**
     * Indique si les méthodes {@link #next} peuvent retourner d'autres données.
     */
    public abstract boolean hasNext();

    /**
     * Retourne la valeur de la longitude courante. Avant d'appeller
     * une seconde fois cette méthode, il faudra <g>obligatoirement</g>
     * avoir appelé {@link #nextY}.
     */
    public abstract float nextX();

    /**
     * Retourne la valeur de la latitude courante, puis avance au point
     * suivant. Chaque appel de cette méthode doit <g>obligatoirement</g>
     * avoir été précédée d'un appel à la méthode {@link #nextX}.
     */
    public abstract float nextY();

    /**
     * Retourne la valeur du point courant dans un objet {@link Point2D},
     * puis avance au point suivant. Cette méthode combine un appel de
     * {@link #nextX} suivit de {@link #nextY}.
     */
    public Object next() {
        return new Point2D.Float(nextX(), nextY());
    }

    /**
     * Opération non-supportée.
     */
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retourne une copie conforme de cet itérateur. Cette
     * copie peut être utile pour balayer une seconde fois
     * les mêmes données à partir du point où se trouve cet
     * itérateur.
     */
    public final Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable
            throw new AssertionError(exception);
        }
    }
}
