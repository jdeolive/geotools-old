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
package org.geotools.gp;

// J2SE dependencies
import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.awt.RenderingHints;
import java.awt.Color;
import java.util.Locale;
import java.lang.reflect.Array;

// JAI dependencies
import javax.media.jai.KernelJAI;
import javax.media.jai.util.Range;
import javax.media.jai.Interpolation;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.EnumeratedParameter;

// OpenGIS dependencies
import org.opengis.gp.GP_Operation;

// Geotools Dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.gc.ParameterInfo;
import org.geotools.ct.CoordinateTransformationFactory;

// Resources
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.DescriptorNaming;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * Provides descriptive information for a grid coverage processing
 * operation. The descriptive information includes such information as the
 * name of the operation, operation description, and number of source grid
 * coverages required for the operation.
 *
 * @version $Id: Operation.java,v 1.13 2003/07/22 15:24:53 desruisseaux Exp $
 * @author <a href="www.opengis.org">OpenGIS</a>
 * @author Martin Desruisseaux
 */
public abstract class Operation implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1280778129220703728L;
    
    /**
     * List of valid names. Note: the "Optimal" type is not
     * implemented because currently not provided by JAI.
     */
    private static final String[] INTERPOLATION_NAMES= {
        "Nearest",          // JAI name
        "NearestNeighbor",  // OpenGIS name
        "Bilinear",
        "Bicubic",
        "Bicubic2"          // Not in OpenGIS specification.
    };
    
    /**
     * Interpolation types (provided by Java Advanced
     * Imaging) for {@link #INTERPOLATION_NAMES}.
     */
    private static final int[] INTERPOLATION_TYPES= {
        Interpolation.INTERP_NEAREST,
        Interpolation.INTERP_NEAREST,
        Interpolation.INTERP_BILINEAR,
        Interpolation.INTERP_BICUBIC,
        Interpolation.INTERP_BICUBIC_2
    };

    /** Convenient constant */ static final Integer ZERO  = new Integer(0);
    /** Convenient constant */ static final Integer ONE   = new Integer(1);
    /** Convenient constant */ static final Integer TWO   = new Integer(2);
    /** Convenient constant */ static final Integer THREE = new Integer(3);
    /** Convenient constant */ static final Range RANGE_0 = new Range(Integer.class, ZERO, null);
    /** Convenient constant */ static final Range RANGE_1 = new Range(Integer.class, ONE,  null);

    /**
     * The name of the processing operation.
     */
    private final String name;
    
    /**
     * The parameters descriptor.
     */
    private final ParameterListDescriptor descriptor;
    
    /**
     * Construct an operation.
     *
     * @param name The name of the processing operation.
     * @param descriptor The parameters descriptor.
     */
    public Operation(final String name, final ParameterListDescriptor descriptor) {
        this.name       = name;
        this.descriptor = descriptor;
    }
    
    /**
     * Returns the name of the processing operation.
     *
     * @see GP_Operation#getName
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the description of the processing operation. If there is no description,
     * returns <code>null</code>. If no description is available in the specified
     * locale, a default one will be used.
     *
     * @param locale The desired locale, or <code>null</code> for the default locale.
     *
     * @see GP_Operation#getDescription
     */
    public String getDescription(final Locale locale) {
        return null;
    }
    
    /**
     * Returns the number of source grid coverages required for the operation.
     *
     * @see GP_Operation#getNumSources
     */
    public int getNumSources() {
        int count=0;
        final Class[] c = descriptor.getParamClasses();
        if (c!=null) {
            for (int i=0; i<c.length; i++) {
                if (GridCoverage.class.isAssignableFrom(c[i])) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Returns the number of parameters for the
     * operation, including source grid coverages.
     *
     * @see GP_Operation#getNumParameters
     */
    public int getNumParameters() {
        return descriptor.getNumParameters();
    }
    
    /**
     * Retrieve the parameter information for a given index.
     * This is mostly a convenience method, since informations
     * are extracted from {@link ParameterListDescriptor}.
     *
     * @see GP_Operation#getParameterInfo
     * @see #getParameterInfo(String)
     */
    public ParameterInfo getParameterInfo(final int index) {
        return new ParameterInfo(descriptor, index);
    }
    
    /**
     * Retrieve the parameter information for a given name. Search is case-insensitive.
     * This is mostly a convenience method, since informations are extracted from
     * {@link ParameterListDescriptor}.
     *
     * @see #getParameterInfo(int)
     */
    public ParameterInfo getParameterInfo(final String name) {
        return new ParameterInfo(descriptor, name);
    }
    
    /**
     * Returns a default parameter list for this operation.
     */
    public ParameterList getParameterList() {
        return new ParameterListImpl(descriptor);
    }

    /**
     * Returns the parameter list descriptor.
     */
    final ParameterListDescriptor getParameterListDescriptor() {
        return descriptor;
    }
    
    /**
     * Cast the specified object to an {@link Interpolation object}.
     *
     * @param  type The interpolation type as an {@link Interpolation}
     *         or a {@link CharSequence} object.
     * @return The interpolation object for the specified type.
     * @throws IllegalArgumentException if the specified interpolation type is not a know one.
     */
    static Interpolation toInterpolation(final Object type) {
        if (type instanceof Interpolation) {
            return (Interpolation) type;
        } else if (type instanceof CharSequence)
        {
            final String name=type.toString();
            for (int i=0; i<INTERPOLATION_NAMES.length; i++) {
                if (INTERPOLATION_NAMES[i].equalsIgnoreCase(name)) {
                    return Interpolation.getInstance(INTERPOLATION_TYPES[i]);
                }
            }
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_UNKNOW_INTERPOLATION_$1, type));
    }

    /**
     * Returns the interpolation name for the specified interpolation.
     */
    static String getInterpolationName(final Interpolation interp) {
        final String prefix = "Interpolation";
        for (Class classe = interp.getClass(); classe!=null; classe=classe.getSuperclass()) {
            String name = Utilities.getShortName(classe);
            int index = name.lastIndexOf(prefix);
            if (index >= 0) {
                return name.substring(index + prefix.length());
            }
        }
        return Utilities.getShortClassName(interp);
    }
    
    /**
     * Apply a process operation to a grid coverage. This method is invoked by
     * {@link GridCoverageProcessor}.
     *
     * @param  parameters List of name value pairs for the parameters
     *         required for the operation.
     * @param  A set of rendering hints, or <code>null</code> if none.
     *         The <code>GridCoverageProcessor</code> may provides hints
     *         for the following keys: {@link Hints#COORDINATE_TRANSFORMATION_FACTORY}
     *         and {@link Hints#JAI_INSTANCE}.
     * @return The result as a grid coverage.
     */
    protected abstract GridCoverage doOperation(final ParameterList  parameters,
                                                final RenderingHints hints);

    /**
     * Returns a hash value for this operation.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        // Since we should have only one operation registered for each name,
        // the name hash code should be enough.
        return name.hashCode();
    }
    
    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            final Operation that = (Operation) object;
            return        Utilities.equals(this.name,       that.name) &&
                   DescriptorNaming.equals(this.descriptor, that.descriptor);
        }
        return false;
    }
    
    /**
     * Returns a string représentation of this operation.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes only.
     */
    public String toString() {
        return Utilities.getShortClassName(this) + '[' + 
               getName() + ": "+descriptor.getNumParameters() + ']';
    }
    
    /**
     * Print a description of this operation to the specified stream.
     * The description include operation name and a list of parameters.
     *
     * @param  out The destination stream.
     * @param  param A List of parameter values, or <code>null</code> if none.
     *         If <code>null</code>, then default values will be printed instead
     *         of actual values.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void print(final Writer out, final ParameterList param) throws IOException {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        out.write(' ');
        out.write(getName());
        out.write(lineSeparator);
        
        final Resources resources = Resources.getResources(null);
        final TableWriter table = new TableWriter(out, " \u2502 ");
        table.setMultiLinesCells(true);
        table.writeHorizontalSeparator();
        table.write(resources.getString(ResourceKeys.NAME));
        table.nextColumn();
        table.write(resources.getString(ResourceKeys.CLASS));
        table.nextColumn();
        table.write(resources.getString(param!=null ? ResourceKeys.VALUE : ResourceKeys.DEFAULT_VALUE));
        table.nextLine();
        table.writeHorizontalSeparator();

        final Object[]   array1 = new Object[1];
        final String[]    names = descriptor.getParamNames();
        final Class []  classes = descriptor.getParamClasses();
        final Object[] defaults = descriptor.getParamDefaults();
        final int numParameters = descriptor.getNumParameters();
        for (int i=0; i<numParameters; i++) {
            table.write(names[i]);
            table.nextColumn();
            table.write(Utilities.getShortName(classes[i]));
            table.nextColumn();
            Object value;
            if (param != null) {
                try {
                    value = param.getObjectParameter(names[i]);
                } catch (IllegalArgumentException exception) {
                    // There is no parameter with the specified name.
                    value = "#ERROR#";
                } catch (IllegalStateException exception) {
                    // The parameter is not set and there is no default.
                    value = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
                }
            } else {
                value = defaults[i];
            }
            final Object array;
            if (value!=null && value.getClass().isArray()) {
                array = value;
            } else {
                array = array1;
                array1[0] = value;
            }
            final int length = Array.getLength(array);
            for (int j=0; j<length; j++) {
                value = Array.get(array, j);
                if (value != ParameterListDescriptor.NO_PARAMETER_DEFAULT) {
                    if (value == null) {
                        value = "(automatic)";
                    } else if (KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL.equals(value)) {
                        value = "GRADIENT_MASK_SOBEL_HORIZONTAL";
                    } else if (KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL.equals(value)) {
                        value = "GRADIENT_MASK_SOBEL_VERTICAL";
                    } else if (value instanceof GridCoverage) {
                        value = ((GridCoverage) value).getName(null);
                    } else if (value instanceof Interpolation) {
                        value = getInterpolationName((Interpolation) value);
                    } else if (value instanceof EnumeratedParameter) {
                        value = ((EnumeratedParameter) value).getName();
                    } else if (value instanceof Color) {
                        final Color c = (Color) value;
                        value = "RGB["+c.getRed()+','+c.getGreen()+','+c.getBlue()+']';
                    }
                    if (j!=0) {
                        table.write(lineSeparator);
                    }
                    table.write(String.valueOf(value));
                }
            }
            table.nextLine();
        }
        table.writeHorizontalSeparator();
        table.flush();
    }
}
