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
package org.geotools.map.event;

// J2SE dependencies
import java.util.EventObject;

// Geotools dependencies
import org.geotools.map.Layer;  // For Javadoc


/**
 * Event fired when some {@linkplain Layer layer} property changes.
 *
 * @author Ian Turton
 * @author Martin Desruisseaux
 * @version $Id: LayerEvent.java,v 1.1 2003/08/18 16:32:31 desruisseaux Exp $
 *
 * @see Layer
 * @see LayerListener
 */
public class LayerEvent extends EventObject {
    /**
     * Flag set when the layer visibility changed. When a visiblity change is the reason for
     * an event, then <code>LayerListener.</code>{@link LayerListener#layerShown layerShown}
     * or {@link LayerListener#layerHidden layerHidden} methods are invoked rather than
     * {@link LayerListener#layerChanged layerChanged}.
     *
     * @see #getReason
     */
    public static final int VISIBILITY_CHANGED = 1;

    /**
     * Flag set when the layer title changed.
     *
     * @see #getReason
     */
    public static final int TITLE_CHANGED = 2;

    /**
     * The reason for the change.
     */
    private final int reason;

    /**
     * Creates a new instance of <code>LayerEvent</code> with the specified reason.
     *
     * @param  source The source of the event change.
     * @param  reason Why the event was fired.
     * @throws IllegalArgumentException If the <code>reason</code> is not a valid enum.
     */
    public LayerEvent(final Object source, final int reason) throws IllegalArgumentException {
        super(source);
        this.reason = reason;
        if (reason<=0 || reason>TITLE_CHANGED) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the reason why this event is fired. It is one of {@link #VISIBILITY_CHANGED}
     * or {@link #TITLE_CHANGED} constants.
     */
    public int getReason() {
        return reason;
    }
}
