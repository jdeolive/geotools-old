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

import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.process.idl.IDLBaseTestCase;
import org.geotools.process.idl.IDLExecutionException;
import org.geotools.process.idl.PrintingProgressListener;
import org.geotools.process.idl.impl.BaseIDLObjectWrapper;
import org.junit.Assert;

import com.idl.javaidl.JIDLArray;
import com.idl.javaidl.JIDLConst;
import com.idl.javaidl.JIDLString;

public class ArrayTest extends IDLBaseTestCase {

    /**
     * Test array Java-IDL bridging.
     */
    @org.junit.Test
    public void testArray() throws IDLExecutionException {
        if (!isIDLAvailable())
            return;
        final ArrayTestWrapper testArray = new ArrayTestWrapper("process1");
        testArray.createObject();
        testArray.addIDLNotifyListener(testArray);
        testArray.setProgressListener(new PrintingProgressListener());

        final Map<String, Object> params = new LinkedHashMap<String, Object>(2);
        params.put("strings", new String[] { "one", "two", "three" });
        params.put("values", new int[] { 4, 5, 6 });
        final JIDLString result = testArray.execute(params);
        final String sResult = result.stringValue();
        final String val[] = sResult.split(":");
        
        Assert.assertEquals(val.length, 2);
        Assert.assertEquals(val[val.length-1].trim(), Integer.toString(15));
    }
    
    class ArrayTestWrapper extends BaseIDLObjectWrapper{

        /**
         * 
         */
        private static final long serialVersionUID = 0L;
        private static final String IDL_CLASS = "ta";
       
       // Constructor
       public ArrayTestWrapper(final String processName) {
          super(IDL_CLASS, processName);
       }

       public JIDLString _IDL__TESTARRAY(
               JIDLArray STRINGS,
               JIDLArray VALUES
               )
            {
               final int ARGC = 2;
               Object[] argv = new Object[ARGC];
               int[] argp = new int[ARGC];

               Object result = null;
               final int FLAGS = 0;

               // Parameter assignments
               argv[0] = STRINGS;
               argp[0] = JIDLConst.PARMFLAG_CONST|JIDLConst.PARMFLAG_CONVMAJORITY;
               argv[1] = VALUES;
               argp[1] = JIDLConst.PARMFLAG_CONST|JIDLConst.PARMFLAG_CONVMAJORITY;

               result = super.callFunction("_IDL__TESTARRAY",
                  ARGC, argv, argp, FLAGS);

               return (JIDLString)result;
            }
    }
}
