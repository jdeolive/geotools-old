/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_LinearUnit;
import org.opengis.cs.CS_Projection;
import org.opengis.cs.CS_ProjectedCoordinateSystem;
import org.opengis.cs.CS_GeographicCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.ct.MissingParameterException;

// J2SE and JAI dependencies
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import javax.media.jai.ParameterList;


/**
 * A 2D cartographic coordinate system. Projected coordinates are the
 * two-dimensional cartesian coordinates typically found on maps and computer
 * displays. The cartesian axes are often called "paper coordinates" or
 * "display coordinates."  The conversions from a three-dimensional curvilinear
 * coordinate system (whether ellipsoidal or spherical) to projected
 * coordinates may be assumed to be well known. Examples of projected
 * coordinate systems are: Lambert, Mercator, and transverse Mercator.
 * Conversions to, and conversions between, projected spatial coordinate
 * systems often do not preserve distances, areas and angles.
 *
 * @version $Id: ProjectedCoordinateSystem.java,v 1.7 2002/10/13 19:56:17 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_ProjectedCoordinateSystem
 */
public class ProjectedCoordinateSystem extends HorizontalCoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5412822472156531329L;
    
    /**
     * The linear unit.
     */
    private final Unit unit;
    
    /**
     * Geographic coordinate system to base projection on.
     */
    private final GeographicCoordinateSystem gcs;
    
    /**
     * Projection from geographic to projected coordinate system.
     */
    private final Projection projection;
    
    /**
     * Creates a projected coordinate system using the specified geographic
     * system. Projected coordinates will be in meters, <var>x</var> values
     * increasing east and <var>y</var> values increasing north.
     *
     * @param  name Name to give new object.
     * @param  gcs Geographic coordinate system to base projection on.
     * @param  projection Projection from geographic to projected coordinate system.
     */
    public ProjectedCoordinateSystem(final CharSequence               name,
                                     final GeographicCoordinateSystem gcs,
                                     final Projection                 projection)
    {
        this(name, gcs, projection, Unit.METRE, AxisInfo.X, AxisInfo.Y);
    }
    
    /**
     * Creates a projected coordinate system using a projection object.
     *
     * @param  name Name to give new object.
     * @param  gcs Geographic coordinate system to base projection on.
     * @param  projection Projection from geographic to projected coordinate system.
     * @param  unit Linear units of created PCS.
     * @param  axis0 Details of 0th ordinates in created PCS coordinates.
     * @param  axis1 Details of 1st ordinates in created PCS coordinates.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createProjectedCoordinateSystem
     */
    public ProjectedCoordinateSystem(final CharSequence               name,
                                     final GeographicCoordinateSystem gcs,
                                           Projection                 projection,
                                     final Unit                       unit,
                                     final AxisInfo                   axis0,
                                     final AxisInfo                   axis1)
    {
        super(name, gcs.getHorizontalDatum(), axis0, axis1);
        ensureNonNull("gcs",        gcs);
        ensureNonNull("projection", projection);
        ensureNonNull("unit",       unit);
        ensureLinearUnit(unit);
        
        final Ellipsoid ellipsoid = getHorizontalDatum().getEllipsoid();
        final double    semiMajor = ellipsoid.getSemiMajorAxis();
        final double    semiMinor = ellipsoid.getSemiMinorAxis();
        String invalidParameter = null;
        boolean resetAxisLength = false;
        try {
            if (semiMinor != projection.getValue("semi_minor")) {
                invalidParameter = "semi_minor";
            }
        } catch (MissingParameterException exception) {
            // Axis length not set.
            resetAxisLength = true;
        }
        try {
            if (semiMajor != projection.getValue("semi_major")) {
                invalidParameter = "semi_major";
            }
        } catch (MissingParameterException exception) {
            // Axis length not set.
            resetAxisLength = true;
        }
        if (invalidParameter != null) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_INCOMPATIBLE_ELLIPSOID_$2,
                    invalidParameter, ellipsoid.getName(null)));
        }
        if (resetAxisLength) {
            final ParameterList parameters = projection.getParameters();
            parameters.setParameter("semi_major", semiMajor);
            parameters.setParameter("semi_minor", semiMinor);
            projection = new Projection(projection.getName(null),
                                        projection.getClassName(),
                                        parameters);
        }
        this.gcs        = gcs;
        this.projection = projection;
        this.unit       = unit;
    }
    
    /**
     * Returns the geographic coordinate system.
     *
     * @see org.opengis.cs.CS_ProjectedCoordinateSystem#getGeographicCoordinateSystem()
     */
    public GeographicCoordinateSystem getGeographicCoordinateSystem() {
        return gcs;
    }
    
    /**
     * Gets the projection.
     *
     * @see org.opengis.cs.CS_ProjectedCoordinateSystem#getProjection()
     */
    public Projection getProjection() {
        return projection;
    }
    
    /**
     * Gets units for dimension within coordinate system.
     * This linear unit is the same for all axes.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_ProjectedCoordinateSystem#getUnits(int)
     * @see org.opengis.cs.CS_ProjectedCoordinateSystem#getLinearUnit()
     */
    public Unit getUnits(final int dimension) {
        if (dimension>=0 && dimension<getDimension()) {
            return unit;
        }
        throw new IndexOutOfBoundsException(Resources.format(
                ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Compare this coordinate system with the specified object for equality.
     *
     * @param  object The object to compare to <code>this</code>.
     * @param  compareNames <code>true</code> to comparare the {@linkplain #getName name},
     *         {@linkplain #getAlias alias}, {@linkplain #getAuthorityCode authority
     *         code}, etc. as well, or <code>false</code> to compare only properties
     *         relevant to transformations.
     * @return <code>true</code> if both objects are equal.
     */
    public boolean equals(final Info object, final boolean compareNames) {
        if (object == this) {
            return true;
        }
        if (super.equals(object, compareNames)) {
            final ProjectedCoordinateSystem that = (ProjectedCoordinateSystem) object;
            return equals(this.gcs,        that.gcs,        compareNames) &&
                   equals(this.projection, that.projection, compareNames) &&
                   equals(this.unit,       that.unit                    );
        }
        return false;
    }
    
    /**
     * Fills the part inside "[...]".
     * Used for formatting Well Known Text (WKT).
     */
    String addString(final StringBuffer buffer, final Unit context) {
        buffer.append(", ");
        buffer.append(gcs);
        buffer.append(", ");
        buffer.append(projection);
        buffer.append(", ");
        projection.addParameters(buffer, unit);
        addUnit(buffer, unit);
        buffer.append(", ");
        buffer.append(getAxis(0));
        buffer.append(", ");
        buffer.append(getAxis(1));
        return "PROJCS";
    }
    
    /**
     * Returns an OpenGIS interface for this projected coordinate
     * system. The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wraps a {@link ProjectedCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     */
    private final class Export extends HorizontalCoordinateSystem.Export implements CS_ProjectedCoordinateSystem {
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Returns the GeographicCoordinateSystem.
         */
        public CS_GeographicCoordinateSystem getGeographicCoordinateSystem() throws RemoteException {
            return adapters.export(ProjectedCoordinateSystem.this.getGeographicCoordinateSystem());
        }
        
        /**
         * Returns the LinearUnits.
         */
        public CS_LinearUnit getLinearUnit() throws RemoteException {
            return (CS_LinearUnit) adapters.export(ProjectedCoordinateSystem.this.getUnits());
        }
        
        /**
         * Gets the projection.
         */
        public CS_Projection getProjection() throws RemoteException {
            return adapters.export(ProjectedCoordinateSystem.this.getProjection());
        }
    }
}
