package unittest.org.geotools.mapinfo;
import org.geotools.mapinfo.*;
import org.geotools.data.*;
import org.geotools.feature.*;

import junit.framework.*;

import java.io.*;
import java.util.*;

import com.vividsolutions.jts.geom.*;

public class testMapInfoDataSource extends TestCase
{
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
	}

	public void testLoad()
	{
		try
		{
			// Load file
			String miffile = "D:\\geotools\\Task6\\test.mif";
			Vector objects = dsMapInfo.readMifMid(miffile);
			System.out.println("Read "+objects.size()+" object");
		}
		catch(DataSourceException dsexp)
		{
			dsexp.printStackTrace();
			fail("Exception : "+dsexp.getMessage());
		}
	}
	
}

