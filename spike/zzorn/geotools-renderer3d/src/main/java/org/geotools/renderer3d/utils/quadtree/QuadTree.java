package org.geotools.renderer3d.utils.quadtree;

import org.geotools.renderer3d.utils.BoundingRectangle;

/**
 * A quadtree datastructure for fast geometrical look-up of nodes in a certain area.
 * <p/>
 * Should also provide access to the quadtree structure itself, e.g. for density field visualization.
 * <p/>
 * Should not have a fixed root node, but instead expand the root as needed also.
 * <p/>
 * N is the type of a data object associated with each QuadTreeNode.
 *
 * @author Hans Häggström
 */
public interface QuadTree<N>
{

    //======================================================================
    // Public Methods

    /**
     * @return the root node of this QuadTree.
     */
    QuadTreeNode<N> getRootNode();


    /**
     * Called by a QuadTreeNode when the root node is changed.
     * <p/>
     * Should not be called from client code.
     *
     * @param newRootNode the new root node.
     */
    void setRootNode( QuadTreeNode<N> newRootNode );

    /**
     * @return a factory used to create node data for the quad tree nodes.  Does not return null.
     */
    NodeDataFactory<N> getNodeDataFactory();

    /**
     * Adds the specified QuadTreeListener.  The listener is notified when the root node changes.
     *
     * @param addedQuadTreeListener should not be null or already added.
     */
    void addQuadTreeListener( QuadTreeListener<N> addedQuadTreeListener );

    /**
     * Removes the specified QuadTreeListener.
     *
     * @param removedQuadTreeListener should not be null, and should be present.
     */
    void removeQuadTreeListener( QuadTreeListener<N> removedQuadTreeListener );

    void releaseQuadTreeNode( final QuadTreeNode<N> node );

    QuadTreeNode<N> createQuadTreeNode( final BoundingRectangle bounds, final QuadTreeNode<N> parentNode );

    void initnodedata( QuadTreeNode<N> quadTreeNode );
}
