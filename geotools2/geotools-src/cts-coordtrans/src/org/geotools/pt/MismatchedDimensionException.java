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
 */
package org.geotools.pt;

// Miscellaneous
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;


/**
 * Indicates that an operation cannot be completed properly because
 * of a mismatch in the dimensions of object attributes.
 *
 * @version $Id: MismatchedDimensionException.java,v 1.2 2003/01/20 23:16:18 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MismatchedDimensionException extends IllegalArgumentException {
    /**
     * Creates new exception without detail message.
     */
    public MismatchedDimensionException() {
    }
    
    /**
     * Constructs an exception with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public MismatchedDimensionException(final String msg) {
        super(msg);
    }
    
    /**
     * Construct an exception with a detail message stating that
     * two objects don't have the same number of dimensions.
     *
     * @param object1 The first dimensioned object.
     * @param object2 The second dimensioned object. Its dimension
     *        should be different than <code>object1</code>'s dimension,
     *        otherwise there is no dimension mismatch!
     */
    public MismatchedDimensionException(final Dimensioned object1, final Dimensioned object2) {
        this(object1.getDimension(), object2.getDimension());
    }
    
    /**
     * Construct an exception with a detail message stating that
     * two objects don't have the same number of dimensions.
     *
     * @param dim1 Number of dimensions for the first object.
     * @param dim2 Number of dimensions for the second object.
     *        It shoud be different than <code>dim1</code>,
     *        otherwise there is no dimension mismatch!
     */
    public MismatchedDimensionException(final int dim1, final int dim2) {
        this(Resources.format(ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                              new Integer(dim1), new Integer(dim2)));
    }
}
