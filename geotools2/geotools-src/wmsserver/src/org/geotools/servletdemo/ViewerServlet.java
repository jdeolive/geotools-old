package org.geotools.servletdemo;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import javax.servlet.*;
import javax.servlet.http.*;

import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.io.*;
import org.geotools.servlet.viewer.*;

import sun.awt.image.codec.JPEGImageEncoderImpl;

public class ViewerServlet extends HttpServlet 
{
	ServletContext context = null;	
	
	public static final String VIEWER = "viewer";
	
    /**
     * Override init() to set up data used by invocations of this servlet.
     */
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);

        // save servlet context
        context = config.getServletContext();
        
    }

	private URL getMapUrl() throws IOException
	{
		URL url = (new File(context.getInitParameter("shapefile"))).toURL();
		return url;
	}
    
    public void loadMaps(MiniViewer viewer) throws IOException
    {
        //build a full URL from the documentBase and the param fetched above. 
        URL url = getMapUrl();
		System.out.println("Got map URL as "+url.toString());
        
        //Build a ShapefileReader from the above URL.  
        //ShapefileReaders allow access to both the geometry and the attribute data
        //contained within the shapefile.
        ShapefileReader sfr = new ShapefileReader(url);
        
        //Using the shapefileReader, a default theme object is created.
        //Other more advanced versions of getTheme are available which construct more interesting themes
        //by using attribute data
        Theme t = sfr.getTheme();
 
        //Finaly, add the theme created above to the Viewer
        viewer.addTheme(t);
        
        //Thats it, the rest is automatic.
    }

	/** Setup the Viewer and add it to the session
	 */
	private MiniViewer setUpViewer(HttpServletRequest request)
	{
		int width = 320;
		int height = 200;

		System.out.println("Creating Viewer");

		try
		{
			if (request.getParameter("WIDTH")!=null)
				width = Integer.parseInt(request.getParameter("WIDTH"));
			if (request.getParameter("HEIGHT")!=null)
				height = Integer.parseInt(request.getParameter("HEIGHT"));
		}
		catch(NumberFormatException nexp)
		{
			System.out.println("ERROR : ViewerServlet setting up viewer : "+nexp.getMessage());
		}
		
		// Create viewer
		MiniViewer viewer = new MiniViewer(width, height);

		// Load layers
		try
		{
			System.out.println("Loading layer(s)");
			loadMaps(viewer);
		}
		catch(IOException ioexp)
		{
			System.out.println("ERROR : ViewerServlet loading maps: "+ioexp.getMessage());
		}
		
		// Add it to the session
		request.getSession().setAttribute(VIEWER, viewer);
		
		return viewer;
	}

	private MiniViewer checkViewer(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		MiniViewer viewer = (MiniViewer)request.getSession().getAttribute(VIEWER);
		
		if (viewer==null)
		{
			viewer = setUpViewer(request);
		}

		// Width and height params
		int width = 320, height = 200;
		
		try
		{
			if (request.getParameter("WIDTH")!=null)
				width = Integer.parseInt(request.getParameter("WIDTH"));
			if (request.getParameter("HEIGHT")!=null)
				height = Integer.parseInt(request.getParameter("HEIGHT"));
				
			viewer.setDimensions(width, height);
		}
		catch(NumberFormatException nexp)
		{
			doException("InvalidCoords", "The WIDTH or HEIGHT parameter is invalid : "+request.getParameter("BBOX")+"("+nexp.getMessage()+")", request, response);
			return null;
		}
		
		// Coord Params
		double xmin=-180, ymin=-90, xmax=180, ymax=90;
				
		// Parse params
		try
		{
			if (request.getParameter("BBOX")!=null)
			{
				// Parse Parameter
				String sBbox = request.getParameter("BBOX");
				StringTokenizer st = new StringTokenizer(sBbox, ",");
				xmin = Double.parseDouble(st.nextToken());
				ymin = Double.parseDouble(st.nextToken());
				xmax = Double.parseDouble(st.nextToken());
				ymax = Double.parseDouble(st.nextToken());
			}
			else
			{
				doException("InvalidBBOX", "The BBOX parameter is invalid : "+request.getParameter("BBOX"), request, response);
				return null;
			}
		}
		catch(NumberFormatException nexp)
		{
			doException("InvalidBBOX", "The BBOX parameter is invalid : "+request.getParameter("BBOX")+"("+nexp.getMessage()+")", request, response);
			return null;
		}

		GeoRectangle g = new GeoRectangle(xmin, ymin, xmax-xmin, ymax-ymin);
			
		if(viewer!=null)
			viewer.setExtent(g);
			
		return viewer;
	}
	
	/** Returns the lat/long points of a map rendered onto a screen, centered, and zoomed to the given point
	 * @param zoomIn Flag indicating whether to zoom in or zoom out
	 * @param zoomInc The zoom increment - how much to zoom by
	 * @param dPoint 4 latlong points - xmin, ymin, xmax, ymax
	 * @param screen The viewerscreen x, y, width, height values
	 * @param x The x point clicked
	 * @param y The y point clicked
	 * @return A double array - xmin, ymin, xmax, ymax, for the new latlong extent
	 */
	public static double[] zoomOnPoint(
		boolean zoomIn,
		double zoomInc,
		double[] dPoint,
		Rectangle screen,
		int x,
		int y)
	{
		// The new coords
		double[] coords = new double[4];

		double xmin = dPoint[0];
		double ymin = dPoint[1];
		double xmax = dPoint[2];
		double ymax = dPoint[3];

		Scaler scaler = new Scaler(new GeoRectangle(xmin, ymin, xmax-xmin, ymax-ymin), screen);
		double [] map = scaler.toMap(x, y);
		
		double xWidth = xmax - xmin;
//		double xCenter = xWidth * x / screen.width - 180;
		double xCenter = map[0];
		
		if (zoomIn)
				xWidth = xWidth - (xWidth * zoomInc);
		else
				xWidth = xWidth + (xWidth * zoomInc);
		xmin = xCenter - xWidth/2;
		xmax = xCenter + xWidth/2;

		double yWidth = ymax - ymin;
//		double yCenter = yWidth * y / screen.height - 90;
		double yCenter = map[1];
		if (zoomIn)
				yWidth = yWidth - (yWidth * zoomInc);
		else
				yWidth = yWidth + (yWidth * zoomInc);
		ymin = yCenter - yWidth/2;
		ymax = yCenter + yWidth/2;

		coords[0] = xmin;
		coords[1] = ymin;
		coords[2] = xmax;
		coords[3] = ymax;
		
		return coords;
	}
    /**
     * Basic servlet method, answers requests fromt the browser.
     * @param request HTTPServletRequest
     * @param response HTTPServletResponse
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("DoGet called from "+request.getRemoteAddr());
		// Nullify caching
		
		// Check request type
		String sRequest = request.getParameter("REQUEST");
		if (sRequest==null || sRequest.trim().length()==0)
		{
			doException("InvalidRequest", "Invalid REQUEST parameter sent to servlet", request, response);
		}
		else if (sRequest.trim().equalsIgnoreCase("GetCapabilities") || sRequest.trim().equalsIgnoreCase("capabilities"))
		{
			doGetCapabilities(request, response);
		}
		else if (sRequest.trim().equalsIgnoreCase("GetMap"))
		{
			doGetMap(request, response);
		}
		else if (sRequest.trim().equalsIgnoreCase("GetFeatureInfo"))
		{
			doGetFeatureInfo(request, response);
		}
	}
	
	/** Returns WMS 1.1.1 compatible response for a getCapabilities request
	 */
	public void doGetCapabilities(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("Sending capabilities");
		// Get the xml file from the jar
		InputStream is = this.getClass().getResourceAsStream("capabilities.xml");
		// Send to client
		//response.setContentType("application/vnd.ogc.wms_xml");
		response.setContentType("text/xml");
		
		OutputStream out = response.getOutputStream();
		int b;
		while ((b = is.read())!=-1)
			out.write(b);
		out.close();
	}

	/** Returns WMS 1.1.1 compatible response for a getMap request
	 */
	public void doGetMap(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		System.out.println("Sending Map");
		// Check validity of request params
		if (request.getParameter("SRS")==null ||  !(request.getParameter("SRS").trim().equals("EPSG:4326")))
		{
			doException("UnsupportedSRS", "SRS not supported or missing", request, response);
			return;
		}
		
		// Check the viewer	
		MiniViewer viewer = checkViewer(request, response);
		
		response.setContentType("image/jpeg");

		// Paint viewer onto offscreen surface
		try
		{
			if (viewer!=null)
			{
				BufferedImage awtImage = new BufferedImage(viewer.getViewerSize().width,viewer.getViewerSize().height,BufferedImage.TYPE_INT_RGB);
			
				Graphics g = awtImage.getGraphics();
			
				viewer.paintThemes(g);
			
				OutputStream out = response.getOutputStream();
				JPEGImageEncoderImpl j = new JPEGImageEncoderImpl(out);
				j.encode(awtImage);
				out.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}				
	}

	/** Returns WMS 1.1.1 compatible response for a getFeatureInfo request
	 * Currently, this returns an exception, as this servlet does not support getFeatureInfo
	 */
	public void doGetFeatureInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doException("NotSupported", "GetFeatureInfo is not supported by this app", request, response);
	}
	
	/** Returns WMS 1.1.1 compatible Exception
	 */
	public void doException(String sCode, String sException, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// Send to client
		//response.setContentType("application/vnd.ogc.se_xml");
		response.setContentType("text/xml");
		
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
	}
	

}

