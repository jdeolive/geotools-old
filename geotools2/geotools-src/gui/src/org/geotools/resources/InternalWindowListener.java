/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.resources;

// Events
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

// Components
import java.awt.Frame;
import javax.swing.JInternalFrame;


/**
 * Wrap a {@link WindowListener} into an {@link InternalFrameListener}. This is used
 * by {@link SwingUtilities} in order to have the same methods working seemless on both
 * {@link Frame} and {@link JInternalFrame}.
 *
 * @version $Id: InternalWindowListener.java,v 1.2 2003/05/13 11:01:39 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class InternalWindowListener implements InternalFrameListener {
    /**
     * The underlying {@link WindowListener}.
     */
    private final WindowListener listener;

    /**
     * Wrap the specified {@link WindowListener} into an {@link InternalFrameListener}.
     * If the specified object is already an {@link InternalFrameListener}, then it is
     * returned as-is.
     */
    public static InternalFrameListener wrap(final WindowListener listener) {
        if (listener == null) {
            return null;
        }
        if (listener instanceof InternalFrameListener) {
            return (InternalFrameListener) listener;
        }
        return new InternalWindowListener(listener);
    }

    /**
     * Construct a new {@link InternalFrameListener}
     * wrapping the specified {@link WindowListener}.
     */
    private InternalWindowListener(final WindowListener listener) {
        this.listener = listener;
    }

    /**
     * Invoked when a internal frame has been opened.
     */
    public void internalFrameOpened(InternalFrameEvent event) {
        listener.windowOpened(null);
    }

    /**
     * Invoked when an internal frame is in the process of being closed.
     * The close operation can be overridden at this point.
     */
    public void internalFrameClosing(InternalFrameEvent event) {
        listener.windowClosing(null);
    }

    /**
     * Invoked when an internal frame has been closed.
     */
    public void internalFrameClosed(InternalFrameEvent event) {
        listener.windowClosed(null);
    }

    /**
     * Invoked when an internal frame is iconified.
     */
    public void internalFrameIconified(InternalFrameEvent event) {
        listener.windowIconified(null);
    }

    /**
     * Invoked when an internal frame is de-iconified.
     */
    public void internalFrameDeiconified(InternalFrameEvent event) {
        listener.windowDeiconified(null);
    }

    /**
     * Invoked when an internal frame is activated.
     */
    public void internalFrameActivated(InternalFrameEvent event) {
        listener.windowActivated(null);
    }

    /**
     * Invoked when an internal frame is de-activated.
     */
    public void internalFrameDeactivated(InternalFrameEvent event) {
        listener.windowDeactivated(null);
    }
}
