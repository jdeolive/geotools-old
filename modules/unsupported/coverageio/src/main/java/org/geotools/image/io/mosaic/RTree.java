/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
 */
package org.geotools.image.io.mosaic;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Queue;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;

import org.geotools.util.logging.Logging;
import org.geotools.resources.OptionalDependencies;


/**
 * An R-Tree like structure having a {@link TreeNode} has its root. This is not a real RTree but
 * provides a few similar features tuned for {@link TileManager} needs (especially regarding the
 * management of subsampling information).
 * <p>
 * This class is <strong>not</strong> thread safe. Instances can be {@linkplain #clone cloned} if
 * needed for concurrent access in different threads. The {@link TreeNode} will not be duplicated
 * so cloning an {@link RTree} can be seen as creating a new worker for the same tree.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RTree {
    /**
     * The logger for debugging information.
     */
    private static final Logger LOGGER = Logging.getLogger(RTree.class);

    /**
     * The logging level for printing a tree of the nodes obtained by {@link #searchTiles}. We
     * use {@link Level#FINER} because it is slightly lower than the {@link MosaicImageReader}
     * one, which logs the final {@link Tile} selected at {@link Level#FINE}.
     */
    private static final Level LEVEL = Level.FINER;

    /**
     * The root of the tree.
     */
    protected final TreeNode root;

    /**
     * The requested region. This field must be set before {@link #searchTiles} is invoked.
     */
    protected Rectangle regionOfInterest;

    /**
     * The subsamplings. Before the search, must be set at the requested subsamplings.
     * After the search, they are set to the subsamplings of the best set of tiles found.
     */
    protected int xSubsampling, ySubsampling;

    /**
     * {@code true} if the search is allowed to look for tiles with finer subsampling than the
     * specified one. This field must be set before {@link #searchTiles} is invoked.
     */
    protected boolean subsamplingChangeAllowed;

    /**
     * Initialized to ({@link #xSubsampling}, {@link #ySubsampling}) at the begining
     * of a search, then modified during the search for internal purpose.
     */
    private Dimension subsampling;

    /**
     * Modified value of {@link #subsampling}.
     * This is a temporary value modified during searchs.
     */
    private final Dimension tmpSubsampling;

    /**
     * The subsampling done so far. This is used during
     * search and emptied once the search is finished.
     */
    private final Set<Dimension> subsamplingDone;

    /**
     * Additional subsampling to try. This is used during
     * search and emptied once the search is finished.
     */
    private final Queue<Dimension> subsamplingToTry;

    /**
     * Used in order to make sure that there is not tile with identical bounds. This is a
     * simple check (checking for inclusion would be more generic), but this case is common
     * enough and using an hash map for that is fast.
     */
    private final Map<Rectangle,SelectedNode> distinctBounds;

    /**
     * {@code true} if this {@code RTree} instance is currently in use by any thread, or
     * {@code false} if it is available for use.
     */
    boolean inUse;

    /**
     * Creates a RTree using the given root node.
     */
    public RTree(final TreeNode root) {
        this.root = root;
        tmpSubsampling   = new Dimension();
        subsamplingDone  = new HashSet<Dimension>();
        subsamplingToTry = new LinkedList<Dimension>();
        distinctBounds   = new HashMap<Rectangle,SelectedNode>();
    }

    /**
     * Returns a copy of this tree.
     */
    @Override
    public RTree clone() {
        return new RTree(root);
    }

    /**
     * Sets the subsampling to the specified value.
     */
    public void setSubsampling(final Dimension subsampling) {
        xSubsampling = subsampling.width;
        ySubsampling = subsampling.height;
    }

    /**
     * Returns the bounding box of all tiles.
     */
    public Rectangle getBounds() {
        return new Rectangle(root);
    }

    /**
     * Returns the largest tile width and largest tile height in the children,
     * not scanning into subtrees.
     */
    public Dimension getTileSize() {
        final Dimension tileSize = new Dimension();
        for (final TreeNode node : root) {
            final GridNode child = (GridNode) node;
            final int width  = child.width  / child.xSubsampling;
            final int height = child.height / child.ySubsampling;
            if (width  > tileSize.width)  tileSize.width  = width;
            if (height > tileSize.height) tileSize.height = height;
        }
        return tileSize;
    }

    /**
     * Returns every tiles that intersect the {@linkplain #regionOfInterest region of interest},
     * which must be set before this method is invoked. This method does not use any cache - the
     * search is performed inconditionnaly.
     * <p>
     * On input, the following fields must be set:
     * <ul>
     *   <li>{@link #regionOfInterest}</li>
     *   <li>{@link #subsamplingChangeAllowed}</li>
     * </ul>
     * <p>
     * On output, the following fields will be set:
     * <ul>
     *   <li>{@link SubsampledRectangle#xSubsampling} and {@link SubsampledRectangle#ySubsampling}
     *       if {@link #allowSubsamplingChange} is {@code true}</li>
     * </ul>
     */
    public List<Tile> searchTiles() throws IOException {
        assert subsamplingDone.isEmpty() && subsamplingToTry.isEmpty() && distinctBounds.isEmpty();
        subsampling = new Dimension(xSubsampling, ySubsampling);
        SelectedNode bestCandidate = null;
        int bestCandidateCount = 0;
        try {
            do {
                final SelectedNode candidate = addTileCandidate(root, Long.MAX_VALUE);
                /*
                 * We now have the final set of tiles for current subsampling. Checks if the cost
                 * of this set is lower than previous sets, and keep as "best candidates" if it is.
                 * If there is other subsamplings to try, we redo the process again in case we find
                 * cheaper set of tiles.
                 */
                if (candidate != null) {
                    final int candidateCount;
                    try {
                        candidate.filter(distinctBounds);
                        candidateCount = distinctBounds.size();
                    } finally {
                        distinctBounds.clear();
                    }
                    if (bestCandidate != null) {
                        if (!candidate.isCheaperThan(bestCandidate)) {
                            continue;
                        }
                    }
                    bestCandidate = candidate;
                    bestCandidateCount = candidateCount;
                    setSubsampling(subsampling);
                }
            } while ((subsampling = subsamplingToTry.poll()) != null);
        } finally {
            subsamplingToTry.clear();
            subsamplingDone .clear();
        }
        /*
         * TODO: sort the result. I'm not sure that it is worth, but if we decide that it is,
         * we could use the Comparator<GridNode> implemented by the GridNode class.
         */
        final List<Tile> tiles = new ArrayList<Tile>(bestCandidateCount);
        if (bestCandidate != null) {
            assert bestCandidate.checkValidity() != null : bestCandidate;
            bestCandidate.getTiles(tiles);
            if (LOGGER.isLoggable(LEVEL)) {
                final String lineSeparator = System.getProperty("line.separator", "\n");
                final StringBuilder message = new StringBuilder("Tiles count: ")
                        .append(tiles.size()).append(lineSeparator);
                OptionalDependencies.format(bestCandidate, message, lineSeparator);
                final LogRecord record = new LogRecord(LEVEL, message.toString());
                record.setSourceClassName("org.geotools.image.io.mosaic.TileManager");
                record.setSourceMethodName("getTiles"); // This is the public API for this method.
                LOGGER.log(record);
            }
        }
        return tiles;
    }

    /**
     * Searchs the tiles starting from the given node. This method invokes
     * itself recursively for scanning the child nodes down the tree.
     * <p>
     * If this method <em>added</em> some tiles to the reading process, their region (identical to
     * the keys in the {@link #distinctBounds} hash map) are {@linkplain SelectedNode#addChild added
     * as child} of the returned object. The children does not include tiles that <em>replaced</em>
     * existing ones rather than adding a new ones.
     *
     * @param  node The root of the subtree to examine.
     * @param  costLimit Stop the children searchs if the cost exceed this amount.
     * @param  candidates The tiles that are under consideration during a search.
     * @return The tile to be read, or {@code null} if it doesn't intersect the area of interest.
     */
    private SelectedNode addTileCandidate(final TreeNode node, long costLimit) throws IOException {
        if (!node.intersects(regionOfInterest)) {
            return null;
        }
        SelectedNode selected = null;
        final Tile tile = node.getUserObject();
        if (tile != null) {
            assert node.equals(tile.getAbsoluteRegion()) : tile;
            final Dimension floor = tile.getSubsamplingFloor(subsampling);
            if (floor == null) {
                /*
                 * The tile in the given node is unable to read its image at the given subsampling
                 * or any smaller subsampling. Skip this tile. However we may try its children at
                 * the end of this method, since they typically have a finer subsampling.
                 */
            } else if (floor != subsampling) {
                /*
                 * The tile in the given node is unable to read its image at the given subsampling,
                 * but would be capable if the subsampling was smaller. If we are allowed to change
                 * the setting, add this item to the queue of subsamplings to try later.
                 */
                if (subsamplingChangeAllowed) {
                    if (subsamplingDone.add(floor)) {
                        subsamplingToTry.add(floor);
                    }
                }
            } else {
                /*
                 * The tile is capable to read its image at the given subsampling.
                 * Computes the cost that reading this tile would have.
                 */
                final Rectangle readRegion = node.intersection(regionOfInterest);
                selected = new SelectedNode(readRegion);
                selected.tile = tile;
                tmpSubsampling.setSize(subsampling);
                selected.cost = tile.countUnwantedPixelsFromAbsolute(readRegion, tmpSubsampling);
            }
        }
        /*
         * At this point, we have processed the node given in argument. If the tile was not selected
         * (typically because its resolution is not suitable), we will create a node without tile to
         * be used as a container for allowing the search to continue with children.
         */
        if (node.isLeaf()) {
            return selected;
        }
        final long cost;
        if (selected == null) {
            selected = new SelectedNode(node.intersection(regionOfInterest));
            cost = selected.cost; // Should be 0.
        } else {
            /*
             * If the region to read encompass entirely this node (otherwise reading a few childs
             * may be cheaper) and if the children subsampling are not higher than the tile's one
             * (they are usually not), then there is no need to continue down the tree since the
             * childs can not do better than this node.
             *
             * TODO: Checks if the children fill completly the bounds (i.e. are "dense").
             */
            cost = selected.cost;
            if (cost == 0 || (selected.equals(node) && !tile.isFinerThan(subsampling))) {
                return selected;
            }
            if (cost < costLimit) {
                costLimit = cost;
            }
        }
        /*
         * If there is any children, invokes this method recursively for each of them. The later
         * search will be canceled before completion (in order to save CPU time) if the children
         * cost exceed the given maximum cost, usually the cost of the parent tile.
         */
        for (final TreeNode child : node) {
            selected.addChild(addTileCandidate(child, costLimit));
            if (selected.cost - cost >= costLimit) {
                /*
                 * Children are going to be too costly, so stop the search immediately. If the
                 * selected node has a tile,  remove the children in order to get the selected
                 * tile used instead. If the selected node has no tile, then keep the children
                 * even if they are incomplete in order to let the invoker known that we reached
                 * the cost limit.
                 */
                if (selected.tile != null) {
                    selected.removeChildren();
                }
                return selected;
            }
        }
        /*
         * At this point, we decided to keep the children in replacement of the selected
         * tile. Clears the tile, adjust the cost remove an indirection level if we can.
         */
        selected.tile = null;
        selected.cost -= cost;
        if (selected.isLeaf()) {
            // The 'selected' node was just a container and we found no children,
            // so it is not worth to returns it.
            return null;
        }
        final TreeNode child = selected.getChild();
        if (child != null && child.equals(selected)) {
            // Founds exactly one child and this child has the same bounding box than
            // the selected node. Returns the child directly for saving one indirection.
            selected.removeChildren();
            selected = (SelectedNode) child;
        }
        return selected;
    }

    /**
     * Returns a string representation of this tree, including children.
     */
    @Override
    public String toString() {
        return OptionalDependencies.toString(root);
    }
}
