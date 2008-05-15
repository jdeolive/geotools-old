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
import java.awt.Font;

// OpenGIS dependencies
import org.opengis.go.display.style.FillPattern;
import org.opengis.go.display.style.FillStyle;
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.TextSymbolizer;
import org.opengis.go.display.style.XAnchor;
import org.opengis.go.display.style.YAnchor;
import org.opengis.go.display.primitive.Graphic; // For javadoc


/**
 * Encapsulates the style data applicable to {@link Graphic}s
 * that are of type Text in the sense of SLD.
 * <p>
 * Note that the "fill color" of a {@code TextSymbolizer} could also be
 * called the "text color" as it is the primary color used to draw the text.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultTextSymbolizer extends DefaultGraphicStyle implements TextSymbolizer {
    /** Background color value. */ private Color       backgroundColor     = DEFAULT_FILL_BACKGROUND_COLOR;
    /** Fill background color.  */ private Color       fillBackgroundColor = DEFAULT_FILL_BACKGROUND_COLOR;
    /** Fill color value.       */ private Color       fillColor           = DEFAULT_FILL_COLOR;
    /** Fill gradient points.   */ private float[]     fillGradientPoints  = null;
    /** Fill opacity value.     */ private float       fillOpacity         = DEFAULT_FILL_OPACITY;
    /** Fill pattern value.     */ private FillPattern fillPattern         = DEFAULT_FILL_PATTERN;
    /** Fill style value.       */ private FillStyle   fillStyle           = DEFAULT_FILL_STYLE;
    /** Font value.             */ private Font        font                = DEFAULT_FONT;
    /** Halo radius value.      */ private float       haloRadius          = DEFAULT_HALO_RADIUS;
    /** Halo color value.       */ private Color       haloColor           = null;
    /** Rotation value.         */ private float       rotation            = DEFAULT_ROTATION;
    /** X anchor value.         */ private XAnchor     xAnchor             = DEFAULT_X_ANCHOR;
    /** X displacement value.   */ private float       xDisplacement       = DEFAULT_X_DISPLACEMENT;
    /** Y anchor value.         */ private YAnchor     yAnchor             = DEFAULT_Y_ANCHOR;
    /** Y displacement value.   */ private float       yDisplacement       = DEFAULT_Y_DISPLACEMENT;
    
    /**
     * Creates a default instance of text symbolizer.
     *
     * @todo As of GeoAPI 2.0, {@code DEFAULT_BACKGROUND_COLOR},
     *       {@code DEFAULT_FILL_GRADIENT_POINTS_COLOR} and {@code DEFAULT_HALO_COLOR}
     *       do not exist in the {@link TextSymbolizer} interface.
     */
    public DefaultTextSymbolizer() {
    }

    /**
     * Returns the color used to draw the text.  This is the color used to fill
     * the interior of the font glyphs.
     */
    public synchronized Color getFillColor() {
        return fillColor = fixAlphaChannel(fillColor, fillOpacity);
    }

    /**
     * {Sets the color used to draw the text.  This is the color used to fill
     * the interior of the font glyphs.
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
     * Returns the color that is used as the pattern background color when a
     * stipple pattern is used for the fill color.
     */
    public synchronized Color getFillBackgroundColor() {
        return fillBackgroundColor;
    }

    /**
     * Sets the color that is used as the pattern background color when a
     * stipple pattern is used for the fill color.
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
     * Returns the fill gradient points value, or null if there is no fill gradient.
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
     * Returns the Font object.
     */
    public synchronized Font getFont() {
        return font;
    }

    /**
     * Sets the Font object.
     */
    public void setFont(final Font font) {
        final Font old;
        synchronized (this) {
            old = this.font;
            this.font = font;
        }
        propertyListeners.firePropertyChange("font", old, font);
    }

    /**
     * Returns the halo radius value, or zero if no halo is to be drawn.
     */
    public synchronized float getHaloRadius() {
        return haloRadius;
    }

    /**
     * Sets the halo radius value.  If zero, no halo will be drawn.
     */
    public void setHaloRadius(final float haloRadius) {
        final float old;
        synchronized (this) {
            old = this.haloRadius;
            this.haloRadius = haloRadius;
        }
        propertyListeners.firePropertyChange("haloRadius", new Float(old), new Float(haloRadius));
    }

    /**
     * Returns the color that is used to fill in the halo,
     * or null if no halo is to be drawn.
     */
    public synchronized Color getHaloColor() {
        return haloColor;
    }

    /**
     * Sets the halo color.
     */
    public void setHaloColor(final Color haloColor) {
        final Color old;
        synchronized (this) {
            old = this.haloColor;
            this.haloColor = haloColor;
        }
        propertyListeners.firePropertyChange("haloColor", old, haloColor);
    }

    /**
     * Returns the color that is used to fill in a bounding box behind the text,
     * or null if no background is to be drawn using this symbolizer.
     */
    public synchronized Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the color that is used for rendering a background behind the text.
     */
    public void setBackgroundColor(final Color backgroundColor) {
        final Color old;
        synchronized (this) {
            old = this.backgroundColor;
            this.backgroundColor = backgroundColor;
        }
        propertyListeners.firePropertyChange("backgroundColor", old, backgroundColor);
    }

    /**
     * Returns the label rotation.
     */
    public synchronized float getRotation() {
        return rotation;
    }

    /**
     * Sets the label rotation.
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
     * Returns the label <var>x</var> anchor.
     */
    public synchronized XAnchor getXAnchor() {
        return xAnchor;
    }

    /**
     * Sets the label <var>x</var> anchor.
     */
    public void setXAnchor(final XAnchor xAnchor) {
        final XAnchor old;
        synchronized (this) {
            old = this.xAnchor;
            this.xAnchor = xAnchor;
        }
        propertyListeners.firePropertyChange("xAnchor", old, xAnchor);
    }

    /**
     * Returns the label <var>x</var> displacement.
     */
    public synchronized float getXDisplacement() {
        return xDisplacement;
    }

    /**
     * Sets the label <var>x</var> displacement.
     */
    public void setXDisplacement(final float xDisplacement) {
        final float old;
        synchronized (this) {
            old = this.xDisplacement;
            this.xDisplacement = xDisplacement;
        }
        propertyListeners.firePropertyChange("xDisplacement", new Float(old), new Float(xDisplacement));
    }

    /**
     * Returns the label <var>y</var> anchor.
     */
    public synchronized YAnchor getYAnchor() {
        return yAnchor;
    }

    /**
     * Sets the label <var>y</var> anchor.
     */
    public void setYAnchor(final YAnchor yAnchor) {
        final YAnchor old;
        synchronized (this) {
            old = this.yAnchor;
            this.yAnchor = yAnchor;
        }
        propertyListeners.firePropertyChange("yAnchor", old, yAnchor);
    }

    /**
     * Returns the label <var>y</var> displacement.
     */
    public synchronized float getYDisplacement() {
        return yDisplacement;
    }
    
    /**
     * Sets the label <var>y</var> displacement.
     */
    public void setYDisplacement(final float yDisplacement) {
        final float old;
        synchronized (this) {
            old = this.yDisplacement;
            this.yDisplacement = yDisplacement;
        }
        propertyListeners.firePropertyChange("yDisplacement", new Float(old), new Float(yDisplacement));
    }

    /**
     * Sets the properties of this {@code GraphicStyle} from the properties of the specified
     * {@code GraphicStyle}.
     */
    public synchronized void setPropertiesFrom(final GraphicStyle graphicStyle) {
        acquireEventLock();
        try {
            super.setPropertiesFrom(graphicStyle);
            if (graphicStyle instanceof DefaultTextSymbolizer) {
                final TextSymbolizer ts = (TextSymbolizer) graphicStyle;
                setBackgroundColor    (ts.getBackgroundColor());
                setFillBackgroundColor(ts.getFillBackgroundColor());
                setFillColor          (ts.getFillColor());
                setFillGradientPoints (ts.getFillGradientPoints());
                setFillOpacity        (ts.getFillOpacity());
                setFillPattern        (ts.getFillPattern());
                setFillStyle          (ts.getFillStyle());
                setFont               (ts.getFont());
                setHaloColor          (ts.getHaloColor());
                setHaloRadius         (ts.getHaloRadius());
                setRotation           (ts.getRotation());
                setXAnchor            (ts.getXAnchor());
                setXDisplacement      (ts.getXDisplacement());
                setYAnchor            (ts.getYAnchor());
                setYDisplacement      (ts.getYDisplacement());
            }
        } finally {
            releaseEventLock();
        }
    }
}
