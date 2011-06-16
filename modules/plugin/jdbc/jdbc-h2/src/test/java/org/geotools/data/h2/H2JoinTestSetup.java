package org.geotools.data.h2;

import org.geotools.jdbc.JDBCJoinTestSetup;

public class H2JoinTestSetup extends JDBCJoinTestSetup {

    protected H2JoinTestSetup() {
        super(new H2TestSetup());
    }

    @Override
    protected void createJoinTable() throws Exception {
        run( "CREATE TABLE \"geotools\".\"ftjoin\" ( \"id\" int, " + "\"name\" VARCHAR, \"geom\" GEOMETRY)" );
        run("CALL AddGeometryColumn('geotools', 'ftjoin', 'geom', 4326, 'GEOMETRY', 2)");
        
        run( "INSERT INTO \"geotools\".\"ftjoin\" VALUES (0, 'zero', ST_GeomFromText('POLYGON ((-0.1 -0.1, -0.1 0.1, 0.1 0.1, 0.1 -0.1, -0.1 -0.1))', 4326))");
        run( "INSERT INTO \"geotools\".\"ftjoin\" VALUES (1, 'one', ST_GeomFromText('POLYGON ((-1.1 -1.1, -1.1 1.1, 1.1 1.1, 1.1 -1.1, -1.1 -1.1))', 4326))");
        run( "INSERT INTO \"geotools\".\"ftjoin\" VALUES (2, 'two', ST_GeomFromText('POLYGON ((-10 -10, -10 10, 10 10, 10 -10, -10 -10))', 4326))");
    }

    @Override
    protected void dropJoinTable() throws Exception {
        run( "DROP TABLE \"geotools\".\"ftjoin\"" );
    }

}
