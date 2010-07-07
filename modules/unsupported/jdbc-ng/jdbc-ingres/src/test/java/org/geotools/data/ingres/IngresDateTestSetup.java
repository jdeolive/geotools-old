package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDateTestSetup;
import org.geotools.jdbc.JDBCTestSetup;

public class IngresDateTestSetup extends JDBCDateTestSetup {


    public IngresDateTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }

    @Override
    protected void createDateTable() throws Exception {
        run( "CREATE TABLE DATES (D ANSIDATE, DT TIMESTAMP, T TIME)");
        
        //_date('1998/05/31:12:00:00AM', 'yyyy/mm/dd:hh:mi:ssam'));
        
        run( "INSERT INTO DATES VALUES (" +
                "DATE '2009-06-28', " +
                "TIMESTAMP '2009-06-28 15:12:41', " +
                "TIME '15:12:41' )");
        
        run( "INSERT INTO DATES VALUES (" +
                "DATE '2009-01-15', " +
                "TIMESTAMP '2009-01-15 13:10:12', " +
                "TIME '13:10:12'  )");
        
        run( "INSERT INTO DATES VALUES (" +
                "DATE '2009-09-29', " +
                "TIMESTAMP '2009-09-29 17:54:23', " +
                "TIME '17:54:23'  )");
    }

    @Override
    protected void dropDateTable() throws Exception {
        runSafe("DROP TABLE DATES");
    }

}