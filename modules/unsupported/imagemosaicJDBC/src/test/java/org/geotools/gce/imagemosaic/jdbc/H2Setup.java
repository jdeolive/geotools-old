package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;

import java.net.URL;

import java.sql.Connection;
import java.sql.PreparedStatement;


public class H2Setup extends JDBCSetup {
    public static H2Setup Singleton = new H2Setup();

    public H2Setup() {
        //        //spatially enable the database
        //        
        //        try {
        //            run(getClass().getResourceAsStream("h2-drop.sql"));
        //        } catch (Exception e) {}
        //        
        //        try {
        //        	run(getClass().getResourceAsStream("h2.sql"));
        //        } catch (Exception e) {
        //        	throw new RuntimeException(e);
        //        }
    }

    @Override
    public String getConfigUrl() {
        return "file:src/test/resources/oek.h2.xml";
    }

    @Override
    protected String getBLOBSQLType() {
        return "BLOB";
    }

    @Override
    protected String getToGemoetryClause(Geometry g) {
        //return "GeomFromText('"+wktText+"',31287)";
        return null;
    }

    @Override
    protected String getMulitPolygonSQLType() {
        //return "blob";
        return null;
    }

    @Override
    protected String getCreateSpatialTableStatement(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected String getCreateSpatialTableStatementJoined(String tableName)
        throws Exception {
        String statement = " CREATE TABLE " + tableName;
        statement += (" ( " + getConfig().getKeyAttributeNameInSpatialTable() +
        " CHAR(64) NOT NULL,");
        statement += (getConfig().getTileMinXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMinYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxXAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getTileMaxYAttribute() + " DOUBLE NOT NULL,");
        statement += (getConfig().getBlobAttributeNameInTileTable() + " " +
        getBLOBSQLType() + ",");
        statement += ("CONSTRAINT " + tableName + "_PK PRIMARY KEY(" +
        getConfig().getKeyAttributeNameInSpatialTable());
        statement += "))";

        return statement;
    }

    @Override
    protected void fillSpatialTable(String spatialTableName, int index,
        Connection con) throws Exception {
        Config config = getConfig();
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = loadIndex(index);
        FeatureIterator<SimpleFeature> it = coll.features();

        String statement = "INSERT INTO " + spatialTableName + " (" +
            config.getKeyAttributeNameInSpatialTable() + "," +
            config.getTileMinXAttribute() + "," +
            config.getTileMinYAttribute() + "," +
            config.getTileMaxXAttribute() + "," +
            config.getTileMaxYAttribute() + ") VALUES  (?,?,?,?,?)";
        PreparedStatement s = con.prepareStatement(statement);

        while (it.hasNext()) {
            SimpleFeature feature = it.next();

            Geometry geom = (Geometry) feature.getDefaultGeometry();
            String location = (String) feature.getAttribute("LOCATION");
            s.setString(1, location);

            Envelope bb = geom.getEnvelopeInternal();
            s.setDouble(2, bb.getMinX());
            s.setDouble(3, bb.getMinY());
            s.setDouble(4, bb.getMaxX());
            s.setDouble(5, bb.getMaxY());
            s.execute();
        }

        coll.close(it);
        con.commit();
    }

    @Override
    protected void fillSpatialTableJoined(String spatialTableName, int index,
        Connection con) throws Exception {
        Config config = getConfig();
        FeatureCollection<SimpleFeatureType, SimpleFeature> coll = loadIndex(index);
        FeatureIterator<SimpleFeature> it = coll.features();

        String statement = "INSERT INTO " + spatialTableName + " (" +
            config.getKeyAttributeNameInSpatialTable() + "," +
            config.getTileMinXAttribute() + "," +
            config.getTileMinYAttribute() + "," +
            config.getTileMaxXAttribute() + "," +
            config.getTileMaxYAttribute() + "," +
            config.getBlobAttributeNameInTileTable() +
            ") VALUES  (?,?,?,?,?,?)";
        PreparedStatement s = con.prepareStatement(statement);

        while (it.hasNext()) {
            SimpleFeature feature = it.next();

            Geometry geom = (Geometry) feature.getDefaultGeometry();
            String location = (String) feature.getAttribute("LOCATION");
            s.setString(1, location);

            Envelope bb = geom.getEnvelopeInternal();
            s.setDouble(2, bb.getMinX());
            s.setDouble(3, bb.getMinY());
            s.setDouble(4, bb.getMaxX());
            s.setDouble(5, bb.getMaxY());

            File file = new File(new URL(BaseDirURL + index).getPath() +
                    File.separator + location);
            s.setBytes(6, getImageBytes(file));
            s.execute();
        }

        coll.close(it);
        con.commit();
    }

    @Override
    protected void createIndex(String tn, Connection con)
        throws Exception {
        String stmt = "CREATE  INDEX IX_" + tn + " ON " + tn + "(" +
            getConfig().getTileMinXAttribute() + "," +
            getConfig().getTileMinYAttribute() + ")";

        con.prepareStatement(stmt).execute();
    }
}
