/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.citation;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Reference date and event used to describe it.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "date", "dateType"
})
@XmlRootElement(name = "CI_Date")
public class CitationDateImpl extends MetadataEntity implements CitationDate {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2884791484254008454L;

    /**
     * Reference date for the cited resource in millisecondes ellapsed sine January 1st, 1970,
     * or {@link Long#MIN_VALUE} if none.
     */
    private long date = Long.MIN_VALUE;

    /**
     * Event used for reference date.
     */
    private DateType dateType;

    /**
     * Constructs an initially empty citation date.
     */
    public CitationDateImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public CitationDateImpl(final CitationDate source) {
        super(source);
    }

    /**
     * Constructs a citation date initialized to the given date.
     */
    public CitationDateImpl(final Date date, final DateType dateType) {
        setDate    (date);
        setDateType(dateType);
    }

    /**
     * Returns the reference date for the cited resource.
     */
    @XmlElement(name = "date", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Date getDate() {
        return (date!=Long.MIN_VALUE) ? new Date(date) : null;
    }

    /**
     * Set the reference date for the cited resource.
     */
    public synchronized void setDate(final Date newValue) {
        checkWritePermission();
        date = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the event used for reference date.
     */
    @XmlElement(name = "dateType", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public DateType getDateType() {
        return dateType;
    }

    /**
     * Set the event used for reference date.
     */
    public synchronized void setDateType(final DateType newValue) {
        checkWritePermission();
        dateType = newValue;
    }
}
