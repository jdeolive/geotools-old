/*
 * SelectedChannelType.java
 *
 * Created on 08 November 2002, 12:48
 */

package org.geotools.styling;

import org.geotools.filter.Expression;

/** A class to hold Channel information for use in ChannelSelction objects.
 *<pre>
 * &lt;xs:complexType name="SelectedChannelType"&gt;
 *   &lt;xs:sequence&gt;
 *     &lt;xs:element ref="sld:SourceChannelName"/&gt;
 *     &lt;xs:element ref="sld:ContrastEnhancement" minOccurs="0"/&gt;
 *   &lt;/xs:sequence&gt;
 * &lt;/xs:complexType&gt;
 * &lt;xs:element name="SourceChannelName" type="xs:string"/&gt;
 *</pre>
 * @author  iant
 */
public interface SelectedChannelType {
    
    public void setChannelName(String name);
    public String getChannelName();
    
    public void setContrastEnhancement(Expression enhancement);
    public Expression getContrastEnhancement();
}
