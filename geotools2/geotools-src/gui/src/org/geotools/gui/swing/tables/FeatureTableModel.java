/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */

package org.geotools.gui.swing.tables;
import org.geotools.feature.*;
import org.geotools.data.*;

/**
 * An implementation of Swing's TableModel which allows FeatureTables
 * to be displayed.
 * TODO: It would be excellent if there were custom cell renderers available
 * for Geometry types.
 *
 * $Id: FeatureTableModel.java,v 1.2 2002/07/15 10:16:48 loxnard Exp $
 * @author James Macgill, CCG
 */
public class FeatureTableModel extends javax.swing.table.AbstractTableModel implements javax.swing.table.TableModel {
    /**
     * Holds the FeatureTable that will be represented by this model.
     */    
    protected FeatureCollection featureTable;
    
    
    /**
     * Creates a new instance of FeatureTableModel
     */
    public FeatureTableModel() {
    }

    /**
     * Sets which featureTable to represent
     * @param featureTable The featureTable to represent.
     *        This could fire a Table Structure Changed event.
     */    
    public void setFeatureCollection(FeatureCollection featureCollection){
        this.featureTable = featureCollection;
        this.fireTableStructureChanged();
    }

    /**
     * The number of columns in the featureTable.
     * Note: for the moment, this is determined by the first feature.
     * @return the number of columns in this featureTable.
     */    
    public int getColumnCount() {
        if (featureTable == null) return 0;
        Feature features[] = featureTable.getFeatures();
        if (features.length == 0) return 0;
        return features[0].getAttributes().length;
    }
    
    /**
     * Gets the row count for the featureTable.
     * @return the number of features in featuretable.
     */    
    public int getRowCount() {
        if (featureTable == null) return 0;
        return featureTable.getFeatures().length;
    }
    
    /**
     * Gets the name of a specified column.
     * @param col the index of the column to get the name of.
     * @return the name of 'col'.
     */    
    public String getColumnName(int col){
        if (featureTable == null) return null;
        Feature features[] = featureTable.getFeatures();
        if (features.length == 0) return null;
        AttributeType[] attribDefn = features[0].getSchema().getAllAttributeTypes();
        return attribDefn[col].getName();
    }
        
    
    /**
     * Gets the value stored in a specified cell.
     * In this case, row=Feature and col=Attribute.
     * @param row the row number.
     * @param col the column number.
     * @return the value in the specified cell.
     */    
    public Object getValueAt(int row, int col) {
        Feature features[] = featureTable.getFeatures();
        return features[row].getAttributes()[col];
    }
    
}
