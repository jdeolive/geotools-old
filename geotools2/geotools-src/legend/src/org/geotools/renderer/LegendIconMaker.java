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
 * LegendNoteIconMaker.java
 *
 * Created on 05 July 2003, 22:05
 */
package org.geotools.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFinder;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.gui.swing.sldeditor.util.StyleCloner;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * DOCUMENT ME!
 *
 * @author jianhuij
 */
public class LegendIconMaker {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.renderer");

    /**
     * for create artificial geometry feature for making legend icon since the
     * icon somehow has somekind of shape such as line, polygon etc. then
     * other code will apply fill and stroke to make an image
     */
    public static GeometryFactory gFac = new GeometryFactory();

    /**
     * if the rule already has defined legendGraphic the stylefactory could
     * create symbolizer to contain it
     */
    public static StyleFactory sFac = StyleFactory.createStyleFactory();
    public static FeatureFactory fFac;

    /**
     * offset for icon, otherwise icons will be connected to others in the
     * legend
     */
    public static int offset = 1;

    /** the current renderer object */
    private static LiteRenderer renderer = new LiteRenderer();
    private static StyleBuilder styleBuilder = new StyleBuilder();
    private static StyleCloner styleCloner = new StyleCloner(styleBuilder.getStyleFactory());

    /**
     * An icon cache that contains no more than a specified number of lastly
     * accessed entries
     */
    private static final int ICON_CACHE_SIZE = 30;
    private static Map iconCache = new LinkedHashMap(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > ICON_CACHE_SIZE;
            }
        };

    // Static initialization block
    static {
        // renderer = new Java2DRenderer();
        AttributeType[] attribs = {
            AttributeTypeFactory.newAttributeType("geometry:text",
                Geometry.class),
            AttributeTypeFactory.newAttributeType("label", String.class)
        };

        try {
            fFac = FeatureTypeFactory.newFeatureType(new AttributeType[] {
                        AttributeTypeFactory.newAttributeType("testGeometry",
                            Geometry.class)
                    }, "legend");
        } catch (SchemaException se) {
            throw new RuntimeException(se);
        }
    }

    private static int missCounter = 0;

    private LegendIconMaker() {
    }

    public static Icon makeLegendIcon(int iconWidth, Color background,
        Rule rule, Feature sample) {
        return makeLegendIcon(iconWidth, background, rule.getSymbolizers(),
            sample);
    }

    public static Icon makeLegendIcon(int iconWidth, Rule rule, Feature sample) {
        return makeLegendIcon(iconWidth, new Color(0, 0, 0, 0), rule, sample);
    }

    public static Icon makeLegendIcon(int iconWidth, Color background,
        Symbolizer[] syms, Feature sample) {
        return makeLegendIcon(iconWidth, iconWidth, background, syms, sample, true);
    }

    public static Icon makeLegendIcon(int iconWidth, int iconHeight,
        Color background, Symbolizer[] syms, Feature sample, boolean cacheIcon) {
        IconDescriptor descriptor = new IconDescriptor(iconWidth, iconHeight,
                background, syms, sample);
        Icon icon = (Icon) iconCache.get(descriptor);

        if (icon == null) {
            icon = reallyMakeLegendIcon(iconWidth, iconHeight, background,
                    syms, sample);
            if(cacheIcon) iconCache.put(descriptor, icon);
        }

        return icon;
    }

    private static Icon reallyMakeLegendIcon(int iconWidth, int iconHeight,
        Color background, Symbolizer[] symbolizers, Feature sample) {
        FeatureCollection fc = FeatureCollections.newCollection();
        
        Symbolizer[] syms = symbolizers;
        for (int i = 0; i < symbolizers.length; i++) {
            syms[i] = styleCloner.clone(syms[i]);
            if(syms[i] instanceof PolygonSymbolizer) {
                PolygonSymbolizer ps = (PolygonSymbolizer) syms[i];
                ps.setGeometryPropertyName(null);
            } if (syms[i] instanceof PointSymbolizer) {
                PointSymbolizer ps = (PointSymbolizer) syms[i];
                ps.setGeometryPropertyName(null);
            } if (syms[i] instanceof LineSymbolizer) {
                LineSymbolizer ls = (LineSymbolizer) syms[i];
                ls.setGeometryPropertyName(null);
            }
        }

        for (int i = 0; i < syms.length; i++) {
            Feature feature = null;

            if (syms[i] instanceof PolygonSymbolizer) {
                Number lineWidth = new Integer(0);
                Stroke stroke = ((PolygonSymbolizer) syms[i]).getStroke();

                if ((stroke != null) && (stroke.getWidth() != null)) {
                    lineWidth = (Number) stroke.getWidth().getValue(sample);
                }

                Coordinate[] c = new Coordinate[5];
                double marginForLineWidth = lineWidth.intValue() / 2.0d;
                c[0] = new Coordinate(offset + marginForLineWidth,
                        offset + marginForLineWidth);
                c[1] = new Coordinate(iconWidth - offset - marginForLineWidth,
                        offset + marginForLineWidth);
                c[2] = new Coordinate(iconWidth - offset - marginForLineWidth,
                        iconHeight - offset - marginForLineWidth);
                c[3] = new Coordinate(offset + marginForLineWidth,
                        iconHeight - offset - marginForLineWidth);
                c[4] = new Coordinate(offset + marginForLineWidth,
                        offset + marginForLineWidth);

                com.vividsolutions.jts.geom.LinearRing r = null;

                try {
                    r = gFac.createLinearRing(c);
                } catch (com.vividsolutions.jts.geom.TopologyException e) {
                    e.printStackTrace();
                    System.err.println("Topology Exception in GMLBox");
                }

                Polygon poly = gFac.createPolygon(r, null);
                Object[] attrib = { poly };

                try {
                    feature = fFac.create(attrib);
                } catch (IllegalAttributeException ife) {
                    throw new RuntimeException(ife);
                }

                LOGGER.fine("feature = " + feature);
            } else if (syms[i] instanceof LineSymbolizer) {
                LOGGER.fine("building line");

                Coordinate[] c = new Coordinate[2];
                c[0] = new Coordinate(offset, offset);

                //                c[1] = new Coordinate(offset + (iconWidth * 0.3), offset + (iconWidth * 0.3));
                //                c[2] = new Coordinate(offset + (iconWidth * 0.3), offset + (iconWidth * 0.7));
                //                c[3] = new Coordinate(offset + (iconWidth * 0.7), offset + (iconWidth * 0.7));
                c[1] = new Coordinate(offset + iconWidth, offset + iconHeight);

                LineString line = gFac.createLineString(c);
                Object[] attrib = { line };

                try {
                    feature = fFac.create(attrib);
                } catch (IllegalAttributeException ife) {
                    throw new RuntimeException(ife);
                }

                LOGGER.fine("feature = " + feature);
            } else if (syms[i] instanceof PointSymbolizer) {
                LOGGER.fine("building point");

                Point p = gFac.createPoint(new Coordinate(offset
                            + (iconWidth / 2.0d), offset + (iconHeight / 2.0d)));
                Object[] attrib = { p };

                try {
                    feature = fFac.create(attrib);
                } catch (IllegalAttributeException ife) {
                    throw new RuntimeException(ife);
                }

                LOGGER.fine("feature = " + feature);
            }

            if (feature != null) {
                fc.add(feature);
            }
        }

        FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle("",
                styleBuilder.createRule(syms));
        fts.setFeatureTypeName(fc.features().next().getFeatureType()
                                 .getTypeName());

        Style s = styleBuilder.createStyle();
        s.addFeatureTypeStyle(fts);

        ImageIcon icon = null;

        BufferedImage image = new BufferedImage(iconWidth, iconHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setBackground(background);
        graphics.setColor(background);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        // set the output area and graphics
        renderer.setConcatTransforms(true);
        renderer.setOutput(graphics,
            new java.awt.Rectangle(0, 0, image.getWidth(), image.getHeight()));
        renderer.render(fc, new Envelope(0, iconWidth, 0, iconWidth), s);
        icon = new ImageIcon(image);

        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            HashMap params1 = new HashMap();
            params1.put("url",
                new File("u:/work/MMPPAS/step1/testdata/geog/eds_region.shp").toURL()
                                                                             .toString());

            DataSource datasource1 = DataSourceFinder.getDataSource(params1);
            int width = UIManager.getIcon("Tree.openIcon").getIconWidth();
            Expression fcolor1 = FilterFactory.createFilterFactory()
                                              .createLiteralExpression(Color.PINK
                    .getRGB() + "");
            Expression fcolor2 = FilterFactory.createFilterFactory()
                                              .createLiteralExpression(Color.BLACK
                    .getRGB() + "");
            Expression lineWidth = FilterFactory.createFilterFactory()
                                                .createLiteralExpression(3.0);
            Fill fill = StyleFactory.createStyleFactory().createFill(fcolor1);
            Stroke stroke = StyleFactory.createStyleFactory().createStroke(fcolor2,
                    lineWidth);
            PolygonSymbolizer polySymbolizer = StyleFactory.createStyleFactory()
                                                           .createPolygonSymbolizer(stroke,
                    fill, "testGeometry");
            JLabel iconJLabel = new JLabel("Legend Icon Example");
            iconJLabel.setIcon(LegendIconMaker.makeLegendIcon(width,
                    new Color(0, 0, 0, 0), new Symbolizer[] { polySymbolizer },
                    (Feature) datasource1.getFeatures().toArray()[0]));

            JFrame f = new JFrame();
            f.getContentPane().setBackground(new Color(204, 204, 255));
            f.getContentPane().add(iconJLabel);
            f.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class IconDescriptor {
        private int iconHeight;
        private int iconWidth;
        private Color background;
        private Symbolizer[] symbolizers;
        private Feature sample;

        public IconDescriptor(int iconWidth, int iconHeight, Color background,
            Symbolizer[] symbolizers, Feature sample) {
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
            this.background = background;
            this.symbolizers = symbolizers;
            this.sample = sample;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof IconDescriptor)) {
                return false;
            }

            IconDescriptor other = (IconDescriptor) obj;

            if ((other.iconWidth != iconWidth)
                    || (other.iconHeight != iconHeight)) {
                return false;
            }

            if (!((background == null && other.background == null) || other.background.equals(background))) {
                return false;
            }

            if (((symbolizers == null) && (other.symbolizers != null))
                    || ((symbolizers != null) && (other.symbolizers == null))
                    || (symbolizers.length != other.symbolizers.length)) {
                return false;
            }

            for (int i = 0; i < symbolizers.length; i++) {
                if (!symbolizers[i].equals(other.symbolizers[i])) {
                    return false;
                }
            }

            return (((sample == null) && (other.sample == null))
            || ((sample != null) && (other.sample != null)
            && sample.equals(other.sample)));
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return ((((((((17 + symbolizersHashCode()) * 37) + iconWidth) * 37)
            + iconHeight) * 37) + (background != null ? background.hashCode() : 0)) * 37)
            + (sample == null ? 0 : sample.hashCode());
        }
        
        private int symbolizersHashCode() {
        	int hash = 17;
        	for(int i = 0; i < symbolizers.length; i++) {
        		hash = (hash + symbolizers[i].hashCode()) * 37;  
        	}
        	return hash;
        }
    }
}
