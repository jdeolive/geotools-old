/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.Map;
import java.awt.Rectangle;
import org.geotools.resources.OptionalDependencies;


/**
 * A tree node selected because of its inclusion in a Region Of Interest (ROI).
 * It contains an estimation of the cost of reading this tile and its children.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial") // Not expected to be serialized.
final class SelectedNode extends TreeNode {
    /**
     * An estimation of the cost of reading this tile, including children.
     */
    protected long cost;

    /**
     * Creates a new node for the given region.
     *
     * @param readRegion The region to be read.
     */
    SelectedNode(final Rectangle readRegion) {
        super(readRegion);
        assert !isEmpty();
    }

    /**
     * Adds the given tile as a child of this tile.
     *
     * @throws ClassCastException if the given child is not an instance of {@code SelectedNode}.
     *         This is an intentional restriction in order to avoid more subtile bugs later.
     */
    @Override
    public void addChild(final TreeNode child) throws ClassCastException {
        super.addChild(child);
        if (child != null) {
            final long added = ((SelectedNode) child).cost;
            if (added != 0) {
                SelectedNode parent = this;
                do {
                    parent.cost += added;
                } while ((parent = (SelectedNode) parent.getParent()) != null);
            }
        }
    }

    /**
     * Removes all children.
     */
    @Override
    public void removeChildren() {
        final long removed = childrenCost();
        if (removed != 0) {
            SelectedNode parent = this;
            do {
                parent.cost -= removed;
            } while ((parent = (SelectedNode) parent.getParent()) != null);
        }
        super.removeChildren();
    }

    /**
     * Removes this tile and all its children from the tree.
     */
    @Override
    public void remove() {
        if (cost != 0) {
            TreeNode parent = this;
            while ((parent = parent.getParent()) != null) {
                ((SelectedNode) parent).cost -= cost;
            }
        }
        super.remove();
    }

    /**
     * Returns the cost of children only, not including the {@linkplain #tile} in this node.
     */
    private long childrenCost() {
        long c = 0;
        SelectedNode child = (SelectedNode) firstChildren();
        while (child != null) {
            c += child.cost;
            child = (SelectedNode) child.nextSibling();
        }
        assert cost >= c;
        return c;
    }

    /**
     * Returns {@code true} if this node has a lower cost than the specified one.
     */
    public final boolean isCheaperThan(final SelectedNode other) {
        if (cost < other.cost) {
            return true;
        }
        if (cost == other.cost) {
            return getTileCount() < other.getTileCount();
        }
        return false;
    }

    /**
     * Removes the nodes having the same bounding box than this tile, then process recursively for
     * children. If such matchs are found, they are probably tiles at a different resolution.
     * Retains the one which minimize the disk reading, and discards the other ones.
     * <p>
     * This check is not generic since we search for an exact match, but this case is common
     * enough. Handling it with a {@link java.util.HashMap} will help to reduce the amount of
     * tiles to handle in a more costly way later.
     *
     * @param overlaps An initially empty map. Will be filled through recursive invocation
     *        of this method while we iterate down the tree.
     */
    final void filter(final Map<Rectangle,SelectedNode> overlaps) {
        /*
         * Must process children first because if any of them are removed, it will lower
         * the cost and consequently can change the decision taken at the end of this method.
         */
        SelectedNode child = (SelectedNode) firstChildren();
        while (child != null) {
            child.filter(overlaps);
            child = (SelectedNode) child.nextSibling();
        }
        SelectedNode existing = overlaps.put(this, this);
        if (existing != null && existing != this) {
            if (!isCheaperThan(existing)) {
                /*
                 * A cheaper tiles existed for the same bounds. Reinsert the previous tile in the
                 * map. We will delete this node from the tree later, except if the previous node
                 * is a children of this node. In the later case, we can't remove completly this
                 * node since it would remove its children as well, so we just nullify the tile.
                 */
                if (existing.getParent() == this) {
                    if (tile != null) {
                        tile = null;
                        cost = childrenCost();
                    }
                    return;
                }
                overlaps.put(existing, existing);
                existing = this;
            }
            existing.remove();
            existing.removeFrom(overlaps);
            assert overlaps.get(existing) != existing;
        }
    }

    /**
     * Removes all children from the given map. Note that we do not removes this node directly
     * because the corresponding Map.Entry is presumed already used by an other node.
     */
    private void removeFrom(final Map<Rectangle,SelectedNode> overlaps) {
        SelectedNode child = (SelectedNode) firstChildren();
        while (child != null) {
            child.removeFrom(overlaps);
            final SelectedNode existing = overlaps.remove(child);
            if (existing != null && existing != child) {
                overlaps.put(existing, existing);
            }
            child = (SelectedNode) child.nextSibling();
        }
    }

    /**
     * Invoked in assertion for checking the validity of the whole tree. Returns a string
     * representation of this selection as a tree. We do not override {@link #toString}
     * because the later is used for formatting the nodes in the tree.
     */
    @Override
    String checkValidity() {
        if (childrenCost() > cost) {
            throw new AssertionError(this);
        }
        String message = super.checkValidity();
        if (isRoot()) {
            message = OptionalDependencies.toString(this);
        }
        return message;
    }
}
