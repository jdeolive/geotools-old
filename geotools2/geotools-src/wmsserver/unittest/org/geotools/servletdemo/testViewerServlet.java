package unittest.org.geotools.servletdemo;

import org.geotools.servletdemo.*;
import junit.framework.*;
import java.awt.*;

public class testViewerServlet extends TestCase
{
	public testViewerServlet(String name)
	{
		super(name);
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(testViewerServlet.class);
	}
	
	public void testZoom()
	{
		double [] coords = new double[] {-180.0, -90.0, 180.0, 90.0};

		double [] newCoords = ViewerServlet.zoomOnPoint(true, 0.5, coords, new Rectangle(0, 0, 320, 200), 80, 150);
		System.out.println("xmin="+newCoords[0]);
		System.out.println("ymin="+newCoords[1]);
		System.out.println("xmax="+newCoords[2]);
		System.out.println("ymax="+newCoords[3]);
		newCoords = ViewerServlet.zoomOnPoint(true, 0.5, newCoords, new Rectangle(0, 0, 320, 200), 160, 100);
		System.out.println("\nxmin="+newCoords[0]);
		System.out.println("ymin="+newCoords[1]);
		System.out.println("xmax="+newCoords[2]);
		System.out.println("ymax="+newCoords[3]);
		newCoords = ViewerServlet.zoomOnPoint(true, 0.5, newCoords, new Rectangle(0, 0, 320, 200), 80, 50);
		System.out.println("\nxmin="+newCoords[0]);
		System.out.println("ymin="+newCoords[1]);
		System.out.println("xmax="+newCoords[2]);
		System.out.println("ymax="+newCoords[3]);
	}
	
}

