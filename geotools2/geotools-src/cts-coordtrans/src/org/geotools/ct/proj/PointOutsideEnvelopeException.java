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
package org.geotools.ct.proj;

// J2SE dependencies
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.CoordinateSystem;


/**
 * Thrown by {@link MapProjection} when a map projection failed because the point is
 * outside the envelope of validity. Bounds are usually 90°S to 90°N and 180°W to 180°E.
 *
 * @version $Id: PointOutsideEnvelopeException.java,v 1.1 2003/05/12 21:27:56 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see CoordinateSystem#getDefaultEnvelope
 */
public class PointOutsideEnvelopeException extends ProjectionException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4384490413772200352L;
    
    /**
     * Constructs a new exception with no detail message.
     */
    public PointOutsideEnvelopeException() {
        super();
    }
    
    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message The error message.
     */
    public PointOutsideEnvelopeException(final String message) {
        super(message);
    }
}
