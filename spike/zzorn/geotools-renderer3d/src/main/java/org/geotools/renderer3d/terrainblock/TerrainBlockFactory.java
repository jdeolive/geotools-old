package org.geotools.renderer3d.terrainblock;

import org.geotools.renderer3d.provider.texture.impl.TextureProvider;
import org.geotools.renderer3d.utils.quadtree.NodeDataFactory;
import org.geotools.renderer3d.utils.quadtree.QuadTreeNode;

/**
 * Creates terrain blocks for areas specified by quad tree nodes.
 *
 * @author Hans Häggström
 */
public final class TerrainBlockFactory
        implements NodeDataFactory<TerrainBlock>
{

    //======================================================================
    // Private Fields

    //======================================================================
    // Private Fields
    private final int myNumberOfGridsPerSide;

    private final TextureProvider myTextureProvider;
    private final int myTextureSize;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param numberOfGridsPerSide number of grid cells along the side of a TerrainBlock.
     * @param textureProvider      something that supplies the textures for terrain blocks.
     * @param textureSize          number of pixels per side for the texture.
     */
    public TerrainBlockFactory( final int numberOfGridsPerSide,
                                final TextureProvider textureProvider,
                                final int textureSize )
    {
        myNumberOfGridsPerSide = numberOfGridsPerSide;
        myTextureProvider = textureProvider;
        myTextureSize = textureSize;

    }

    //----------------------------------------------------------------------
    // NodeDataFactory Implementation

    //----------------------------------------------------------------------
    // NodeDataFactory ImplementationK}}

    public TerrainBlock createNodeDataObject( final QuadTreeNode<TerrainBlock> node )
    {
        final TerrainBlockImpl terrainBlock = new TerrainBlockImpl( node,
                                                                    myNumberOfGridsPerSide,
                                                                    myTextureSize,
                                                                    myTextureProvider );

        return terrainBlock;
    }


    public TerrainBlock reuseNodeDataObject( final QuadTreeNode<TerrainBlock> node,
                                             final TerrainBlock existingTerrainBlock )
    {
        if ( existingTerrainBlock == null )
        {
            return createNodeDataObject( node );
        }
        else
        {

            existingTerrainBlock.updateDerivedData();

            return existingTerrainBlock;
        }
    }

    public void onDataObjectUnused( final TerrainBlock nodeData )
    {
        // DEBUG
        if ( nodeData != null )
        {
            nodeData.clearPicture();
        }
    }

}
