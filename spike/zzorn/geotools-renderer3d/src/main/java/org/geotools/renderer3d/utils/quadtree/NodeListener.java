package org.geotools.renderer3d.utils.quadtree;

/**
 * Listens to QuadTreeNode events, such as expansion or collapse, and deletion.
 *
 * @author Hans Häggström
 */
public interface NodeListener<N>
{
    /**
     * Called when a node is collapsed, that is, its child nodes are removed.
     */
    void onCollapsed( QuadTreeNode<N> node );

    /**
     * Called when a node is expanded, that is, child nodes are added.
     */
    void onExpanded( QuadTreeNode<N> node );
}
