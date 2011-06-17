package org.geotools.data.postgis;

import org.geotools.jdbc.JDBCJoinTestSetup;

public class PostgisJoinTestSetup extends JDBCJoinTestSetup {

    public PostgisJoinTestSetup() {
        super(new PostGISTestSetup());
    }

    @Override
    protected void createJoinTable() throws Exception {
        run("CREATE TABLE \"ftjoin\" ( \"id\" int, " + "\"name\" VARCHAR, \"geom\" GEOMETRY)" );
        run("INSERT INTO geometry_columns VALUES ('', 'public', 'ftjoin', 'geom', 2, 4326, 'GEOMETRY')");
        
        run( "INSERT INTO \"ftjoin\" VALUES (0, 'zero', ST_GeomFromText('POLYGON ((-0.1 -0.1, -0.1 0.1, 0.1 0.1, 0.1 -0.1, -0.1 -0.1))', 4326))");
        run( "INSERT INTO \"ftjoin\" VALUES (1, 'one', ST_GeomFromText('POLYGON ((-1.1 -1.1, -1.1 1.1, 1.1 1.1, 1.1 -1.1, -1.1 -1.1))', 4326))");
        run( "INSERT INTO \"ftjoin\" VALUES (2, 'two', ST_GeomFromText('POLYGON ((-10 -10, -10 10, 10 10, 10 -10, -10 -10))', 4326))");
    }

    @Override
    protected void dropJoinTable() throws Exception {
        run( "DROP TABLE \"ftjoin\"" );
        run( "DELETE FROM geometry_columns WHERE f_table_name = 'ftjoin'");
    }

}
