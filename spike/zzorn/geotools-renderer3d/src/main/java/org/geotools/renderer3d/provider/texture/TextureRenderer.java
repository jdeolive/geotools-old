package org.geotools.renderer3d.provider.texture;

import org.geotools.renderer3d.utils.BoundingRectangle;

import java.awt.image.BufferedImage;

/**
 * An interface for something that can render a specified rectangular area of the map to a BufferedImage.
 * <p/>
 * Can block until ready (as normal), the calling code will be executed in a separate worker thread so that there
 * are no delays or freezes in the UI even if it takes time to fetch map rasters from a disk or similar.
 *
 * @author Hans Häggström
 */
public interface TextureRenderer
{

    /**
     * Renders a texture on the specified buffered image that depicts the specified world area.
     *
     * @param area   the world area to create the texture for. TODO: What units are the coordinates given in?
     * @param target the image to render the texture to.
     */
    void renderArea( BoundingRectangle area, BufferedImage target );
}
