/*
 * GMLReader.java
 *
 * Created on 04 March 2002, 12:49
 */

package org.geotools.gml;
import org.geotools.datasource.*;
import java.io.*;
import java.util.*;
import org.xml.sax.helpers.*;
import org.xml.sax.*;
import com.vividsolutions.jts.geom.*;

/** Reads and parses a GML file into a geometry collection
 *
 * @author ian
 * @version $Id: GMLReader.java,v 1.2 2002/03/06 17:55:13 ianturton Exp $
 */
public class GMLReader extends org.xml.sax.helpers.DefaultHandler {
    boolean stopped = false;
    XMLReader parser;
    InputSource in;
    Stack handlers = new Stack();
    int errCount=0;
    GeometryFactory factory = new GeometryFactory();
    GMLHandler handler,head;
    
    boolean coord = false;
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
            System.err.println(""+errCount+" errors encountered in getFeatures");
        }catch(Exception e){
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
        System.out.println("Got Start of entity: "+qName);
        for(int i=0;i<atts.getLength();i++){
            System.out.println("attr:"+i+":"+atts.getQName(i));
        }
        
        if(localName.toLowerCase().startsWith("coord")){ 
            coord=true;
            return;
        }
        // we need to provide handlers for each of the primative geometries
        // if it is not primative then we are inside a primative and probably in the process of
        // building it.
        
            try{
                handler = getGMLHandler(qName);
                if(handler==null){
                    System.err.println("Unknown Geometry type: "+qName);
                    errCount++;
                    if(errCount>20){
                        throw new SAXException("Too many errors reading GML");
                    }
                }else{
                    if(head==null) head=handler;
                    handlers.push(handler);
                }
            }catch(GMLException e){
                throw new SAXException(e);
            }
        
    }
    /** called by parser when a charater string is found
     */    
    public void characters(char[] ch,int start, int length) throws SAXException{
        String s = new String(ch,start,length).trim();
        System.out.println("->"+new String(ch,start,length).trim());
        if(coord){
            // parse to x and y add to handler
            StringTokenizer st = new StringTokenizer(s,",");
            if(st.countTokens()>2){
                System.out.println("problem parsing coordinate "+s);
            }else{
                double x = Double.parseDouble(st.nextToken());
                double y = Double.parseDouble(st.nextToken());
                // the coordinate allways belongs to the current handler
                handler.addCoordinate(new Coordinate(x,y));
                coord=false;
            }
        }
    }
    /** called by parser when an entitiy finishes
     */    
    public void endElement(String namespace,String localName, String qName) throws SAXException{
        System.out.println("end: "+qName);
        if(handler!=null){
            Geometry g = handler.finish(factory);
            if(!(handler==head)){
                handler=(GMLHandler)handlers.pop();
                handler.addGeometry(g);
                return;
            }else{
              System.out.println("finished top Feature Collection?");  
            }
        }else{
            System.out.println("whoops handler == null in end element");
        }
    }
    private GMLHandler getGMLHandler(String name) throws GMLException{
        System.out.println("getgmlhandler looking for "+name);
        int index = name.indexOf(':');
        String primativeName;
        if(index>0){
            primativeName=name.substring(index+1);
        }else{
            primativeName=name;
        }
        System.out.println("?shortened to "+primativeName);
        String handleName="org.geotools.gml.handlers.GML"+Character.toUpperCase(primativeName.charAt(0))+primativeName.substring(1)+"Handler"; // convert to Java naming conventions
        System.out.println("now looking for "+handleName);
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