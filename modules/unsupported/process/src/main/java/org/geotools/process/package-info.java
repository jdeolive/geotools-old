/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
/**
 * Define a Process API used to wrap up processes for reuse.
 * <p>
 * This API is made available at three levels:
 * <ul>
 * <li>Process an interface similar in spirit to Runnable used to make spatial data manipulation, transformation, etc.. available to client code
 * <li>ProcessFinder used for discovery and creation of Process implementations
 * <li>ProcessFactory used to advertise additional implementations to the framework (via the FactorySPI plug-in system)
 * </ul>
 */
package org.geotools.process;