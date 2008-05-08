package org.geotools.renderer.lite;

import java.awt.Color;

import junit.framework.TestCase;

import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 * @author  pc
 */
public class RenderingBufferExtractorTest extends TestCase {
    StyleBuilder sb = new StyleBuilder();

    public void testNoStroke() {
        Style style = sb.createStyle(sb.createTextSymbolizer());
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        assertEquals(0, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
        rbe.visit(style);
        assertEquals(0, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testSimpleStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(10.0)));
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(10, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testSimpleGraphic() {
        PointSymbolizer ps = sb.createPointSymbolizer(sb.createGraphic(null, sb
                .createMark(sb.MARK_CIRCLE), null));
        ps.getGraphic().setSize(sb.literalExpression(15));
        Style style = sb.createStyle(ps);

        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(15, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }
    
    public void testNpePreventionGraphic() {
        PointSymbolizer ps = sb.createPointSymbolizer(sb.createGraphic(null, sb
                .createMark(sb.MARK_CIRCLE), null));
        ps.getGraphic().setSize(sb.literalExpression(null));
        Style style = sb.createStyle(ps);

        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(0, rbe.getBuffer());
        assertFalse(rbe.isEstimateAccurate());
    }

    public void testNonIntegerStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(10.8)));
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(11, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testMultiSymbolizers() {
        Symbolizer ls = sb.createLineSymbolizer(sb.createStroke(10.8));
        Symbolizer ps = sb.createPolygonSymbolizer(sb.createStroke(12), sb.createFill());
        Rule r = sb.createRule(new Symbolizer[] { ls, ps });
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(r);
        assertEquals(12, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }

    public void testPropertyWidth() {
        Symbolizer ls = sb.createLineSymbolizer(sb.createStroke(sb.colorExpression(Color.BLACK), sb
                .attributeExpression("gimbo")));
        Symbolizer ps = sb.createPolygonSymbolizer(sb.createStroke(12), sb.createFill());
        Rule r = sb.createRule(new Symbolizer[] { ls, ps });
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(r);
        assertEquals(12, rbe.getBuffer());
        assertTrue(!rbe.isEstimateAccurate());
    }

    public void testLiteralParseStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(sb
                .colorExpression(Color.BLACK), sb.literalExpression("10.0"))));
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(10, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }
    
    public void testNpePreventionStroke() {
        Style style = sb.createStyle(sb.createLineSymbolizer(sb.createStroke(sb
                .colorExpression(Color.BLACK), sb.literalExpression(null))));
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(style);
        assertEquals(0, rbe.getBuffer());
        assertFalse(rbe.isEstimateAccurate());
    }
    
    public void testLiteralParseGraphics() {
        Graphic g = sb.createGraphic();
        g.setSize(sb.literalExpression("10.0"));
        MetaBufferEstimator rbe = new MetaBufferEstimator();
        rbe.visit(g);
        assertEquals(10, rbe.getBuffer());
        assertTrue(rbe.isEstimateAccurate());
    }
}
