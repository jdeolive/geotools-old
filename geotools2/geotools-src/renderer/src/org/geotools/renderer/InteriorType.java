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
package org.geotools.renderer;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// Resources
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Enumeration class specifing the type of a {@linkplain Polygon polygon}'s interior.
 * The interior may be an {@linkplain #ELEVATION elevation} (e.g. the coastline of an
 * island on the ocean) or a {@link #DEPRESSION depression} (e.g. the coastline of a
 * lake in an island).
 *
 * @version $Id: InteriorType.java,v 1.2 2003/01/20 00:06:34 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class InteriorType extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1534788354133047237L;

    /**
     * The enum value for <code>null</code>, which means "do not apply".
     */
    static final int UNCLOSED = 0;
    
    /**
     * Constant indicating that a polygon's interior is an elevation.
     * For the 0 meter isobath, an elevation may be an island on the ocean.
     */
    public static final InteriorType ELEVATION = new InteriorType("ELEVATION", 1,
                                                                  ResourceKeys.ELEVATION);
    
    /**
     * Constant indicating that a polygon's interior is a depression.
     * For the 0 meter isobath, a depression may be a lake on an island.
     */
    public static final InteriorType DEPRESSION = new InteriorType("DEPRESSION", 2,
                                                                  ResourceKeys.DEPRESSION);
    
    /**
     * Constant indicating that a polygon's interior is neither an elevation or a depression.
     */
    public static final InteriorType FLAT = new InteriorType("FLAT", 3, ResourceKeys.FLAT);
    
    /**
     * Interior type by value. Used to
     * canonicalize after deserialization.
     */
    private static final InteriorType[] ENUMS = {
        null, ELEVATION, DEPRESSION, FLAT
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (getValue(ENUMS[i]) != i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Resource key, used for building localized name. This key doesn't need to be
     * serialized, since {@link #readResolve} canonicalize enums according their
     * {@link #getValue()}. Furthermore, its value is implementation-dependent
     * (which is an other raison why it should not be serialized).
     */
    private transient final int key;
    
    /**
     * Construct a new enum with the specified value.
     */
    private InteriorType(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
    }
    
    /**
     * Return the enum for the specified value.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    static InteriorType getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }

    /**
     * Return the value for the specified enum, which may be <code>null</code>.
     */
    static int getValue(final InteriorType type) {
        return (type!=null) ? type.getValue() : UNCLOSED;
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale,
     * a default one will be used.
     *
     * @param  locale The locale, or <code>null</code> for the default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(key);
    }
    
    /**
     * Use a single instance of {@link InteriorType} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        int value = getValue();
        if (value<0 || value>=ENUMS.length) {
            // Collapse unknow value to a single canonical one
            value = UNCLOSED;
        }
        return ENUMS[value]; // Canonicalize
    }
}
