package org.geotools.mapinfo;

import uk.ac.leeds.ccg.geotools.layer.*;
import uk.ac.leeds.ccg.geotools.*;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.Extent;
import org.geotools.feature.*;

import java.net.URL;
import java.io.*;
import java.util.Vector;
import java.util.Iterator;

import com.vividsolutions.jts.geom.*;

/** A MapInfo implementation of the DataSource interface
 */
public class MapInfoDataSource implements DataSource
{
	MIFMIDReader reader;

	public MapInfoDataSource()
	{
		reader = null;
	}
	
	public MapInfoDataSource(MIFMIDReader mmReader)
	{
		reader = mmReader;
	}
	
	/** Sets the source file for this DataSource
	 */
	public void setSource(File file) throws FileNotFoundException, IOException, MIFObjectTypeException
	{
		setSource(file.toURL());
	}
	
	/** Sets the source URL for this DataSource
	 */
	public void setSource(URL url) throws FileNotFoundException, IOException, MIFObjectTypeException
	{
		setSource(new MIFMIDReader(url));
	}
	
	/** Sets the source MIFMIDReader for this DataSource
	 */
	public void setSource(MIFMIDReader mmReader) throws FileNotFoundException, IOException, MIFObjectTypeException
	{
		reader = mmReader;
	}
	
	/**
	 * @see DataSource#importFeatures(FeatureCollection, Extent)
	 */
	public void importFeatures(FeatureCollection ft, Extent ex)
		throws DataSourceException
	{
		try
		{
			// Read the file
			MIFMIDFile file = reader.getMIFMIDFile();
			
			// Get the layers - ignore MultiLayer, I don't think we need it
			PointLayer pointLayer = file.getPointLayer();
			LineLayer lineLayer = file.getLineLayer();
			PolygonLayer polygonLayer = file.getPolygonLayer();
			GeoData [] pointData = file.getPointGeoData();
			GeoData [] lineData = file.getLineGeoData();
			GeoData [] polygonData = file.getPolygonGeoData();
			
			// Get the column names for the attributes
			Vector vColumns = new Vector();
			for (int i=0;pointData!=null && i<pointData.length;i++)
				if (!vColumns.contains(pointData[i].getName()))
					vColumns.addElement(pointData[i].getName());
			for (int i=0;lineData!=null && i<lineData.length;i++)
				if (!vColumns.contains(lineData[i].getName()))
					vColumns.addElement(lineData[i].getName());
			for (int i=0;polygonData!=null && i<polygonData.length;i++)
				if (!vColumns.contains(polygonData[i].getName()))
					vColumns.addElement(polygonData[i].getName());
			
			for (int i=0;i<vColumns.size();i++)
				System.out.println("Column : "+vColumns.elementAt(i));
			
			Vector vFeatures = new Vector();
			
			// Two features - Geometry and data string
			AttributeType [] attribs = new AttributeType[1+vColumns.size()];
			attribs[0] = new AttributeTypeDefault("feature", Geometry.class);
			for (int i=0;i<vColumns.size();i++)
				attribs[i+1] = new AttributeTypeDefault((String)vColumns.elementAt(i), String.class);
				
			FeatureType featureType = new FeatureTypeFlat(attribs);
			// Create point Features
			if (pointLayer!=null)
			{
				vFeatures.addAll(createPointFeatures(pointLayer.getShapes(), pointData, featureType));
			}
			if (lineLayer!=null)
			{
				vFeatures.addAll(createLineFeatures(lineLayer.getShapes(), lineData, featureType));
			}
			if (polygonLayer!=null)
			{
				vFeatures.addAll(createPolygonFeatures(polygonLayer.getShapes(), polygonData, featureType));
			}
			
			// Filter out features which are not within the given Extent
			for (int i=vFeatures.size()-1;i>=0;i--)
				if (!ex.containsFeature((Feature)vFeatures.elementAt(i)))
					vFeatures.remove(i);
			
			// Add the Shapes to the FeatureCollection as Features 
			ft.addFeatures((Feature[])vFeatures.toArray(new Feature[vFeatures.size()]));
		}
		catch(Exception exp)
		{
			throw new DataSourceException("Unknown Exception reading Features - "+exp.getClass().getName()+" : "+exp.getMessage());
		}
	}

	private Vector createPointFeatures(Vector pointShapes, GeoData [] pointData, FeatureType featureType) throws DataSourceException
	{
		System.out.println("Creating Points");
		
		Vector vFeatures = new Vector();
		
		// Set up factory
		FeatureFactory fac = new FeatureFactory(featureType);
		GeometryFactory factory = new GeometryFactory();

		Iterator it = pointShapes.iterator();
		while (it.hasNext())
		{
			GeoPoint point = (GeoPoint)it.next();
			Geometry pointGeom = factory.createPoint(new Coordinate(point.getX(),point.getY()));
			
			Object [] row = new Object[featureType.getAllAttributeTypes().length];
			row[0] = pointGeom;
			if (pointData!=null)
				for (int i=0;i<pointData.length;i++)
					row[getAttributeOffset(pointData[i].getName(), featureType)] = pointData[i].getText(point.getID());
			try
			{
				Feature pointFeature = fac.create(row);
				vFeatures.addElement(pointFeature);
			}
			catch(IllegalFeatureException ifexp)
			{
				throw new DataSourceException("Illegal Feature type");
			}
		}
		
		return vFeatures;
	}

