package org.geotools.data.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.geotools.jdbc.JDBCLobTestSetup;

public class OracleLobTestSetup extends JDBCLobTestSetup {

    protected OracleLobTestSetup() {
        super(new OracleTestSetup());
    }

    @Override
    protected void createLobTable() throws Exception {
        run("CREATE TABLE testlob (fid int, blob_field BLOB, clob_field CLOB, PRIMARY KEY (fid) )");
        run("CREATE SEQUENCE testlob_pkey_seq START WITH 0 MINVALUE 0");
        run("CREATE TRIGGER testlob_pkey_trigger " 
                + "BEFORE INSERT ON testlob " 
                + "FOR EACH ROW "
                + "BEGIN " 
                + "SELECT testlob_pkey_seq.nextval INTO :new.fid FROM dual; " + "END;");

        // insert data. We need to use prepared statements in order to insert blobs
        Connection conn = getConnection();
        PreparedStatement ps = null;
        try {
            String sql = "INSERT INTO testlob(blob_field, clob_field) VALUES(?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setBytes(1, new byte[] {1,2,3,4,5});
            ps.setString(2, "small clob");
            ps.execute();
        } finally {
            if(ps != null) ps.close();
            conn.close();
        }
    }

    @Override
    protected void dropLobTable() throws Exception {
        runSafe("DROP SEQUENCE testlob_pkey_seq");
        runSafe("DROP TABLE testlob");
    }

}
