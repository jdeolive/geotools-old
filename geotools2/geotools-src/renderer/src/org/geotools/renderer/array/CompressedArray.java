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
import java.awt.geom.Point2D;
import org.geotools.resources.XArray;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Tableaux de points compressés. Les objets de cette classe sont immutables.
 *
 * @task TODO: The compression algorithm (as computed in the constructor) should be improved.
 *             The {@link #scaleX} and {@link #scaleY} values doesn't need to macth the widest
 *             range of values. Instead, we should select some value close to the mean and allow
 *             the constructor to create intermediate points if needed.
 *
 * @task TODO: An other algorithm should be implemented in a new class: <code>ClockArray</code>
 *             or something like that. Instead of storing (dx,dy) value for each point, we should
 *             store only the angle (theta) in a 0-255 range (resolution of 1.41°). It should
 *             work providing that each points are approximatively equidistant. The current
 *             {@link org.geotools.renderer.geom.Polygon#setResolution} method ensure exactly that.
 *
 * @version $Id: CompressedArray.java,v 1.5 2003/05/13 11:00:45 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class CompressedArray extends PointArray {
    /**
     * Numéro de série (pour compatibilité avec des versions antérieures).
     */
    private static final long serialVersionUID = 7412677491468764036L;

    /**
     * Tableaux des coordonnées <u>relatives</u>. Ces coordonnées
     * sont normalement mémorisées sous forme de paires (dx,dy).
     * Chaque paire (dx,dy) représente le déplacement par rapport
     * au point précédent.
     */
    protected final byte[] array;

    /**
     * Coordonnées du point qui précède le premier point.
     * Les coordonnées du "vrai" premier point seront obtenues par:
     *
     * <pre>
     *     x = x0 + array[0]*scaleX;
     *     y = y0 + array[1]*scaleY;
     * </pre>
     */
    protected final float x0, y0;

    /**
     * Constantes servant à transformer linéairement les
     * valeurs {@link #array} vers des <code>float</code>.
     */
    protected final float scaleX, scaleY;

    /**
     * Construit un sous-tableau à partir d'un autre tableau compressé.
     *
     * @param  other Tableau source.
     * @param  lower Index de la première coordonnées <var>x</var> à
     *         prendre en compte dans le tableau <code>other</code>.
     */
    protected CompressedArray(final CompressedArray other, final int lower) {
        if (lower < other.lower()) {
            throw new IllegalArgumentException(lower+" < "+other.lower());
        }
        this.scaleX = other.scaleX;
        this.scaleY = other.scaleY;
        this.array  = other.array;

        int dx=0,dy=0;
        for (int i=other.lower(); i<lower;) {
            dx += array[i++];
            dy += array[i++];
        }
        this.x0 = other.x0 + scaleX*dx;
        this.y0 = other.y0 + scaleY*dy;
    }

    /**
     * Construit un tableau compressé.
     *
     * @param  coord Tableau de coordonnées (<var>x</var>,<var>y</var>).
     * @param  lower Index de la première coordonnées <var>x</var> à
     *         prendre en compte dans le tableau <code>coord</code>.
     * @param  upper Index suivant celui de la dernière coordonnée <var>y</var> à
     *         prendre en compte dans le tableau <code>coord</code>. La différence
     *         <code>upper-lower</code> doit obligatoirement être paire.
     * @throws ArithmeticException Si la compression a échouée à
     *         cause d'une erreur arithmétique dans l'algorithme.
     */
    public CompressedArray(final float[] coord, final int lower, final int upper)
            throws ArithmeticException
    {
        checkRange(coord, lower, upper);
        if (upper-lower < 2) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_RANGE_$2,
                                               new Integer(lower), new Integer(upper)));
        }
        /*
         * Calcule les plus grands écarts de longitude (<var>dx</var>)
         * et de latitude (<var>dy</var>) entre deux points.
         */
        float dxMin=Float.POSITIVE_INFINITY;
        float dxMax=Float.NEGATIVE_INFINITY;
        float dyMin=Float.POSITIVE_INFINITY;
        float dyMax=Float.NEGATIVE_INFINITY;
        for (int i=lower+2; i<upper; i++) {
            float delta;
            delta = coord[i]-coord[i-2];
            if (delta<dxMin) dxMin=delta;
            if (delta>dxMax) dxMax=delta;
            i++;
            delta = coord[i]-coord[i-2];
            if (delta<dyMin) dyMin=delta;
            if (delta>dyMax) dyMax=delta;
        }
        /*
         * Construit le tableau de coordonnées compressées.
         */
        this.x0    = coord[lower+0];
        this.y0    = coord[lower+1];
        byte[] array = new byte[upper-lower];
        int reduceXMin = 0;
        int reduceXMax = 0;
        int reduceYMin = 0;
        int reduceYMax = 0;
  init: for (int test=0; test<16; test++) {
            final float scaleX = Math.max(dxMax/(Byte.MAX_VALUE-reduceXMax), dxMin/(Byte.MIN_VALUE+reduceXMin));
            final float scaleY = Math.max(dyMax/(Byte.MAX_VALUE-reduceYMax), dyMin/(Byte.MIN_VALUE+reduceYMin));
            int lastx = 0;
            int lasty = 0;
            for (int j=0,i=lower; i<upper;) {
                final int  x = Math.round((coord[i++]-x0)/scaleX);
                final int  y = Math.round((coord[i++]-y0)/scaleY);
                final int dx = x-lastx;
                final int dy = y-lasty;
                if (dx<Byte.MIN_VALUE) {reduceXMin++; continue init;}
                if (dx>Byte.MAX_VALUE) {reduceXMax++; continue init;}
                if (dy<Byte.MIN_VALUE) {reduceYMin++; continue init;}
                if (dy>Byte.MAX_VALUE) {reduceYMax++; continue init;}
                array[j++] = (byte) dx;
                array[j++] = (byte) dy;
                lastx = x;
                lasty = y;
            }
            assert array[0]==0;
            assert array[1]==0;
            /*
             * Remove (0,0) values, if any. It should not happen often, but some
             * algorithm performs badly if two consecutive poins are identical.
             */
            for (int i=array.length; (i-=2)>=2;) {
                if (array[i]==0 && array[i+1]==0) {
                    array = XArray.remove(array, i, 2);
                }
            }
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.array  = array;
            return;
        }
        throw new ArithmeticException(); // Should not happen
    }

    /**
     * Retourne l'index de la première coordonnée valide.
     */
    protected int lower() {
        return 0;
    }

    /**
     * Retourne l'index suivant celui de la dernière coordonnée valide.
     */
    protected int upper() {
        return array.length;
    }

    /**
     * Retourne le nombre de points dans ce tableau.
     */
    public final int count() {
        return (upper()-lower())/2;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method count 2 bytes for each
     * (x,y) points plus 20 bytes for the internal fields (the {@link #array}, {@link #x0},
     * {@link #y0}, {@link #scaleX} and {@link #scaleY} fields).
     */
    public long getMemoryUsage() {
        return 2*count() + 20;
    }

    /**
     * Mémorise dans l'objet spécifié les coordonnées du premier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public final Point2D getFirstPoint(final Point2D point) {
        final int lower = lower();
        final float x = x0+scaleX*array[lower+0];
        final float y = y0+scaleY*array[lower+1];
        if (point != null) {
            point.setLocation(x,y);
            return point;
        } else {
            return new Point2D.Float(x,y);
        }
    }

    /**
     * Mémorise dans l'objet spécifié les coordonnées du dernier point.
     *
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     */
    public final Point2D getLastPoint(final Point2D point) {
        int dx=0;
        int dy=0;
        final int upper=upper();
        for (int i=lower(); i<upper;) {
            dx += array[i++];
            dy += array[i++];
        }
        final float x = x0+scaleX*dx;
        final float y = y0+scaleY*dy;
        if (point != null) {
            point.setLocation(x,y);
            return point;
        } else {
            return new Point2D.Float(x,y);
        }
    }

    /**
     * Retourne un itérateur qui balaiera les points partir de l'index spécifié.
     */
    public final PointIterator iterator(final int index) {
        return new CompressedIterator(this, index);
    }

    /**
     * Retourne un tableau enveloppant les mêmes points que le tableau courant,
     * mais des index <code>lower</code> inclusivement jusqu'à <code>upper</code>
     * exclusivement. Si le sous-tableau ne contient aucun point (c'est-à-dire si
     * <code>lower==upper</code>), alors cette méthode retourne <code>null</code>.
     *
     * @param lower Index du premier point à prendre en compte.
     * @param upper Index suivant celui du dernier point à prendre en compte.
     */
    public final PointArray subarray(int lower, int upper) {
        final int thisLower=lower();
        final int thisUpper=upper();
        lower = lower*2 + thisLower;
        upper = upper*2 + thisLower;
        if (lower            == upper           ) return null;
        if (lower==thisLower && upper==thisUpper) return this;
        return new SubCompressedArray(this, lower, upper);
    }

    /**
     * Insère les données (<var>x</var>,<var>y</var>) du tableau <code>toMerge</code> spécifié.
     * Si le drapeau <code>reverse</code> à la valeur <code>true</code>, alors les points de
     * <code>toMerge</code> seront copiées en ordre inverse.
     *
     * @param  index Index à partir d'où insérer les points dans ce tableau. Le point à cet
     *         index ainsi que tous ceux qui le suivent seront décalés vers des index plus élevés.
     * @param  toMerge Tableau de coordonnées (<var>x</var>,<var>y</var>) à insérer dans ce
     *         tableau de points. Ses valeurs seront copiées.
     * @param  lower Index de la première coordonnée de <code>toMerge</code> à copier dans ce tableau.
     * @param  upper Index suivant celui de la dernière coordonnée de <code>toMerge</code> à copier.
     * @param  reverse <code>true</code> s'il faut inverser l'ordre des points de <code>toMerge</code>
     *         lors de la copie. Cette inversion ne change pas l'ordre (<var>x</var>,<var>y</var>) des
     *         coordonnées de chaque points.
     *
     * @return Un nouveau tableau non-compressé.
     */
    public final PointArray insertAt(final int index, final float toMerge[],
                                     final int lower, final int upper, final boolean reverse)
    {
        if (lower == upper) {
            return this;
        }
        return new DynamicArray(this).insertAt(index, toMerge, lower, upper, reverse);
    }

    /**
     * Renverse l'ordre de tous les points compris dans ce tableau.
     *
     * @return Un nouveau tableau non-compressé qui
     *         contiendra les points en ordre inverse.
     */
    public final PointArray reverse() {
        return new DynamicArray(this).reverse();
    }

    /**
     * Retourne un tableau immutable qui contient les mêmes données que celui-ci.
     * Cette méthode retourne toujours <code>this</code> puisque ce tableau est
     * déjà immutable et compressé.
     */
    public final PointArray getFinal(final boolean compress) {
        return this;
    }

    /**
     * Append (<var>x</var>,<var>y</var>) coordinates to the specified destination array.
     * The destination array will be filled starting at index {@link ArrayData#length}.
     * If <code>resolution2</code> is greater than 0, then points that are closer than
     * <code>sqrt(resolution2)</code> from previous one will be skiped.
     *
     * @param  The destination array. The coordinates will be filled in
     *         {@link ArrayData#array}, which will be expanded if needed.
     *         After this method completed, {@link ArrayData#length} will
     *         contains the index after the <code>array</code>'s element
     *         filled with the last <var>y</var> ordinate.
     * @param  resolution2 The minimum squared distance desired between points.
     */
    public final void toArray(final ArrayData dest, final float resolution2) {
        if (!(resolution2 >= 0)) {
            throw new IllegalArgumentException(String.valueOf(resolution2));
        }
        float[]   copy   = dest.array;
        final int lower  = lower();
        final int upper  = upper();
        final int offset = dest.length;
        int       dst    = offset;
        int dxi=0, dyi=0;
        if (resolution2 == 0) {
            for (int src=lower; src<upper;) {
                dxi += array[src++];
                dyi += array[src++];
                if (copy.length <= dst) {
                    dest.array = copy = XArray.resize(copy, capacity(src, dst, offset));
                }
                copy[dst++] = x0 + scaleX*dxi;
                copy[dst++] = y0 + scaleY*dyi;
            }
        } else if (lower < upper) {
            if (copy.length <= dst) {
                dest.array = copy = XArray.resize(copy, capacity(lower, dst, offset));
            }
            copy[dst++] = x0;
            copy[dst++] = y0;
            float lastX = 0;
            float lastY = 0;
            int src = lower + 2;
            while (src < upper) {
                dxi += array[src++];
                dyi += array[src++];
                final float dxf = scaleX*dxi;
                final float dyf = scaleY*dyi;
                final double dx = (double)dxf - (double)lastX;
                final double dy = (double)dyf - (double)lastY;
                if ((dx*dx + dy*dy) >= resolution2) {
                    if (copy.length <= dst) {
                        dest.array = copy = XArray.resize(copy, capacity(src, dst, offset));
                    }
                    copy[dst++] = x0 + (lastX=dxf);
                    copy[dst++] = y0 + (lastY=dyf);
                }
            }
        }
        dest.length = dst;
        assert dest.length <= dest.array.length;
    }
}
