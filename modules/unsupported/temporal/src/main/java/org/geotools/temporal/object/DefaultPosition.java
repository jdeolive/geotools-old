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

import java.sql.Time;
import java.text.ParseException;
import java.util.Date;
import org.geotools.resources.Utilities;
import org.opengis.temporal.Position;
import org.opengis.temporal.TemporalPosition;
import org.opengis.util.InternationalString;

/**
 *
 * @author Mehdi Sidhoum
 */
public class DefaultPosition implements Position {

    /**
     * this object represents one of the data types listed as : Date, Time, DateTime, and TemporalPosition with its subtypes
     */
    private Object position;

    public DefaultPosition(Date date) {
        this.position = date;
    }

    public DefaultPosition(Time time) {
        this.position = time;
    }

    /**
     * This constructor replace the constructor with further DateTime object which will be included in the futur version of jdk (jdk7).
     * @param dateTime
     * @throws java.text.ParseException
     */
    public DefaultPosition(InternationalString dateTime) throws ParseException {
        this.position = dateTime;
    }

    public DefaultPosition(TemporalPosition anyOther) {
        this.position = anyOther;
    }

    public TemporalPosition anyOther() {
        return (this.position instanceof TemporalPosition) ? (TemporalPosition) position : null;
    }

    public Date getDate() {
        return (this.position instanceof Date) ? (Date) position : null;
    }

    public Time getTime() {
        return (this.position instanceof Time) ? (Time) position : null;
    }

    public InternationalString getDateTime() {
        return (this.position instanceof InternationalString) ? (InternationalString) position : null;
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof DefaultPosition) {
            final DefaultPosition that = (DefaultPosition) object;
            return Utilities.equals(this.position, that.position);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.position != null ? this.position.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Position:").append('\n');
        if (position != null) {
            s.append("position:").append(position).append('\n');
        }
        return s.toString();
    }
}
