/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import org.opengis.cs.CS_DatumType;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// Geotools dependencies
import org.geotools.util.WeakHashSet;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Type of the datum expressed as an enumerated value.
 * The enumeration is split into ranges which indicate the datum's type.
 * The value should be one of the predefined values, or within the range
 * for local types. This will allow the OpenGIS Consortium to coordinate the
 * addition of new interoperable codes.
 *
 * @version $Id: DatumType.java,v 1.6 2003/05/13 10:58:47 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_DatumType
 */
public abstract class DatumType extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4829955972189625202L;
    
    /**
     * These datums, such as ED50, NAD27 and NAD83, have been designed
     * to support horizontal positions on the ellipsoid as opposed to positions
     * in 3-D space.  These datums were designed mainly to support a horizontal
     * component of a position in a domain of limited extent, such as a country,
     * a region or a continent.
     *
     * @see org.opengis.cs.CS_DatumType#CS_HD_Classic
     */
    public static final Horizontal CLASSIC = new Horizontal("CLASSIC", CS_DatumType.CS_HD_Classic, ResourceKeys.CLASSIC);
    
    /**
     * A geocentric datum is a "satellite age" modern geodetic datum
     * mainly of global extent, such as WGS84 (used in GPS),
     * PZ90 (used in GLONASS) and ITRF. These datums were designed to
     * support both a horizontal component of position and a vertical
     * component of position (through ellipsoidal heights).  The regional
     * realizations of ITRF, such as ETRF, are also included in this category.
     *
     * @see org.opengis.cs.CS_DatumType#CS_HD_Geocentric
     */
    public static final Horizontal GEOCENTRIC = new Horizontal("GEOCENTRIC", CS_DatumType.CS_HD_Geocentric, ResourceKeys.GEOCENTRIC);
    
    /**
     * A vertical datum for orthometric heights
     * that are measured along the plumb line.
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_Orthometric
     */
    public static final Vertical ORTHOMETRIC = new Vertical("ORTHOMETRIC", CS_DatumType.CS_VD_Orthometric, ResourceKeys.ORTHOMETRIC);
    
    /**
     * A vertical datum for ellipsoidal heights that are measured along the
     * normal to the ellipsoid used in the definition of horizontal datum.
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_Ellipsoidal
     */
    public static final Vertical ELLIPSOIDAL = new Vertical("ELLIPSOIDAL", CS_DatumType.CS_VD_Ellipsoidal, ResourceKeys.ELLIPSOIDAL);
    
    /**
     * The vertical datum of altitudes or heights in the atmosphere.
     * These are approximations of orthometric heights obtained with
     * the help of a barometer or a barometric altimeter. These values
     * are usually expressed in one of the following units: meters, feet,
     * millibars (used to measure pressure levels), or theta value (units
     * used to measure geopotential height).
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_AltitudeBarometric
     */
    public static final Vertical ALTITUDE_BAROMETRIC = new Vertical("ALTITUDE_BAROMETRIC", CS_DatumType.CS_VD_AltitudeBarometric, ResourceKeys.BAROMETRIC_ALTITUDE);
    
    /**
     * A normal height system.
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_Normal
     */
    public static final Vertical NORMAL = new Vertical("NORMAL", CS_DatumType.CS_VD_Normal, ResourceKeys.NORMAL);
    
    /**
     * A vertical datum of geoid model derived heights,
     * also called GPS-derived heights. These heights are approximations
     * of orthometric heights (<var>H</var>), constructed from the
     * ellipsoidal heights (<var>h</var>) by the use of the given
     * geoid undulation model (<var>N</var>) through the equation:
     * <var>H</var>=<var>h</var>-<var>N</var>.
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_GeoidModelDerived
     */
    public static final Vertical GEOID_MODEL_DERIVED = new Vertical("GEOID_MODEL_DERIVED", CS_DatumType.CS_VD_GeoidModelDerived, ResourceKeys.GEOID_MODEL_DERIVED);
    
    /**
     * This attribute is used to support the set of datums generated
     * for hydrographic engineering projects where depth measurements below
     * sea level are needed. It is often called a hydrographic or a marine
     * datum. Depths are measured in the direction perpendicular
     * (approximately) to the actual equipotential surfaces of the earth's
     * gravity field, using such procedures as echo-sounding.
     *
     * @see org.opengis.cs.CS_DatumType#CS_VD_Depth
     */
    public static final Vertical DEPTH = new Vertical("DEPTH", CS_DatumType.CS_VD_Depth, ResourceKeys.DEPTH);
    
    /**
     * A temporal datum for Universal Time (UTC).
     * UTC is based on an atomic clock, while GMT is based on astronomical
     * observations.
     * <br><br>
     * <strong>Note: This enum is not part of OpenGIS specification. It may
     *         change in an incompatible way if OpenGIS define an equivalent
     *         enum.</strong>
     */
    public static final Temporal UTC = new Temporal("UTC", 3001, ResourceKeys.UTC);
    
    /**
     * A temporal datum for Greenwich Mean Time (GMT).
     * GMT is based on astronomical observations, while UTC is based on an
     * atomic clock.
     * <br><br>
     * <strong>Note: This enum is not part of OpenGIS specification. It may
     *         change in an incompatible way if OpenGIS define an equivalent
     *         enum.</strong>
     */
    public static final Temporal GMT = new Temporal("GMT", 3002, ResourceKeys.GMT);
    
    /**
     * List of predefined enum types.
     */
    private static final DatumType[] ENUMS = {
        Horizontal.OTHER,
        CLASSIC,
        GEOCENTRIC,
        Vertical.OTHER,
        ORTHOMETRIC,
        ELLIPSOIDAL,
        ALTITUDE_BAROMETRIC,
        NORMAL,
        GEOID_MODEL_DERIVED,
        DEPTH,
        UTC,
        GMT
    };
    
    /**
     * Resource key, used for building localized name. This key doesn't need to
     * be serialized, since {@link #readResolve} canonicalize enums according
     * to their {@link #value}. Furthermore, its value is
     * implementation-dependent (which is another reason why it should not
     * be serialized).
     */
    private transient final int key;
    
    /**
     * Constructs a new enum with the specified value.
     */
    private DatumType(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
        if (!(value>=getMinimum() && value<=getMaximum())) {
            throw new IllegalArgumentException(String.valueOf(value));
        }
    }

    /**
     * Returns the enum for the specified value.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     */
    public static DatumType getEnum(final int value) {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()==value) {
                return ENUMS[i];
            }
        }
        final DatumType datum;
        if (value>=Horizontal.MINIMUM && value<=Horizontal.MAXIMUM) {
            datum = new Horizontal("Custom", value, -1);
        } else if (value>=Vertical.MINIMUM && value<=Vertical.MAXIMUM) {
            datum = new Vertical("Custom", value, -1);
        } else if (value>=Temporal.MINIMUM && value<=Temporal.MAXIMUM) {
            datum = new Temporal("Custom", value, -1);
        } else if (value>=Local.MINIMUM && value<=Local.MAXIMUM) {
            datum = new Local("Custom", value, -1);
        } else {
            throw new IllegalArgumentException(String.valueOf(value));
        }
        return (DatumType) Info.pool.canonicalize(datum);
    }

    /**
     * Returns the enum for the specified name.
     * Search is case and locale insensitive.
     *
     * @param name One of the constant values ({@link #GEOCENTRIC}, {@link #ELLIPSOIDAL}, etc.)
     * @return The enum for the specified name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static DatumType getEnum(String name) {
        name = name.trim().replace(' ', '_');
        for (int i=0; i<ENUMS.length; i++) {
            final DatumType candidate = ENUMS[i];
            if (name.equalsIgnoreCase(candidate.getName())) {
                return candidate;
            }
        }
        throw new NoSuchElementException(name);
    }

    /**
     * Returns the enum for the specified localized name.
     * Search is case-insensitive.
     *
     * @param name The localized name (e.g. "Géocentrique", "Ellipsoïdal", etc.)
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return The enum for the specified localized name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static DatumType getEnum(String name, final Locale locale) {
        name = name.trim();
        final Resources resources = Resources.getResources(locale);
        for (int i=0; i<ENUMS.length; i++) {
            final DatumType candidate = ENUMS[i];
            if (name.equalsIgnoreCase(resources.getString(candidate.key))) {
                return candidate;
            }
        }
        throw new NoSuchElementException(name);
    }

    /**
     * Returns <code>true</code> if the specified orientation is compatible
     * with this datum type. For example, a vertical datum is compatible only
     * with orientations UP and DOWN.
     */
    abstract boolean isCompatibleOrientation(final AxisOrientation orientation);
    
    /**
     * Gets the minimum value.
     */
    abstract int getMinimum();
    
    /**
     * Gets the maximum value.
     */
    abstract int getMaximum();
    
    /**
     * Returns the type key.
     */
    abstract int getTypeKey();
    
    /**
     * Returns the type name in the specified locale.
     * Type may be "Horizontal", "Vertical", "Temporal" or "Local".
     */
    public String getType(final Locale locale) {
        return Resources.getResources(locale).getString(getTypeKey());
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale, a default one will
     * be used.
     *
     * @param  locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return (key>=0) ? Resources.getResources(locale).getString(key) : getName();
    }

    /**
     * Returns the enum value as a hash code.
     *
     * @return The hash code value. This value doesn't need to be the same
     *         in past or future versions of this class.
     */
    public int hashCode() {
        return (int)serialVersionUID + 37*getValue();
    }

    /**
     * Compare this <code>DatumType</code> with the specified object for
     * equality.  Two datum types are equals if, and only if, they are of
     * the same class and have the same enum value.
     */
    public boolean equals(final Object obj) {
        if (obj!=null && obj.getClass().equals(getClass())) {
            final DatumType that = (DatumType) obj;
            return that.getValue() == getValue();
        }
        return false;
    }
    
    /**
     * Uses a single instance of {@link DatumType} after deserialization.
     * It allows client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization fails.
     */
    private Object readResolve() throws ObjectStreamException {
        return getEnum(getValue());
    }
    
    /**
     * Horizontal datum type.
     *
     * @see org.opengis.cs.CS_DatumType
     */
    public static final class Horizontal extends DatumType {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -9026322423891329754L;
        
        /**
         * Lowest possible value for horizontal datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_HD_Min
         */
        public static final int MINIMUM = CS_DatumType.CS_HD_Min;
        
        /**
         * Highest possible value for horizontal datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_HD_Max
         */
        public static final int MAXIMUM = CS_DatumType.CS_HD_Max;
        
        /**
         * Unspecified horizontal datum type.
         * Horizontal datums with this type should never supply
         * a conversion to WGS84 using Bursa Wolf parameters.
         *
         * @see org.opengis.cs.CS_DatumType#CS_HD_Other
         */
        public static final Horizontal OTHER = new Horizontal("OTHER", CS_DatumType.CS_HD_Other, ResourceKeys.OTHER);
        
        /**
         * Constructs a new enum with the specified value.
         */
        private Horizontal(final String name, final int value, final int key) {
            super(name, value, key);
        }
        
        /**
         * Returns <code>true</code> if the specified orientation is compatible
         * with this datum type. Compatible orientations are NORTH, SOUTH, EAST
         * and WEST.
         */
        boolean isCompatibleOrientation(final AxisOrientation orientation) {
            return AxisOrientation.NORTH.equals(orientation) ||
            AxisOrientation.SOUTH.equals(orientation) ||
            AxisOrientation.EAST .equals(orientation) ||
            AxisOrientation.WEST .equals(orientation);
        }
        
        /** Gets the minimum value. */ final int getMinimum() {return MINIMUM;}
        /** Gets the maximum value. */ final int getMaximum() {return MAXIMUM;}
        /** Returns the type key.   */ final int getTypeKey() {return ResourceKeys.HORIZONTAL;}
    }
    
    /**
     * Vertical datum type.
     *
     * @see org.opengis.cs.CS_DatumType
     */
    public static final class Vertical extends DatumType {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = -916273252180448822L;
        
        /**
         * Lowest possible value for vertical datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_VD_Min
         */
        public static final int MINIMUM = CS_DatumType.CS_VD_Min;
        
        /**
         * Highest possible value for vertical datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_VD_Max
         */
        public static final int MAXIMUM = CS_DatumType.CS_VD_Max;
        
        /**
         * Unspecified vertical datum type.
         *
         * @see org.opengis.cs.CS_DatumType#CS_VD_Other
         */
        public static final Vertical OTHER = new Vertical("OTHER", CS_DatumType.CS_VD_Other, ResourceKeys.OTHER);
        
        /**
         * Constructs a new enum with the specified value.
         */
        private Vertical(final String name, final int value, final int key) {
            super(name, value, key);
        }
        
        /**
         * Returns <code>true</code> if the specified orientation is compatible
         * with this datum type. Compatible orientations are UP and DOWN.
         */
        boolean isCompatibleOrientation(final AxisOrientation orientation) {
            return AxisOrientation.UP  .equals(orientation) ||
                   AxisOrientation.DOWN.equals(orientation);
        }
        
        /** Gets the minimum value. */ final int getMinimum() {return MINIMUM;}
        /** Gets the maximum value. */ final int getMaximum() {return MAXIMUM;}
        /** Returns the type key.   */ final int getTypeKey() {return ResourceKeys.VERTICAL;}
    }
    
    /**
     * Temporal datum type.
     */
    public static final class Temporal extends DatumType {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 731901694455984836L;
        
        /**
         * Lowest possible value for temporal datum types.
         * <br><br>
         * <strong>Note: Temporal enums are not part of OpenGIS specification.
         *               The <code>MINIMUM</code> "constant" may change in the
         *               future if OpenGIS defines an equivalent datum type.
         *               If this value changes, developers will have to
         *               recompile their code.</strong>
         */
        public static final int MINIMUM = 3000;
        
        /**
         * Highest possible value for temporal datum types.
         * <br><br>
         * <strong>Note: Temporal enums are not part of OpenGIS specification.
         *               The <code>MAXIMUM</code> "constant" may change in the
         *               future if OpenGIS defines an equivalent datum type.
         *               If this value changes, developers will have to
         *               recompile their code.</strong>
         */
        public static final int MAXIMUM = 3999;
        
        /**
         * Constructs a new enum with the specified value.
         */
        private Temporal(final String name, final int value, final int key) {
            super(name, value, key);
        }
        
        /**
         * Returns <code>true</code> if the specified orientation is compatible
         * with this datum type. Compatible orientations are FUTURE and PAST.
         */
        boolean isCompatibleOrientation(final AxisOrientation orientation) {
            return AxisOrientation.FUTURE.equals(orientation) ||
                   AxisOrientation.PAST  .equals(orientation);
        }
        
        /** Gets the minimum value. */ final int getMinimum() {return MINIMUM;}
        /** Gets the maximum value. */ final int getMaximum() {return MAXIMUM;}
        /** Returns the type key.   */ final int getTypeKey() {return ResourceKeys.TEMPORAL;}
    }
    
    /**
     * Local datum type.
     *
     * @see org.opengis.cs.CS_DatumType
     */
    public static final class Local extends DatumType {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 412409825333947716L;
        
        /**
         * Lowest possible value for local datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_LD_Min
         */
        public static final int MINIMUM = CS_DatumType.CS_LD_Min;
        
        /**
         * Highest possible value for local datum types.
         *
         * @see org.opengis.cs.CS_DatumType#CS_LD_Max
         */
        public static final int MAXIMUM = CS_DatumType.CS_LD_Max;
        
        /**
         * Constructs a new enum with the specified value.
         */
        private Local(final String name, final int value, final int key) {
            super(name, value, key);
        }
        
        /**
         * Returns <code>true</code> if the specified orientation is compatible
         * with this datum type. Local datum accept all orientations.
         */
        boolean isCompatibleOrientation(final AxisOrientation orientation) {
            return true;
        }
        
        /** Gets the minimum value. */ final int getMinimum() {return MINIMUM;}
        /** Gets the maximum value. */ final int getMaximum() {return MAXIMUM;}
        /** Returns the type key.   */ final int getTypeKey() {return ResourceKeys.LOCAL;}
    }
}
