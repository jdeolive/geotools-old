/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;

// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


/**
 * @version $Id: StyleImpl.java,v 1.12 2003/07/22 16:37:10 ianturton Exp $
 * @author James Macgill, CCG
 */
public class StyleImpl implements org.geotools.styling.Style {
    /**
     * The logger for the default core module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.styling");
    private List featureTypeStyleList = new ArrayList();
    private String abstractText = "";
    private String name = "Default Styler";
    private String title = "Default Styler";
    private boolean defaultB = false;

    /** Creates a new instance of StyleImpl */
    protected StyleImpl() {
    }

    public String getAbstract() {
        return abstractText;
    }

    public FeatureTypeStyle[] getFeatureTypeStyles() {
        FeatureTypeStyle[] ret = new FeatureTypeStyleImpl[] {
            new FeatureTypeStyleImpl()
        };

        if ((featureTypeStyleList != null) 
               && (featureTypeStyleList.size() != 0)) {
            LOGGER.fine("number of fts set " + featureTypeStyleList.size());
            ret = (FeatureTypeStyle[]) featureTypeStyleList.toArray(
                            new FeatureTypeStyle[] {  });
        }

        return ret;
    }

    public void setFeatureTypeStyles(FeatureTypeStyle[] featureTypeStyles) {
        featureTypeStyleList.clear();

        for (int i = 0; i < featureTypeStyles.length; i++) {
            addFeatureTypeStyle(featureTypeStyles[i]);
        }

        LOGGER.fine("StyleImpl added " + featureTypeStyleList.size() + 
                    " feature types");
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDefault() {
        return defaultB;
    }

    public void setAbstract(String abstractStr) {
        abstractText = abstractStr;
    }

    public void setIsDefault(boolean isDefault) {
        defaultB = isDefault;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addFeatureTypeStyle(FeatureTypeStyle type) {
        featureTypeStyleList.add(type);
    }

    /**
     * Convenience method for logging a message with an exception.
     */
    private static void severe(final String method, final String message, 
                               final Exception exception) {
        final LogRecord record = new LogRecord(Level.SEVERE, message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
    }
}