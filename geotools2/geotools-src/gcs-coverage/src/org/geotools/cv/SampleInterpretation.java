/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.cv;

// Color model
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

// Other J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// Resources
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Enumeration class specifing the mapping of a sample to a geophysics value.
 * A sample value may be an index (i.e. the geophysics value can be computed
 * using some equation likes <code>offset&nbsp;+&nbsp;scale*<var>index</var></code>)
 * or it may be already the geophysics value.
 *
 * @version 1.00
 * @author Martin Desruisseaux
 */
public final class SampleInterpretation extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4630275596610499020L;
    
    /**
     * Sample is not associated with a geophysics value.
     */
    public static final SampleInterpretation UNDEFINED = new SampleInterpretation("UNDEFINED",
                        0, ResourceKeys.UNDEFINED);
    
    /**
     * Sample is an index convertible in geophysics value.
     */
    public static final SampleInterpretation INDEXED = new SampleInterpretation("INDEXED",
                        1, ResourceKeys.INDEXED);
    
    /**
     * Sample is already a geophysics value.
     */
    public static final SampleInterpretation GEOPHYSICS = new SampleInterpretation("GEOPHYSICS",
                        2, ResourceKeys.GEOPHYSICS);
    
    /**
     * Sample interpretation by value. Used to
     * canonicalize after deserialization.
     */
    private static final SampleInterpretation[] ENUMS = {
        UNDEFINED, INDEXED, GEOPHYSICS
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue()!=i) {
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
    private SampleInterpretation(final String name, final int value, final int key) {
        super(name, value);
        this.key = key;
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
     * Use a single instance of {@link ColorInterpretation} after deserialization.
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
            value = 0;
        }
        return ENUMS[value]; // Canonicalize
    }
}
