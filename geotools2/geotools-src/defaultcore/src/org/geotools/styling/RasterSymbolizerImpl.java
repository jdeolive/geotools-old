/*
 * RasterSymbolizerImpl.java
 *
 * Created on 13 November 2002, 13:47
 */
package org.geotools.styling;

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;


/**
 *
 * @author  iant
 */
public class RasterSymbolizerImpl implements RasterSymbolizer {
    private FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private ChannelSelection channelSelction = new ChannelSelectionImpl();
    private ColorMap colorMap = new ColorMapImpl();
    private ContrastEnhancement contrastEnhancement = new ContrastEnhancementImpl();
    private ShadedRelief shadedRelief = new ShadedReliefImpl();
    private String geometryName = "raster";
    private Symbolizer symbolizer;
    private Expression opacity;
    private Expression overlap;

    /** Creates a new instance of RasterSymbolizerImpl */
    public RasterSymbolizerImpl() {
        opacity = filterFactory.createLiteralExpression(1.0);
        overlap = filterFactory.createLiteralExpression("Random");
    }

    public int hashcode() {
        int key = 0;
        key = channelSelction.hashCode();
        key = (key * 13) + colorMap.hashCode();
        key = (key * 13) + contrastEnhancement.hashCode();
        key = (key * 13) + shadedRelief.hashCode();
        key = (key * 13) + opacity.hashCode();
        key = (key * 13) + overlap.hashCode();
        key = (key * 13) + geometryName.hashCode();

        return key;
    }

    /** The ChannelSelection element specifies the false-color channel selection for a multi-spectral raster
     * source (such as a multi-band satellite-imagery source).
     * Either a channel may be selected to display in each of red, green, and blue, or a single channel
     * may be selected to display in grayscale.  (The spelling “gray” is used since it seems to be more
     * common on the Web than “grey” by a ratio of about 3:1.)
     * Contrast enhancement may be applied to each channel in isolation.  Channels are identified by a system
     * and data-dependent character identifier.  Commonly, channels will be labelled as “1”, “2”, etc.
     * @return the ChannelSelection object set or null if none is available.
     *
     */
    public ChannelSelection getChannelSelection() {
        return channelSelction;
    }

    /** The ColorMap element defines either the colors of a palette-type raster source or the mapping of
     * fixed-numeric pixel values to colors.
     * For example, a DEM raster giving elevations in meters above sea level can be translated to a colored
     * image with a ColorMap.  The quantity attributes of a color-map are used for translating between numeric
     * matrixes and color rasters and the ColorMap entries should be in order of increasing numeric quantity so
     * that intermediate numeric values can be matched to a color (or be interpolated between two colors).
     * Labels may be used for legends or may be used in the future to match character values.
     * Not all systems can support opacity in colormaps.  The default opacity is 1.0 (fully opaque).
     * Defaults for quantity and label are system-dependent.
     *
     * @return the ColorMap for the raster
     *
     */
    public ColorMap getColorMap() {
        return colorMap;
    }

    /** The ContrastEnhancement element defines contrast enhancement for a channel of a false-color image or
     * for a color image.
     * In the case of a color image, the relative grayscale brightness of a pixel color is used.
     * “Normalize” means to stretch the contrast so that the dimmest color is stretched to black and
     * the brightest color is stretched to white, with all colors in between stretched out linearly.
     * “Histogram” means to stretch the contrast based on a histogram of how many colors are at
     * each brightness level on input, with the goal of producing equal number of pixels in the image
     * at each brightness level on output.  This has the effect of revealing many subtle ground features.
     * A “GammaValue” tells how much to brighten (value greater than 1.0) or dim (value less than 1.0) an image.
     * The default GammaValue is 1.0 (no change). If none of Normalize, Histogram, or GammaValue are selected
     * in a ContrastEnhancement, then no enhancement is performed.
     * @return the ContrastEnhancement
     *
     */
    public ContrastEnhancement getContrastEnhancement() {
        return contrastEnhancement;
    }

    /** The interpretation of Geometry is system-dependent, as raster data may be organized differently from feature data,
     * though omitting this element selects the default raster-data source.  Geometry-type transformations are also
     * system-dependent and it is assumed that this capability will be little used.
     * @return the name of the geometry
     *
     */
    public String getGeometryPropertyName() {
        return geometryName;
    }

