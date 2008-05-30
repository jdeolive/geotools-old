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

import java.util.Date;
import org.geotools.resources.Utilities;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.Position;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultPeriod extends DefaultTemporalGeometricPrimitive implements Period {

    /**
     * This is the TM_Instant at which this Period starts.
     */
    private Instant begining;
    /**
     * This is the TM_Instant at which this Period ends.
     */
    private Instant ending;
    private Position beginPosition;
    private Position endPosition;

    public DefaultPeriod(Instant begining, Instant ending) {
        if (((DefaultInstant) begining).getPosition().getDate().before(((DefaultInstant) begining).getPosition().getDate())) {
            this.begining = begining;
            this.ending = ending;
        } else {
            throw new IllegalArgumentException("The temporal position of the beginning of the period must be less than (i.e. earlier than) the temporal position of the end of the period");
        }

    }

    public DefaultPeriod(Position beginPosition, Position endPosition) {
        this.beginPosition = beginPosition;
        this.endPosition = endPosition;
    }

    public Instant getBeginning() {
        return begining;
    }

    public void setBegining(Instant begining) {
        this.begining = begining;
    }

    public void setBegining(Date date) {
        this.begining = new DefaultInstant(new DefaultPosition(date));
    }

    public Instant getEnding() {
        return ending;
    }

    public void setEnding(Instant ending) {
        this.ending = ending;
    }

    public void setEnding(Date date) {
        this.ending = new DefaultInstant(new DefaultPosition(date));
    }

    public Position getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(Position beginPosition) {
        this.beginPosition = beginPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Position endPosition) {
        this.endPosition = endPosition;
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof DefaultPeriod) {
            final DefaultPeriod that = (DefaultPeriod) object;

            return Utilities.equals(this.begining, that.begining) &&
                    Utilities.equals(this.getBeginPosition(), that.getBeginPosition()) &&
                    Utilities.equals(this.getEndPosition(), that.getEndPosition()) &&
                    Utilities.equals(this.ending, that.ending);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.getBeginPosition() != null ? this.getBeginPosition().hashCode() : 0);
        hash = 37 * hash + (this.begining != null ? this.begining.hashCode() : 0);
        hash = 37 * hash + (this.getEndPosition() != null ? this.getEndPosition().hashCode() : 0);
        hash = 37 * hash + (this.ending != null ? this.ending.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Period:").append('\n');
        if (begining != null) {
            s.append("begin:").append(begining).append('\n');
        }
        if (ending != null) {
            s.append("end:").append(ending).append('\n');
        }
        if (getBeginPosition() != null) {
            s.append("beginPosition:").append(getBeginPosition()).append('\n');
        }
        if (getEndPosition() != null) {
            s.append("endPosition:").append(getEndPosition()).append('\n');
        }

        return s.toString();
    }
}