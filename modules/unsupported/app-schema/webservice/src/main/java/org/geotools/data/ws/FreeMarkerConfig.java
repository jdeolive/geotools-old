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

package org.geotools.data.ws;

import java.io.File;
import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class FreeMarkerConfig {

    private static Configuration cfg;
    
    public static void createConfiguration(String directoryPath) {
        Configuration cfg = new Configuration();
        // Specify the data source where the template files come from.
        // Here I set a file directory for it:
        try {
           cfg.setDirectoryForTemplateLoading(
                    new File(directoryPath));
       } catch (IOException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
       }
        // Specify how templates will see the data-model. This is an advanced topic...
        // but just use this:
        cfg.setObjectWrapper(new DefaultObjectWrapper()); 
    }
    
    public static Configuration getInstance() {
        return cfg;
    }
    
    private FreeMarkerConfig() {}
}
