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
 */
package org.geotools.io.image;

// Image I/O
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;


/**
 * An abstract adapter class for receiving image progress events.
 * The methods in this class are empty. This class exists as
 * convenience for creating listener objects.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class IIOReadProgressAdapter implements IIOReadProgressListener {
    public void sequenceStarted  (ImageReader source, int minIndex)                       {}
    public void sequenceComplete (ImageReader source)                                     {}
    public void imageStarted     (ImageReader source, int imageIndex)                     {}
    public void imageProgress    (ImageReader source, float percentageDone)               {}
    public void imageComplete    (ImageReader source)                                     {}
    public void thumbnailStarted (ImageReader source, int imageIndex, int thumbnailIndex) {}
    public void thumbnailProgress(ImageReader source, float percentageDone)               {}
    public void thumbnailComplete(ImageReader source)                                     {}
    public void readAborted      (ImageReader source)                                     {}
}
