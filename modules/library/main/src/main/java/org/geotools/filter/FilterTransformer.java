/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.filter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.geotools.gml.producer.GeometryTransformer;
import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An XMLEncoder for Filters and Expressions.
 *
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 *
 */
public class FilterTransformer extends TransformerBase {
    
    /** The namespace to use if none is provided. */
    private static String defaultNamespace = "http://www.opengis.net/ogc";

    /** Map of comparison types to sql representation */
    private static Map comparisions = new HashMap();
    
    /** Map of spatial types to sql representation */
    private static Map spatial = new HashMap();
    
    /** Map of logical types to sql representation */
    private static Map logical = new HashMap();
    
    /** Map of expression types to sql representation */
    private static Map expressions = new HashMap();
    
    static {
        comparisions.put(new Integer(FilterType.COMPARE_EQUALS),
        "PropertyIsEqualTo");
        comparisions.put(new Integer(FilterType.COMPARE_GREATER_THAN),
        "PropertyIsGreaterThan");
        comparisions.put(new Integer(FilterType.COMPARE_GREATER_THAN_EQUAL),
        "PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(FilterType.COMPARE_LESS_THAN),
        "PropertyIsLessThan");
        comparisions.put(new Integer(FilterType.COMPARE_LESS_THAN_EQUAL),
        "PropertyIsLessThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.LIKE), "PropertyIsLike");
        comparisions.put(new Integer(AbstractFilter.NULL), "PropertyIsNull");
        comparisions.put(new Integer(AbstractFilter.BETWEEN),
        "PropertyIsBetween");
        
        expressions.put(new Integer(DefaultExpression.MATH_ADD), "Add");
        expressions.put(new Integer(DefaultExpression.MATH_DIVIDE), "Div");
        expressions.put(new Integer(DefaultExpression.MATH_MULTIPLY), "Mul");
        expressions.put(new Integer(DefaultExpression.MATH_SUBTRACT), "Sub");
        expressions.put(new Integer(DefaultExpression.FUNCTION), "Function");
        
        //more to come
        spatial.put(new Integer(AbstractFilter.GEOMETRY_EQUALS), "Equals");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_DISJOINT), "Disjoint");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_INTERSECTS),
        "Intersects");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_TOUCHES), "Touches");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CROSSES), "Crosses");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_WITHIN), "Within");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_CONTAINS), "Contains");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_OVERLAPS), "Overlaps");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BEYOND), "Beyond");
        spatial.put(new Integer(AbstractFilter.GEOMETRY_BBOX), "BBOX");
        
        logical.put(new Integer(AbstractFilter.LOGIC_AND), "And");
        logical.put(new Integer(AbstractFilter.LOGIC_OR), "Or");
        logical.put(new Integer(AbstractFilter.LOGIC_NOT), "Not");
    }
    
    /**
     * A typed convenience method for converting a Filter into XML.
     */
    public String transform(Filter f) throws TransformerException {
        return super.transform(f);
    }
    
    public org.geotools.xml.transform.Translator createTranslator(ContentHandler handler) {
        return new FilterTranslator(handler);
    }    
    
    public static class FilterTranslator extends TranslatorSupport implements FilterVisitor {
        
        GeometryTransformer.GeometryTranslator geometryEncoder;
        
        public FilterTranslator(ContentHandler handler) {
            super(handler, "ogc" ,defaultNamespace);
 
            geometryEncoder = new GeometryTransformer.GeometryTranslator(handler);
            
            addNamespaceDeclarations(geometryEncoder);
        }
        
        public void visit(org.geotools.filter.LogicFilter filter) {
            filter.getFilterType();
            
            String type = (String) logical.get(new Integer(filter.getFilterType()));
            
            start(type);
            
            java.util.Iterator list = filter.getFilterIterator();
            
            while (list.hasNext()) {
                Filters.accept((org.opengis.filter.Filter)list.next(),this);
            }
            
            end(type);
        }
        
        public void visit(org.geotools.filter.NullFilter filter) {
            Expression expr = (Expression) filter.getNullCheckValue();
            
            String type = (String) comparisions.get(new Integer(
            filter.getFilterType()));
            
            start(type);
            expr.accept(this);
            end(type);
            
        }
        
        public void visit(org.geotools.filter.FidFilter filter) {
            String[] fids = filter.getFids();
            
            for (int i = 0; i < fids.length; i++) {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "fid", "fid", "", fids[i]);
                element("FeatureId",null,atts);
            }
        }
        
        public void visit(Filter filter) {
            try {
                contentHandler.startElement("", "!--","!--", NULL_ATTS);
                chars("Unidentified Filter " + filter.getClass());
                contentHandler.endElement("","--","--");
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
            
        }
        
        public void visit(org.geotools.filter.LikeFilter filter) {
            String wcm = filter.getWildcardMulti();
            String wcs = filter.getWildcardSingle();
            String esc = filter.getEscape();
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "wildCard", "wildCard", "", wcm);
            atts.addAttribute("", "singleChar", "singleChar", "", wcs);
            atts.addAttribute("", "escape", "escape", "", esc);
            start("PropertyIsLike",atts);
            encode( filter.getValue() );
            element("Literal",filter.getPattern());
            end("PropertyIsLike");
        }
        
        public void visit(org.geotools.filter.BetweenFilter filter) {
            Expression left = (Expression) filter.getLeftValue();
            Expression right = (Expression) filter.getRightValue();
            Expression mid = (Expression) filter.getMiddleValue();
            
            String type = (String) comparisions.get(new Integer(filter.getFilterType()));
            
            start(type);
            mid.accept(this);
            start("LowerBoundary");
            left.accept(this);
            end("LowerBoundary");
            start("UpperBoundary");
            right.accept(this);
            end("UpperBoundary");
            end(type);
            
        }
        
        public void visit(org.geotools.filter.AttributeExpression expression) {
            element("PropertyName",expression.getAttributePath());
        }
        
        public void visit(org.geotools.filter.MathExpression expression) {
            String type = (String) expressions.get(new Integer(expression.getType()));
            start(type);
            encode(expression.getLeftValue());
            encode(expression.getRightValue());
            end(type);
        }
        
        public void visit(org.geotools.filter.FunctionExpression expression) {
            String type = (String) expressions.get(new Integer(expression.getType()));

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "name", "", expression.getName());
            start(type,atts);

            Expression[] args = expression.getArgs();

            for (int i = 0; i < args.length; i++) {
                args[i].accept(this);
            }

            end(type);
        }
        
        public void visit(org.geotools.filter.CompareFilter filter) {
            Expression left = filter.getLeftValue();
            Expression right = filter.getRightValue();
            
            String type = (String) comparisions.get(new Integer(
            filter.getFilterType()));
            
            start(type);
            left.accept(this);
            right.accept(this);
            end(type);
        }
        
        public void visit(org.geotools.filter.GeometryFilter filter) {
            Expression left = filter.getLeftGeometry();
            Expression right = filter.getRightGeometry();
            String type = (String) spatial.get(new Integer(filter.getFilterType()));
            
            start(type);
            left.accept(this);
            right.accept(this);
            end(type);
        }
        
        public void visit(Expression expression) {
        }
        
        public void visit(org.geotools.filter.LiteralExpression expression) {
            Object value = expression.getLiteral();

            if (Geometry.class.isAssignableFrom(value.getClass())) {
                geometryEncoder.encode( (Geometry) value );
            } else {
                element("Literal",value.toString());
            }
        }
        
        public void encode(Expression e) {
            e.accept(this);
        }
        
        public void encode(Filter f) {
            start("Filter");
            f.accept(this);
            end("Filter");
        }
        
        public void encode(Object o) {
            if (o instanceof Filter)
                encode( (Filter) o);
            else if (o instanceof Expression)
                encode( (Expression) o);
            else 
                throw new IllegalArgumentException("Cannot encode " + (o == null ? "null" : o.getClass().getName()));
        }
        
    }
    
}
