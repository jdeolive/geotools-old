/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.renderer.style;

import org.geotools.feature.Feature;
import org.geotools.styling.Fill;
import org.geotools.styling.PolygonSymbolizer;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;


/**
 * A dynamic polygon style, that will compute its parameters each time they are requested instead
 * of caching them
 *
 * @author jamesm
 */
public class DynamicPolygonStyle2D extends org.geotools.renderer.style.PolygonStyle2D {
    Feature feature;
    PolygonSymbolizer ps;

    /**
     * Creates a new instance of DynamicPolygonStyle2D
     */
    public DynamicPolygonStyle2D(Feature f, PolygonSymbolizer sym) {
        feature = f;
        ps = sym;
    }

    /**
     * Computes and returns the fill based on the feature and the symbolizer
     */
    public java.awt.Paint getFill() {
        Fill fill = ps.getFill();

        if (fill == null) {
            return null;
        }

        Paint fillPaint = Color.decode((String) fill.getColor().getValue(feature));

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            SLDStyleFactory fac = new SLDStyleFactory();
            fillPaint = fac.getTexturePaint(gr, feature);
        }

        return fillPaint;
    }

    /**
     * Computes and returns the fill composite based on the feature and the symbolizer
     */
    public Composite getFillComposite() {
        Fill fill = ps.getFill();

        if (fill == null) {
            return null;
        }

        // get the opacity and prepare the composite
        float opacity = ((Number) fill.getOpacity().getValue(feature)).floatValue();

        if (opacity == 1) {
            return null;
        }

        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
    }
}
