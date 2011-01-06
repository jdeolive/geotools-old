/*$************************************************************************************************
 **
 ** $Id: KeyConstantsTest.java 1265 2008-07-09 18:24:37Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/test/java/org/opengis/referencing/KeyConstantsTest.java $
 **
 ** Copyright (C) 2003 Open GIS Consortium, Inc. All Rights Reserved. http://www.opengis.org/Legal/
 **
 *************************************************************************************************/
package org.opengis.referencing;

import org.opengis.referencing.datum.Datum;

import org.junit.*;
import org.opengis.referencing.operation.CoordinateOperation;
import static org.opengis.referencing.ReferenceSystem.*;
import static org.junit.Assert.*;


/**
 * Tests the value of key constants.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
public final class KeyConstantsTest {
    /**
     * Ensures that the key that are expected to be the same are really the same.
     * We use {@code assertSame} instead than {@code assertEquals} because we
     * expect the JVM to have {@linkplain String#intern internalized} the strings.
     */
    @Test
    public void testSame() {
        assertSame(SCOPE_KEY,              Datum              .SCOPE_KEY);
        assertSame(SCOPE_KEY,              CoordinateOperation.SCOPE_KEY);
        assertSame(DOMAIN_OF_VALIDITY_KEY, Datum              .DOMAIN_OF_VALIDITY_KEY);
        assertSame(DOMAIN_OF_VALIDITY_KEY, CoordinateOperation.DOMAIN_OF_VALIDITY_KEY);
    }
}
