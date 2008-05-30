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

import org.geotools.resources.Utilities;
import org.opengis.temporal.IndeterminateValue;
import org.opengis.temporal.TemporalCoordinate;
import org.opengis.temporal.TemporalReferenceSystem;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultTemporalCoordinate extends DefaultTemporalPosition implements TemporalCoordinate {

    /**
     * This is the distance from the scale origin expressed as a multiple of the standard interval associated with the temporal coordinate system.
     */
    private Number coordinateValue;

    public DefaultTemporalCoordinate(TemporalReferenceSystem frame, IndeterminateValue indeterminatePosition, Number coordinateValue) {
        super(frame, indeterminatePosition);
        this.coordinateValue = coordinateValue;
    }

    @Override
    public Number getCoordinateValue() {
        return coordinateValue;
    }

    public void setCoordinateValue(Number coordinateValue) {
        this.coordinateValue = coordinateValue;
    }

    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final DefaultTemporalCoordinate that = (DefaultTemporalCoordinate) object;

            return Utilities.equals(this.coordinateValue, that.coordinateValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.coordinateValue != null ? this.coordinateValue.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("TemporalCoordinate:").append('\n');
        if (coordinateValue != null) {
            s.append("coordinateValue:").append(coordinateValue).append('\n');
        }
        return s.toString();
    }
}
