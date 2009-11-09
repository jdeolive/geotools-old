/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.idl.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.process.idl.IDLBaseTestCase;
import org.geotools.process.idl.IDLExecutionException;
import org.geotools.process.idl.impl.BaseIDLObjectWrapper;
import org.geotools.process.idl.impl.IDLFeatureExtractionWrapper;
import org.geotools.test.TestData;

import com.idl.javaidl.JIDLString;

public class IDLWrapperTest extends IDLBaseTestCase {

    @org.junit.Test
    public void testInvocation() throws InterruptedException, FileNotFoundException, IOException, IDLExecutionException {
        if (!isIDLAvailable())
            return;
        final BaseIDLObjectWrapper wrapper = new IDLFeatureExtractionWrapper("test");
        wrapper.createObject();
        final Map<String, Object> values = new LinkedHashMap<String, Object>(1);
        JIDLString result;
        
        //Testing wrapper with right parameters
        final File testData = TestData.file(this, "testin.tif");
        values.put("IMAGEFILE", testData.getAbsolutePath());
        result = wrapper.execute(values);
        wrapper.destroyObject();
    }
}
