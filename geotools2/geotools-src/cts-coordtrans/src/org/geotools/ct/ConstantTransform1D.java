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

// J2SE dependencies
import java.util.Arrays;
import java.io.Serializable;


/**
 * A one dimensional, constant transform. Output values are set to a constant value regardless
 * of input values. This class is really a special case of {@link LinearTransform1D} in which
 * <code>{@link #scale} = 0</code> and <code>{@link #offset} = constant</code>. However, this
 * specialized <code>ConstantTransform1D</code> class is faster.
 *
 * @version $Id: ConstantTransform1D.java,v 1.2 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class ConstantTransform1D extends LinearTransform1D {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1583675681650985947L;

    /**
     * Construct a new constant transform.
     *
     * @param offset The <code>offset</code> term in the linear equation.
     */
    protected ConstantTransform1D(final double offset) {
        super(0, offset);
    }
    
    /**
     * Transforms the specified value.
     */
    public double transform(double value) {
        return offset;
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final float[] srcPts, int srcOff,
                          final float[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, (float)offset);
    }
    
    /**
     * Transforms a list of coordinate point ordinal values.
     */
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
    {
        Arrays.fill(dstPts, dstOff, dstOff+numPts, offset);
    }
}
