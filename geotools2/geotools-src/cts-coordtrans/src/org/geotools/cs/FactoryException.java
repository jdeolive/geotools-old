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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;


/**
 * Thrown to indicate that a factory operation could not complete because of
 * a failure in the backing store, or a failure to contact the backing store.
 * The most common raison is a failure to find the authority code, which is
 * indicated by {@link NoSuchAuthorityCodeException}.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class FactoryException extends Exception {
    /**
     * Construct an exception with no message.
     */
    public FactoryException() {
        super();
    }
    
    /**
     * Construct an exception with the specified detail message.
     */
    public FactoryException(final String message) {
        super(message);
    }
    
    /**
     * Construct an exception with the specified detail message
     * and a cause.
     */
    public FactoryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
