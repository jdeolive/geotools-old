/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
package org.geotools.gc;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.Serializable;
import javax.media.jai.util.Range;
import javax.media.jai.ParameterListDescriptor;

// OpenGIS dependencies
import org.opengis.gc.GC_ParameterInfo;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Informations about a parameter required for a grid coverage processing
 * operation. This information includes the name of the parameter, parameter
 * description, parameter type etc.
 *
 * This is mostly a convenience class,  since all informations are extracted
 * from {@link ParameterListDescriptor}. We provide it because it is part of
 * OpenGIS specification and its API is significantly different from Java
 * Advanced Imaging.
 *
 * @version $Id: ParameterInfo.java,v 1.4 2003/07/11 16:57:47 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 *
 * @see GC_ParameterInfo
 */
public class ParameterInfo implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7348471832696172385L;
    
    /**
     * The parameter name.
     */
    private final String name;
    
    /**
     * The parameter type.
     */
    private final Class type;
    
    /**
     * The originating parameter list descriptor.
     */
    private final ParameterListDescriptor descriptor;
    
    /**
     * Construct a new info for a parameter.
     *
     * @param  descriptor The originating parameter list descriptor.
     * @param  name The parameter name.
     * @throws IllegalArgumentException if <code>name</code> is
     *         not a parameter of <code>descriptor</code>.
     */
    public ParameterInfo(final ParameterListDescriptor descriptor, final String name)
            throws IllegalArgumentException
    {
        this.descriptor      = descriptor;
        final String[] names = descriptor.getParamNames();
        final Class[]  types = descriptor.getParamClasses();
        if (names!=null) {
            for (int i=0; i<names.length; i++) {
                if (names[i].equalsIgnoreCase(name)) {
                    this.name = names[i];
                    this.type = types[i];
                    return;
                }
            }
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_UNKNOW_PARAMETER_NAME_$1, name));
    }
    
    /**
     * Construct a new info for a parameter.
     *
     * @param  descriptor The originating parameter list descriptor.
     * @param  index The parameter index.
     */
    public ParameterInfo(final ParameterListDescriptor descriptor, final int index) {
        this.descriptor = descriptor;
        this.name       = descriptor.getParamNames  ()[index];
        this.type       = descriptor.getParamClasses()[index];
    }
    
    /**
     * Returns the parameter name.
     *
     * @see GC_ParameterInfo#getName
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the parameter description. If no description is available,
     * the value will be <code>null</code>. If a locale is supplied, this
     * method will try to returns a description in this locale. The default
     * implementation always returns <code>null</code>.
     *
     * @param  locale The locale, or <code>null</code> for a default one.
     * @return The description, or <code>null</code> if there is none.
     *
     * @see GC_ParameterInfo#getDescription
     */
    public String getDescription(final Locale locale) {
        return null;
    }
    
    /**
     * Returns the parameter type.
     *
     * @see GC_ParameterInfo#getType
     */
    public Class getType() {
        return type;
    }
    
    /**
     * Returns the default value for parameter.
     * The type {@link Object} can be any type including a {@link Number}
     * or a {@link String}. For example, a filtering operation could have
     * a default kernel size of 3. If there is no default value, this method
     * returns <code>null</code>.
     *
     * @see GC_ParameterInfo#getDefaultValue
     */
    public Object getDefaultValue() {
        final Object value = descriptor.getParamDefaultValue(name);
        return value!=ParameterListDescriptor.NO_PARAMETER_DEFAULT ? value : null;
    }
    
    /**
     * Returns the minimum parameter value. For example,
     * a filtering operation could have a minimum kernel size of 3.
     * If there is no minimum value, this method returns <code>null</code>.
     *
     * @see GC_ParameterInfo#getMinimumValue
     */
    public Number getMinimumValue() {
        final Range range = descriptor.getParamValueRange(name);
        if (range!=null) {
            final Comparable value = range.getMinValue();
            if (value instanceof Number) {
                return (Number) value;
            }
        }
        return null;
    }
    
    /**
     * Returns the maximum parameter value. For example,
     * a filtering operation could have a maximum kernel size of 9.
     * If there is no maximum value, this method returns <code>null</code>.
     *
     * @see GC_ParameterInfo#getMaximumValue
     */
    public Number getMaximumValue() {
        final Range range = descriptor.getParamValueRange(name);
        if (range!=null) {
            final Comparable value = range.getMaxValue();
            if (value instanceof Number) {
                return (Number) value;
            }
        }
        return null;
    }
    
    /**
     * Returns a hash value for this parameter.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = 782365;
        if (name!=null) code += name.hashCode();
        if (type!=null) code = code*37 + type.hashCode();
        return code;
    }
    
    /**
     * Compares the specified object with
     * this parameter for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final ParameterInfo that = (ParameterInfo) object;
            return Utilities.equals(this.name,       that.name) &&
                   Utilities.equals(this.type,       that.type) &&
                   Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }
    
    /**
     * Returns a string représentation of this parameter.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(name);
        buffer.append(']');
        return buffer.toString();
    }
}
