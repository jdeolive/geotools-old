/*
 * FilterXMLParser.java
 *
 * Created on 10 July 2002, 17:14
 */

package org.geotools.filter;

// J2SE dependencies
import java.util.logging.Logger;
import org.w3c.dom.*;


/**
 * A dom based parser to build filters as per OGC 01-067
 *
 * @author  iant
 */
public class FilterDOMParser {

    /**
     * The logger for the filter module.
     */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.filter");
    private static final org.geotools.filter.FilterFactory filterFactory = org.geotools.filter.FilterFactory.createFilterFactory();
    /** Creates a new instance of FilterXMLParser */
    public FilterDOMParser() {
    }
    public static Filter parseFilter(Node root){
        LOGGER.fine("parsingFilter "+root.getNodeName());
        
        //NodeList children = root.getChildNodes();
        //LOGGER.finest("children "+children);
        
        if(root == null || root.getNodeType() != Node.ELEMENT_NODE){
            LOGGER.finer("bad node input ");
            return null;
        }
        LOGGER.finer("processing root "+root.getNodeName());
        Node child = root;
        String childName = child.getNodeName();
        LOGGER.finer("looking up "+childName);
        if(comparisions.containsKey(childName)){
            LOGGER.finer("a comparision filter "+childName);
            boolean like =false, between = false;
            try{
                short type = ((Integer)comparisions.get(childName)).shortValue();
                CompareFilter filter = null;
                if(type == AbstractFilter.BETWEEN) {
                    BetweenFilter bfilter = filterFactory.createBetweenFilter();
                    
                    NodeList kids = child.getChildNodes();
                    if(kids.getLength() < 3 ){
                        throw new IllegalFilterException("wrong number of children in Between filter: expected 3 got " + kids.getLength());
                    }
                        
                        Node value = child.getFirstChild();
                        while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                        // first expression
                        //value = kid.getFirstChild();
                        //while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                        LOGGER.fine("add middle value -> "+value+"<-");
                        bfilter.addMiddleValue(ExpressionDOMParser.parseExpression(value));
                   for(int i = 0; i< kids.getLength();i++){
                       Node kid = kids.item(i);
                        if(kid.getNodeName().equalsIgnoreCase("LowerBoundary")){
                            value = kid.getFirstChild();
                            while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                            LOGGER.fine("add left value -> "+value+"<-");
                            bfilter.addLeftValue(ExpressionDOMParser.parseExpression(value));
                        }
                        if(kid.getNodeName().equalsIgnoreCase("UpperBoundary")){
                            value = kid.getFirstChild();
                            while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                            LOGGER.fine("add right value -> "+value+"<-");
                            bfilter.addRightValue(ExpressionDOMParser.parseExpression(value));
                        }
                   }
                    
                        
                    
                    return bfilter;
                }else if(type==AbstractFilter.LIKE){
                    String wildcard=null,single=null,escape=null,pattern=null;
                    Expression value=null;
                    NodeList map = child.getChildNodes();
                    for(int i=0;i<map.getLength();i++){
                        Node kid=map.item(i);
                        if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                        String res = kid.getNodeName();
                        if(res.equalsIgnoreCase("PropertyName")){
                            value = ExpressionDOMParser.parseExpression(kid);
                        }
                        if(res.equalsIgnoreCase("Literal")){
                            pattern = ExpressionDOMParser.parseExpression(kid).toString();
                        }
                    }
                    NamedNodeMap kids = child.getAttributes();
                    for(int i=0;i<kids.getLength();i++){
                        Node kid = kids.item(i);
                        //if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                        String res = kid.getNodeName();
                        if(res.equalsIgnoreCase("wildCard")){
                            wildcard = kid.getNodeValue();
                        }
                        if(res.equalsIgnoreCase("singleChar")){
                            single = kid.getNodeValue();
                        }
                        if(res.equalsIgnoreCase("escapeChar")){
                            escape = kid.getNodeValue();
                        }
                    }
                    if(!(wildcard==null||single==null||escape==null||pattern==null)){
                        LikeFilter lfilter = filterFactory.createLikeFilter();
                        LOGGER.finer("Building like filter "+value.toString()+"\n"+pattern+" "+wildcard+" "+single+" "+escape);
                        lfilter.setValue(value);
                        lfilter.setPattern(pattern,wildcard,single,escape);
                        return lfilter;
                    }
                    LOGGER.finer("Problem building like filter\n"+pattern+" "+wildcard+" "+single+" "+escape);
                    return null;
                }else{                    
                    filter = new CompareFilterImpl(type);
                }

                // find and parse left and right values 

                Node value = child.getFirstChild();
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                LOGGER.finer("add left value -> "+value+"<-");
                filter.addLeftValue(ExpressionDOMParser.parseExpression(value));
                value = value.getNextSibling();
                
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                LOGGER.finer("add right value -> "+value+"<-");
                filter.addRightValue(ExpressionDOMParser.parseExpression(value));
                return filter;
                
            }catch (IllegalFilterException ife){
                LOGGER.warning("Unable to build filter: " + ife);
                return null;
            }
        } else if(spatial.containsKey(childName)){
            LOGGER.finer("a spatial filter "+childName);
            try{
                short type = ((Integer)spatial.get(childName)).shortValue();
                GeometryFilter filter = filterFactory.createGeometryFilter(type);
                Node value = child.getFirstChild();
                    while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                    LOGGER.finer("add left value -> "+value+"<-");
                    filter.addLeftGeometry(ExpressionDOMParser.parseExpression(value));
                    value = value.getNextSibling();
                    
                    while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                    LOGGER.finer("add right value -> "+value+"<-");
                    if(!(value.getNodeName().equalsIgnoreCase("Literal")
                    ||value.getNodeName().equalsIgnoreCase("propertyname"))){
                        Element literal = value.getOwnerDocument().createElement("literal");
                        
                        literal.appendChild(value);
                        LOGGER.finer("Built new literal "+literal);
                        value = literal;
                    }
        
                    filter.addRightGeometry(ExpressionDOMParser.parseExpression(value));
                    return filter;
                }catch (IllegalFilterException ife){
                LOGGER.warning("Unable to build filter: " + ife);
                return null;
            }
        } else if (logical.containsKey(childName)){
            LOGGER.finer("a logical filter "+childName);
            try{
                short type = ((Integer)logical.get(childName)).shortValue();
                LOGGER.finer("logic type "+type);
                LogicFilter filter = filterFactory.createLogicFilter(type);
                NodeList map = child.getChildNodes();
                for(int i=0;i<map.getLength();i++){
                    Node kid=map.item(i);
                    if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                    LOGGER.finer("adding to logic filter "+kid.getNodeName());
                    filter.addFilter(parseFilter(kid));
                }
                return filter;
             }catch (IllegalFilterException ife){
                LOGGER.warning("Unable to build filter: " + ife);
                return null;
            }
        }
        LOGGER.warning("unknown filter "+root);
        return null;
    }
    