	private Vector createLineFeatures(Vector lineShapes, GeoData [] lineData, FeatureType featureType) throws DataSourceException
	{
		System.out.println("Creating Lines");
		
		Vector vFeatures = new Vector();

		// Set up factory
		FeatureFactory fac = new FeatureFactory(featureType);
		GeometryFactory factory = new GeometryFactory();

		Iterator it = lineShapes.iterator();
		while (it.hasNext())
		{
			GeoLine line = (GeoLine)it.next();
			// Construct array of coordinates
			Vector points = line.getPoints();
			Coordinate [] cPoints = new Coordinate[points.size()];
			for (int i=0;i<cPoints.length;i++)
				cPoints[i] = new Coordinate(((GeoPoint)points.elementAt(i)).getX(), ((GeoPoint)points.elementAt(i)).getY());
				
			// Construct Line Geomety (LineString)
			Geometry lineGeom = factory.createLineString(cPoints);
			
			Object [] row = new Object[featureType.getAllAttributeTypes().length];
			row[0] = lineGeom;
			if (lineData!=null)
				for (int i=0;i<lineData.length;i++)
					row[getAttributeOffset(lineData[i].getName(), featureType)] = lineData[i].getText(line.getID());
			try
			{
				Feature lineFeature = fac.create(row);
				vFeatures.addElement(lineFeature);
			}
			catch(IllegalFeatureException ifexp)
			{
				throw new DataSourceException("Illegal Feature type");
			}
		}

		return vFeatures;
	}

	private Vector createPolygonFeatures(Vector polygonShapes, GeoData [] polygonData, FeatureType featureType) throws DataSourceException
	{
		System.out.println("Creating Polygons");
		
		Vector vFeatures = new Vector();

		// Set up factory
		FeatureFactory fac = new FeatureFactory(featureType);
		GeometryFactory factory = new GeometryFactory();

		Iterator it = polygonShapes.iterator();
		while (it.hasNext())
		{
			GeoPolygon polygon = (GeoPolygon)it.next();
			// Construct array of coordinates
			Vector points = polygon.getPoints();
			Coordinate [] cPoints = new Coordinate[points.size()];
			for (int i=0;i<cPoints.length;i++)
				cPoints[i] = new Coordinate(((GeoPoint)points.elementAt(i)).getX(), ((GeoPoint)points.elementAt(i)).getY());
				
			// Construct Polygon Geomety (PolygonString)
			Geometry polygonGeom;
			try
			{
				polygonGeom = factory.createPolygon(factory.createLinearRing(cPoints), null);
			}
			catch(TopologyException topexp)
			{
				throw new DataSourceException("TopologyException building JTS polygon : "+topexp.getMessage());
			}
			
			Object [] row = new Object[featureType.getAllAttributeTypes().length];
			row[0] = polygonGeom;
			if (polygonData!=null)
				for (int i=0;i<polygonData.length;i++)
					row[getAttributeOffset(polygonData[i].getName(), featureType)] = polygonData[i].getText(polygon.getID());
			try
			{
				Feature polygonFeature = fac.create(row);
				vFeatures.addElement(polygonFeature);
			}
			catch(IllegalFeatureException ifexp)
			{
				throw new DataSourceException("Illegal Feature type");
			}
		}

		return vFeatures;	
	}

	private int getAttributeOffset(String name, FeatureType featureType) throws DataSourceException
	{
		if (featureType==null) throw new DataSourceException("Illegal AttributeType name '"+name+"'");
		AttributeType [] types = featureType.getAllAttributeTypes();
		for (int i=0;i<types.length;i++)
			if (types[i].getName().equals(name))
				return i;
		throw new DataSourceException("Illegal AttributeType name '"+name+"'");
	}

	/**
	 * @see DataSource#exportFeatures(FeatureCollection, Extent)
	 */
	public void exportFeatures(FeatureCollection ft, Extent ex)
		throws DataSourceException
	{
	}

	/**
	 * @see DataSource#stopLoading()
	 * will be deprecated pretty soon, I think
	 */
	public void stopLoading()
	{
		
	}

	/**
	 * @see DataSource#getExtent()
	 */
	public Extent getExtent()
	{
		return null;
	}

	/**
	 * @see DataSource#getExtent(boolean)
	 */
	public Extent getExtent(boolean speed)
	{
		return null;
	}

}

