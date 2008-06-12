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
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.opengis.annotation.UML;
import org.opengis.util.CodeList;


/**
 * An adapter for {@link CodeList}, in order to implement the ISO-19139 standard.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public abstract class CodeListAdapter<ValueType extends CodeListAdapter, BoundType extends CodeList>
        extends XmlAdapter<ValueType,BoundType>
{
    /**
     * A proxy form of the {@link CodeList}.
     */
    protected CodeListProxy proxy;

    /**
     * Empty constructor for JAXB only.
     */
    protected CodeListAdapter() {
    }

    /**
     * Generate a wrapper around the {@link CodeList}, in order to handle the format specified
     * in ISO-19139.
     *
     * @param proxy The proxy version of {@link CodeList}, designed to be marshalled.
     */
    protected CodeListAdapter(final CodeListProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Forces the initialisation of the given code list class, since some
     * calls to {@link CodeList#valueOf} are done whereas the constructor
     * has not already been called.
     */
    protected static void ensureClassLoaded(final Class<? extends CodeList> type) {
        try {
            Class.forName(type.getName(), true, type.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex); // Should never happen.
        }
    }

    /**
     * Wraps the proxy value into an adapter.
     *
     * @param proxy The proxy version of {@link CodeList}, designed to be marshalled.
     * @return The adapter that surrounds the proxy values.
     */
    protected abstract ValueType wrap(final CodeListProxy proxy);

    /**
     * Returns the code list class.
     */
    protected abstract Class<BoundType> getCodeListClass();

    /**
     * Does the link between the adapter value red from an XML stream and the
     * object which will contains this value. JAXB calls automatically this method at
     * unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return An {@link InternationalString} which represents the metadata value.
     */
    public final BoundType unmarshal(final ValueType value) {
        if (value == null) {
            return null;
        }
        return CodeList.valueOf(getCodeListClass(), value.proxy.getCodeListValue());
    }

    /**
     * Does the link between the code list and the way they will be marshalled into an
     * XML file or stream (adapter). JAXB calls automatically this method at marshalling-time.
     *
     * @param value The string value.
     * @return The adapter for this string.
     */
    public final ValueType marshal(final BoundType value) throws NoSuchFieldException {
        if (value == null) {
            return null;
        }
        final CodeListProxy codeList = new CodeListProxy(value);
        final String identifier = value.getClass().getField(value.name()).getAnnotation(UML.class).identifier();
        codeList.setCodeListValue(identifier);
        return wrap(codeList);
    }
}
