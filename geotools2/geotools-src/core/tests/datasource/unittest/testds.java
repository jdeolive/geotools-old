package unittest;

import uk.ac.leeds.ccg.geotools.*;
import com.sun.java.util.collections.*;
import datasource.*;

public class testds implements TableChangedListener
{
	FeatureTable ft = null;
	FeatureIndex fi = null;
	
	public static void main(String[] args)
	{
		testds test = new testds();
		test.run();
	}

	public void run()
	{
		System.out.println("Run() called");
		RectExtent r = new RectExtent();
		r.setBounds(new GeoRectangle(50, 0, 310.0, 180.0));
		ft = new FeatureTable(new VeryBasicDataSource("c:\\Furizibad.csv"));
		ft.setLoadMode(FeatureTable.MODE_LOAD_INTERSECT);
		ft.addTableChangedListener(this);
		// Request extent
		try
		{
			fi = new SimpleIndex(ft, "LONGITUDE");
			ft.requestExtent(r);		
		}
		catch(Exception exp)
		{
			System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
		}
		// Request another extent - should get in ahead of the first extent completing, stop the load, and load this extent instead
		try
		{
			r.setBounds(new GeoRectangle(0, 0, 50.0, 180.0));
			ft.requestExtent(r);		
		}
		catch(Exception exp)
		{
			System.out.println("Exception requesting Extent : "+exp.getClass().getName()+" : "+exp.getMessage());
		}
	}
	
	public void tableChanged(TableChangedEvent tce)
	{
		System.out.println("tableChanged called()");
		System.out.println("tableChanged() : Return code : "+tce.getCode());
		if (tce.getCode()!=tce.TABLE_OK)
		{
			System.out.println("tableChanged() : Exception :"+tce.getException().getClass().getName());
			tce.getException().printStackTrace();
		}
		else
		{
			System.out.println("Load code ok - Reading Index");
			Iterator it = fi.getFeatures().iterator();
			while (it.hasNext())
			{
				Feature f = (Feature)it.next();
				System.out.println("Feature  : "+f.row[0].toString());
			}
		}
	}
}

