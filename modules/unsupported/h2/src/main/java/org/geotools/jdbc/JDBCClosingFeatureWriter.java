package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;

import org.geotools.data.DelegatingFeatureReader;
import org.geotools.data.DelegatingFeatureWriter;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCClosingFeatureWriter implements FeatureWriter<SimpleFeatureType,SimpleFeature> {

    FeatureWriter writer;
    
    public JDBCClosingFeatureWriter(FeatureWriter writer) {
        this.writer = writer;
    }

   public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) writer.getFeatureType();
    }

    public boolean hasNext() throws IOException {
        return writer.hasNext();
    }


    public SimpleFeature next() throws IOException {
        return (SimpleFeature) writer.next();
    }

    
    public void remove() throws IOException {
        writer.remove();
    }

    public void write() throws IOException {
        writer.write();
    }
    
    public void close() throws IOException {
        FeatureWriter w = writer;
        while( w instanceof DelegatingFeatureWriter ) {
            if ( w instanceof JDBCFeatureReader ) {
                break;
            }
            
            w = ((DelegatingFeatureWriter)w).getDelegate();
        }
        
        if ( w instanceof JDBCFeatureReader ) {
            JDBCFeatureReader jdbcReader = (JDBCFeatureReader) w;
            JDBCFeatureStore fs = jdbcReader.featureStore;
            Connection cx = jdbcReader.cx;

            try {
                writer.close();
            }
            finally {
                fs.getDataStore().releaseConnection( cx, fs.getState() );
            }
        }
    }
}
