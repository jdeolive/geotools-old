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

import java.io.*;

import java.net.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.*;

import org.geotools.filter.*;

import org.w3c.dom.*;


/**
 * A class to read and parse an SLD file based on verion 0.7.2 of the OGC
 * Styled Layer Descriptor Spec.
 *
 * 
 * @version $Id: SLDStyle.java,v 1.32 2003/03/20 14:16:34 ianturton Exp $
 * @author Ian Turton
 * @author Sean Geoghegan <Sean.Geoghegan@dsto.defence.gov.au>
 */
public class SLDStyle {
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.styling");
    private static final org.geotools.filter.FilterFactory filterFactory = 
            org.geotools.filter.FilterFactory.createFilterFactory();
    private InputStream instream;
    private Document dom;
    private StyleFactory factory;

    public SLDStyle(StyleFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates a new instance of SLDStyler
     * 
     * @param filename The file to be read.
     */
    public SLDStyle(StyleFactory factory, String filename) {
        this(factory);

        File f = new File(filename);
        setInput(f);
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param f the File to be read
     */
    public SLDStyle(StyleFactory factory, File f) {
        this(factory);
        setInput(f);
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param url the URL to be read.
     */
    public SLDStyle(StyleFactory factory, URL url) {
        this(factory);
        setInput(url);
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param s The inputstream to be read
     */
    public SLDStyle(StyleFactory factory, InputStream s) {
        this(factory);
        instream = s;
    }

    public void setInput(String filename) {
        try {
            instream = new FileInputStream(new File(filename));
        } catch (FileNotFoundException e) {
            System.out.println("file " + filename + " not found\n" + e);
            instream = null;
        }
    }

    public void setInput(File f) {
        try {
            instream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println("file " + f + " not found\n" + e);
            instream = null;
        }
    }

    public void setInput(URL url) {
        try {
            instream = url.openStream();
        } catch (IOException e) {
            System.out.println("IO Exception in SLDStyler");
            instream = null;
        }
    }

    public void setInput(InputStream in) {
        instream = in;
    }

    /**
     * Read the xml inputsource provided and create a Style object for each user style found
     * @return Style[] the styles constructed.
     */   
    public Style[] readXML() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(instream);
        } catch (Exception e) {
            System.out.println("exception in sldStyler " + e);
        }
        
        return readDOM(dom);
    }
    
    /**
     * Read the DOM provided and create a Style object for each user style found
     * @return Style[] the styles constructed.
     */   
    public Style[] readDOM(Document dom){
        // for our next trick do something with the dom.
        NodeList nodes = dom.getElementsByTagName("UserStyle");
        Style[] styles = new Style[nodes.getLength()];
        
        for(int i=0;i<nodes.getLength();i++){
            styles[i] = parseStyle(nodes.item(i));
        }
        
        return styles;
    }
    
    /** build a style for the Node provided
     * @param Node n the node which contains the style to be parsed.
     * @return the Style constructed.
     */
    public Style parseStyle(Node n){
        
            Style style = factory.createStyle();

            NodeList children = n.getChildNodes();

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("" + children.getLength() + 
                              " children to process");
            }

            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);

                if ((child == null) || 
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("processing " + child.getNodeName());
                }

                if (child.getNodeName().equalsIgnoreCase("Name")) {
                    style.setName(child.getFirstChild().getNodeValue());
                }

                if (child.getNodeName().equalsIgnoreCase("Title")) {
                    style.setTitle(child.getFirstChild().getNodeValue());
                }

                if (child.getNodeName().equalsIgnoreCase("Abstract")) {
                    style.setAbstract(child.getFirstChild().getNodeValue());
                }

                if (child.getNodeName().equalsIgnoreCase("FeatureTypeStyle")) {
                    style.addFeatureTypeStyle(parseFeatureTypeStyle(child));
                }
            }
        
