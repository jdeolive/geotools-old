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
package org.geotools.io.coverage;

// Resources
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Thrown when one or more properties have ambiguous values. This exception is typically
 * thrown when a property is defined twice with different values.  It may also be thrown
 * if a property can be computed from other properties, but their values are inconsistent.
 *
 * @version $Id: AmbiguousPropertyException.java,v 1.3 2003/05/13 10:59:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class AmbiguousPropertyException extends PropertyException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 9024148330467307209L;

    /**
     * Construct an exception with the specified message. This exception is
     * usually raised because different values was found for the key <code>key</code>.
     *
     * @param message The message. If <code>null</code>, a message will
     *                be constructed from the alias.
     * @param key     The property key which was the cause for this exception, or
     *                <code>null</code> if none. This is a format neutral key,
     *                for example {@link PropertyParser#DATUM}.
     * @param alias   The alias used for for the key <code>key</code>, or <code>null</code>
     *                if none. This is usually the name used in the external file parsed.
     */
    public AmbiguousPropertyException(final String message,
                                      final PropertyParser.Key key,
                                      final String alias)
    {
        super((message!=null) ? message :  Resources.format(
              ResourceKeys.ERROR_INCONSISTENT_PROPERTY_$1, alias), key, alias);
    }
}
