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
