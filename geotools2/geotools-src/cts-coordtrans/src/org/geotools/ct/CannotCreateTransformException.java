/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.ct;

// Geotools dependences
import org.geotools.cs.Datum;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.FactoryException;

// Resources
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Thrown when a coordinate transformation can't be created.
 * It may be because there is no known path between source and coordinate systems,
 * or because the requested transformation is not available in the environment.
 *
 * @version $Id: CannotCreateTransformException.java,v 1.5 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CannotCreateTransformException extends TransformException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5368463308772454145L;
    
    /**
     * Construct an exception with no detail message.
     */
    public CannotCreateTransformException() {
    }

    /**
     * Construct an exception with the specified detail message.
     *
     * @param message The details message.
     */
    public CannotCreateTransformException(final String message) {
        super(message);
    }

    /**
     * Construct an exception with the specified detail message and cause.
     *
     * @param message The details message.
     * @param cause The cause for this exception.
     */
    public CannotCreateTransformException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct an exception with a message stating that no transformation
     * path has been found between the specified coordinate systems.
     *
     * @param sourceCS The source coordinate system.
     * @param targetCS The target coordinate system.
     */
    public CannotCreateTransformException(final CoordinateSystem sourceCS,
                                          final CoordinateSystem targetCS)
    {
        super(Resources.format(ResourceKeys.ERROR_NO_TRANSFORMATION_PATH_$2,
                               getName(sourceCS), getName(targetCS)));
    }
    
    /**
     * Construct an exception with a message stating that no transformation
     * path has been found between the specified coordinate systems.
     *
     * @param sourceCS The source coordinate system.
     * @param targetCS The target coordinate system.
     * @param cause    The cause for the failure.
     */
    public CannotCreateTransformException(final CoordinateSystem sourceCS,
                                          final CoordinateSystem targetCS,
                                          final Throwable           cause)
    {
        super(Resources.format(ResourceKeys.ERROR_NO_TRANSFORMATION_PATH_$2,
                               getName(sourceCS), getName(targetCS)), cause);
    }
    
    /**
     * Gets a display name for the specified coordinate system.
     */
    private static String getName(final CoordinateSystem cs) {
        return Utilities.getShortClassName(cs)+'('+cs.getName(null)+')';
    }
}
