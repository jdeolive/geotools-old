package org.geotools.renderer3d.navigationgestures;

import com.jme.renderer.Camera;

/**
 * An interface that allows accessing the camera for a 3D view.
 *
 * @author Hans Häggström
 */
public interface CameraAccessor
{
    /**
     * @return the camera for a 3D view.  Can be modified by calling camera modifying methods.
     */
    Camera getCamera();
}
