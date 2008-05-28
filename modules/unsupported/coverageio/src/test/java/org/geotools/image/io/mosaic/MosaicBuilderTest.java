/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
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
 */
package org.geotools.image.io.mosaic;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link MosaicBuilder}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class MosaicBuilderTest extends TestBase {
    /**
     * Tests with a set of files corresponding to a Blue Marble mosaic.
     *
     * @throws IOException If an I/O operation failed.
     * @throws ClassNotFoundException if a deserialization failed.
     */
    @Test
    public void testBlueMarble() throws IOException, ClassNotFoundException {
        assertTrue(MosaicBuilder.class.desiredAssertionStatus());
        builder.setSubsamplings((Dimension[]) null);
        builder.setTileSize(null);

        Rectangle bounds = new Rectangle(SOURCE_SIZE*4, SOURCE_SIZE*2);
        builder.setUntiledImageBounds(bounds);
        assertEquals(bounds, builder.getUntiledImageBounds());

        Dimension size = builder.getTileSize();
        assertEquals(480, size.width);
        assertEquals(480, size.height);
        builder.setTileSize(new Dimension(960,900));

        Dimension[] subsamplings = builder.getSubsamplings();
        int[] width  = new int[] {1,2,3,3,5,6,9,10,10,15,18,18,30,45,90};
        int[] height = new int[] {1,2,3,4,4,6,8, 8,12,16,16,24,24,48,90};
        for (int i=0; i<subsamplings.length; i++) {
            assertEquals("width["  + i + ']', width [i], subsamplings[i].width);
            assertEquals("height[" + i + ']', height[i], subsamplings[i].height);
        }

        builder.setTileSize(new Dimension(960,960));
        builder.setSubsamplings((Dimension[]) null); // For forcing new computation.
        subsamplings = builder.getSubsamplings();
        width  = new int[] {1,3,5,9,15,45,90};
        height = new int[] {1,3,5,9,15,45,90};
        for (int i=0; i<subsamplings.length; i++) {
            assertEquals("width["  + i + ']', width [i], subsamplings[i].width);
            assertEquals("height[" + i + ']', height[i], subsamplings[i].height);
        }

        TileManager tileManager = builder.createTileManager(sourceTiles);
        assertEquals(4733, tileManager.getTiles().size());
        final String asText = tileManager.toString();
        assertFalse(asText.trim().length() == 0);
        assertTrue("Expected tiles created as in setUp()", manager.equals(tileManager));
        // we don't use assertEquals because the message is too long to format in case of failure.

        // Tests serialization
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(tileManager);
        out.close();

        final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        final TileManager serialized = (TileManager) in.readObject();
        in.close();

        assertNotSame(tileManager, serialized);
        assertEquals(tileManager, serialized);
        assertEquals(tileManager.getImageReaderSpis(), serialized.getImageReaderSpis());
    }
}
