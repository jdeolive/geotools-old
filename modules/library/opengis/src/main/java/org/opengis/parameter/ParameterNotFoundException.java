/*$************************************************************************************************
 **
 ** $Id: ParameterNotFoundException.java 1421 2009-06-04 20:18:07Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/parameter/ParameterNotFoundException.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.parameter;

import org.opengis.annotation.Extension;


/**
 * Thrown when a required parameter was not found in a
 * {@linkplain ParameterDescriptorGroup parameter group}.
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 *
 * @see ParameterDescriptorGroup#descriptor
 * @see ParameterValueGroup#parameter
 */
@Extension
public class ParameterNotFoundException extends IllegalArgumentException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8074834945993975175L;

    /**
     * The invalid parameter name.
     */
    private final String parameterName;

    /**
     * Creates an exception with the specified message and parameter name.
     *
     * @param message The detail message. The detail message is saved for
     *        later retrieval by the {@link #getMessage()} method.
     * @param parameterName The name of the parameter which was required but not found.
     */
    public ParameterNotFoundException(String message, String parameterName) {
        super(message);
        this.parameterName = parameterName;
    }

    /**
     * Returns the name of the parameter which was required but not found.
     *
     * @return The required parameter name.
     */
    public String getParameterName() {
        return parameterName;
    }
}
