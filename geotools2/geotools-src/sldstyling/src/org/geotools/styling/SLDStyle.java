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
 * @version $Id: SLDStyle.java,v 1.20 2002/09/03 16:05:54 ianturton Exp $
 * @author Ian Turton
 */
public class SLDStyle implements org.geotools.styling.Style {
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.styling");
    private String abstractStr = new String();
    private String name = new String();
    private String title = new String();
    private ArrayList fts = new ArrayList();
    private boolean defaultB;
    private InputStream instream;
    private Document dom;

    /**
     * Creates a new instance of SLDStyler
     * 
     * @param filename The file to be read.
     */
    public SLDStyle(String filename) {
        File f = new File(filename);
        setInput(f);
        readXML();
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param f the File to be read
     */
    public SLDStyle(File f) {
        setInput(f);
        readXML();
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param url the URL to be read.
     */
    public SLDStyle(URL url) {
        setInput(url);
        readXML();
    }

    /**
     * Creates a new SLDStyle object.
     * 
     * @param s The inputstream to be read
     */
    public SLDStyle(InputStream s) {
        instream = s;
        readXML();
    }

    private void setInput(File f) {
        try {
            instream = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            System.out.println("file " + f + " not found\n" + e);
            instream = null;
        }
    }

    private void setInput(URL url) {
        try {
            instream = url.openStream();
        } catch (IOException e) {
            System.out.println("IO Exception in SLDStyler");
            instream = null;
        }
    }

    /**
     * Fetches the abstract associated with this SLD style
     * 
     * @return The abstract
     */
    public String getAbstract() {
        return abstractStr;
    }

    private void addFeatureTypeStyle(FeatureTypeStyle ft) {
        fts.add(ft);
    }

    /**
     * Fetches the featureTypeStyles used in this style
     * 
     * @return an array of FeatureTypeStyle
     */
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        return (FeatureTypeStyle[]) fts.toArray(new FeatureTypeStyle[0]);
    }

    /**
     * get the name of the style
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the title of the style
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Determines if this style is a default style
     * 
     * @return true if the style is a default
     */
    public boolean isDefault() {
        return defaultB;
    }

    /**
     * Setter for property name.
     * 
     * @param name New value of property name.
     */
    private void setName(java.lang.String name) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("setting name " + name);
        }

