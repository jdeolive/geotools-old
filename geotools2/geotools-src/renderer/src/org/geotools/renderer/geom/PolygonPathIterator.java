/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2001, Institut de Recherche pour le Développement
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
import java.util.Iterator;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;
import java.awt.geom.IllegalPathStateException;

// Geotools dependencies
import org.geotools.renderer.array.ArrayData;


/**
 * Itérateur balayant les points d'un polygone ou d'un isobath.
 *
 * @version $Id: PolygonPathIterator.java,v 1.3 2003/02/10 23:09:38 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class PolygonPathIterator extends ArrayData implements PathIterator {
    /**
     * Itérateur balayant les objets
     * {@link Polygon} à tracer.
     */
    private final Iterator polygons;

    /**
     * Le polygone en cours de traçage.
     */
    private Polygon polygon;

    /**
     * Transformation a appliquer aux coordonnées (rotation, translation, échelle...).
     */
    private final AffineTransform transform;

    /**
     * Index de la prochaine valeur à retourner dans le tableau {@link #array}.
     */
    private int index;

    /**
     * The type of the next curve to returns.
     */
    private int curveType = SEG_MOVETO;

    /**
     * Construit un itérateur qui balayera les points d'un seul polygone.
     *
     * @param polygone Polygone à tracer.
     * @param transform Transformation affine facultative (peut être nulle).
     */
    public PolygonPathIterator(final Polygon polygon, final AffineTransform transform) {
        final PolygonCache cache = polygon.getCache();
        this.polygon   = polygon;
        this.polygons  = null;
        this.transform = transform;
        cache.getRenderingArray(polygon, this, transform);
        if (array==null || length==0) {
            cache.releaseRenderingArray(array);
            array = null;
        }
    }

    /**
     * Construit un itérateur qui balayera les points d'un ensemble de polygones.
     *
     * @param polygons Itérateur balayant une liste d'objets {@link Polygons}.
     * @param transform Transformation affine facultative (peut être nulle).
     */
    public PolygonPathIterator(final Iterator polygons, final AffineTransform transform) {
        this.polygons  = polygons;
        this.transform = transform;
        while (polygons.hasNext()) {
            polygon = (Polygon) polygons.next();
            final PolygonCache cache = polygon.getCache();
            cache.getRenderingArray(polygon, this, transform);
            if (array!=null && length!=0) {
                break;
            }
            cache.releaseRenderingArray(array);
            array = null;
        }
    }

    /**
     * Tests if there are more points to read.
     * @return true if there are no more points to read.
     */
    public boolean isDone() {
        return array == null;
    }

    /**
     * Moves the polygons to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    public void next() {
        if (array != null) {
            switch (curveType) {
                case SEG_MOVETO:  // fall through
                case SEG_LINETO:  index+=2; break;
                case SEG_QUADTO:  index+=4; break;
                case SEG_CUBICTO: index+=6; break;
                default: throw new IllegalPathStateException();
            }
            if (index >= length) {
                if (index!=length || !polygon.isClosed()) {
                    synchronized (polygon) {
                        polygon.getCache().releaseRenderingArray(array);
                        setData(null, 0, null);
                        index = 0;
                    }
                    if (polygons != null) {
                        while (polygons.hasNext()) {
                            polygon = (Polygon) polygons.next();
                            synchronized (polygon) {
                                final PolygonCache cache = polygon.getCache();
                                cache.getRenderingArray(polygon, this, transform);
                                if (array!=null && length!=0) {
                                    break;
                                }
                                cache.releaseRenderingArray(array);
                                setData(null, 0, null);
                            }
                        }
                    }
                }
            }
            curveType = getCurveType(index);
        }
    }

    /**
     * Retourne la coordonnée et le type du prochain segment. Cette méthode retourne
     * {@link #SEG_MOVETO} au début de chaque polygone. Après le dernier point, elle
     * retourne {@link #SEG_CLOSE} si la forme géométrique est une île, un lac ou tout
     * autre forme fermée. Entre ces deux extrémités, elle retourne toujours {@link #SEG_LINETO}.
     *
     * @param into Tableau dans lequel mémoriser les coordonnées du prochain point.
     *             Ce tableau doit avoir une longueur d'au moins 2 (pour 1 point).
     */
    public int currentSegment(final float into[]) {
        if (index < length) {
            final int n;
            switch (curveType) {
                case SEG_CUBICTO: n=6; break;
                case SEG_QUADTO:  n=4; break;
                case SEG_LINETO:  // fall through
                case SEG_MOVETO:  n=2; break;
                default: throw new IllegalPathStateException();
            }
            System.arraycopy(array, index, into, 0, n);
            return curveType;
        }
        return SEG_CLOSE;
    }

    /**
     * Retourne la coordonnée et le type du prochain segment. Cette méthode retourne
     * {@link #SEG_MOVETO} au début de chaque polygone. Après le dernier point, elle
     * retourne {@link #SEG_CLOSE} si la forme géométrique est une île, un lac ou tout
     * autre forme fermée. Entre ces deux extrémités, elle retourne toujours {@link #SEG_LINETO}.
     *
     * @param into Tableau dans lequel mémoriser les coordonnées du prochain point.
     *             Ce tableau doit avoir une longueur d'au moins 2 (pour 1 point).
     */
    public int currentSegment(final double into[]) {
        if (index < length) {
            switch (curveType) {
                case SEG_CUBICTO: {
                    into[5] = array[index+5];
                    into[4] = array[index+4];
                    // fall through
                }
                case SEG_QUADTO: {
                    into[3] = array[index+3];
                    into[2] = array[index+2];
                    // fall through
                }
                case SEG_LINETO: {
                    // fall through
                }
                case SEG_MOVETO: {
                    into[1] = array[index+1];
                    into[0] = array[index+0];
                    break;
                }
                default: {
                    throw new IllegalPathStateException();
                }
            }
            return curveType;
        }
        return SEG_CLOSE;
    }

    /**
     * Return the winding rule for determining the interior of the path.
     * @return <code>WIND_EVEN_ODD</code> by default.
     */
    public int getWindingRule() {
        return WIND_EVEN_ODD;
    }
}
