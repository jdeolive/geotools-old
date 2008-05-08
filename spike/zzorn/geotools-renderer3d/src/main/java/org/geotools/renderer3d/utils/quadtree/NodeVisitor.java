package org.geotools.renderer3d.utils.quadtree;

/**
 * A visitor class for visiting QuadTreeNodes.
 *
 * @author Hans Häggström
 */
public interface NodeVisitor<N>
{
    /**
     * @param node the node to visit
     *
     * @return true if we should continue visiting nodes, false if we should stop now.
     */
    boolean visitNode( QuadTreeNode<N> node );
}
