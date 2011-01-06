/*$************************************************************************************************
 **
 ** $Id: AssociationDescriptor.java 1176 2008-03-26 16:15:59Z avcuster $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/feature/type/AssociationDescriptor.java $
 **
 ** Copyright (C) 2004-2007 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.feature.type;


/**
 * Describes an instance of an Association.
 *
 * @author Jody Garnett, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 */
public interface AssociationDescriptor extends PropertyDescriptor {

    /**
     * Override of {@link PropertyDescriptor#getType()} which type narrows to
     * {@link AssocicationType}.
     *
     *  @see PropertyDescriptor#getType()
     */
    AssociationType getType();

}
