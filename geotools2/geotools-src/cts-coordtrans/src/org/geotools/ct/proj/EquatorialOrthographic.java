/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here.
 */
/*
** Permission is hereby granted, free of charge, to any person obtaining
** a copy of this software and associated documentation files (the
** "Software"), to deal in the Software without restriction, including
** without limitation the rights to use, copy, modify, merge, publish,
** distribute, sublicense, and/or sell copies of the Software, and to
** permit persons to whom the Software is furnished to do so, subject to
** the following conditions:
**
** The above copyright notice and this permission notice shall be
** included in all copies or substantial portions of the Software.
**
** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
** EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
** MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
** IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
** CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
** TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
** SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.geotools.ct.proj;

// J2SE dependencies
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.cs.Projection;
import org.geotools.ct.MissingParameterException;

// Resources
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * The equatorial case of the {@link Orthographic} projection. This is a 
 * simplification of the oblique case for {@link #latitudeOfOrigin} == 0.0. 
 * Only the spherical form is given here.
 *
 * @task REVISIT: this code is identical to the oblique except for 6 lines.
 *                It could be moved to the oblique with an isEquatorial flag.
 *
 * @version $Id: EquatorialOrthographic.java,v 1.1 2004/01/11 17:11:55 desruisseaux Exp $
 * @author Rueben Schulz
 */
public class EquatorialOrthographic extends ObliqueOrthographic {
    /**
     * Construct an equatorial orthographic projection.
     *
     * @param  parameters The parameter values in standard units.
     * @throws MissingParameterException if a mandatory parameter is missing.
     */
    protected EquatorialOrthographic(final Projection parameters) throws MissingParameterException {
        super(parameters);
        
        assert (latitudeOfOrigin < EPS) : latitudeOfOrigin;
        latitudeOfOrigin = 0.0;
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in <code>ptDst</code> (units in meters). 
     */
    protected Point2D transformNormalized(double x, double y, Point2D ptDst)
            throws ProjectionException
    {
        // Compute using oblique formulas, for comparaison later.
        assert (ptDst = super.transformNormalized(x, y, ptDst)) != null;
        
        double cosphi = Math.cos(y);
	double coslam = Math.cos(x);
        
        if (cosphi * coslam < -TOL) {
            throw new ProjectionException(Resources.format(
                ResourceKeys.ERROR_POINT_OUTSIDE_HEMISPHERE));
        }
        
	y = Math.sin(y);
        x = cosphi * Math.sin(x);   
      
        assert Math.abs(ptDst.getX()-x) <= EPS*globalScale : x;
        assert Math.abs(ptDst.getY()-y) <= EPS*globalScale : y;
        
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
    
    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in <code>ptDst</code>.
     */
    protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst)
            throws ProjectionException
    {

        // Compute using oblique formulas, for comparaison later.
        assert (ptDst = super.inverseTransformNormalized(x, y, ptDst)) != null;
            
        final double rho = Math.sqrt(x*x + y*y);
        double sinc = rho;
        if (sinc > 1.0) {
            if ((sinc - 1.0) > TOL) {
                throw new ProjectionException(Resources.format(
                    ResourceKeys.ERROR_POINT_OUTSIDE_HEMISPHERE));
            }
            sinc = 1.0;
        }
        
        double cosc = Math.sqrt(1.0 - sinc * sinc); /* in this range OK */
        if (Math.abs(rho) <= TOL) {
            y = latitudeOfOrigin;
            x = 0.0;
        } else {
            double phi = y * sinc / rho;
            x *= sinc;
            y = cosc * rho;
            
            //begin sinchk
            if (Math.abs(phi) >= 1.0) {
                phi = (phi < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
            }
            else {
                phi = Math.asin(phi);
            }
            //end sinchk
            
            if (y == 0.0) {
                if(x == 0.0) {
                    x = 0.0;
                } else {
                    x = (x < 0.0) ? -Math.PI/2.0 : Math.PI/2.0;
                }
            } else {
                x = Math.atan2(x, y);
            }
            y = phi;
        }
        
        assert Math.abs(ptDst.getX()-x) <= EPS : x;
        assert Math.abs(ptDst.getY()-y) <= EPS : y;
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
    }
}
