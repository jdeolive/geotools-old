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
/*
 * Created on 19-feb-2004
 *
 */
package org.geotools.gui.swing.sldeditor.symbolizer;

import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.styling.Symbolizer;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import javax.swing.JDialog;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public abstract class SymbolizerChooserDialog extends JDialog implements SLDEditor {
    public static final int POINT = 1;
    public static final int POLYGON = 2;
    public static final int LINE = 3;
    public static final int TEXT = 4;
    public static final int RASTER = 5;

    /**
     * DOCUMENT ME!
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog() throws HeadlessException {
        super();
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Dialog owner) throws HeadlessException {
        super(owner);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param modal
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Dialog owner, boolean modal)
        throws HeadlessException {
        super(owner, modal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Frame owner) throws HeadlessException {
        super(owner);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param modal
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Frame owner, boolean modal)
        throws HeadlessException {
        super(owner, modal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Dialog owner, String title)
        throws HeadlessException {
        super(owner, title);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     * @param modal
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Dialog owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Frame owner, String title)
        throws HeadlessException {
        super(owner, title);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     * @param modal
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Frame owner, String title, boolean modal)
        throws HeadlessException {
        super(owner, title, modal);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     * @param modal
     * @param gc
     *
     * @throws HeadlessException
     */
    public SymbolizerChooserDialog(Dialog owner, String title, boolean modal,
        GraphicsConfiguration gc) throws HeadlessException {
        super(owner, title, modal, gc);
    }

    /**
     * DOCUMENT ME!
     *
     * @param owner
     * @param title
     * @param modal
     * @param gc
     */
    public SymbolizerChooserDialog(Frame owner, String title, boolean modal,
        GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
    }

    public abstract int getSelectionCode();

    public Symbolizer getSelectedSymbolizer() {
        Symbolizer s = null;

        switch (getSelectionCode()) {
        case SymbolizerChooserDialog.POINT:
            s = styleBuilder.createPointSymbolizer();

            break;

        case SymbolizerChooserDialog.LINE:
            s = styleBuilder.createLineSymbolizer();

            break;

        case SymbolizerChooserDialog.POLYGON:
            s = styleBuilder.createPolygonSymbolizer();

            break;
            
        case SymbolizerChooserDialog.TEXT:
            s = styleBuilder.createTextSymbolizer();
        
            break;
            
        case SymbolizerChooserDialog.RASTER:
            s = styleBuilder.createRasterSymbolizer(); 

        default:
            throw new RuntimeException("This should not happen!!!");
        }

        return s;
    }

    public abstract boolean exitOk();
}
