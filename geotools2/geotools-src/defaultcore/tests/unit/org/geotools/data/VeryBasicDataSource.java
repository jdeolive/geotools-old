package org.geotools.data;

import org.geotools.feature.*;
import java.util.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;


public class VeryBasicDataSource implements DataSource {
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
    
    /** gets the extent of this data source using the default speed of
     * this datasource as set by the implementer.
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent() {
        return null;
    }
    
    /** gets the extent of this data source using the speed of
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed) {
        return null;
    }
    
   
    
    /**
     * Saves the given features to the datasource.
     * @param ft featureTable to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is
     * not supported
     */
    public void exportFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        throw new DataSourceException("Very Basic Data Source does not support exporting");
    }
    
    /**
     * Loads Feature rows for the given Extent from the datasource.
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all
     * features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureCollection ft, Extent ex) throws DataSourceException {
        System.out.println("VeryBasicDataSource.load() called");
        
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

                FeatureType testType = new FeatureTypeFlat(geometryAttribute); 
                for(int att=0;att<row.length-1;att++){
                    testType = testType.setAttributeType(stringAttribute);
                }
                
                
                System.out.println("adding P "+p);
                row[0] = geomFac.createPoint(p);
                System.out.println("as Point "+(Point)row[0]);
                for(int val=0;val<row.length;val++){
                    System.out.println("attribue "+val+" is "+row[val].getClass().getName());
                }
                System.out.println("Test Type is "+testType);
                Feature feat = new FeatureFlat((FeatureTypeFlat) testType, row);

                // Filter Feature Feature Filter
                if (ex.containsFeature(feat)){
                    ft.addFeatures(new Feature[] {feat});
                }
            }
            
        }
        catch(Exception exp) {
            System.out.println("Exception loading data");
            throw new DataSourceException("Exception loading data : "+exp.getMessage());
        }
    }
    
}

