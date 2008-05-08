/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2003-2004, Julian J. Ray, All Rights Reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.tiger;

/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author Julian J. Ray
 * @source $URL$
 * @version 1.0
 */
public class TigerSchemaElement {
    /** DOCUMENT ME! */
    protected String attributeName;

    /** DOCUMENT ME! */
    protected String classType;

    /** DOCUMENT ME! */
    protected int startPos;

    /** DOCUMENT ME! */
    protected int endPos;

    /**
     * TigerSchemaElement
     */
    TigerSchemaElement() {
    }

    /**
     * TigerSchemaElement
     *
     * @param name String
     * @param classType String
     * @param startPos int
     * @param endPos int
     */
    TigerSchemaElement(String name, String classType, int startPos, int endPos) {
        this.attributeName = name;
        this.classType = classType;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    /**
     * getAttributeName
     *
     * @return String
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * getClassType
     *
     * @return String
     */
    public String getClassType() {
        return classType;
    }

    /**
     * getStartPos
     *
     * @return int
     */
    public int getStartPos() {
        return startPos;
    }

    /**
     * getEndPos
     *
     * @return int
     */
    public int getEndPos() {
        return endPos;
    }
}
