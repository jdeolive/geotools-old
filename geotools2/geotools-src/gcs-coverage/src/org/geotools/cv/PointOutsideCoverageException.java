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
package org.geotools.cv;

// J2SE dependencies
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.pt.CoordinatePoint;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Throws when a <code>Coverage.evaluate</code>
 * method is invoked with a point outside coverage.
 *
 * @version $Id: PointOutsideCoverageException.java,v 1.4 2003/05/13 10:59:50 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class PointOutsideCoverageException extends CannotEvaluateException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6254433330922415993L;
    
    /**
     * Construct an exception with no message.
     */
    public PointOutsideCoverageException() {
    }
    
    /**
     * Construct an exception with the specified message.
     */
    public PointOutsideCoverageException(final String message) {
        super(message);
    }
    
    /**
     * Construct an exception with a message for the specified point.
     */
    public PointOutsideCoverageException(final Point2D point) {
        this(new CoordinatePoint(point));
    }
    
    /**
     * Construct an exception with a message for the specified point.
     */
    public PointOutsideCoverageException(final CoordinatePoint point) {
        super(Resources.format(ResourceKeys.ERROR_POINT_OUTSIDE_COVERAGE_$1, toString(point)));
    }
}
