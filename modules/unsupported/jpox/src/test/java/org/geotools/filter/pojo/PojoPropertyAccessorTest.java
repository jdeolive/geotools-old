package org.geotools.filter.pojo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;

public class PojoPropertyAccessorTest extends TestCase {
	protected PojoPropertyAccessorFactory factory;

    private FilterFactory ff;

    protected PojoPropertyAccessor access;

    protected void setUp() throws Exception {
    	factory = new PojoPropertyAccessorFactory();
        ff = CommonFactoryFinder.getFilterFactory(null);
        super.setUp();
    }

    public void testWithNullAndEmptyArguments() {
        assertNull(access.get(null, null,null));
        assertNull(access.get(null, "test",null));
        assertNull(access.get("test", null,null));
        assertNull(access.get("test", "",null));
        assertNull(access.get("test", " ",null));
        assertNull(access.get("test", " \t\n ",null));
    }

    public void testDateAccess() {
        Date date = new Date();
        assertCanHandleAndEquals(date, new Integer(date.getHours()), "hours");
        assertCanHandleAndEquals(date, new Integer(date.getHours()), "hOuRS");
    }

    public void testURL() {
        try {
            URL url = new URL(
                    "http://www.example.com:88/index.php?foo=bar#anchor");

            assertCanHandleAndEquals(url, url.getFile(), "file");
            assertCanHandleAndEquals(url, url.getProtocol(), "protocol");
            assertCanHandleAndEquals(url, url.getHost(), "host");
            assertCanHandleAndEquals(url, url.getQuery(), "query");
            assertCanHandleAndEquals(url, url.getAuthority(), "authority");
            assertCanHandleAndEquals(url, url.getPath(), "path");
            assertCanHandleAndEquals(url, url.getUserInfo(), "userInfo");
            assertCanHandleAndEquals(url, url.getRef(), "ref");
        } catch (MalformedURLException e) {
            assert false : e;
        }
    }

    private void assertCanHandleAndEquals(Object object, Object result,
            String xpath) {
        //assertTrue(access.canHandle(object, xpath));
        assertEquals(result, access.get(object, xpath,null));
    }

    /**
     * Type fudge for the Java 5 impared.
     * 
     * @param expected
     *            new Integer( expected )
     * @param value
     */
    protected void assertEquals(int expected, Object value) {
        assertEquals(new Integer(expected), value);
    }
}