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

package org.geotools.wms;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import javax.servlet.*;
import javax.servlet.http.*;

import sun.awt.image.codec.JPEGImageEncoderImpl;

import org.geotools.feature.Feature;

/** A servlet implementation of the WMS spec. This servlet delegates the required call to an implementor of WMSServer.
 * Much of the front-end logic, such as Exception throwing, and Feature Formatting, is handled here, leaving the implementation of WMSServer abstracted away from the WMS details as much as possible.
 * The exception to this rule is the getCapabilites call, which returns the capabilities XML directly from the WMSServer. This may be changed later.
 */
public class WMSServlet extends HttpServlet {
    ServletContext context = null;
    
    // Basic service elements parameters
    public static final String PARAM_VERSION		= "VERSION";
    public static final String PARAM_SERVICE		= "SERVICE";
    public static final String PARAM_REQUEST		= "REQUEST";
    
    // GetCapabilites parameters
    public static final String PARAM_UPDATESEQUENCE	= "UPDATESEQUENCE";
    
    // GetMap parameters
    public static final String PARAM_LAYERS			= "LAYERS";
    public static final String PARAM_STYLES			= "STYLES";
    public static final String PARAM_SRS			= "SRS";
    public static final String PARAM_BBOX			= "BBOX";
    public static final String PARAM_WIDTH			= "WIDTH";
    public static final String PARAM_HEIGHT			= "HEIGHT";
    public static final String PARAM_FORMAT			= "FORMAT";
    public static final String PARAM_TRANSPARENT	= "TRANSPARENT";
    public static final String PARAM_BGCOLOR		= "BGCOLOR";
    public static final String PARAM_EXCEPTIONS		= "EXCEPTIONS";
    public static final String PARAM_TIME			= "TIME";
    public static final String PARAM_ELEVATION		= "ELEVATION";
    
    // GetFeatureInfo parameters
    public static final String PARAM_QUERY_LAYERS	= "QUERY_LAYERS";
    public static final String PARAM_INFO_FORMAT	= "INFO_FORMAT";
    public static final String PARAM_FEATURE_COUNT	= "FEATURE_COUNT";
    public static final String PARAM_X				= "X";
    public static final String PARAM_Y				= "Y";
    
    // Default values
    public static final String DEFAULT_FORMAT		= "image/jpeg";
    public static final String DEFAULT_FEATURE_FORMAT		= "text/xml";
    public static final String DEFAULT_COLOR		= "#FFFFFF";
    public static final String DEFAULT_EXCEPTION	= "test/xml";
    
    // Capabilities XML tags
    public static final String XML_MAPFORMATS		= "<?GEO MAPFORMATS ?>";
    public static final String XML_GETFEATUREINFO	= "<?GEO GETFEATUREINFO ?>";
    public static final String XML_EXCEPTIONFORMATS	= "<?GEO EXCEPTIONFORMATS ?>";
    public static final String XML_VENDORSPECIFIC	= "<?GEO VENDORSPECIFICCAPABILITIES ?>";
    public static final String XML_LAYERS		= "<?GEO LAYERS ?>";
    public static final String XML_GETURL               = "<?GEO GETURL ?>";
    
