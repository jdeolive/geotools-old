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
package org.geotools.ct;

// Resources
import org.geotools.resources.Geometry;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.Point2D;


/**
 * Concatenated transform in which the resulting transform is two-dimensional.
 *
 * @version $Id: ConcatenatedTransform2D.java,v 1.1 2002/07/12 16:42:31 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ConcatenatedTransform2D extends ConcatenatedTransform implements MathTransform2D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7307709788564866500L;
    
    /**
     * Construct a concatenated transform.
     */
    public ConcatenatedTransform2D(final MathTransformFactory provider,
                                   final MathTransform transform1,
                                   final MathTransform transform2)
    {
        super(provider, transform1, transform2);
    }
    
    /**
     * Check if transforms are compatibles
     * with this implementation.
     */
    protected boolean isValid() {
        return super.isValid() && getDimSource()==2 && getDimTarget()==2;
    }
}
