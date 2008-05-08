package org.geotools.filter.pojo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import junit.framework.TestCase;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;

/**
 * Now that we got PojoAccessor lets see if the filters really work.
 *
 * @author Jody Garnett, Refractions Research Inc.
 */
public class PojoFilterTest extends TestCase {
    private FilterFactory ff;

    protected void setUp() throws Exception {
        ff = CommonFactoryFinder.getFilterFactory(null);
        super.setUp();
    }

    public void testDateAccess() {
        Date date = new Date();
        assertEquals( new Integer(date.getHours()), ff.property("hours").evaluate( date ) );
        assertEquals( new Integer(date.getHours()), ff.property("hOuRS").evaluate( date ) );
    }

    public void testURL() throws Exception {
        URL url = new URL(
                "http://www.example.com:88/index.php?foo=bar#anchor");
        
        assertEquals( url.getFile(), ff.property("file").evaluate( url ) );
        
//        assertCanHandleAndEquals(url, url.getFile(), "file");
//        assertCanHandleAndEquals(url, url.getProtocol(), "protocol");
//        assertCanHandleAndEquals(url, url.getHost(), "host");
//        assertCanHandleAndEquals(url, url.getQuery(), "query");
//        assertCanHandleAndEquals(url, url.getAuthority(), "authority");
//        assertCanHandleAndEquals(url, url.getPath(), "path");
//        assertCanHandleAndEquals(url, url.getUserInfo(), "userInfo");
//        assertCanHandleAndEquals(url, url.getRef(), "ref");
    }
}