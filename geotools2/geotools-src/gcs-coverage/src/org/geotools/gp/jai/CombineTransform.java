/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
 */
package org.geotools.gp.jai;


/**
 * Transform the sample values for one pixel during a "{@link Combine Combine}" operation.
 * The method {@link #transformSamples} is invoked by {@link Combine#computeRect
 * Combine.computeRect(...)} just before the sample values are combined as
 *
 * <code>values[0]*row[0] + values[1]*row[1] + values[2]*row[2] + ... + row[sourceBands]</code>.
 *
 * This interface provides a hook where non-linear transformations can be performed before the
 * linear one. For example, the <code>transformSamples</code> method could substitutes some
 * values by their logarithm.
 *
 * @version $Id: CombineTransform.java,v 1.2 2003/11/12 14:13:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface CombineTransform {
    /**
     * Transform the sample values for one pixel before the linear combinaison.
     *
     * @param values The sampel values to transformation.
     *               Transformation are performed in-place.
     */
    public abstract void transformSamples(final double[] values);

    /**
     * Returns <code>true</code> if the transformation performed by {@link #transformSamples}
     * do not depends on the ordering of samples in the <code>values</code> array. This method
     * can returns <code>true</code> if the <code>transformSamples(double[])</code> implementation
     * meet the following conditions:
     *
     * <ul>
     *   <li>The transformation is separable, i.e. the output value <code>values[i]</code> depends
     *       only on the input value <code>values[i]</code> for all <code>i</code>.</li>
     *   <li>The transformation do not depends on the value of the index <code>i</code>.
     * </ul>
     *
     * For example, the following implementations meets the above mentioned conditions:
     *
     * <blockquote><pre>
     * for (int i=0; i<values.length; i++) {
     *     values[i] = someFunction(values[i]);
     * }
     * </pre></blockquote>
     *
     * A <code>true</code> value will allows some optimisations inside the
     * {@link Combine#computeRect Combine.computeRect(...)} method. This method
     * may conservatly returns <code>false</code> if this information is unknow.
     */
    public abstract boolean isSeparable();
}
