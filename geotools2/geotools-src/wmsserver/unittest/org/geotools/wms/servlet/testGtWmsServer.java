package unittest.org.geotools.wms.servlet;

import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;

import java.awt.image.*;

public class testGtWmsServer extends TestCase
{
	WMSServer server;
	
	public testGtWmsServer(String name)
	{
		super(name);
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(testGtWmsServer.class);
	}

	public void setUp()
	{
		server = new GtWmsServer();
	}
	
	public void testGetCapabilities()
	{
		try
		{
			String capabilities = server.getCapabilities(null);
			System.out.println(capabilities);			
		}
		catch(WMSException wmsexp)
		{
			fail ("WMSException : "+wmsexp.getMessage());
		}
	}
	
	public void testGetMap()
	{
		try
		{
			BufferedImage map = server.getMap(new String[] {"my layer"}, null, "EPSG:4326", new double[] {-180.0, -90.0, 180.0, 90.0}, 320, 200, false, null);
			ImageView view = new ImageView(map, "the map");
			view.createFrame();
		}
		catch(WMSException wmsexp)
		{
			fail("WMSException : "+wmsexp.getMessage());
		}
	}
}

