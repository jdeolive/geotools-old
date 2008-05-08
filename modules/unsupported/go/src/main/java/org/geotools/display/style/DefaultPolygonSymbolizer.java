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
import org.opengis.go.display.style.FillPattern;
import org.opengis.go.display.style.FillStyle;
import org.opengis.go.display.style.GraphicStyle;
import org.opengis.go.display.style.PolygonSymbolizer;
import org.opengis.go.display.primitive.Graphic;    // For javadoc


/**
 * Encapsulates the style data applicable to {@link Graphic}s that are of type {@code Polygon}
 * in the sense of SLD.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultPolygonSymbolizer extends DefaultLineSymbolizer implements PolygonSymbolizer {
    /** Fill background color value. */ private Color       fillBackgroundColor = DEFAULT_FILL_BACKGROUND_COLOR;
    /** Fill color value.            */ private Color       fillColor           = DEFAULT_FILL_COLOR;
    /** Fill gradient points values. */ private float[]     fillGradientPoints  = DEFAULT_FILL_GRADIENT_POINTS;
    /** Fill opacity value.          */ private float       fillOpacity         = DEFAULT_FILL_OPACITY;
    /** Fill pattern value.          */ private FillPattern fillPattern         = DEFAULT_FILL_PATTERN;
    /** Fill style value.            */ private FillStyle   fillStyle           = DEFAULT_FILL_STYLE;
    
    /**
     * Creates a default instance of polygon symbolizer.
     */
    public DefaultPolygonSymbolizer() {
    }

    /**
     * Returns the fill color RGB value. In the Geotools implementation, the
     * {@linkplain Color#getAlpha alpha channel} is set according the
     * {@linkplain #getFillOpacity fill opacity} value.
     */
    public synchronized Color getFillColor() {
        return fillColor = fixAlphaChannel(fillColor, fillOpacity);
    }

    /**
     * Sets the fill color RGB value.
     * Do not use the {@link Color} alpha channel to set fill opacity; 
     * use {@link #setFillOpacity} instead.
     */
    public void setFillColor(final Color fillColor) {
        final Color old;
        synchronized (this) {
            old = this.fillColor;
            this.fillColor = fillColor;
        }
        listeners.firePropertyChange("fillColor", old, fillColor);
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
        listeners.firePropertyChange("fillBackgroundColor", old, fillBackgroundColor);
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
        listeners.firePropertyChange("fillGradientPoints", old, fillGradientPoints);
    }

    /**
     * Returns the fill opacity value.
     */
    public synchronized float getFillOpacity() {
        return fillOpacity;
    }

    /**
     * Sets the fill opacity value. If semi-transparency is specified in both this attribute and
     * the {@linkplain Color#getAlpha alpha value} of the {@linkplain #getFillColor fill color},
     * the alpha channel value should be ignored.
     */
    public void setFillOpacity(final float fillOpacity) {
        final float old;
        synchronized (this) {
            old = this.fillOpacity;
            this.fillOpacity = fillOpacity;
        }
        listeners.firePropertyChange("fillOpacity", new Float(old), new Float(fillOpacity));
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
        listeners.firePropertyChange("fillPattern", old, fillPattern);
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
        listeners.firePropertyChange("fillStyle", old, fillStyle);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setPropertiesFrom(final GraphicStyle graphicStyle) {
        acquireEventLock();
        try {
            super.setPropertiesFrom(graphicStyle);
            if (graphicStyle instanceof PolygonSymbolizer) {
                final PolygonSymbolizer ps = (PolygonSymbolizer) graphicStyle;
                setFillBackgroundColor(ps.getFillBackgroundColor());
                setFillColor          (ps.getFillColor());
                setFillGradientPoints (ps.getFillGradientPoints());
                setFillOpacity        (ps.getFillOpacity());
                setFillPattern        (ps.getFillPattern());
                setFillStyle          (ps.getFillStyle());
            }
        } finally {
            releaseEventLock();
        }
    }
}
