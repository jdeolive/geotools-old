/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.styling;

/**
 * A class to read and parse an SLD file based on verion 0.7.2 of
 * the OGC Styled Layer Descriptor Spec.
 *
 * @version $Id: SLDStyle.java,v 1.8 2002/06/07 16:43:09 ianturton Exp $
 * @author Ian Turton, CCG
 *

 */

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.geotools.filter.*;

public class SLDStyle implements org.geotools.styling.Style {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(SLDStyle.class);
    private String abstractStr = new String();
    private String name = new String();
    private String title = new String();
    private ArrayList fts = new ArrayList();
    private boolean defaultB;
    private InputStream instream;
    private Document dom;
    /** Creates a new instance of SLDStyler */
    public SLDStyle(String filename) {
        
        File f = new File(filename);
        setInput(f);
        readXML();
    }
    public SLDStyle(File f){
        setInput(f);
        readXML();
    }
    public SLDStyle(URL url){
        setInput(url);
        readXML();
    }
    public SLDStyle(InputStream s){
        instream = s;
        readXML();
    }
    
    private void setInput(File f){
        try{
            
            instream = new FileInputStream(f);
        } catch (FileNotFoundException e){
            System.out.println("file "+f+" not found\n"+e);
            instream = null;
        }
    }
    
    private void setInput(URL url){
        try{
            instream = url.openStream();
        } catch (IOException e){
            System.out.println("IO Exception in SLDStyler");
            instream = null;
        }
        
    }
    
    public String getAbstract() {
        return abstractStr;
    }
    
