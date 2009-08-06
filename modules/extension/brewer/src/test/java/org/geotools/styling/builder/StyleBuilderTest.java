package org.geotools.styling.builder;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.junit.Test;
import static org.junit.Assert.*;

public class StyleBuilderTest {
	
	@Test
	public void allDefaults() {
		Style style = new StyleBuilder().build();
		
		assertNotNull(style);
		assertNull(style.getName());
		assertEquals(1, style.featureTypeStyles().size());
		
		FeatureTypeStyle fts = style.featureTypeStyles().get(0);
		assertNotNull(fts);
		assertEquals(0, fts.featureTypeNames().size());
		assertEquals(1, fts.rules().size());
		
		Rule r = fts.rules().get(0);
		assertNotNull(r);
		assertNull(r.getName());
		assertEquals(1, r.symbolizers().size());
		
		PointSymbolizer ps = (PointSymbolizer) r.symbolizers().get(0);
		assertNull(ps.getGeometryPropertyName());
		assertEquals(1, ps.getGraphic().graphicalSymbols().size());
		
		Mark mark = (Mark) ps.getGraphic().graphicalSymbols().get(0);
		assertEquals("square", mark.getWellKnownName().evaluate(null));
	}
	
	

}
