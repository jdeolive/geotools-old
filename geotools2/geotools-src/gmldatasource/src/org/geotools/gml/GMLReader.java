/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:49
 */

package org.geotools.gml;

import org.geotools.gml.handlers.*;
import java.io.*;
import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.vividsolutions.jts.geom.*;

/** Reads and parses a GML file into a geometry collection
 *
 * @author ian
 * @version $Id: GMLReader.java,v 1.7 2002/03/15 07:55:36 ianturton Exp $
 */
public class GMLReader extends org.xml.sax.helpers.DefaultHandler {
    boolean stopped = false;
    XMLReader parser;
    InputSource in;
    Stack handlers = new Stack();
    int errCount=0;
    GeometryFactory factory = new GeometryFactory();
    GMLHandler handler,head;
    
    
    /** Creates a new instance of GMLReader
     * @param in inputStream to be read
     * @throws DataSourceException if an IO Exception occurs
     */
    public GMLReader(Reader in) throws GMLException{
        if(in==null){
            throw new GMLException("Null input stream in GMLReader");
        }
        this.in= new InputSource(in);
        try{
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser.setContentHandler(this);
            
        }catch(SAXException e){
            throw new GMLException("GMLReader setup error:"+e);
        }
        
        
    }
    /** Reads the inputstream
     * @throws DataSourceException if anything goes wrong
     * @return a geometry collection of the geometries read in
     */
    public GeometryCollection read()throws GMLException{
        try{
            parser.parse(in);
        }catch(Exception e){
            e.printStackTrace();
            throw new GMLException("GMLReader reading error:"+e);
        }
        return (GeometryCollection) head.finish(factory);
    }
    
    /** allows for the read process to be stopped
     */
    public void stopLoading(){
        stopped=true;
        // we should actually stop here!
    }
    /** Called by parser when a new entity is found.
     * @param namespace the namespace of the element
     * @param localName local name of the element
     * @param qName Qualified name of the element
     * @param atts attributes of the element
     * @throws SAXException if anything goes wrong
     */
    public void startElement(String namespace, String localName, String qName, Attributes atts)
    throws SAXException{
        
        /* at the start of each entity we need to find a handler for
         * the entitiy and then push that on to the stack. For convience we
         * also hold the reference in handler.
         */
        
        if(handler!=null) handlers.push(handler);
        try{
            
            handler = getGMLHandler(qName);
            if(handler==null){
                System.err.println("Unknown Geometry type: "+qName);
                errCount++;
                if(errCount>0){
                    throw new SAXException("Unknown Geometry type: "+qName);
                }
            }else{
                if(head==null) head=handler;
            }
        }catch(GMLException e){
            throw new SAXException(e);
        }
        
    }
    /** Called by parser when a charater string is found.
     * at present we handle exactly two cases here either its a coordinate or an X,Y or Z inside a coord
     * @param ch the character array which contains the string
     * @param start where the string starts in ch
     * @param length how long the string is
     *
     * @throws SAXException when something goes wrong
     */
    public void characters(char[] ch,int start, int length) throws SAXException{
        String s = new String(ch,start,length).trim();
        
        if(handler instanceof GMLCoordinatesHandler){
            StringTokenizer outer = new StringTokenizer(s," ");
            while(outer.hasMoreElements()){
                // parse to x and y add to handler
                String pair = outer.nextToken();
                ((GMLCoordinatesHandler)handler).parseText(pair);
            }
        }else if(handler instanceof GMLXYZHandler){
            ((GMLXYZHandler)handler).parseText(s);
        }
    }
    /** Called by parser when an entitiy finishes.
     * first we "capture" the info insider the handler for the entitiy we've
     * just finished, e.g. geometry, coordinates, etc.
     * then we need to remove the current handler from the top of the stack
     * we can then add the info retieved to the new head of the stack.
     * @param namespace the namespace of the element
     * @param localName local name of the element
     * @param qName Qualified name of the element
     * @throws SAXException when something goes wrong
     */
    public void endElement(String namespace,String localName, String qName) throws SAXException{
        
        GMLHandler h ;
        if(handler==head) return;
        if(handler!=null){
            Coordinate[] coords = null;
            Geometry g = null;
            
            if(handler instanceof GMLCoordinatesHandler){
                coords = ((GMLCoordinatesHandler)handler).getCoordinates();
                handler = (GMLHandler)handlers.pop();
                if(coords!=null){
                    for(int i=0;i<coords.length;i++){
                        handler.addCoordinate(coords[i]);
                    }
                }
            
                return;
                
            }else if(handler instanceof GMLCoordHandler){
            
                Coordinate coord=((GMLCoordHandler)handler).getCoordinate();
                handler = (GMLHandler)handlers.pop();
                handler.addCoordinate(coord);
            
                return;
            }else if(handler instanceof GMLXHandler){
            
                double v = ((GMLXHandler)handler).getX();
            
                handler=(GMLHandler)handlers.pop();
            
                ((GMLCoordHandler)handler).setX(v);
            
            
                return;
            }else if(handler instanceof GMLYHandler){
                double v = ((GMLYHandler)handler).getY();
                handler=(GMLHandler)handlers.pop();
                ((GMLCoordHandler)handler).setY(v);
            
                return;
            }else if(handler instanceof GMLZHandler){
                double v = ((GMLZHandler)handler).getZ();
                handler=(GMLHandler)handlers.pop();
            
                ((GMLCoordHandler)handler).setZ(v);
            
                return;
            }else{
                // its a geometry type
                g = handler.finish(factory);
                handler=(GMLHandler)handlers.pop();
                
                handler.addGeometry(g);
               
            
                return;
            }
        }else{
            System.out.println("whoops handler == null in end element");
        }
    }
    private GMLHandler getGMLHandler(String name) throws GMLException{
        int index = name.indexOf(':');
        String primativeName;
        if(index>0){
            primativeName=name.substring(index+1);
        }else{
            primativeName=name;
        }
        
        String handleName="org.geotools.gml.handlers.GML"+Character.toUpperCase(primativeName.charAt(0))+primativeName.substring(1)+"Handler"; // convert to Java naming conventions
        
        GMLHandler h;
        try{
            h = (GMLHandler)Class.forName(handleName).newInstance();
        }catch(ClassNotFoundException e){
            System.out.println(""+e);
            return null; // when finished could throw error here?
        }
        catch(Exception ex){
            throw new GMLException("Error creating instance of "+handleName+"\n"+ex);
        }
        return h;
    }
    
    /** Getter for property factory.
     * @return Value of property factory.
     */
    public com.vividsolutions.jts.geom.GeometryFactory getFactory() {
        return factory;
    }
    
    /** Setter for property factory.
     * @param factory New value of property factory.
     */
    public void setFactory(com.vividsolutions.jts.geom.GeometryFactory factory) {
        this.factory = factory;
    }
    
    private String[] primativeGeoms = {"Point","Box","LineString","LinearRing","Polygon"};
}