    private void addFeatureTypeStyle(FeatureTypeStyle ft){
        fts.add(ft);
    }
    
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        return (FeatureTypeStyle[]) fts.toArray(new FeatureTypeStyle[0]);
    }
    
    public String getName() {
        return name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public boolean isDefault() {
        return defaultB;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    private void setName(java.lang.String name) {
        _log.debug("setting name "+name);
        this.name = name;
    }
    
    
    /**
     * Setter for property abstractStr.
     * @param abstractStr New value of property abstractStr.
     */
    private void setAbstract(java.lang.String abstractStr) {
        _log.debug("setting abstract "+abstractStr);
        this.abstractStr = abstractStr;
    }
    
    /**
     * Setter for property title.
     * @param title New value of property title.
     */
    private void setTitle(java.lang.String title) {
        _log.debug("setting title "+title);
        this.title = title;
    }
    
    private void readXML(){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(instream);
        } catch (Exception e){
            System.out.println("exception in sldStyler "+e);
        }
        // for our next trick do something with the dom.
        
        NodeList nodes = dom.getElementsByTagName("UserStyle");
        if (nodes.getLength()>1){
            System.out.println("Currently only able to handle one user style per sld file"+
            " only parsing first one found");
        }
        
        
        Node n = nodes.item(0);
        
        NodeList children = n.getChildNodes();
        
        _log.debug(""+children.getLength()+" children to process");
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            _log.debug("processing "+child.getNodeName());
            
            if( child.getNodeName().equalsIgnoreCase("Name")){
                setName(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Title")){
                setTitle(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Abstract")){
                setAbstract(child.getFirstChild().getNodeValue());
            }
            if(child.getNodeName().equalsIgnoreCase("FeatureTypeStyle")){
                addFeatureTypeStyle(parseFeatureTypeStyle(child));
            }
        }
        
        
    }
    private FeatureTypeStyle parseFeatureTypeStyle(Node style){
        _log.debug("Parsing featuretype style "+style.getNodeName());
        DefaultFeatureTypeStyle ft = new DefaultFeatureTypeStyle();
        
        ArrayList rules = new ArrayList();
        NodeList children = style.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            _log.debug("processing "+child.getNodeName());
            if( child.getNodeName().equalsIgnoreCase("Name")){
                ft.setName(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Title")){
                ft.setTitle(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Abstract")){
                ft.setAbstract(child.getFirstChild().getNodeValue());
            }
            if (child.getNodeName().equalsIgnoreCase("FeatureTypeName")){
                ft.setFeatureTypeName(child.getFirstChild().getNodeValue());
            }
            if (child.getNodeName().equalsIgnoreCase("SemanticTypeIdentifier")){
                // experimental part of the spec
                // probably ignore it for now
            }
            if (child.getNodeName().equalsIgnoreCase("Rule")){
                rules.add(parseRule(child));
            }
            
            
        }
        ft.setRules((Rule[])rules.toArray(new Rule[0]));
        return ft;
    }
    private Rule parseRule(Node ruleNode){
        _log.debug("Parsing rule "+ruleNode.getNodeName());
        DefaultRule rule = new DefaultRule();
        ArrayList symbolizers = new ArrayList();
        NodeList children = ruleNode.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            _log.debug("processing "+child.getNodeName());
            if( child.getNodeName().equalsIgnoreCase("Name")){
                rule.setName(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Title")){
                rule.setTitle(child.getFirstChild().getNodeValue());
            }
            if( child.getNodeName().equalsIgnoreCase("Abstract")){
                rule.setAbstract(child.getFirstChild().getNodeValue());
            }
            if (child.getNodeName().equalsIgnoreCase("Filter")
            || child.getNodeName().equalsIgnoreCase("ElseFilter")){
                //TODO: set a filter
            }
            if( child.getNodeName().equalsIgnoreCase("LegendGraphic")){
                NodeList g = ((Element)child).getElementsByTagName("Graphic");
                for(int k=0;k<g.getLength();k++){
                    rule.addLegendGraphic(parseGraphic(g.item(k)));
                }
            }
            if (child.getNodeName().equalsIgnoreCase("LineSymbolizer")){
                symbolizers.add(parseLineSymbolizer(child));
            }
            if (child.getNodeName().equalsIgnoreCase("PolygonSymbolizer")){
                symbolizers.add(parsePolygonSymbolizer(child));
            }
            if (child.getNodeName().equalsIgnoreCase("PointSymbolizer")){
                symbolizers.add(parsePointSymbolizer(child));
            }
            if (child.getNodeName().equalsIgnoreCase("TextSymbolizer")){
                //TODO: implement symbolizers.add(parseTextymbolizer(child));
            }
            if (child.getNodeName().equalsIgnoreCase("RasterSymbolizer")){
                //TODO: implement symbolizers.add(parseRasterSymbolizer(Child));
            }
        }
        rule.setSymbolizers((Symbolizer[])symbolizers.toArray(new Symbolizer[0]));
        return rule;
        
    }
    
    public LineSymbolizer parseLineSymbolizer(Node root){
        DefaultLineSymbolizer symbol = new DefaultLineSymbolizer();
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            if(child.getNodeName().equalsIgnoreCase("Geometry")){
                //symbol.setGeometryPropertyName(child.getNodeValue());
            }
            if(child.getNodeName().equalsIgnoreCase("Stroke")){
                symbol.setStroke(parseStroke(child));
            }
        }
        return symbol;
    }
    public PolygonSymbolizer parsePolygonSymbolizer(Node root){
        DefaultPolygonSymbolizer symbol = new DefaultPolygonSymbolizer();
        symbol.setFill((Fill)null);
        symbol.setStroke((Stroke)null);
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            if(child.getNodeName().equalsIgnoreCase("Geometry")){
                // hmm I don't understand the spec here?
                // TODO: read spec carefully
            }
            if(child.getNodeName().equalsIgnoreCase("Stroke")){
                symbol.setStroke(parseStroke(child));
            }
            if(child.getNodeName().equalsIgnoreCase("Fill")){
                symbol.setFill(parseFill(child));
            }
        }
        return symbol;
    }
    
    public PointSymbolizer parsePointSymbolizer(Node root){
        DefaultPointSymbolizer symbol = new DefaultPointSymbolizer();
        symbol.setGraphic(null);
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            if(child.getNodeName().equalsIgnoreCase("Geometry")){
                symbol.setGeometryPropertyName(child.getNodeValue());
            }
            if(child.getNodeName().equalsIgnoreCase("Graphic")){
                symbol.setGraphic(parseGraphic(child));
            }
        }
        return symbol;
    }
    private Graphic parseGraphic(Node root){
        _log.debug("processing graphic");
        DefaultGraphic graphic = new DefaultGraphic();
        
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            if(child.getNodeName().equalsIgnoreCase("Geometry")){
                // hmm I don't understand the spec here?
                // TODO: read spec carefully
            }
            if(child.getNodeName().equalsIgnoreCase("ExternalGraphic")){
                _log.debug("parsing extgraphic "+child);
                graphic.addExternalGraphic(parseExternalGraphic(child));
            }
            if(child.getNodeName().equalsIgnoreCase("Mark")){
                graphic.addMark(parseMark(child));
            }
            if(child.getNodeName().equalsIgnoreCase("opacity")){
                graphic.setOpacity(Double.parseDouble(child.getFirstChild().getNodeValue()));
            }
            if(child.getNodeName().equalsIgnoreCase("size")){
                graphic.setSize(Double.parseDouble(child.getFirstChild().getNodeValue()));
            }
            if(child.getNodeName().equalsIgnoreCase("rotation")){
                graphic.setRotation(Double.parseDouble(child.getFirstChild().getNodeValue()));
            }
        }
        return graphic;
    }
    private DefaultMark parseMark(Node root){
        DefaultMark mark = new DefaultMark();
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            if(child.getNodeName().equalsIgnoreCase("Stroke")){
                mark.setStroke(parseStroke(child));
            }
            if(child.getNodeName().equalsIgnoreCase("Fill")){
                mark.setFill(parseFill(child));
            }
            if(child.getNodeName().equalsIgnoreCase("WellKnownName")){
                _log.debug("setting mark to "+child.getFirstChild().getNodeValue());
                mark.setWellKnownName(child.getFirstChild().getNodeValue());
            }
        }
        return mark;
    }
    
    private ExternalGraphic parseExternalGraphic(Node root){
        _log.debug("processing external graphic ");
        DefaultExternalGraphic extgraph = new DefaultExternalGraphic();
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            if(child.getNodeName().equalsIgnoreCase("OnLineResource")){
                _log.debug("seting ExtGraph uri "+child.getNodeValue());
                extgraph.setURI(child.getNodeValue());
            }
            if(child.getNodeName().equalsIgnoreCase("format")){
                _log.debug("seting ExtGraph format "+child.getNodeValue());
                extgraph.setFormat(child.getNodeValue());
            }
        }
        return extgraph;
    }
    private Stroke parseStroke(Node root){
        DefaultStroke stroke = new DefaultStroke();
        NodeList list = ((Element)root).getElementsByTagName("GraphicFill");
        if(list.getLength()>0){
            //stroke.setGraphicFill(new DefaultGraphic(list.item(0).getNodeValue()));
        }
        list = ((Element)root).getElementsByTagName("GraphicStroke");
        if(list.getLength()>0){
            //stroke.setGraphicStroke(new DefaultGraphic(list.item(0).getNodeValue()));
        }
        list = ((Element)root).getElementsByTagName("CssParameter");
        for(int i=0;i<list.getLength();i++){
            Node child = list.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            _log.debug("now I am processing "+child);
            Element param = (Element)child;
            NamedNodeMap map =  param.getAttributes();
            
            _log.debug("attributes "+map.toString());
            for(int k=0;k<map.getLength();k++){
                String res = map.item(k).getNodeValue();
                _log.debug("processing attribute "+res);
                
                if(res.equalsIgnoreCase("stroke")){
                    stroke.setColor(parseCssParameter(child));
                }
                if(res.equalsIgnoreCase("width")||res.equalsIgnoreCase("stroke-width")){
                    stroke.setWidth(parseCssParameter(child));
                }
                if(res.equalsIgnoreCase("opacity")||res.equalsIgnoreCase("stroke-opacity")){
                    stroke.setOpacity(parseCssParameter(child));
                }
                if(res.equalsIgnoreCase("linecap")||res.equalsIgnoreCase("stroke-linecap")){
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineCap(parseCssParameter(child));
                }
                if(res.equalsIgnoreCase("linejoin")||res.equalsIgnoreCase("stroke-linejoin")){
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineJoin(parseCssParameter(child));
                }
                if(res.equalsIgnoreCase("dasharray")||res.equalsIgnoreCase("stroke-dasharray")){
                    StringTokenizer stok = new StringTokenizer(child.getFirstChild().getNodeValue()," ");
                    float[] dashes = new float[stok.countTokens()];
                    for(int l=0;l<stok.countTokens();l++){
                        dashes[l]=Float.parseFloat(stok.nextToken());
                    }
                    
                    stroke.setDashArray(dashes);
                }
                if(res.equalsIgnoreCase("dashoffset")||res.equalsIgnoreCase("stroke-dashoffset")){
                    stroke.setDashOffset(parseCssParameter(child));
                }
            }
        }
        return stroke;
    }
    
        private Fill parseFill(Node root){
        DefaultFill fill = new DefaultFill();
        NodeList list = ((Element)root).getElementsByTagName("GraphicFill");
        if(list.getLength()>0){
            //fill.setGraphicFill(new DefaultGraphic(list.item(0).getNodeValue()));
        }
        list = ((Element)root).getElementsByTagName("CssParameter");
        for(int i=0;i<list.getLength();i++){
            Node child = list.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            Element param = (Element)child;
            NamedNodeMap map =  param.getAttributes();
            _log.debug("now I am processing "+child);
            _log.debug("attributes "+map.toString());
            for(int k=0;k<map.getLength();k++){
                String res = map.item(k).getNodeValue();
                _log.debug("processing attribute "+res);
                
                if(res.equalsIgnoreCase("fill")){
                    fill.setColor(parseCssParameter(child));
                }
                
                if(res.equalsIgnoreCase("opacity")||res.equalsIgnoreCase("fill-opacity")){
                    fill.setOpacity(parseCssParameter(child));
                }
            }
        }
        return fill;
        
    }
    private Expression parseCssParameter(Node root){
        _log.info("parsingCssParam "+root);
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            _log.debug("about to parse "+child.getNodeName());
            return parseExpression(child);
        }
        _log.debug("no children in CssParam");
      
            Element literal = dom.createElement("literal");
            Node child = dom.createTextNode(root.getFirstChild().getNodeValue());
            
            
            literal.appendChild(child);
            _log.debug("Built new literal "+literal);
            return parseExpression(literal);
    }
    
    private Expression parseExpression(Node root){
        _log.info("parsingExpression "+root.getNodeName());
        
        //NodeList children = root.getChildNodes();
        //_log.debug("children "+children);
        
        if(root == null || root.getNodeType() != Node.ELEMENT_NODE){
            _log.debug("bad node input ");
            return null;
        }
        _log.debug("processing root "+root.getNodeName());
        Node child = root;
        if(child.getNodeName().equalsIgnoreCase("add")){
            try{
                _log.info("processing an Add");
                Node left=null,right=null;
                
                ExpressionMath math = new ExpressionMath(ExpressionMath.MATH_ADD);
                Node value = child.getFirstChild();
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                _log.debug("add left value -> "+value+"<-");
                math.addLeftValue(parseExpression(value));
                value = value.getNextSibling();
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                _log.debug("add right value -> "+value+"<-");
                math.addRightValue(parseExpression(value));
                return math;
            }catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        if(child.getNodeName().equalsIgnoreCase("sub")){
            try{
                NodeList kids = child.getChildNodes();
                ExpressionMath math = new ExpressionMath(ExpressionMath.MATH_SUBTRACT);
                math.addLeftValue(parseExpression(child.getFirstChild()));
                math.addRightValue(parseExpression(child.getLastChild()));
                return math;
            }catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        if(child.getNodeName().equalsIgnoreCase("mul")){
            try{
                NodeList kids = child.getChildNodes();
                ExpressionMath math = new ExpressionMath(ExpressionMath.MATH_MULTIPLY);
                math.addLeftValue(parseExpression(child.getFirstChild()));
                math.addRightValue(parseExpression(child.getLastChild()));
                return math;
            }catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        if(child.getNodeName().equalsIgnoreCase("div")){
            try{
                NodeList kids = child.getChildNodes();
                ExpressionMath math = new ExpressionMath(ExpressionMath.MATH_DIVIDE);
                math.addLeftValue(parseExpression(child.getFirstChild()));
                math.addRightValue(parseExpression(child.getLastChild()));
                return math;
            }catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        if(child.getNodeName().equalsIgnoreCase("Literal")){
            
            String nodeValue = child.getFirstChild().getNodeValue();
            _log.info("processing literal "+nodeValue);
            // see if it's an int
            try{
                try{
                    Integer I = new Integer(nodeValue);
                    _log.debug("An integer");
                    return new ExpressionLiteral(I);
                } catch (NumberFormatException e){
                    /* really empty */
                }
                try{
                    Double D = new Double(nodeValue);
                    _log.debug("A double");
                    return new ExpressionLiteral(D);
                } catch (NumberFormatException e){
                    /* really empty */
                }
                _log.debug("defaulting to string");
                return new ExpressionLiteral(nodeValue);
            } catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
            
            
        }
        if(child.getNodeName().equalsIgnoreCase("PropertyName")){
            //try{
            NodeList kids = child.getChildNodes();
            ExpressionAttribute attribute = new ExpressionAttribute();
            attribute.setAttributePath(child.getFirstChild().getNodeValue());
            return attribute;
                /* }catch (IllegalFilterException ife){
                    _log.error("Unable to build expression ",ife);
                    return null;
                } */
        }
        if(child.getNodeName().equalsIgnoreCase("Function")){
            
            // TODO: should have a name and a (or more?) expressions
            // TODO: find out what really happens here
            
        }
        
        if(child.getNodeType()== Node.TEXT_NODE){
            _log.debug("processing a text node "+root.getNodeValue());
            String nodeValue = root.getNodeValue();
            _log.info("Text name "+nodeValue);
            // see if it's an int
            try{
                try{
                    Integer I = new Integer(nodeValue);
                    return new ExpressionLiteral(I);
                } catch (NumberFormatException e){
                    /* really empty */
                }
                try{
                    Double D = new Double(nodeValue);
                    return new ExpressionLiteral(D);
                } catch (NumberFormatException e){
                    /* really empty */
                }
                return new ExpressionLiteral(nodeValue);
            } catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
            }
        }
        
        return null;
    }

}
