/*
 * FeatureTableModel.java
 *
 * Created on March 18, 2002, 4:06 PM
 */

package org.geotools.gui.swing.tables;
import org.geotools.feature.*;
import org.geotools.data.*;

/**
 * An implementaion of swings TableModel which allows FeatureTables to be displayed.
 * TODO: It would be excelent if there were custom cell renderers available for Geometry types
 * @author James Macgill
 */
public class FeatureTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.table.TableModel {
    /** Holds the FeatureTable that will be represented by this model
     */    
    protected FeatureCollection featureTable;
    
    
    /** Creates a new instance of FeatureTableModel */
    public FeatureTableModel() {
    }

    /** Set which featuretable to represent
     * @param featureTable The feature table to represent.  This could fire a Table Structure Changed event.
     */    
    public void setFeatureCollection(FeatureCollection featureCollection){
        this.featureTable = featureCollection;
        this.fireTableStructureChanged();
    }

    /** The number of columns in the feature table
     * Note, for the moment this is determined by the first feature.
     * @return the number of columns in this feature table
     */    
    public int getColumnCount() {
        if(featureTable==null)return 0;
        Feature features[] = featureTable.getFeatures();
        if(features.length==0)return 0;
        return features[0].getAttributes().length;
    }
    
    /** gets the row count for the feature table
     * @return the number of features in featuretable
     */    
    public int getRowCount() {
        if(featureTable==null)return 0;
        return featureTable.getFeatures().length;
    }
    
    /** Gets the name of a specified column
     * @param col The index of the column to get the name of.
     * @return The name of 'col'
     */    
    public String getColumnName(int col){
        if(featureTable==null)return null;
        Feature features[] = featureTable.getFeatures();
        if(features.length==0)return null;
        AttributeType[] attribDefn = features[0].getSchema().getAllAttributeTypes();
        return attribDefn[col].getName();
    }
        
    
    /** Get the value stored in a specifed cell.
     * In this case, row=Feature and col=Attribute
     * @param row the row number
     * @param col the column number
     * @return the value in the specifed cell
     */    
    public Object getValueAt(int row, int col) {
        Feature features[] = featureTable.getFeatures();
        return features[row].getAttributes()[col];
    }
    
}
