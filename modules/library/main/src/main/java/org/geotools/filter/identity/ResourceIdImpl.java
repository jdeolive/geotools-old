/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.identity;

import java.util.Date;

import org.geotools.util.Utilities;
import org.opengis.filter.identity.ResourceId;
import org.opengis.filter.identity.Version;

/**
 * Implementation of {@link ResourceId}
 * <p>
 * This class is mutable under one condition only; during a commit a datastore can update the
 * internal fid to reflect the real identify assigned by the database or wfs.
 * <p>
 * 
 * @author Justin Deoliveira, The Open Planning Project
 * 
 * 
 * 
 * @source $URL$
 */
public class ResourceIdImpl extends FeatureIdImpl implements ResourceId {

    private String previousRid;

    private Version version;

    private Date startTime;

    private Date endTime;

    private final String featureVersion;

    /**
     * @param fid
     *            the feature id, non {@code null}
     * @param featureVersion
     *            the feature version, may be {@code null}
     */
    public ResourceIdImpl(final String fid, final String featureVersion) {
        super(fid);
        this.featureVersion = featureVersion;
    }

    @Override
    public String getID() {
        return super.getID();
    }

    @Override
    public String getFeatureVersion() {
        return featureVersion;
    }

    @Override
    public String getRid() {
        return featureVersion == null ? getID() : new StringBuilder(getID())
                .append(VERSION_SEPARATOR).append(featureVersion).toString();
    }

    public void setRid(String rid) {
        setID(rid);
    }

    @Override
    public String getPreviousRid() {
        return previousRid;
    }

    public void setPreviousRid(final String previousRid) {
        this.previousRid = previousRid;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ResourceId)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final ResourceId o = (ResourceId) obj;
        return Utilities.equals(featureVersion, o.getFeatureVersion())
                && Utilities.equals(previousRid, o.getPreviousRid())
                && Utilities.equals(version, o.getVersion())
                && Utilities.equals(startTime, o.getStartTime())
                && Utilities.equals(endTime, o.getEndTime());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = Utilities.hash(previousRid, hash);
        hash = Utilities.hash(version, hash);
        hash = Utilities.hash(startTime, hash);
        hash = Utilities.hash(endTime, hash);
        return hash;
    }

}
