package unittest.org.geotools.wms.servlet;

import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;

public class testLayerReader extends TestCase
{
	LayerReader reader;
	
	public testLayerReader(String name)
	{
		super(name);
	}
	
	public static void main(String args[]) {
		junit.textui.TestRunner.run(testLayerReader.class);
	}

	public void setUp()
	{
		reader = new LayerReader();
	}
	
	public void testParse()
	{
		LayerEntry [] layers = reader.read(this.getClass().getResourceAsStream("/org/geotools/wms/gtserver/layers.xml"));
		if (layers!=null)
		{
			for (int i=0;i<layers.length;i++)
				System.out.println("Layer : "+layers[i].id+" = "+layers[i].description);
		}
		else
			System.out.println("Layers are null");
	}
}

