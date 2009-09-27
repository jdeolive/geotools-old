package org.geotools.swing.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.geotools.feature.FeatureCollection;
import org.geotools.util.NullProgressListener;
import org.jdesktop.swingworker.SwingWorker;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * TableModel that will display a feature collection.
 * <p>
 */
public class FeatureCollectionTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -7119885084300393935L;

    private SimpleFeatureType schema;
    
    List<Object[]> cache = new ArrayList<Object[]>();

    public IOException exception;

    class TableWorker extends SwingWorker<List<Object[]>, Object[]> {
        FeatureCollection<SimpleFeatureType,SimpleFeature> features;
        
        TableWorker( FeatureCollection<SimpleFeatureType,SimpleFeature> features ) { 
            this.features = features;            
        }
        
        public List<Object[]> doInBackground() {
            List<Object[]> list = new ArrayList<Object[]>();
            
            final NullProgressListener listener = new NullProgressListener();
            try {
                features.accepts( new FeatureVisitor() {                
                    public void visit(Feature feature) {
                        SimpleFeature simple = (SimpleFeature) feature;
                        Object[] row = simple.getAttributes().toArray();
                        publish( row );
                        
                        if( isCancelled() ) listener.setCanceled(true);
                    }
                } , listener );
            } catch (IOException e) {
                exception = e;
            }           
            return list;
        }
        protected void process(List<Object[]> chunks) {            
            int from = cache.size();
            cache.addAll( chunks );
            int to = cache.size();
            fireTableRowsInserted( from, to );
        }        
    }
    TableWorker load;
    /**
     * @param features
     */
    public FeatureCollectionTableModel( FeatureCollection<SimpleFeatureType,SimpleFeature> features ){
        this.load = new TableWorker( features );
        load.execute();
        this.schema = features.getSchema();
    }
    
    public void dispose() {
        load.cancel(false);        
    }
    
    public String getColumnName(int column) {
        return schema.getDescriptor( column ).getLocalName();
    }
    
    public int getColumnCount() {
        if( exception != null ){
            return 1;
        }
        return schema.getAttributeCount();
    }

    public int getRowCount() {
        if( exception != null ){
            return 1;
        }
        return cache.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( rowIndex < cache.size() ){
            Object row[] = cache.get( rowIndex );
            return row[ columnIndex ];
        }
        return null;
    }

}
