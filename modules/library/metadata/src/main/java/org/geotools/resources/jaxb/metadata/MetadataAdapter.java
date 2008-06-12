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
package org.geotools.resources.jaxb.metadata;

import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * Do the link between an interface object and its implementation object. It waits for
 * two classes in parameters, which are the {@code adapter} class and the interface then.
 *
 * @see XmlAdapter
 * @source $URL$
 * @author Cédric Briançon
 */
public abstract class MetadataAdapter<ValueType extends MetadataAdapter<ValueType,BoundType>, BoundType>
        extends XmlAdapter<ValueType,BoundType>
{
    /**
     * The wrapped GeoAPI metadata interface.
     */
    protected BoundType metadata;

    /**
     * Empty constructor for JAXB only.
     */
    protected MetadataAdapter() {
    }

    /**
     * Builds an adapter for the wrapped GeoAPI interface given.
     *
     * @param metadata The interface to wrap.
     */
    protected MetadataAdapter(final BoundType metadata) {
        this.metadata = metadata;
    }

    /**
     * Creates a new instance of this class wrapping the given metadata.
     * This method is invoked by {@link #marshal} after making sure that
     * {@code value} is not null.
     */
    protected abstract ValueType wrap(final BoundType value);

    /**
     * Does the link between java object and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The bound type value, here the interface.
     * @return The adapter for this interface.
     */
    public final ValueType marshal(final BoundType value) {
        if (value == null) {
            return null;
        }
        return wrap(value);
    }

    /**
     * Does the link between metadata red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A java object which represents the metadata value.
     */
    public final BoundType unmarshal(final ValueType value) {
        if (value == null) {
            return null;
        }
        return value.metadata;
    }
}
