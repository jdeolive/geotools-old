package org.geotools.renderer3d.navigationgestures;

import com.jme.renderer.Camera;

import java.awt.event.MouseWheelEvent;

/**
 * A gesture for moving forward or back.
 * <p/>
 * Move camera with W,S,A,D or arrow keys or scroll wheel (forward and back and strafes to the sides,
 * acceleration is enabled (and a bit of inertia too for effect)).
 * With scroll wheel, a few scrolls gives a small thrust, many scrolls leaves the thrust on.
 * Scroll back to turn the thrust off again (and to thrust backwards).
 * <p/>
 * Also scale move amount by altitude.
 *
 * @author Hans Häggström
 */
public final class MoveGesture
        extends AbstractNavigationGesture
{

    //======================================================================
    // Private Constants

    private static final float DEFAULT_MOVE_SENSITIVITY = 10.0f;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    public MoveGesture()
    {
        super( DEFAULT_MOVE_SENSITIVITY );
    }

    //----------------------------------------------------------------------
    // MouseWheelListener Implementation

    public void mouseWheelMoved( final MouseWheelEvent e )
    {
        final Camera camera = getCamera();

        final float movementAmount = -e.getWheelRotation() * getSensitivity() * getAltitudeFactor();

        // TODO: Implement inertia, acceleration, locked movement, and keyboard input

        camera.getLocation().scaleAdd( movementAmount, camera.getDirection(), camera.getLocation() );
        camera.onFrameChange();
    }

}
