/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */

package org.geotools.vpf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * VPFDate.java Created: Tue Jan 28 20:50:51 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version
 */
public class VPFDate {
    protected SimpleDateFormat sdf = null;
    protected byte[] dateBin = null;

    /**
     * Creates a new VPFDate object.
     *
     * @param date DOCUMENT ME!
     */
    public VPFDate(byte[] date) {
        dateBin = (byte[]) date.clone();
        initialize();
    }

    /**
     * Creates a new VPFDate object.
     *
     * @param date DOCUMENT ME!
     */
    public VPFDate(String date) {
        dateBin = new byte[date.length()];

        for (int i = 0; i < date.length(); i++) {
            dateBin[i] = (byte) date.charAt(i);
        }

        initialize();
    }

    protected void initialize() {
        for (int i = 0; i < dateBin.length; i++) {
            if ((char) dateBin[i] == ' ') {
                dateBin[i] = (byte) '0';
            }
        }

        sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        StringBuffer sb = new StringBuffer();

        for (int i = 15; i < dateBin.length; i++) {
            char cr = (char) dateBin[i];

            if (i == 18) {
                sb.append(':');
            }

            sb.append(cr);
        }

        sdf.setTimeZone(TimeZone.getTimeZone(sb.toString()));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(dateBin.length);

        for (int i = 0; i < dateBin.length; i++) {
            sb.append((char) dateBin[i]);
        }

        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Date getDate() {
        try {
            return sdf.parse(toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // end of try-catch
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Calendar getCalendar() {
        try {
            sdf.parse(toString());

            return sdf.getCalendar();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // end of try-catch
        return null;
    }
}


// VPFDate
