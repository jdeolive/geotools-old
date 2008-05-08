/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.metadata.iso.content;

import javax.units.Unit;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.content.Band;


/**
 * Range of wavelengths in the electromagnetic spectrum.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(name = "MD_Band", propOrder={
    "maxValue", "minValue", "peakResponse", "bitsPerValue", "toneGradation", "scaleFactor",
    "offset"
})
@XmlRootElement(name = "MD_Band")
public class BandImpl extends RangeDimensionImpl implements Band {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2302918545469034653L;

    /**
     * Longest wavelength that the sensor is capable of collecting within a designated band.
     */
    private Double maxValue;

    /**
     * Shortest wavelength that the sensor is capable of collecting within a designated band.
     */
    private Double minValue;

    /**
     * Units in which sensor wavelengths are expressed. Should be non-null if
     * {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    private Unit units;

    /**
     * Wavelength at which the response is the highest.
     * {@code null} if unspecified.
     */
    private Double peakResponse;

    /**
     * Maximum number of significant bits in the uncompressed representation for the value
     * in each band of each pixel.
     * {@code null} if unspecified.
     */
    private Integer bitsPerValue;

    /**
     * Number of discrete numerical values in the grid data.
     * {@code null} if unspecified.
     */
    private Integer toneGradation;

    /**
     * Scale factor which has been applied to the cell value.
     * {@code null} if unspecified.
     */
    private Double scaleFactor;

    /**
     * The physical value corresponding to a cell value of zero.
     * {@code null} if unspecified.
     */
    private Double offset;

    /**
     * Constructs an initially empty band.
     */
    public BandImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public BandImpl(final Band source) {
        super(source);
    }

    /**
     * Returns the longest wavelength that the sensor is capable of collecting within
     * a designated band. Returns {@code null} if unspecified.
     */
    @XmlElement(name = "maxValue", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * Set the longest wavelength that the sensor is capable of collecting within a
     * designated band. Returns {@code null} if unspecified.
     */
    public synchronized void setMaxValue(final Double newValue) {
        checkWritePermission();
        maxValue = newValue;
    }

    /**
     * Returns the shortest wavelength that the sensor is capable of collecting
     * within a designated band.
     */
    @XmlElement(name = "minValue", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getMinValue() {
        return minValue;
    }

    /**
     * Set the shortest wavelength that the sensor is capable of collecting within
     * a designated band.
     */
    public synchronized void setMinValue(final Double newValue) {
        checkWritePermission();
        minValue = newValue;
    }

    /**
     * Returns the units in which sensor wavelengths are expressed. Should be non-null
     * if {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    //@XmlElement(name = "units", required = false)
    public Unit getUnits() {
        return units;
    }

    /**
     * Set the units in which sensor wavelengths are expressed. Should be non-null if
     * {@linkplain #getMinValue min value} or {@linkplain #getMaxValue max value}
     * are provided.
     */
    public synchronized void setUnits(final Unit newValue) {
        checkWritePermission();
        units = newValue;
    }

    /**
     * Returns the wavelength at which the response is the highest.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "peakResponse", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getPeakResponse() {
        return peakResponse;
    }

    /**
     * Set the wavelength at which the response is the highest.
     */
    public synchronized void setPeakResponse(final Double newValue) {
        checkWritePermission();
        peakResponse = newValue;
    }

    /**
     * Returns the maximum number of significant bits in the uncompressed
     * representation for the value in each band of each pixel.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "bitsPerValue", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Integer getBitsPerValue() {
        return bitsPerValue;
    }

    /**
     * Set the maximum number of significant bits in the uncompressed representation
     * for the value in each band of each pixel.
     */
    public synchronized void setBitsPerValue(final Integer newValue) {
        checkWritePermission();
        bitsPerValue = newValue;
    }

    /**
     * Returns the number of discrete numerical values in the grid data.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "toneGradation", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Integer getToneGradation() {
        return toneGradation;
    }

    /**
     * Set the number of discrete numerical values in the grid data.
     */
    public synchronized void setToneGradation(final Integer newValue) {
        checkWritePermission();
        toneGradation = newValue;
    }

    /**
     * Returns the scale factor which has been applied to the cell value.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "scaleFactor", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Set the scale factor which has been applied to the cell value.
     */
    public synchronized void setScaleFactor(final Double newValue) {
        checkWritePermission();
        scaleFactor = newValue;
    }

    /**
     * Returns the physical value corresponding to a cell value of zero.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "offset", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getOffset() {
        return offset;
    }

    /**
     * Set the physical value corresponding to a cell value of zero.
     */
    public synchronized void setOffset(final Double newValue) {
        checkWritePermission();
        offset = newValue;
    }
}
