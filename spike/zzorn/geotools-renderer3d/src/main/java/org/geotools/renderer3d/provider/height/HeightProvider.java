package org.geotools.renderer3d.provider.height;

import org.geotools.renderer3d.utils.BoundingRectangle;

/**
 * Something that provides ground height data for requested areas.
 * The height data may be loaded in the background in a separate thread.
 *
 * @author Hans Häggström
 */
public interface HeightProvider
{
    /**
     * Start creating or loading the height data of the specified size for the specified world area.
     * Provide the height data to the specified listener when ready.
     *
     * @param area            the world area to create the texture for. TODO: What units are the coordinates given in?
     * @param heightGridSizeX size of the grid along x axis.
     * @param heightGridSizeY size of the grid along y axis.
     * @param heightListener  a listener that should be called back when the height data is ready.
     */
    void requestHeightData( BoundingRectangle area,
                            int heightGridSizeX,
                            int heightGridSizeY,
                            HeightListener heightListener );

}
