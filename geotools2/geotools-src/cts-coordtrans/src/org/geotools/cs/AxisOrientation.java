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
import org.opengis.cs.CS_AxisOrientationEnum;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Orientation of axis. Some coordinate systems use non-standard orientations.
 * For example, the first axis in South African grids usually points West,
 * instead of East. This information is obviously relevant for algorithms
 * converting South African grid coordinates into Lat/Long.
 * <br><br>
 * The <em>natural ordering</em> for axis orientations is defined
 * as (EAST-WEST), (NORTH-SOUTH), (UP-DOWN), (FUTURE-PAST) and OTHER, which is
 * the ordering for a (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>)
 * coordinate system. This means that when an array of <code>AxisOrientation</code>s
 * is sorted using {@link java.util.Arrays#sort(Object[])}, EAST and WEST
 * orientations will appear first. NORTH and SOUTH will be next, followed
 * by UP and DOWN, etc.
 *
 * Care should be exercised if <code>AxisOrientation</code>s are to be used as
 * keys in a sorted map or elements in a sorted set, as
 * <code>AxisOrientation</code>'s natural ordering is inconsistent with equals.
 * See {@link java.lang.Comparable}, {@link java.util.SortedMap} or
 * {@link java.util.SortedSet} for more information.
 *
 * @version $Id: AxisOrientation.java,v 1.4 2002/07/28 21:41:31 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_AxisOrientationEnum
 */
public final class AxisOrientation extends EnumeratedParameter implements Comparable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4649182002820021468L;
    
    // NOTE: The following enum values are from the OpenGIS specification.
    //       IF THOSE VALUES CHANGE, THEN inverse() AND absolute() MUST BE
    //       UPDATED.
    
    /**
     * Unknown or unspecified axis orientation.
     * This can be used for local or fitted coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Other
     */
    public static final AxisOrientation OTHER = new AxisOrientation("OTHER", CS_AxisOrientationEnum.CS_AO_Other, ResourceKeys.OTHER);
    
    /**
     * Increasing ordinates values go North.
     * This is usually used for Grid Y coordinates and Latitude.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_North
     */
    public static final AxisOrientation NORTH = new AxisOrientation("NORTH", CS_AxisOrientationEnum.CS_AO_North, ResourceKeys.NORTH);
    
    /**
     * Increasing ordinates values go South.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_South
     */
    public static final AxisOrientation SOUTH = new AxisOrientation("SOUTH", CS_AxisOrientationEnum.CS_AO_South, ResourceKeys.SOUTH);
    
    /**
     * Increasing ordinates values go East.
     * This is usually used for Grid X coordinates and Longitude.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_East
     */
    public static final AxisOrientation EAST = new AxisOrientation("EAST", CS_AxisOrientationEnum.CS_AO_East, ResourceKeys.EAST);
    
    /**
     * Increasing ordinates values go West.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_West
     */
    public static final AxisOrientation WEST = new AxisOrientation("WEST", CS_AxisOrientationEnum.CS_AO_West, ResourceKeys.WEST);
    
    /**
     * Increasing ordinates values go up.
     * This is used for vertical coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Up
     */
    public static final AxisOrientation UP = new AxisOrientation("UP", CS_AxisOrientationEnum.CS_AO_Up, ResourceKeys.UP);
    
    /**
     * Increasing ordinates values go down.
     * This is used for vertical coordinate systems.
     *
     * @see org.opengis.cs.CS_AxisOrientationEnum#CS_AO_Down
     */
    public static final AxisOrientation DOWN = new AxisOrientation("DOWN", CS_AxisOrientationEnum.CS_AO_Down, ResourceKeys.DOWN);
    
    /**
     * Increasing time go toward future.
     * This is used for temporal axis.
     */
    public static final AxisOrientation FUTURE = new AxisOrientation("FUTURE", 7, ResourceKeys.FUTURE);
    
    /**
     * Increasing time go toward past.
     * This is used for temporal axis.
     */
    public static final AxisOrientation PAST = new AxisOrientation("PAST", 8, ResourceKeys.PAST);
    
    /**
     * The last paired value. Paired values are NORTH-SOUTH, EAST-WEST,
     * UP-DOWN, FUTURE-PAST.
     */
    private static final int LAST_PAIRED_VALUE = 8;
    
    /**
     * Axis orientations by value. Used to
     * canonicalize after deserialization.
     */
    private static final AxisOrientation[] ENUMS = {OTHER,NORTH,SOUTH,EAST,WEST,UP,DOWN,FUTURE,PAST};
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * The axis order. Used for {@link #compareTo} implementation.
     */
    private static final AxisOrientation[] ORDER = {EAST, NORTH, UP, FUTURE};
    
    /**
     * Resource key, used for building localized name. This key doesn't need to
     * be serialized, since {@link #readResolve} canonicalizes enums according
     * to their {@link #value}. Furthermore, its value is
     * implementation-dependent (which is another raison why it should not be
     * serialized).
     */
    private transient final int key;
    
    /**
     * Constructs a new enum with the specified value.
     */
    private AxisOrientation(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
    }
    
    /**
     * Returns the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.cs.CS_AxisOrientationEnum}.
     *
     * @param value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static AxisOrientation getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Returns the enum for the specified name.
     * Search is case and locale insensitive.
     *
     * @param name One of the constant values ({@link #NORTH}, {@link #SOUTH}, etc.)
     * @return The enum for the specified name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static AxisOrientation getEnum(final String name) {
        for (int i=0; i<ENUMS.length; i++) {
            final AxisOrientation candidate = ENUMS[i];
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
     * @param name The localized name (e.g. "Nord", "Sud", "Est", "Ouest", etc.)
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return The enum for the specified localized name.
     * @throws NoSuchElementException if there is no enum for the specified name.
     */
    public static AxisOrientation getEnum(final String name, final Locale locale) {
        final Resources resources = Resources.getResources(locale);
        for (int i=0; i<ENUMS.length; i++) {
            final AxisOrientation candidate = ENUMS[i];
            if (name.equalsIgnoreCase(resources.getString(candidate.key))) {
                return candidate;
            }
        }
        throw new NoSuchElementException(name);
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale, a default one will
     * be used.
     *
     * @param locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(key);
    }
    
    /**
     * Returns the opposite orientation of this axis.
     * The opposite of North is South, and the opposite of South is North.
     * The same applies to East-West, Up-Down and Future-Past.
     * Other axis orientations are returned unchanged.
     */
    public AxisOrientation inverse() {
        final int value=getValue()-1;
        if (value>=0 && value<LAST_PAIRED_VALUE) {
            return ENUMS[(value ^ 1)+1];
        } else {
            return this;
        }
    }
    
    /**
     * Returns the "absolute" orientation of this axis.
     * This "absolute" operation is similar to the <code>Math.abs(int)</code>
     * method in that "negative" orientations (<code>SOUTH</code>,
     * <code>WEST</code>, <code>DOWN</code>, <code>PAST</code>) are changed
     * for their positive counterparts (<code>NORTH</code>, <code>EAST</code>,
     * <code>UP</code>, <code>FUTURE</code>). More specifically, the
     * following conversion table is applied.
     * <br>&nbsp;
     * <table align="center" cellpadding="3" border="1" bgcolor="F4F8FF">
     *   <tr bgcolor="#B9DCFF">
     *     <th>&nbsp;&nbsp;Orientation&nbsp;&nbsp;</th>
     *     <th>&nbsp;&nbsp;Absolute value&nbsp;&nbsp;</th>
     *   </tr>
     *   <tr align="center"><td>NORTH</td> <td>NORTH</td> </tr>
     *   <tr align="center"><td>SOUTH</td> <td>NORTH</td> </tr>
     *   <tr align="center"><td>EAST</td>  <td>EAST</td>  </tr>
     *   <tr align="center"><td>WEST</td>  <td>EAST</td>  </tr>
     *   <tr align="center"><td>UP</td>    <td>UP</td>    </tr>
     *   <tr align="center"><td>DOWN</td>  <td>UP</td>    </tr>
     *   <tr align="center"><td>FUTURE</td><td>FUTURE</td></tr>
     *   <tr align="center"><td>PAST</td>  <td>FUTURE</td></tr>
     *   <tr align="center"><td>OTHER</td> <td>OTHER</td> </tr>
     * </table>
     */
    public AxisOrientation absolute() {
        final int value=getValue()-1;
        if (value>=0 && value<LAST_PAIRED_VALUE) {
            return ENUMS[(value & ~1)+1];
        } else {
            return this;
        }
    }
    
    /**
     * Compares this <code>AxisOrientation</code> with the specified
     * orientation.  The <em>natural ordering</em> is defined as
     * (EAST-WEST), (NORTH-SOUTH), (UP-DOWN), (FUTURE-PAST) and OTHER,
     * which is the ordering for a
     * (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>) coordinate system.
     * Two <code>AxisOrientation</code>s that are along the same axis but with
     * an opposite direction (e.g. EAST vs WEST) are considered equal by this
     * method.
     *
     * @param  ao An <code>AxisOrientation</code> object to be compared with.
     * @throws ClassCastException if <code>ao</code> is not an
     *         <code>AxisOrientation</code> object.
     */
    public int compareTo(final Object ao) {
        final AxisOrientation that = (AxisOrientation)ao;
        final int thisOrder = this.absolute().getOrder();
        final int thatOrder = that.absolute().getOrder();
        if (thisOrder > thatOrder) return +1;
        if (thisOrder < thatOrder) return -1;
        return 0;
    }
    
    /**
     * Returns the order for this axis orientation
     * (i.e. the index in the {@link #ORDER} table).
     */
    private int getOrder() {
        int i;
        for (i=0; i<ORDER.length; i++) {
            if (equals(ORDER[i])) {
                break;
            }
        }
        return i;
    }
    
    /**
     * Uses a single instance of {@link AxisOrientation} after deserialization.
     * It allows client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException if deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        final int value = getValue();
        if (value>=0 && value<ENUMS.length) {
            // Canonicalize
            return ENUMS[value];
        } else {
            // Collapse unknown value to a single canonical one
            return ENUMS[0]; 
        }
    }
}
