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
 */
package org.geotools.io.image;

// Input/output
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.metadata.IIOMetadata;


/**
 * Base class for text image encoders. "Text images" are usually ASCII files
 * containing pixel values (often geophysical values, like sea level anomalies).
 *
 * TODO: NOT YET IMPLEMENTED
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class TextImageWriter extends ImageWriter {
    /**
     * Constructs a <code>TextImageWriter</code>.
     *
     * @param originatingProvider The <code>ImageWriterSpi</code> that
     *        is constructing this object, or <code>null</code>.
     */
    protected TextImageWriter(final ImageWriterSpi provider) {
        super(provider);
    }
    
    /**
     * Appends a complete image stream containing a single image.
     */
    //  public void write(final IIOMetadata streamMetadata, final IIOImage image, final ImageWriteParam param) throws IOException
    //  {write(image.getRenderedImage(), new AffineTransform(), new Theme[0], param);}
    
    /**
     * Returns always <code>null</code> since
     * this encoder doesn't support meta-data.
     */
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }
    
    /**
     * Returns always <code>null</code> since
     * this encoder doesn't support meta-data.
     */
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,
                                               ImageWriteParam    param)
    {
        return null;
    }
    
    /**
     * Returns always <code>inData</code> since
     * this encoder doesn't support meta-data.
     */
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return inData;
    }
    
    /**
     * Returns always <code>inData</code> since
     * this encoder doesn't support meta-data.
     */
    public IIOMetadata convertImageMetadata(IIOMetadata        inData,
                                            ImageTypeSpecifier imageType,
                                            ImageWriteParam    param)
    {
        return inData;
    }
}
