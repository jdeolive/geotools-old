/*
 * Created on 19-feb-2004
 *
 */
package org.geotools.gui.swing.sldeditor.util;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author wolf
 */
public class WindowMinSizer {
    private Window window;
    private Dimension newDimension = new Dimension();

    public WindowMinSizer(Window window) {
        this.window = window;
        window.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                handleWindowResize();
            }
        });
    }

    private void handleWindowResize() {
        Dimension minimum = window.getMinimumSize();
        Dimension actual = window.getSize();
        newDimension.setSize(actual);
        if(actual.width < minimum.width) {
            newDimension.width = minimum.width;
        } 
        if(actual.height < minimum.height) {
            newDimension.height = minimum.height;
        }
        
        if(!newDimension.equals(actual))
            window.setSize(newDimension);
    }
}
