/*
 * SLDTransformer.java
 *
 * Created on October 17, 2003, 1:51 PM
 */

package org.geotools.styling;

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterTransformer;
import org.geotools.styling.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * Producers SLD to an output stream.
 *
 * @author Ian Schneider
 *
 */
public class SLDTransformer extends XMLFilterImpl implements XMLReader {
    
    static Attributes NULL_ATTS = new AttributesImpl();
    
    static final String XLINK_NAMESPACE = "http://www.w3.org/1999/xlink";
    
    /** handler to do the processing */
    private ContentHandler contentHandler;
    
    private int indent = 4;
    
    /** The namespace to use if none is provided. */
    private String defaultNamespace = "";
    
    private String prefix = "";
    
    private Style[] styles;
    
    private boolean prettyPrint = false;
    
    
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
    public synchronized void transform(Style[] styles, OutputStream out)
    throws TransformerException {
        this.styles = styles;
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        
        if (prettyPrint) {
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent + "");
        }
        
        InputSource inputSource = new InputSource();
        SAXSource source = new SAXSource(this, inputSource);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }
    
    
    /**
     * Performs the iteration, walking over the collection and  firing events.
     *
     * @param collection the features to walk over.
     *
     * @throws SAXException DOCUMENT ME!
     */
    private void walk() throws SAXException {
        contentHandler.startDocument();
        
        OutputVisitor output = new OutputVisitor();
        
        output.start("StyledLayerDescriptor");
        
        for (int i = 0, ii = styles.length; i < ii; i++) {
            styles[i].accept(output);
        }
        
        output.end("StyledLayerDescriptor");
        
        contentHandler.endDocument();
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
    
    /**
     * sets the content handler.
     *
     * @param handler DOCUMENT ME!
     */
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }
    
    
    class OutputVisitor implements StyleVisitor {
        
        FilterTransformer.OutputVisitor filterOutput;

        FilterTransformer.OutputVisitor filterOutput() {
            if (filterOutput == null)
                filterOutput = FilterTransformer.createOutputVisitor(contentHandler);
            return filterOutput;
        }
        
        void element(String element,Expression e) {
            start(element);
            filterOutput().encode(e);
            end(element);
        }
        
        void element(String element,Filter f) {
            start(element);
            filterOutput().encode(f);
            end(element);
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
                contentHandler.startElement(defaultNamespace, "", element, atts);
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
        
        void end(String element) {
            try {
                contentHandler.endElement(defaultNamespace, "", element);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        public void visit(PointPlacement pp) {
            start("LabelPlacement");
            start("PointPlacement");
            System.out.println("FIX ME");
            //pp.getAnchorPoint().accept(this);
            //pp.getDisplacement().accept(this);
            start("Rotation");
            System.out.println("FIX ME");
            //encode(pp.getRotation());
            end("Rotation");
            end("PointPlacement");
            end("LabelPlacement");
        }
        
        public void visit(Stroke stroke) {
            start("Stroke");

            if (stroke.getGraphicFill() != null) {
                stroke.getGraphicFill().accept(this);
            }

            if (stroke.getGraphicStroke() != null) {
                stroke.getGraphicStroke().accept(this);
            }

            encodeCssParam("stroke", stroke.getColor());
            encodeCssParam("stroke-linecap", stroke.getLineCap());
            encodeCssParam("stroke-linejoin", stroke.getLineJoin());
            encodeCssParam("stroke-opacity", stroke.getOpacity());
            encodeCssParam("stroke-width", stroke.getWidth());
            encodeCssParam("stroke-dashoffset", stroke.getDashOffset());

            float[] dash = stroke.getDashArray();
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < dash.length; i++) {
                sb.append(dash[i] + " ");
            }

            encodeCssParam("stroke-dasharray", sb.toString());
            end("Stroke");
        }
        
        public void visit(LinePlacement lp) {
            start("LabelPlacement");
            start("LinePlacement");
            element("PerpendicularOffset",lp.getPerpendicularOffset());
            end("LinePlacement");
            end("LabelPlacement");
        }
        
        public void visit(AnchorPoint ap) {
            start("AnchorPoint");
            element("AnchorPointX",ap.getAnchorPointX());
            element("AnchorPointY",ap.getAnchorPointY());
            end("AnchorPoint");
        }
        
        public void visit(TextSymbolizer text) {
            start("TextSymbolizer");

            encodeGeometryProperty(text.getGeometryPropertyName());
            
            start("Label");
            System.out.println("FIX ME");
            //encode(sym.getLabel());
            end("Label");
            
            start("Font");
            Font[] fonts = text.getFonts();
            for (int i = 0; i < fonts.length; i++) {
                encodeCssParam("font-family", fonts[i].getFontFamily());
            }
            encodeCssParam("font-size", fonts[0].getFontSize());
            encodeCssParam("font-style", fonts[0].getFontStyle());
            encodeCssParam("font-weight", fonts[0].getFontWeight());
            end("Font");
            start("Label");
            System.out.println("FIX ME");
            //sym.getLabelPlacement().accept(this);
            end("Label");
            System.out.println("FIX ME");
            //sym.getHalo().accept(this);
            System.out.println("FIX ME");
            //sym.getFill().accept(this);
            end("TextSymbolizer");
        }
        
        public void visit(Symbolizer sym) {
            try {
                contentHandler.startElement("", "!--","!--", NULL_ATTS);
                chars("Unidentified Symbolizer " + sym.getClass());
                contentHandler.endElement("","--","--");
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }
        }
        
        public void visit(PolygonSymbolizer poly) {
            start("PolygonSymbolizer");
            encodeGeometryProperty(poly.getGeometryPropertyName());
            
            System.out.println("FIX ME");
//            if (sym.getFill() != null) {
//                sym.getFill().accept(this);
//            }
//
//            if (sym.getStroke() != null) {
//                sym.getStroke().accept(this);
//            }

            end("PolygonSymbolizer");
        }
        
        public void visit(ExternalGraphic exgr) {
            start("ExternalGraphic");
            element("Format",exgr.getFormat());
            AttributesImpl atts = new AttributesImpl();
            try {
                atts.addAttribute(XLINK_NAMESPACE, "type", "", "", "simple");
                atts.addAttribute(XLINK_NAMESPACE, "xlink", "","", exgr.getLocation().toString());
            } catch (java.net.MalformedURLException murle) {
                throw new Error("YOU CODED THE X LINK NAMESPACE WRONG!!");
            }   
            element("OnlineResource",null,atts);
            end("ExternalGraphic");
        }
        
        public void visit(LineSymbolizer line) {
            start("LineSymbolizer");
            
            encodeGeometryProperty(line.getGeometryPropertyName());
           
            System.out.println("FIX ME - Filter Encoding");
            //sym.getStroke().accept(this);
            end("LineSymbolizer");
        }
        
        public void visit(Fill fill) {
            start("Fill");
            
            if (fill.getGraphicFill() != null) {
                fill.getGraphicFill().accept(this);
            }
            
            encodeCssParam("fill", fill.getColor());
            encodeCssParam("fill-opacity", fill.getOpacity());
            end("Fill");
        }
        
        public void visit(Rule rule) {
            start("Rule");
            element("Name",rule.getName());
            element("Abstract",rule.getAbstract());
            element("Title",rule.getTitle());
            
            if (rule.getMaxScaleDenominator() != Double.POSITIVE_INFINITY) {
                element("MaxScaleDenominator",rule.getMaxScaleDenominator() + "");
            }
            
            if (rule.getMinScaleDenominator() != 0.0) {
                element("MinScaleDenominator",rule.getMinScaleDenominator() + "");
            }
            
            org.geotools.filter.Filter filter = rule.getFilter();
            
            if (filter != null) {
                System.out.println("FIX ME - Filter Encoding");
            }
            
            if (rule.hasElseFilter()) {
                start("ElseFilter");
                end("ElseFilter");
            }
            
            Graphic[] gr = rule.getLegendGraphic();
            
            for (int i = 0; i < gr.length; i++) {
                gr[i].accept(this);
            }
            
            Symbolizer[] sym = rule.getSymbolizers();
            
            for (int i = 0; i < sym.length; i++) {
                sym[i].accept(this);
            }
            
            end("Rule");
        }
        
        public void visit(Mark mark) {
            start("Mark");
            start("WellKnownName");
            
            System.out.println("FIX ME");
            //mark.getWellKnownName().accept(filterEncoder);
            end("WellKnownName");

            System.out.println("FIX ME");
//            if (mark.getFill() != null) {
//                mark.getFill().accept(this);
//            }
//
//            if (mark.getStroke() != null) {
//                mark.getStroke().accept(this);
//            }

            end("Mark");
        }
        
        public void visit(PointSymbolizer ps) {
            start("PointSymbolizer");
            
            encodeGeometryProperty(ps.getGeometryPropertyName());
            
            ps.getGraphic().accept(this);
            end("PointSymbolizer");
        }
        
        public void visit(Halo halo) {
            start("Halo");
            System.out.println("FIX ME");
            //halo.getFill().accept(this);
            start("Radius");
            System.out.println("FIX ME");
            //encode(halo.getRadius());
            end("Radius");
            end("Halo");
        }
        
        public void visit(Graphic gr) {
            start("Graphic");
            
            encodeGeometryProperty(gr.getGeometryPropertyName());
            
            System.out.println("FIX ME - Filter Encoding");
            //            out.write("<Size>\n");
            //            encode(gr.getSize());
            //            out.write("</Size>\n");
            //
            //            out.write("<Opacity>\n");
            //            encode(gr.getOpacity());
            //            out.write("</Opacity>\n");
            //            out.write("<Rotation>\n");
            //            encode(gr.getRotation());
            //            out.write("</Rotation>\n");
            
            Symbol[] symbols = gr.getSymbols();
            
            for (int i = 0; i < symbols.length; i++) {
                symbols[i].accept(this);
            }
            
            end("Graphic");
        }
        
        public void visit(Style style) {
            start("NamedLayer");
            element("Title",style.getTitle());
            element("Name",style.getName());
            element("Abstract",style.getAbstract());
            start("UserLayer");
            
            FeatureTypeStyle[] fts = style.getFeatureTypeStyles();
            
            for (int i = 0; i < fts.length; i++) {
                visit(fts[i]);
            }
            
            end("UserLayer");
            end("NamedLayer");
        }
        
        public void visit(FeatureTypeStyle fts) {
            start("FeatureTypeStyle");
            element("FeatureTypeName",fts.getName());
            
            Rule[] rules = fts.getRules();
            
            for (int i = 0; i < rules.length; i++) {
                rules[i].accept(this);
            }
            
            end("FeatureTypeStyle");
        }
        
        public void visit(Displacement dis) {
            start("Displacement");
            element("DisplacementX",dis.getDisplacementX());
            element("DisplacementY",dis.getDisplacementY());
            end("Displacement");
        }
        
        void encodeGeometryProperty(String name) {
            if (name == null || name.trim().length() == 0) return;
            start("Geometry");
            element("PropertyName",name);
            end("Geometry");
        }
        
        void encodeCssParam(String name,org.geotools.filter.Expression expression) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "", "", name);
            start("CssParameter",atts);
            System.out.println("FIX ME- Expressions");
            end("CssParameter");
            
        }
        void encodeCssParam(String name,String expression) {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "name", "", "", name);
            start("CssParameter",atts);
            chars(expression);
            end("CssParameter");
            
        }
        
    }
    
    
    /**
     * Currently does nothing.
     *
     * @param args DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public static final void main(String[] args) throws Exception {
        java.net.URL url = new java.io.File(args[0]).toURL();
        SLDStyle s = new SLDStyle(StyleFactory.createStyleFactory(),url);
        SLDTransformer transformer = new SLDTransformer();
        transformer.setPrettyPrint(true);
        transformer.setDefaultNamespace("http://www.somewhere.org");
        transformer.setPrefix("cool");
        transformer.transform(s.readXML(), new FileOutputStream(System.getProperty("java.io.tmpdir") + "/junk.eraseme"));
    }
    
}
