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
package org.geotools.ct;

// OpenGIS dependencies
import org.opengis.ct.CT_TransformType;

// Geotools dependencies
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;


/**
 * Semantic type of transform used in coordinate transformation.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.ct.CT_TransformType
 */
public final class TransformType extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4186653001664797298L;
    
    /**
     * Unknown or unspecified type of transform.
     *
     * @see org.opengis.ct.CT_TransformType#CT_TT_Other
     */
    public static final TransformType OTHER = new TransformType("OTHER", CT_TransformType.CT_TT_Other, ResourceKeys.OTHER);
    
    /**
     * Transform depends only on defined parameters.
     * For example, a cartographic projection.
     *
     * @see org.opengis.ct.CT_TransformType#CT_TT_Conversion
     */
    public static final TransformType CONVERSION = new TransformType("CONVERSION", CT_TransformType.CT_TT_Conversion, ResourceKeys.CONVERSION);
    
    /**
     * Transform depends only on empirically derived parameters.
     * For example a datum transformation.
     *
     * @see org.opengis.ct.CT_TransformType#CT_TT_Transformation
     */
    public static final TransformType TRANSFORMATION = new TransformType("TRANSFORMATION", CT_TransformType.CT_TT_Transformation, ResourceKeys.TRANSFORMATION);
    
    /**
     * Transform depends on both defined and empirical parameters.
     *
     * @see org.opengis.ct.CT_TransformType#CT_TT_ConversionAndTransformation
     */
    public static final TransformType CONVERSION_AND_TRANSFORMATION = new TransformType("CONVERSION_AND_TRANSFORMATION", CT_TransformType.CT_TT_ConversionAndTransformation, ResourceKeys.CONVERSION_AND_TRANSFORMATION);
    
    /**
     * Transform types by value. Used to
     * canonicalize after deserialization.
     */
    private static final TransformType[] ENUMS = {
        OTHER,
        CONVERSION,
        TRANSFORMATION,
        CONVERSION_AND_TRANSFORMATION
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Resource key, used for building localized name. This key doesn't need to
     * be serialized, since {@link #readResolve} canonicalize enums according their
     * {@link #value}. Furthermore, its value is implementation-dependent (which is
     * an other raison why it should not be serialized).
     */
    private transient final int key;
    
    /**
     * Construct a new enum value.
     */
    private TransformType(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
    }
    
    /**
     * Return the enum for the specified value.
     * This method is provided for compatibility with
     * {@link org.opengis.ct.CT_TransformType}.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    public static TransformType getEnum(final int value) throws NoSuchElementException {
        if (value>=1 && value<ENUMS.length) {
            return ENUMS[value];
        }
        throw new NoSuchElementException(String.valueOf(value));
    }
    
    /**
     * Returns this enum's name in the specified locale.
     * If no name is available for the specified locale, a default one will be used.
     *
     * @param  locale The locale, or <code>null</code> for the current default locale.
     * @return Enum's name in the specified locale.
     */
    public String getName(final Locale locale) {
        return Resources.getResources(locale).getString(key);
    }
    
    /**
     * Concatenate this transform type with the specified transform type.
     * If at least one transform type is {@link #OTHER}, then {@link #OTHER}
     * is returned. Otherwise, transform type values are combined as with the
     * logical "OR" operand.
     */
    public TransformType concatenate(final TransformType type) {
        final int thisValue = this.getValue();
        final int thatValue = type.getValue();
        if (thisValue==0 || thatValue==0) {
            return OTHER;
        }
        return getEnum(thisValue | thatValue);
    }
    
    /**
     * Use a single instance of {@link TransformType} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        final int value = getValue();
        if (value>=0 && value<ENUMS.length) {
            // Canonicalize
            return ENUMS[value];
        } else {
            // Collapse unknow value to a single canonical one
            return ENUMS[0];
        }
    }
}
