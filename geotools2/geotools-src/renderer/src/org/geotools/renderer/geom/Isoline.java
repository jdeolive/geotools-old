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

// Geometry and graphics
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

// Collections and utils
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Locale;

// Geotools dependencies
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.math.Statistics;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * An isoline built from a set of polylgons. An isoline is initially built with a {@linkplain
 * #value} (for example "50" for the 50 meters isobath) and a {@linkplain CoordinateSystem
 * coordinate system}. An arbitrary amount of {@linkplain Polygon polygons} can be added after
 * construction using {@link #add(Polygon)} or {@link #add(float[])}.
 * <br><br>
 * Note: this class has a natural ordering that is inconsistent with equals.
 * The {@link #compareTo} method compare only the isoline's values, while
 * {@link #equals} compares also all polygon points. The natural ordering
 * for <code>Isoline</code> is convenient for sorting isolines in increasing
 * order of altitude.
 *
 * <TABLE WIDTH="80%" ALIGN="center" CELLPADDING="18" BORDER="4" BGCOLOR="#FFE0B0"><TR><TD>
 * <P ALIGN="justify"><STRONG>This class may change in a future version, hopefully toward
 * ISO-19107. Do not rely on it.</STRONG>
 * </TD></TR></TABLE>
 *
 * @version $Id: Isoline.java,v 1.2 2003/02/04 12:30:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Polygon
 */
public class Isoline extends GeoShape implements Comparable {
    /**
     * Numéro de version pour compatibilité avec des
     * bathymétries enregistrées sous d'anciennes versions.
     */
    private static final long serialVersionUID = 6249238975475964338L;

    /**
     * The value for this isoline. In the case
     * of isobath, the value is the altitude.
     */
    public final float value;

    /**
     * Système de coordonnées.
     */
    private CoordinateSystem coordinateSystem;

    /**
     * Ensemble de polygones constituant cet isoligne. Les éléments de
     * ce tableau peuvent être classés de façon à améliorer la qualité
     * de l'affichage lorsqu'ils sont dessinés du dernier au premier.
     *
     * This array contains cloned polygon (remind: cloned polgons still share their data).
     * The <code>Isoline</code> API should never expose its internal polygons in anyway.
     * If a polygon is to be returned, it must be cloned again. Cloning polygons allow to
     * protect their state, especially the <code>Polygon.setRenderingResolution(...)</code>
     * value which is implementation details and should be hidden to the user. The only
     * exception to this rule is calls to {@link Polygon.Renderer#paint}, which do not
     * clone polygons for performance reasons.
     */
    private Polygon[] polygons;

    /**
     * Nombre d'éléments valides dans <code>polygons</code>.
     */
    private int polygonCount;

    /**
     * Indique si les polygones contenus dans le tableau <code>polygons</code>
     * ont été classés. Si ce n'est pas le cas, le classement devrait être fait
     * avant de dessiner les polygones.
     */
    private boolean sorted;

    /**
     * Rectangle englobant complètement cet isoligne. Ce rectangle est
     * calculé une fois pour toute et conservée dans une cache interne
     * pour accélérer certaines vérifications.
     */
    private Rectangle2D bounds;

    /**
     * The statistics about resolution, or <code>null</code> if none.
     * This object is computed when first requested and cached for next uses.
     */
    private Statistics resolution;

    /**
     * Construct an initialy empty isoline. Polygon may be added using one
     * of <code>add(...)</code> methods.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     * @param coordinateSystem The coordinate system to use for all
     *        points in this isoline, or <code>null</code> if unknow.
     *
     * @see #add(float[])
     * @see #add(Shape)
     * @see #add(Polygon)
     * @see #add(Isoline)
     */
    public Isoline(final float value, final CoordinateSystem coordinateSystem) {
        this.value = value;
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Construct an isoline with the same data than
     * the specified isoline. The new isoline will
     * have a copy semantic.
     */
    public Isoline(final Isoline isoline) {
        this.value            = isoline.value;
        this.coordinateSystem = isoline.coordinateSystem;
        this.polygonCount     = isoline.polygonCount;
        this.sorted           = isoline.sorted;
        this.bounds           = isoline.bounds;
        this.polygons         = new Polygon[polygonCount];
        for (int i=0; i<polygonCount; i++) {
            polygons[i] = (Polygon) isoline.polygons[i].clone();
        }
    }

    /**
     * Returns the isoline's coordinate system, or <code>null</code> if unknow.
     */
    public synchronized CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the isoline's coordinate system. Calling this method is equivalents
     * to reproject all polylgons from the old coordinate system to the new one.
     *
     * @param  The new coordinate system. A <code>null</code> value reset the
     *         coordinate system given at construction time.
     * @throws TransformException If a transformation failed. In case of failure,
     *         the state of this object will stay unchanged (as if this method has
     *         never been invoked).
     */
    public synchronized void setCoordinateSystem(final CoordinateSystem coordinateSystem)
            throws TransformException
    {
        bounds = null;
        final CoordinateSystem oldCoordinateSystem = this.coordinateSystem;
        if (Utilities.equals(oldCoordinateSystem, coordinateSystem)) return;
        int i=polygonCount;
        try {
            while (--i>=0) {
                polygons[i].setCoordinateSystem(coordinateSystem);
            }
            this.coordinateSystem = coordinateSystem; // Do it last.
        } catch (TransformException exception) {
            /*
             * If a map projection failed, reset
             * to the original coordinate system.
             */
            while (++i < polygonCount) {
                try {
                    polygons[i].setCoordinateSystem(oldCoordinateSystem);
                } catch (TransformException unexpected) {
                    // Should not happen, since the old coordinate system is supposed to be ok.
                    Polygon.unexpectedException("setCoordinateSystem", unexpected);
                }
            }
            throw exception;
        }
    }

    /**
     * Determines whetever the isoline is empty.
     */
    public synchronized boolean isEmpty() {
        for (int i=polygonCount; --i>=0;) {
            if (!polygons[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the bounding box of this isoline. This methode returns
     * a direct reference to the internally cached bounding box. DO
     * NOT MODIFY!
     */
    final Rectangle2D getCachedBounds() {
        assert Thread.holdsLock(this);
        if (bounds == null) {
            for (int i=polygonCount; --i>=0;) {
                final Polygon polygon = polygons[i];
                if (!polygon.isEmpty()) {
                    final Rectangle2D polygonBounds=polygon.getBounds2D();
                    if (bounds == null) {
                        bounds = polygonBounds;
                    } else {
                        bounds.add(polygonBounds);
                    }
                }
            }
            if (bounds == null) {
                bounds = new Rectangle2D.Float();
            }
        }
        return bounds;
    }

    /**
     * Return the bounding box of this isoline, including its possible
     * borders. This method uses a cache, such that after a first calling,
     * the following calls should be fairly quick.
     *
     * @return A bounding box of this isoline. Changes to the
     *         fields of this rectangle will not affect the cache.
     */
    public synchronized Rectangle2D getBounds2D() {
        return (Rectangle2D) getCachedBounds().clone();
    }

    /**
     * Returns the smallest bounding box containing {@link #getBounds2D}.
     *
     * @deprecated This method is required by the {@link Shape} interface,
     *             but it doesn't provides enough precision for most cases.
     *             Use {@link #getBounds2D()} instead.
     */
    public synchronized Rectangle getBounds() {
        final Rectangle rect = new Rectangle();
        rect.setRect(getCachedBounds()); // Perform the appropriate rounding.
        return rect;
    }

    /**
     * Indique si le point (<var>x</var>,<var>y</var>) spécifié est à l'intérieur
     * de cet isoligne. Les coordonnées du point doivent être exprimées selon le système
     * de coordonnées de l'isoligne, soit {@link #getCoordinateSystem()}. Cette méthode
     * recherchera le plus petit polygone qui contient le polygone spécifié, et retournera
     * <code>true</code> si ce point est une élévation (par exemple une île) ou
     * <code>false</code> s'il est une dépression (par exemple un lac).
     */
    public synchronized boolean contains(final double x, final double y) {
        if (getCachedBounds().contains(x,y)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                if (polygon.contains(x,y)) {
                    final InteriorType interiorType = polygon.getInteriorType();
                    if (InteriorType.ELEVATION.equals(interiorType)) {
                        return true;
                    }
                    if (InteriorType.DEPRESSION.equals(interiorType)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Indique si le point spécifié spécifiée est à l'intérieur de cet isoligne. Les coordonnées
     * du point doivent être exprimées selon le système de coordonnées de l'isoligne, soit
     * {@link #getCoordinateSystem()}. Cette méthode recherchera le plus petit polygone qui
     * contient le point spécifié, et retournera <code>true</code> si ce polygone est une élévation
     * (par exemple une île) ou <code>false</code> s'il est une dépression (par exemple un lac).
     */
    public synchronized boolean contains(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                if (polygon.contains(point)) {
                    final InteriorType interiorType = polygon.getInteriorType();
                    if (InteriorType.ELEVATION.equals(interiorType)) {
                        return true;
                    }
                    if (InteriorType.DEPRESSION.equals(interiorType)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si le rectangle spécifié est entièrement compris dans cet isoligne.
     * Les coordonnées du rectangle doivent être exprimées selon le système de
     * coordonnées de l'isoligne, soit {@link #getCoordinateSystem()}. Cette
     * méthode recherchera le plus petit polygone qui contient le rectangle spécifié,
     * et retournera <code>true</code> si ce polygone est une élévation (par exemple
     * une île) ou <code>false</code> s'il est une dépression (par exemple un lac).
     */
    public synchronized boolean contains(final Rectangle2D rect) {
        if (getCachedBounds().contains(rect)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.contains(rect)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.intersects(rect)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si la forme spécifiée est entièrement compris dans cet isoligne.
     * Les coordonnées de la forme doivent être exprimées selon le système de
     * coordonnées de l'isoligne, soit {@link #getCoordinateSystem()}. Cette
     * méthode recherchera le plus petit polygone qui contient la forme spécifiée,
     * et retournera <code>true</code> si ce polygone est une élévation (par exemple
     * une île) ou <code>false</code> s'il est une dépression (par exemple un lac).
     */
    public synchronized boolean contains(final Shape shape) {
        if (getCachedBounds().contains(shape.getBounds2D())) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.contains(shape)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.intersects(shape)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Test if the specified rectangle intercept with
     * the interior of a polylgons of this isoline.
     */
    public synchronized boolean intersects(final Rectangle2D rect) {
        if (getCachedBounds().intersects(rect)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.intersects(rect)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.contains(rect)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Test if the specified shape intercept with
     * the interior of a polylgons of this isoline.
     */
    public synchronized boolean intersects(final Shape shape) {
        if (getCachedBounds().intersects(shape.getBounds2D())) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final Polygon polygon = polygons[i];
                final InteriorType interiorType = polygon.getInteriorType();
                if (InteriorType.ELEVATION.equals(interiorType)) {
                    if (polygon.intersects(shape)) {
                        return true;
                    }
                } else if (InteriorType.DEPRESSION.equals(interiorType)) {
                    if (polygon.contains(shape)) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the string to be used as the tooltip for the given location.
     * If there is no such tooltip, returns <code>null</code>. The default
     * implementation search for a polygon's tooltip at the given location.
     *
     * @param  point Coordinates (usually mouse coordinates). Must be
     *         specified in this isoline's coordinate system
     *         (as returned by {@link #getCoordinateSystem}).
     * @param  locale The desired locale for the tool tips.
     * @return The tooltip text for the given location,
     *         or <code>null</code> if there is none.
     */
    public synchronized String getToolTipText(final Point2D point, final Locale locale) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            for (int i=0; i<polygonCount; i++) {
                final String name = polygons[i].getToolTipText(point, locale);
                if (name != null) {
                    return name;
                }
            }
        }
        return null;
    }

    /**
     * Paint this isoline using the specified {@link Polygon.Renderer}.
     * This method is faster than <code>graphics.draw(this)</code> since
     * it reuse internal cache when possible.
     *
     * @param  renderer The destination renderer. The {@link Polygon.Renderer#paint}
     *         method will be invoked for each polygon to renderer.
     */
    public synchronized void paint(final Polygon.Renderer renderer) {
        int rendered=0, recomputed=0;
        double meanResolution = 0;
        final Shape clip = renderer.getClip();
        if (clip.intersects(getCachedBounds())) {
            if (!sorted) {
                sort();
            }
            for (int i=polygonCount; --i>=0;) {
                final Polygon polygon = polygons[i];
                synchronized (polygon) {
                    if (clip.intersects(polygon.getCachedBounds())) {
                        /*
                         * Compute the rendering resolution and paint the polygon.
                         */
                        float resolution = polygon.getRenderingResolution();
                        resolution = renderer.getRenderingResolution(resolution);
                        polygon.setRenderingResolution(resolution);
                        renderer.paint(polygon);
                        /*
                         * Get statistical data for monitoring the cache performance.
                         */
                        final PolygonCache cache = polygon.getCache();
                        final int numPts = cache.getLength()/2;
                        rendered += numPts;
                        if (cache.recomputed()) {
                            recomputed += numPts;
                        }
                        meanResolution += resolution * numPts;
                    }
                }
            }
        }
        meanResolution /= rendered;
        renderer.paintCompleted(rendered, recomputed, meanResolution);
    }

    /**
     * Returns a path iterator for this isoline.
     */
    public synchronized PathIterator getPathIterator(final AffineTransform transform) {
        return new PolygonPathIterator(getPolygonList(true).iterator(), transform);
    }

    /**
     * Returns a path iterator for this isoline.
     */
    public PathIterator getPathIterator(final AffineTransform transform, final double flatness) {
        return getPathIterator(transform);
    }

    /**
     * Return the number of points describing this isobath.
     */
    public synchronized int getPointCount() {
        int total=0;
        for (int i=polygonCount; --i>=0;) {
            total += polygons[i].getPointCount();
        }
        return total;
    }

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purpose only. The memory really used by two isolines may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values,  since isolines try to
     * share their data when possible. Furthermore, this method do not take in account
     * the extra bytes generated by Java Virtual Machine for each objects.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    final synchronized long getMemoryUsage() {
        long total = 48;
        if (polygons != null) {
            total += 4*polygons.length;
        }
        for (int i=polygonCount; --i>=0;) {
            total += polygons[i].getMemoryUsage();
        }
        return total;
    }

    /**
     * Returns the set of {@link Polygon} objects in this isoline. If possible,
     * the set's iterator will return continents last and small lakes within the
     * continents fist. Therefore, rendering done in a reverse order should yield
     * good results.
     * <br><br>
     * The order in which the iterator returns {@link Polygon} objects make it
     * convenient to find the smallest island or lake that a given point contains.
     * For example:
     *
     * <blockquote><pre>
     * &nbsp;public Polygon getSmallestIslandAt(double x, double y) {
     * &nbsp;    final Iterator it = isobath.getPolygons().iterator();
     * &nbsp;    while (it.hasNext()) {
     * &nbsp;        final Polygon île=(Polygon) it.next();
     * &nbsp;        if (île.contains(x,y)) {
     * &nbsp;            return île;
     * &nbsp;        }
     * &nbsp;    }
     * &nbsp;    return null;
     * &nbsp;}
     * </pre></blockquote>
     *
     * @return A set of {@link Polygon} objects.
     */
    public synchronized Set getPolygons() {
        return new LinkedHashSet(getPolygonList(false));
    }

    /**
     * Returns the set of polygons as a list. This method is faster than
     * {@link #getPolygons} and is optimized for {@link #getPathIterator}.
     *
     * @param  reverse <code>true</code> for reversing order (i.e. returning
     *         big continents first, and small lakes or islands last). This
     *         reverse order is appropriate for rendering, while the "normal"
     *         order is more appropriate for searching a polygon.
     * @return The set of polygons. This set will be ordered if possible.
     */
    private List getPolygonList(final boolean reverse) {
        assert Thread.holdsLock(this);
        if (!sorted) {
            sort();
        }
        final List list = new ArrayList(polygonCount);
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = (Polygon) polygons[i].clone();
            polygon.setRenderingResolution(0);
            list.add(polygon);
        }
        if (!reverse) {
            // Elements was inserted in reverse order.
            // (remind: this method is optimized for getPathIterator)
            Collections.reverse(list);
        }
        return list;
    }

    /**
     * Returns the set of polygons containing the specified point.
     *
     * @param  point A coordinate expressed according {@link #getCoordinateSystem}.
     * @return The set of polygons under the specified point.
     */
    public synchronized Set getPolygons(final Point2D point) {
        if (getCachedBounds().contains(point)) {
            if (!sorted) {
                sort();
            }
            final Polygon[] copy = new Polygon[polygonCount];
            System.arraycopy(polygons, 0, copy, 0, polygonCount);
            return new FilteredSet(copy, point, null, null);
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the set of polygons containing or intersecting the specified shape.
     *
     * @param  shape A shape with coordinates expressed according {@link #getCoordinateSystem}.
     * @param  intersects <code>false</code> to search for polygons containing <code>shape</code>,
     *         or <code>true</code> to search for polygons intercepting <code>shape</code>.
     * @return The set of polygons containing or intersecting the specified shape.
     */
    public synchronized Set getPolygons(final Shape shape, final boolean intersects) {
        if (shape.intersects(getCachedBounds())) {
            if (!sorted) {
                sort();
            }
            final Polygon[] copy = new Polygon[polygonCount];
            System.arraycopy(polygons, 0, copy, 0, polygonCount);
            if (intersects) {
                return new FilteredSet(copy, null, null, shape);
            } else {
                return new FilteredSet(copy, null, shape, null);
            }
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Ajoute des points à cet isobath. Les données doivent être écrites sous forme de
     * paires (<var>x</var>,<var>y</var>) dans le système de coordonnées de cet isoligne
     * ({@link #getCoordinateSystem}). Les <code>NaN</code> seront considérés comme des
     * trous; aucune ligne ne joindra les points entre deux <code>NaN</code>.
     *
     * @param  data Tableau de coordonnées (peut contenir des NaN). Ces données seront copiées,
     *         de sorte que toute modification future de <code>data</code> n'aura pas d'impact
     *         sur les polygones créés.
     */
    public synchronized void add(final float[] array) {
        final Polygon[] toAdd = Polygon.getInstances(array, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            if (!toAdd[i].isEmpty()) {
                addImpl(toAdd[i]);
            }
        }
    }

    /**
     * Add polygons from the specified shape. Shape's coordinates
     * must be express in this isoline's coordinate system.
     *
     * @param  shape The shape to add.
     * @throws IllegalArgumentException if the specified shape can't be added. This error may
     *         occurs if <code>shape</code> is an instance of {@link Polygon} or {@link Isoline}
     *         and use an incompatible coordinate system and/or {@linkplain #value}.
     */
    public synchronized void add(final Shape shape) throws IllegalArgumentException {
        try {
            if (shape instanceof Polygon) {
                add((Polygon) shape);
                return;
            }
            if (shape instanceof Isoline) {
                add((Isoline) shape);
                return;
            }
        } catch (TransformException exception) {
            // TODO: localize this message, if it worth it.
            final IllegalArgumentException e = new IllegalArgumentException("Incompatible CS");
            e.initCause(exception);
            throw e;
        }
        final Polygon[] toAdd = Polygon.getInstances(shape, coordinateSystem);
        for (int i=0; i<toAdd.length; i++) {
            if (!toAdd[i].isEmpty()) {
                addImpl(toAdd[i]);
            }
        }
    }

    /**
     * Add all polygons from the specified isoline. Both isolines must have
     * the same {@linkplain #value}.
     *
     * @param  toAdd Isoline to add.
     * @throws IllegalArgumentException if both isolines doesn't
     *         have the same {@linkplain #value}.
     * @throws TransformException if the specified isoline can't
     *         be transformed in this isoline's coordinate system.
     */
    public synchronized void add(final Isoline toAdd) throws TransformException {
        if (toAdd != null) {
            if (Float.floatToIntBits(toAdd.value) != Float.floatToIntBits(value)) {
                // TODO: localize this message, if it worth it.
                throw new IllegalArgumentException("Incompatible values");
            }
            final Polygon[] polyToAdd = (Polygon[]) toAdd.polygons.clone();
            for (int i=0; i<polyToAdd.length; i++) {
                add(polyToAdd[i]);
            }
        }
    }

    /**
     * Add a polygon to this isoline.
     *
     * @param  toAdd Polylgon to add.
     * @throws TransformException if the specified polygon can't
     *         be transformed in this isoline's coordinate system.
     */
    public synchronized void add(Polygon toAdd) throws TransformException {
        if (toAdd != null) {
            toAdd = (Polygon) toAdd.clone();
            if (coordinateSystem != null) {
                toAdd.setCoordinateSystem(coordinateSystem);
            } else {
                coordinateSystem = toAdd.getCoordinateSystem();
                if (coordinateSystem!=null) {
                    setCoordinateSystem(coordinateSystem);
                }
            }
            addImpl(toAdd);
        }
    }

    /**
     * Add a polylgon to this isoline. This method do not clone
     * the polygon and doesn't set the coordinate system.
     */
    private void addImpl(final Polygon toAdd) {
        assert Thread.holdsLock(this);
        if (polygons == null) {
            polygons = new Polygon[16];
        }
        if (polygonCount >= polygons.length) {
            polygons = (Polygon[])XArray.resize(polygons, polygonCount+Math.min(polygonCount, 256));
        }
        polygons[polygonCount++] = toAdd;
        sorted = false;
        bounds = null;
    }

    /**
     * Remove a polylgon from this isobath.
     *
     * @param toRemove The polygon to remove.
     * @return <code>true</code> if the polylgon has been removed.
     */
    public synchronized boolean remove(final Polygon toRemove) {
        boolean removed = false;
        for (int i=polygonCount; --i>=0;) {
            if (polygons[i].equals(toRemove)) {
                remove(i);
                removed = true;
            }
        }
        return removed;
        // No change to sorting order.
    }

    /**
     * Remote the polylgon at the specified index.
     */
    private void remove(final int index) {
        assert Thread.holdsLock(this);
        bounds = null;
        System.arraycopy(polygons, index+1, polygons, index, polygonCount-(index+1));
        polygons[--polygonCount] = null;
    }

    /**
     * Returns the isoline's resolution.  The mean resolution is the mean distance between
     * every pair of consecutive points in this isoline  (ignoring "extra" points used for
     * drawing a border, if there is one). This method try to express the resolution in
     * linear units (usually meters) no matter if the coordinate systems is actually a
     * {@link ProjectedCoordinateSystem} or a {@link GeographicCoordinateSystem}.
     * More specifically:
     * <ul>
     *   <li>If the coordinate system is a {@linkplain GeographicCoordinateSystem geographic}
     *       one, then the resolution is expressed in units of the underlying
     *       {@linkplain Ellipsoid#getAxisUnit ellipsoid's axis length}.</li>
     *   <li>Otherwise (especially if the coordinate system is a {@linkplain
     *       ProjectedCoordinateSystem projected} one), the resolution is expressed in
     *       {@linkplain ProjectedCoordinateSystem#getUnits units of the coordinate system}.</li>
     * </ul>
     *
     * @see Polygon#getResolution
     */
    public synchronized Statistics getResolution() {
        if (resolution == null) {
            for (int i=polygonCount; --i>=0;) {
                final Statistics toAdd = polygons[i].getResolution();
                if (resolution == null) {
                    resolution = toAdd;
                } else {
                    resolution.add(toAdd);
                }
            }
        }
        return (Statistics) resolution.clone();
    }

    /**
     * Set the isoline's resolution. This method try to interpolate new points in such a way
     * that every point is spaced by exactly <code>resolution</code> units (usually meters)
     * from the previous one.
     *
     * @param  resolution Desired resolution, in the same units than {@link #getResolution}.
     * @throws TransformException If some coordinate transformations were needed and failed.
     *         There is no guaranteed on contour's state in case of failure.
     *
     * @see Polygon#setResolution
     */
    public synchronized void setResolution(final double resolution) throws TransformException {
        bounds = null;
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = polygons[i];
            polygon.setResolution(resolution);
            if (polygon.isEmpty()) {
                remove(i);
            }
        }
    }

    /**
     * Compress all polygons in this isoline. This method invokes {@link Polygon#compress}
     * for each polygon.
     *
     * @param  factor Facteur contrôlant la baisse de résolution.  Les valeurs élevées
     *         déciment davantage de points, ce qui réduit d'autant la consommation de
     *         mémoire. Ce facteur est généralement positif, mais il peut aussi être 0
     *         ou même légèrement négatif.
     * @return A <em>estimation</em> of the compression rate. For example a value of 0.2
     *         means that the new polygon use <em>approximatively</em> 20% less memory.
     *         Warning: this value may be inacurate, for example if the old polygon was
     *         used to shares its data with an other polygon, compressing one polygon
     *         may actually increase memory usage since the two polygons will no longer
     *         share their data.
     * @throws TransformException Si une erreur est survenue lors d'une projection cartographique.
     *
     * @see Polygon#compress
     */
    public synchronized float compress(final float factor) throws TransformException {
        polygons = (Polygon[]) XArray.resize(polygons, polygonCount);
        bounds   = null;
        final long memoryUsage = getMemoryUsage();
        for (int i=polygonCount; --i>=0;) {
            final Polygon polygon = polygons[i];
            polygon.compress(factor);
            if (polygon.isEmpty()) {
                remove(i);
            }
        }
        return (float) (memoryUsage - getMemoryUsage()) / (float) memoryUsage;
        // No change to sorting order.
    }

    /**
     * Returns an isoline approximatively equals to this isoline clipped to the specified bounds.
     * The clip is only approximative  in that  the resulting isoline may extends outside the clip
     * area. However, it is garanted that the resulting isoline contains at least all the interior
     * of the clip area.
     *
     * If this method can't performs the clip, or if it believe that it doesn't worth to do a clip,
     * it returns <code>this</code>. If this isoline doesn't intersect the clip area, then this method
     * returns <code>null</code>. Otherwise, a new isoline is created and returned. The new isoline
     * will try to share as much internal data as possible with <code>this</code> in order to keep
     * memory footprint low.
     *
     * @param  clipper An object containing the clip area.
     * @return <code>null</code> if this isoline doesn't intersect the clip, <code>this</code>
     *         if no clip has been performed, or a new clipped isoline otherwise.
     */
//    final Isoline getClipped(final Clipper clipper) {
//        final Rectangle2D clipRegion = clipper.setCoordinateSystem(coordinateSystem);
//        final Polygon[] clipPolygons = new Polygon[polygonCount];
//        int         clipPolygonCount = 0;
//        boolean              changed = false;
//        /*
//         * Clip all polygons, discarding
//         * polygons outside the clip.
//         */
//        for (int i=0; i<polygonCount; i++) {
//            final Polygon toClip  = polygons[i];
//            final Polygon clipped = toClip.getClipped(clipper);
//            if (clipped!=null && !clipped.isEmpty()) {
//                clipPolygons[clipPolygonCount++] = clipped;
//                if (!toClip.equals(clipped)) {
//                    changed = true;
//                }
//            } else {
//                changed = true;
//            }
//        }
//        if (changed) {
//            /*
//             * If at least one polygon has been clipped, returns a new isoline.
//             * Note: we set the new bounds to the clip region. It may be bigger
//             * than computed bounds, but it is needed for optimal behaviour of
//             * {@link RenderingContext#clip}. Clipped isolines should not be
//             * public anyways (except for very short time).
//             */
//             final Isoline isoline = new Isoline(value, coordinateSystem);
//             isoline.polygons      = XArray.resize(clipPolygons, clipPolygonCount);
//             isoline.polygonCount  = clipPolygonCount;
//             isoline.bounds        = clipRegion;
//             isoline.setName(super.getName(null));
//             return isoline;
//        } else {
//            return this;
//        }
//    }

    /**
     * Returns a hash value for this isoline.
     */
    public synchronized int hashCode() {
        int code = 4782135;
        for (int i=0; i<polygonCount; i++) {
            // Must be insensitive to order.
            code += polygons[i].hashCode();
        }
        return code;
    }

    /**
     * Compare this isoline with the specified isoline.   Note that this method is
     * inconsistent with <code>equals</code>. <code>compareTo</code> compares only
     * isoline values, while <code>equals</code> compare all polygon points.   The
     * natural ordering for <code>Isoline</code> is convenient for sorting isolines
     * in increasing order of altitude.
     *
     * @param  iso The isoline to compare value with.
     * @return <ul>
     *           <li>+1 if this isoline's value is greater than the specified isoline value.</li>
     *           <li>-1 if this isoline's value is less than the specified isoline value.</li>
     *           <li>0 is both isoline has the same value.</li>
     *         </ul>
     */
    public int compareTo(final Object iso) {
        return Float.compare(value, ((Isoline) iso).value);
    }

    /**
     * Compares the specified object with this isoline for equality.
     * This methods checks for isoline values ({@link #value})  and
     * all polygon points. This is different from {@link #compareTo},
     * which compares only isoline values.
     */
    public synchronized boolean equals(final Object object) {
        if (object==this) {
            // Slight optimization
            return true;
        }
        if (super.equals(object)) {
            final Isoline that = (Isoline) object;
            if (Float.floatToIntBits(this.value) == Float.floatToIntBits(that.value) &&
                this.polygonCount == that.polygonCount)
            {
                // Compare ignoring order. Note: we don't call any synchronized
                // methods on 'that' in order to avoid dead lock.
                return getPolygons().containsAll(that.getPolygonList(true));
            }
        }
        return false;
    }

    /**
     * Return a copy of this isoline. The clone has a deep copy semantic,
     * but will shares many internal arrays with the original isoline.
     *
     * @see Polygon#clone
     */
    public synchronized Object clone() {
        // Note: we can't use the 'Isoline(Isoline)' constructor,
        //       because the user way have subclassed this isoline.
        final Isoline isoline = (Isoline) super.clone();
        isoline.polygons = new Polygon[polygonCount];
        for (int i=isoline.polygons.length; --i>=0;) {
            isoline.polygons[i] = (Polygon) polygons[i].clone();
        }
        return isoline;
    }

    /**
     * Efface toutes les informations qui étaient conservées dans une cache interne.
     * Cette méthode peut être appelée lorsque l'on sait que cet isoligne ne sera plus
     * utilisé avant un certain temps. Elle ne cause la perte d'aucune information,
     * mais rendra les prochaines utilisations de cet isoligne plus lentes (le temps
     * que les caches internes soient reconstruites, après quoi l'isoligne retrouvera
     * sa vitesse normale).
     */
    final void clearCache() {
        bounds     = null;
        resolution = null;
        for (int i=polygonCount; --i>=0;) {
            polygons[i].clearCache();
        }
    }

    /**
     * Classe les polygones de façon à faire apparaître les petites îles
     * ou les lacs en premiers, et les gros continents en derniers.
     *
     * @task TODO: Not yet implemented.
     */
    private void sort() {
        // TODO
    }




    /**
     * The set of polygons under a point. The check of inclusion
     * or intersection will be performed only when needed.
     *
     * @version $Id: Isoline.java,v 1.2 2003/02/04 12:30:52 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class FilteredSet extends AbstractSet {
        /**
         * The polygons to check. This array must be a copy of
         * {@link Isoline#polygons}. It will be changed during
         * iteration: polygons that do not obey to condition
         * will be set to <code>null</code>.
         */
        final Polygon[] polygons;

        /**
         * The point to check for inclusion, or <code>null</code> if none.
         */
        private final Point2D point;

        /**
         * The shape to check for inclusion, or <code>null</code> if none.
         */
        private final Shape contains;

        /**
         * The shape to check for intersection, or <code>null</code> if none.
         */
        private final Shape intersects;

        /**
         * Index of the next polygon to check. All polygons
         * before this index are considered valid.
         */
        private int upper;

        /**
         * Construct a filtered set.
         *
         * @param polygons The polygon array. This array <strong>must be a copy</strong>
         *                 of {@link Isoline#polygons}. It must not be the original!
         */
        public FilteredSet(final Polygon[] polygons, final Point2D point,
                           final Shape contains, final Shape intersects)
        {
            this.polygons   = polygons;
            this.point      = point;
            this.contains   = contains;
            this.intersects = intersects;
        }

        /**
         * Returns the index of the next valid polygon starting at of after the specified
         * index. If there is no polygon left, returns a number greater than or equals to
         * <code>polygons.length</code>. This method should be invoked with increasing
         * value of <code>from</code> only (values in random order are not supported).
         */
        final int next(int from) {
            while (from < polygons.length) {
                Polygon polygon = polygons[from];
                if (polygon != null) {
                    if (from >= upper) {
                        // This polygon has not been
                        // checked yet for validity.
                        upper = from+1;
                        if ((     point!=null && !polygon.contains  (point   )) ||
                            (  contains!=null && !polygon.contains  (contains)) ||
                            (intersects!=null && !polygon.intersects(intersects)))
                        {
                            polygons[from] = null;
                            continue;
                        }
                        polygon = (Polygon) polygon.clone();
                        polygon.setRenderingResolution(0);
                        polygons[from] = polygon;
                    }
                    break;
                }
            }
            return from;
        }
        
        /**
         * Returns the number of elements in this collection.
         */
        public int size() {
            int count = 0;
            for (int i=next(0); i<polygons.length; i=next(i+1)) {
                count++;
            }
            return count;
        }

        /**
         * Returns an iterator over the elements in this collection.
         */
        public Iterator iterator() {
            return new Iterator() {
                /** Index of the next valid polygon. */
                private int index = FilteredSet.this.next(0);

                /** Check if there is more polygons. */
                public boolean hasNext() {
                    return index < polygons.length;
                }

                /** Returns the next polygon. */
                public Object next() {
                    if (index < polygons.length) {
                        final Polygon next = polygons[index];
                        index = FilteredSet.this.next(index+1);
                        return next;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                /** Unsupported operation. */
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
