/**
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

package org.geotools.wms.gtserver;

import java.util.*;
import java.io.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import org.geotools.data.DataSource;



/** Reads the layers defined in the layers.xml file and exposes them for use in an app.
 * Call read() to read all the layers.
 */
public class LayerReader extends DefaultHandler {
    XMLReader reader;
    // Use the xerces parser
    //public static String parserName = "org.apache.xerces.parsers.SAXParser";
    
    // Variables for the current parsing operation (parser is assumed to be synchronized)
    public HashMap layers;
    public LayerEntry currentLayer;
    public String currentTag = "";
    
    public static final String TAG_LAYER = "/WMSServer/layer";
    public static final String TAG_DESCRIPTION = "/WMSServer/layer/description";
    public static final String TAG_DATASOURCE = "/WMSServer/layer/datasource";
    public static final String TAG_PARAM = "/WMSServer/layer/datasource/param";
    public static final String TAG_STYLE = "/WMSServer/layer/style";
    
    
    public static final String ATTRIB_ID = "id";
    public static final String ATTRIB_SRS = "srs";
    public static final String ATTRIB_CLASS = "class";
    public static final String ATTRIB_NAME = "name";
    public static final String ATTRIB_VALUE = "value";
    public static final String ATTRIB_FILENAME = "filename";
    public static final String ATTRIB_DEFAULTSTYLE = "default";
    
    
    public LayerReader() throws Exception {
        //try {
            // Create the reader
            //reader = XMLReaderFactory.createXMLReader();
            SAXParserFactory fac = SAXParserFactory.newInstance();
            SAXParser parser = fac.newSAXParser();
           
            reader = new ParserAdapter(parser.getParser());
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
        //}
        //catch(Exception exp) {
           // System.out.println("Exception "+exp.getClass().getName()+" initializing LayerReader : "+exp.getMessage());
            //			exp.printStackTrace();
        //}
    }
    
    /** Reads the layers.xml file stream
     */
    public HashMap read(InputStream is) {
        // Read the xml stream
        try {
            reader.parse(new InputSource(is));
        }
        catch(IOException ioexp) {
            System.out.println("IOException reading layers : "+ioexp.getMessage());
            ioexp.printStackTrace();
        }
        catch(SAXException saxexp) {
            System.out.println("SAXException reading layers : "+saxexp.getMessage());
            saxexp.printStackTrace();
        }
        
        // Return the result of the parse
        if (layers!=null)
            return layers;
        else
            return null;
    }
    
    // SAX methods
    
    /** Start document. */
    public void startDocument() {
        System.out.println("LayerReader : Started parsing document");
        layers = new HashMap();
        currentLayer = null;
    }
    
    
    /** Start element. */
    public void startElement(String uri, String localName, String qName, Attributes attrs) {
        currentTag = currentTag+"/"+localName;
        System.out.print(currentTag+" ");
        // <layer> tag
        if (currentTag.equalsIgnoreCase(TAG_LAYER)) {
            currentLayer = new LayerEntry();
            currentLayer.id = attrs.getValue(ATTRIB_ID);
            System.out.println(""+attrs.getValue(ATTRIB_ID));
            String temp = attrs.getValue(ATTRIB_SRS);
            if(temp != null) {
                currentLayer.srs = attrs.getValue(ATTRIB_SRS);
            }
            return ;
        }
        // <datasource> tag
        if (currentTag.equalsIgnoreCase(TAG_DATASOURCE)) {
            currentLayer.datasource = attrs.getValue(ATTRIB_CLASS);
            System.out.println(""+attrs.getValue(ATTRIB_CLASS));
            currentLayer.properties = new Properties();
            return ;
        }
        // <param> tag
        if (currentTag.equalsIgnoreCase(TAG_PARAM)){
            currentLayer.properties.setProperty(attrs.getValue(ATTRIB_NAME), attrs.getValue(ATTRIB_VALUE));
            System.out.println(""+attrs.getValue(ATTRIB_NAME)+ " "+attrs.getValue(ATTRIB_VALUE));
            return ;
        }
        if (currentTag.equalsIgnoreCase(TAG_STYLE)) {
            if(currentLayer.styles == null){
                currentLayer.styles = new HashMap();
            }
            currentLayer.styles.put(attrs.getValue(ATTRIB_ID), attrs.getValue(ATTRIB_FILENAME));
            System.out.println(""+attrs.getValue(ATTRIB_ID));
            String style = attrs.getValue(ATTRIB_DEFAULTSTYLE);
            //System.out.println("default style attrib is " + style);
            if(style != null && style.equalsIgnoreCase("true")){
                currentLayer.defaultStyle = attrs.getValue(ATTRIB_ID);
            }
            return ;
        }
        System.out.println(" ");
        return ;
    }
    
    /** Characters. */
    public void characters(char ch[], int start, int length) {
        if (currentTag.equalsIgnoreCase(TAG_DESCRIPTION))
            currentLayer.description = new String(ch, start, length);
    }
    
    
    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length) {
    }
    
    
    /** End element. */
    public void endElement(String uri, String localName, String qName) {
        // Close <layer> tag, add to list
        if (currentTag.equalsIgnoreCase(TAG_LAYER)) {
            layers.put(currentLayer.id,currentLayer);
            currentLayer = null;
        }
        
        
        currentTag = currentTag.substring(0, currentTag.length()-localName.length()-1);
    }
    
    
    /** End document. */
    public void endDocument() {
    }
    
    // ErrorHandler methods
    
    /** Warning. */
    public void warning(SAXParseException ex) {
        System.err.println("[Warning] "+
        ex.getMessage());
    }
    
    
    /** Error. */
    public void error(SAXParseException ex) {
        System.err.println("[Error] "+
        ex.getMessage());
    }
    
    
    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        System.err.println("[Fatal Error] "+
        ex.getMessage());
        throw ex;
    }
}


