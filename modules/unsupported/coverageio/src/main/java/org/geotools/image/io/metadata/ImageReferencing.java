/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.image.io.metadata;

import org.opengis.referencing.datum.Datum;                     // For javadoc
import org.opengis.referencing.datum.TemporalDatum;             // For javadoc
import org.opengis.referencing.cs.AxisDirection;                // For javadoc
import org.opengis.referencing.cs.CoordinateSystem;             // For javadoc
import org.opengis.referencing.cs.CoordinateSystemAxis;         // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem;   // For javadoc


/**
 * A {@code <CoordinateReferenceSystem>} element in
 * {@linkplain GeographicMetadataFormat geographic metadata format}, together with its
 * {@code <CoordinateSystem>} and {@code <Datum>} child elements.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Cédric Briançon
 */
public class ImageReferencing extends MetadataAccessor {
    /**
     * The {@code "rectifiedGridDomain/crs/datum"} node.
     */
    private final MetadataAccessor datum;

    /**
     * The {@code "rectifiedGridDomain/crs/cs"} node.
     */
    final ChildList<Axis> cs;

    /**
     * Creates a parser for a coordinate system. This constructor should not be invoked
     * directly; use {@link GeographicMetadata#getReferencing} instead.
     *
     * @param metadata The metadata node.
     */
    protected ImageReferencing(final GeographicMetadata metadata) {
        super(metadata, "rectifiedGridDomain/crs", null);
        datum = new MetadataAccessor(metadata, "rectifiedGridDomain/crs/datum",  null);
        cs    = new ChildList.Axes(metadata);
    }

    /**
     * Returns the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain CoordinateReferenceSystem coordinate reference system}.
     *
     * @see CoordinateReferenceSystem
     */
    public Identification getCoordinateReferenceSystem() {
        return new Identification(this);
    }

    /**
     * Sets the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain CoordinateReferenceSystem coordinate reference system}.
     *
     * @param name The coordinate reference system name, or {@code null} if unknown.
     * @param type The coordinate reference system type (usually
     *             {@value GeographicMetadataFormat#GEOGRAPHIC} or
     *             {@value GeographicMetadataFormat#PROJECTED}), or {@code null} if unknown.
     *
     * @see CoordinateReferenceSystem
     */
    public void setCoordinateReferenceSystem(final String name, final String type) {
        setAttributeAsString("name", name);
        setAttributeAsEnum  ("type", type, GeographicMetadataFormat.CRS_TYPES);
    }

    /**
     * Returns the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain CoordinateSystem coordinate system}.
     *
     * @see CoordinateSystem
     */
    public Identification getCoordinateSystem() {
        return new Identification(cs);
    }

    /**
     * Sets the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain CoordinateSystem coordinate system}.
     *
     * @param name The coordinate system name, or {@code null} if unknown.
     * @param type The coordinate system type (usually
     *             {@value GeographicMetadataFormat#ELLIPSOIDAL} or
     *             {@value GeographicMetadataFormat#CARTESIAN}), or {@code null} if unknown.
     *
     * @see CoordinateSystem
     */
    public void setCoordinateSystem(final String name, final String type) {
        cs.setAttributeAsString("name", name);
        cs.setAttributeAsEnum  ("type", type, GeographicMetadataFormat.CS_TYPES);
    }

    /**
     * Returns the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain Datum datum}.
     *
     * @see Datum
     */
    public Identification getDatum() {
        return new Identification(datum);
    }

    /**
     * Sets the {@linkplain Identification#name name} and {@linkplain Identification#type type}
     * of the {@linkplain Datum datum}.
     *
     * @param name The datum name, or {@code null} if unknown.
     *
     * @see Datum
     */
    public void setDatum(final String name) {
        datum.setAttributeAsString("name", name);
    }

    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return cs.childCount();
    }

    /**
     * Returns the axis at the specified index.
     *
     * @param  index the axis index, ranging from 0 inclusive to {@link #getDimension} exclusive.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public Axis getAxis(final int index) throws IndexOutOfBoundsException {
        return cs.getChild(index);
    }

    /**
     * Adds an {@linkplain CoordinateSystemAxis axis} to the
     * {@linkplain CoordinateSystem coordinate system}.
     *
     * @param name The axis name, or {@code null} if unknown.
     * @param direction The {@linkplain AxisDirection axis direction}
     *        (usually {@code "east"}, {@code "weast"}, {@code "north"}, {@code "south"},
     *        {@code "up"} or {@code "down"}), or {@code null} if unknown.
     * @param units The axis units symbol, or {@code null} if unknown.
     *
     * @see CoordinateSystemAxis
     * @see AxisDirection
     */
    public Axis addAxis(final String name, final String direction, final String units) {
        final Axis axis = cs.addChild();
        axis.setName(name);
        axis.setDirection(direction);
        axis.setUnits(units);
        return axis;
    }

    /**
     * Returns the {@linkplain CoordinateReferenceSystem coordinate reference system} in
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> format</A>, or {@code null} if none.
     */
    @Deprecated
    public String getWKT() {
        return getAttributeAsString("WKT");
    }

    /**
     * Sets the {@linkplain CoordinateReferenceSystem coordinate reference system} in
     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
     * Known Text</cite> format</A>.
     */
    @Deprecated
    public void setWKT(final String wkt) {
        setAttributeAsString("WKT", wkt);
    }
}
