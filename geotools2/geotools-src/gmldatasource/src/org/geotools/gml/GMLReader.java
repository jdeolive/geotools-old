/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:49
 */

package org.geotools.gml;
import org.geotools.datasource.*;
import org.geotools.gml.handlers.*;
import java.io.*;
import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.vividsolutions.jts.geom.*;

/** Reads and parses a GML file into a geometry collection
 *
 * @author ian
 * @version $Id: GMLReader.java,v 1.3 2002/03/08 18:06:03 ianturton Exp $
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
    public GMLReader(Reader in) throws DataSourceException{
        if(in==null){
            throw new DataSourceException("Null input stream in GMLReader");
        }
        this.in= new InputSource(in);
        try{
            parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser.setContentHandler(this);
        }catch(SAXException e){
            throw new DataSourceException("GMLReader setup error:"+e);
        }
        
        
    }
    /** Reads the inputstream
     * @throws DataSourceException
     * @return a geometry collection of the geometries read in
     */
    public GeometryCollection read()throws DataSourceException{
        try{
            parser.parse(in);
        }catch(Exception e){
            e.printStackTrace();
            throw new DataSourceException("GMLReader reading error:"+e);
        }
        return (GeometryCollection) head.finish(factory);
    }
    
    /** allows for the read process to be stopped
     */
    public void stopLoading(){
        stopped=true;
    }
    /** called by parser when a new entity is found
     */
    public void startElement(String namespace, String localName, String qName, Attributes atts)
    throws SAXException{
        
        /* at the start of each entity we need to find a handler for
         * the entitiy and then push that on to the stack. For convience we
         * also hold the reference in handler.
         */
        
        
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
                handlers.push(handler);
                System.out.println("start of "+qName+"\nstack:"+handlers);
                
            }
        }catch(GMLException e){
            throw new SAXException(e);
        }
        
    }
    /** called by parser when a charater string is found
     * at present we handle exactly two cases here either its a coordinate or an X,Y or Z inside a coord
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
            System.out.println("XYZ s="+s+"*");
            ((GMLXYZHandler)handler).parseText(s);
        }
    }
    /** called by parser when an entitiy finishes
     * first we "capture" the info insider the handler for the entitiy we've
     * just finished, e.g. geometry, coordinates, etc.
     * then we need to remove the current handler from the top of the stack
     * we can then add the info retieved to the new head of the stack.
     */
    public void endElement(String namespace,String localName, String qName) throws SAXException{
        
        GMLHandler h ;
        if(handler!=null){
            
            
                System.out.println("****End of entity "+localName);
                System.out.println("current stack:"+handlers);
            
            Coordinate[] coords = null;
            Geometry g = null;
            
            if(handler instanceof GMLCoordinatesHandler){
                coords = ((GMLCoordinatesHandler)handler).getCoordinates();
                handler = (GMLHandler)handlers.pop();
                if(coords!=null){
                    for(int i=0;i<coords.length;i++){
                        ((GMLHandler)handlers.peek()).addCoordinate(coords[i]);
                    }
                }
                System.out.println("return stack:"+handlers);
                return;
                
            }else if(handler instanceof GMLCoordHandler){
                System.out.println("finishing CoordHandler");
                Coordinate coord=((GMLCoordHandler)handler).getCoordinate();
                handler = (GMLHandler)handlers.pop();
                ((GMLHandler)handlers.peek()).addCoordinate(coord);
                System.out.println("return stack:"+handlers);
                return;
            }else if(handler instanceof GMLXHandler){
                System.out.println("about to get X");
                double v = ((GMLXHandler)handler).getX();
                System.out.println("got X "+v);
                handler=(GMLHandler)handlers.pop();
                System.out.println("post pop "+handler);
                ((GMLCoordHandler)handlers.peek()).setX(v);
                System.out.println("set X in coord");
                System.out.println("return stack:"+handlers);
                return;
            }else if(handler instanceof GMLYHandler){
                double v = ((GMLYHandler)handler).getY();
                handler=(GMLHandler)handlers.pop();
                ((GMLCoordHandler)handlers.peek()).setY(v);
                System.out.println("return stack:"+handlers);
                return;
            }else if(handler instanceof GMLZHandler){
                double v = ((GMLZHandler)handler).getZ();
                handler=(GMLHandler)handlers.pop();
                System.out.println("post pop "+handler);
                ((GMLCoordHandler)handlers.peek()).setZ(v);
                System.out.println("return stack:"+handlers);
                return;
            }else{
                // its a geometry type
                g = handler.finish(factory);
                handler=(GMLHandler)handlers.pop();             
                if(handlers.size()>0){
                    ((GMLHandler)handlers.peek()).addGeometry(g);
                } // when the stack is empty we'return finished and ready to return head.
                System.out.println("return stack:"+handlers);
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
    private String[] primativeGeoms = {"Point","Box","LineString","LinearRing","Polygon"};
}