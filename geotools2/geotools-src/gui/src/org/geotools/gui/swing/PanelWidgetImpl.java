package org.geotools.gui.swing;

/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 */

import javax.swing.JPanel;
import java.util.logging.Logger;
import org.geotools.gui.widget.PanelWidget;
import java.awt.event.MouseListener;

/**
 * This is the base widget class that all widgets inherit from.
 * @version $Id: PanelWidgetImpl.java,v 1.5 2003/02/25 11:13:13 camerons Exp $
 * @author Cameron Shorter
 * @deprecated Use JPanel instead.
 */

public class PanelWidgetImpl extends JPanel implements PanelWidget
{

    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.swing.PanelWidgetImpl");

    /**
     * Create a Widget.
     * A MapPane marshals the drawing of maps.
     * This should only be called from WidgetFactory.
     *
     */
    protected PanelWidgetImpl()
    {
    }

    // The following methods are common to all widgets however, since
    // java doesn't allow multiple inheritence, and interfaces are not
    // defined for Swing components, we need to include the same code
    // in all the widgets which wrap Swing base components.
    
    /**
     * Adds the specified mouse listener to receive mouse events from
     * this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #removeMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void addMouseListener(Object l) {
        super.addMouseListener((MouseListener)l);
    }

    /**
     * Removes the specified mouse listener so that it no longer
     * receives mouse events from this component. This method performs 
     * no function, nor does it throw an exception, if the listener 
     * specified by the argument was not previously added to this component.
     * If listener <code>l</code> is <code>null</code>,
     * no exception is thrown and no action is performed.
     *
     * @param    l   the mouse listener
     * @see      java.awt.event.MouseEvent
     * @see      java.awt.event.MouseListener
     * @see      #addMouseListener
     * @see      #getMouseListeners
     * @since    JDK1.1
     */
    public synchronized void removeMouseListener(Object l) {
        super.removeMouseListener((MouseListener)l);
    }

    /**
     * Returns the current width of this component.
     * @return the current width of this component
     */
    public int getWidth() {
        return super.getWidth();
    }
}
