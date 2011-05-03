package org.geotools.filter.v2_0.bindings;

import org.geotools.filter.v2_0.FESTestSupport;
import org.opengis.filter.And;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Within;

import com.vividsolutions.jts.geom.Polygon;

public class FilterTypeBindingTest extends FESTestSupport {

    public void testParseId() throws Exception {
        String xml = 
            "<fes:Filter xmlns:fes='http://www.opengis.net/fes/2.0'>" + 
                "<fes:ResourceId rid='InWaterA_1M.1234'/>" + 
            "</fes:Filter>";
        
        buildDocument(xml);
        Id f = (Id) parse();
        assertNotNull(f);
        
        assertEquals(1, f.getIdentifiers().size());
        assertEquals("InWaterA_1M.1234", f.getIdentifiers().iterator().next().getID());
    }
    
    public void testParseSpatial() throws Exception {
        String xml = 
            "<fes:Filter" + 
            "   xmlns:fes='http://www.opengis.net/fes/2.0' " + 
            "   xmlns:gml='http://www.opengis.net/gml/3.2' " +
            "   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " + 
            "   xsi:schemaLocation='http://www.opengis.net/fes/2.0 http://schemas.opengis.net/filter/2.0/filterAll.xsd" + 
            " http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd'> " + 
            "   <fes:Overlaps> " + 
            "      <fes:ValueReference>Geometry</fes:ValueReference> " + 
            "      <gml:Polygon gml:id='P1' srsName='urn:ogc:def:crs:EPSG::4326'> " + 
            "         <gml:exterior> " + 
            "            <gml:LinearRing> " + 
            "               <gml:posList>10 10 20 20 30 30 40 40 10 10</gml:posList> " + 
            "            </gml:LinearRing> " + 
            "         </gml:exterior> " + 
            "      </gml:Polygon> " + 
            "   </fes:Overlaps> " + 
            "</fes:Filter> ";
        buildDocument(xml);
        
        Overlaps f = (Overlaps) parse();
        assertNotNull(f);
        
        PropertyName e1 = (PropertyName) f.getExpression1();
        assertEquals("Geometry", e1.getPropertyName());
        
        Literal e2 = (Literal) f.getExpression2();
        assertTrue(e2.getValue() instanceof Polygon);
    }
    
    public void testParseLogical() throws Exception {
        String xml = 
            "<fes:Filter " + 
            "   xmlns:fes='http://www.opengis.net/fes/2.0' " + 
            "   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " + 
            "   xsi:schemaLocation='http://www.opengis.net/fes/2.0 " + 
            "   http://schemas.opengis.net/filter/2.0/filterAll.xsd'> " + 
            "   <fes:And> " + 
            "      <fes:Or> " + 
            "         <fes:PropertyIsEqualTo> " + 
            "            <fes:ValueReference>FIELD1</fes:ValueReference> " + 
            "            <fes:Literal>10</fes:Literal> " + 
            "         </fes:PropertyIsEqualTo> " + 
            "         <fes:PropertyIsEqualTo> " + 
            "            <fes:ValueReference>FIELD1</fes:ValueReference> " + 
            "            <fes:Literal>20</fes:Literal> " + 
            "         </fes:PropertyIsEqualTo> " + 
            "      </fes:Or> " + 
            "      <fes:PropertyIsEqualTo> " + 
            "         <fes:ValueReference>STATUS</fes:ValueReference> " + 
            "         <fes:Literal>VALID</fes:Literal> " + 
            "      </fes:PropertyIsEqualTo> " + 
            "   </fes:And> " + 
            "</fes:Filter> ";
        buildDocument(xml);

        And f = (And) parse();
        assertNotNull(f);
        assertEquals(2, f.getChildren().size());
        
        Or f1 = (Or) f.getChildren().get(0);
        assertEquals(2, f1.getChildren().size());
        
        PropertyIsEqualTo f11 = (PropertyIsEqualTo) f1.getChildren().get(0);
        assertEquals("FIELD1", ((PropertyName)f11.getExpression1()).getPropertyName());
        assertEquals("10", ((Literal)f11.getExpression2()).evaluate(null, String.class));
        
        PropertyIsEqualTo f12 = (PropertyIsEqualTo) f1.getChildren().get(1);
        assertEquals("FIELD1", ((PropertyName)f12.getExpression1()).getPropertyName());
        assertEquals("20", ((Literal)f12.getExpression2()).evaluate(null, String.class));
        
        PropertyIsEqualTo f2 = (PropertyIsEqualTo) f.getChildren().get(1);
        assertEquals("STATUS", ((PropertyName)f2.getExpression1()).getPropertyName());
        assertEquals("VALID", ((Literal)f2.getExpression2()).evaluate(null, String.class));
    }
    
    public void testParse1() throws Exception {
        String xml = 
            "<fes:Filter " + 
            "   xmlns:fes='http://www.opengis.net/fes/2.0' " + 
            "   xmlns:gml='http://www.opengis.net/gml/3.2' " + 
            "   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " + 
            "   xsi:schemaLocation='http://www.opengis.net/fes/2.0 " + 
            "   http://schemas.opengis.net/filter/2.0/filterAll.xsd " + 
            "   http://www.opengis.net/gml/3.2 " + 
            "   http://schemas.opengis.net/gml/3.2.1/gml.xsd'> " + 
            "   <fes:And> " + 
            "      <fes:Within> " + 
            "         <fes:ValueReference>WKB_GEOM</fes:ValueReference> " + 
            "         <gml:Polygon gml:id='P1' srsName='urn:ogc:def:crs:EPSG::4326'> " + 
            "            <gml:exterior> " + 
            "               <gml:LinearRing> " + 
            "                  <gml:posList>10 10 20 20 30 30 40 40 10 10</gml:posList> " + 
            "               </gml:LinearRing> " + 
            "            </gml:exterior> " + 
            "         </gml:Polygon> " + 
            "      </fes:Within> " + 
            "      <fes:PropertyIsBetween> " + 
            "         <fes:ValueReference>DEPTH</fes:ValueReference> " + 
            "         <fes:LowerBoundary> " + 
            "            <fes:Literal>400</fes:Literal> " + 
            "         </fes:LowerBoundary> " + 
            "         <fes:UpperBoundary> " + 
            "            <fes:Literal>800</fes:Literal> " + 
            "         </fes:UpperBoundary> " + 
            "      </fes:PropertyIsBetween> " + 
            "   </fes:And> " + 
            "</fes:Filter>"; 
        
        buildDocument(xml);

        And f = (And) parse();
        assertNotNull(f);
        assertEquals(2, f.getChildren().size());
        
        Within f1 = (Within) f.getChildren().get(0);
        assertEquals("WKB_GEOM", ((PropertyName)f1.getExpression1()).getPropertyName());
        assertTrue(f1.getExpression2().evaluate(null) instanceof Polygon);
        
        PropertyIsBetween f2 = (PropertyIsBetween) f.getChildren().get(1);
        assertEquals("DEPTH", ((PropertyName)f2.getExpression()).getPropertyName());
        assertEquals(400, f2.getLowerBoundary().evaluate(null, Integer.class).intValue());
        assertEquals(800, f2.getUpperBoundary().evaluate(null, Integer.class).intValue());
        
    }
}