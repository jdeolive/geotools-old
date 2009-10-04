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

package org.geotools.swing.data;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.Parameter;
import org.geotools.swing.wizard.JWizard;

/**
 * Wizard prompting the user to enter or review connection parameters.
 */
public class JParameterListWizard extends JWizard {
    private static final long serialVersionUID = -3961250234483352643L;

    /**
     * Initial page of user focused options
     */
    private JParameterListPage userPage;

    /**
     * Optional page2 used for advanced options
     */
    private JParameterListPage advancedPage;

    /**
     * Connection parameters; shared with pages for editing
     */
    protected Map<String, Object> connectionParameters;

    /**
     * Quick transition from JFileDataStoreChooser; allowing applications to migrate to connection
     * parameters.
     * 
     * @param extension
     *            Extension used to look up FileDataStoreFactory
     */
    public JParameterListWizard(String title, String description, List<Parameter<?>> contents,
            Map<String, Object> params) {
        super(title);
        this.connectionParameters = params == null ? new HashMap<String, Object>() : params;
        fillInDefaults(contents, this.connectionParameters);

        List<Parameter<?>> userContents = contentsForLevel(contents, "user");

        userPage = new JParameterListPage(title, description, userContents, connectionParameters);
        userPage.setPageIdentifier("userPage");
        registerWizardPanel(userPage);

        List<Parameter<?>> advancedContents = contentsForLevel(contents, "advanced");

        if (advancedContents.size() > 0) {
            advancedPage = new JParameterListPage(title, description, advancedContents,
                    connectionParameters);
            advancedPage.setPageIdentifier("advancedPage");
            advancedPage.setBackPageIdentifier("userPage");
            registerWizardPanel(advancedPage);

            // link from page 1
            userPage.setNextPageIdentifier("advancedPage");
        }
        setCurrentPanel("userPage");
    }

    public JParameterListWizard(String title, String description, List<Parameter<?>> contents) {
        this( title, description, contents, new HashMap<String,Object>() );
    }

    /**
     * Method used to fill in any required "programming" level defaults such as dbtype.
     * 
     * @param format2
     * @param params
     */
    private void fillInDefaults(List<Parameter<?>> contents,
            Map<String, Object> connectionParameters) {
        if (connectionParameters == null)
            return;
        for (Parameter<?> param : contents) {
            if (param.required && "program".equals(param.getLevel())) {
                if (!connectionParameters.containsKey(param.key)) {
                    connectionParameters.put(param.key, param.sample);
                }
            }
        }
    }

    List<Parameter<?>> contentsForLevel(List<Parameter<?>> contents, String level) {
        List<Parameter<?>> list = new ArrayList<Parameter<?>>();
        if (level == null) {
            level = "user";
        }
        if (contents != null) {
            for (Parameter<?> param : contents) {
                if (level != null) {
                    String check = param.metadata == null ? "user" : (String) param.metadata
                            .get(Parameter.LEVEL);
                    if (check == null) {
                        check = "user";
                    }
                    if (level.equals(check)) {
                        // we are good this is the one we want
                        list.add(param);
                    }
                }
            }
        }
        return list;
    }

    private int countParamsAtLevel(List<Parameter<?>> contents, String level) {
        if (contents == null)
            return 0;
        int count = 0;
        if (level == null) {
            return contents.size();
        }
        for (Parameter<?> param : contents) {
            String check = param.getLevel();
            if (level.equals(check)) {
                count++;
            }
        }
        return count;
    }

    public Map<String, Object> getConnectionParameters() {
        return connectionParameters;
    }

    /**
     * Helper method to check if for "url" parameter.
     * 
     * @return url parameters as a File, or null if not applicable
     */
    public File getFile() {
        URL url = (URL) connectionParameters.get("url");
        return DataUtilities.urlToFile(url);
    }

}
