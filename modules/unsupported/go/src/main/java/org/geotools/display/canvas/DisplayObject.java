/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.geotools.display.canvas;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;  // For javadoc
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.geotools.util.logging.Logging;


/**
 * The base class for {@linkplain AbstractCanvas canvas} and {@linkplain AbstractGraphic graphic
 * primitives}. This base class provides support for {@linkplain PropertyChangeListener property
 * change listeners}, and some basic services particular to the Geotools implementation
 * like {@linkplain #getLogger logging}, <cite>etc.</cite>
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DisplayObject {
    /**
     * The logger for the GO implementation module.
     */
    private static final Logger LOGGER = Logging.getLogger("org.geotools.display");

    /**
     * Listeners to be notified about any changes in this canvas properties.
     */
    protected final PropertyChangeSupport propertyListeners;

    /**
     * Creates a new instance of display object.
     */
    protected DisplayObject() {
        this.propertyListeners = new PropertyChangeSupport(this);
    }

    /**
     * Returns a rendering hint. The default implementation always returns {@code null}.
     * The {@link AbstractCanvas} and {@link org.geotools.display.style.GraphicStyle2D}
     * subclasses override this method in order to performs real work.
     *
     * @param  key The hint key.
     * @return The hint value for the specified key, or {@code null} if none.
     *
     * @see #getImplHint
     */
    public Object getHint(final RenderingHints.Key key) {
        return null;
    }

    /**
     * Adds a rendering hint. The default implementation ignore the hint value and does nothing.
     * The {@link AbstractCanvas} and {@link org.geotools.display.style.GraphicStyle2D}
     * subclasses override this method in order to performs real work.
     *
     * @param key   The hint key.
     * @param value The hint value. A {@code null} value remove the hint.
     *
     * @see #setImplHint
     */
    public void setHint(RenderingHints.Key key, Object value) {
    }

    /**
     * Adds a property change listener to the listener list. The listener is registered
     * for all properties. For example, {@linkplain AbstractCanvas#add adding} or
     * {@linkplain AbstractCanvas#remove removing} graphics in a canvas may fire
     * {@value #GRAPHICS_PROPERTY} change events and, indirectly, some other side-effect
     * events like {@value #ENVELOPE_PROPERTY}.
     *
     * @param listener The property change listener to be added
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        synchronized (propertyListeners) {
            propertyListeners.addPropertyChangeListener(listener);
            listenersChanged();
        }
    }

    /**
     * Adds a property change listener for a specific property.
     * The listener will be invoked only when that specific property changes.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The property change listener to be added.
     */
    public void addPropertyChangeListener(final String propertyName,
                                          final PropertyChangeListener listener)
    {
        synchronized (propertyListeners) {
            propertyListeners.addPropertyChangeListener(propertyName, listener);
            listenersChanged();
        }
    }

    /**
     * Removes a property change listener from the listener list. This removes a listener
     * that was registered for all properties.
     *
     * @param listener The property change listener to be removed
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        synchronized (propertyListeners) {
            propertyListeners.removePropertyChangeListener(listener);
            listenersChanged();
        }
    }

    /**
     * Remove a property change listener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The property change listener to be removed.
     */
    public void removePropertyChangeListener(final String propertyName,
                                             final PropertyChangeListener listener)
    {
        synchronized (propertyListeners) {
            propertyListeners.removePropertyChangeListener(propertyName, listener);
            listenersChanged();
        }
    }

    /**
     * Invoked when a property change listener has been {@linkplain #addPropertyChangeListener
     * added} or {@linkplain #removePropertyChangeListener removed}. Some subclasses may be
     * interrested to know if there is any registered listener of a particular kind. Such
     * subclasses can override this method in order to perform their check only once.
     */
    void listenersChanged() {
    }

    /**
     * Returns the locale for this object. The default implementation returns the
     * {@linkplain Locale#getDefault system locale}.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns the logger for all messages to be logged by the Geotools implementation of GO-1.
     */
    protected Logger getLogger() {
        return LOGGER;
    }

    /**
     * Invoked when an unexpected exception occured. This exception may happen while a rendering
     * is in process, so this method should not popup any dialog box and returns fast. The default
     * implementation sends a record to the {@linkplain #getLogger() logger} with the
     * {@link Level#WARNING WARNING} level.
     *
     * @param  sourceClassName  The caller's class name, for logging purpose.
     * @param  sourceMethodName The caller's method name, for logging purpose.
     * @param  exception        The exception.
     */
    protected void handleException(final Class<?> sourceClassName,
                                   final String  sourceMethodName,
                                   final Exception exception)
    {
        Logging.unexpectedException(getLogger(),
                sourceClassName, sourceMethodName, exception);
    }

    /**
     * Clears all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. For example it may be invoked from {@link java.applet.Applet#stop()}. Note
     * that this method doesn't changes the renderer setting; it will just slow down the first
     * rendering after this method call.
     *
     * @see #dispose
     */
    public void clearCache() {
    }

    /**
     * Method that can be called when an object is no longer needed. Implementations may use
     * this method to release resources, if needed. Implementations may also implement this
     * method to return an object to an object pool. It is an error to reference a
     * {@link org.opengis.display.primitive.Graphic} or {@link Canvas} in any way after its
     * dispose method has been called.
     */
    public void dispose() {
        synchronized (propertyListeners) {
            final PropertyChangeListener[] list = propertyListeners.getPropertyChangeListeners();
            for (int i=list.length; --i>=0;) {
                propertyListeners.removePropertyChangeListener(list[i]);
            }
            listenersChanged();
        }
    }
}
