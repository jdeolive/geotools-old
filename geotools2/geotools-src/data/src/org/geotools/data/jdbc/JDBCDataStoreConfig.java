/* $Id: JDBCDataStoreConfig.java,v 1.1 2004/01/08 04:28:18 seangeo Exp $
 * 
 * Created on 8/01/2004
 */
package org.geotools.data.jdbc;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/** Configuration object for JDBCDataStore. 
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: JDBCDataStoreConfig.java,v 1.1 2004/01/08 04:28:18 seangeo Exp $
 * Last Modified: $Date: 2004/01/08 04:28:18 $ 
 */
public class JDBCDataStoreConfig {
    public static final String FID_GEN_INSERT_NULL = "INSERT_NULL";
    public static final String FID_GEN_MANUAL_INC = "MANUAL_INC";
    public static final String DEFAULT_FID_GEN_KEY = "DEFAULT_GEN";
    public static final String DEFAULT_FID_GEN = FID_GEN_INSERT_NULL;
            
    private final String namespace;
    private final String databaseSchemaName;
    protected final Properties fidColumnOverrideMap = new Properties();
    protected final Properties fidGenerationMap = new Properties(); 
    
    public JDBCDataStoreConfig() {
        this(null, null, Collections.EMPTY_MAP, Collections.EMPTY_MAP);        
    }
    
    /**
     * 
     */
    public JDBCDataStoreConfig(String namespace, String databaseSchemaName, 
                               Map fidColumnOverrideMap, Map fidGenerationMap) {
        this.namespace = namespace;
        this.databaseSchemaName = databaseSchemaName;
        this.fidColumnOverrideMap.putAll(fidColumnOverrideMap);
        this.fidGenerationMap.putAll(fidGenerationMap);
    }
    
    public static JDBCDataStoreConfig createWithNameSpaceAndSchemaName(String namespace, String schemaName) {
        return new JDBCDataStoreConfig(namespace, schemaName, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    }
    
    public static JDBCDataStoreConfig createWithSchemaNameAndFIDGenMap(String schemaName, Map fidGenerationMap) {
        return new JDBCDataStoreConfig(null, schemaName, Collections.EMPTY_MAP, fidGenerationMap);
    }

    /**
     * @return Returns the databaseSchemaName.
     */
    public String getDatabaseSchemaName() {
        return databaseSchemaName;
    }

    /**
     * @return Returns the namespace.
     */
    public String getNamespace() {
        return namespace;
    }

    /** Gets the FID Column name for the given feature type name.
     *  
     * @param typeName The name of the feature type.
     * @return The name of the column to use for the FID. If no column has been
     * defined for the type then null is returned.
     */
    public String getFidOverrideColumnFor(String typeName) {
        return (String) fidColumnOverrideMap.get(typeName);
    }
    
    /** Gets the FID Generation Type ID for the given feature type name.
     *  If no generation ID exists for the type name, it will check if a default
     *  ID has been defined, if not default is defined it will use the system default
     *  of INSERT_NULL. 
     * 
     * @param typeName The name of the feature type to get the generation id for.
     * @return The FID generation type id. Could be either FID_GEN_INSERT_NULL,
     * FID_GEN_MANUAL_INC or a data store specific id. 
     */
    public String getFidGenerationIdFor(String typeName) {
        String strategy = fidGenerationMap.getProperty(typeName);
        
        if (strategy == null) {
            strategy = fidGenerationMap.getProperty(DEFAULT_FID_GEN_KEY);
            
            if (strategy == null) {
                strategy = DEFAULT_FID_GEN;
            }
        }
        
        return strategy;
    }
}
