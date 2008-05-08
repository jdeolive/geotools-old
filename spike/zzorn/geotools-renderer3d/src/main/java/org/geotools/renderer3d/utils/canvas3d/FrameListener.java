package org.geotools.renderer3d.utils.canvas3d;

/**
 * A listener that is called for each frame that is rendered.
 * The listener is called in the swing thread, so it can update the swing UI.
 *
 * @author Hans Häggström
 */
public interface FrameListener
{
    /**
     * Called each frame, in the swing thread context.
     *
     * @param secondsSinceLastFrame number of seconds since the last frame, or a negative value if this is the first frame.
     */
    void onFrame( double secondsSinceLastFrame );
}
