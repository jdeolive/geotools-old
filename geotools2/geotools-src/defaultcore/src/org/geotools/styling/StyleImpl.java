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
 * @version $Id: StyleImpl.java,v 1.16 2003/08/10 08:39:28 seangeo Exp $
 * @author James Macgill, CCG
 */
public class StyleImpl implements org.geotools.styling.Style, Cloneable {
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
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
    
    /** Clones the Style.  Creates deep copy clone of the style.
     * 
     * @return the Clone of the style.
     * @see org.geotools.styling.Style#clone()
     */
    public Object clone() {
        Style clone;
        try {
            clone = (Style) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // this should never happen since we implement Cloneable
        }
        
        FeatureTypeStyle[] ftsArray = new FeatureTypeStyle[featureTypeStyleList.size()];
        
        for (int i = 0; i < ftsArray.length; i++) {
            FeatureTypeStyle fts = (FeatureTypeStyle) featureTypeStyleList.get(i);
            ftsArray[i] = (FeatureTypeStyle) fts.clone();
        }
        
        clone.setFeatureTypeStyles(ftsArray);
        
        return clone;
    }

    /** Overrides hashcode.
     *  
     *  @return The hash code.
     */
    public int hashCode() {
        final int PRIME = 1000003;
        int result = 0;
        if (featureTypeStyleList != null) {
            result = PRIME * result + featureTypeStyleList.hashCode();
        }
        if (abstractText != null) {
            result = PRIME * result + abstractText.hashCode();
        }
        if (name != null) {
            result = PRIME * result + name.hashCode();
        }
        if (title != null) {
            result = PRIME * result + title.hashCode();
        }
        result = PRIME * result + (defaultB ? 1 : 0);

        return result;
    }

    /** Compares this Style with another.
     * 
     *  <p>Two StyleImpl are equal if they have the same
     *  properties and the same list of FeatureTypeStyles.
     * 
     *  @param oth The object to compare with this for equality.
     *  @return True if this and oth are equal.
     */
    public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth == null) {
            return false;
        }

        if (oth.getClass() != getClass()) {
            return false;
        }

        StyleImpl other = (StyleImpl) oth;
        
        if (this.abstractText == null) {
            if (other.abstractText != null) {
                return false;
            }
        } else {
            if (!this.abstractText.equals(other.abstractText)) {
                return false;
            }
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else {
            if (!this.name.equals(other.name)) {
                return false;
            }
        }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        } else {
            if (!this.title.equals(other.title)) {
                return false;
            }
        }
        if (this.defaultB != other.defaultB) {
            return false;
        }
        if (this.featureTypeStyleList == null) {
            if (other.featureTypeStyleList != null) {
                return false;
            }
        } else {
            if (!this.featureTypeStyleList.equals(other.featureTypeStyleList)) {
                return false;
            }
        }
        
        return true;
    }

}