    /** The ImageOutline element specifies that individual source rasters in a multi-raster set (such as a
     * set of satellite-image scenes) should be outlined with either a LineStringSymbol or PolygonSymbol.
     * It is defined as:
     * <pre>
     * &lt;xs:element name="ImageOutline"&gt;
     *   &lt;xs:complexType&gt;
     *     &lt;xs:choice&gt;
     *       &lt;xs:element ref="sld:LineSymbolizer"/&gt;
     *       &lt;xs:element ref="sld:PolygonSymbolizer"/&gt;
     *     &lt;/xs:choice&gt;
     *   &lt;/xs:complexType&gt;
     * &lt;/xs:element&gt;
     * </pre>
     * An Opacity of 0.0 can be selected for the main raster to avoid rendering the main-raster pixels, or
     * an opacity can be used for a PolygonSymbolizer Fill to allow the main-raster data be visible through
     * the fill.
     *
     * @return The relevent symbolizer
     *
     */
    public Symbolizer getImageOutline() {
        return symbolizer;
    }

    /** fetch the expresion which evaluates to the opacity fo rthis coverage
     * @return The expression
     *
     */
    public Expression getOpacity() {
        return opacity;
    }

    /** The OverlapBehavior element tells a system how to behave when multiple raster images in a layer
     * overlap each other, for example with satellite-image scenes.
     *  LATEST_ON_TOP and EARLIEST_ON_TOP refer to the time the scene was captured.
     * AVERAGE means to average multiple scenes together.
     * This can produce blurry results if the source images are not perfectly aligned in their geo-referencing.
     * RANDOM means to select an image (or piece thereof) randomly and place it on top.  This can produce crisper
     * results than AVERAGE potentially more efficiently than LATEST_ON_TOP or EARLIEST_ON_TOP.
     *
     * The default behaviour is system-dependent.
     *
     * @return The expression which evaluates to LATEST_ON_TOP, EARLIEST_ON_TOP, AVERAGE or RANDOM
     *
     */
    public Expression getOverlap() {
        return overlap;
    }

    /** The ShadedRelief element selects the application of relief shading (or “hill shading”) to an image for
     * a three-dimensional visual effect.  It is defined as:
     * Exact parameters of the shading are system-dependent (for now).  If the BrightnessOnly flag is “0”
     * (false, default), the shading is applied to the layer being rendered as the current RasterSymbol.
     * If BrightnessOnly is “1” (true), the shading is applied to the brightness of the colors in the rendering
     * canvas generated so far by other layers, with the effect of relief-shading these other layers.
     * The default for BrightnessOnly is “0” (false).  The ReliefFactor gives the amount of exaggeration to
     * use for the height of the “hills.”  A value of around 55 (times) gives reasonable results for Earth-based DEMs.
     * The default value is system-dependent.
     *
     * @return the shadedrelief object
     *
     */
    public ShadedRelief getShadedRelief() {
        return shadedRelief;
    }

    /** The ChannelSelection element specifies the false-color channel selection for a multi-spectral raster
     * source (such as a multi-band satellite-imagery source).
     * Either a channel may be selected to display in each of red, green, and blue, or a single channel
     * may be selected to display in grayscale.  (The spelling “gray” is used since it seems to be more
     * common on the Web than “grey” by a ratio of about 3:1.)
     * Contrast enhancement may be applied to each channel in isolation.  Channels are identified by a system
     * and data-dependent character identifier.  Commonly, channels will be labelled as “1”, “2”, etc.
     * @param channel the channel selected
     *
     */
    public void setChannelSelection(ChannelSelection channel) {
        channelSelction = channel;
    }

    /** The ColorMap element defines either the colors of a palette-type raster source or the mapping of
     * fixed-numeric pixel values to colors.
     * For example, a DEM raster giving elevations in meters above sea level can be translated to a colored
     * image with a ColorMap.  The quantity attributes of a color-map are used for translating between numeric
     * matrixes and color rasters and the ColorMap entries should be in order of increasing numeric quantity so
     * that intermediate numeric values can be matched to a color (or be interpolated between two colors).
     * Labels may be used for legends or may be used in the future to match character values.
     * Not all systems can support opacity in colormaps.  The default opacity is 1.0 (fully opaque).
     * Defaults for quantity and label are system-dependent.
     * @param colorMap the ColorMap for the raster
     *
     */
    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    /** The ContrastEnhancement element defines contrast enhancement for a channel of a false-color image or
     * for a color image.
     * In the case of a color image, the relative grayscale brightness of a pixel color is used.
     * “Normalize” means to stretch the contrast so that the dimmest color is stretched to black and
     * the brightest color is stretched to white, with all colors in between stretched out linearly.
     * “Histogram” means to stretch the contrast based on a histogram of how many colors are at
     * each brightness level on input, with the goal of producing equal number of pixels in the image
     * at each brightness level on output.  This has the effect of revealing many subtle ground features.
     * A “GammaValue” tells how much to brighten (value greater than 1.0) or dim (value less than 1.0) an image.
     * The default GammaValue is 1.0 (no change). If none of Normalize, Histogram, or GammaValue are selected
     * in a ContrastEnhancement, then no enhancement is performed.
     * @param cEnhancement the contrastEnhancement
     *
     */
    public void setContrastEnhancement(ContrastEnhancement cEnhancement) {
        contrastEnhancement = cEnhancement;
    }

