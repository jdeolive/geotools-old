/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.filter.identity;

import java.util.Date;

import org.opengis.annotation.XmlElement;

/**
 * Resource identifier as per FES 2.0.
 * <p>
 * If an implementation that references this International Standard does not support versioning, any
 * value specified for the attributes {@link #getPreviousRid() previousRid}, {@link #getVersion()
 * version}, {@link #getStartTime() startTime}, and {@link #getEndTime() endTime} shall be ignored
 * and the predicate shall always select the single version that is available.
 * </p>
 */
@XmlElement("FeatureId")
public interface ResourceId extends FeatureId {

    public static final char VERSION_SEPARATOR = '@';

    /**
     * id of the resource that shall be selected by the predicate.
     * <p>
     * Equals to {@link #getID()} if no feature version is provided, or
     * {@code getID() + "@" + getFeatureVersion()} if {@code getFeatureVersion() != null}
     * 
     * <p>
     * If an implementation that references this International Standard supports versioning, the rid
     * shall be a system generated hash containing a logical resource identifier and a version
     * number. The specific details of the hash are implementation dependant and shall be opaque to
     * a client
     * </p>
     * <p>
     * If versioning is not supported, the same value than {@link FeatureId#getID()} shall be
     * returned.
     * </p>
     */
    @XmlElement("rid")
    String getRid();

    /**
     * Version identifier for the feature instance, may be {@code null}
     * 
     * @see #getID()
     * @see #getRid()
     */
    String getFeatureVersion();

    /**
     * previousRid attribute may be used, in implementations that support versioning, to report the
     * previous identifier of a resource.
     */
    @XmlElement("previousRid")
    String getPreviousRid();

    /**
     * Used to navigate versions of a resource if an implementation that references this
     * International Standard supports versioning.
     */
    @XmlElement("version")
    Version getVersion();

    /**
     * The version attribute may then be used to navigate the various versions of a resource.
     * <p>
     * Used to navigate versions of a resource if an implementation that references this
     * International Standard supports versioning.
     * </p>
     */
    @XmlElement("startTime")
    Date getStartTime();

    /**
     * Used to navigate versions of a resource if an implementation that references this
     * International Standard supports versioning.
     */
    @XmlElement("endTime")
    Date getEndTime();

    /**
     * Evaluates the identifer value against the given resource.
     * 
     * @param resource
     *            The resource to be tested.
     * @return {@code true} if a match, otherwise {@code false}.
     */
    @Override
    boolean matches(Object resource);
}
