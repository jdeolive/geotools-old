package org.geotools.renderer3d.utils.quadtree;

/**
 * A listener that is notified when the root node changes in a quad tree.
 *
 * @author Hans Häggström
 */
public interface QuadTreeListener<N>
{
    void onRootChanged( QuadTreeNode<N> newRoot );
}
