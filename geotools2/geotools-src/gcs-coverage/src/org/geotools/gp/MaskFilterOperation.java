/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.RenderingHints;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.EnumeratedParameter;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.operator.MinFilterShape;
import javax.media.jai.operator.MaxFilterShape;
import javax.media.jai.operator.MedianFilterShape;
import javax.media.jai.operator.MinFilterDescriptor;
import javax.media.jai.operator.MaxFilterDescriptor;
import javax.media.jai.operator.MedianFilterDescriptor;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.cs.CoordinateSystem;


/**
 * The implementation of JAI's {@link MinFilterDescriptor MinFilter},
 * {@link MaxFilterDescriptor MaxFilter} and {@link MedianFilterDescriptor MedianFilter}.
 *
 * @version $Id: MaskFilterOperation.java,v 1.1 2003/07/23 10:33:14 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class MaskFilterOperation extends FilterOperation {
    /**
     * Returns the default value for mask shape.
     */
    private static EnumeratedParameter getDefaultMaskShape(final String name) {
        if (name.equalsIgnoreCase(   "MinFilter")) return    MinFilterDescriptor.   MIN_MASK_SQUARE;
        if (name.equalsIgnoreCase(   "MaxFilter")) return    MaxFilterDescriptor.   MAX_MASK_SQUARE;
        if (name.equalsIgnoreCase("MedianFilter")) return MedianFilterDescriptor.MEDIAN_MASK_SQUARE;
        throw new IllegalArgumentException(name);
    }

    /**
     * Construct a new filter operation.
     *
     * @param name The operation name. Should be "MinFilter", "MaxFilter" or "MedianFilter".
     */
    public MaskFilterOperation(final String name) {
        this(getOperationDescriptor(name), getDefaultMaskShape(name));
    }

    /**
     * Construct a new filter operation.
     *
     * @param descriptor The operation descriptor.
     * @param defaultShape The default mask shape. Should be an enumeration of kind
     *        {@link MinFilterShape}, {@link MaxFilterShape} or {@link MedianFilterShape}.
     *
     * @task TODO: The "SampleDimension" argument is not yet supported.
     */
    private MaskFilterOperation(final OperationDescriptor descriptor,
                                final EnumeratedParameter defaultShape)
    {
        super(descriptor.getName(), descriptor, new ParameterListDescriptorImpl(
          descriptor,    // the object to be reflected upon for enumerated values.
          new String[] { // the names of each parameter.
              "Source",
           // "SampleDimension",
              "Xsize",
              "Ysize",
              "MaskShape" // Not an OpenGIS parameter.
          },
          new Class[]   // the class of each parameter.
          {
              GridCoverage.class,
           // Integer.class,
              Integer.class,
              Integer.class,
              defaultShape.getClass()
          },
          new Object[] // The default values for each parameter.
          {
              ParameterListDescriptor.NO_PARAMETER_DEFAULT,
           // ZERO,
              THREE,
              THREE,
              defaultShape
          },
          new Object[] // Defines the valid values for each parameter.
          {
              null,
           // RANGE_0,
              RANGE_1,
              RANGE_1,
              null
          }));
    }

    /**
     * Set a parameter. This method override the {@link OperationJAI} method
     * in order to apply some conversions from OpenGIS to JAI parameter names.
     *
     * @param block The parameter block in which to set a parameter.
     * @param name  The parameter OpenGIS name.
     * @param value The parameter OpenGIS value.
     */
    void setParameter(final ParameterBlockJAI block, String name, final Object value) {
        if (name.equalsIgnoreCase("Xsize") || name.equalsIgnoreCase("Ysize")) {
            name = "maskSize";
        }
        block.setParameter(name, value);
    }

    /**
     * Apply the operation.
     */
    protected GridCoverage doOperation(final ParameterList  parameters, RenderingHints hints) {
        final int xSize = parameters.getIntParameter("Xsize");
        final int ySize = parameters.getIntParameter("Ysize");
        if (xSize != ySize) {
            throw new UnsupportedOperationException("Xsize and Ysize must have the same value.");
        }
        return super.doOperation(parameters, hints);
    }
}
