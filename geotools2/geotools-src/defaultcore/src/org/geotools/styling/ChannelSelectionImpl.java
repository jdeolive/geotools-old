/*
 * ChannelSelectionImpl.java
 *
 * Created on 13 November 2002, 13:50
 */

package org.geotools.styling;

/**
 *
 * @author  iant
 */
public class ChannelSelectionImpl implements ChannelSelection {
    SelectedChannelType gray, red, blue, green;
    /** Creates a new instance of ChannelSelectionImpl */
    public ChannelSelectionImpl() {
    }
    
    public SelectedChannelType getGrayChannel() {
        return gray;
    }
    
    public SelectedChannelType[] getRGBChannels() {
        return new SelectedChannelType[]{red,green,blue};
    }
    
    public SelectedChannelType[] getSelectedChannels() {
        if(gray == null){
            return new SelectedChannelType[]{red,green,blue};
        }else{
            return new SelectedChannelType[]{gray};
        }
    }
    
    public void setGrayChannel(SelectedChannelType gray) {
        this.gray = gray;
    }
    
    public void setRGBChannels(SelectedChannelType[] channels) {
        if(channels.length != 3 ) throw new IllegalArgumentException(
            "Three channels are required in setRGBChannels, got "+ channels.length);
        
        red = channels[0];
        green = channels[1];
        blue = channels[2];
    }
    
    public void setRGBChannels(SelectedChannelType red, SelectedChannelType green, SelectedChannelType blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
    
    public void setSelectedChannels(SelectedChannelType[] channels) {
        if(channels.length == 1){
            gray = channels[0];
        } else if (channels.length == 3){
            red = channels[0];
            green = channels[1];
            blue = channels[2];
        } else {
            throw new IllegalArgumentException(
                "Wrong number of elements in setSelectedChannels, expected 1 or 3, got " + channels.length);
        }
            
    }
    
}
