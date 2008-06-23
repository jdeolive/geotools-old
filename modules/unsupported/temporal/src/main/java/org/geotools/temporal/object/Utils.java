/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import org.geotools.temporal.reference.DefaultOrdinalEra;
import org.geotools.temporal.reference.DefaultTemporalCoordinateSystem;
import org.geotools.util.SimpleInternationalString;
import org.opengis.temporal.CalendarDate;
import org.opengis.temporal.DateAndTime;
import org.opengis.temporal.IndeterminateValue;
import org.opengis.temporal.JulianDate;
import org.opengis.temporal.OrdinalEra;
import org.opengis.temporal.OrdinalPosition;
import org.opengis.temporal.TemporalCoordinate;
import org.opengis.temporal.TemporalCoordinateSystem;

/**
 * This is a tool class to convert DateTime from ISO8601 to Date object.
 * 
 * @author Mehdi Sidhoum (Geomatys)
 */
public class Utils {

    Logger logger = Logger.getLogger("org.geotools.temporal");
    /**
     * The number of millisecond in one year.
     */
    private final static long yearMS = 31536000000L;
    /**
     * The number of millisecond in one month.
     */
    private final static long monthMS = 2628000000L;
    /**
     * The number of millisecond in one week.
     */
    private final static long weekMS = 604800000L;
    /**
     * The number of millisecond in one day.
     */
    private final static long dayMS = 86400000L;
    /**
     * The number of millisecond in one hour.
     */
    private final static long hourMS = 3600000L;
    /**
     * The number of millisecond in one minute.
     */
    private final static long minMS = 60000;
    /**
     * The number of millisecond in one second.
     */
    private final static long secondMS = 1000;

    public Utils() {
    }

    /**
     * Returns the length of the duration (PnYnMnDTnHnMnS Iso 8601) in milli-seconds.
     * @param duration
     * @param startInstant
     * @return
     */
    public static long getTimeFromDuration(String durationString, Date startInstant) throws DatatypeConfigurationException {
        DatatypeFactory df = DatatypeFactory.newInstance();
        Duration duration = df.newDuration(durationString);
        return duration.getTimeInMillis(startInstant);
    }

    /**
     * Returns a Date object from an ISO-8601 representation string. (String defined with pattern yyyy-MM-dd'T'HH:mm:ss.SSSZ or yyyy-MM-dd).
     * @param dateString
     * @return
     */
    public static Date getDateFromString(String dateString) throws ParseException {
        Date response = null;
        String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        String DATE_FORMAT2 = "yyyy-MM-dd";
        SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
        SimpleDateFormat sdf2 = new java.text.SimpleDateFormat(DATE_FORMAT2);

        if (dateString.contains("T")) {
            //set the timezone if exists.
            String timezone = getTimeZone(dateString);
            sdf.setTimeZone(TimeZone.getTimeZone(timezone));

            response = sdf.parse(dateString);
        } else {
            if (dateString.contains("-")) {
                response = sdf2.parse(dateString);
            }
        }
        return response;
    }

    public static String getTimeZone(String dateString) {
        int index = dateString.lastIndexOf("+");
        if (index == -1) {
            index = dateString.lastIndexOf("-");
        }
        return "GMT" + dateString.substring(index);
    }