    /** The interpretation of Geometry is system-dependent, as raster data may be organized differently from feature data,
     * though omitting this element selects the default raster-data source.  Geometry-type transformations are also
     * system-dependent and it is assumed that this capability will be little used.
     * @param geometryPropertyName the name of the Geometry
     *
     */
    public void setGeometryPropertyName(String geometryPropertyName) {
        this.geometryName = geometryPropertyName;
    }

    /** The ImageOutline element specifies that individual source rasters in a multi-raster set (such as a
     * set of satellite-image scenes) should be outlined with either a LineStringSymbol or PolygonSymbol.
     * It is defined as:
     * <pre>
     * &lt;xs:element name="ImageOutline"&gt;
     *   &lt;xs:complexType&gt;
     *     &lt;xs:choice&gt;
     *       &lt;xs:element ref="sld:LineSymbolizer"/&gt;
     *       &lt;xs:element ref="sld:PolygonSymbolizer"/&gt;
     *     &lt;/xs:choice&gt;
     *   &lt;/xs:complexType&gt;
     * &lt;/xs:element&gt;
     * </pre>
     * An Opacity of 0.0 can be selected for the main raster to avoid rendering the main-raster pixels, or
     * an opacity can be used for a PolygonSymbolizer Fill to allow the main-raster data be visible through
     * the fill.
     *
     * @param symbolizer the symbolizer to be used. If this is <B>not</B> a polygon or a line symbolizer
     * an unexpected argument exception may be thrown by an implementing class.
     *
     */
    public void setImageOutline(Symbolizer symbolizer) {
        if (symbolizer instanceof LineSymbolizer || 
                symbolizer instanceof PolygonSymbolizer) {
            this.symbolizer = symbolizer;

            return;
        }

        throw new IllegalArgumentException(
                "Only a line or polygon symbolizer may be used to outline a raster");
    }

    /** sets the opacity for the coverage, it has the usual meaning.
     * @param opacity An expression which evaluates to the the opacity (0-1)
     *
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    /** The OverlapBehavior element tells a system how to behave when multiple raster images in a layer
     * overlap each other, for example with satellite-image scenes.
     * LATEST_ON_TOP and EARLIEST_ON_TOP refer to the time the scene was captured.
     * AVERAGE means to average multiple scenes together.
     * This can produce blurry results if the source images are not perfectly aligned in their geo-referencing.
     * RANDOM means to select an image (or piece thereof) randomly and place it on top.  This can produce crisper
     * results than AVERAGE potentially more efficiently than LATEST_ON_TOP or EARLIEST_ON_TOP.
     *
     * The default behaviour is system-dependent.
     * @param overlap the expression which evaluates to LATEST_ON_TOP, EARLIEST_ON_TOP, AVERAGE or RANDOM
     *
     */
    public void setOverlap(Expression overlap) {
        this.overlap = overlap;
    }

    /** The ShadedRelief element selects the application of relief shading (or “hill shading”) to an image for
     * a three-dimensional visual effect.  It is defined as:
     * Exact parameters of the shading are system-dependent (for now).  If the BrightnessOnly flag is “0”
     * (false, default), the shading is applied to the layer being rendered as the current RasterSymbol.
     * If BrightnessOnly is “1” (true), the shading is applied to the brightness of the colors in the rendering
     * canvas generated so far by other layers, with the effect of relief-shading these other layers.
     * The default for BrightnessOnly is “0” (false).  The ReliefFactor gives the amount of exaggeration to
     * use for the height of the “hills.”  A value of around 55 (times) gives reasonable results for Earth-based DEMs.
     * The default value is system-dependent.
     * @param relief the shadedrelief object
     *
     */
    public void setShadedRelief(ShadedRelief relief) {
        shadedRelief = relief;
    }
}