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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;


/**
 * Throws when a "resample" operation has been requested
 * but the specified grid coverage can't be reprojected.
 *
 * @version $Id: CannotReprojectException.java,v 1.3 2003/05/13 10:59:52 desruisseaux Exp $
 * @author  Martin Desruisseaux
 */
public class CannotReprojectException extends RuntimeException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8145425848361056027L;
    
    /**
     * Creates a new <code>CannotReprojectException</code> without detail message.
     */
    public CannotReprojectException() {
    }
    
    /**
     * Constructs a <code>CannotReprojectException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public CannotReprojectException(final String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param msg the detail message.
     * @param cause The cause of this exception.
     */
    public CannotReprojectException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
