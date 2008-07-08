/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.metadata.iso.extent;

import javax.measure.unit.Unit;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.datum.VerticalDatum;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Vertical domain of dataset.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "minimumValue", "maximumValue"
})
@XmlRootElement(name = "EX_VerticalExtent")
public class VerticalExtentImpl extends MetadataEntity implements VerticalExtent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3214554246909844079L;

    /**
     * The lowest vertical extent contained in the dataset.
     */
    private Double minimumValue;

    /**
     * The highest vertical extent contained in the dataset.
     */
    private Double maximumValue;

    /**
     * Provides information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured. The CRS
     * identification includes unit of measure.
     */
    private VerticalCRS verticalCRS;

    /**
     * Constructs an initially empty vertical extent.
     */
    public VerticalExtentImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public VerticalExtentImpl(final VerticalExtent source) {
        super(source);
    }

    /**
     * Creates a vertical extent initialized to the specified values.
     *
     * @since 2.4
     */
    public VerticalExtentImpl(final Double minimumValue,
                              final Double maximumValue,
                              final VerticalCRS verticalCRS)
    {
        setMinimumValue(minimumValue);
        setMaximumValue(maximumValue);
        setVerticalCRS (verticalCRS );
    }

    /**
     * Creates a vertical extent initialized to the specified values.
     *
     * @deprecated Use {@link #VerticalExtentImpl(Double,Double,VerticalCRS)} instead.
     */
    public VerticalExtentImpl(final double minimumValue,
                              final double maximumValue,
                              final Unit   unit,
                              final VerticalDatum verticalDatum)
    {
        setMinimumValue (minimumValue);
        setMaximumValue (maximumValue);
        setUnit         (unit);
        setVerticalDatum(verticalDatum);
    }

    /**
     * Returns the lowest vertical extent contained in the dataset.
     */
    @XmlElement(name = "minimumValue", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Set the lowest vertical extent contained in the dataset.
     */
    public synchronized void setMinimumValue(final Double newValue) {
        checkWritePermission();
        minimumValue = newValue;
    }

    /**
     * Returns the highest vertical extent contained in the dataset.
     */
    @XmlElement(name = "maximumValue", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Set the highest vertical extent contained in the dataset.
     */
    public synchronized void setMaximumValue(final Double newValue) {
        checkWritePermission();
        maximumValue = newValue;
    }

    /**
     * Provides information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured. The CRS
     * identification includes unit of measure.
     *
     * @since 2.4
     */
    // Need to annotate the referencing module of geotools before here.
    //@XmlElement(name = "minimumValue", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public VerticalCRS getVerticalCRS() {
        return verticalCRS;
    }

    /**
     * Set the information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured.
     *
     * @since 2.4
     */
    public synchronized void setVerticalCRS(final VerticalCRS newValue) {
        checkWritePermission();
        verticalCRS = newValue;
    }

    /**
     * Returns the vertical units used for vertical extent information.
     * Examples: metres, feet, millimetres, hectopascals.
     * <p>
     * This convenience method get the unit from the {@linkplain #getVerticalCRS vertical CRS},
     * if any.
     *
     * @deprecated removed from ISO_19115:2003/Cor.1:2006
     */
    //@XmlElement(name = "unitOfMeasure", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public Unit getUnit()  {
        return (verticalCRS != null) ? verticalCRS.getCoordinateSystem().getAxis(0).getUnit() : null;
    }

    /**
     * Set the vertical units used for vertical extent information.
     * Examples: metres, feet, millimetres, hectopascals.
     *
     * @deprecated Use {@link #setVerticalCRS} instead.
     */
    public void setUnit(final Unit newValue) {
        throw new UnsupportedOperationException("Use setVerticalCRS instead.");
    }

    /**
     * Provides information about the origin from which the
     * maximum and minimum elevation values are measured.
     *
     * @deprecated Use {@link #getVerticalCRS} instead.
     */
    public VerticalDatum getVerticalDatum()  {
        return (verticalCRS != null) ? (VerticalDatum) verticalCRS.getDatum() : null;
    }

    /**
     * Set information about the origin from which the
     * maximum and minimum elevation values are measured.
     *
     * @deprecated Use {@link #setVerticalCRS} instead.
     */
    public void setVerticalDatum(final VerticalDatum newValue) {
        throw new UnsupportedOperationException("Use setVerticalCRS instead.");
    }
}