        return style; 
    }
    
    private FeatureTypeStyle parseFeatureTypeStyle(Node style) { 
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Parsing featuretype style " + style.getNodeName());
        }

        FeatureTypeStyle ft = factory.createFeatureTypeStyle();

        ArrayList rules = new ArrayList();
        NodeList children = style.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("processing " + child.getNodeName());
            }

            if (child.getNodeName().equalsIgnoreCase("Name")) {
                ft.setName(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Title")) {
                ft.setTitle(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Abstract")) {
                ft.setAbstract(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("FeatureTypeName")) {
                ft.setFeatureTypeName(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("SemanticTypeIdentifier")) {
                // experimental part of the spec
                // probably ignore it for now
            }

            if (child.getNodeName().equalsIgnoreCase("Rule")) {
                rules.add(parseRule(child));
            }
        }

        ft.setRules((Rule[]) rules.toArray(new Rule[0]));

        return ft;
    }

    private Rule parseRule(Node ruleNode) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Parsing rule " + ruleNode.getNodeName());
        }

        Rule rule = factory.createRule();
        ArrayList symbolizers = new ArrayList();
        NodeList children = ruleNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("processing " + child.getNodeName());
            }

            //            if (child.getNodeName().equalsIgnoreCase("Name")) {
            //                rule.setName(child.getFirstChild().getNodeValue());
            //            }
            //
            //            if (child.getNodeName().equalsIgnoreCase("Title")) {
            //                rule.setTitle(child.getFirstChild().getNodeValue());
            //            }
            //
            //            if (child.getNodeName().equalsIgnoreCase("Abstract")) {
            //                rule.setAbstract(child.getFirstChild().getNodeValue());
            //            }
            if (child.getNodeName().equalsIgnoreCase("Filter")) {
                NodeList list = child.getChildNodes();
                Node kid = null;

                for (int k = 0; k < list.getLength(); k++) {
                    kid = list.item(k);

                    if ((kid == null) || 
                            (kid.getNodeType() != Node.ELEMENT_NODE)) {
                        continue;
                    }

                    Filter filter = FilterDOMParser.parseFilter(kid);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("filter: " + 
                                      filter.getClass().toString());
                        LOGGER.finest("parsed: " + filter.toString());
                    }

                    rule.setFilter(filter);
                }
            }

            if (child.getNodeName().equalsIgnoreCase("ElseFilter")) {
                rule.setIsElseFilter(true);
            }

            if (child.getNodeName().equalsIgnoreCase("LegendGraphic")) {
                NodeList g = ((Element) child).getElementsByTagName("Graphic");
                ArrayList legends = new ArrayList();

                for (int k = 0; k < g.getLength(); k++) {
                    legends.add(parseGraphic(g.item(k)));
                }

                rule.setLegendGraphic(
                        (Graphic[]) legends.toArray(new Graphic[0]));
            }

            if (child.getNodeName().equalsIgnoreCase("LineSymbolizer")) {
                symbolizers.add(parseLineSymbolizer(child));
            }

            if (child.getNodeName().equalsIgnoreCase("PolygonSymbolizer")) {
                symbolizers.add(parsePolygonSymbolizer(child));
            }

            if (child.getNodeName().equalsIgnoreCase("PointSymbolizer")) {
                symbolizers.add(parsePointSymbolizer(child));
            }

            if (child.getNodeName().equalsIgnoreCase("TextSymbolizer")) {
                symbolizers.add(parseTextSymbolizer(child));
            }

            if (child.getNodeName().equalsIgnoreCase("RasterSymbolizer")) {
                //TODO: implement symbolizers.add(parseRasterSymbolizer(Child));
            }
        }

        rule.setSymbolizers(
                (Symbolizer[]) symbolizers.toArray(new Symbolizer[0]));

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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Stroke")) {
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Stroke")) {
                symbol.setStroke(parseStroke(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Fill")) {
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Fill")) {
                symbol.setFill(parseFill(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Label")) {
                LOGGER.finest("parsing label " + child.getNodeValue());
                symbol.setLabel(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Font")) {
                fonts.add(parseFont(child));
            }

            if (child.getNodeName().equalsIgnoreCase("LabelPlacement")) {
                symbol.setLabelPlacement(parseLabelPlacement(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Halo")) {
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
        PointSymbolizer symbol = factory.createPointSymbolizer();
        symbol.setGraphic(null);

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                symbol.setGeometryPropertyName(parseGeometryName(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Graphic")) {
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                graphic.setGeometryPropertyName(parseGeometryName(child));
            }

            if (child.getNodeName().equalsIgnoreCase("ExternalGraphic")) {
                LOGGER.finest("parsing extgraphic " + child);
                graphic.addExternalGraphic(parseExternalGraphic(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Mark")) {
                graphic.addMark(parseMark(child));
            }

            if (child.getNodeName().equalsIgnoreCase("opacity")) {
                graphic.setOpacity(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("size")) {
                graphic.setSize(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("rotation")) {
                graphic.setRotation(parseCssParameter(child));
            }
        }

        return graphic;
    }

    private String parseGeometryName(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing GeometryName");
        }

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            return parseCssParameter(child).toString();
        }

        return null;
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Stroke")) {
                mark.setStroke(parseStroke(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Fill")) {
                mark.setFill(parseFill(child));
            }

            if (child.getNodeName().equalsIgnoreCase("WellKnownName")) {
                LOGGER.finest("setting mark to " + 
                              child.getFirstChild().getNodeValue());
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

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("OnLineResource")) {
                Element param = (Element) child;
                NamedNodeMap map = param.getAttributes();

                LOGGER.finest("attributes " + map.toString());

                for (int k = 0; k < map.getLength(); k++) {
                    String res = map.item(k).getNodeValue();
                    String name = map.item(k).getNodeName();

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("processing attribute " + name + "=" + 
                                      res);
                    }

                    // TODO: process the name space properly
                    if (map.item(k).getNodeName()
                           .equalsIgnoreCase("xlink:href")) {
                        LOGGER.finest("seting ExtGraph uri " + res);
                        uri = res;
                    }
                }
            }

            if (child.getNodeName().equalsIgnoreCase("format")) {
                LOGGER.finest("format child is " + child);
                LOGGER.finest("seting ExtGraph format " + 
                              child.getFirstChild().getNodeValue());
                format = (child.getFirstChild().getNodeValue());
            }
        }

        ExternalGraphic extgraph = factory.createExternalGraphic(uri, format);

        return extgraph;
    }

    private Stroke parseStroke(Node root) {
        Stroke stroke = factory.getDefaultStroke();
        NodeList list = ((Element) root).getElementsByTagName("GraphicFill");

        if (list.getLength() > 0) {
            LOGGER.finest("stroke: found a graphic fill " + list.item(0));

            NodeList kids = list.item(0).getChildNodes();

            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);

                if ((child == null) || 
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                if (child.getNodeName().equalsIgnoreCase("Graphic")) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting stroke graphicfill with " + g);
                    stroke.setGraphicFill(g);
                }
            }
        }

        list = ((Element) root).getElementsByTagName("GraphicStroke");

        if (list.getLength() > 0) {
            LOGGER.finest("stroke: found a graphic stroke " + list.item(0));

            NodeList kids = list.item(0).getChildNodes();

            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);

                if ((child == null) || 
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                if (child.getNodeName().equalsIgnoreCase("Graphic")) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting stroke graphicStroke with " + g);
                    stroke.setGraphicStroke(g);
                }
            }
        }

        list = ((Element) root).getElementsByTagName("CssParameter");

        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("now I am processing " + child);
            }

            Element param = (Element) child;
            NamedNodeMap map = param.getAttributes();

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

                if (res.equalsIgnoreCase("width") || 
                        res.equalsIgnoreCase("stroke-width")) {
                    stroke.setWidth(parseCssParameter(child));
                }

                if (res.equalsIgnoreCase("opacity") || 
                        res.equalsIgnoreCase("stroke-opacity")) {
                    stroke.setOpacity(parseCssParameter(child));
                }

                if (res.equalsIgnoreCase("linecap") || 
                        res.equalsIgnoreCase("stroke-linecap")) {
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineCap(parseCssParameter(child));
                }

                if (res.equalsIgnoreCase("linejoin") || 
                        res.equalsIgnoreCase("stroke-linejoin")) {
                    // since these are system-dependent just pass them through and hope.
                    stroke.setLineJoin(parseCssParameter(child));
                }

                if (res.equalsIgnoreCase("dasharray") || 
                        res.equalsIgnoreCase("stroke-dasharray")) {
                    StringTokenizer stok = new StringTokenizer(
                                                   child.getFirstChild()
                                                        .getNodeValue(), " ");
                    float[] dashes = new float[stok.countTokens()];

                    for (int l = 0; l < stok.countTokens(); l++) {
                        dashes[l] = Float.parseFloat(stok.nextToken());
                    }

                    stroke.setDashArray(dashes);
                }

                if (res.equalsIgnoreCase("dashoffset") || 
                        res.equalsIgnoreCase("stroke-dashoffset")) {
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
        NodeList list = ((Element) root).getElementsByTagName("GraphicFill");

        if (list.getLength() > 0) {
            LOGGER.finest("fill found a graphic fill " + list.item(0));

            NodeList kids = list.item(0).getChildNodes();

            for (int i = 0; i < kids.getLength(); i++) {
                Node child = kids.item(i);

                if ((child == null) || 
                        (child.getNodeType() != Node.ELEMENT_NODE)) {
                    continue;
                }

                if (child.getNodeName().equalsIgnoreCase("Graphic")) {
                    Graphic g = parseGraphic(child);
                    LOGGER.finest("setting fill graphic with " + g);
                    fill.setGraphicFill(g);
                }
            }
        }

        list = ((Element) root).getElementsByTagName("CssParameter");

        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            Element param = (Element) child;
            NamedNodeMap map = param.getAttributes();

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

                if (res.equalsIgnoreCase("fill")) {
                    fill.setColor(parseCssParameter(child));
                }

                if (res.equalsIgnoreCase("opacity") || 
                        res.equalsIgnoreCase("fill-opacity")) {
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsingCssParam " + root);
        }

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("about to parse " + child.getNodeName());
            }

            return ExpressionDOMParser.parseExpression(child);
        }

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("no children in CssParam");
        }

        Element literal = dom.createElement("literal");
        Node child = dom.createTextNode(root.getFirstChild().getNodeValue());

        literal.appendChild(child);

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Built new literal " + literal);
        }

        return ExpressionDOMParser.parseExpression(literal);
    }

    private Font parseFont(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing font");
        }

        Font font = factory.getDefaultFont();
        NodeList list = ((Element) root).getElementsByTagName("CssParameter");

        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            Element param = (Element) child;
            NamedNodeMap map = param.getAttributes();

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

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("PointPlacement")) {
                return parsePointPlacement(child);
            }

            if (child.getNodeName().equalsIgnoreCase("LinePlacement")) {
                return parseLinePlacement(child);
            }
        }

        return null;
    }

    private PointPlacement parsePointPlacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing pointPlacement");
        }

        Expression rotation = filterFactory.createLiteralExpression(0.0);
        AnchorPoint ap = null;
        Displacement dp = null;

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPoint")) {
                ap = (parseAnchorPoint(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Displacement")) {
                dp = (parseDisplacement(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Rotation")) {
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

        Expression offset = filterFactory.createLiteralExpression(0.0);
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("PerpendicularOffset")) {
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPointX")) {
                x = (parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPointY")) {
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

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("DisplacementX")) {
                x = (parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("DisplacementY")) {
                y = (parseCssParameter(child));
            }
        }

        Displacement dd = factory.createDisplacement(x, y);

        return dd;
    }

    private Halo parseHalo(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing halo");
        }

        Halo halo = factory.createHalo(factory.getDefaultFill(), 
                                       filterFactory.createLiteralExpression(
                                               0.0));

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Fill")) {
                halo.setFill(parseFill(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Radius")) {
                halo.setRadius(parseCssParameter(child));
            }
        }

        return halo;
    }
}