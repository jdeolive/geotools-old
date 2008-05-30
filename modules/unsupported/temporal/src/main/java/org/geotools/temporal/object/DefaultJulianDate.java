/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.temporal.object;

import org.opengis.temporal.IndeterminateValue;
import org.opengis.temporal.JulianDate;
import org.opengis.temporal.TemporalReferenceSystem;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultJulianDate extends DefaultTemporalCoordinate implements JulianDate {

    /**
     * Creates a new instance of JulianDate.
     * @param frame
     * @param indeterminatePosition
     * @param coordinateValue
     */
    public DefaultJulianDate(TemporalReferenceSystem frame, IndeterminateValue indeterminatePosition, Number coordinateValue) {
        super(frame, indeterminatePosition, coordinateValue);
    }
}
