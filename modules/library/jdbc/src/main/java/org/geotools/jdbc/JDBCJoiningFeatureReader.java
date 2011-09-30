package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.Join;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.jdbc.JoinInfo.JoinPart;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCJoiningFeatureReader extends JDBCFeatureReader {

    List<JDBCFeatureReader> joinReaders;

    public JDBCJoiningFeatureReader(String sql, Connection cx, JDBCFeatureSource featureSource,
        SimpleFeatureType featureType, JoinInfo join, Hints hints) 
        throws SQLException, IOException {

        super(sql, cx, featureSource, retype(featureType, join), hints);

        init(cx, featureSource, featureType, join, hints);
    }
    
    public JDBCJoiningFeatureReader(PreparedStatement st, Connection cx, JDBCFeatureSource featureSource,
        SimpleFeatureType featureType, JoinInfo join, Hints hints) 
        throws SQLException, IOException {

        super(st, cx, featureSource, retype(featureType, join), hints);

        init(cx, featureSource, featureType, join, hints);
    }

    void init(Connection cx, JDBCFeatureSource featureSource, SimpleFeatureType featureType, 
        JoinInfo join, Hints hints) throws SQLException, IOException {
        joinReaders = new ArrayList<JDBCFeatureReader>();
        int offset = featureType.getAttributeCount() + getPrimaryKey().getColumns().size();

        for (JoinPart part : join.getParts()) {
            SimpleFeatureType ft = part.getQueryFeatureType();
            joinReaders.add(new JDBCFeatureReader(rs, cx, offset,
                featureSource.getDataStore().getAbsoluteFeatureSource(ft.getTypeName()), ft, hints)); 
        }
    }

    @Override
    public boolean hasNext() throws IOException {
        boolean next = super.hasNext();
        for (JDBCFeatureReader r : joinReaders) {
            r.setNext(next);
        }
        return next;
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        //read the regular feature
        SimpleFeature f = super.next();

        //add additional attributes for joined features
        for (int i = 0; i < joinReaders.size(); i++) {
            JDBCFeatureReader r = joinReaders.get(i);
            f.setAttribute(f.getAttributeCount() - joinReaders.size() + i, r.next());
        }

        return f;
    }

    @Override
    public void close() throws IOException {
        super.close();

        //we don't need to close the delegate readers because they share the same result set 
        // and connection as this reader
    }

    static SimpleFeatureType retype(SimpleFeatureType featureType, JoinInfo join) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.init(featureType);
        
        for (JoinPart part : join.getParts()) {
            b.add(part.getAttributeName(), SimpleFeature.class);
        }
        return b.buildFeatureType();
    }
}
