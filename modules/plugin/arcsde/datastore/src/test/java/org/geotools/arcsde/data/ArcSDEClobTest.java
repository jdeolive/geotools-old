package org.geotools.arcsde.data;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.data.versioning.ArcSdeVersionHandler;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.ISession;
import org.geotools.arcsde.pool.SessionPool;
import org.geotools.arcsde.pool.SessionPoolFactory;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Id;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeTable;

public class ArcSDEClobTest {
	private static ClobTestData testData;
	private String temp_table = "clob_test";
	private String[] columnNames = {"IntegerField", "ClobField" };

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		testData = new ClobTestData();
		testData.setUp();
        final boolean insertTestData = true;
        testData.createTempTable(insertTestData);
	}

	@AfterClass
	public static void finalTearDown() {
        boolean cleanTestTable = false;
		boolean cleanPool = true;
		testData.tearDown(cleanTestTable, cleanPool);
	}


	/**
	 * loads {@code test-data/testparams.properties} into a Properties object, wich is used to
	 * obtain test tables names and is used as parameter to find the DataStore
	 */
	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testRead() throws Exception { 
		ISession session = null;
		try {
			ArcSDEDataStore dstore = testData.getDataStore();
			session = dstore.getSession(Transaction.AUTO_COMMIT);
			// TODO: This is crap.  If data can't be loaded, add another config for the existing table
			String typeName = testData.getTempTableName(session);
			SimpleFeatureType ftype = dstore.getSchema(typeName);
			// The row id column is not returned, but the geometry column is (x+1-1=x) 
			assertEquals("Verify attribute count.", columnNames.length, ftype.getAttributeCount());
			ArcSDEQuery query = ArcSDEQuery.createQuery(session, ftype, Query.ALL, FIDReader.NULL_READER,
					ArcSdeVersionHandler.NONVERSIONED_HANDLER);
			query.execute();
			SdeRow row = query.fetch(); 
			assertNotNull("Verify first result is returned.", row);
			Object longString = row.getObject(0);
			assertNotNull("Verify the non-nullity of first CLOB.", longString);
			assertEquals("Verify stringiness.", longString.getClass(), String.class);
			row = query.fetch();
			longString = row.getObject(0);
			assertNotNull("Verify the non-nullity of second CLOB.", longString);
			query.close(); 
		} finally {
			if(session != null) {
				session.dispose();
			}
		}
	}


	private String getTempTableName(ISession session) throws IOException {
		String tempTableName;
		String dbName = session.getDatabaseName();
		String user = session.getUser();
		StringBuffer sb = new StringBuffer();
		if (dbName != null && dbName.length() > 0) {
			sb.append(dbName).append(".");
		}
		if (user != null && user.length() > 0) {
			sb.append(user).append(".");
		}
		sb.append(this.temp_table);
		tempTableName = sb.toString().toUpperCase();
		return tempTableName;
	}

}
