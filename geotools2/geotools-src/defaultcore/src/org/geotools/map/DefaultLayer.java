/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.map;

import org.geotools.feature.FeatureCollection;

// Geotools dependencies
import org.geotools.map.event.LayerEvent;
import org.geotools.map.event.LayerListener;
import org.geotools.resources.Utilities;
import org.geotools.styling.Style;

// J2SE dependencies
import javax.swing.event.EventListenerList;


/**
 * A default implementation for {@link Layer}.
 *
 * @author Cameron Shorter
 * @author Martin Desruisseaux
 * @version $Id: DefaultLayer.java,v 1.6 2003/08/20 20:51:16 cholmesny Exp $
 *
 * @task REVISIT: This class maybe should contain CoordinateSystem, which could
 *       either be set externally, or derived from one of its features.
 */
public class DefaultLayer implements Layer {
    /**
     * The features collection for this layer.
     *
     * @see #getFeatures
     */
    private FeatureCollection features;

    /**
     * Specify the style for this layer.
     *
     * @see #getStyle
     */
    private Style style;

    /**
     * The title of this layer for use in Legend and similar. May be
     * <code>null</code> if not set.
     *
     * @see #getTitle
     * @see #setTitle
     */
    private String title;

    /**
     * Specify whether this layer is visible or not. Defaults to
     * <code>true</code> on initialisation.
     *
     * @see #isVisible
     * @see #setVisible
     */
    private boolean visible = true;

    /**
     * Classes to notify if the layer changes. Will be constructed only when
     * first needed.
     */
    private EventListenerList listenerList;

    /**
     * Creates a Layer.
     *
     * @param features The features for this layer.
     * @param style The style to use when rendering features associated with
     *        this layer.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected DefaultLayer(FeatureCollection features, Style style)
        throws IllegalArgumentException {
        if ((style == null) || (features == null)) {
            throw new IllegalArgumentException();
        } else {
            this.features = features;
            this.style = style;
        }
    }

    /**
     * {@inheritDoc}
     */
    public FeatureCollection getFeatures() {
        return features;
    }

    /**
     * {@inheritDoc}
     */
    public Style getStyle() {
        return style;
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        if (title == null) {
            return "";
        } else {
            return title;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setTitle(final String title) {
        if (!Utilities.equals(this.title, title)) {
            this.title = title;
            fireLayerChanged(LayerEvent.TITLE_CHANGED);
        }
    }

    /**
     * Determine whether this layer is visible on a map pane or whether the
     * layer is hidden. Visibility defaults to <code>true</code> on
     * initialisation.
     *
     * @return <code>true</code> if the layer is visible, or <code>false</code>
     *         if the layer is hidden.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Specify whether this layer is visible on a map pane or whether the layer
     * is hidden.
     *
     * @param visible Shown the layer visible if <code>true</code>.
     */
    public void setVisible(final boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            fireLayerChanged(LayerEvent.VISIBILITY_CHANGED);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #isVisible} instead.
     */
    public boolean getVisability() {
        return isVisible();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link #setVisible} instead.
     */
    public void setVisability(boolean visability) {
        setVisible(visability);
    }

    /**
     * {@inheritDoc}
     */
    public void addLayerListener(final LayerListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
            listenerList.add(LayerListener.class, listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeLayerListener(final LayerListener listener) {
        if (listenerList != null) {
            listenerList.remove(LayerListener.class, listener);

            if (listenerList.getListenerCount() == 0) {
                listenerList = null;
            }
        }
    }

    /**
     * Add interest in receiving an LayerChangedEvent.
     *
     * @param llce The object to send LayerChangedEvents.
     *
     * @deprecated Use {@link #addLayerListener} instead.
     */
    public void addLayerChangedListener(LayerListener llce) {
        addLayerListener(llce);
    }

    /**
     * Remove interest in receiving an LayerChangedEvent.
     *
     * @param llcl The object to stop sending LayerChangedEvents.
     *
     * @deprecated Use {@link #removeLayerListener} instead.
     */
    public void removeLayerChangedListener(LayerListener llcl) {
        removeLayerListener(llcl);
    }

    /**
     * Notify all listeners that have registered interest for notification on
     * {@linkplain LayerEvent layer change event}. If the reason is {@link
     * LayerEvent#VISIBILITY_CHANGED}, then one of
     * <code>LayerListener.</code>{@link LayerListener#layerShown layerShown}
     * or {@link LayerListener#layerHidden layerHidden} methods will be
     * invoked instead of {@link LayerListener#layerChanged layerChanged}.
     *
     * @param reason The reason for the change. Must be one of the constants
     *        defined in {@link LayerEvent}.
     */
    protected void fireLayerChanged(final int reason) {
        if (listenerList != null) {
            final int method;

            if (reason == LayerEvent.VISIBILITY_CHANGED) {
                method = visible ? 1 : 2;
            } else {
                method = 0;
            }

            // Guaranteed to return a non-null array
            Object[] listeners = listenerList.getListenerList();

            // Process the listeners last to first, notifying
            // those that are interested in this event
            LayerEvent event = null;

            for (int i = listeners.length; (i -= 2) >= 0;) {
                if (listeners[i] == LayerListener.class) {
                    if (event == null) {
                        event = new LayerEvent(this, reason);
                    }

                    final LayerListener listener = ((LayerListener) listeners[i
                        + 1]);

                    switch (method) {
                    case 0:
                        listener.layerChanged(event);

                        break;

                    case 1:
                        listener.layerShown(event);

                        break;

                    case 2:
                        listener.layerHidden(event);

                        break;

                    default:
                        throw new AssertionError(method); // Should not happen.
                    }
                }
            }
        }
    }

    /**
     * Return the title of this layer. If no title has been defined, then the
     * class name is returned.
     *
     * @return the title of this layer.
     */
    public String toString() {
        if (title == null) {
            return Utilities.getShortClassName(this);
        } else {
            return title;
        }
    }
}
