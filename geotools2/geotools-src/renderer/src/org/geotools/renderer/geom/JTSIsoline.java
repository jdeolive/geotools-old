/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
import java.util.List;
import java.util.ArrayList;

// JTS dependencies
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.sfs.SFSPoint;
import com.vividsolutions.jts.geom.sfs.SFSPolygon;
import com.vividsolutions.jts.geom.sfs.SFSGeometry;
import com.vividsolutions.jts.geom.sfs.SFSLineString;
import com.vividsolutions.jts.geom.sfs.SFSGeometryCollection;

// Geotools dependencies
import org.geotools.math.Statistics;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.renderer.array.JTSArray;


/**
 * An {@link Isoline} backed by one or many JTS {@link Geometry} objects.
 *
 * @version $Id: JTSIsoline.java,v 1.4 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class JTSIsoline extends Isoline {
    /**
     * Numéro de version pour compatibilité avec des
     * bathymétries enregistrées sous d'anciennes versions.
     */
    private static final long serialVersionUID = 1313504311244991561L;

    /**
     * Construct an initially empty isoline using the
     * {@linkplain org.geotools.cs.GeographicCoordinateSystem#WGS84 default}
     * geographic coordinate system. Polygons can be added using {@link #add}.
     * method.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     */
    public JTSIsoline(final float value) {
        super(value);
    }

    /**
     * Construct an initialy empty isoline. Polygon may be added using
     * {@link #add} method.
     *
     * @param value The value for this isoline. In the case
     *        of isobath, the value is the altitude.
     * @param coordinateSystem The coordinate system to use for all
     *        points in this isoline, or <code>null</code> if unknow.
     */
    public JTSIsoline(final float value, final CoordinateSystem cs) {
        super(value, cs);
    }

    /**
     * Construct an isoline for the specified geometry. The {@link #value} is computed
     * from the mean value of all {@link Coordinate#z} in the specified geometry.
     *
     * @param geometry The geometry to wrap, or <code>null</code> if none.
     *
     * @task TODO: The coordinate system currently default to WGS84. We should
     *             find it from the SRID code.
     */
    public JTSIsoline(final SFSGeometry geometry) {
        super((geometry!=null) ? (float)statistics(geometry).mean() : 0);
        if (geometry!=null) try {
            add(geometry);
        } catch (TransformException exception) {
            // Should not happen, since this isoline is suppose to be
            // set to the same coordinate system than the geometry.
            final IllegalArgumentException e;
            e = new IllegalArgumentException(exception.getLocalizedMessage());
            e.initCause(exception);
            throw e;
        }
    }

    /**
     * Returns the coordinate system for the specified JTS geometry.
     *
     * @task TODO: We should construct the coordinate system from SRID using
     *             {@link org.geotools.cs.CoordinateSystemAuthorityFactory}.
     */
    private CoordinateSystem getCoordinateSystem(final SFSGeometry geometry) {
        final int id = geometry.getSRID();
        // TODO: construct CS here.
        return getCoordinateSystem();
    }

    /**
     * Compute statistics about the <var>z</var> values in the specified geometry.
     * Statistics include minimum, maximum, mean value and standard deviation.
     * Unknow classes are ignored.
     *
     * @param  geometry The geometry to analyse.
     * @return The statistics.
     */
    private static Statistics statistics(final SFSGeometry geometry) {
        if (geometry instanceof SFSPolygon) {
            final SFSPolygon polygon = (SFSPolygon) geometry;
            final Statistics stats = statistics(polygon.getExteriorRing());
            final int n = polygon.getNumInteriorRing();
            for (int i=0; i<n; i++) {
                stats.add(statistics(polygon.getInteriorRingN(i)));
            }
            return stats;
        }
        final Statistics stats = new Statistics();
        if (geometry instanceof SFSGeometryCollection) {
            final SFSGeometryCollection collection = (SFSGeometryCollection) geometry;
            final int n = collection.getNumGeometries();
            for (int i=0; i<n; i++) {
                stats.add(statistics(collection.getGeometryN(i)));
            }
        }
        else if (geometry instanceof SFSPoint) {
            stats.add(((SFSPoint) geometry).getCoordinate().z);
        }
        else if (geometry instanceof SFSLineString) {
            final SFSLineString line = (SFSLineString) geometry;
            final int n = line.getNumPoints();
            for (int i=0; i<n; i++) {
                stats.add(line.getCoordinateN(i).z);
            }
        }
        return stats;
    }

    /**
     * Add the specified point to this isoline. This method should rarely be
     * used, since isoline are not designed for handling individual points.
     *
     * @param  geometry The point to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void addSF(final SFSPoint geometry) throws TransformException {
        final Coordinate[] coords;
        if (geometry instanceof Point) {
            coords = ((Point) geometry).getCoordinates();
        } else {
            coords = new Coordinate[] {geometry.getCoordinate()};
        }
        add(new Polygon(new JTSArray(coords), getCoordinateSystem(geometry)));
    }

    /**
     * Add the specified line string to this isoline.
     *
     * @param  geometry The line string to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void addSF(final SFSLineString geometry) throws TransformException {
        addSF(geometry, InteriorType.ELEVATION);
    }

    /**
     * Add the specified line string to this isoline. The shape will be closed using the
     * specified type (usually {@link InteriorType#ELEVATION}, except for holes in which
     * case it is {@link InteriorType#DEPRESSION}).
     *
     * @param  geometry The line string to add.
     * @param  type The type ({@link InteriorType#ELEVATION} or {@link InteriorType#DEPRESSION}).
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void addSF(final SFSLineString geometry, final InteriorType type)
            throws TransformException
    {
        final Coordinate[] coords;
        if (geometry instanceof LineString) {
            coords = ((LineString) geometry).getCoordinates();
        } else {
            coords = new Coordinate[geometry.getNumPoints()];
            for (int i=0; i<coords.length; i++) {
                coords[i] = geometry.getCoordinateN(i);
            }
        }
        final Polygon polygon = new Polygon(new JTSArray(coords), getCoordinateSystem(geometry));
        if (geometry.isRing()) {
            polygon.close(type);
        }
        add(polygon);
    }

    /**
     * Add the specified polygon to this isoline.
     *
     * @param  geometry The polygon to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void addSF(final SFSPolygon geometry) throws TransformException {
        addSF(geometry.getExteriorRing());
        final int n = geometry.getNumInteriorRing();
        for (int i=0; i<n; i++) {
            addSF(geometry.getInteriorRingN(i), InteriorType.DEPRESSION);
        }
    }

    /**
     * Add the specified geometry collection to this isoline.
     *
     * @param  geometry The geometry collection to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     */
    private void addSF(final SFSGeometryCollection geometry) throws TransformException {
        final int n = geometry.getNumGeometries();
        for (int i=0; i<n; i++) {
            addAny(geometry.getGeometryN(i));
        }
    }

    /**
     * Add the specified geometry to this isoline. The geometry must be one
     * of the following classes: {@link SFSPoint}, {@link SFSLineString},
     * {@link SFSPolygon} or {@link SFSGeometryCollection}.
     *
     * @param  geometry The geometry to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     * @throws IllegalArgumentException if the geometry is not a a valid class.
     */
    private void addAny(final SFSGeometry geometry) throws TransformException,
                                                           IllegalArgumentException
    {
        if (geometry instanceof SFSPoint) {
            addSF((SFSPoint) geometry);
            return;
        }
        if (geometry instanceof SFSLineString) {
            addSF((SFSLineString) geometry);
            return;
        }
        if (geometry instanceof SFSPolygon) {
            addSF((SFSPolygon) geometry);
            return;
        }
        if (geometry instanceof SFSGeometryCollection) {
            addSF((SFSGeometryCollection) geometry);
            return;
        }
        throw new IllegalArgumentException(Utilities.getShortClassName(geometry));
    }

    /**
     * Add the specified geometry to this isoline. The geometry must be one
     * of the following classes: {@link SFSPoint}, {@link SFSLineString},
     * {@link SFSPolygon} or {@link SFSGeometryCollection}.
     *
     * @param  geometry The geometry to add.
     * @throws TransformException if the specified geometry can't
     *         be transformed in this isoline's coordinate system.
     * @throws IllegalArgumentException if the geometry is not a a valid class.
     */
    public void add(final SFSGeometry geometry) throws TransformException, IllegalArgumentException
    {
        addAny(geometry);
        /*
         * TODO: If we want to keep reference to Geometry objects, keep them here (NOT in addAny).
         */
    }
}
