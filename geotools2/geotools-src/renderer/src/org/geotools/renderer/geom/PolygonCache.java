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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Soft reference for <code>float[]</code> array of (<var>x</var>,<var>y</var>) coordinates.
 * There is at most one instance of this class for each instance of {@link Polygon}. This
 * class is strictly for internal use by {@link PolygonPathIterator}.
 *
 * @version $Id: PolygonCache.java,v 1.1 2003/02/03 09:51:59 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @task TODO: More work are needed: hold a strong reference to the array for some time before
 *             to let it go with soft reference, in order to reduce the amount of time it is
 *             recomputed. Impose a global memory limit before the strong reference is cleared.
 *             Manage the list of PolygonCache using a double-linked list, in order to know
 *             which one to clear (the last used).
 */
final class PolygonCache {
    /**
     * Cache vers les transformation affine déjà créées. Cette cache utilise des références
     * faibles pour ne retenir les transformations que si ells sont déjà utilisées ailleurs
     * dans la machine virtuelle. <strong>Tous les objets placés dans cette cache devraient
     * être laissés constants (immutables).</strong>
     */
    private static final WeakHashSet pool = new WeakHashSet();

    /**
     * Transformation affine identité. Cette transformation affine
     * sera partagée par plusieurs objets {@link PolygonCache}  et
     * ne doit pas être modifiée.
     */
    private static final AffineTransform IDENTITY = new AffineTransform();

    /**
     * Transformation affine qui avait été utilisée pour transformer les données cachée.
     */
    private AffineTransform transform = IDENTITY;

    /**
     * Nombre d'objets {@link PathIterator} qui utilisent le tableau de points {@link #array}.
     * Ce nombre sera incrémenté à chaque appel de {@link #getRenderingArray} et décrémenté
     * par {@link #releaseRenderingArray}.
     */
    private short lockCount;

    /**
     * The array of (<var>x</var>,<var>y</var>) coordinates.   It may be either a strong
     * reference to <code>float[]</code>, a soft {@link Reference}, or <code>null</code>.
     */
    private Object array;

    /**
     * Number of valid elements in {@link #array}. This is twice the number of valid points.
     */
    private int length;

    /**
     * <code>true</code> if the last call of {@link #getRenderingArray} has recomputed
     * the cache array. This information is for statistics purpose only.
     */
    private boolean recomputed;

    /**
     * Construct a new empty cache. This constructor is used by {@link Polygon#getCache} only.
     */
    PolygonCache() {
    }

    /**
     * Returns an array of decimated and transformed (<var>x</var>,<var>y</var>) coordinates.
     * The method {@link #releaseRenderingArray} <strong>must</strong> be invoked once the
     * rendering is finished.
     *
     * @param  polygon The source polygon.
     * @param  newTransform Transformation affine à appliquer sur les données. La valeur
     *         <code>null</code> sera interprétée comme étant la transformation identitée.
     * @return Un tableau de points (<var>x</var>,<var>y</var>). Cette méthode retourne une
     *         référence directe vers un tableau interne. En conséquence, aucune modification
     *         ne doit être faite au tableau retourné.
     *
     * @see #releaseRenderingArray
     * @see #getPointCount
     */
    public float[] getRenderingArray(final Polygon polygon, AffineTransform newTransform) {
        assert Thread.holdsLock(polygon);
        if (newTransform != null) {
            newTransform = (AffineTransform) pool.canonicalize(new AffineTransform(newTransform));
            // TODO: This line may fill 'pool' with a lot of entries
            //       (>100) when the user change zoom often (e.g. is
            //       scrolling). Should we look for an other way?
        } else {
            newTransform = IDENTITY;
        }
        /*
         * Gets the cached array (which may be a strong or a soft reference) and cast it
         * to type 'float[]'. The 'array' local variable hide the 'array' class variable,
         * but both of them always refer to the same object.  The local variable is just
         * casted to 'float[]'.
         */
        if (array instanceof Reference) {
            array = ((Reference) array).get();
        }
        float[] array = (float[]) this.array;
        /*
         * Si la transformation affine n'a pas changé depuis la dernière fois, alors on pourra
         * retourner le tableau directement.  Sinon, on tentera de modifier les coordonnées en
         * prenant en compte seulement le **changement** de la transformation affine depuis la
         * dernière fois.   Mais cette étape ne sera faite qu'à la condition que le tableau ne
         * soit pas en cours d'utilisation par un autre itérateur (lockCount==0).
         */
        if (array != null) {
            // If we are using this array for the second time, it may be worth to trim it...
            this.array = array = XArray.resize(array, length);
            if (newTransform.equals(transform)) {
                lockCount++;
                recomputed = false;
                return array;
            }
            if (lockCount == 0) try {
                final AffineTransform change = transform.createInverse();
                change.preConcatenate(newTransform);
                change.transform(array, 0, array, 0, length/2);
                transform = newTransform;
                lockCount = 1;
                recomputed = false;
                return array;
            } catch (NoninvertibleTransformException exception) {
                Utilities.unexpectedException("org.geotools.renderer.geom", "Polygon",
                                              "getPathIterator", exception);
                // Continue... On va simplement reconstruire le tableau à partir de la base.
            } else {
                // Should be uncommon. Doesn't hurt, but may be a memory issue for big polygon.
                Polygon.LOGGER.info(Resources.format(ResourceKeys.WARNING_EXCESSIVE_MEMORY_USAGE));
                this.array = array = new float[32];
            }
        } else {
            this.array = array = new float[32];
        }
        /*
         * Reconstruit le tableau de points à partir des données de bas niveau.
         * La projection cartographique sera appliquée par {@link Polygon#toArray}.
         */
        final float[][] arrays = new float[][]{array};
        length = polygon.toArray(arrays, polygon.getRenderingResolution());
        this.array = array = arrays[0];
        assert (length & 1) == 0;
        if (array.length >= 2*length) {
            // If the array is much bigger then needed, trim to size.
            this.array = array = XArray.resize(array, length);
        }
        lockCount  = 1;
        transform  = newTransform;
        transform.transform(array, 0, array, 0, length/2);
        recomputed = true;
        return array;
    }

    /**
     * Signal that an array is no longer in use. This method <strong>must</strong>
     * be invoked after {@link #getRenderingArray}.
     *
     * @param array The array to release (got from {@link #getRenderingArray}).
     *
     * @task TODO: in some future version, we should wait a little bit longer
     *             before to change a strong reference into a soft one.
     */
    final void releaseRenderingArray(final float[] array) {
        if (array == null) {
            return;
        }
        final Object intern = this.array;
        if (intern == array) {
            // TODO: in some future version, we should wait a little bit longer
            //       before to change a strong reference into a soft one.
            this.array = new SoftReference(array);
        }
        else if (!(intern instanceof Reference) || ((Reference)intern).get()!=array) {
            // This cache doesn't own the array. Nothing to do.
            return;
        }
        lockCount--;
        assert lockCount >= 0;
    }

    /**
     * Returns the number of valid elements in the last array got from {@link #getRenderingArray}.
     * This is twice the number of valid points.
     */
    final int getLength() {
        return length;
    }

    /**
     * Returns <code>true</code> if the last call of {@link #getRenderingArray} has recomputed
     * the cache array. This information is for statistics purpose only.
     */
    final boolean recomputed() {
        return recomputed;
    }
}
