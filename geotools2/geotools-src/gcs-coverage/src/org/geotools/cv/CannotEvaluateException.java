/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
package org.geotools.cv;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.FieldPosition;

// Geotools dependencies
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * The base class for exceptions thrown when a quantity can't be evaluated.
 * This exception is usually invoked by a <code>Coverage.evaluate</code>
 * method, for example when a point is outside the coverage.
 *
 * @version $Id: CannotEvaluateException.java,v 1.2 2003/05/13 10:59:49 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class CannotEvaluateException extends RuntimeException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7498354094801102258L;
    
    /**
     * Construct an exception with no message.
     */
    public CannotEvaluateException() {
    }

    /**
     * Construct an exception with the specified message.
     *
     * @param  message The detail message. The detail message is saved for 
     *         later retrieval by the {@link #getMessage()} method.
     */
    public CannotEvaluateException(final String message) {
        super(message);
    }

    /**
     * Construct an exception with the specified message.
     *
     * @param  message The detail message. The detail message is saved for 
     *         later retrieval by the {@link #getMessage()} method.
     * @param  cause The cause, which is saved for later retrieval by the
     *         {@link #getCause()} method. A <code>null</code> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.
     */
    public CannotEvaluateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct an exception with a message built from the specified coordinate point.
     *
     * @param  point The coordinate point which can't be evaluated.
     * @param  cause The cause, which is saved for later retrieval by the
     *         {@link #getCause()} method. A <code>null</code> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.
     */
    public CannotEvaluateException(final Point2D point, final Throwable cause) {
        this(new CoordinatePoint(point), cause);
    }

    /**
     * Construct an exception with a message built from the specified coordinate point.
     *
     * @param  point The coordinate point which can't be evaluated.
     * @param  cause The cause, which is saved for later retrieval by the
     *         {@link #getCause()} method. A <code>null</code> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.
     */
    public CannotEvaluateException(final CoordinatePoint point, final Throwable cause) {
        super(Resources.format(ResourceKeys.ERROR_CANT_EVALUATE_$1, toString(point)), cause);
    }
    
    /**
     * Construct a string for the specified point. This is
     * used by constructor expecting a coordinate point.
     *
     * @param  point The coordinate point to format.
     * @return The coordinate point as a string, without '(' or ')' characters.
     */
    static String toString(final CoordinatePoint point) {
        final StringBuffer buffer = new StringBuffer();
        final FieldPosition dummy = new FieldPosition(0);
        final NumberFormat format = NumberFormat.getNumberInstance();
        final int       dimension = point.getDimension();
        for (int i=0; i<dimension; i++) {
            if (i!=0) {
                buffer.append(", ");
            }
            format.format(point.getOrdinate(i), buffer, dummy);
        }
        return buffer.toString();
    }
}
