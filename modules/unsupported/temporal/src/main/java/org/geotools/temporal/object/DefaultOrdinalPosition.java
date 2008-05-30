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
import org.opengis.temporal.OrdinalEra;
import org.opengis.temporal.OrdinalPosition;
import org.opengis.temporal.TemporalReferenceSystem;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultOrdinalPosition extends DefaultTemporalPosition implements OrdinalPosition {

    /**
     * This is z reference to the ordinal era in which the instant occurs.
     */
    private OrdinalEra ordinalPosition;

    public DefaultOrdinalPosition(TemporalReferenceSystem frame, IndeterminateValue indeterminatePosition, OrdinalEra ordinalPosition) {
        super(frame, indeterminatePosition);
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public OrdinalEra getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(OrdinalEra ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final DefaultOrdinalPosition that = (DefaultOrdinalPosition) object;

            return Utilities.equals(this.ordinalPosition, that.ordinalPosition);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.ordinalPosition != null ? this.ordinalPosition.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("OrdinalPosition:").append('\n');
        if (ordinalPosition != null) {
            s.append("ordinalPosition:").append(ordinalPosition).append('\n');
        }
        return s.toString();
    }
}
