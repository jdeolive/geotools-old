package org.geotools.data.jdbc.fidmapper;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Types;

import junit.framework.TestCase;

public class AutoIncrementFIDMapperTest extends TestCase {

    /**
     * A weak test to make sure that AutoIncrementFIDMapper supports multiple
     * (common) data types for its primary key column.
     */
    public void testGetPKAttributes() throws IOException {
        AutoIncrementFIDMapper mapper = new AutoIncrementFIDMapper("column_1", Types.INTEGER);
        Object[] attr = mapper.getPKAttributes("12345");
        assertEquals(Integer.class, attr[0].getClass());
                
        mapper = new AutoIncrementFIDMapper("column_2", Types.BIGINT);
        attr = mapper.getPKAttributes("1234567");
        assertEquals(BigInteger.class, attr[0].getClass());
        
        mapper = new AutoIncrementFIDMapper("column_3", Types.SMALLINT);
        attr = mapper.getPKAttributes("123");
        assertEquals(Integer.class, attr[0].getClass());
        
        mapper = new AutoIncrementFIDMapper("column_4", Types.TINYINT);
        attr = mapper.getPKAttributes("1");
        assertEquals(Integer.class, attr[0].getClass());
        
        mapper = new AutoIncrementFIDMapper("column_5", Types.VARCHAR);
        attr = mapper.getPKAttributes("UNIQUE_FID");
        assertEquals(String.class, attr[0].getClass());
    }
    
}
