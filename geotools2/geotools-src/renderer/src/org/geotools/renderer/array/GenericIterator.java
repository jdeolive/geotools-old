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

// J2SE dependencies
import java.awt.Point;
import java.awt.geom.Point2D;


/**
 * Itérateur balayant les données d'un tableau {@link GenericArray}.
 *
 * @version $Id: GenericIterator.java,v 1.1 2003/05/23 17:58:59 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class GenericIterator extends PointIterator {
    /**
     * The <var>x</var> and <var>y</var> vectors.
     */
    private final GenericArray.Vector x,y;

    /**
     * Index suivant celui de la dernière donnée à balayer.
     */
    private final int upper;

    /**
     * Index de la prochaine donnée à retourner.
     */
    private int index;

    /**
     * The data type for {@link #next}.
     */
    private final int type;

    /**
     * Construit un itérateur qui balaiera la plage spécifiée d'un tableau de données.
     */
    public GenericIterator(GenericArray.Vector x, GenericArray.Vector y, int lower, int upper) {
        this.x     = x;
        this.y     = y;
        this.index = lower;
        this.upper = upper;
        this.type  = Math.max(x.type(), y.type());
    }

    /**
     * Indique si les méthodes {@link #next} peuvent retourner d'autres données.
     */
    public boolean hasNext() {
        return index < upper;
    }

    /**
     * Retourne la valeur de la longitude courante.
     */
    public float nextX() {
        return x.getAsFloat(index);
    }

    /**
     * Retourne la valeur de la latitude courante, puis avance au point suivant.
     */
    public float nextY() {
        return y.getAsFloat(index++);
    }

    /**
     * Retourne la valeur du point courant dans un objet {@link Point2D},
     * puis avance au point suivant. Cette méthode combine un appel de
     * {@link #nextX} suivit de {@link #nextY}.
     */
    public Object next() {
        switch (type) {
            case 0:  return new Point         (x.getAsInteger(index), y.getAsInteger(index++));
            case 1:  return new Point2D.Float (x.getAsFloat  (index), y.getAsFloat  (index++));
            default: return new Point2D.Double(x.getAsDouble (index), y.getAsDouble (index++));
        }
    }
}
