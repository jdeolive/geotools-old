package org.geotools.data;

import org.geotools.feature.*;
import java.util.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.filter.Filter;

public class VeryBasicDataSource extends AbstractDataSource 
    implements DataSource {
    String sFilename = null;
    String [] sColumnNames = null;
    boolean stopped = false;
    GeometryFactory geomFac = new GeometryFactory();
    
    public VeryBasicDataSource(String filename) {
        sFilename = filename;
    }
    
   
   
    
    /** gets the Column names (used by FeatureTable) for this DataSource
     */
    public String [] getColumnNames() {

        System.out.println("getColumnNames returning "+sColumnNames);
        return sColumnNames;
    }
    
    /** Stops this DataSource from loading
     */
    public void stopLoading() {
        stopped=true;
        System.out.println("Stopped called on VBdatasource");
    }
    
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection ft, Query query) throws DataSourceException {
       System.out.println("VeryBasicDataSource.load() called");
       Filter filter = null;
       if (query != null) {
	   filter = query.getFilter();
       }
       //FeatureCollectionDefault ft = (FeatureCollectionDefault)collection;
        Vector Features = new Vector();
        
        // Open file
        try {
            File f = new File(sFilename);
            FileInputStream fi = new FileInputStream(f);
            StringBuffer sb = new StringBuffer();
            int o=0;
            byte b[] = new byte[100];
            while (o>-1 && !stopped) {
                o = fi.read(b, 0, 100);
                if (o>-1) {
                    String s = new String(b, 0, o);
                    sb.append(s);
                }
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
                
                AttributeType geometryAttribute = new AttributeTypeDefault("theGeometry", geomFac.createPoint(p).getClass());
                AttributeType stringAttribute = new AttributeTypeDefault("theString", String.class);
                AttributeType[] attDefs = new AttributeType[row.length];
                attDefs[0] = geometryAttribute.setPosition(0);
                
                
                for(int att=1;att<row.length;att++){
                    attDefs[att] = stringAttribute.setPosition(att);
                }
                FeatureType testType = FeatureTypeFactory.create(attDefs);
                //FeatureType testType = new FeatureTypeFlat(geometryAttribute); 
                
                System.out.println("adding P "+p);
                row[0] = geomFac.createPoint(p);
                System.out.println("as Point "+(Point)row[0]);
                for(int val=0;val<row.length;val++){
                    System.out.println("attribue "+val+" is "+row[val].getClass().getName());
                }
                System.out.println("Test Type is "+testType);
                FeatureFactory fac = new FeatureFactory( testType);
                Feature feat = fac.create(row);

                // Filter Feature Feature Filter
                System.out.println("filter test "+filter.toString()+" -> "+filter.contains(feat));
                if (filter.contains(feat)){
                    System.out.println("Adding feature");
                    ft.addFeatures(new Feature[] {feat}); 
                }
            }
            
        }
        catch(Exception exp) {
            System.out.println("Exception loading data");
            exp.printStackTrace();
            throw new DataSourceException("Exception loading data : "+exp.getMessage());
        }
        
    }
    

    /**
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     * @tasks TODO: implement this method.
     */
    public FeatureType getSchema(){
	return null;
    }


}

