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
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.CRIFImpl;


/**
 * The image factory for the {@link Combine} operation.
 *
 * @version $Id: CombineCRIF.java,v 1.1 2003/07/11 16:57:48 desruisseaux Exp $
 * @author Remi Eve
 */
public final class CombineCRIF extends CRIFImpl {
    /**
     * Creates a {@link RenderedImage} representing the results of an imaging
     * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
     */
    public RenderedImage create(final ParameterBlock param,
                                final RenderingHints hints)
    {
        final RenderedImage src0 = (RenderedImage) param.getSource(0);
        final RenderedImage src1 = (RenderedImage) param.getSource(1);
        final double[]  weights0 = (double[])param.getObjectParameter(0);
        final double[]  weights1 = (double[])param.getObjectParameter(1);
        final double[]   offsets = (double[])param.getObjectParameter(2);
        return new Combine(src0, src1, weights0, weights1, offsets, hints);
    }
}
