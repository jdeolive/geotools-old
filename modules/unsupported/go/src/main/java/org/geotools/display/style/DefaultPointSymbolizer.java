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
import org.opengis.go.display.style.Mark;
import org.opengis.go.display.style.PointSymbolizer;
import org.opengis.go.display.primitive.Graphic;   // For javadoc


/**
 * Encapsulates the point and mark attributes that can be applied to any point
 * {@link org.opengis.go.display.primitive.Graphic}.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @todo Should the {@code PointSymbolizer} interface contains {@code getStrokeOpacity()}
 *       and {@code setStrokeOpacity(strokeOpacity)} as well?
 */
public class DefaultPointSymbolizer extends DefaultGraphicStyle implements PointSymbolizer {
    /** Fill background color.   */ private Color       fillBackgroundColor       = DEFAULT_FILL_BACKGROUND_COLOR;
    /** Fill color value.        */ private Color       fillColor                 = DEFAULT_FILL_COLOR;
    /** Fill gradient points.    */ private float[]     fillGradientPoints        = DEFAULT_FILL_GRADIENT_POINTS;
    /** Fill opacity value.      */ private float       fillOpacity               = DEFAULT_FILL_OPACITY;
    /** Fill pattern value.      */ private FillPattern fillPattern               = DEFAULT_FILL_PATTERN;
    /** Fill style value.        */ private FillStyle   fillStyle                 = DEFAULT_FILL_STYLE;
    /** Mark value.              */ private Mark        mark                      = DEFAULT_MARK;
    /** Opacity value.           */ private float       opacity                   = DEFAULT_OPACITY;
    /** Rotation value.          */ private float       rotation                  = DEFAULT_ROTATION;
    /** Size value.              */ private float       size                      = DEFAULT_SIZE;
    /** Begin arrow style.       */ private ArrowStyle  strokeBeginArrowStyle     = DEFAULT_STROKE_BEGIN_ARROW_STYLE;
    /** Stroke color value.      */ private Color       strokeColor               = DEFAULT_STROKE_COLOR;
    /** Stroke dash array.       */ private DashArray   strokeDashArray           = DEFAULT_STROKE_DASH_ARRAY;
    /** Stroke dash offset.      */ private float       strokeDashOffset          = DEFAULT_STROKE_DASH_OFFSET;
    /** End arrow style.         */ private ArrowStyle  strokeEndArrowStyle       = DEFAULT_STROKE_END_ARROW_STYLE;
    /** Stroke background color. */ private Color       strokeFillBackgroundColor = DEFAULT_STROKE_FILL_BACKGROUND_COLOR;
    /** Stroke fill color.       */ private Color       strokeFillColor           = DEFAULT_STROKE_FILL_COLOR;
    /** Stroke gradient points.  */ private float[]     strokeFillGradientPoints  = DEFAULT_STROKE_FILL_GRADIENT_POINTS;
    /** Stroke fill opacity.     */ private float       strokeFillOpacity         = DEFAULT_STROKE_FILL_OPACITY;
    /** Stroke fill pattern.     */ private FillPattern strokeFillPattern         = DEFAULT_STROKE_FILL_PATTERN;
    /** Stroke fill style.       */ private FillStyle   strokeFillStyle           = DEFAULT_STROKE_FILL_STYLE;
    /** Stroke line cap.         */ private LineCap     strokeLineCap             = DEFAULT_STROKE_LINE_CAP;
    /** Stroke line gap.         */ private float       strokeLineGap             = DEFAULT_STROKE_LINE_GAP;
    /** Stroke line join.        */ private LineJoin    strokeLineJoin            = DEFAULT_STROKE_LINE_JOIN;
    /** Stroke line pattern.     */ private LinePattern strokeLinePattern         = DEFAULT_STROKE_LINE_PATTERN;
    /** Stroke line style.       */ private LineStyle   strokeLineStyle           = DEFAULT_STROKE_LINE_STYLE;
    /** Stroke width value.      */ private float       strokeWidth               = DEFAULT_STROKE_WIDTH;
    
    /**
     * Creates a default instance of point symbolizer.
     */
    public DefaultPointSymbolizer() {
    }

