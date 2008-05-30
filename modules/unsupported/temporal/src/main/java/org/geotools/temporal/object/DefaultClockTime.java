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
import org.opengis.temporal.ClockTime;
import org.opengis.temporal.IndeterminateValue;
import org.opengis.temporal.TemporalReferenceSystem;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultClockTime extends DefaultTemporalPosition implements ClockTime {

    /**
     * This is a sequence of positive numbers with a structure similar to a CalendarDate.
     */
    private Number[] clockTime;

    public DefaultClockTime(TemporalReferenceSystem frame, IndeterminateValue indeterminatePosition, Number[] clockTime) {
        super(frame, indeterminatePosition);
        this.clockTime = clockTime;
    }

    @Override
    public Number[] getClockTime() {
        return clockTime;
    }

    public void setClockTime(Number[] clockTime) {
        this.clockTime = clockTime;
    }

    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final DefaultClockTime that = (DefaultClockTime) object;

            return Utilities.equals(this.clockTime, that.clockTime);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.clockTime != null ? this.clockTime.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("ClockTime:").append('\n');
        if (clockTime != null) {
            s.append("clockTime:").append(clockTime).append('\n');
        }
        return s.toString();
    }
}
