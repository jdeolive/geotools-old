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
package org.geotools.styling;

import org.geotools.filter.Filter;
import org.geotools.filter.FilterAttributeExtractor;


/**
 * A simple visitor whose purpose is to extract the set of attributes used by a Style, that is,
 * those that the Style expects to find in order to work properly
 *
 * @author wolf
 */
public class StyleAttributeExtractor extends FilterAttributeExtractor implements StyleVisitor {
    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Style)
     */
    public void visit(Style style) {
        FeatureTypeStyle[] ftStyles = style.getFeatureTypeStyles();

        for (int i = 0; i < ftStyles.length; i++) {
            ftStyles[i].accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Rule)
     */
    public void visit(Rule rule) {
        Filter filter = rule.getFilter();

        if (filter != null) {
            filter.accept(this);
        }

        Symbolizer[] symbolizers = rule.getSymbolizers();

        if (symbolizers != null) {
            for (int i = 0; i < symbolizers.length; i++) {
                Symbolizer symbolizer = symbolizers[i];
                symbolizer.accept(this);
            }
        }

        Graphic[] legendGraphics = rule.getLegendGraphic();

        if (legendGraphics != null) {
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.FeatureTypeStyle)
     */
    public void visit(FeatureTypeStyle fts) {
        Rule[] rules = fts.getRules();

        for (int i = 0; i < rules.length; i++) {
            Rule rule = rules[i];
            rule.accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Fill)
     */
    public void visit(Fill fill) {
        if (fill.getBackgroundColor() != null) {
            fill.getBackgroundColor().accept(this);
        }

        if (fill.getColor() != null) {
            fill.getColor().accept(this);
        }

        if (fill.getGraphicFill() != null) {
            fill.getGraphicFill().accept(this);
        }

        if (fill.getOpacity() != null) {
            fill.getOpacity().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Stroke)
     */
    public void visit(Stroke stroke) {
        if (stroke.getColor() != null) {
            stroke.getColor().accept(this);
        }

        if (stroke.getDashOffset() != null) {
            stroke.getDashOffset().accept(this);
        }

        if (stroke.getGraphicFill() != null) {
            stroke.getGraphicFill().accept(this);
        }

        if (stroke.getGraphicStroke() != null) {
            stroke.getGraphicStroke().accept(this);
        }

        if (stroke.getLineCap() != null) {
            stroke.getLineCap().accept(this);
        }

        if (stroke.getLineJoin() != null) {
            stroke.getLineJoin().accept(this);
        }

        if (stroke.getOpacity() != null) {
            stroke.getOpacity().accept(this);
        }

        if (stroke.getWidth() != null) {
            stroke.getWidth().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Symbolizer)
     */
    public void visit(Symbolizer sym) {
        if (sym instanceof PointSymbolizer) {
            visit((PointSymbolizer) sym);
        }

        if (sym instanceof LineSymbolizer) {
            visit((LineSymbolizer) sym);
        }

        if (sym instanceof PolygonSymbolizer) {
            visit((PolygonSymbolizer) sym);
        }

        if (sym instanceof TextSymbolizer) {
            visit((TextSymbolizer) sym);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PointSymbolizer)
     */
    public void visit(PointSymbolizer ps) {
        if (ps.getGeometryPropertyName() != null) {
            attributeNames.add(ps.getGeometryPropertyName());
        }

        if (ps.getGraphic() != null) {
            ps.getGraphic().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.LineSymbolizer)
     */
    public void visit(LineSymbolizer line) {
        if (line.getGeometryPropertyName() != null) {
            attributeNames.add(line.getGeometryPropertyName());
        }

        if (line.getStroke() != null) {
            line.getStroke().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PolygonSymbolizer)
     */
    public void visit(PolygonSymbolizer poly) {
        if (poly.getGeometryPropertyName() != null) {
            attributeNames.add(poly.getGeometryPropertyName());
        }

        if (poly.getStroke() != null) {
            poly.getStroke().accept(this);
        }

        if (poly.getFill() != null) {
            poly.getFill().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.TextSymbolizer)
     */
    public void visit(TextSymbolizer text) {
        if (text.getGeometryPropertyName() != null) {
            attributeNames.add(text.getGeometryPropertyName());
        }

        if (text.getFill() != null) {
            text.getFill().accept(this);
        }

        if (text.getHalo() != null) {
            text.getHalo().accept(this);
        }

        if (text.getFonts() != null) {
            Font[] fonts = text.getFonts();

            for (int i = 0; i < fonts.length; i++) {
                Font font = fonts[i];

                if (font.getFontFamily() != null) {
                    font.getFontFamily().accept(this);
                }

                if (font.getFontSize() != null) {
                    font.getFontSize().accept(this);
                }

                if (font.getFontStyle() != null) {
                    font.getFontStyle().accept(this);
                }

                if (font.getFontWeight() != null) {
                    font.getFontWeight().accept(this);
                }
            }
        }

        if (text.getHalo() != null) {
            text.getHalo().accept(this);
        }

        if (text.getLabel() != null) {
            text.getLabel().accept(this);
        }

        if (text.getLabelPlacement() != null) {
            text.getLabelPlacement().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Graphic)
     */
    public void visit(Graphic gr) {
        if (gr.getSymbols() != null) {
            Symbol[] symbols = gr.getSymbols();

            for (int i = 0; i < symbols.length; i++) {
                Symbol symbol = symbols[i];
                symbol.accept(this);
            }
        }

        if (gr.getOpacity() != null) {
            gr.getOpacity().accept(this);
        }

        if (gr.getRotation() != null) {
            gr.getRotation().accept(this);
        }

        if (gr.getSize() != null) {
            gr.getSize().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Mark)
     */
    public void visit(Mark mark) {
        if (mark.getFill() != null) {
            mark.getFill().accept(this);
        }

        if (mark.getStroke() != null) {
            mark.getStroke().accept(this);
        }

        if (mark.getRotation() != null) {
            mark.getRotation().accept(this);
        }

        if (mark.getSize() != null) {
            mark.getSize().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.ExternalGraphic)
     */
    public void visit(ExternalGraphic exgr) {
        // nothing to do
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.PointPlacement)
     */
    public void visit(PointPlacement pp) {
        if (pp.getAnchorPoint() != null) {
            pp.getAnchorPoint().accept(this);
        }

        if (pp.getDisplacement() != null) {
            pp.getDisplacement().accept(this);
        }

        if (pp.getRotation() != null) {
            pp.getRotation().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.AnchorPoint)
     */
    public void visit(AnchorPoint ap) {
        if (ap.getAnchorPointX() != null) {
            ap.getAnchorPointX().accept(this);
        }

        if (ap.getAnchorPointY() != null) {
            ap.getAnchorPointY().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Displacement)
     */
    public void visit(Displacement dis) {
        if (dis.getDisplacementX() != null) {
            dis.getDisplacementX().accept(this);
        }

        if (dis.getDisplacementY() != null) {
            dis.getDisplacementY().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.LinePlacement)
     */
    public void visit(LinePlacement lp) {
        if (lp.getPerpendicularOffset() != null) {
            lp.getPerpendicularOffset().accept(this);
        }
    }

    /**
     * @see org.geotools.styling.StyleVisitor#visit(org.geotools.styling.Halo)
     */
    public void visit(Halo halo) {
        if (halo.getFill() != null) {
            halo.getFill().accept(this);
        }

        if (halo.getRadius() != null) {
            halo.getRadius().accept(this);
        }
    }
}
