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
        if(comparisions.containsKey(childName)){
            _log.debug("a comparision filter");
            try{
                short type = ((Integer)comparisions.get(childName)).shortValue();
                CompareFilter filter = null;
                if(type != AbstractFilter.BETWEEN) {
                    filter = new CompareFilter(type);
                }else{
                    filter = new BetweenFilter();
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
                _log.error("Unable to build expression ",ife);
                return null;
            }
        }
        return null;
    }
    
    private static java.util.HashMap comparisions = new java.util.HashMap();
    private static java.util.HashMap spatial = new java.util.HashMap();
    private static java.util.HashMap logic = new java.util.HashMap();
    
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
        
        logic.put("And",new Integer(AbstractFilter.LOGIC_AND));
        logic.put("Or",new Integer(AbstractFilter.LOGIC_OR));
        logic.put("Not",new Integer(AbstractFilter.LOGIC_NOT));
        
    }
}
