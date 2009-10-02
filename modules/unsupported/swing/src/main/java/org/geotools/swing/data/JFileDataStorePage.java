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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.swing.wizard.JPage;
import org.geotools.swing.wizard.ParamField;

/**
 * A wizard page that will prompt the user for a file of the supplied format ask for any additional
 * information.
 */
public class JFileDataStorePage extends JPage {

    protected FileDataStoreFactorySpi format;

    protected File file;

    private JTextField field;

    private JButton browse;

    /**
     * @param id
     *            Page identifer; example JPage.DEFAULT for the initial page
     * @param file
     *            Initial file provided by user; may be null
     * @param format
     *            FileFormat we are working with
     */
    public JFileDataStorePage(File file, FileDataStoreFactorySpi format) {
        this.file = file;
        this.format = format;
    }

    @Override
    public JPanel createPanel() {
        final JPanel page = super.createPanel();
        page.setLayout(new MigLayout());
        
        JLabel title = new JLabel( format.getDisplayName() );
        Font titleFont = new Font("Arial", Font.BOLD, 14 );
        title.setFont(titleFont);
        page.add( title, "span");
        JLabel description = new JLabel( format.getDescription() );
        page.add( description, "span" );
        
//        JTextArea description = new JTextArea( format.getDescription() );
//        description.setEditable(false);
//        description.setLineWrap(true);
//        description.setBackground( page.getBackground() );
        
        page.add( description, "grow, span" );
        
        page.add(new JLabel("File:"));
        page.add(field = new JTextField(32), "width 200::700, growx");
        field.setActionCommand(FINISH); // pressing return will Finish wizard

        page.add(browse = new JButton("Browse"), "wrap" );
        browse.addActionListener( new ActionListener() {            
            public void actionPerformed(ActionEvent e) {
                JFileDataStoreChooser dialog = new JFileDataStoreChooser( format );
                dialog.setSelectedFile(JFileDataStorePage.this.file);
                
                int returnVal = dialog.showOpenDialog(page);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    file = dialog.getSelectedFile();
                    field.setText(file.getPath());
                    getJWizard().getController().syncButtonsToPage();
                }
            }
        });
        
        for( Param param : format.getParametersInfo() ){
            if( param.key == "url" ){
                continue;
            }
            JLabel label = new JLabel( param.title.toString() );
            page.add( label );
            
            ParamField field = ParamField.create( param );
            JComponent component = field.doLayout();
            page.add( component, "span, wrap");
            
            if( param.description != null ){
                JLabel info = new JLabel( param.description.toString() );
                page.add( info, "skip, span, wrap" );
            }
        }
        return page;
    }

    @Override
    public void preDisplayPanel() {
        field.setText(file.getPath());
        field.addKeyListener(getJWizard().getController());
    }

    @Override
    public void preClosePanel() {
        field.removeKeyListener(getJWizard().getController());
    }

    @Override
    public boolean isValid() {
        try {
            file = new File(field.getText());
            URL url = file.toURI().toURL();
            return format.canProcess(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
