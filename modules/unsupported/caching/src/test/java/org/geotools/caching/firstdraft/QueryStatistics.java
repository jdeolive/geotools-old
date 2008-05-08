/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.caching.firstdraft;


/**
 * @author crousson
 *
 */
class QueryStatistics {
    private int numberOfFeatures;
    private long executionTime;

    /**
     * @return  the executionTime
     * @uml.property  name="executionTime"
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * @param executionTime  the executionTime to set
     * @uml.property  name="executionTime"
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * @return  the numberOfFeatures
     * @uml.property  name="numberOfFeatures"
     */
    public int getNumberOfFeatures() {
        return numberOfFeatures;
    }

    /**
     * @param numberOfFeatures  the numberOfFeatures to set
     * @uml.property  name="numberOfFeatures"
     */
    public void setNumberOfFeatures(int numberOfFeatures) {
        this.numberOfFeatures = numberOfFeatures;
    }
}
