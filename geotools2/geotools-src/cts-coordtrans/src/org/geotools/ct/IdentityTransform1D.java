/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
package org.geotools.ct;

// J2SE dependencies
import java.io.Serializable;


/**
 * A one dimensional, identity transform. Output values are identical to input values.
 * This class is really a special case of {@link LinearTransform1D} optimized for speed.
 *
 * @version $Id: IdentityTransform1D.java,v 1.1 2002/07/24 17:14:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class IdentityTransform1D extends LinearTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7378774584053573789L;

    /**
     * The shared instance of the identity transform.
     */
    public static final LinearTransform1D ONE = new IdentityTransform1D();

    /**
     * Construct a new identity transform.
     */
    private IdentityTransform1D() {
        super(1, 0);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(double value) {
        return value;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts);
    }
}
