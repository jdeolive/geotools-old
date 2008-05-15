/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.display.style;

// J2SE dependencies
import java.awt.Color;

// OpenGIS dependencies
import org.opengis.go.display.style.ArrowStyle;
import org.opengis.go.display.style.DashArray;
import org.opengis.go.display.style.FillPattern;
import org.opengis.go.display.style.FillStyle;
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.LineCap;
import org.opengis.go.display.style.LineJoin;
import org.opengis.go.display.style.LinePattern;
import org.opengis.go.display.style.LineStyle;
import org.opengis.go.display.style.LineSymbolizer;
import org.opengis.go.display.primitive.Graphic; // For javadoc


/**
 * Encapsulates the style data applicable to {@link Graphic}s that are of type {@code Line} in
 * the sense of SLD.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultLineSymbolizer extends DefaultGraphicStyle implements LineSymbolizer {
    /** Begin arrow style value.  */ private ArrowStyle  strokeBeginArrowStyle     = DEFAULT_STROKE_BEGIN_ARROW_STYLE;
    /** Stroke color value.       */ private Color       strokeColor               = DEFAULT_STROKE_FILL_COLOR;
    /** The dash pattern to draw. */ private DashArray   strokeDashArray           = DEFAULT_STROKE_DASH_ARRAY;
    /** Dash offset value.        */ private float       strokeDashOffset          = DEFAULT_STROKE_DASH_OFFSET;
    /** End arrow style value.    */ private ArrowStyle  strokeEndArrowStyle       = DEFAULT_STROKE_END_ARROW_STYLE;
    /** Fill background color.    */ private Color       strokeFillBackgroundColor = DEFAULT_STROKE_FILL_BACKGROUND_COLOR;
    /** Fill color value.         */ private Color       strokeFillColor           = DEFAULT_STROKE_FILL_COLOR;
    /** Fill gradient points.     */ private float[]     strokeFillGradientPoints  = DEFAULT_STROKE_FILL_GRADIENT_POINTS;
    /** Fill opacity value.       */ private float       strokeFillOpacity         = DEFAULT_STROKE_FILL_OPACITY;
    /** Fill pattern value.       */ private FillPattern strokeFillPattern         = DEFAULT_STROKE_FILL_PATTERN;
    /** Fille style value.        */ private FillStyle   strokeFillStyle           = DEFAULT_STROKE_FILL_STYLE;
    /** Way of capping line end.  */ private LineCap     strokeLineCap             = DEFAULT_STROKE_LINE_CAP;
    /** Line gap value.           */ private float       strokeLineGap             = DEFAULT_STROKE_LINE_GAP;
    /** Line join value.          */ private LineJoin    strokeLineJoin            = DEFAULT_STROKE_LINE_JOIN;
    /** Line pattern value.       */ private LinePattern strokeLinePattern         = DEFAULT_STROKE_LINE_PATTERN;
    /** Line style value.         */ private LineStyle   strokeLineStyle           = DEFAULT_STROKE_LINE_STYLE;
    /** Stroke opacity value.     */ private float       strokeOpacity             = DEFAULT_STROKE_OPACITY;
    /** Stroke width value.       */ private float       strokeWidth               = DEFAULT_STROKE_WIDTH;

    /**
     * Creates a default instance of line symbolizer.
     *
     * @todo As of GeoAPI 2.0, {@code DEFAULT_STROKE_COLOR} do not exists in
     *       the {@link LineSymbolizer} interface.
     */
    public DefaultLineSymbolizer() {
    }

    /**
     * Returns the stroke begin arrow style value.
     */
    public synchronized ArrowStyle getStrokeBeginArrowStyle() {
        return strokeBeginArrowStyle;
    }

    /**
     * Sets the stroke begin arrow style value.
     */
    public void setStrokeBeginArrowStyle(final ArrowStyle strokeBeginArrowStyle) {
        final ArrowStyle old;
        synchronized (this) {
            old = this.strokeBeginArrowStyle;
            this.strokeBeginArrowStyle = strokeBeginArrowStyle;
        }
        propertyListeners.firePropertyChange("strokeBeginArrowStyle", old, strokeBeginArrowStyle);
    }

    /**
     * Returns the stroke end arrow style value.
     */
    public synchronized ArrowStyle getStrokeEndArrowStyle() {
        return strokeEndArrowStyle;
    }

    /**
     * Sets the stroke end arrow style value.
     */
    public void setStrokeEndArrowStyle(final ArrowStyle strokeEndArrowStyle) {
        final ArrowStyle old;
        synchronized (this) {
            old = this.strokeEndArrowStyle;
            this.strokeEndArrowStyle = strokeEndArrowStyle;
        }
        propertyListeners.firePropertyChange("strokeEndArrowStyle", old, strokeEndArrowStyle);
    }

    /**
     * Returns the stroke color value. In the Geotools implementation, the
     * {@linkplain Color#getAlpha alpha channel} is set according the
     * {@linkplain #getStrokeOpacity stroke opacity} value.
     */
    public synchronized Color getStrokeColor() {
        return strokeColor = fixAlphaChannel(strokeColor, strokeOpacity);
    }

    /**
     * Sets the stroke color value.
     */
    public void setStrokeColor(final Color strokeColor) {
        final Color old;
        synchronized (this) {
            old = this.strokeColor;
            this.strokeColor = strokeColor;
        }
        propertyListeners.firePropertyChange("strokeColor", old, strokeColor);
    }

    /**
     * Returns the stroke dash array value.
     */
    public synchronized DashArray getStrokeDashArray() {
        return strokeDashArray;
    }

    /**
     * Sets the stroke dash array value.
     */
    public void setStrokeDashArray(final DashArray strokeDashArray) {
        final DashArray old;
        synchronized (this) {
            old = this.strokeDashArray;
            this.strokeDashArray = strokeDashArray;
        }
        propertyListeners.firePropertyChange("strokeDashArray", old, strokeDashArray);
    }

    /**
     * Returns the stroke dash offset value.
     */
    public synchronized float getStrokeDashOffset() {
        return strokeDashOffset;
    }

    /**
     * Sets the stroke dash offset value.
     */
    public void setStrokeDashOffset(final float strokeDashOffset) {
        final float old;
        synchronized (this) {
            old = this.strokeDashOffset;
            this.strokeDashOffset = strokeDashOffset;
        }
        propertyListeners.firePropertyChange("strokeDashOffset", new Float(old), new Float(strokeDashOffset));
    }

    /**
     * Returns the fill color value. In the Geotools implementation, the
     * {@linkplain Color#getAlpha alpha channel} is set according the
     * {@linkplain #getStrokeFillOpacity stroke fill opacity} value.
     */
    public synchronized Color getStrokeFillColor() {
        return strokeFillColor = fixAlphaChannel(strokeFillColor, strokeFillOpacity);
    }

    /**
     * Sets the fill color value.
     */
    public void setStrokeFillColor(final Color strokeFillColor) {
        final Color old;
        synchronized (this) {
            old = this.strokeFillColor;
            this.strokeFillColor = strokeFillColor;
        }
        propertyListeners.firePropertyChange("strokeFillColor", old, strokeFillColor);
    }

    /**
     * Returns the fill background color value.
     */
    public synchronized Color getStrokeFillBackgroundColor() {
        return strokeFillBackgroundColor;
    }

    /**
     * Sets the fill background color value.
     */
    public void setStrokeFillBackgroundColor(final Color strokeFillBackgroundColor) {
        final Color old;
        synchronized (this) {
            old = this.strokeFillBackgroundColor;
            this.strokeFillBackgroundColor = strokeFillBackgroundColor;
        }
        propertyListeners.firePropertyChange("strokeFillBackgroundColor", old, strokeFillBackgroundColor);
    }
    
    /**
     * Returns the fill gradient points value, or null if there is no fill gradient.
     */
    public synchronized float[] getStrokeFillGradientPoints() {
        return (float[]) strokeFillGradientPoints.clone();
    }

    /**
     * Sets the fill gradient points value.
     */
    public void setStrokeFillGradientPoints(float[] strokeFillGradientPoints) {
        strokeFillGradientPoints = (float[]) strokeFillGradientPoints.clone();
        final float[] old;
        synchronized (this) {
            old = this.strokeFillGradientPoints;
            this.strokeFillGradientPoints = strokeFillGradientPoints;
        }
        propertyListeners.firePropertyChange("strokeFillGradientPoints", old, strokeFillGradientPoints);
    }

    /**
     * Returns the fill opacity value.
     */
    public synchronized float getStrokeFillOpacity() {
        return strokeFillOpacity;
    }

    /**
     * Sets the fill opacity value.
     */
    public void setStrokeFillOpacity(final float strokeFillOpacity) {
        final float old;
        synchronized (this) {
            old = this.strokeFillOpacity;
            this.strokeFillOpacity = strokeFillOpacity;
        }
        propertyListeners.firePropertyChange("strokeFillOpacity", new Float(old), new Float(strokeFillOpacity));
    }

    /**
     * Returns the fill pattern value.
     */
    public synchronized FillPattern getStrokeFillPattern() {
        return strokeFillPattern;
    }

    /**
     * Sets the fill pattern value.
     */
    public void setStrokeFillPattern(final FillPattern strokeFillPattern) {
        final FillPattern old;
        synchronized (this) {
            old = this.strokeFillPattern;
            this.strokeFillPattern = strokeFillPattern;
        }
        propertyListeners.firePropertyChange("strokeFillPattern", old, strokeFillPattern);
    }

    /**
     * Returns the fill style value.
     */
    public synchronized FillStyle getStrokeFillStyle() {
        return strokeFillStyle;
    }

    /**
     * Sets the fill style value.
     */
    public void setStrokeFillStyle(final FillStyle strokeFillStyle) {
        final FillStyle old;
        synchronized (this) {
            old = this.strokeFillStyle;
            this.strokeFillStyle = strokeFillStyle;
        }
        propertyListeners.firePropertyChange("strokeFillStyle", old, strokeFillStyle);
    }

    /**
     * Returns the stroke line cap value.
     */
    public synchronized LineCap getStrokeLineCap() {
        return strokeLineCap;
    }

    /**
     * Sets the stroke line cap value.
     */
    public void setStrokeLineCap(final LineCap strokeLineCap) {
        final LineCap old;
        synchronized (this) {
            old = this.strokeLineCap;
            this.strokeLineCap = strokeLineCap;
        }
        propertyListeners.firePropertyChange("strokeLineCap", old, strokeLineCap);
    }

    /**
     * Returns the stroke line gap value.
     */
    public synchronized float getStrokeLineGap() {
        return strokeLineGap;
    }

    /**
     * Sets the stroke line gap value.
     */
    public void setStrokeLineGap(final float strokeLineGap) {
        final float old;
        synchronized (this) {
            old = this.strokeLineGap;
            this.strokeLineGap = strokeLineGap;
        }
        propertyListeners.firePropertyChange("strokeLineGap", new Float(old), new Float(strokeLineGap));
    }

    /**
     * Returns the stroke line join value.
     */
    public synchronized LineJoin getStrokeLineJoin() {
        return strokeLineJoin;
    }

    /**
     * Sets the stroke line join value.
     */
    public void setStrokeLineJoin(final LineJoin strokeLineJoin) {
        final LineJoin old;
        synchronized (this) {
            old = this.strokeLineJoin;
            this.strokeLineJoin = strokeLineJoin;
        }
        propertyListeners.firePropertyChange("strokeLineJoin", old, strokeLineJoin);
    }

    /**
     * Returns the stroke line pattern value.
     */
    public synchronized LinePattern getStrokeLinePattern() {
        return strokeLinePattern;
    }

    /**
     * Sets the stroke line pattern value.
     */
    public void setStrokeLinePattern(final LinePattern strokeLinePattern) {
        final LinePattern old;
        synchronized (this) {
            old = this.strokeLinePattern;
            this.strokeLinePattern = strokeLinePattern;
        }
        propertyListeners.firePropertyChange("strokeLinePattern", old, strokeLinePattern);
    }

    /**
     * Returns the stroke line style value.
     */
    public synchronized LineStyle getStrokeLineStyle() {
        return strokeLineStyle;
    }

    /**
     * Sets the stroke line style value.
     */
    public void setStrokeLineStyle(final LineStyle strokeLineStyle) {
        final LineStyle old;
        synchronized (this) {
            old = this.strokeLineStyle;
            this.strokeLineStyle = strokeLineStyle;
        }
        propertyListeners.firePropertyChange("strokeLineStyle", old, strokeLineStyle);
    }
    
    /**
     * Returns the stroke opacity value.
     */
    public synchronized float getStrokeOpacity() {
        return strokeOpacity;
    }

    /**
     * Sets the stroke opacity value.
     */
    public void setStrokeOpacity(final float strokeOpacity) {
        final float old;
        synchronized (this) {
            old = this.strokeOpacity;
            this.strokeOpacity = strokeOpacity;
        }
        propertyListeners.firePropertyChange("strokeOpacity", new Float(old), new Float(strokeOpacity));
    }

    /**
     * Returns the width value.
     */
    public synchronized float getStrokeWidth() {
        return strokeWidth;
    }
    
    /**
     * Sets the width value.
     */
    public void setStrokeWidth(final float strokeWidth) {
        final float old;
        synchronized (this) {
            old = this.strokeWidth;
            this.strokeWidth = strokeWidth;
        }
        propertyListeners.firePropertyChange("strokeWidth", new Float(old), new Float(strokeWidth));
    }

    /**
     * Sets the properties of this {@code GraphicStyle} from the properties of the specified
     * {@code GraphicStyle}.
     */
    public synchronized void setPropertiesFrom(final GraphicStyle graphicStyle) {
        acquireEventLock();
        try {
            super.setPropertiesFrom(graphicStyle);
            if (graphicStyle instanceof LineSymbolizer) {
                final LineSymbolizer ls = (LineSymbolizer) graphicStyle;
                setStrokeBeginArrowStyle    (ls.getStrokeBeginArrowStyle());
                setStrokeColor              (ls.getStrokeColor());
                setStrokeDashArray          (ls.getStrokeDashArray());
                setStrokeDashOffset         (ls.getStrokeDashOffset());
                setStrokeEndArrowStyle      (ls.getStrokeEndArrowStyle());
                setStrokeFillBackgroundColor(ls.getStrokeFillBackgroundColor());
                setStrokeFillColor          (ls.getStrokeFillColor());
                setStrokeFillGradientPoints (ls.getStrokeFillGradientPoints());
                setStrokeFillOpacity        (ls.getStrokeFillOpacity());
                setStrokeFillPattern        (ls.getStrokeFillPattern());
                setStrokeFillStyle          (ls.getStrokeFillStyle());
                setStrokeLineCap            (ls.getStrokeLineCap());
                setStrokeLineGap            (ls.getStrokeLineGap());
                setStrokeLineJoin           (ls.getStrokeLineJoin());
                setStrokeLinePattern        (ls.getStrokeLinePattern());
                setStrokeLineStyle          (ls.getStrokeLineStyle());
                setStrokeOpacity            (ls.getStrokeOpacity());
                setStrokeWidth              (ls.getStrokeWidth());
            }
        } finally {
            releaseEventLock();
        }
    }
}
