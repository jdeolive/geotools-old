package org.geotools.data;

import org.geotools.feature.*;
import java.util.*;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.filter.Filter;

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
    
    
    /** Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    
    /** Adds all features from the passed feature collection to the datasource.
     *
     * @param collection The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public void addFeatures(FeatureCollection collection) throws DataSourceException {
    }
    
    /** Gets the bounding box of this datasource using the default speed of
     * this datasource as set by the implementer.
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox() {
        return new Envelope();
    }
    
    /** Gets the bounding box of this datasource using the speed of
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean speed) {
        return new Envelope();
    }
    
    /** Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) throws DataSourceException {
       System.out.println("VeryBasicDataSource.load() called");
        FeatureCollectionDefault ft = new FeatureCollectionDefault();
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
            if (stopped) return null;
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
        return ft;
    }
    
    /** Loads features from the datasource into the passed collection, based on
     * the passed filter.  Note that all data sources must support this method
     * at a minimum.
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */
    public void getFeatures(FeatureCollection collection, Filter filter) throws DataSourceException {

    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter) throws DataSourceException {
    }
    
    /** Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter) throws DataSourceException {
    }
    
    /** Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     */
    public void removeFeatures(Filter filter) throws DataSourceException {
    }
    
}