    private WMSServer server;
    private Vector featureFormatters;
    private String getUrl;
    
    
    /**
     * Override init() to set up data used by invocations of this servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // save servlet context
        context = config.getServletContext();
        
        // The installed featureFormatters - none is this version
        featureFormatters = new Vector();
        
        // Get the WMSServer class to be used by this servlet
        try {
            server = (WMSServer)Class.forName(config.getInitParameter("WMSServerClass")).newInstance();
            // Build properties to send to the server implementation
            Properties prop = new Properties();
            Enumeration en = config.getInitParameterNames();
            while (en.hasMoreElements()) {
                String key = (String)en.nextElement();
                prop.setProperty(key, config.getInitParameter(key));
            }
            //pass in the context as well
            String real = getServletContext().getRealPath("");
            System.out.println("setting base.url to " + real);
            prop.setProperty("base.url",real);
            server.init(prop);
        }
        catch(Exception exp) {
            throw new ServletException("Cannot instantiate WMSServer class specified in WEB.XML", exp);
        }
    }
    
    /**
     * Basic servlet method, answers requests fromt the browser.
     * @param request HTTPServletRequest
     * @param response HTTPServletResponse
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("DoGet called from "+request.getRemoteAddr());
        // Nullify caching
        
        //What's my address?
        getUrl = HttpUtils.getRequestURL(request).append("?").toString();
        // Check request type
        String sRequest = getParameter(request, PARAM_REQUEST);
        if (sRequest==null || sRequest.trim().length()==0) {
            doException("InvalidRequest", "Invalid REQUEST parameter sent to servlet", request, response);
        }
        else if (sRequest.trim().equalsIgnoreCase("GetCapabilities") || sRequest.trim().equalsIgnoreCase("capabilities")) {
            doGetCapabilities(request, response);
        }
        else if (sRequest.trim().equalsIgnoreCase("GetMap")) {
            doGetMap(request, response);
        }
        else if (sRequest.trim().equalsIgnoreCase("GetFeatureInfo")) {
            doGetFeatureInfo(request, response);
        }
    }
    
    /** Gets the given parameter value, in a non case-sensitive way
     * @param param The parameter to get the value for
     * @param request The HttpServletRequest object to search for the parameter value
     */
    private String getParameter(HttpServletRequest request, String param) {
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String key = (String)en.nextElement();
            if (key.trim().equalsIgnoreCase(param.trim()))
                return request.getParameter(key);
        }
        return null;
    }
    
    /** Returns WMS 1.1.1 compatible response for a getCapabilities request
     */
    public void doGetCapabilities(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Sending capabilities");
        System.out.println("My path is " + request.getServletPath() +"?");
        try {
            // Get Capabilities object from server implementation
            Capabilities capabilities = server.getCapabilities();
            
            // Convert the object to XML
            String xml = capabilitiesToXML(capabilities);
            
            // Send to client
            response.setContentType("text/xml");
            
            PrintWriter pr = response.getWriter();
            pr.print(xml);
            pr.close();
        }
        catch(WMSException wmsexp) {
            doException(wmsexp.getCode(), wmsexp.getMessage(), request, response);
        }
        catch(Exception exp) {
            System.out.println("Unexpected exception "+exp);
            exp.printStackTrace();
            doException(null, "Unknown exception : "+exp.getMessage(), request, response);
        }
    }
    
    
    /** Returns WMS 1.1.1 compatible response for a getMap request
     */
    public void doGetMap(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Sending Map");
        
        // Get the requested exception mime-type (if any)
        String exceptions = getParameter(request, PARAM_EXCEPTIONS);
        
        try {
            // Get all the parameters for the call
            String [] layers = commaSeparated(getParameter(request, PARAM_LAYERS));
            String [] styles = commaSeparated(getParameter(request, PARAM_STYLES), layers.length);
            String srs = getParameter(request, PARAM_SRS);
            String bbox = getParameter(request, PARAM_BBOX);
            int width = posIntParam(getParameter(request, PARAM_WIDTH));
            int height = posIntParam(getParameter(request, PARAM_HEIGHT));
            boolean trans = boolParam(getParameter(request, PARAM_TRANSPARENT));
            Color bgcolor = colorParam(getParameter(request, PARAM_BGCOLOR));
            String format = getParameter(request, PARAM_FORMAT);
            
            System.out.println("Checking params");
            
            // Check values
            if (layers==null || layers.length==0) {
                doException(null, "No Layers defined", request, response, exceptions);
                return;
            }
            if (styles!=null && styles.length!=layers.length) {
                doException(null, "Invalid number of style defined for passed layers", request, response, exceptions);
                return;
            }
            if (srs==null) {
                doException(WMSException.WMSCODE_INVALIDSRS, "SRS not defined", request, response, exceptions);
                return;
            }
            if (bbox==null) {
                doException(null, "BBOX not defined", request, response, exceptions);
                return;
            }
            if (height==-1 || width==-1) {
                doException(null, "HEIGHT or WIDTH not defined", request, response, exceptions);
                return;
            }
            
            if (format==null)
                format = DEFAULT_FORMAT;
            
            // Check the bbox
            String [] sBbox = commaSeparated(bbox, 4);
            if (sBbox==null) {
                doException(null, "Invalid bbox : "+bbox, request, response, exceptions);
            }
            double [] dBbox = new double[4];
            for (int i=0;i<4;i++)
                dBbox[i] = doubleParam(sBbox[i]);
            
            System.out.println("Params check out - getting image");
            
            // Get the image
            BufferedImage image = server.getMap(layers, styles, srs, dBbox, width, height, trans, bgcolor);
            
            System.out.println("Got image - sending response as "+format+" ("+image.getWidth(null)+","+image.getHeight(null)+")");
            
            // Write the response
            response.setContentType(format);
            OutputStream out = response.getOutputStream();
            // avoid caching in browser
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires",0);
            
            formatImageOutputStream(format, image, out);
            out.close();
        }
        catch(WMSException wmsexp) {
            doException(wmsexp.getCode(), wmsexp.getMessage(), request, response, exceptions);
        }
        catch(Exception exp) {
            doException(null, "Unknown exception : "+exp.getMessage(), request, response, exceptions);
        }
        
    }
    
    /** Gets an outputstream of the given image, formatted to the given mime format
     * Currently only supports "image/jpeg"
     * @param format The mime-type of the format for the image (image/jpeg)
     * @param image The image to be formatted
     * @param outStream OutputStream of the formatted image
     */
    public void formatImageOutputStream(String format, BufferedImage image, OutputStream outStream) throws WMSException {
        if (!format.equalsIgnoreCase(DEFAULT_FORMAT))
            throw new WMSException(WMSException.WMSCODE_INVALIDFORMAT, "Invalid format : "+format);
        JPEGImageEncoderImpl j = new JPEGImageEncoderImpl(outStream);
        try {
            j.encode(image);
        }
        catch(IOException ioexp) {
            throw new WMSException(null, "IOException : "+ioexp.getMessage());
        }
    }
    
    /** Returns WMS 1.1.1 compatible response for a getFeatureInfo request
     * Currently, this returns an exception, as this servlet does not support getFeatureInfo
     */
    public void doGetFeatureInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the requested exception mime-type (if any)
        String exceptions = getParameter(request, PARAM_EXCEPTIONS);
        
        try {
            // Get parameters
            String [] layers = commaSeparated(getParameter(request, PARAM_QUERY_LAYERS));
            String srs = getParameter(request, PARAM_SRS);
            String bbox = getParameter(request, PARAM_BBOX);
            int width = posIntParam(getParameter(request, PARAM_WIDTH));
            int height = posIntParam(getParameter(request, PARAM_HEIGHT));
            String format = getParameter(request, PARAM_INFO_FORMAT);
            int featureCount = posIntParam(getParameter(request, PARAM_FEATURE_COUNT));
            int x = posIntParam(getParameter(request, PARAM_X));
            int y = posIntParam(getParameter(request, PARAM_Y));
            
            // Check values
            if (layers==null || layers.length==0) {
                doException(null, "No Layers defined", request, response, exceptions);
                return;
            }
            if (srs==null) {
                doException(WMSException.WMSCODE_INVALIDSRS, "SRS not defined", request, response, exceptions);
                return;
            }
            if (bbox==null) {
                doException(null, "BBOX not defined", request, response, exceptions);
                return;
            }
            if (height==-1 || width==-1) {
                doException(null, "HEIGHT or WIDTH not defined", request, response, exceptions);
                return;
            }
            // Check the bbox
            String [] sBbox = commaSeparated(bbox, 4);
            if (sBbox==null) {
                doException(null, "Invalid bbox : "+bbox, request, response, exceptions);
            }
            double [] dBbox = new double[4];
            for (int i=0;i<4;i++)
                dBbox[i] = doubleParam(sBbox[i]);
            // Check the feature count
            if (featureCount<1)
                featureCount = 1;
            if (x<0 || y<0) {
                doException(null, "Invalid X or Y parameter", request, response, exceptions);
                return;
            }
            // Check the feature format
            if (format==null || format.trim().length()==0)
                format = DEFAULT_FEATURE_FORMAT;
            
            // Get features
            Feature [] features = server.getFeatureInfo(layers, srs, dBbox, width, height, featureCount, x, y);
            
            // Get featureFormatter with the requested mime-type
            WMSFeatureFormatter formatter = getFeatureFormatter(format);
            
            // return Features
            response.setContentType(formatter.getMimeType());
            OutputStream out = response.getOutputStream();
            formatter.formatFeatures(features, out);
            try {
                out.close();
            } catch(IOException ioexp) {}
        }
        catch(WMSException wmsexp) {
            doException(wmsexp.getCode(), wmsexp.getMessage(), request, response, exceptions);
        }
        catch(Exception exp) {
            doException(null, "Unknown exception : "+exp.getMessage(), request, response, exceptions);
        }
    }
    
    private WMSFeatureFormatter getFeatureFormatter(String mime) throws WMSException {
        for (int i=0;i<featureFormatters.size();i++)
            if (((WMSFeatureFormatter)featureFormatters.elementAt(i)).getMimeType().equalsIgnoreCase(mime))
                return (WMSFeatureFormatter)featureFormatters.elementAt(i);
        throw new WMSException(WMSException.WMSCODE_INVALIDFORMAT, "Invalid Feature Format "+mime);
    }
    
    /** Returns WMS 1.1.1 compatible Exception
     * @param sCode The WMS 1.1.1 exception code @see WMSException
     * @param sException The detailed exception message
     * @param request The ServletRequest object for the current request
     * @param response The ServletResponse object for the current request
     */
    public void doException(String sCode, String sException, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doException(sCode, sException, request, response, DEFAULT_EXCEPTION);
    }
    
    /** Returns WMS 1.1.1 compatible Exception
     * @param sCode The WMS 1.1.1 exception code @see WMSException
     * @param sException The detailed exception message
     * @param request The ServletRequest object for the current request
     * @param response The ServletResponse object for the current request
     * @param exp_type The mime-type for the exception to be returned as
     */
    public void doException(String sCode, String sException, HttpServletRequest request, HttpServletResponse response, String exp_type) throws ServletException, IOException {
        // Send to client
        if (exp_type==null || exp_type.trim().length()==0)
            exp_type = DEFAULT_EXCEPTION;
        System.out.println("Its all gone wrong "+sException);
        // Check the optional response code (mime-type of exception)
        //      if (exp_type.equalsIgnoreCase("application/vnd.ogc.se_xml") || exp_type.equalsIgnoreCase("text/xml")) {
        response.setContentType(exp_type);
        PrintWriter pw = response.getWriter();
        // Write header
        pw.println("  <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
        pw.println("  <!DOCTYPE ServiceExceptionReport SYSTEM \"http://www.digitalearth.gov/wmt/xml/exception_1_1_0.dtd\"> ");
        pw.println("  <ServiceExceptionReport version=\"1.1.0\">");
        // Write exception code
        pw.println("    <ServiceException"+(sCode!=null?" code="+sCode:"")+">"+sException+"</ServiceException>");
        // Write footer
        pw.println("  </ServiceExceptionReport>");
        
        pw.close();
        //    }
     /*   if (exp_type.equalsIgnoreCase("text/plain")) {
            response.setContentType(exp_type);
            PrintWriter pw = response.getWriter();
            pw.println("Exception : Code="+sCode);
            pw.println(sException);
      
        }*/
        // Other exception types (graphcal, whatever) to go here
    }
    
    /** Converts this object into a WMS 1.1.1 compliant Capabilities XML string.
     */
    public String capabilitiesToXML(Capabilities cap) {
        InputStream is = this.getClass().getResourceAsStream("capabilities.xml");
        System.out.println("input stream " + is);
        StringBuffer xml = new StringBuffer();
        int length = 0;
        byte [] b = new byte [100];
        try {
            while ((length = is.read(b))!=-1)
                xml.append(new String(b, 0, length));
        }
        catch(IOException ioexp) {
            return null;
        }
        
        // address of this service
        String resource = "<OnlineResource xmlns:xlink=\"http://www.w3.org/1999/xlink\" "+
        "xlink:href=\"" + getUrl + "\"/>";
        
        xml.replace(xml.toString().indexOf(XML_GETURL), xml.toString().indexOf(XML_GETURL)+ XML_GETURL.length(), resource);
        
        
        // Map formats
        String mapFormats = "";
        // -we're not supporting more than one map format at this time
        xml.replace(xml.toString().indexOf(XML_MAPFORMATS), xml.toString().indexOf(XML_MAPFORMATS)+ XML_MAPFORMATS.length(), mapFormats);
        
        // GetFeatureInfo
        String getFeatureInfo = "";
        if (cap.getSupportsGetFeatureInfo()) {
            
        }
        xml.replace(xml.toString().indexOf(XML_GETFEATUREINFO), xml.toString().indexOf(XML_GETFEATUREINFO)+ XML_GETFEATUREINFO.length(), getFeatureInfo);
        
        // Exception formats
        String exceptionFormats = "";
        // -No more exception formats at this time
        xml.replace(xml.toString().indexOf(XML_EXCEPTIONFORMATS), xml.toString().indexOf(XML_EXCEPTIONFORMATS)+ XML_EXCEPTIONFORMATS.length(), exceptionFormats);
        
        // Vendor specific capabilities
        String vendorSpecific = "";
        if (cap.getVendorSpecificCapabilitiesXML()!=null)
            vendorSpecific = cap.getVendorSpecificCapabilitiesXML();
        xml.replace(xml.toString().indexOf(XML_VENDORSPECIFIC), xml.toString().indexOf(XML_VENDORSPECIFIC)+ XML_VENDORSPECIFIC.length(), vendorSpecific);
        
        // Layers
        String layerStr = "<Layer>\n<Name>Experimental Web Map Server</Name>\n";
        layerStr += "<Title>GeoTools2 web map server</Title>";
        layerStr += "<SRS>EPSG:4326</SRS>\n";
        layerStr += "<LatLonBoundingBox minx=\"-1\" miny=\"-1\" maxx=\"-1\" maxy=\"-1\" />\n";
        layerStr += "<BoundingBox SRS=\"EPSG:4326\" minx=\"-1\" miny=\"-1\" maxx=\"-1\" maxy=\"-1\" />\n";
        
        Enumeration en = cap.layers.elements();
        while (en.hasMoreElements()) {
            Capabilities.Layer l = (Capabilities.Layer)en.nextElement();
            // Layer properties
            layerStr += layersToXml(l, 1);
        }
        layerStr += "</Layer>";
        xml.replace(xml.toString().indexOf(XML_LAYERS), xml.toString().indexOf(XML_LAYERS)+ XML_LAYERS.length(), layerStr);
        
        return xml.toString();
    }
    
    /**
     * add layer to the capabilites xml
     * @task TODO: Support LegendUrl which requires additional format and
     *             size information.
     */
    private String layersToXml(Capabilities.Layer root, int tabIndex) {
        String tab = "\t";
        for (int t=0;t<tabIndex;t++) tab += "\t";
        String xml = tab+"<Layer>\n";
        
        // Tab in a little
        if (root.name!=null) xml += tab+"<Name>"+root.name+"</Name>\n";
        xml += tab+"<Title>"+root.title+"</Title>\n";
        xml += tab+"<Abstract></Abstract>\n";
        xml += tab+"<LatLongBoundingBox minx=\""+root.bbox[0]+"\" miny=\""+root.bbox[1]+"\" maxx=\""+root.bbox[2]+"\" maxy=\""+root.bbox[3]+"\" />\n";
        if (root.srs!=null) xml += tab+"<SRS>"+root.srs+"</SRS>\n";
        // Styles
        if (root.styles!=null) {
            Enumeration styles = root.styles.elements();
            while (styles.hasMoreElements()) {
                Capabilities.Style s = (Capabilities.Style)styles.nextElement();
                xml += tab+"<Style>\n";
                xml += tab+"<Name>"+s.name+"</Name>\n";
                xml += tab+"<Title>"+s.title+"</Title>\n";
                //xml += tab+"<LegendUrl>"+s.legendUrl+"</LegenUrl>\n";
                xml += tab+"</Style>\n";
            }
        }
        // Recurse child nodes
        for (int i=0;i<root.layers.size();i++) {
            Capabilities.Layer l = (Capabilities.Layer)root.layers.elementAt(i);
            xml += layersToXml(l, tabIndex+1);
        }
        
        xml += tab+"</Layer>\n";
        
        return xml;
    }
    
    /** Parse a given comma-separated parameter string and return the values it contains
     */
    private String [] commaSeparated(String paramStr) {
        if (paramStr==null)
            return new String[0];
        
        StringTokenizer st = new StringTokenizer(paramStr, ",");
        String [] params = new String[st.countTokens()];
        int index = 0;
        while (st.hasMoreTokens()) {
            params[index] = st.nextToken();
            index++;
        }
        
        return params;
    }
    
    /** Parse a given comma-separated parameter string and return the values it contains - must contain the number of values in numParams
     */
    private String [] commaSeparated(String paramStr, int numParams) {
        String [] params = commaSeparated(paramStr);
        if (params==null || params.length!=numParams)
            return null;
        
        return params;
    }
    
    /** Parses a positive integer parameter into an integer
     * @return The positive integer value of param. -1 if the parameter was invalid, or less then 0.
     */
    private int posIntParam(String param) {
        try {
            int val = Integer.parseInt(param);
            if (val<0)
                return -1;
            return val;
        }
        catch(NumberFormatException nfexp) {
            return -1;
        }
    }
    
    /** Parses a parameter into a double
     * @return a valid double value, NaN if param is invalid
     */
    private double doubleParam(String param) {
        try {
            double val = Double.parseDouble(param);
            return val;
        }
        catch(NumberFormatException nfexp) {
            return Double.NaN;
        }
    }
    
    /** Parses a parameter into a boolean value
     * @return true if param equals "true", "t", "1", "yes" - not case-sensitive.
     */
    private boolean boolParam(String param) {
        if (param==null) return false;
        if (param.equalsIgnoreCase("true") || param.equalsIgnoreCase("t") || param.equalsIgnoreCase("1") || param.equalsIgnoreCase("yes"))
            return true;
        return false;
    }
    
    /** Parses a color value of the form "#FFFFFF", defaults to white;
     */
    private Color colorParam(String color) {
        if (color==null)
            return colorParam(DEFAULT_COLOR);
        
        // Parse the string
        color = color.replace('#', ' ');
        try {
            System.out.println("decoding "+color);
            return new Color(Integer.parseInt(color.trim(), 16));
        }
        catch(NumberFormatException nfexp) {
            System.out.println("Cannot decode "+color+", using default bgcolor");
            return colorParam(DEFAULT_COLOR);
        }
    }
    
}