    /**
     * Returns the fill color value.
     */
    public synchronized Color getFillColor() {
        return fillColor = fixAlphaChannel(fillColor, fillOpacity);
    }

    /**
     * Sets the fill color value.
     */
    public void setFillColor(final Color fillColor) {
        final Color old;
        synchronized (this) {
            old = this.fillColor;
            this.fillColor = fillColor;
        }
        propertyListeners.firePropertyChange("fillColor", old, fillColor);
    }

    /**
     * Returns the fill background color value.
     */
    public synchronized Color getFillBackgroundColor() {
        return fillBackgroundColor;
    }
    
    /**
     * Sets the fill background color value.
     */
    public void setFillBackgroundColor(final Color fillBackgroundColor) {
        final Color old;
        synchronized (this) {
            old = this.fillBackgroundColor;
            this.fillBackgroundColor = fillBackgroundColor;
        }
        propertyListeners.firePropertyChange("fillBackgroundColor", old, fillBackgroundColor);
    }

    /**
     * Returns the fill gradient points value.
     */
    public synchronized float[] getFillGradientPoints() {
        return fillGradientPoints;
    }

    /**
     * Sets the fill gradient points value.
     */
    public void setFillGradientPoints(final float[] fillGradientPoints) {
        final float[] old;
        synchronized (this) {
            old = this.fillGradientPoints;
            this.fillGradientPoints = fillGradientPoints;
        }
        propertyListeners.firePropertyChange("fillGradientPoints", old, fillGradientPoints);
    }

    /**
     * Returns the fill opacity value.
     */
    public synchronized float getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Sets the fill opacity value.
     */
    public void setFillOpacity(final float fillOpacity) {
        final float old;
        synchronized (this) {
            old = this.fillOpacity;
            this.fillOpacity = fillOpacity;
        }
        propertyListeners.firePropertyChange("fillOpacity", new Float(old), new Float(fillOpacity));
    }

    /**
     * Returns the fill pattern value.
     */
    public synchronized FillPattern getFillPattern() {
        return fillPattern;
    }

    /**
     * Sets the fill pattern value.
     */
    public void setFillPattern(final FillPattern fillPattern) {
        final FillPattern old;
        synchronized (this) {
            old = this.fillPattern;
            this.fillPattern = fillPattern;
        }
        propertyListeners.firePropertyChange("fillPattern", old, fillPattern);
    }

    /**
     * Returns the fill style value.
     */
    public synchronized FillStyle getFillStyle() {
        return fillStyle;
    }

    /**
     * Sets the fill style value.
     */
    public void setFillStyle(final FillStyle fillStyle) {
        final FillStyle old;
        synchronized (this) {
            old = this.fillStyle;
            this.fillStyle = fillStyle;
        }
        propertyListeners.firePropertyChange("fillStyle", old, fillStyle);
    }

    /**
     * Returns the point mark value.
     */
    public synchronized Mark getMark() {
        return mark;
    }

    /**
     * Sets the point mark value.
     */
    public void setMark(final Mark mark) {
        final Mark old;
        synchronized (this) {
            old = this.mark;
            this.mark = mark;
        }
        propertyListeners.firePropertyChange("mark", old, mark);
    }

    /**
     * Returns the point rotation value.
     */
    public synchronized float getRotation() {
        return rotation;
    }

    /**
     * Sets the point rotation value.
     */
    public void setRotation(final float rotation) {
        final float old;
        synchronized (this) {
            old = this.rotation;
            this.rotation = rotation;
        }
        propertyListeners.firePropertyChange("rotation", new Float(old), new Float(rotation));
    }

    /**
     * Returns the point size value.
     */
    public synchronized float getSize() {
        return size;
    }
    
    /**
     * Sets the point size value.
     */
    public void setSize(final float size) {
        final float old;
        synchronized (this) {
            old = this.size;
            this.size = size;
        }
        propertyListeners.firePropertyChange("size", new Float(old), new Float(size));
    }

    /**
     * Returns the point opacity value.
     */
    public synchronized float getOpacity() {
        return opacity;
    }

