/*
 *    GeoTools - The Open Source Java GIS Tookit
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
 */
package org.geotools.metadata.iso;

import java.io.Serializable;

import java.util.Collection;
import org.geotools.metadata.MetadataStandard;
import org.geotools.metadata.ModifiableMetadata;
import org.geotools.metadata.InvalidMetadataException;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * A superclass for implementing ISO 19115 metadata interfaces. Subclasses
 * must implement at least one of the ISO MetaData interface provided by
 * <A HREF="http://geoapi.sourceforge.net">GeoAPI</A>.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class MetadataEntity extends ModifiableMetadata implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5730550742604669102L;

    /**
     * If a XML marshalling with JAXB is under progress, value {@link Boolean#TRUE}. Otherwise
     * {@link Boolean#FALSE}. This implementation assumes that JAXB performs marshalling in the
     * same thread than the one that invoke the {@code beforeMarshal(...)} method.
     */
    private transient ThreadLocal<Boolean> xmlMarshalling;

    /**
     * Constructs an initially empty metadata entity.
     */
    protected MetadataEntity() {
        super();
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     * The {@code source} metadata must implements the same metadata interface than this class.
     *
     * @param  source The metadata to copy values from.
     * @throws ClassCastException if the specified metadata don't implements the expected
     *         metadata interface.
     *
     * @since 2.4
     */
    protected MetadataEntity(final Object source) throws ClassCastException {
        super(source);
    }

    /**
     * Returns the metadata standard implemented by subclasses,
     * which is {@linkplain MetadataStandard#ISO_19115 ISO 19115}.
     *
     * @since 2.4
     */
    public MetadataStandard getStandard() {
        return MetadataStandard.ISO_19115;
    }

    /**
     * Makes sure that an argument is non-null. This is used for checking if
     * a mandatory attribute is presents.
     *
     * @param  name   Argument name.
     * @param  object User argument.
     * @throws InvalidMetadataException if {@code object} is null.
     *
     * @since 2.4
     */
    protected static void ensureNonNull(final String name, final Object object)
            throws InvalidMetadataException
    {
        if (object == null) {
            throw new InvalidMetadataException(Errors.format(ErrorKeys.NULL_ATTRIBUTE_$1, name));
        }
    }

    /**
     * Invoked with value {@code true} if a XML marshalling is begining,
     * or {@code false} if XML marshalling ended.
     *
     * @since 2.5
     *
     * @todo Current implementation will not work if more than one thread are marshalling in
     *       same time. It may also leave the object in an unstable state if the marshalling
     *       failed with an exception. We need to find a better mechanism.
     */
    protected final synchronized void xmlMarshalling(final boolean marshalling) {
        if (xmlMarshalling == null) {
            if (!marshalling) {
                return;
            }
            xmlMarshalling = new ThreadLocal<Boolean>();
        }
        xmlMarshalling.set(Boolean.valueOf(marshalling));
    }

    /**
     * If a XML marshalling is under progress and the given collection is empty, returns
     * {@code null}. Otherwise returns the collection unchanged. This method is invoked
     * by implementation having optional elements to ommit when empty.
     *
     * @param  elements The collection to return.
     * @return The given collection, or {@code null} if the collection is empty and a marshalling
     *         is under progress.
     *
     * @since 2.5
     */
    protected final <E> Collection<E> xmlOptional(final Collection<E> elements) {
        assert Thread.holdsLock(this);
        if (elements != null && elements.isEmpty()) {
            final ThreadLocal<Boolean> xmlMarshalling = this.xmlMarshalling;
            if (xmlMarshalling != null) {
                final Boolean isMarshalling = xmlMarshalling.get();
                if (Boolean.TRUE.equals(isMarshalling)) {
                    return null;
                }
            }
        }
        return elements;
    }
}
