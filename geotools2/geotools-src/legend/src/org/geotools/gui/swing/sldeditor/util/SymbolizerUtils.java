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
/*
 * SymbolizerUtils.java
 *
 * Created on 14 dicembre 2003, 11.55
 */
package org.geotools.gui.swing.sldeditor.util;

import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class SymbolizerUtils implements SLDEditor {
    /**
     * Creates a new instance of SymbolizerUtils
     */
    private SymbolizerUtils() {
    }

    public static SymbolizerEditor getDefaultSymbolizerEditor(FeatureType ft) {
        Class geomType = null;

        if(ft == null || ft.getDefaultGeometry() == null) {
            geomType = Polygon.class;
        } else {
            AttributeType at = ft.getDefaultGeometry();
            geomType = at.getType();
        }
        
        
        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            return symbolizerEditorFactory.createPolygonSymbolizerEditor(ft);
        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            return symbolizerEditorFactory.createLineSymbolizerEditor(ft);
        } else if (Point.class.isAssignableFrom(geomType)
                || MultiPoint.class.isAssignableFrom(geomType)) {
            return symbolizerEditorFactory.createPointSymbolizerEditor(ft);
        } else {
            throw new IllegalArgumentException("Unsupported geometry type");
        }
    }

    public static Symbolizer getDefaultSymbolizer(FeatureType ft) {
        Class geomType = null;

        if(ft == null || ft.getDefaultGeometry() == null) {
        	geomType = Polygon.class;
        } else {
        	AttributeType at = ft.getDefaultGeometry();
            geomType = at.getType();
        }
        
        if (Polygon.class.isAssignableFrom(geomType)
                || MultiPolygon.class.isAssignableFrom(geomType)) {
            return styleBuilder.createPolygonSymbolizer();
        } else if (LineString.class.isAssignableFrom(geomType)
                || MultiLineString.class.isAssignableFrom(geomType)) {
            return styleBuilder.createLineSymbolizer();
        } else if (Point.class.isAssignableFrom(geomType)
                || MultiPoint.class.isAssignableFrom(geomType)) {
            return styleBuilder.createPointSymbolizer();
        } else {
            return styleBuilder.createPointSymbolizer();
        }
    }

    public static String getSymbolizerName(Symbolizer s) {
        if (s instanceof LineSymbolizer) {
            return "Line";
        } else if (s instanceof PointSymbolizer) {
            return "Point";
        } else if (s instanceof PolygonSymbolizer) {
            return "Polygon";
        } else if (s instanceof TextSymbolizer) {
        	return "Text";
        } else {
            throw new IllegalArgumentException("Unsupported symbolizer: " + s.getClass().getName());
        }
    }

    public static SymbolizerEditor getSymbolizerEditor(Symbolizer s, FeatureType ft) {
        SymbolizerEditor symbolizerEditor = null;
        if (s instanceof LineSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createLineSymbolizerEditor(ft);
        } else if (s instanceof PointSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createPointSymbolizerEditor(ft);
        } else if (s instanceof PolygonSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createPolygonSymbolizerEditor(ft); 
        } else if (s instanceof TextSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createTextSymbolizerEditor(ft);
        } else if (s instanceof RasterSymbolizer) {
            symbolizerEditor = symbolizerEditorFactory.createRasterSymbolizerEditor(ft);
        } else {
            throw new IllegalArgumentException("Unsupported symbolizer");
        }
        symbolizerEditor.setSymbolizer(s);
        return symbolizerEditor;
    }
}
