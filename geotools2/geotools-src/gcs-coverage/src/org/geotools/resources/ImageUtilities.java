/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.resources;

// Image, color and geometry (Java2D)
import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;

// Utilities
import java.util.Arrays;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.ImageLayout;


/**
 * A set of static methods working on images and for handling of colors
 * informations. Some of those methods are useful, but not really rigorous.
 * This is why they do not appear in any "official" package, but instead in
 * this private one.
 *
 *                      <strong>Do not rely on this API!</strong>
 *
 * It may change in incompatible way in any future version.
 *
 * @version $Id: ImageUtilities.java,v 1.2 2002/07/23 17:53:37 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class ImageUtilities {
    /**
     * The default tile size. This default tile size can be
     * overriden with a call to {@link JAI#setDefaultTileSize}.
     */
    private static final Dimension DEFAULT_TILE_SIZE = new Dimension(512,512);

    /**
     * The minimum tile size.
     */
    private static final int MIN_TILE_SIZE = 128;

    /**
     * Small number for rounding errors.
     */
    private static final double EPS = 1E-6;

    /**
     * Do not allow creation of
     * instances of this class.
     */
    private ImageUtilities() {
    }

    /**
     * Suggest an {@link ImageLayout} for the specified image.
     * All parameters are initially set equal to those of the
     * given {@link RenderedImage}, and then the tile size is
     * updated according the image's size.  This method never
     * returns <code>null</code>.
     */
    public static ImageLayout getImageLayout(final RenderedImage image) {
        return getImageLayout(image, true);
    }

    /**
     * Returns an {@link ImageLayout} for the specified image.
     * If <code>initToImage</code> is <code>true</code>, then
     * All parameters are initially set equal to those of the
     * given {@link RenderedImage} and the returned layout is
     * never <code>null</code>.
     */
    private static ImageLayout getImageLayout(final RenderedImage image, final boolean initToImage) {
        ImageLayout layout = initToImage ? new ImageLayout(image) : null;
        if (image.getNumXTiles()==1 && image.getNumYTiles()==1) {
            // If the image was already tiled, reuse the same tile size.
            // Otherwise, compute default tile size.  If a default tile
            // size can't be computed, it will be left unset.
            if (layout != null) {
                layout = layout.unsetTileLayout();
            }
            Dimension defaultSize = JAI.getDefaultTileSize();
            if (defaultSize!=null) {
                defaultSize = DEFAULT_TILE_SIZE;
            }
            int s;
            if ((s=toTileSize(image.getWidth(), defaultSize.width)) != 0) {
                if (layout==null) {
                    layout=new ImageLayout();
                }
                layout = layout.setTileWidth(s);
            }
            if ((s=toTileSize(image.getHeight(), defaultSize.height)) != 0) {
                if (layout==null) {
                    layout=new ImageLayout();
                }
                layout = layout.setTileHeight(s);
            }
        }
        return layout;
    }

    /**
     * Suggest a set of {@link RenderingHints} for the specified image.
     * The rendering hints may include the following parameters:
     *
     * <ul>
     *   <li>{@link JAI#KEY_IMAGE_LAYOUT} with a proposed tile size.</li>
     * </ul>
     *
     * This method may returns <code>null</code>
     * if no rendering hints is proposed.
     */
    public static RenderingHints getRenderingHints(final RenderedImage image) {
        final ImageLayout layout = getImageLayout(image, false);
        return (layout!=null) ? new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout) : null;
    }

    /**
     * Suggest a tile size for the specified image size. On input,
     * <code>size</code> is the image's size. On output, it is the
     * tile size. This method returns <code>size</code> for convenience.
     */
    public static Dimension toTileSize(final Dimension size) {
        Dimension defaultSize = JAI.getDefaultTileSize();
        if (defaultSize!=null) {
            defaultSize = DEFAULT_TILE_SIZE;
        }
        int s;
        if ((s=toTileSize(size.width,  defaultSize.width )) != 0) size.width  = s;
        if ((s=toTileSize(size.height, defaultSize.height)) != 0) size.height = s;
        return size;
    }

    /**
     * Suggest a tile size close to <code>tileSize</code> for the specified
     * <code>imageSize</code>. If this method can't suggest a size, then it
     * returns 0.
     */
    private static int toTileSize(final int imageSize, final int tileSize) {
        int sopt=0, rmax=0;
        final int MAX_TILE_SIZE = Math.min(tileSize*2, imageSize);
        final int stop = Math.max(tileSize-MIN_TILE_SIZE, MAX_TILE_SIZE-tileSize);
        for (int i=0; i<=stop; i++) {
            int s,r;
            if ((s=tileSize-i) >= MIN_TILE_SIZE) {
                r = imageSize % s;
                if (r==0) return s;
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
            if ((s=tileSize+i) <= MAX_TILE_SIZE) {
                r = imageSize % s;
                if (r==0) return s;
                if (r > rmax) {
                    rmax = r;
                    sopt = s;
                }
            }
        }
        return (tileSize-rmax <= tileSize/4) ? sopt : 0;
    }

    /**
     * Copy <code>colors</code> into array <code>ARGB</code> from index <code>lower</code>
     * inclusive to index <code>upper</code> exclusive. If <code>upper-lower</code> is not
     * equals to the length of <code>colors</code> array, then colors will be interpolated.
     *
     * @param colors Colors to copy into the <code>ARGB</code> array.
     * @param ARGB   Array of integer to write ARGB values to.
     * @param lower  Index (inclusive) of the first element of <code>ARGB</code> to change.
     * @param upper  Index (exclusive) of the last  element of <code>ARGB</code> to change.
     */
    public static void expand(final Color[] colors, final int[] ARGB,
                              final int lower, final int upper)
    {
        switch (colors.length) {
            case 1: Arrays.fill(ARGB, lower, upper, colors[0].getRGB()); // fall through
            case 0: return; // Note: getRGB() is really getARGB()
        }
        switch (upper-lower) {
            case 1: ARGB[lower] = colors[0].getRGB(); // fall through
            case 0: return; // Note: getRGB() is really getARGB()
        }
        final int  maxBase = colors.length-2;
        final double scale = (double)(colors.length-1) / (double)(upper-1-lower);
        for (int i=lower; i<upper; i++) {
            final double index = (i-lower)*scale;
            final int     base = Math.min(maxBase, (int)(index+EPS)); // Round toward 0, which is really what we want.
            final double delta = index-base;
            final Color     C0 = colors[base+0];
            final Color     C1 = colors[base+1];
            int A = C0.getAlpha();
            int R = C0.getRed  ();
            int G = C0.getGreen();
            int B = C0.getBlue ();
            ARGB[i] = (round(A+delta*(C1.getAlpha()-A)) << 24) |
                      (round(R+delta*(C1.getRed  ()-R)) << 16) |
                      (round(G+delta*(C1.getGreen()-G)) <<  8) |
                      (round(B+delta*(C1.getBlue ()-B)) <<  0);
        }
    }

    /**
     * Round a float value and clamp the
     * result between 0 and 255 inclusive.
     */
    private static int round(final double value) {
        return Math.min(Math.max((int)Math.round(value),0),255);
    }

    /**
     * Returns an index color model for specified ARGB codes.   If the specified
     * array has not transparent color (i.e. all alpha values are 255), then the
     * returned color model will be opaque. Otherwise, if the specified array has
     * one and only one color with alpha value of 0, the returned color model will
     * have only this transparent color. Otherwise, the returned color model will
     * be translucide.
     *
     * @param  ARGB An array of ARGB values.
     * @return An index color model for the specified array.
     */
    public static IndexColorModel getIndexColorModel(final int[] ARGB) {
        boolean hasAlpha = false;
        int  transparent = -1;
        for (int i=0; i<ARGB.length; i++) {
            final int alpha = ARGB[i] & 0xFF000000;
            if (alpha!=0xFF000000) {
                if (alpha==0x00000000 && transparent<0) {
                    transparent=i;
                    continue;
                }
                hasAlpha=true;
                break;
            }
        }
        return new IndexColorModel(getBitCount(ARGB.length), ARGB.length, ARGB, 0,
                                   hasAlpha, transparent, getTransferType(ARGB.length));
    }

    /**
     * Returns a suggered bit count for an {@link IndexColorModel} of
     * <code>mapSize</code> colors. This method returns 1, 2, 4, 8 or
     * 16 according the value of <code>mapSize</code>. It is guaranteed
     * that the following relation is hold:
     *
     * <center><pre>(1 << getBitCount(mapSize)) >= mapSize</pre></center>
     */
    public static int getBitCount(final int mapSize) {
        if (mapSize <= 0x00002) return  1;
        if (mapSize <= 0x00004) return  2;
        if (mapSize <= 0x00010) return  4;
        if (mapSize <= 0x00100) return  8;
        if (mapSize <= 0x10000) return 16;
        throw new IllegalArgumentException(Integer.toString(mapSize));
    }

    /**
     * Returns a suggered type for an {@link IndexColorModel}
     * of <code>mapSize</code> colors. This method returns
     * {@link DataBuffer#TYPE_BYTE} or {@link DataBuffer#TYPE_USHORT}.
     */
    private static int getTransferType(final int mapSize) {
        return (mapSize <= 256) ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
    }
}
