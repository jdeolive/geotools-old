/*
 * SLDStyler.java
 *
 * Created on 22 May 2002, 14:22
 */

package org.geotools.styling;

/**
 * A class to read and parse an SLD file based on verions 0.7.2 of
 * the OGC Styled Layer Descriptor Spec
 *
 * @author  iant
 *
 * @version $Id: SLDStyle.java,v 1.3 2002/05/29 14:51:19 ianturton Exp $
 */

import org.w3c.dom.*;
import javax.xml.parsers.*;
//import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import java.net.*;
import java.io.*;
import java.util.*;

public class SLDStyle implements org.geotools.styling.Style {
    private static org.apache.log4j.Category _log = 
        org.apache.log4j.Category.getInstance("sldstyling.styling");    
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
    
    /** Setter for property name.
     * @param name New value of property name.
     */
    private void setName(java.lang.String name) {
        System.out.println("setting name "+name);
        this.name = name;
    }
  
    
    /** Setter for property abstractStr.
     * @param abstractStr New value of property abstractStr.
     */
    private void setAbstract(java.lang.String abstractStr) {
        System.out.println("setting abstract "+abstractStr);
        this.abstractStr = abstractStr;
    }
    
    /** Setter for property title.
     * @param title New value of property title.
     */
    private void setTitle(java.lang.String title) {
        System.out.println("setting title "+title);
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
        //AdapterNode root = new AdapterNode(n);
        //System.out.println(""+root.printNodes());
        
        NodeList children = n.getChildNodes();
        System.out.println(""+children.getLength()+" children to process");
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            System.out.println("processing "+child.getNodeName());
            //System.out.println("hasChildren "+child.hasChildNodes()+" attribs "+child.hasAttributes());
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
        System.out.println("Parsing featuretype style "+style.getNodeName());
        DefaultFeatureTypeStyle ft = new DefaultFeatureTypeStyle();

        ArrayList rules = new ArrayList();
        //System.out.println(""+style.toString());
        NodeList children = style.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            System.out.println("processing "+child.getNodeName());
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
        System.out.println("Parsing rule "+ruleNode.getNodeName());
        DefaultRule rule = new DefaultRule();
        ArrayList symbolizers = new ArrayList();
        //System.out.println(""+style.toString());
        NodeList children = ruleNode.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            
            System.out.println("processing "+child.getNodeName());
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
                // set a filter
            }
            if( child.getNodeName().equalsIgnoreCase("LegendGraphic")){
                NodeList g = ((Element)child).getElementsByTagName("Graphic");
                for(int k=0;k<g.getLength();k++){
                    rule.addLegendGraphic(new DefaultGraphic(g.item(k).getNodeValue()));
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
                //symbolizers.add(parseTextymbolizer(child));
            }
            if (child.getNodeName().equalsIgnoreCase("RasterSymbolizer")){
                //symbolizers.add(parseRasterSymbolizer(Child));
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
    private Mark parseMark(Node root){
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
                if(!mark.setWellKnownName(child.getFirstChild().getNodeValue())){
                    return null;
                }
            }
        }
        return mark;
    }
    
    private ExternalGraphic parseExternalGraphic(Node root){
        DefaultExternalGraphic extgraph = new DefaultExternalGraphic();
        NodeList children = root.getChildNodes();
        for(int i=0; i<children.getLength(); i++){
            Node child = children.item(i);
            if(child == null || child.getNodeType() != Node.ELEMENT_NODE){
                continue;
            }
            if(child.getNodeName().equalsIgnoreCase("OnLineResource")){
                extgraph.setURI(child.getNodeValue());
            }
            if(child.getNodeName().equalsIgnoreCase("format")){
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
            System.out.println("now I am processing "+child);
            Element param = (Element)child;
            NamedNodeMap map =  param.getAttributes();
            
            System.out.println("attributes "+map.toString());
            for(int k=0;k<map.getLength();k++){
                String res = map.item(k).getNodeValue();
                System.out.println("processing attribute "+res);
                
                if(res.equalsIgnoreCase("stroke")){
                    System.out.println("setting color "+child.getFirstChild().getNodeValue());
                    stroke.setColor(child.getFirstChild().getNodeValue());
                }
                if(res.equalsIgnoreCase("width")||res.equalsIgnoreCase("stroke-width")){
                    stroke.setWidth(Double.parseDouble(child.getFirstChild().getNodeValue()));
                }
                if(res.equalsIgnoreCase("opacity")||res.equalsIgnoreCase("stroke-opacity")){
                    stroke.setOpacity(Double.parseDouble(child.getFirstChild().getNodeValue()));
                }
                if(res.equalsIgnoreCase("linecap")||res.equalsIgnoreCase("stroke-linecap")){
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineCap(child.getFirstChild().getNodeValue());
                }
                if(res.equalsIgnoreCase("linejoin")||res.equalsIgnoreCase("stroke-linejoin")){
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineJoin(child.getFirstChild().getNodeValue());
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
                    stroke.setDashOffset(Float.parseFloat(child.getFirstChild().getNodeValue()));
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
            System.out.println("now I am processing "+child);
            System.out.println("attributes "+map.toString());
            for(int k=0;k<map.getLength();k++){
                String res = map.item(k).getNodeValue();
                System.out.println("processing attribute "+res);
                
                if(res.equalsIgnoreCase("fill")){
                    fill.setColor(child.getFirstChild().getNodeValue());
                }
                
                if(res.equalsIgnoreCase("opacity")||res.equalsIgnoreCase("fill-opacity")){
                    fill.setOpacity(Double.parseDouble(child.getFirstChild().getNodeValue()));
                }
            }
        }
        return fill;
        
    }
}
