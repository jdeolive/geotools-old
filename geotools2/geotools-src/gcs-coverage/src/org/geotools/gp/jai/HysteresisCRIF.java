/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, 2ie Technologie
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
package org.geotools.gp.jai;

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;


/**
 * The factory for the {@link Hysteresis} operation.
 *
 * @version $Id: HysteresisCRIF.java,v 1.1 2003/07/18 13:49:56 desruisseaux Exp $
 * @author Lionel Flahaut
 */
public class HysteresisCRIF extends CRIFImpl {
    /**
     * Construct a default factory.
     */
    public HysteresisCRIF() {
    }

    /**
     * Creates a {@link RenderedImage} representing the results of an imaging
     * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
     */
    public RenderedImage create(final ParameterBlock param,
                                final RenderingHints hints)
    {
        final RenderedImage image = (RenderedImage)param.getSource(0);
        final ImageLayout  layout = (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT);
        final double      low = param.getDoubleParameter(0);
        final double     high = param.getDoubleParameter(1);
        final double padValue = param.getDoubleParameter(2);
        return new Hysteresis(image, layout, hints, low, high, padValue);
    }
}	
