package org.geotools.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.DefaultFeatureType;

public class VeryBasicDataSource extends AbstractDataSource 
    implements DataSource {
    String [] sColumnNames = null;
    volatile boolean stopped = false;
    GeometryFactory geomFac = new GeometryFactory();
    URL url;
    
    public VeryBasicDataSource(URL url) throws IOException {
        this.url = url;
        url.openStream().close();
    }
    
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection ft, Query query) throws DataSourceException {
       Filter filter = null;
       if (query != null) {
	   filter = query.getFilter();
       }
       //FeatureCollectionDefault ft = (FeatureCollectionDefault)collection;
        Vector Features = new Vector();
        
        // Open file
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer sb = new StringBuffer();
            
            while (reader.ready() && !stopped) {
                sb.append(reader.readLine());
                sb.append('\n');
            }
            if (stopped) return;
            // Split up the string into rows
            StringTokenizer st = new StringTokenizer(sb.toString(), "\n");
            Vector rows = new Vector();
            while (st.hasMoreElements()) {
                String sRow = (String)st.nextElement();
                sRow = sRow.trim();
                System.out.println("Reading row : "+sRow);
                // Split up into columns
                Vector columns = new Vector();
                columns.addElement("PRIMARY");	// The primary position
                StringTokenizer stc = new StringTokenizer(sRow, ",");
                while (stc.hasMoreElements())
                    columns.addElement(stc.nextElement());
                rows.addElement((String[])columns.toArray(new String[columns.size()]));
                System.out.println("read row:"+rows.size()+" with "+columns.size()+" elements");
            }
            // Get the first row (column names)
            sColumnNames = (String[])rows.elementAt(0);
            
            // Get each Feature - as a GeoPoint + attribs
            for (int i=1;i<rows.size() && !stopped;i++) {
                Coordinate p = new Coordinate();
                Object [] objrow = (Object[])rows.elementAt(i);
                // Create now Object[] for the row
                Object [] row = new Object[objrow.length];
                for (int t=0;t<row.length;t++)
                    row[t] = objrow[t];
                for (int j=0;j<sColumnNames.length;j++) {
                    if (sColumnNames[j].equals("LONGITUDE"))
                        p.x = (new Double(row[j].toString())).doubleValue();
                    if (sColumnNames[j].equals("LATITUDE"))
                        p.y = (new Double(row[j].toString())).doubleValue();
                }
                
                AttributeType geometryAttribute = AttributeTypeFactory.newAttributeType("theGeometry", geomFac.createPoint(p).getClass());
                AttributeType[] attDefs = new AttributeType[row.length];
                attDefs[0] = geometryAttribute;
                
                
                for(int att=1;att<row.length;att++){
                    attDefs[att] = AttributeTypeFactory.newAttributeType(sColumnNames[att],String.class);
                }
                FeatureType testType = FeatureTypeFactory.newFeatureType(attDefs,"test");
                //FeatureType testType = new FeatureTypeFlat(geometryAttribute); 
                
                System.out.println("adding P "+p);
                row[0] = geomFac.createPoint(p);
                System.out.println("as Point "+(Point)row[0]);
                for(int val=0;val<row.length;val++){
                    System.out.println("attribue "+val+" is "+row[val].getClass().getName());
                }
                System.out.println("Test Type is "+testType);
                Feature feat = testType.create(row);

                // Filter Feature Feature Filter
                System.out.println("filter test "+filter.toString()+" -> "+filter.contains(feat));
                if (filter.contains(feat)){
                    System.out.println("Adding feature");
                    ft.add(feat); 
                }
            }
            
        }
        catch(Exception exp) {
            System.out.println("Exception loading data");
            exp.printStackTrace();
            throw new DataSourceException("Exception loading data : "+exp.getMessage(),exp);
        }
        
    }
    

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks TODO: implement this method.
     */
    public FeatureType getSchema(){
	return DefaultFeatureType.EMPTY;
    }


}

