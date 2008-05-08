package org.geotools.renderer3d.navigationgestures;


import org.geotools.renderer3d.utils.CursorChanger;

import javax.swing.event.MouseInputListener;
import java.awt.Component;
import java.awt.event.MouseWheelListener;

/**
 * An interface for handling navigation gestures done to the 3D view.
 *
 * @author Hans Häggström
 */
public interface NavigationGesture
        extends MouseInputListener, MouseWheelListener
{
    /**
     * Initializes the navigation gesture.
     *
     * @param component      the component that the gesture should listen to.  Can be asked e.g. the size and position.
     *                       Might also be used to add rubber band graphics or similar on top of the component
     * @param cursorChanger  an interface that can be used to change the mouse cursor on the 3D canvas rendered to.
     * @param cameraAccessor Can be asked for the camera that the navigation gesture listener should modify when gestures happen.
     */
    void init( Component component,
               CursorChanger cursorChanger,
               CameraAccessor cameraAccessor );

    /**
     * De-initializes the navigation gesture.  The navigation gesture should stop listening to the component, and should
     * remove its cursor settings.
     */
    void deInit();
}
