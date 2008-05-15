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
import java.awt.Color;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;     // For javadoc
import java.beans.PropertyChangeListener;  // For javadoc

// OpenGIS dependencies
import org.opengis.go.display.primitive.Graphic;  // For javadoc
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.event.GraphicStyleEvent;  // For javadoc
import org.opengis.go.display.style.event.GraphicStyleListener;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.display.canvas.DisplayObject;


/**
 * Base classe for the collection of drawing attributes that are applied to a {@link Graphic}.
 * Subclasses provide attributes for specifying SLD-based line symbolizer, polygon symbolizer,
 * point symbolizer, text symbolizer.  Attributes common to all types of geometry, related to
 * viewability, editability, and highlighting, are contained in {@code Graphic}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultGraphicStyle extends DisplayObject implements GraphicStyle {
    /**
     * Map of the implementation-specific hint identified by a rendering hint key.
     */
    private Hints hints;

    /**
     * List of the registered graphic style listeners.
     */
    private GraphicStyleListenerList graphicStyleListeners;

    /**
     * Creates a default instance of graphic style.
     */
    public DefaultGraphicStyle() {
        hints                 = new Hints();
        graphicStyleListeners = new GraphicStyleListenerList(this);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setRenderingHint(final RenderingHints.Key key, final Object value) {
        if (value != null) {
            hints.put(key, value);
        } else {
            hints.remove(key);
        }
    }

    /**
     * Registers the given object as a listener to receive events when the properties of this style
     * have changed. A GO-1 {@linkplain GraphicStyleListener graphic style listener} is similar to
     * a Java {@linkplain PropertyChangeListener property change listener}, except for the following
     * differences:
     *
     * <ul>
     *   <li><p>Property change listeners can be
     *       {@linkplain #addPropertyChangeListener(String,PropertyChangeListener) registered
     *       for a single property}, while graphic style listeners are always registered for
     *       all properties.</p></li>
     *   <li><p>A {@linkplain PropertyChangeEvent property change event} represents a change in
     *       a single property, while a {@linkplain GraphicStyleEvent graphic style event} can
     *       represents changes in many properties. See {@link #setPropertiesFrom} for an
     *       example.</p></li>
     * </ul>
     */
    public synchronized void addGraphicStyleListener(final GraphicStyleListener listener) {
        if (graphicStyleListeners.add(listener)) {
            propertyListeners.addPropertyChangeListener(graphicStyleListeners);
        }
    }

    /**
     * For a listener that was previously added using the {@link #addGraphicStyleListener
     * addGraphicStyleListener} method, de-registers it so that it will no longer receive
     * events when the properties of this style have changed.
     */
    public synchronized void removeGraphicStyleListener(final GraphicStyleListener listener) {
        if (graphicStyleListeners.remove(listener)) {
            propertyListeners.removePropertyChangeListener(graphicStyleListeners);
        }
    }

    /**
     * Tells that all subsequent {@linkplain PropertyChangeEvent property change events}
     * should be grouped into a single {@linkplain GraphicStyleEvent graphic style event}
     * until {@link #releaseEventLock} is invoked. This method is typically invoked in a
     * block like below:
     *
     * <blockquote><pre>
     * acquireEventLock();
     * try {
     *     // Performs a bunch of changes here.
     * } finally {
     *     releaseEventLock();
     * }
     * </pre></blockquote>
     */
    protected void acquireEventLock() {
        // Do not synhronize; synchronization is performed by GraphicStyleListenerList.
        graphicStyleListeners.acquireEventLock();
    }

    /**
     * Fires a single {@linkplain GraphicStyleEvent graphic style event} for all changes that
     * occured since the call to {@link #acquireEventLock}.
     */
    protected void releaseEventLock() {
        // Do not synhronize; synchronization is performed by GraphicStyleListenerList.
        graphicStyleListeners.releaseEventLock();
    }

    /**
     * Ensures that the color alpha channel has the specified opacity.
     */
    static Color fixAlphaChannel(Color color, final float opacity) {
        final int alpha = Math.max(0, Math.min(255, Math.round(256*opacity))) << 24;
        final int RGB   = color.getRGB();
        if ((RGB & 0xFF000000) != alpha) {
            color = new Color((RGB & 0x00FFFFFF) | alpha, true);
        }
        return color;
    }

    /**
     * Sets the properties of this {@code GraphicStyle} from the properties of the specified
     * {@code GraphicStyle}. This method does not copy the listeners neither the implementation
     * hints.
     * <p>
     * This method may fires many {@linkplain PropertyChangeEvent property change events}, one for
     * every property that changed. But it fires at most one {@linkplain GraphicStyleEvent graphic
     * style event} with the list of all modified properties as an array.
     * <p>
     * Subclasses should implement this method as below:
     *
     * <blockquote><pre>
     * public synchronized void setPropertiesFrom(GraphicStyle graphicStyle) {
     *     {@linkplain #acquireEventLock()};
     *     try {
     *         super.setPropertiesFrom(graphicStyle);
     *         if (graphicStyle instanceof MySymbolizer) {
     *             final MySymbolizer ms = (MySymbolizer) graphicStyle;
     *             setMyProperty(ms.getMyProperty());
     *             // etc...
     *         }
     *     } finally {
     *         {@linkplain #releaseEventLock()};
     *     }
     * }
     * </pre></blockquote>
     */
    public void setPropertiesFrom(final GraphicStyle graphicStyle) {
    }

    /**
     * Returns a shallow copy of this object. This means that all of the subordinate objects
     * referenced by this object will also be referenced by the result. These objects include
     * the values for {@linkplain #getImplHint implementation hints}, <cite>etc.</cite>
     */
    public GraphicStyle clone() {
        final DefaultGraphicStyle clone;
        try {
            clone = (DefaultGraphicStyle) super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should never happen since we are cloneable.
            throw new AssertionError(exception);
        }
        clone.hints = (Hints) hints.clone();
        clone.graphicStyleListeners = new GraphicStyleListenerList(graphicStyleListeners);
        return clone;
    }

    public Object getImplHint(String arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setImplHint(String arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
