/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gui.swing.tables;

import org.geotools.data.*;
import org.geotools.feature.*;


/**
 * An implementation of Swing's TableModel which allows FeatureTables to be
 * displayed.
 *
 * @author James Macgill, CCG
 * @version $Id: FeatureTableModel.java,v 1.4 2004/02/27 14:02:00 aaime Exp $
 *
 * @task TODO: It would be excellent if there were custom cell renderers
 *       available for Geometry types.
 */
public class FeatureTableModel extends javax.swing.table.AbstractTableModel
    implements javax.swing.table.TableModel {
    /** Holds the FeatureTable that will be represented by this model. */
    protected FeatureCollection featureTable;

    /**
     * Creates a new instance of FeatureTableModel
     */
    public FeatureTableModel() {
    }
    
    /**
     * Creates a new instance of FeatureTableModel based on the feature collection provided
     * @param featureCollection
     */
    public FeatureTableModel(FeatureCollection featureCollection) {
        setFeatureCollection(featureCollection);
    }

    /**
     * Sets which featureTable to represent
     *
     * @param featureCollection The featureTable to represent. This could fire
     *        a Table Structure Changed event.
     */
    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureTable = featureCollection;
        this.fireTableStructureChanged();
    }

    /**
     * The number of columns in the featureTable. Note: for the moment, this is
     * determined by the first feature.
     *
     * @return the number of columns in this featureTable.
     *
     * @task HACK: Just gets first feature type - should use typed feature
     *       collection.  Revisit when we have FeatureDocument.
     */
    public int getColumnCount() {
        if (featureTable == null) {
            return 0;
        }

        //Feature features[] = featureTable.getFeatures();
        if (featureTable.size() == 0) {
            return 0;
        }

        return featureTable.features().next().getNumberOfAttributes();
    }

    /**
     * Gets the row count for the featureTable.
     *
     * @return the number of features in featuretable.
     */
    public int getRowCount() {
        if (featureTable == null) {
            return 0;
        }

        return featureTable.size();
    }

    /**
     * Gets the name of a specified column.
     *
     * @param col the index of the column to get the name of.
     *
     * @return the name of 'col'.
     *
     * @task HACK: Just gets first feature type - should use typed feature
     *       collection.  Revisit when we have FeatureDocument.
     */
    public String getColumnName(int col) {
        if (featureTable == null) {
            return null;
        }

        //Feature features[] = featureTable.getFeatures();
        if (featureTable.size() == 0) {
            return null;
        }

        Feature firstFeature = featureTable.features().next();
        FeatureType firstType = firstFeature.getFeatureType();

        return firstType.getAttributeType(col).getName();
    }

    /**
     * Gets the value stored in a specified cell. In this case, row=Feature and
     * col=Attribute.
     *
     * @param row the row number.
     * @param col the column number.
     *
     * @return the value in the specified cell.
     */
    public Object getValueAt(int row, int col) {
        int n = featureTable.size();
        Feature[] features = (Feature[]) featureTable.toArray(new Feature[n]);

        return features[row].getAttribute(col);
    }
}
