/*
 * FilterXMLParser.java
 *
 * Created on 10 July 2002, 17:14
 */

package org.geotools.filter;

/**
 * A dom based parser to build filters as per OGC 01-067
 *
 * @author  iant
 */
import org.w3c.dom.*;

public class FilterXMLParser {
    private static org.apache.log4j.Logger _log =
    org.apache.log4j.Logger.getLogger(FilterXMLParser.class);
    
    /** Creates a new instance of FilterXMLParser */
    public FilterXMLParser() {
    }
    public static Filter parseFilter(Node root){
        _log.info("parsingFilter "+root.getNodeName());
        
        //NodeList children = root.getChildNodes();
        //_log.debug("children "+children);
        
        if(root == null || root.getNodeType() != Node.ELEMENT_NODE){
            _log.debug("bad node input ");
            return null;
        }
        _log.debug("processing root "+root.getNodeName());
        Node child = root;
        String childName = child.getNodeName();
        _log.debug("looking up "+childName);
        if(comparisions.containsKey(childName)){
            _log.debug("a comparision filter "+childName);
            boolean like =false, between = false;
            try{
                short type = ((Integer)comparisions.get(childName)).shortValue();
                CompareFilter filter = null;
                if(type == AbstractFilter.BETWEEN) {
                    BetweenFilter bfilter = new BetweenFilter();
                    
                    NodeList kids = child.getChildNodes();
                    for(int i=0;i<kids.getLength();i++){
                        Node kid = kids.item(i);
                        Node value;
                        if(kid.getNodeName().equalsIgnoreCase("LowerBoundary")){
                            value = kid.getFirstChild();
                            while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                            bfilter.addLeftValue(ExpressionXmlParser.parseExpression(value));
                        }
                        if(kid.getNodeName().equalsIgnoreCase("UpperBoundary")){
                            value = kid.getFirstChild();
                            while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                            bfilter.addRightValue(ExpressionXmlParser.parseExpression(value));
                        }
                        if(kid.getNodeName().equalsIgnoreCase("PropertyName")){
                            bfilter.addMiddleValue(ExpressionXmlParser.parseExpression(kid));
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
                            value = ExpressionXmlParser.parseExpression(kid);
                        }
                        if(res.equalsIgnoreCase("Literal")){
                            pattern = ExpressionXmlParser.parseExpression(kid).toString();
                        }
                    }
                    NamedNodeMap kids = child.getAttributes();
                    for(int i=0;i<kids.getLength();i++){
                        Node kid = kids.item(i);
                        if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                        String res = kid.getNodeName();
                        if(res.equalsIgnoreCase("wildCard")){
                            wildcard = kid.getNodeValue();
                        }
                        if(res.equalsIgnoreCase("singleChar")){
                            single = kid.getNodeValue();
                        }
                        if(res.equalsIgnoreCase("escape")){
                            escape = kid.getNodeValue();
                        }
                    }
                    if(!(wildcard==null||single==null||escape==null||pattern==null)){
                        LikeFilter lfilter = new LikeFilter();
                        lfilter.setValue(value);
                        lfilter.setPattern(pattern,wildcard,single,escape);
                        return lfilter;
                    }
                    _log.error("Problem building like filter\n"+pattern+" "+wildcard+" "+single+" "+escape);
                    return null;
                }else{                    
                    filter = new CompareFilter(type);
                }

                // find and parse left and right values 

                Node value = child.getFirstChild();
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                _log.debug("add left value -> "+value+"<-");
                filter.addLeftValue(ExpressionXmlParser.parseExpression(value));
                value = value.getNextSibling();
                
                while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                _log.debug("add right value -> "+value+"<-");
                filter.addRightValue(ExpressionXmlParser.parseExpression(value));
                return filter;
                
            }catch (IllegalFilterException ife){
                _log.error("Unable to build filter ",ife);
                return null;
            }
        } else if(spatial.containsKey(childName)){
            _log.debug("a spatial filter "+childName);
            try{
                short type = ((Integer)spatial.get(childName)).shortValue();
                GeometryFilter filter = new GeometryFilter(type);
                Node value = child.getFirstChild();
                    while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                    _log.debug("add left value -> "+value+"<-");
                    filter.addLeftGeometry(ExpressionXmlParser.parseExpression(value));
                    value = value.getNextSibling();
                    
                    while(value.getNodeType() != Node.ELEMENT_NODE ) value = value.getNextSibling();
                    _log.debug("add right value -> "+value+"<-");
        
                    filter.addRightGeometry(ExpressionXmlParser.parseExpression(value));
                    return filter;
                }catch (IllegalFilterException ife){
                _log.error("Unable to build filter ",ife);
                return null;
            }
        } else if (logical.containsKey(childName)){
            _log.debug("a logical filter "+childName);
            try{
                short type = ((Integer)logical.get(childName)).shortValue();
                _log.debug("logic type "+type);
                LogicFilter filter = new LogicFilter(type);
                NodeList map = child.getChildNodes();
                for(int i=0;i<map.getLength();i++){
                    Node kid=map.item(i);
                    if(kid == null || kid.getNodeType() != Node.ELEMENT_NODE) continue;
                    _log.debug("adding to logic filter "+kid.getNodeName());
                    filter.addFilter(parseFilter(kid));
                }
                return filter;
             }catch (IllegalFilterException ife){
                _log.error("Unable to build filter ",ife);
                return null;
            }
        }
        _log.info("unknown filter "+root);
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
