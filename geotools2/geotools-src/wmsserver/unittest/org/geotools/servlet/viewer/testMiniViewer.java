package unittest.org.geotools.servlet.viewer;

import junit.framework.*;
import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.io.*;
import org.geotools.servlet.viewer.*;
import sun.awt.image.codec.JPEGImageEncoderImpl;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;

public class testMiniViewer extends TestCase
{
	MiniViewer viewer;
	String shapeFile;
	URL codeBase;
	
	File jpg;
	
	public testMiniViewer(String name)
	{
		super(name);
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(testMiniViewer.class);
	}
	
	public void setUp()
	{
		viewer = new MiniViewer(320, 200);

		shapeFile = "statepop.zip";
		try
		{
			codeBase = (new File("D:\\geotools\\Task1\\src\\demonstrations\\maps")).toURL();
			jpg = new File("c:\\out.jpg");
		}
		catch(Exception exp)
		{
			fail(exp.toString());
		}
	}
	
	public void testDraw()
	{
		try
		{
			loadMaps();
		}
		catch (Exception exp)
		{
			fail(exp.toString());
		}

		// Extent to top left corner or worldmap
		viewer.setExtent(new GeoRectangle(-180, 0, 180, 90));

		try
		{
			BufferedImage awtImage = new BufferedImage(320,200,BufferedImage.TYPE_INT_RGB);
			
			Graphics g = awtImage.getGraphics();
			
			viewer.paintThemes(g);
			
			OutputStream out = new FileOutputStream(jpg);
			JPEGImageEncoderImpl j = new JPEGImageEncoderImpl(out);
			j.encode(awtImage);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
    public void loadMaps() throws IOException{
        //build a full URL from the documentBase and the param fetched above. 
        URL url = new URL(codeBase,shapeFile);
        
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
    
}