    /**
     * Return a Date (long time) from a String description
     * 
     * @param periodDuration
     * @return
     */
    public static long getTimeInMillis(String periodDuration) {

        long time = 0;
        //we remove the 'P'
        periodDuration = periodDuration.substring(1);

        //we look if the period contains years (31536000000 ms)
        if (periodDuration.indexOf('Y') != -1) {
            int nbYear = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('Y')));
            time += nbYear * yearMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('Y') + 1);
        }

        //we look if the period contains months (2628000000 ms)
        if (periodDuration.indexOf('M') != -1 &&
                (periodDuration.indexOf("T") == -1 || periodDuration.indexOf("T") > periodDuration.indexOf('M'))) {
            int nbMonth = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('M')));
            time += nbMonth * monthMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('M') + 1);
        }

        //we look if the period contains weeks (604800000 ms)
        if (periodDuration.indexOf('W') != -1) {
            int nbWeek = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('W')));
            time += nbWeek * weekMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('W') + 1);
        }

        //we look if the period contains days (86400000 ms)
        if (periodDuration.indexOf('D') != -1) {
            int nbDay = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('D')));
            time += nbDay * dayMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('D') + 1);
        }

        //if the periodDuration is not over we pass to the hours by removing 'T'
        if (periodDuration.indexOf('T') != -1) {
            periodDuration = periodDuration.substring(1);
        }

        //we look if the period contains hours (3600000 ms)
        if (periodDuration.indexOf('H') != -1) {
            int nbHour = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('H')));
            time += nbHour * hourMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('H') + 1);
        }

        //we look if the period contains minutes (60000 ms)
        if (periodDuration.indexOf('M') != -1) {
            int nbMin = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('M')));
            time += nbMin * minMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('M') + 1);
        }

        //we look if the period contains seconds (1000 ms)
        if (periodDuration.indexOf('S') != -1) {
            int nbSec = Integer.parseInt(periodDuration.substring(0, periodDuration.indexOf('S')));
            time += nbSec * secondMS;
            periodDuration = periodDuration.substring(periodDuration.indexOf('S') + 1);
        }

        if (periodDuration.length() != 0) {
            throw new IllegalArgumentException("The period descritpion is malformed");
        }
        return time;
    }

    /**
     * Convert a JulianDate to Date
     */
    public static Date JulianToDate(JulianDate jdt) {
        if (jdt == null) {
            return null;
        }
        Date response = null;

        int JGREG = 15 + 31 * (10 + 12 * 1582);
        int jalpha, ja, jb, jc, jd, je, year, month, day;
        ja = (int) jdt.getCoordinateValue().intValue();
        if (ja >= JGREG) {
            jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
            ja = ja + 1 + jalpha - jalpha / 4;
        }

        jb = ja + 1524;
        jc = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
        jd = 365 * jc + jc / 4;
        je = (int) ((jb - jd) / 30.6001);
        day = jb - jd - (int) (30.6001 * je);
        month = je - 1;
        if (month > 12) {
            month = month - 12;
        }
        year = jc - 4715;
        if (month > 2) {
            year--;
        }
        if (year <= 0) {
            year--;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        response = cal.getTime();
        return response;
    }

    /**
     * Convert a CalendarDate object to java.util.Date.
     * @param calDate
     * @return
     */
    public static Date calendarDateToDate(CalendarDate calDate) {
        if (calDate == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        DefaultCalendarDate caldate = (DefaultCalendarDate) calDate;
        if (caldate != null) {
            int[] cal = calDate.getCalendarDate();
            int year = 0;
            int month = 0;
            int day = 0;
            if (cal.length > 3) {
                throw new IllegalArgumentException("The CalendarDate integer array is malformed ! see ISO 8601 format.");
            } else {
                year = cal[0];
                if (cal.length > 0) {
                    month = cal[1];
                }
                if (cal.length > 1) {
                    day = cal[2];
                }
                calendar.set(year, month, day);
                return calendar.getTime();
            }
        }
        return null;
    }

    /**
     * Convert a DateAndTime object to Date.
     * @param dateAndTime
     * @return
     */
    public static Date dateAndTimeToDate(DateAndTime dateAndTime) {
        if (dateAndTime == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        DefaultDateAndTime dateTime = (DefaultDateAndTime) dateAndTime;
        if (dateTime != null) {
            int[] cal = dateTime.getCalendarDate();
            int year = 0;
            int month = 0;
            int day = 0;
            if (cal.length > 3) {
                throw new IllegalArgumentException("The CalendarDate integer array is malformed ! see ISO 8601 format.");
            } else {
                year = cal[0];
                if (cal.length > 0) {
                    month = cal[1];
                }
                if (cal.length > 1) {
                    day = cal[2];
                }
            }

            Number[] clock = dateTime.getClockTime();
            Number hour = 0;
            Number minute = 0;
            Number second = 0;
            if (clock.length > 3) {
                throw new IllegalArgumentException("The ClockTime Number array is malformed ! see ISO 8601 format.");
            } else {
                hour = clock[0];
                if (clock.length > 0) {
                    minute = clock[1];
                }
                if (clock.length > 1) {
                    second = clock[2];
                }
            }
            calendar.set(year, month, day, hour.intValue(), minute.intValue(), second.intValue());
            return calendar.getTime();
        }
        return null;
    }

    /**
     * Convert a TemporalCoordinate object to Date.
     * @param temporalCoord
     */
    public static Date temporalCoordToDate(TemporalCoordinate temporalCoord) {
        if (temporalCoord == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        DefaultTemporalCoordinate timeCoord = (DefaultTemporalCoordinate) temporalCoord;
        Number value = timeCoord.getCoordinateValue();
        if (timeCoord.getFrame() instanceof TemporalCoordinateSystem) {
            DefaultTemporalCoordinateSystem coordSystem = (DefaultTemporalCoordinateSystem) timeCoord.getFrame();
            Date origin = coordSystem.getOrigin();
            String interval = coordSystem.getInterval().toString();

            Long timeInMS = 0L;

            if (interval.equals("year")) {
                timeInMS = value.longValue() * yearMS;
            } else if (interval.equals("month")) {
                timeInMS = value.longValue() * monthMS;
            } else if (interval.equals("week")) {
                timeInMS = value.longValue() * weekMS;
            } else if (interval.equals("day")) {
                timeInMS = value.longValue() * dayMS;
            } else if (interval.equals("hour")) {
                timeInMS = value.longValue() * hourMS;
            } else if (interval.equals("minute")) {
                timeInMS = value.longValue() * minMS;
            } else if (interval.equals("second")) {
                timeInMS = value.longValue() * secondMS;
            } else {
                throw new IllegalArgumentException(" The interval of TemporalCoordinateSystem for this TemporalCoordinate object is unknown ! ");
            }
            timeInMS = timeInMS + origin.getTime();
            calendar.setTimeInMillis(timeInMS);
            return calendar.getTime();
        } else {
            throw new IllegalArgumentException("The frame of this TemporalCoordinate object must be an instance of TemporalCoordinateSystem");
        }
    }
    
    public static Date ordinalToDate(OrdinalPosition ordinalPosition) {
        if (ordinalPosition == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        if (ordinalPosition.getOrdinalPosition() != null) {
            Date beginEra = ordinalPosition.getOrdinalPosition().getBeginning();
            Date endEra = ordinalPosition.getOrdinalPosition().getEnd();
            Long middle =((endEra.getTime() - beginEra.getTime()) / 2) + beginEra.getTime();
            calendar.setTimeInMillis(middle);
            return calendar.getTime();
        }
        else return null;
    }
}