    /**
     * Sets the point opacity value.
     */
    public void setOpacity(final float opacity) {
        final float old;
        synchronized (this) {
            old = this.opacity;
            this.opacity = opacity;
        }
        propertyListeners.firePropertyChange("opacity", new Float(old), new Float(opacity));
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
     * Returns the stroke color value.
     */
    public synchronized Color getStrokeColor() {
        return strokeColor;
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
     * Returns the stroke fill color value.
     */
    public synchronized Color getStrokeFillColor() {
        return strokeFillColor = fixAlphaChannel(strokeFillColor, strokeFillOpacity);
    }

    /**
     * Sets the stroke fill color value.
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
     * Returns the stroke fill background color value.
     */
    public synchronized Color getStrokeFillBackgroundColor() {
        return strokeFillBackgroundColor;
    }

    /**
     * Sets the stroke fill background color value.
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
     * Returns the stroke fill gradient points value.
     */
    public synchronized float[] getStrokeFillGradientPoints() {
        return strokeFillGradientPoints;
    }

    /**
     * Sets the stroke fill gradient points value.
     */
    public void setStrokeFillGradientPoints(final float[] strokeFillGradientPoints) {
        final float[] old;
        synchronized (this) {
            old = this.strokeFillGradientPoints;
            this.strokeFillGradientPoints = strokeFillGradientPoints;
        }
        propertyListeners.firePropertyChange("strokeFillGradientPoints", old, strokeFillGradientPoints);
    }

    /**
     * Returns the stroke fill opacity value.
     */
    public synchronized float getStrokeFillOpacity() {
        return strokeFillOpacity;
    }

    /**
     * Sets the stroke fill opacity value.
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
     * Returns the stroke fill pattern value.
     */
    public synchronized FillPattern getStrokeFillPattern() {
        return strokeFillPattern;
    }

    /**
     * Sets the stroke fill pattern value.
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
     * Returns the stroke fill style value.
     */
    public synchronized FillStyle getStrokeFillStyle() {
        return strokeFillStyle;
    }

    /**
     * Sets the stroke fill style value.
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
     * Returns the stroke width value.
     */
    public synchronized float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Sets the stroke width value.
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
            if (graphicStyle instanceof DefaultPointSymbolizer) {
                final PointSymbolizer ps = (PointSymbolizer) graphicStyle;
                setFillBackgroundColor      (ps.getFillBackgroundColor());
                setFillColor                (ps.getFillColor());
                setFillGradientPoints       (ps.getFillGradientPoints());
                setFillOpacity              (ps.getFillOpacity());
                setFillPattern              (ps.getFillPattern());
                setFillStyle                (ps.getFillStyle());
                setMark                     (ps.getMark());
                setOpacity                  (ps.getOpacity());
                setRotation                 (ps.getRotation());
                setSize                     (ps.getSize());
                setStrokeBeginArrowStyle    (ps.getStrokeBeginArrowStyle());
                setStrokeColor              (ps.getStrokeColor());
                setStrokeDashArray          (ps.getStrokeDashArray());
                setStrokeDashOffset         (ps.getStrokeDashOffset());
                setStrokeEndArrowStyle      (ps.getStrokeEndArrowStyle());
                setStrokeFillBackgroundColor(ps.getStrokeFillBackgroundColor());
                setStrokeFillColor          (ps.getStrokeFillColor());
                setStrokeFillGradientPoints (ps.getStrokeFillGradientPoints());
                setStrokeFillOpacity        (ps.getStrokeFillOpacity());
                setStrokeFillPattern        (ps.getStrokeFillPattern());
                setStrokeFillStyle          (ps.getStrokeFillStyle());
                setStrokeLineCap            (ps.getStrokeLineCap());
                setStrokeLineGap            (ps.getStrokeLineGap());
                setStrokeLineJoin           (ps.getStrokeLineJoin());
                setStrokeLinePattern        (ps.getStrokeLinePattern());
                setStrokeLineStyle          (ps.getStrokeLineStyle());
                setStrokeWidth              (ps.getStrokeWidth());
            }
        } finally {
            releaseEventLock();
        }
    }
}
