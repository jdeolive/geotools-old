package org.geotools.styling;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.geotools.filter.Expression;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SLDParser {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
    .getLogger("org.geotools.styling");
    private static final org.geotools.filter.FilterFactory FILTERFACTORY = org.geotools.filter.FilterFactory
    .createFilterFactory();
    protected java.io.InputStream instream;
    private org.w3c.dom.Document dom;
    protected StyleFactory factory;
    private String graphicSt = "Graphic";    // to make pmd to shut up
    private String geomSt = "Geometry";    // to make pmd to shut up
    private String fillSt = "Fill";
    
     /**
     * Create a Stylereader - use if you already have a dom to parse.
     *
     * @param factory The StyleFactory to use to build the style
     */
    public SLDParser(StyleFactory factory) {
        this.factory = factory;
    }
    
    /**
     * Creates a new instance of SLDStyler
     *
     * @param factory The StyleFactory to use to read the file
     * @param filename The file to be read.
     *
     * @throws java.io.FileNotFoundException - if the file is missing
     */
    public SLDParser(StyleFactory factory, String filename)
    throws java.io.FileNotFoundException {
        this(factory);
        
        File f = new File(filename);
        setInput(f);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param f the File to be read
     *
     * @throws java.io.FileNotFoundException - if the file is missing
     */
    public SLDParser(StyleFactory factory, File f)
    throws java.io.FileNotFoundException {
        this(factory);
        setInput(f);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param url the URL to be read.
     *
     * @throws java.io.IOException - if something goes wrong reading the file
     */
    public SLDParser(StyleFactory factory, java.net.URL url)
    throws java.io.IOException {
        this(factory);
        setInput(url);
    }
    
    /**
     * Creates a new SLDStyle object.
     *
     * @param factory The StyleFactory to use to read the file
     * @param s The inputstream to be read
     */
    public SLDParser(StyleFactory factory, java.io.InputStream s) {
        this(factory);
        instream = s;
    }
    
    /**
     * set the file to read the SLD from
     *
     * @param filename the file to read the SLD from
     *
     * @throws java.io.FileNotFoundException if the file is missing
     */
    public void setInput(String filename) throws java.io.FileNotFoundException {
        instream = new java.io.FileInputStream(new File(filename));
    }
    
    /**
     * Sets the file to use to read the SLD from
     *
     * @param f the file to use
     *
     * @throws java.io.FileNotFoundException if the file is missing
     */
    public void setInput(File f) throws java.io.FileNotFoundException {
        instream = new java.io.FileInputStream(f);
    }
    
    /**
     * sets an URL to read the SLD from
     *
     * @param url the url to read the SLD from
     *
     * @throws java.io.IOException If anything goes wrong opening the url
     */
    public void setInput(java.net.URL url) throws java.io.IOException {
        instream = url.openStream();
    }
    
    /**
     * Sets the input stream to read the SLD from
     *
     * @param in the inputstream used to read the SLD from
     */
    public void setInput(java.io.InputStream in) {
        instream = in;
    }
    
    /**
     * Read the xml inputsource provided and create a Style object for each
     * user style found
     *
     * @return Style[] the styles constructed.
     *
     * @throws RuntimeException if a parsing error occurs
     */
    public Style[] readXML() {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
        .newInstance();
        dbf.setNamespaceAware(true);
        try {
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(instream);
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        } catch (org.xml.sax.SAXException se) {
            throw new RuntimeException(se);
        } catch (java.io.IOException ie) {
            throw new RuntimeException(ie);
        }
        
        return readDOM(dom);
    }
    
    /**
     * Read the DOM provided and create a Style object for each user style
     * found
     *
     * @param document a dom containing the SLD
     *
     * @return Style[] the styles constructed.
     */
    public Style[] readDOM(org.w3c.dom.Document document) {
        this.dom = document;
       
        
        // for our next trick do something with the dom.
        NodeList nodes = findElements(document, "UserStyle");
        
        if(nodes == null)
        	return new Style[0];
        
        Style[] styles = new Style[nodes.getLength()];
        
        for (int i = 0; i < nodes.getLength(); i++) {
            styles[i] = parseStyle(nodes.item(i));
        }
        
        return styles;
    }

    /**
     * @param document
     * @param name
     * @return
     */
    private NodeList findElements(final org.w3c.dom.Document document, final String name) {
        NodeList nodes = document.getElementsByTagNameNS("*",name);
        
        if(nodes.getLength() == 0){
            nodes = document.getElementsByTagName(name);
        }

        return nodes;
    }
    
    private NodeList findElements(final org.w3c.dom.Element element, final String name) {
        NodeList nodes = element.getElementsByTagNameNS("*",name);
        
        if(nodes.getLength() == 0){
            nodes = element.getElementsByTagName(name);
        }

        return nodes;
    }
    
    public StyledLayerDescriptor parseSLD(){
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
        .newInstance();
        
        try {
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(instream);
            // for our next trick do something with the dom.
       
            NodeList nodes  = findElements(dom, "StyledLayerDescriptor");
     
            
            StyledLayerDescriptor sld = parseDescriptor( dom.getDocumentElement());//should only be one per file
            return sld;
            
            
        } catch (javax.xml.parsers.ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        } catch (org.xml.sax.SAXException se) {
            throw new RuntimeException(se);
        } catch (java.io.IOException ie) {
            throw new RuntimeException(ie);
        }
    }
    
      private StyledLayerDescriptor parseDescriptor(Node root) {
        StyledLayerDescriptor sld = new StyledLayerDescriptor();
          //StyledLayer layer = null;
        //LineSymbolizer symbol = factory.createLineSymbolizer();
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("Name")) {
             sld.setName(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Title")) {
             sld.setTitle(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Abstract")) {
             sld.setAbstract(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("NamedLayer")) {
           
            }
            
            if (child.getLocalName().equalsIgnoreCase("UserLayer")) {
              StyledLayer layer = parseUserLayer(child);
              sld.addStyledLayer(layer);
            }
        }
        
        return sld;
    }
      
       private StyledLayer parseUserLayer(Node root) {
        UserLayer layer = new UserLayer();
        //LineSymbolizer symbol = factory.createLineSymbolizer();
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("UserStyle")) {
             Style user = parseStyle(child);
             layer.addUserStyle(user);
            }
            
            if (child.getLocalName().equalsIgnoreCase("Name")) {
              layer.setName(child.getNodeValue());
              
             //   symbol.setStroke(parseStroke(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("LayerFeatureConstraints")) {
              layer = new UserLayer();
            }
            
           
        }
        
        return layer;
    }
      
      private StyledLayer parseLayer(Node root) {
        StyledLayer layer = null;
        //LineSymbolizer symbol = factory.createLineSymbolizer();
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("NamedLayer")) {
             
            }
            
            if (child.getLocalName().equalsIgnoreCase("UserLayer")) {
              
              layer = new UserLayer();
              
             //   symbol.setStroke(parseStroke(child));
            }
        }
        
        return layer;
    }
    
    /**
     * build a style for the Node provided
     *
     * @param n the node which contains the style to be parsed.
     *
     * @return the Style constructed.
     *
     * @throws RuntimeException if an error occurs setting up the parser
     */
    public Style parseStyle(Node n) {
        if (dom == null) {
            try {
                javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
                .newInstance();
                javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
                dom = db.newDocument();
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                throw new RuntimeException(pce);
            }
        }
        
        Style style = factory.createStyle();
        
        NodeList children = n.getChildNodes();
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("" + children.getLength() + " children to process");
        }
        
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
//            System.out.println("The child is: " + child.getNodeName() + " or " + child.getLocalName() + " prefix is " +child.getPrefix());
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("processing " + child.getLocalName());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Name")) {
                style.setName(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Title")) {
                style.setTitle(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Abstract")) {
                style.setAbstract(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("FeatureTypeStyle")) {
                style.addFeatureTypeStyle(parseFeatureTypeStyle(child));
            }
        }
        
        return style;
    }
    
    private FeatureTypeStyle parseFeatureTypeStyle(Node style) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Parsing featuretype style " + style.getLocalName());
        }
        
        FeatureTypeStyle ft = factory.createFeatureTypeStyle();
        
        ArrayList rules = new ArrayList();
        NodeList children = style.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("processing " + child.getLocalName());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Name")) {
                ft.setName(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Title")) {
                ft.setTitle(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Abstract")) {
                ft.setAbstract(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("FeatureTypeName")) {
                ft.setFeatureTypeName(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Rule")) {
                rules.add(parseRule(child));
            }
        }
        
        ft.setRules((Rule[]) rules.toArray(new Rule[0]));
        
        return ft;
    }
    
    private Rule parseRule(Node ruleNode) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Parsing rule " + ruleNode.getLocalName());
        }
        
        Rule rule = factory.createRule();
        ArrayList symbolizers = new ArrayList();
        NodeList children = ruleNode.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("processing " + child.getLocalName());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Name")) {
                rule.setName(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Title")) {
                rule.setTitle(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Abstract")) {
                rule.setAbstract(child.getFirstChild().getNodeValue());
            }
            
            if (child.getLocalName().equalsIgnoreCase("Filter")) {
                NodeList list = child.getChildNodes();
                Node kid = null;
                
                for (int k = 0; k < list.getLength(); k++) {
                    kid = list.item(k);
                    
                    if ((kid == null)
                    || (kid.getNodeType() != Node.ELEMENT_NODE)) {
                        continue;
                    }
                    
                    org.geotools.filter.Filter filter = org.geotools.filter.FilterDOMParser
                    .parseFilter(kid);
                    
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("filter: " + filter.getClass().toString());
                        LOGGER.finest("parsed: " + filter.toString());
                    }
                    
                    rule.setFilter(filter);
                }
            }
            
            if (child.getLocalName().equalsIgnoreCase("ElseFilter")) {
                rule.setIsElseFilter(true);
            }
            
            if (child.getLocalName().equalsIgnoreCase("LegendGraphic")) {
                findElements(((Element)child),graphicSt);
                NodeList g = findElements(((Element) child),graphicSt);
                ArrayList legends = new ArrayList();
                
                for (int k = 0; k < g.getLength(); k++) {
                    legends.add(parseGraphic(g.item(k)));
                }
                
                rule.setLegendGraphic((Graphic[]) legends.toArray(
                new Graphic[0]));
            }
            
            if (child.getLocalName().equalsIgnoreCase("LineSymbolizer")) {
                symbolizers.add(parseLineSymbolizer(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("PolygonSymbolizer")) {
                symbolizers.add(parsePolygonSymbolizer(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("PointSymbolizer")) {
                symbolizers.add(parsePointSymbolizer(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("TextSymbolizer")) {
                symbolizers.add(parseTextSymbolizer(child));
            }
            
            //            if (child.getLocalName().equalsIgnoreCase("RasterSymbolizer")) {
            //                //TODO: implement symbolizers.add(parseRasterSymbolizer(Child));
            //            }
        }
        
        rule.setSymbolizers((Symbolizer[]) symbolizers.toArray(
        new Symbolizer[0]));
        
        return rule;
    }
    
    /**
     * parses the SLD for a linesymbolizer
     *
     * @param root a w2c Dom Node
     *
     * @return the linesymbolizer
     */
    private LineSymbolizer parseLineSymbolizer(Node root) {
        LineSymbolizer symbol = factory.createLineSymbolizer();
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(geomSt)) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Stroke")) {
                symbol.setStroke(parseStroke(child));
            }
        }
        
        return symbol;
    }
    
    /**
     * parses the SLD for a polygonsymbolizer
     *
     * @param root w3c dom node
     *
     * @return the polygon symbolizer
     */
    private PolygonSymbolizer parsePolygonSymbolizer(Node root) {
        PolygonSymbolizer symbol = factory.createPolygonSymbolizer();
        symbol.setFill((Fill) null);
        symbol.setStroke((Stroke) null);
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(geomSt)) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Stroke")) {
                symbol.setStroke(parseStroke(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase(fillSt)) {
                symbol.setFill(parseFill(child));
            }
        }
        
        return symbol;
    }
    
    /**
     * parses the SLD for a text symbolizer
     *
     * @param root w3c dom node
     *
     * @return the TextSymbolizer
     */
    private TextSymbolizer parseTextSymbolizer(Node root) {
        TextSymbolizer symbol = factory.createTextSymbolizer();
        symbol.setFill(null);
        
        ArrayList fonts = new ArrayList();
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(geomSt)) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase(fillSt)) {
                symbol.setFill(parseFill(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Label")) {
                LOGGER.finest("parsing label " + child.getNodeValue());
                symbol.setLabel(parseCssParameter(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Font")) {
                fonts.add(parseFont(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("LabelPlacement")) {
                symbol.setLabelPlacement(parseLabelPlacement(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Halo")) {
                symbol.setHalo(parseHalo(child));
            }
        }
        
        symbol.setFonts((Font[]) fonts.toArray(new Font[0]));
        
        return symbol;
    }
    
    /**
     * parses the SLD for a point symbolizer
     *
     * @param root a w3c dom node
     *
     * @return the pointsymbolizer
     */
    private PointSymbolizer parsePointSymbolizer(Node root) {
        PointSymbolizer symbol = factory.getDefaultPointSymbolizer();
        // symbol.setGraphic(null);
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(geomSt)) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase(graphicSt)) {
                symbol.setGraphic(parseGraphic(child));
            }
        }
        
        return symbol;
    }
    
    private Graphic parseGraphic(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("processing graphic " + root);
        }
        
        Graphic graphic = factory.getDefaultGraphic();
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(geomSt)) {
                graphic.setGeometryPropertyName(parseGeometryName(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("ExternalGraphic")) {
                LOGGER.finest("parsing extgraphic " + child);
                graphic.addExternalGraphic(parseExternalGraphic(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Mark")) {
                graphic.addMark(parseMark(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("opacity")) {
                graphic.setOpacity(parseCssParameter(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("size")) {
                graphic.setSize(parseCssParameter(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("rotation")) {
                graphic.setRotation(parseCssParameter(child));
            }
        }
        
        return graphic;
    }
    
    private String parseGeometryName(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing GeometryName");
        }
        
        String ret = null;
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            ret = parseCssParameter(child).toString();
        }
        
        return ret;
    }
    
    private Mark parseMark(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing mark");
        }
        
        Mark mark = factory.createMark();
        mark.setFill(null);
        mark.setStroke(null);
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("Stroke")) {
                mark.setStroke(parseStroke(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase(fillSt)) {
                mark.setFill(parseFill(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("WellKnownName")) {
                LOGGER.finest("setting mark to "
                + child.getFirstChild().getNodeValue());
                mark.setWellKnownName(parseCssParameter(child));
            }
        }
        
        return mark;
    }
    
    private ExternalGraphic parseExternalGraphic(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("processing external graphic ");
        }
        
        String format = "";
        String uri = "";
        Map paramList = new HashMap();
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("OnLineResource")) {
                Element param = (Element) child;
                org.w3c.dom.NamedNodeMap map = param.getAttributes();
                
                LOGGER.finest("attributes " + map.toString());
                
                for (int k = 0; k < map.getLength(); k++) {
                    String res = map.item(k).getNodeValue();
                    String name = map.item(k).getLocalName();
                    
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("processing attribute " + name + "="
                        + res);
                    }
                    
                    // TODO: process the name space properly
                    if (map.item(k).getLocalName().equalsIgnoreCase("xlink:href")) {
                        LOGGER.finest("seting ExtGraph uri " + res);
                        uri = res;
                    }
                }
            }
            
            if (child.getLocalName().equalsIgnoreCase("format")) {
                LOGGER.finest("format child is " + child);
                LOGGER.finest("seting ExtGraph format "
                + child.getFirstChild().getNodeValue());
                format = (child.getFirstChild().getNodeValue());
            }
            if (child.getLocalName().equalsIgnoreCase("customProperty")) {
                LOGGER.finest("custom child is " + child);
                String propName = child.getAttributes().getNamedItem("name").getNodeValue();
                LOGGER.finest("seting custom property " + propName + " to "
                + child.getFirstChild().getNodeValue());
                Expression value = parseCssParameter(child); 
                paramList.put(propName, value);
                
            }
        }
        
        ExternalGraphic extgraph = factory.createExternalGraphic(uri, format);
        extgraph.setCustomProperties(paramList);
        return extgraph;
    }
    
    private Stroke parseStroke(Node root) {
        Stroke stroke = factory.getDefaultStroke();
        NodeList list = findElements(((Element) root),"GraphicFill");
        
        if (list.getLength() > 0) {
            LOGGER.finest("stroke: found a graphic fill " + list.item(0));
            
            NodeList kids = list.item(0).getChildNodes();
            
            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);
                
                if ((child == null)
                || (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }
                
                if (child.getLocalName().equalsIgnoreCase(graphicSt)) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting stroke graphicfill with " + g);
                    stroke.setGraphicFill(g);
                }
            }
        }
        
        list = findElements(((Element) root),"GraphicStroke");
        
        if (list.getLength() > 0) {
            LOGGER.finest("stroke: found a graphic stroke " + list.item(0));
            
            NodeList kids = list.item(0).getChildNodes();
            
            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);
                
                if ((child == null)
                || (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }
                
                if (child.getLocalName().equalsIgnoreCase(graphicSt)) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting stroke graphicStroke with " + g);
                    stroke.setGraphicStroke(g);
                }
            }
        }
        
        list = findElements(((Element) root),"CssParameter");
        
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("now I am processing " + child);
            }
            
            Element param = (Element) child;
            org.w3c.dom.NamedNodeMap map = param.getAttributes();
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("attributes " + map.toString());
            }
            
            for (int k = 0; k < map.getLength(); k++) {
                String res = map.item(k).getNodeValue();
                
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("processing attribute " + res);
                }
                
                if (res.equalsIgnoreCase("stroke")) {
                    stroke.setColor(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("width")
                || res.equalsIgnoreCase("stroke-width")) {
                    stroke.setWidth(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("opacity")
                || res.equalsIgnoreCase("stroke-opacity")) {
                    stroke.setOpacity(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("linecap")
                || res.equalsIgnoreCase("stroke-linecap")) {
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineCap(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("linejoin")
                || res.equalsIgnoreCase("stroke-linejoin")) {
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineJoin(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("dasharray")
                        || res.equalsIgnoreCase("stroke-dasharray")) {
                    String dashString = child.getFirstChild().getNodeValue();
                    StringTokenizer stok = new StringTokenizer(dashString, " ");
                    float[] dashes = new float[stok.countTokens()];

                    for (int l = 0; l < dashes.length; l++) {
                        dashes[l] = Float.parseFloat(stok.nextToken());
                    }
                    
                    stroke.setDashArray(dashes);
                }
                
                if (res.equalsIgnoreCase("dashoffset")
                || res.equalsIgnoreCase("stroke-dashoffset")) {
                    stroke.setDashOffset(parseCssParameter(child));
                }
            }
        }
        
        return stroke;
    }
    
    private Fill parseFill(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing fill ");
        }
        
        Fill fill = factory.getDefaultFill();
        NodeList list = findElements(((Element) root),"GraphicFill");
        
        if (list.getLength() > 0) {
            LOGGER.finest("fill found a graphic fill " + list.item(0));
            
            NodeList kids = list.item(0).getChildNodes();
            
            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);
                
                if ((child == null)
                || (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }
                
                if (child.getLocalName().equalsIgnoreCase(graphicSt)) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting fill graphic with " + g);
                    fill.setGraphicFill(g);
                }
            }
        }
        
        list = findElements(((Element) root),"CssParameter");
        
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            Element param = (Element) child;
            org.w3c.dom.NamedNodeMap map = param.getAttributes();
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("now I am processing " + child);
            }
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("attributes " + map.toString());
            }
            
            for (int k = 0; k < map.getLength(); k++) {
                String res = map.item(k).getNodeValue();
                
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("processing attribute " + res);
                }
                
                if (res.equalsIgnoreCase(fillSt)) {
                    fill.setColor(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("opacity")
                || res.equalsIgnoreCase("fill-opacity")) {
                    fill.setOpacity(parseCssParameter(child));
                }
            }
        }
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("fill graphic " + fill.getGraphicFill());
        }
        
        return fill;
    }
    
    private Expression parseCssParameter(Node root) {
        Expression ret = null;
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsingCssParam " + root);
        }
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("about to parse " + child.getLocalName());
            }
            
            ret = org.geotools.filter.ExpressionDOMParser.parseExpression(child);
            
            break;
        }
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("no children in CssParam");
        }
        
        if (ret == null) {
            Element literal = dom.createElement("literal");
            Node child = dom.createTextNode(root.getFirstChild().getNodeValue());
            
            literal.appendChild(child);
            
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Built new literal " + literal);
            }
            
            ret = org.geotools.filter.ExpressionDOMParser.parseExpression(literal);
        }
        
        return ret;
    }
    
    private Font parseFont(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing font");
        }
        
        Font font = factory.getDefaultFont();
        NodeList list = findElements(((Element) root),"CssParameter");
        
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            Element param = (Element) child;
            org.w3c.dom.NamedNodeMap map = param.getAttributes();
            
            for (int k = 0; k < map.getLength(); k++) {
                String res = map.item(k).getNodeValue();
                
                if (res.equalsIgnoreCase("font-family")) {
                    font.setFontFamily(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("font-style")) {
                    font.setFontStyle(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("font-size")) {
                    font.setFontSize(parseCssParameter(child));
                }
                
                if (res.equalsIgnoreCase("font-weight")) {
                    font.setFontWeight(parseCssParameter(child));
                }
            }
        }
        
        return font;
    }
    
    private LabelPlacement parseLabelPlacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing labelPlacement");
        }
        
        LabelPlacement ret = null;
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("PointPlacement")) {
                ret = parsePointPlacement(child);
            }
            
            if (child.getLocalName().equalsIgnoreCase("LinePlacement")) {
                ret = parseLinePlacement(child);
            }
        }
        
        return ret;
    }
    
    private PointPlacement parsePointPlacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing pointPlacement");
        }
        
        Expression rotation = FILTERFACTORY.createLiteralExpression(0.0);
        AnchorPoint ap = null;
        Displacement dp = null;
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("AnchorPoint")) {
                ap = (parseAnchorPoint(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Displacement")) {
                dp = (parseDisplacement(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Rotation")) {
                rotation = (parseCssParameter(child));
            }
        }
        
        LOGGER.fine("setting anchorPoint " + ap);
        LOGGER.fine("setting displacement " + dp);
        
        PointPlacement dpp = factory.createPointPlacement(ap, dp, rotation);
        
        return dpp;
    }
    
    private LinePlacement parseLinePlacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing linePlacement");
        }
        
        Expression offset = FILTERFACTORY.createLiteralExpression(0.0);
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("PerpendicularOffset")) {
                offset = parseCssParameter(child);
            }
        }
        
        LinePlacement dlp = factory.createLinePlacement(offset);
        
        return dlp;
    }
    
    private AnchorPoint parseAnchorPoint(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing anchorPoint");
        }
        
        Expression x = null;
        Expression y = null;
        
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("AnchorPointX")) {
                x = (parseCssParameter(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("AnchorPointY")) {
                y = (parseCssParameter(child));
            }
        }
        
        AnchorPoint dap = factory.createAnchorPoint(x, y);
        
        return dap;
    }
    
    private Displacement parseDisplacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing displacment");
        }
        
        Expression x = null;
        Expression y = null;
        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase("DisplacementX")) {
                x = (parseCssParameter(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("DisplacementY")) {
                y = (parseCssParameter(child));
            }
        }
        
        Displacement dd = factory.createDisplacement(x, y);
        
        return dd;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param root
     *
     * @return
     */
    private Halo parseHalo(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing halo");
        }
        Halo halo = factory.createHalo(
            factory.createFill(FILTERFACTORY.createLiteralExpression("#FFFFFF")),
                FILTERFACTORY.createLiteralExpression(1.0));

        NodeList children = root.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }
            
            if (child.getLocalName().equalsIgnoreCase(fillSt)) {
                halo.setFill(parseFill(child));
            }
            
            if (child.getLocalName().equalsIgnoreCase("Radius")) {
                halo.setRadius(parseCssParameter(child));
            }
        }
        
        return halo;
    }

}
