package unittest.org.geotools.mapinfo;

import org.geotools.mapinfo.*;
import org.geotools.data.*;
import org.geotools.feature.*;

import junit.framework.*;

import java.io.*;

import com.vividsolutions.jts.geom.*;

public class testMapInfoDataSource extends TestCase
{
	FeatureCollection fc;
	MapInfoDataSource dsMapInfo;
	
	public testMapInfoDataSource(String name)
	{
		super(name);
	}
	
	public static void main(String args[]) {
		System.out.println("Running");
		junit.textui.TestRunner.run(testMapInfoDataSource.class);
	}
	
	public void setUp()
	{
		dsMapInfo = new MapInfoDataSource();
		try
		{
			dsMapInfo.setSource(new File("D:\\geotools\\Task6\\MapInfo\\Arable_Land.MIF"));
		}
		catch(FileNotFoundException fnfexp)
		{
			fail ("FileNotFound Exception : "+fnfexp.getMessage());
		}
		catch(IOException ioexp)
		{
			fail ("IO Exception : "+ioexp.getMessage());
		}
		catch(MIFObjectTypeException mifexp)
		{
			fail ("FileNotFound Exception : "+mifexp.getMessage());
		}
		fc = new FeatureCollectionDefault(dsMapInfo);
	}
	
	public void testReadDataSource()
	{
		System.out.println("testReadDataSource()");
		fc.setExtent(new DummyExtent());
		try
		{
			dsMapInfo.importFeatures(fc, new DummyExtent());
			System.out.println("Read "+fc.getFeatures().length+" features");
			
			Feature [] features = fc.getFeatures();
			
			for (int i=0;i<10;i++)
			{
				Polygon poly = (Polygon)features[i].getDefaultGeometry();
				System.out.println("Poly : "+poly.toString());
			}
		}
		catch(DataSourceException dsexp)
		{
			fail("DataSource Exception : "+dsexp.getMessage());
		}
	}
}

