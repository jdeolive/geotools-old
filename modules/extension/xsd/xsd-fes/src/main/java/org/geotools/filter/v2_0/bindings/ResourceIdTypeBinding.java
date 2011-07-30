package org.geotools.filter.v2_0.bindings;

import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.geotools.filter.v2_0.FES;
import org.geotools.xml.AbstractComplexBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;

public class ResourceIdTypeBinding extends AbstractComplexBinding {

    FilterFactory factory;

    public ResourceIdTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }

    public Class<?> getType() {
        return ResourceId.class;
    }

    public QName getTarget() {
        return FES.ResourceIdType;
    }

    @Override
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        final String rid = (String) node.getAttributeValue("rid");
        final String previousRid = (String) node.getAttributeValue("previousRid");
        final Version version = (Version) node.getAttributeValue("version");
        final Calendar startTimeAtt = (Calendar) node.getAttributeValue("startDate");
        final Calendar endTimeAtt = (Calendar) node.getAttributeValue("endDate");

        Date startTime = startTimeAtt == null ? null : startTimeAtt.getTime();
        Date endTime = endTimeAtt == null ? null : endTimeAtt.getTime();

        ResourceId resourceId = factory.resourceId(rid, previousRid, version, startTime, endTime);
        return resourceId;
    }

    @Override
    public Object getProperty(Object object, QName name) throws Exception {
        if (object == null) {
            return null;
        }

        final ResourceId rid = (ResourceId) object;
        final String localName = name.getLocalPart();
        if ("rid".equals(localName)) {
            return rid.getID();
        }
        if ("previousRid".equals(localName)) {
            return rid.getPreviousRid();
        }
        if ("version".equals(localName)) {
            return rid.getVersion();
        }
        if ("startDate".equals(localName)) {
            return rid.getStartTime();
        }
        if ("endDate".equals(localName)) {
            return rid.getEndTime();
        }

        return null;
    }
}