        this.name = name;
    }

    /**
     * Setter for property abstractStr.
     * 
     * @param abstractStr New value of property abstractStr.
     */
    private void setAbstract(java.lang.String abstractStr) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("setting abstract " + abstractStr);
        }

        this.abstractStr = abstractStr;
    }

    /**
     * Setter for property title.
     * 
     * @param title New value of property title.
     */
    private void setTitle(java.lang.String title) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("setting title " + title);
        }

        this.title = title;
    }

    private void readXML() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(instream);
        } catch (Exception e) {
            System.out.println("exception in sldStyler " + e);
        }

        // for our next trick do something with the dom.
        NodeList nodes = dom.getElementsByTagName("UserStyle");

        if (nodes.getLength() > 1) {
            System.out.println(
                    "Currently only able to handle one user style per sld file" + 
                    " only parsing first one found");
        }

        Node n = nodes.item(0);

        NodeList children = n.getChildNodes();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("" + children.getLength() + " children to process");
        }

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
                setName(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Title")) {
                setTitle(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Abstract")) {
                setAbstract(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("FeatureTypeStyle")) {
                addFeatureTypeStyle(parseFeatureTypeStyle(child));
            }
        }
    }

    private FeatureTypeStyle parseFeatureTypeStyle(Node style) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Parsing featuretype style " + style.getNodeName());
        }

        DefaultFeatureTypeStyle ft = new DefaultFeatureTypeStyle();

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

        DefaultRule rule = new DefaultRule();
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

            if (child.getNodeName().equalsIgnoreCase("Name")) {
                rule.setName(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Title")) {
                rule.setTitle(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Abstract")) {
                rule.setAbstract(child.getFirstChild().getNodeValue());
            }

            if (child.getNodeName().equalsIgnoreCase("Filter")) {
                NodeList list = child.getChildNodes();
                Node kid = null;

                for (int k = 0; k < list.getLength(); k++) {
                    kid = list.item(k);

                    if ((kid == null) || 
                            (kid.getNodeType() != Node.ELEMENT_NODE)) {
                        continue;
                    }

                    Filter filter = FilterXMLParser.parseFilter(kid);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("filter: " + 
                                      filter.getClass().toString());
                    }

                    LOGGER.info("parsed: " + filter.toString());
                    rule.setFilter(filter);
                }
            }

            if (child.getNodeName().equalsIgnoreCase("ElseFilter")) {
                rule.setHasElseFilter();
            }

            if (child.getNodeName().equalsIgnoreCase("LegendGraphic")) {
                NodeList g = ((Element) child).getElementsByTagName("Graphic");

                for (int k = 0; k < g.getLength(); k++) {
                    rule.addLegendGraphic(parseGraphic(g.item(k)));
                }
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
    public LineSymbolizer parseLineSymbolizer(Node root) {
        DefaultLineSymbolizer symbol = new DefaultLineSymbolizer();
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
    public PolygonSymbolizer parsePolygonSymbolizer(Node root) {
        DefaultPolygonSymbolizer symbol = new DefaultPolygonSymbolizer();
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
    public TextSymbolizer parseTextSymbolizer(Node root) {
        DefaultTextSymbolizer symbol = new DefaultTextSymbolizer();
        symbol.setFill(null);

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
                symbol.setFill((DefaultFill) parseFill(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Label")) {
                LOGGER.finest("parsing label " + child.getNodeValue());
                symbol.setLabel(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Font")) {
                symbol.addFont(parseFont(child));
            }

            if (child.getNodeName().equalsIgnoreCase("LabelPlacement")) {
                symbol.setLabelPlacement(parseLabelPlacement(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Halo")) {
                symbol.setHalo(parseHalo(child));
            }
        }

        return symbol;
    }

    /**
     * parses the SLD for a point symbolizer
     * 
     * @param root a w3c dom node
     * 
     * @return the pointsymbolizer
     */
    public PointSymbolizer parsePointSymbolizer(Node root) {
        DefaultPointSymbolizer symbol = new DefaultPointSymbolizer();
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

        DefaultGraphic graphic = new DefaultGraphic();

        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Geometry")) {
                //TODO: add this method to Graphic
                //graphic.setGeometryPropertyName(parseGeometryName(child));
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

    private DefaultMark parseMark(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing mark");
        }

        DefaultMark mark = new DefaultMark();
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

        DefaultExternalGraphic extgraph = new DefaultExternalGraphic();
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
                        extgraph.setURI(res);
                    }
                }
            }

            if (child.getNodeName().equalsIgnoreCase("format")) {
                LOGGER.finest("format child is " + child);
                LOGGER.finest("seting ExtGraph format " + 
                              child.getFirstChild().getNodeValue());
                extgraph.setFormat(child.getFirstChild().getNodeValue());
            }
        }

        return extgraph;
    }

    private Stroke parseStroke(Node root) {
        DefaultStroke stroke = new DefaultStroke();
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

        DefaultFill fill = new DefaultFill();
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
        LOGGER.info("parsingCssParam " + root);

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

            return ExpressionXmlParser.parseExpression(child);
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

        return ExpressionXmlParser.parseExpression(literal);
    }

    private Font parseFont(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing font");
        }

        DefaultFont font = new DefaultFont();
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

        DefaultPointPlacement dpp = new DefaultPointPlacement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPoint")) {
                dpp.setAnchorPoint(parseAnchorPoint(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Displacement")) {
                dpp.setDisplacement(parseDisplacement(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Rotation")) {
                dpp.setRotation(parseCssParameter(child));
            }
        }

        return dpp;
    }

    private LinePlacement parseLinePlacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing linePlacement");
        }

        DefaultLinePlacement dlp = new DefaultLinePlacement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("PerpendicularOffset")) {
                dlp.setPerpendicularOffset(parseCssParameter(child));
            }
        }

        return dlp;
    }

    private AnchorPoint parseAnchorPoint(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing anchorPoint");
        }

        DefaultAnchorPoint dap = new DefaultAnchorPoint();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPointX")) {
                dap.setAnchorPointX(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("AnchorPointY")) {
                dap.setAnchorPointY(parseCssParameter(child));
            }
        }

        return dap;
    }

    private Displacement parseDisplacement(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing displacment");
        }

        DefaultDisplacement dd = new DefaultDisplacement();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("DisplacementX")) {
                dd.setDisplacementX(parseCssParameter(child));
            }

            if (child.getNodeName().equalsIgnoreCase("DisplacementY")) {
                dd.setDisplacementY(parseCssParameter(child));
            }
        }

        return dd;
    }

    private Halo parseHalo(Node root) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("parsing halo");
        }

        DefaultHalo halo = new DefaultHalo();
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);

            if ((child == null) || 
                    (child.getNodeType() != Node.ELEMENT_NODE)) {
                continue;
            }

            if (child.getNodeName().equalsIgnoreCase("Fill")) {
                halo.setFill((DefaultFill) parseFill(child));
            }

            if (child.getNodeName().equalsIgnoreCase("Radius")) {
                halo.setRadius(parseCssParameter(child));
            }
        }

        return halo;
    }
}