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
package org.geotools.io.coverage;

// Resources
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Thrown when a property is required but can't be found. This error typically occurs
 * when a raster is being read but the file doesn't contains enough information for
 * constructing the raster's coordinate system.
 *
 * @version $Id: MissingPropertyException.java,v 1.1 2002/08/18 19:59:55 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MissingPropertyException extends PropertyException {
    /**
     * Construct an exception with the specified message.
     *
     * @param message  The message. If <code>null</code>, a message will
     *                 be constructed from the property name.
     * @param property The property name which has raised this exception.
     */
    public MissingPropertyException(final String message, final String property) {
        super( (message!=null) ? message :  Resources.format(
              (property!=null) ? ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1 :
                                 ResourceKeys.ERROR_UNDEFINED_PROPERTY, property), property);
    }
}
