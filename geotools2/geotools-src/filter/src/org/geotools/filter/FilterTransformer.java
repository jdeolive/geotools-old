/*
 * SLDTransformer.java
 *
 * Created on October 17, 2003, 1:51 PM
 */

package org.geotools.filter;

import com.vividsolutions.jts.geom.Geometry;
import java.io.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.geotools.filter.*;
import org.geotools.gml.producer.GeometryTransformer;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Producer Filter to an output stream.
 * @todo This class should not be an XMLReader, rather it should use an internal
 * XMLReader class as this causes confusion with the API.
 *
 * @author Ian Schneider
 *
 */
public class FilterTransformer extends XMLFilterImpl implements XMLReader {
    
    static Attributes NULL_ATTS = new AttributesImpl();
    
    private int indent = 4;
    
    /** The namespace to use if none is provided. */
    private String defaultNamespace = "http://www.opengis.net/ogc";
    
    private String prefix = null;
    
    private Object object;
    
    private boolean prettyPrint = false;

    /** Map of comparison types to sql representation */
    private static Map comparisions = new HashMap();
    
    /** Map of spatial types to sql representation */
    private static Map spatial = new HashMap();
    
    /** Map of logical types to sql representation */
    private static Map logical = new HashMap();
    
    /** Map of expression types to sql representation */
    private static Map expressions = new HashMap();
    
    static {
        comparisions.put(new Integer(AbstractFilter.COMPARE_EQUALS),
        "PropertyIsEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN),
        "PropertyIsGreaterThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL),
        "PropertyIsGreaterThanOrEqualTo");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN),
        "PropertyIsLessThan");
        comparisions.put(new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL),
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
     * Sets a default namespace to use.
     *
     * @param namespace the namespace to use, should be a uri.
     */
    public void setDefaultNamespace(String namespace) {
        this.defaultNamespace = namespace;
    }
    
    /**
     * Sets if newlines and indents should be used for printing.
     *
     * @param pp true if pretty printing is desired.
     */
    public void setPrettyPrint(boolean pp) {
        prettyPrint = pp;
    }
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setIndent(int indent) {
        this.indent = indent;
    }
    
    /**
     * performs the sending of sax events from the passed in  feature
     * collection.
     *
     * @param collection the collection to turn to gml.
     * @param out the stream to send the output to.
     *
     * @throws TransformerException DOCUMENT ME!
     */
    public synchronized void transform(Object object, OutputStream out)
    throws TransformerException {
        transform(object,new StreamResult(out));
    }
    
    public synchronized void transform(Object object, Writer out)
    throws TransformerException {
        transform(object,new StreamResult(out));
    }
    
    public synchronized void transform(Object object,StreamResult result)
    throws TransformerException {
        this.object = object;
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        
        if (prettyPrint) {
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent + "");
        }
        
        InputSource inputSource = new InputSource();
        SAXSource source = new SAXSource(this, inputSource);
        transformer.transform(source, result);
    }
    
    public static OutputVisitor createOutputVisitor(ContentHandler handler,String nsURI,String prefix) {
        return new OutputVisitor(handler,nsURI,prefix);
    }
    
    /**
     * Performs the iteration, walking over the collection and  firing events.
     *
     * @param collection the features to walk over.
     *
     * @throws SAXException DOCUMENT ME!
     */
    private void walk() throws SAXException {
        ContentHandler handler = getContentHandler();
        handler.startDocument();
        
        OutputVisitor output = createOutputVisitor(handler,defaultNamespace,prefix);
        
        if (object instanceof Expression) {
            output.encode( (Expression) object);
        } else if (object instanceof Filter) {
            output.encode( (Filter) object);
        } else {
            throw new RuntimeException("Filter encoder encodes Filter or Expression, not " + object.getClass());
        }
        
        handler.endDocument();
    }
    
    
    /**
     * walks the given collection.
     *
     * @param systemId DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void parse(String systemId) throws java.io.IOException, SAXException {
        walk();
    }
    
    /**
     * walks the given collection.
     *
     * @param input DOCUMENT ME!
     *
     * @throws java.io.IOException DOCUMENT ME!
     * @throws SAXException DOCUMENT ME!
     */
    public void parse(InputSource input)
    throws java.io.IOException, SAXException {
        walk();
    }
    
    
    public static class OutputVisitor implements FilterVisitor {
        
        ContentHandler contentHandler;
        String defaultNamespace = null;
        String prefix = null;
        GeometryTransformer geometryEncoder;
        
        public OutputVisitor(ContentHandler handler,String nsURI,String prefix) {
            this.contentHandler = handler;
            this.defaultNamespace = nsURI;
            if (prefix == "")
              prefix = null;
            this.prefix = prefix;
            
            geometryEncoder = new GeometryTransformer(handler);
        }
        
        void element(String element,String content) {
            element(element,content,NULL_ATTS);
        }
        
        void element(String element,String content,Attributes atts) {
            start(element,atts);
            if (content != null)
                chars(content);
            end(element);
        }
        
        void start(String element) {
            start(element,NULL_ATTS);
        }
        
        void start(String element,Attributes atts) {
            try {
                String el = prefix == null ? element : prefix + ":" + element;
                contentHandler.startElement("", "", el, atts);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        void chars(String text) {
            try {
                char[] ch = text.toCharArray();
                contentHandler.characters(ch,0,ch.length);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        public void end(String element) {
            try {
                String el = prefix == null ? element : prefix + ":" + element;
                contentHandler.endElement("", "", el);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        public void visit(org.geotools.filter.LogicFilter filter) {
            filter.getFilterType();
            
            String type = (String) logical.get(new Integer(filter.getFilterType()));
            
            start(type);
            
            java.util.Iterator list = filter.getFilterIterator();
            
            while (list.hasNext()) {
                ((AbstractFilter) list.next()).accept(this);
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
                atts.addAttribute("", "fid", "", "", fids[i]);
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
            atts.addAttribute("", "wildCard", "", "", wcm);
            atts.addAttribute("", "singleChar", "", "", wcs);
            atts.addAttribute("", "escape", "", "", esc);
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
            atts.addAttribute("", "name", "", "", expression.getName());
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
        
    }
    
}
