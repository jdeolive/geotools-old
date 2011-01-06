/*$************************************************************************************************
 **
 ** $Id: FeatureVisitor.java 1275 2008-07-16 05:37:46Z Jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/feature/FeatureVisitor.java $
 **
 ** Copyright (C) 2004-2007 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.feature;

/**
 * FeatureVisitor interface to allow for container optimised traversal.
 * <p>
 * The iterator construct from the Collections api is well understood and
 * loved, but breaks down for working with large GIS data volumes. By using a
 * visitor we allow the implementor of a Feature Collection to make use of
 * additional resources (such as multiple processors or tiled data)
 * concurrently.
 * </p>
 * This interface is most often used for calculations and data
 * transformations and an implementations may intercept known visitors
 * (such as "bounds" or reprojection) and engage an alternate work flow.
 * </p>
 * @author Cory Horner (Refractions Research, Inc)
 */
public interface FeatureVisitor {
    /**
     * Visit the provided feature.
     * <p>
     * Please consult the documentation for the FeatureCollection you are visiting
     * to learn more - the provided feature may be invalid, or read only.
     * @param feature
     */
    void visit(Feature feature);
}