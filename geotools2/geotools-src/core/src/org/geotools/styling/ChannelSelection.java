/*
 * ChanelSelection.java
 *
 * Created on 08 November 2002, 12:46
 */

package org.geotools.styling;

/**
 * The ChannelSelection element specifies the false-color channel selection for a multi-spectral raster source 
 * (such as a multi-band satellite-imagery source).  It is defined as:
 * <PRE>
 * &lt;xs:element name="ChannelSelection"&gt;
 * &lt;xs:complexType&gt;
 *     &lt;xs:choice&gt;
 *       &lt;xs:sequence&gt;
 *         &lt;xs:element ref="sld:RedChannel"/&gt;
 *         &lt;xs:element ref="sld:GreenChannel"/&gt;
 *         &lt;xs:element ref="sld:BlueChannel"/&gt;
 *       &lt;/xs:sequence&gt;
 *       &lt;xs:element ref="sld:GrayChannel"/&gt;
 *     &lt;/xs:choice&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="RedChannel" type="sld:SelectedChannelType"/&gt;
 * &lt;xs:element name="GreenChannel" type="sld:SelectedChannelType"/&gt;
 * &lt;xs:element name="BlueChannel" type="sld:SelectedChannelType"/&gt;
 * &lt;xs:element name="GrayChannel" type="sld:SelectedChannelType"/&gt;
 * </PRE>
 * Either a channel may be selected to display in each of red, green, and blue, or a single channel
 * may be selected to display in grayscale.  (The spelling “gray” is used since it seems to be more
 * common on the Web than “grey” by a ratio of about 3:1.)
 * Contrast enhancement may be applied to each channel in isolation.  Channels are identified by a system
 * and data-dependent character identifier.  Commonly, channels will be labelled as “1”, “2”, etc.
 *
 * @author  iant
 */
public interface ChannelSelection {
    public void setRGBChannels(SelectedChannelType red, SelectedChannelType green, SelectedChannelType blue);
    public void setRGBChannels(SelectedChannelType[] channels);
    
    public SelectedChannelType[] getRGBChannels();
    
    public void setGrayChannel(SelectedChannelType gray);
    public SelectedChannelType getGrayChannel();
    
    public void setSelectedChannels(SelectedChannelType[] channels);
    public SelectedChannelType[] getSelectedChannels();
        
}