    private static java.util.HashMap comparisions = new java.util.HashMap();
    private static java.util.HashMap spatial = new java.util.HashMap();
    private static java.util.HashMap logical = new java.util.HashMap();
    
    static{
        comparisions.put("PropertyIsEqualTo",new Integer(AbstractFilter.COMPARE_EQUALS));
        comparisions.put("PropertyIsGreaterThan",new Integer(AbstractFilter.COMPARE_GREATER_THAN));
        comparisions.put("PropertyIsGreaterThanOrEqualTo",new Integer(AbstractFilter.COMPARE_GREATER_THAN_EQUAL));
        comparisions.put("PropertyIsLessThan",new Integer(AbstractFilter.COMPARE_LESS_THAN));
        comparisions.put("PropertyIsLessThanOrEqualTo",new Integer(AbstractFilter.COMPARE_LESS_THAN_EQUAL));
        comparisions.put("PropertyIsLike",new Integer(AbstractFilter.LIKE));
        comparisions.put("PropertyIsNull",new Integer(AbstractFilter.NULL));
        comparisions.put("PropertyIsBetween",new Integer(AbstractFilter.BETWEEN));
        
        spatial.put("Equals",new Integer(AbstractFilter.GEOMETRY_EQUALS));
        spatial.put("Disjoint",new Integer(AbstractFilter.GEOMETRY_DISJOINT));
        spatial.put("Intersects",new Integer(AbstractFilter.GEOMETRY_INTERSECTS));
        spatial.put("Touches",new Integer(AbstractFilter.GEOMETRY_TOUCHES));
        spatial.put("Crosses",new Integer(AbstractFilter.GEOMETRY_CROSSES));
        spatial.put("Within",new Integer(AbstractFilter.GEOMETRY_WITHIN));
        spatial.put("Contains",new Integer(AbstractFilter.GEOMETRY_CONTAINS));
        spatial.put("Overlaps",new Integer(AbstractFilter.GEOMETRY_OVERLAPS));
        spatial.put("Beyond",new Integer(AbstractFilter.GEOMETRY_BEYOND));
        spatial.put("BBOX",new Integer(AbstractFilter.GEOMETRY_BBOX));
        
        logical.put("And",new Integer(AbstractFilter.LOGIC_AND));
        logical.put("Or",new Integer(AbstractFilter.LOGIC_OR));
        logical.put("Not",new Integer(AbstractFilter.LOGIC_NOT));
        
    }
}
