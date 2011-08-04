package org.geotools.filter.v2_0.bindings;

import static org.opengis.filter.identity.VersionAction.ALL;
import static org.opengis.filter.identity.VersionAction.FIRST;
import static org.opengis.filter.identity.VersionAction.LAST;
import static org.opengis.filter.identity.VersionAction.NEXT;
import static org.opengis.filter.identity.VersionAction.PREVIOUS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v2_0.FESTestSupport;
import org.geotools.xml.impl.DatatypeConverterImpl;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;

public class ResourceIdTypeBindingTest extends FESTestSupport {

    public void testParse() throws Exception {
        String xml = "<fes:Filter "
                + "   xmlns:fes='http://www.opengis.net/fes/2.0' "
                + "   xmlns:gml='http://www.opengis.net/gml/3.2' "
                + "   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
                + "   xsi:schemaLocation='http://www.opengis.net/fes/2.0 http://schemas.opengis.net/filter/2.0/filterAll.xsd"
                + " http://www.opengis.net/gml/3.2 http://schemas.opengis.net/gml/3.2.1/gml.xsd'>"
                + "   <fes:ResourceId rid=\"rid1@abc\" previousRid=\"previous1\" version=\"FIRST\"/> "
                + "   <fes:ResourceId rid=\"rid2\" version=\"LAST\"/> "
                + "   <fes:ResourceId rid=\"rid3\" version=\"PREVIOUS\"/> "
                + "   <fes:ResourceId rid=\"rid4\" version=\"NEXT\"/> "
                + "   <fes:ResourceId rid=\"rid5\" version=\"ALL\"/> "
                + "   <fes:ResourceId rid=\"rid6\" previousRid=\"previous2\" version=\"4\" startDate=\"1977-01-17T01:05:40Z\" endDate=\"2011-07-29T23:49:40Z\" /> "
                + "   <fes:ResourceId rid=\"rid7@123\" version=\"1977-01-17T01:05:40Z\"/> "
                + "</fes:Filter>";

        buildDocument(xml);

        Id filter = (Id) parse();
        assertNotNull(filter);
        assertEquals(7, filter.getIdentifiers().size());
        List<ResourceId> ids = new ArrayList<ResourceId>(7);
        for (Identifier id : filter.getIdentifiers()) {
            assertTrue(id instanceof ResourceId);
            ids.add((ResourceId) id);
        }
        Collections.sort(ids, new Comparator<ResourceId>() {
            @Override
            public int compare(ResourceId o1, ResourceId o2) {
                return o1.getRid().compareTo(o2.getRid());
            }
        });

        final DatatypeConverterImpl dateParser = DatatypeConverterImpl.getInstance();
        final Date date1 = dateParser.parseDateTime("1977-01-17T01:05:40Z").getTime();
        final Date date2 = dateParser.parseDateTime("2011-07-29T23:49:40Z").getTime();
        FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

        assertEquals(ff.resourceId("rid1", "abc", "previous1", new Version(FIRST), null, null),
                ids.get(0));
        assertEquals(ff.resourceId("rid2", null, null, new Version(LAST), null, null), ids.get(1));
        assertEquals(ff.resourceId("rid3", null, null, new Version(PREVIOUS), null, null),
                ids.get(2));
        assertEquals(ff.resourceId("rid4", null, null, new Version(NEXT), null, null), ids.get(3));
        assertEquals(ff.resourceId("rid5", null, null, new Version(ALL), null, null), ids.get(4));
        assertEquals(ff.resourceId("rid6", null, "previous2", new Version(4), date1, date2),
                ids.get(5));
        assertEquals(ff.resourceId("rid7", "123", null, new Version(date1), null, null), ids.get(6));
    }
}
