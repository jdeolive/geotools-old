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
package org.geotools.gui.widget;

import java.awt.event.WindowListener;


/**
 * A frame which can contain other widgets.
 *
 * @author Cameron Shorter
 * @version $Id: FrameWidget.java,v 1.4 2003/08/20 21:37:58 cholmesny Exp $
 */
public interface FrameWidget extends Widget {
    /**
     * Sets the title for this frame to the specified string.
     *
     * @param title the title to be displayed in the frame's border. A
     *        <code>null</code> value is treated as an empty string, "".
     *
     * @see #getTitle
     */
    void setTitle(String title);

    /**
     * Set up a BorderLayout.
     *
     * @task TODO Allow setting of other Layout types.
     */
    void setBorderLayout();

    /**
     * Add a widget to be displayed inside this frame.
     *
     * @param widget to be displayed.
     *
     * @task REVISIT We should be able to pass in Widget instead of PanelWidget
     *       and then walk up the class hierarchy to determine which type of
     *       Widget it is.  The type of Widget is required so that we can
     *       caste the Widget into the correct Java Component type.
     */
    void addPanelWidget(PanelWidget widget);

    /**
     * Pack the widgets within this frame.
     */
    void pack();

    /**
     * Adds the specified window listener to receive window events from this
     * window. If l is null, no exception is thrown and no action is
     * performed.
     *
     * @param l the window listener
     *
     * @see #removeWindowListener
     * @see #getWindowListeners
     */
    void addWindowListener(WindowListener l);

    /**
     * Makes the Window visible. If the Window and/or its owner are not yet
     * displayable, both are made displayable.  The  Window will be validated
     * prior to being made visible.   If the Window is already visible, this
     * will bring the Window  to the front.
     *
     * @see Component#isDisplayable
     * @see #toFront
     * @see Component#setVisible
     */
    void show();
}
