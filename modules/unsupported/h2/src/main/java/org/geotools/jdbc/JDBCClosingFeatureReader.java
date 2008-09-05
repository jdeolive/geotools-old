package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.util.NoSuchElementException;

import org.geotools.data.DelegatingFeatureReader;
import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCClosingFeatureReader implements DelegatingFeatureReader<SimpleFeatureType, SimpleFeature> {

    FeatureReader reader;
    
    public JDBCClosingFeatureReader( FeatureReader reader ) {
        this.reader = reader;
    }
    
    public FeatureReader<SimpleFeatureType, SimpleFeature> getDelegate() {
        return reader;
    }
    
    public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) reader.getFeatureType();
    }

    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        return (SimpleFeature) reader.next();
    }

    public void close() throws IOException {
        
        FeatureReader r = reader;
        while( r instanceof DelegatingFeatureReader ) {
            if ( r instanceof JDBCFeatureReader ) {
                break;
            }
            
            r = ((DelegatingFeatureReader)r).getDelegate();
        }
        
        if ( r instanceof JDBCFeatureReader ) {
            JDBCFeatureReader jdbcReader = (JDBCFeatureReader) r;
            JDBCFeatureStore fs = jdbcReader.featureStore;
            Connection cx = jdbcReader.cx;

            try {
                reader.close();
            }
            finally {
                fs.getDataStore().releaseConnection( cx, fs.getState() );
            }
        }
    }
}
