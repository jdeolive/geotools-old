/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gp.jai;

// J2SE dependencies
import java.lang.reflect.Array;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * The operation descriptor for the {@link Combine} operation.
 *
 * @version $Id: CombineDescriptor.java,v 1.2 2003/07/18 13:49:56 desruisseaux Exp $
 * @author Remi Eve
 */
public final class CombineDescriptor extends OperationDescriptorImpl {
    /**
     * The operation name.
     */
    public static final String OPERATION_NAME = "org.geotools.Combine";

    /**
     * Construct the descriptor.
     */
    public CombineDescriptor() {
        super(new String[][]{{"GlobalName",  OPERATION_NAME},
                             {"LocalName",   OPERATION_NAME},
                             {"Vendor",      "Geotools 2"},
                             {"Description", "Combine two rendered images using a linear relation."},
                             {"DocURL",      "http://modules.geotools.org/gcs-coverage"},
                             {"Version",     "1.0"}},
              new String[]   {RenderedRegistryMode.MODE_NAME}, 2,
              new String[]   {"weights0",            // Argument names
                              "weights1",
                              "offsets"},
              new Class []   {double[].class,        // Argument classes
                              double[].class,
                              double[].class},
              new Object[]   {NO_PARAMETER_DEFAULT,  // Default values for parameters
                              NO_PARAMETER_DEFAULT,
                              new double[1]},
              null                                   // Valid parameter values.
        );
    }

    /**
     * Returns <code>true</code> if the parameters are valids. This implementation check
     * that the number of bands in the source src1 is equals to the number of bands of 
     * source src2.
     *
     * @param modeName The mode name (usually "Rendered").
     * @param param The parameter block for the operation to performs.
     * @param message A buffer for formatting an error message if any.
     */
    protected boolean validateParameters(final String      modeName,
                                         final ParameterBlock param,
                                         final StringBuffer message)
    {
        if (!super.validateParameters(modeName, param, message))  {
            return false;
        }
        final RenderedImage src1 = (RenderedImage)param.getSource(0);
        final RenderedImage src2 = (RenderedImage)param.getSource(1);            
        final int numBands1 = src1.getSampleModel().getNumBands();
        final int numBands2 = src2.getSampleModel().getNumBands();
        if (numBands1 != numBands2) {
            message.append(Resources.format(ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH));
            return false;
        }
        for (int i=param.getNumParameters(); --i>=0;) {
            if (Array.getLength(param.getObjectParameter(i)) == 0) {
                message.append(Resources.format(ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH));
                return false;
            }
        }
        return true;
    }
}
