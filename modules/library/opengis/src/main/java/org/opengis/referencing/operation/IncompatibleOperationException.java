/*$************************************************************************************************
 **
 ** $Id: IncompatibleOperationException.java 1265 2008-07-09 18:24:37Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M1/geoapi/src/main/java/org/opengis/referencing/operation/IncompatibleOperationException.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.referencing.operation;


/**
 * Thrown when an operation is applied in a manner inconsistent with one or both of
 * two particular CRS objects.
 *
 * @author  Jesse Crossley (SYS Technologies)
 * @since   GeoAPI 1.0
 */
public class IncompatibleOperationException extends Exception {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3197174832430350656L;

    /**
     * The invalid Operation name.
     */
    private final String operationName;

    /**
     * Creates an exception with the specified message and operation name.
     *
     * @param  message The detail message. The detail message is saved for
     *         later retrieval by the {@link #getMessage()} method.
     * @param  operationName The invalid operation name.
     */
    public IncompatibleOperationException(String message, String operationName) {
        super(message);
        this.operationName = operationName;
    }

    /**
     * Returns the invalid operation name.
     *
     * @return The invalid operation name.
     */
    public String getOperationName() {
        return operationName;
    }
}
