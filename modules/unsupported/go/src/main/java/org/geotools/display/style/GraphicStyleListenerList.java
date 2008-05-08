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

// OpenGIS dependencies
import java.util.Map;
import java.util.LinkedHashMap;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// OpenGIS dependencies
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.event.GraphicStyleEvent;
import org.opengis.go.display.style.event.GraphicStyleListener;

// Geotools dependencies
import org.geotools.util.logging.Logging;
import org.geotools.resources.XArray;


/**
 * A list of {@link GraphicStyleListener}s.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GraphicStyleListenerList implements PropertyChangeListener {
    /**
     * The owner of this list.
     */
    private final GraphicStyle source;

    /**
     * The list of listeners.
     */
    private GraphicStyleListener[] listeners;

    /**
     * A list of changes underway.
     */
    private final Map/*<String,ValuePair>*/ changes = new LinkedHashMap/*<String,ValuePair>*/();

    /**
     * {@code >0} if the next property change events should be groupped into a single
     * graphic style event.
     */
    private int groupCount;

    /**
     * Creates a new list of graphic style listener.
     */
    public GraphicStyleListenerList(final GraphicStyle source) {
        this.source = source;
        listeners = new GraphicStyleListener[0];
    }

    /**
     * Creates a new list of graphic style listener initialized to the same values
     * than the specified one, except for the grouping status.
     */
    public GraphicStyleListenerList(final GraphicStyleListenerList clone) {
        this.source    = clone.source;
        this.listeners = clone.listeners; // Safe to share.
    }

    /**
     * Adds the specified listener to the list.
     * Returns {@code true} if the list was empty prior the addition of the specified listener.
     * <p>
     * <strong>Implementation note:</strong> a new array is created on every addition. This is
     * somewhat inefficient, but suffisient if listener addition are very rare compared to change
     * events. This is an easy way to avoid synchronization in the {@link #fire} method.
     */
    public boolean add(final GraphicStyleListener listener) {
        assert Thread.holdsLock(source);
        final int count = listeners.length;
        for (int i=count; --i>=0;) {
            if (listeners[i] == listener) {
                return false;
            }
        }
        listeners = (GraphicStyleListener[]) XArray.resize(listeners, count+1);
        listeners[count] = listener;
        return count == 0;
    }

    /**
     * Removes the specified listener from the list.
     * Returns {@code true} if the list is empty as a result of the listener removal.
     * <p>
     * <strong>Implementation note:</strong> a new array is created on every removal. This is
     * somewhat inefficient, but suffisient if listener removal are very rare compared to change
     * events. This is an easy way to avoid synchronization in the {@link #fire} method.
     */
    public boolean remove(final GraphicStyleListener listener) {
        assert Thread.holdsLock(source);
        for (int i=listeners.length; --i>=0;) {
            if (listeners[i] == listener) {
                listeners = (GraphicStyleListener[]) XArray.remove(listeners, i, 1);
                return listeners.length == 0;
            }
        }
        return false;
    }

    /**
     * Notifies all listeners that a change occured. This method should be invoked outside
     * synchronized block, if possible.
     */
    private static void fire(final GraphicStyleListener[] listeners,
                             final GraphicStyleEvent      event)
    {
        for (int i=0; i<listeners.length; i++) {
            try {
                listeners[i].styleChanged(event);
            } catch (RuntimeException exception) {
                Logging.unexpectedException(GraphicStyleListener.class, "styleChanged", exception);
                /*
                 * Continues to notify the other listeners, since they may be unrelated
                 * to the faulty one and we don't want to prevent other listeners to work.
                 */
            }
        }
    }

    /**
     * Tells that all subsequent {@linkplain PropertyChangeEvent property change events}
     * should be grouped into a single {@linkplain GraphicStyleEvent graphic style event}
     * until {@link #releaseEventLock} is invoked.
     */
    public void acquireEventLock() {
        synchronized (source) {
            ++groupCount;
        }
    }

    /**
     * Fires a single {@linkplain GraphicStyleEvent graphic style event} for all changes that
     * occured since the call to {@link #acquireEventLock}.
     */
    public void releaseEventLock() {
        final GraphicStyleEvent      event;
        final GraphicStyleListener[] listeners;
        synchronized (source) {
            if (groupCount == 0) {
                throw new IllegalStateException();
            }
            if (--groupCount != 0 || changes.isEmpty()) {
                return;
            }
            listeners = this.listeners; // Protect from changes.
            event = new DefaultGraphicStyleEvent(source, changes);
            changes.clear();
        }
        fire(listeners, event);
    }

    /**
     * Invoked when a property changed. This method add the modified property to an internal list,
     * and notifies all {@linkplain GraphicStyleListener graphic style listeners} at once when
     * {@link #releaseEventLock} is invoked.
     */
    public void propertyChange(final PropertyChangeEvent change) {
        final GraphicStyleEvent      event;
        final GraphicStyleListener[] listeners;
        synchronized (source) {
            final String name = change.getPropertyName();
            final ValuePair pair = new ValuePair(change);
            final ValuePair previous = (ValuePair) changes.put(name, pair);
            if (previous != null) {
                /*
                 * This property already changed previously (note that we didn't invoked Map.get
                 * prior to Map.put in order to avoid accessing the map twice; most of the time,
                 * 'previous' will be null). Restore the previous value pair, except if the latest
                 * change cancel the previous one. In this later case, we will discart completly
                 * the change.
                 */
                if (previous.concatenate(pair)) {
                    changes.remove(name);
                } else {
                    changes.put(name, previous);
                }
            }
            if (groupCount != 0) {
                return;
            }
            listeners = this.listeners; // Protect from changes.
            event = new DefaultGraphicStyleEvent(source, changes);
            changes.clear();
        }
        fire(listeners, event);
    }
}
