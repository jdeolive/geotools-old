/**
 * 
 */
package org.geotools.gce.imagemosaic.index;

import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;

/**
 * Sparse utilities for the various mosaic classes. I use them to extract complex code from other places.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 *
 */
class Utils {
	/**
	 * Makes sure that an argument is non-null.
	 * 
	 * @param name
	 *                Argument name.
	 * @param object
	 *                User argument.
	 * @throws IllegalArgumentException
	 *                 if {@code object} is null.
	 */
	static void ensureNonNull(final String name, final Object object)
	        throws NullPointerException {
	    if (object == null) {
	        throw new NullPointerException(Errors.format(
	                ErrorKeys.NULL_ARGUMENT_$1, name));
	    }
	}
	
	
}
