/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 * Created on Jan 14, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.dto;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * TestSuiteConfig purpose.
 * 
 * <p>
 * Description of TestSuiteConfig ...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: TestSuiteDTO.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class TestSuiteDTO {
    /** the test suite name */
    private String name;

    /** the test suite description */
    private String description;

    /** the list of tests */
    private Map tests;

    /**
     * TestSuiteConfig constructor.
     * 
     * <p>
     * Does nothing
     * </p>
     */
    public TestSuiteDTO() {
    }

    /**
     * TestSuiteConfig constructor.
     * 
     * <p>
     * Creates a copy of the TestSuiteConfig passed in.
     * </p>
     *
     * @param ts The Test Suite to copy
     */
    public TestSuiteDTO(TestSuiteDTO ts) {
        name = ts.getName();
        description = ts.getDescription();
        tests = new HashMap();

        Iterator i = ts.getTests().keySet().iterator();

        while (i.hasNext()) {
            TestDTO t = (TestDTO) ts.getTests().get(i.next());
            tests.put(t.getName(),new TestDTO(t));
        }
    }

    /**
     * Implementation of clone.
     *
     * @return An instance of TestSuiteConfig.
     *
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        return new TestSuiteDTO(this);
    }

    public int hashCode() {
        int r = 1;

        if (tests != null) {
            r *= tests.hashCode();
        }

        if (name != null) {
            r *= name.hashCode();
        }

        if (description != null) {
            r *= description.hashCode();
        }

        return r;
    }

    /**
     * Implementation of equals.
     *
     * @param obj An object to compare for equality.
     *
     * @return true when the objects have the same data in the same order.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof TestSuiteDTO)) {
            return false;
        }

        boolean r = true;
        TestSuiteDTO ts = (TestSuiteDTO) obj;

        if (name != null) {
            r = r && (name.equals(ts.getName()));
        }

        if (description != null) {
            r = r && (description.equals(ts.getDescription()));
        }

        if (tests == null) {
            if (ts.getTests() != null) {
                return false;
            }
        } else {
            if (ts.getTests() != null) {
                r = r && tests.equals(ts.getTests());
            } else {
                return false;
            }
        }

        return r;
    }

    /**
     * Access description property.
     *
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set description to description.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Access name property.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set name to name.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Access tests property.
     *
     * @return Returns the tests.
     */
    public Map getTests() {
        return tests;
    }

    /**
     * Set tests to tests.
     *
     * @param tests The tests to set.
     */
    public void setTests(Map tests) {
        this.tests = tests;
    }
}
