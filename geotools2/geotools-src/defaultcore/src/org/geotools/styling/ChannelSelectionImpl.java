/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.styling;

/**
 * DOCUMENT ME!
 *
 * @author iant
 */
public class ChannelSelectionImpl implements ChannelSelection {
    SelectedChannelType gray;
    SelectedChannelType red;
    SelectedChannelType blue;
    SelectedChannelType green;

    public SelectedChannelType getGrayChannel() {
        return gray;
    }

    public SelectedChannelType[] getRGBChannels() {
        return new SelectedChannelType[] { red, green, blue };
    }

    public SelectedChannelType[] getSelectedChannels() {
        if (gray == null) {
            return new SelectedChannelType[] { red, green, blue };
        } else {
            return new SelectedChannelType[] { gray };
        }
    }

    public void setGrayChannel(SelectedChannelType gray) {
        this.gray = gray;
    }

    public void setRGBChannels(SelectedChannelType[] channels) {
        if (channels.length != 3) {
            throw new IllegalArgumentException(
                "Three channels are required in setRGBChannels, got " +
                channels.length);
        }

        red = channels[0];
        green = channels[1];
        blue = channels[2];
    }

    public void setRGBChannels(SelectedChannelType red,
        SelectedChannelType green, SelectedChannelType blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setSelectedChannels(SelectedChannelType[] channels) {
        if (channels.length == 1) {
            gray = channels[0];
        } else if (channels.length == 3) {
            red = channels[0];
            green = channels[1];
            blue = channels[2];
        } else {
            throw new IllegalArgumentException(
                "Wrong number of elements in setSelectedChannels, expected 1 or 3, got " +
                channels.length);
        }
    }
}
