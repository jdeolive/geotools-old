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
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.operator.BandCombineDescriptor;

// Geotools dependencies
import org.geotools.gp.jai.CombineDescriptor;


/**
 * The &quot;Combine&quot; operation. This operation can be simplified to a JAI's
 * &quot;{@linkplain BandCombineDescriptor BandCombine}&quot; if the operation has
 * only one source.
 *
 * @version $Id: CombineOperation.java,v 1.1 2003/07/23 18:04:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class CombineOperation extends PolyadicOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5007404929716610690L;
    
    /**
     * Creates a new instance of <code>CombineOperation</code>
     */
    public CombineOperation() {
        super(CombineDescriptor.OPERATION_NAME);
    }

    /**
     * Apply the JAI operation. If the operation has only one source and no transform, then this
     * method maps directly to a JAI &quot;{@linkplain BandCombineDescriptor BandCombine}&quot;
     * operation.
     *
     * @param parameters The parameters to be given to JAI.
     * @param hints The rendering hints to be given to JAI.
     */
    protected RenderedImage createRenderedImage(final ParameterBlockJAI parameters,
                                                final RenderingHints    hints)
    {
        if (parameters.getNumSources() == 1) {
            final Object transform = parameters.getObjectParameter("transform");
            if (transform == null) {
                return getJAI(hints).createNS("BandCombine", parameters, hints);
            }
        }
        return super.createRenderedImage(parameters, hints);
    }
}
