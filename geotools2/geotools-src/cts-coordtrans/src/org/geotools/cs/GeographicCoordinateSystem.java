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
import org.opengis.cs.CS_AngularUnit;
import org.opengis.cs.CS_PrimeMeridian;
import org.opengis.cs.CS_WGS84ConversionInfo;
import org.opengis.cs.CS_GeographicCoordinateSystem;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.pt.Envelope;
import org.geotools.pt.Latitude;
import org.geotools.pt.Longitude;
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.util.Set;
import java.util.Collections;
import java.rmi.RemoteException;


/**
 * A coordinate system based on latitude and longitude.
 * Some geographic coordinate systems are <var>latitude</var>/<var>longiude</var>,
 * and some are <var>longitude</var>/<var>latitude</var>. You can find out
 * which this is by examining the axes. You should also check the angular
 * units, since not all geographic coordinate systems use degrees.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_GeographicCoordinateSystem
 */
public class GeographicCoordinateSystem extends HorizontalCoordinateSystem {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2024367470686889008L;
    
    /**
     * A geographic coordinate system using WGS84 datum.
     * This coordinate system use <var>longitude</var>/<var>latitude</var> ordinates
     * with longitude values increasing north and latitude values increasing east.
     * Angular units are degrees and prime meridian is Greenwich.
     */
    public static final GeographicCoordinateSystem WGS84 = (GeographicCoordinateSystem) pool.canonicalize(
                    new GeographicCoordinateSystem("WGS84", HorizontalDatum.WGS84));
    
    /**
     * The angular unit.
     */
    private final Unit unit;
    
    /**
     * The prime meridian.
     */
    private final PrimeMeridian meridian;
    
    /**
     * Creates a geographic coordinate system.  This coordinate system will use
     * <var>longitude</var>/<var>latitude</var> ordinates with longitude values
     * increasing east and latitude values increasing north.  Angular units are
     * degrees and prime meridian is Greenwich.
     *
     * @param name      Name to give new object.
     * @param datum     Horizontal datum for created coordinate system.
     */
    public GeographicCoordinateSystem(final CharSequence name, final HorizontalDatum datum)
    {
        this(name, Unit.DEGREE, datum, PrimeMeridian.GREENWICH,
             AxisInfo.LONGITUDE, AxisInfo.LATITUDE);
    }
    
    /**
     * Creates a geographic coordinate system, which could be <var>latitude</var>/<var>longiude</var>
     * or <var>longitude</var>/<var>latitude</var>.
     *
     * @param name      Name to give new object.
     * @param unit      Angular units for created coordinate system.
     * @param datum     Horizontal datum for created coordinate system.
     * @param meridian  Prime Meridian for created coordinate system.
     * @param axis0     Details of 0th ordinates.
     * @param axis1     Details of 1st ordinates.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createGeographicCoordinateSystem
     */
    public GeographicCoordinateSystem(final CharSequence    name,
                                      final Unit            unit,
                                      final HorizontalDatum datum,
                                      final PrimeMeridian   meridian,
                                      final AxisInfo        axis0,
                                      final AxisInfo        axis1)
    {
        super(name, datum, axis0, axis1);
        ensureNonNull("unit",     unit);
        ensureNonNull("meridian", meridian);
        ensureAngularUnit(unit);
        this.unit     = unit;
        this.meridian = meridian;
    }
    
