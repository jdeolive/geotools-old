package org.geotools.renderer3d.provider.texture.impl;

import org.geotools.renderer3d.utils.BoundingRectangle;

import java.awt.image.BufferedImage;

/**
 * Something that provides ground texture data for requested areas.
 * The textures may be loaded in the background in a separate thread.
 *
 * @author Hans Häggström
 */
public interface TextureProvider
{

    /**
     * Start creating or loading a texture of the specified width and height for the specified world area.
     * Provide the texture to the specified listener when ready.
     *
     * @param area            the world area to create the texture for. TODO: What units are the coordinates given in?
     * @param buffer          the image to render the texture to.
     * @param textureListener a listener that should be called back when the texture is ready.
     */
    void requestTexture( BoundingRectangle area, BufferedImage buffer, TextureListener textureListener );

    /**
     * Cancels the first found request with the specified listener.
     * The request is canceled even if it is already being rendered.
     *
     * @param textureListener the listener to cancel a texture request for.
     */
    void cancelRequest( final TextureListener textureListener );
}
