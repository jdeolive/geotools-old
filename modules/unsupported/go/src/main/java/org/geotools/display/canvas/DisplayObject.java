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
import java.util.logging.LogRecord;
import java.awt.RenderingHints;
import java.beans.PropertyChangeEvent;  // For javadoc
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.opengis.go.display.primitive.Graphic;  // For javadoc

import org.geotools.util.logging.Logging;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;


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
     * List of classes that provides rendering hints as public static fields.
     * This is used by {@link #toRenderingHintKey}.
     */
    private static final String[] HINT_CLASSES = {
        "java.awt.RenderingHints",
        "org.geotools.factory.Hints",
        "org.geotools.display.canvas.AbstractCanvas",
        "javax.media.jai.JAI"
    };

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getName graphic name} changed.
     */
    public static final String NAME_PROPERTY = "name";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractCanvas#getTitle canvas title} changed.
     */
    public static final String TITLE_PROPERTY = "title";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getObjectiveCRS objective CRS} changed.
     */
    public static final String OBJECTIVE_CRS_PROPERTY = "objectiveCRS";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getDisplayCRS display CRS} changed.
     */
    public static final String DISPLAY_CRS_PROPERTY = "displayCRS";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractCanvas#getGraphics set of graphics} in this canvas changed.
     */
    public static final String GRAPHICS_PROPERTY = "graphics";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getVisible graphic visibility} changed.
     */
    public static final String VISIBLE_PROPERTY = "visible";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getZOrderHint z order hint} changed.
     */
    public static final String Z_ORDER_HINT_PROPERTY = "zOrderHint";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event}
     * fired when the {@linkplain ReferencedCanvas#getEnvelope canvas envelope} or
     * {@linkplain ReferencedGraphic#getEnvelope graphic envelope} changed.
     */
    public static final String ENVELOPE_PROPERTY = "envelope";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas#getScale canvas scale} changed.
     */
    public static final String SCALE_PROPERTY = "scale";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas2D#getDisplayBounds display bounds} changed.
     */
    public static final String DISPLAY_BOUNDS_PROPERTY = "displayBounds";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getParent graphic parent} changed.
     */
    public static final String PARENT_PROPERTY = "parent";

    /**
     * Listeners to be notified about any changes in this canvas properties.
     */
    protected final PropertyChangeSupport listeners;

    /**
     * Creates a new instance of display object.
     */
    protected DisplayObject() {
        this.listeners = new PropertyChangeSupport(this);
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
    public Object getRenderingHint(final RenderingHints.Key key) {
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
    public void setRenderingHint(RenderingHints.Key key, Object value) {
    }

    /**
     * Returns the rendering hint associated with the hint name. The default implementation looks
     * for a rendering hint key of the given name in some known classes like {@link RenderingHints}
     * and {@link javax.media.jai.JAI}, and invokes {@link #getRenderingHint} with that key.
     *
     * @param  name the name of the hint.
     * @return The hint value for the specified key, or {@code null} if none.
     */
    public Object getImplHint(final String name) throws IllegalArgumentException {
        return getRenderingHint(toRenderingHintKey(name, "getImplHint"));
    }

    /**
     * Sets a rendering hint for implementation or platform specific rendering information.
     * The default implementation looks for a rendering hint key of the given name in some
     * known classes like {@link RenderingHints} and {@link javax.media.jai.JAI}, and invokes
     * {@link #setRenderingHint} with that key. For example the two following method calls are
     * close to equivalent:
     * <p>
     * <ol>
     *   <li><code>setRenderingHint({@linkplain javax.media.jai.JAI#KEY_TILE_CACHE}, value);</code></li>
     *   <li><code>setImplHint("KEY_TILE_CACHE", value);</code></li>
     * </ol>
     * <p>
     * The main differences are that approach 1 is more type-safe but will fails on a machine
     * without JAI installation, while approach 2 is not type-safe but will silently ignore
     * the hint on a machine without JAI installation. Likewise, a user can write for example
     * <code>setImplHint("FINEST_RESOLUTION", value)</code> for setting the
     * {@link AbstractCanvas#FINEST_RESOLUTION FINEST_RESOLUTION} hint without immediate
     * dependency to the {@link AbstractCanvas} Geotools implementation.
     *
     * @param name  the name of the hint.
     * @param value The hint value. A {@code null} value remove the hint.
     */
    public void setImplHint(final String name, final Object value) {
        final RenderingHints.Key key = toRenderingHintKey(name, "setImplHint");
        if (key != null) {
            setRenderingHint(key, value);
        } else {
            getLogger().fine(Loggings.getResources(getLocale()).getString(
                    LoggingKeys.HINT_IGNORED_$1, name));
        }
    }

    /**
     * Returns the rendering hint key for the specified name.
     *
     * @param  name       The key name.
     * @param  methodName The caller name, for logging purpose only.
     * @return A rendering hint key of the given name, or {@code null}
     *         if no key were found for the given name.
     */
    private RenderingHints.Key toRenderingHintKey(String name, final String methodName) {
        if (true) {
            /*
             * Converts the name in upper case, adding '_' as needed.
             * For example "someName" will be converted as "SOME_NAME".
             */
            final int length = name.length();
            final StringBuffer buffer = new StringBuffer(length);
            for (int i=0; i<length; i++) {
                char c = name.charAt(i);
                if (Character.isUpperCase(c)) {
                    if (i!=0 && Character.isLowerCase(name.charAt(i-1))) {
                        buffer.append('_');
                    }
                } else {
                    c = Character.toUpperCase(c);
                }
                buffer.append(c);
            }
            name = buffer.toString();
        }
        /*
         * Now searchs for the public static constants defined in some known classes.
         */
        for (int i=0; i<HINT_CLASSES.length; i++) {
            try {
                return (RenderingHints.Key) Class.forName(HINT_CLASSES[i]).getField(name).get(null);
            } catch (Exception e) {
                /*
                 * May be SecurityException, ClassNotFoundException, NoSuchFieldException,
                 * IllegalAccessException, NullPointerException, ClassCastException and more...
                 * We ignore all of them and just try the next class.
                 */
                final LogRecord record = new LogRecord(Level.FINEST, name);
                record.setSourceClassName(DisplayObject.class.getName());
                record.setSourceMethodName(methodName);
                record.setThrown(e);
                getLogger().log(record);
            }
        }
        return null;
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
        synchronized (listeners) {
            listeners.addPropertyChangeListener(listener);
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
        synchronized (listeners) {
            listeners.addPropertyChangeListener(propertyName, listener);
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
        synchronized (listeners) {
            listeners.removePropertyChangeListener(listener);
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
        synchronized (listeners) {
            listeners.removePropertyChangeListener(propertyName, listener);
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
    protected void clearCache() {
    }

    /**
     * Method that can be called when an object is no longer needed. Implementations may use
     * this method to release resources, if needed. Implementations may also implement this
     * method to return an object to an object pool. It is an error to reference a
     * {@link Graphic} or {@link Canvas} in any way after its dispose method has been called.
     */
    public void dispose() {
        synchronized (listeners) {
            final PropertyChangeListener[] list = listeners.getPropertyChangeListeners();
            for (int i=list.length; --i>=0;) {
                listeners.removePropertyChangeListener(list[i]);
            }
            listenersChanged();
        }
    }
}
