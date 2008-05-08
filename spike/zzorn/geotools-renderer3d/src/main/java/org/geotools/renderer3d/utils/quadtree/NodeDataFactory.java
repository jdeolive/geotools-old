package org.geotools.renderer3d.utils.quadtree;

/**
 * Creates the data object for a quad tree node, when needed.
 * <p/>
 * N is the type of a data object associated with each QuadTreeNode.
 *
 * @author Hans Häggström
 */
public interface NodeDataFactory<N>
{
    /**
     * Creates the data object for a quad tree node.
     * <p/>
     * Called when the node is created, or alternatively when the node data object is first requested.
     *
     * @param node The node to create the data object for.
     *
     * @return the data object for the node.  May be null.
     */
    N createNodeDataObject( QuadTreeNode<N> node );

    /**
     * Called when a node is reused, and the corresponding data object could also be reused (or just re-calculated).
     *
     * @param node     the reused node.  The node will have its bounding box and parent set.
     * @param nodeData
     */
    N reuseNodeDataObject( final QuadTreeNode<N> node, final N nodeData );

    /**
     * Called when the quad tree node containing the specified data object is released and put into the pool for
     * recycling.
     *
     * @param nodeData
     */
    void onDataObjectUnused( final N nodeData );
}
