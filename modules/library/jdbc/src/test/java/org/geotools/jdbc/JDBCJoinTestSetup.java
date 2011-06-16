package org.geotools.jdbc;

import java.sql.SQLException;

public abstract class JDBCJoinTestSetup extends JDBCDelegatingTestSetup {

    protected JDBCJoinTestSetup(JDBCTestSetup delegate) {
        super(delegate);
    }

    protected final void setUpData() throws Exception {
        delegate.setUpData();
        
        //kill all the data
        try {
            dropJoinTable();
        } catch (SQLException e) {
        }

        //create all the data
        createJoinTable();
    }

    /**
     * Creates a table with the following schema:
     * <p>
     * ftjoin( id:Integer; name:String; geom:POLYGON )
     * </p>
     * <p>
     * The table should be populated with the following data:
     * 0 | 'zero' | POLYGON ((-0.1 -0.1, -0.1 0.1, 0.1 0.1, 0.1 -0.1, -0.1 -0.1))
     * 1 | 'one' | POLYGON ((-1 -1, -1 1, 1 1, 1 -1, -1 -1))
     * 2 | 'two' | POLYGON ((-10 -10, -10 10, 10 10, 10 -10, -10 -10))
     * </p>
     */
    protected abstract void createJoinTable() throws Exception;

    protected abstract void dropJoinTable() throws Exception;

}