    /**
     * Gets units for dimension within coordinate system.
     * This angular unit is the same for all axis.
     *
     * @param dimension Zero based index of axis.
     *
     * @see org.opengis.cs.CS_GeographicCoordinateSystem#getUnits(int)
     */
    public Unit getUnits(final int dimension) {
        if (dimension>=0 && dimension<getDimension()) {
            return unit;
        }
        throw new IndexOutOfBoundsException(Resources.format(
                ResourceKeys.ERROR_INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension)));
    }
    
    /**
     * Returns the prime meridian.
     *
     * @see org.opengis.cs.CS_GeographicCoordinateSystem#getPrimeMeridian()
     */
    public PrimeMeridian getPrimeMeridian() {
        return meridian;
    }
    
    /**
     * Gets default envelope of coordinate system.
     *
     * @see org.opengis.cs.CS_GeographicCoordinateSystem#getDefaultEnvelope()
     */
    public Envelope getDefaultEnvelope() {
        final int dimension = getDimension();
        final CoordinatePoint minCP = new CoordinatePoint(dimension);
        final CoordinatePoint maxCP = new CoordinatePoint(dimension);
        for (int i=0; i<dimension; i++) {
            double min, max;
            final Unit unit = getUnits(i);
            final AxisOrientation orientation = getAxis(i).orientation;
            if (AxisOrientation.NORTH.equals(orientation.absolute())) {
                min = Latitude.MIN_VALUE;
                max = Latitude.MAX_VALUE;
            } else if (AxisOrientation.EAST.equals(orientation.absolute())) {
                min = Longitude.MIN_VALUE;
                max = Longitude.MAX_VALUE;
            } else {
                min = Double.NEGATIVE_INFINITY;
                max = Double.POSITIVE_INFINITY;
            }
            min = unit.convert(min, Unit.DEGREE);
            max = unit.convert(max, Unit.DEGREE);
            minCP.ord[i] = Math.min(min, max);
            maxCP.ord[i] = Math.max(min, max);
        }
        return new Envelope(minCP, maxCP);
    }
    
    /**
     * Gets details on conversions to WGS84.  Some geographic coordinate systems
     * provide several transformations into WGS84, which are designed to provide
     * good accuracy in different areas of interest. The first conversion should
     * provide acceptable accuracy over the largest possible area of interest.
     *
     * @return A set of conversions info to WGS84. The default
     *         implementation returns an empty set.
     *
     * @see org.opengis.cs.CS_GeographicCoordinateSystem#getNumConversionToWGS84()
     * @see org.opengis.cs.CS_GeographicCoordinateSystem#getWGS84ConversionInfo(int)
     */
    public Set getWGS84ConversionInfos() {
        return Collections.EMPTY_SET;
    }
    
    /**
     * Returns  <code>true</code> if this coordinate system is equivalents to
     * the specified coordinate system. Two coordinate systems are considered
     * equivalent if the {@link org.geotools.ct.CoordinateTransformation} from
     * <code>this</code> to <code>cs</code> would be the identity transform.
     * The default implementation compare datum, units and axis, but ignore
     * name, alias and other meta-data informations.
     *
     * @param  cs The coordinate system (may be <code>null</code>).
     * @return <code>true</code> if both coordinate systems are equivalent.
     */
    public boolean equivalents(final CoordinateSystem cs) {
        if (cs==this) {
            return true;
        }
        if (super.equivalents(cs)) {
            final GeographicCoordinateSystem that = (GeographicCoordinateSystem) cs;
            return Utilities.equals(this.unit,     that.unit) &&
                   Utilities.equals(this.meridian, that.meridian);
        }
        return false;
    }
    
    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Know Text (WKT).
     */
    String addString(final StringBuffer buffer, Object context) {
        buffer.append(", ");
        buffer.append(getDatum());
        buffer.append(", ");
        buffer.append(meridian.toString(unit));
        buffer.append(", ");
        addUnit(buffer, unit);
        buffer.append(", ");
        buffer.append(getAxis(0));
        buffer.append(", ");
        buffer.append(getAxis(1));
        return "GEOGCS";
    }
    
    /**
     * Returns an OpenGIS interface for this geographic coordinate
     * system. The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid too early class loading of OpenGIS interface.
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
     * Wrap a {@link GeographicCoordinateSystem} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Export extends HorizontalCoordinateSystem.Export implements CS_GeographicCoordinateSystem {
        /**
         * Conversions infos. This array is constructed
         * only the first time it is requested.
         */
        private transient WGS84ConversionInfo[] infos;
        
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Returns the angular unit.
         */
        public CS_AngularUnit getAngularUnit() throws RemoteException {
            return (CS_AngularUnit) adapters.export(GeographicCoordinateSystem.this.getUnits());
        }
        
        /**
         * Returns the PrimeMeridian.
         */
        public CS_PrimeMeridian getPrimeMeridian() throws RemoteException {
            return adapters.export(GeographicCoordinateSystem.this.getPrimeMeridian());
        }
        
        /**
         * Gets the number of available conversions to WGS84 coordinates.
         */
        public int getNumConversionToWGS84() throws RemoteException {
            final WGS84ConversionInfo[] infos = getWGS84ConversionInfos();
            return (infos!=null) ? infos.length : 0;
        }
        
        /**
         * Gets details on a conversion to WGS84.
         */
        public CS_WGS84ConversionInfo getWGS84ConversionInfo(final int index) throws RemoteException {
            final WGS84ConversionInfo[] infos = getWGS84ConversionInfos();
            return (infos!=null) ? adapters.export(infos[index]) : null;
        }
        
        /**
         * Returns the set of conversions infos.
         */
        private synchronized WGS84ConversionInfo[] getWGS84ConversionInfos() {
            if (infos==null) {
                final Set set = GeographicCoordinateSystem.this.getWGS84ConversionInfos();
                if (set!=null) {
                    infos = (WGS84ConversionInfo[]) set.toArray(new WGS84ConversionInfo[set.size()]);
                }
            }
            return infos;
        }
    }
}
