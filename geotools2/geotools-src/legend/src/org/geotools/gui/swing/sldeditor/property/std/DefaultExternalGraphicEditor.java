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
 * ExternalGraphicEditor.java
 *
 * Created on 8 dicembre 2003, 11.52
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.geotools.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.ImageFilter;
import org.geotools.styling.ExternalGraphic;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultExternalGraphicEditor extends ExternalGraphicEditor {
    ExternalGraphic externalGraphic;
    JLabel lblUrl;
    JLabel lblMimeType;
    JTextField txtUrl;
    JComboBox cmbMimeType;
    JButton btnOpen;

    /**
     * Creates a new instance of ExternalGraphicEditor
     */
    public DefaultExternalGraphicEditor() {
        this(null);
    }

    public DefaultExternalGraphicEditor(ExternalGraphic externalGraphic) {
        setLayout(new GridBagLayout());

        lblUrl = new JLabel("Image location");
        txtUrl = new JTextField("12345678901234567890");
        txtUrl.setPreferredSize(txtUrl.getPreferredSize());
        txtUrl.setText("");
        FormUtils.addRowInGBL(this, 0, 0, lblUrl, txtUrl);

        btnOpen = new JButton("...");
        btnOpen.setPreferredSize(FormUtils.getButtonDimension());
        btnOpen.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    openFileDialog();
                }
            });
        FormUtils.addRowInGBL(this, 0, 2, btnOpen);

        lblMimeType = new JLabel("Image type");
        cmbMimeType = new JComboBox(getMimeTypes());
        FormUtils.addRowInGBL(this, 1, 0, lblMimeType, cmbMimeType);
        FormUtils.addFiller(this, 2, 0);

        setExternalGraphic(externalGraphic);
    }

    public void setExternalGraphic(ExternalGraphic externalGraphic) {
        if (externalGraphic == null) {
            txtUrl.setText("");
            cmbMimeType.setSelectedIndex(0);

            return;
        }

        try {
            txtUrl.setText(externalGraphic.getLocation().toString());
            cmbMimeType.setSelectedItem(externalGraphic.getFormat());

            this.externalGraphic = externalGraphic;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Illegal url in external graphic", e);
        }
    }

    public ExternalGraphic getExternalGraphic() {
        externalGraphic.setURI(txtUrl.getText());
        if (cmbMimeType.getSelectedIndex() == -1) {
            externalGraphic.setFormat((String) cmbMimeType.getSelectedItem());
        }

        return this.externalGraphic;
    }

    private void openFileDialog() {
        String[] mimes = ImageIO.getReaderMIMETypes();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new ImageFilter());

        int retval = fileChooser.showOpenDialog(this);

        if (retval == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();

            txtUrl.setText(f.toURI().toString());

            String extension = ImageFilter.getExtension(f);

            String mime = null;
            Iterator it = ImageIO.getImageReadersBySuffix(extension);
            while (it.hasNext()) {
                ImageReader reader = (ImageReader) it.next();
                mime = reader.getOriginatingProvider().getMIMETypes()[0];

                break;
            }

            if (mime != null) {
                cmbMimeType.setSelectedItem(mime);
            } else {
                cmbMimeType.setSelectedIndex(0);
            }
        }
    }

    private String[] getMimeTypes() {
        String[] mimes = ImageIO.getReaderMIMETypes();
        String[] types = new String[mimes.length + 1];
        types[0] = "<unknown type>";
        System.arraycopy(mimes, 0, types, 1, mimes.length);

        return types;
    }

    public static void main(String[] args) {
        FormUtils.show(new DefaultExternalGraphicEditor());
    }
}
