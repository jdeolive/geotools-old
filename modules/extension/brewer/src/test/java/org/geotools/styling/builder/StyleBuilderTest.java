package org.geotools.styling.builder;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.geotools.filter.expression.ExpressionBuilder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.junit.Test;
import org.opengis.filter.expression.Expression;
import org.opengis.style.Halo;

public class StyleBuilderTest {
    @Test
    public void expression(){
        ExpressionBuilder b = new ExpressionBuilder();
        assertNotNull( b.build() );
        assertNull( b.unset().build() );
        assertEquals( Expression.NIL, b.reset().build());
        assertNotNull( b.reset().literal(1).build() );
        
        assertEquals( b.unset().literal(1).build(), b.reset().literal(1).build() );
    }
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
    @Test
    public void fill() throws Exception {
        FillBuilder b = new FillBuilder();
        Fill fill = b.color( "#0000FF" ).opacity(0.75).build();        
        assertNotNull( fill );
    }
    
    @Test
    public void halo(){
        HaloBuilder b = new HaloBuilder();
        Halo halo = b.build();
        
        assertNotNull( halo );        
    }

    /*
    public void test(){
        FeatureTypeFactory factory = CommonFactoryFinder.getFeatureTypeFactory(null);        
        
        AttributeTypeBuilder b = new AttributeTypeBuilder(factory);
        AttributeType ANY_URI = b.name("anyURI").binding(URI.class).buildType();        
        AttributeType DOUBLE = b.name("Double").binding(Double.class).buildType();
        
        AttributeDescriptor uom = b.buildDescriptor("uom", ANY_URI );
        AttributeDescriptor value = b.inline(true).buildDescriptor("value", DOUBLE );
        
        Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();
        properties.add( value );
        properties.add( uom );
        
        ComplexType MEASURE_TYPE = factory.createComplexType( new NameImpl("MeasureType"),
                properties, true, false, Collections.EMPTY_LIST, null,  null );
        
                
    }
    */
}
