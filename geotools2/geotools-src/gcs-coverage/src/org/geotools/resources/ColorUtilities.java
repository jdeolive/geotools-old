/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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

// J2SE dependencies
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.Arrays;


/**
 * A set of static methods for handling of colors informations. Some of those methods are useful,
 * but not really rigorous. This is why they do not appear in any &quot;official&quot; package,
 * but instead in this private one.
 *
 *                      <strong>Do not rely on this API!</strong>
 *
 * It may change in incompatible way in any future version.
 *
 * @version $Id: ColorUtilities.java,v 1.1 2003/07/22 15:24:54 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class ColorUtilities {
    /**
     * Small number for rounding errors.
     */
    private static final double EPS = 1E-6;

    /**
     * Do not allow creation of instances of this class.
     */
    private ColorUtilities() {
    }

    /**
     * Returns a subarray of the specified color array. The <code>lower</code> and
     * <code>upper</code> index will be clamb into the <code>palette</code> range.
     * If they are completly out of range, or if they would result in an empty array,
     * then <code>null</code> is returned.
     *
     * This method is used by {@link org.geotools.cv.SampleDimension} as an heuristic
     * approach for distributing palette colors into a list of categories.
     *
     * @param  palette The color array (may be <code>null</code>).
     * @param  lower  The lower index, inclusive.
     * @param  upper  The upper index, inclusive.
     * @return The subarray (may be <code>palette</code> if the original array already fit),
     *         or <code>null</code> if the <code>lower</code> and <code>upper</code> index
     *         are out of <code>palette</code> bounds.
     */
    public static Color[] subarray(final Color[] palette, int lower, int upper) {
        if (palette != null) {
            lower = Math.max(lower, 0);
            upper = Math.min(upper, palette.length);
            if (lower >= upper) {
                return null;
            }
            if (lower!=0 || upper!=palette.length) {
                final Color[] sub = new Color[upper-lower];
                System.arraycopy(palette, lower, sub, 0, sub.length);
                return sub;
            }
        }
        return palette;
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
        return getIndexColorModel(ARGB, 1, 0);
    }

    /**
     * Returns a tolerant index color model for the specified ARGB code. This color model accept
     * image with the specified number of bands.
     *
     * @param  ARGB         An array of ARGB values.
     * @param  numBands     The number of bands.
     * @param  visibleBands The band to display.
     * @return An index color model for the specified array.
     */
    public static IndexColorModel getIndexColorModel(final int[] ARGB,
                                                     final int numBands,
                                                     final int visibleBand)
    {
        boolean hasAlpha = false;
        int  transparent = -1;
        for (int i=0; i<ARGB.length; i++) {
            final int alpha = ARGB[i] & 0xFF000000;
            if (alpha != 0xFF000000) {
                if (alpha==0x00000000 && transparent<0) {
                    transparent = i;
                    continue;
                }
                hasAlpha = true;
                break;
            }
        }
        final int bits = getBitCount(ARGB.length);
        final int type = getTransferType(ARGB.length);
        if (numBands == 1) {
            return new IndexColorModel(bits, ARGB.length, ARGB, 0, hasAlpha, transparent, type);
        } else {
            return new MultiBandsIndexColorModel(bits, ARGB.length, ARGB, 0, hasAlpha, transparent,
                                                 type, numBands, visibleBand);
        }
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

    /**
     * Transform a color from XYZ color space to LAB. The color are transformed
     * in place. This method returns <code>color</code> for convenience.
     * Reference: http://www.brucelindbloom.com/index.html?ColorDifferenceCalc.html
     */
    private static float[] XYZtoLAB(final float[] color) {
        color[0] /= 0.9642;   // Other refeference: 0.95047;
        color[1] /= 1.0000;   //                    1.00000;
        color[2] /= 0.8249;   //                    1.08883;
        for (int i=0; i<3; i++) {
            final float c = color[i];
            color[i] = (float)((c > 216/24389f) ? Math.pow(c, 1.0/3) : ((24389/27.0)*c + 16)/116);
        }
        final float L = 116 *  color[1] - 16;
        final float a = 500 * (color[0] - color[1]);
        final float b = 200 * (color[1] - color[2]);
        assert !Float.isNaN(L) && !Float.isNaN(a) && !Float.isNaN(b);
        color[0] = L;
        color[1] = a;
        color[2] = b;
        return color;
    }

    /**
     * Compute the distance E (CIE 1994) between two colors in LAB color space.
     * Reference: http://www.brucelindbloom.com/index.html?ColorDifferenceCalc.html
     */
    private static float colorDistance(final float[] lab1, final float[] lab2) {
        if (false) {
            // Compute distance using CIE94 formula.
            // NOTE: this formula sometime fails because of negative
            //       value in the first Math.sqrt(...) expression.
            final double dL = (double)lab1[0] - lab2[0];
            final double da = (double)lab1[1] - lab2[1];
            final double db = (double)lab1[2] - lab2[2];
            final double C1 = XMath.hypot(lab1[1], lab1[2]);
            final double C2 = XMath.hypot(lab2[1], lab2[2]);
            final double dC = C1 - C2;
            final double dH = Math.sqrt(da*da + db*db - dC*dC);
            final double sL = dL / 2;
            final double sC = dC / (1 + 0.048*C1);
            final double sH = dH / (1 + 0.014*C1);
            return (float)Math.sqrt(sL*sL + sC*sC + sH*sH);
        } else {
            // Compute distance using delta E formula.
            double sum = 0;
            for (int i=Math.min(lab1.length, lab2.length); --i>=0;) {
                final double delta = lab1[i] - lab2[i];
                sum += delta*delta;
            }
            return (float)Math.sqrt(sum);
        }
    }

    /**
     * Returns the most transparent pixel in the specified color model. If many colors has
     * the same alpha value, than the darkest one is returned. This method never returns
     * a negative value (0 is returned if the color model has no colors).
     *
     * @param  colors The color model in which to look for a transparent color.
     * @return The index of a transparent color, or 0.
     */
    public static int getTransparentPixel(final IndexColorModel colors) {
        int index = colors.getTransparentPixel();
        if (index < 0) {
            index = 0;
            int   alpha = Integer.MAX_VALUE;
            float delta = Float.POSITIVE_INFINITY;
            final ColorSpace space = colors.getColorSpace();
            final float[] RGB   = new float[3];
            final float[] BLACK = XYZtoLAB(space.toCIEXYZ(RGB)); // Black in Lab color space.
            assert BLACK != RGB;
            for (int i=colors.getMapSize(); --i>=0;) {
                final int a = colors.getAlpha(i);
                if (a <= alpha) {
                    RGB[0] = colors.getRed  (i)/255f;
                    RGB[1] = colors.getGreen(i)/255f;
                    RGB[2] = colors.getBlue (i)/255f;
                    final float d = colorDistance(XYZtoLAB(space.toCIEXYZ(RGB)), BLACK);
                    assert d >= 0 : i; // Check mostly for NaN value
                    if (a<alpha || d<delta) {
                        alpha = a;
                        delta = d;
                        index = i;
                    }
                }
            }
        }
        return index;
    }

    /**
     * Returns the index of the specified color, excluding the specified one. If the color
     * is not explicitly found, a close color is returned. This method never returns a negative
     * value (0 is returned if the color model has no colors).
     *
     * @param  colors The color model in which to look for a color index.
     * @param  color The color to search for.
     * @param  exclude An index to exclude from the search (usually the background or the
     *         {@linkplain #getTransparentPixel transparent} pixel), or -1 if none.
     * @return The index of the color, or 0.
     */
    public static int getColorIndex(final IndexColorModel colors,
                                    final Color color,
                                    final int exclude)
    {
        final ColorSpace space = colors.getColorSpace();
        final float[] RGB = {
            color.getRed()  /255f,
            color.getGreen()/255f,
            color.getBlue() /255f
        };
        final float[] REF = XYZtoLAB(space.toCIEXYZ(RGB));
        float delta = Float.POSITIVE_INFINITY;
        int index = 0;
        assert REF != RGB;
        for (int i=colors.getMapSize(); --i>=0;) {
            if (i != exclude) {
                RGB[0] = colors.getRed  (i)/255f;
                RGB[1] = colors.getGreen(i)/255f;
                RGB[2] = colors.getBlue (i)/255f;
                final float d = colorDistance(XYZtoLAB(space.toCIEXYZ(RGB)), REF);
                assert d >= 0 : i; // Check mostly for NaN value
                if (d <= delta) {
                    delta = d;
                    index = i;
                }
            }
        }
        return index;
    }
}
