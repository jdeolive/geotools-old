/*
 * ExpressionXmlParser.java
 *
 * Created on 03 July 2002, 10:21
 */

package org.geotools.filter;

/**
 *
 * @author  iant
 */
import org.w3c.dom.*;
import com.vividsolutions.jts.geom.*;
import java.util.*;

public class ExpressionXmlParser {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(ExpressionXmlParser.class);
    /** Creates a new instance of ExpressionXmlParser */
    public ExpressionXmlParser() {
    }
    public static Expression parseExpression(Node root){
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
            _log.info("processing literal "+child);
            
            NodeList kidList = child.getChildNodes();
            //_log.debug("literal elements ("+kidList.getLength()+") "+kidList.toString());
            for(int i=0;i<kidList.getLength();i++){
                Node kid = kidList.item(i);
                //_log.debug("kid "+i+" "+kid);
                if(kid==null){
                    //_log.debug("Skipping ");
                    continue;
                }
                if(kid.getNodeValue()==null){
                    /* it might be a gml string so we need to convert it into a geometry
                     * this is a bit tricky since our standard gml parser is SAX based and
                     * we're a DOM here.
                     */
                    _log.debug("node "+kid.getNodeValue()+" namespace "+kid.getNamespaceURI());
                    _log.info("a literal gml string?");
                    try{
                        Geometry geom = parseGML(kid);
                        if(geom!=null){
                            _log.debug("built a "+geom.getGeometryType()+" from gml");
                            _log.debug("\tpoints: "+geom.getNumPoints());
                        }else{
                            _log.debug("got a null geometry back from gml parser");
                        }
                        return new ExpressionLiteral(geom);
                    } catch (IllegalFilterException ife){
                        _log.error("Problem building GML/JTS object",ife);
                    }
                    return null;
                }
                if(kid.getNodeValue().trim().length()==0){
                    //_log.debug("empty text element");
                    continue;
                }
                // debuging only
                /*switch(kid.getNodeType()){
                    case Node.ELEMENT_NODE:
                        _log.debug("element :"+kid);
                        break;
                    case Node.TEXT_NODE:
                        _log.debug("text :"+kid);
                        break;
                    case Node.ATTRIBUTE_NODE:
                        _log.debug("Attribute :"+kid);
                        break;
                    case Node.CDATA_SECTION_NODE:
                        _log.debug("Cdata :"+kid);
                        break;
                    case Node.COMMENT_NODE:
                        _log.debug("comment :"+kid);
                        break;
                } */
                
                
                String nodeValue = kid.getNodeValue();
                _log.debug("processing "+nodeValue);
                
                // see if it's an int
                try{
                    try{
                        Integer I = new Integer(nodeValue);
                        _log.debug("An integer");
                        return new ExpressionLiteral(I);
                    } catch (NumberFormatException e){
                        /* really empty */
                    }
                    // A double?
                    try{
                        Double D = new Double(nodeValue);
                        _log.debug("A double");
                        return new ExpressionLiteral(D);
                    } catch (NumberFormatException e){
                        /* really empty */
                    }
                    // must be a string (or we have a problem)
                    _log.debug("defaulting to string");
                    return new ExpressionLiteral(nodeValue);
                } catch (IllegalFilterException ife){
                    _log.error("Unable to build expression ",ife);
                    return null;
                }
            }
            
        }
        if(child.getNodeName().equalsIgnoreCase("PropertyName")){
            try{
                NodeList kids = child.getChildNodes();
                ExpressionAttribute attribute = new ExpressionAttribute(null);
                attribute.setAttributePath(child.getFirstChild().getNodeValue());
                return attribute;
            }catch (IllegalFilterException ife){
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        if(child.getNodeName().equalsIgnoreCase("Function")){
            _log.info("function not yet implemented");
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
    /** parsez short sections of gml for use in expressions and filters
     * Hopefully we can get away without a full parser here.
     */
    static GeometryFactory gfac = new GeometryFactory();
    public static Geometry parseGML(Node root){
        _log.debug("processing gml "+root);
        java.util.ArrayList coords = new java.util.ArrayList();
        int type = 0;
        Node child = root;
        
        if(child.getNodeName().equalsIgnoreCase("gml:box")){
            _log.debug("box");
            type = GML_BOX;
            coords = parseCoords(child);
            com.vividsolutions.jts.geom.Envelope env = new com.vividsolutions.jts.geom.Envelope();
            for(int i=0;i<coords.size();i++){
                env.expandToInclude((Coordinate)coords.get(i));
            }
            Coordinate[] c = new Coordinate[5];
            c[0] = new Coordinate(env.getMinX(), env.getMinY());
            c[1] = new Coordinate(env.getMinX(), env.getMaxY());
            c[2] = new Coordinate(env.getMaxX(), env.getMaxY());
            c[3] = new Coordinate(env.getMaxX(), env.getMinY());
            c[4] = new Coordinate(env.getMinX(), env.getMinY());
            com.vividsolutions.jts.geom.LinearRing r = null;
            try {
                r = gfac.createLinearRing(c);
            } catch (com.vividsolutions.jts.geom.TopologyException e){
                System.err.println("Topology Exception in GMLBox");
                return null;
            }
            return gfac.createPolygon(r, null);
            
        }
        if(child.getNodeName().equalsIgnoreCase("gml:polygon")){
            _log.debug("polygon");
            type = GML_POLYGON;
            LinearRing  outer =null;
            ArrayList inner = new ArrayList();
            NodeList kids = root.getChildNodes();
            for(int i=0;i<kids.getLength();i++){
                Node kid = kids.item(i);
                _log.debug("doing "+kid);
                if(kid.getNodeName().equalsIgnoreCase("gml:outerBoundaryIs")){
                    outer = (LinearRing) parseGML(kid);
                }
                if(kid.getNodeName().equalsIgnoreCase("gml:innerBoundaryIs")){
                    inner.add((LinearRing) parseGML(kid));
                }
            }
            if(inner.size()>0){
                return gfac.createPolygon(outer,(LinearRing[]) inner.toArray(new LinearRing[0]));
            }else{
                return gfac.createPolygon(outer, null);
            }
        }
        if(child.getNodeName().equalsIgnoreCase("gml:outerBoundaryIs") ||
        child.getNodeName().equalsIgnoreCase("gml:innerBoundaryIs") ){
            _log.debug("Boundary layer");
            NodeList kids = ((Element)child).getElementsByTagName("gml:LinearRing");
            
            return parseGML(kids.item(0));
        }
        
        if(child.getNodeName().equalsIgnoreCase("gml:linearRing")){
            _log.debug("LinearRing");
            coords = parseCoords(child);
            com.vividsolutions.jts.geom.LinearRing r = null;
            
            try{
                r = gfac.createLinearRing((Coordinate[])coords.toArray(new Coordinate[]{}));
            } catch (TopologyException te ){
                _log.error("Topology Exception build linear ring ",te);
                return null;
            }
            return r;
        }
        
        if(child.getNodeName().equalsIgnoreCase("gml:linestring")){
            _log.debug("linestring");
            type = GML_LINESTRING;
            coords = parseCoords(child);
            com.vividsolutions.jts.geom.LineString r = null;
            r = gfac.createLineString((Coordinate[])coords.toArray(new Coordinate[]{}));
            
            return r;
        }
        if(child.getNodeName().equalsIgnoreCase("gml:point")){
            _log.debug("point");
            type = GML_POINT;
            coords = parseCoords(child);
            com.vividsolutions.jts.geom.Point r = null;
            r = gfac.createPoint((Coordinate)coords.get(0));
            return r;
        }
        if(child.getNodeName().toLowerCase().startsWith("gml:multiPolygon")){
            _log.debug("MultiPolygon");
            ArrayList gc = new ArrayList();
            // parse all children thru parseGML
            NodeList kids = child.getChildNodes();
            for(int i=0;i<kids.getLength();i++){
                gc.add(parseGML(kids.item(i)));
            }
            return gfac.createMultiPolygon((Polygon[])gc.toArray(new Polygon[0]));
        }
        
        return null;
    }
    
    public static java.util.ArrayList parseCoords(Node root){
        _log.debug("parsing coordinate(s) "+root);
        ArrayList clist = new ArrayList();
        NodeList kids = root.getChildNodes();
        for(int i=0;i<kids.getLength();i++){
            Node child = kids.item(i);
            _log.debug("doing "+child);
            if(child.getNodeName().equalsIgnoreCase("gml:coordinate")){
                String internal = child.getNodeValue();
                
                
            }
            if(child.getNodeName().equalsIgnoreCase("gml:coordinates")){
                _log.debug("coordinates "+child.getFirstChild().getNodeValue());
                NodeList grandKids = child.getChildNodes();
                for(int k=0;k<grandKids.getLength();k++){
                    Node grandKid = grandKids.item(k);
                    if(grandKid.getNodeValue()==null) continue;
                    if(grandKid.getNodeValue().trim().length()==0) continue;
                    String outer = grandKid.getNodeValue();
                    StringTokenizer ost = new StringTokenizer(outer," ");
                    while(ost.hasMoreTokens()){
                        String internal = ost.nextToken();
                        StringTokenizer ist = new StringTokenizer(internal,",");
                        double x = Double.parseDouble(ist.nextToken());
                        double y = Double.parseDouble(ist.nextToken());
                        double z = Double.NaN;
                        if(ist.hasMoreTokens()){
                            z = Double.parseDouble(ist.nextToken());
                        }
                        clist.add(new Coordinate(x,y,z));
                    }
                }
                
            }
        }
        
        return clist;
    }
    
    static int GML_BOX = 1;
    static int GML_POLYGON = 2;
    static int GML_LINESTRING = 3;
    static int GML_POINT = 4;
}
