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
 * @version $Id: MissingPropertyException.java,v 1.2 2002/08/22 11:16:08 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MissingPropertyException extends PropertyException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5215286265847774754L;

    /**
     * Construct an exception with the specified message. This exception is
     * usually raised because no value was defined for the key <code>key</code>.
     *
     * @param message The message. If <code>null</code>, a message will
     *                be constructed from the alias.
     * @param key     The property key which was the cause for this exception, or
     *                <code>null</code> if none. This is a format neutral key,
     *                for example {@link PropertyParser#DATUM}.
     * @param alias   The alias used for for the key <code>key</code>, or <code>null</code>
     *                if none. This is usually the name used in the external file parsed.
     */
    public MissingPropertyException(final String message,
                                    final PropertyParser.Key key,
                                    final String alias)
    {
        super((message!=null) ? message :  Resources.format(
                (alias!=null) ? ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1 :
                                ResourceKeys.ERROR_UNDEFINED_PROPERTY, alias), key, alias);
    }
}
