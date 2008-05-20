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
import java.io.File;
import java.io.IOException;
import javax.imageio.spi.ImageReaderSpi;

import javax.swing.JTree;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Base class for tests. The {@linkplain #sourceTiles source tiles} are 8 tiles from Nasa
 * Blue Marble. The {@linkplain #targetTiles target tiles} are a few thousands of smaller
 * tiles created from the source tiles by the {@linkplain #builder}. A {@linkplain #manager
 * tile manager} is created for those target tiles.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class TestBase {
    /**
     * Source tile size for BlueMarble mosaic.
     */
    protected static final int SOURCE_SIZE = 21600;

    /**
     * Target tile size for BlueMarble mosaic.
     */
    protected static final int TARGET_SIZE = 960;

    /**
     * The mosaic builder used for creating {@link #targetTiles}.
     */
    protected MosaicBuilder builder;

    /**
     * Tiles given as input to the {@linkplain #builder}.
     */
    protected Tile[] sourceTiles;

    /**
     * Tiles produces as output by the {@linkplain #builder}.
     */
    protected Tile[] targetTiles;

    /**
     * The tile manager for {@link #targetTiles}.
     */
    protected TileManager manager;

    /**
     * The tile manager factory to be given to the {@linkplain #builder}, or {@code null} for the
     * default one. Subclasses can override this method in order to test specific implementations
     * of {@link TileManager}.
     *
     * @return The tile manager factory to use.
     * @throws IOException If an I/O operation was required and failed.
     */
    protected TileManagerFactory getTileManagerFactory() throws IOException {
        return null;
    }

    /**
     * Initializes every fields declared in this {@link TestBase} class.
     *
     * @throws IOException If an I/O operation was required and failed.
     */
    @Before
    public final void initTileManager() throws IOException {
        assertTrue("Assertions should be enabled.", MosaicBuilder.class.desiredAssertionStatus());

        builder = new MosaicBuilder(getTileManagerFactory());
        assertNull("No initial provider expected.", builder.getTileReaderSpi());
        builder.setTileReaderSpi("png");
        final ImageReaderSpi spi = builder.getTileReaderSpi();
        assertNotNull("Provider should be defined.", spi);

        final File directory = new File("geodata"); // Dummy directory - will not be read.
        final int S = SOURCE_SIZE; // For making reading easier below.
        sourceTiles = new Tile[] {
            new Tile(spi, new File(directory, "A1.png"), 0, new Rectangle(0*S, 0, S, S)),
            new Tile(spi, new File(directory, "B1.png"), 0, new Rectangle(1*S, 0, S, S)),
            new Tile(spi, new File(directory, "C1.png"), 0, new Rectangle(2*S, 0, S, S)),
            new Tile(spi, new File(directory, "D1.png"), 0, new Rectangle(3*S, 0, S, S)),
            new Tile(spi, new File(directory, "A2.png"), 0, new Rectangle(0*S, S, S, S)),
            new Tile(spi, new File(directory, "B2.png"), 0, new Rectangle(1*S, S, S, S)),
            new Tile(spi, new File(directory, "C2.png"), 0, new Rectangle(2*S, S, S, S)),
            new Tile(spi, new File(directory, "D2.png"), 0, new Rectangle(3*S, S, S, S))
        };
        builder.setTileDirectory(new File("S960")); // Dummy directory - will not be written.
        builder.setTileSize(new Dimension(TARGET_SIZE, TARGET_SIZE));
        manager = builder.createTileManager(sourceTiles, 0, false);
        targetTiles = manager.getTiles().toArray(new Tile[manager.getTiles().size()]);
    }

    /**
     * Shows the given tree in a Swing widget. This is used for debugging purpose only.
     */
    final void show(final javax.swing.tree.TreeNode root) {
        final Thread thread = Thread.currentThread();
        final JFrame frame = new JFrame("TreeNode");
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                thread.interrupt();
            }
        });
        frame.add(new JScrollPane(new JTree(root)));
        frame.pack();
        frame.setVisible(true);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // Go back to work.
        }
        frame.dispose();
    }
}
