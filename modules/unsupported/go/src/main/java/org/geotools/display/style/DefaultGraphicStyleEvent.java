/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
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
package org.geotools.display.style;

// J2SE dependencies
import java.util.Map;
import java.util.Iterator;
import java.util.EventObject;

// OpenGIS dependencies
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.event.GraphicStyleEvent;


/**
 * Event that contains information about a modification to a {@link GraphicStyle} object.
 * Each event object contains an array of strings denoting which properties have changed.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DefaultGraphicStyleEvent extends EventObject implements GraphicStyleEvent {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 1585323076760907516L;

    /**
     * An array naming each of the properties that changed.
     */
    private final String[] properties;

    /**
     * An array that lists the previous values of properties that changed.
     */
    private final Object[] oldValues;

    /**
     * An array that lists the new values of properties that changed.
     */
    private final Object[] newValues;

    /**
     * Creates a new style change event. The content of the supplied collections are copied;
     * changes to those collections after this constructor call will not be reflected into
     * this event object.
     */
    public DefaultGraphicStyleEvent(final GraphicStyle source, final Map/*<String,ValuePair>*/ changes) {
        super(source);
        properties = new String[changes.size()];
        oldValues  = new Object[properties.length];
        newValues  = new Object[properties.length];
        int  count = 0;
        for (final Iterator it=changes.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry  = (Map.Entry) it.next();
            final ValuePair values = (ValuePair) entry.getValue();
            properties[count] = (String) entry.getKey();
            oldValues [count] = values.oldValue;
            newValues [count] = values.newValue;
            count++;
        }
    }

    /**
     * Returns the style whose property or properties changed.
     */
    public GraphicStyle getGraphicStyle() {
        return (GraphicStyle) getSource();
    }

    /**
     * Returns an array naming each of the properties that changed.
     */
    public String[] getProperties() {
        return properties;
    }

    /**
     * Returns an array that lists the previous values of properties that
     * changed.  The value at a given index in the array corresponds with the
     * property named at the same index in the array returned by getProperties.
     */
    public Object[] getOldValues() {
        return oldValues;
    }

    /**
     * Returns an array that lists the new values of properties that changed.
     * The value at a given index in the array corresponds with the property
     * named at the same index in the array returned by getProperties.
     */
    public Object[] getNewValues() {
        return newValues;
    }
}
