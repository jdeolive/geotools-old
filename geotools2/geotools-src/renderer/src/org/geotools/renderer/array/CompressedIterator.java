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
package org.geotools.renderer.array;


/**
 * Itérateur balayant les données d'un tableau {@link CompressedArray}.
 *
 * @version $Id: CompressedIterator.java,v 1.2 2003/01/29 23:18:07 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class CompressedIterator extends PointIterator {
    /**
     * Décalages servant à calculer les valeurs <var>x</var>,<var>y</var> à retourner.
     */
    private int dx,dy;

    /**
     * Valeurs du premier point à partir d'où seront fait les calculs.
     */
    private final float x0, y0;

    /**
     * Constantes servant à transformer linéairement les
     * valeurs {@link #array} vers des <code>float</code>.
     */
    private final float scaleX, scaleY;

    /**
     * Tableau de données à balayer.
     */
    private final byte[] array;

    /**
     * Index de la prochaine donnée à retourner.
     */
    private int index;

    /**
     * Index suivant celui de la dernière donnée à balayer.
     */
    private final int upper;

    /**
     * Construit un itérateur qui balaiera des données compressées.
     */
    public CompressedIterator(final CompressedArray data, int pointIndex) {
        this.scaleX = data.scaleX;
        this.scaleY = data.scaleY;
        this.array  = data.array;
        this.x0     = data.x0;
        this.y0     = data.y0;
        this.index  = data.lower();
        this.upper  = data.upper();
        if (pointIndex >= 0) {
            while (--pointIndex>=0) {
                dx += array[index++];
                dy += array[index++];
            }
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(pointIndex));
        }
    }

    /**
     * Indique si les méthodes {@link #next} peuvent retourner d'autres données.
     */
    public boolean hasNext() {
        return index < upper;
    }

    /**
     * Retourne la valeur de la longitude courante. Avant d'appeller
     * une seconde fois cette méthode, il faudra <g>obligatoirement</g>
     * avoir appelé {@link #nextY}.
     */
    public float nextX() {
        assert (index & 1) == 0;
        dx += array[index++];
        return x0 + scaleX*dx;
    }

    /**
     * Retourne la valeur de la latitude courante, puis avance au point
     * suivant. Chaque appel de cette méthode doit <g>obligatoirement</g>
     * avoir été précédée d'un appel à la méthode {@link #nextX}.
     */
    public float nextY() {
        assert (index & 1)==1;
        dy += array[index++];
        return y0 + scaleY*dy;
    }
}
