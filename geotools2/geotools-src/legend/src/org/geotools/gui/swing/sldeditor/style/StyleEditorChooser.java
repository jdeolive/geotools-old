/*
 * Created on 21-dic-2003
 *
 */
package org.geotools.gui.swing.sldeditor.style;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.style.full.*;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.styling.Style;

/**
 * Allows editing a simple style based on one featuretype
 * @author wolf
 */
public class StyleEditorChooser extends JPanel implements StyleEditor {
    private JPanel editorPanel;
    JLabel lblType;
    JComboBox cmbType; 
    StyleEditor styleEditor;
    private static final int SIMPLE = 0;
    private static final int FULL = 1;
    String[] types = new String[] {"Simple", "Full cream"};
    FeatureSource featureSource;
    
    public StyleEditorChooser(FeatureSource featureSource) {
    	this(featureSource, null);
    }
    
	/**
	 * Inizializes the default style editor chooser panel
	 */
	public StyleEditorChooser(FeatureSource featureSource, Style s) {
		super();
        this.featureSource = featureSource; 
		
        initialize();
        setStyle(s);
        setOpaque(true);
	}
    
    private void initialize() {
    	lblType = new JLabel("Style editor type:");
        cmbType = new JComboBox(types);
        cmbType.setSelectedIndex(1);
        editorPanel = new JPanel();
        editorPanel.setLayout(new BorderLayout());
        
        setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, lblType, cmbType);
        FormUtils.addFiller(this, 1, 0, editorPanel, false);
        
        cmbType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeStyleEditor();
			}
		});
        
        this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				editorPanel.revalidate();
			}
		});
    }

	/**
	 * Called when the editor needs to be changed to  
	 */
	private void changeStyleEditor() {
        // a change style event is triggered during initialization and the styleeditor
        // is still null, exit immediatly in this case
        if(styleEditor == null) return;
        
        StyleEditor se = null;
        
		if(cmbType.getSelectedIndex() == SIMPLE && !(styleEditor instanceof SingleRuleEditor)) {
			SingleRuleEditor re = new SingleRuleEditor(getFeatureType(), true);
            se = re;
        } else if(cmbType.getSelectedIndex() == FULL && !(styleEditor instanceof FullStyleEditor)) {
            se = new FullStyleEditor(getFeatureType());
        }
        if(se == null)  // no change required
            return;
        
        Style s = styleEditor.getStyle();
        if(se.canEdit(s)) {
        	se.setStyle(s);
        } else {
        	int answer = JOptionPane.showConfirmDialog(this, "The chosen style editor does not fully support the current style.\n" + 
                    "If you press yes I will try to convert the style, but this may result in partial to full style information loss", "Style editor", JOptionPane.YES_NO_OPTION);
            if(answer == JOptionPane.YES_OPTION) {
                se.setStyle(s);
            } else {
            	if(styleEditor instanceof SingleRuleEditor)
            		cmbType.setSelectedIndex(SIMPLE);
            	else
            		cmbType.setSelectedIndex(FULL);
                return;
            }
        }
        
        // remove((JComponent) styleEditor);
        styleEditor = se;
        // FormUtils.addFiller(this, 1, 0, (JComponent) styleEditor, false);
        editorPanel.removeAll();
        editorPanel.add((JComponent) se);
        FormUtils.repackParentWindow(this);
        revalidate();
	}

	/**
     * Extracts a featureType from the feature source, or returns null
     * if no feature source is available
	 * @return
	 */
	private FeatureType getFeatureType() {
		if(featureSource != null)
            return featureSource.getSchema();
        else
			return null;
	}
	
	/**
	 * @see org.geotools.gui.swing.sldeditor.StyleEditor#getStyle()
	 */
	public Style getStyle() {
		return styleEditor.getStyle();
	}

	/**
	 * @see org.geotools.gui.swing.sldeditor.StyleEditor#setStyle(org.geotools.styling.Style)
	 */
	public void setStyle(Style s) {
		if(SingleRuleEditor.canEditStyle(s)) {
			styleEditor = new SingleRuleEditor(getFeatureType(), s);
            cmbType.setSelectedIndex(SIMPLE);
        } else {
        	styleEditor = new FullStyleEditor(getFeatureType(), s);
            cmbType.setSelectedIndex(FULL);
        }
        
        editorPanel.removeAll();
        editorPanel.add((JComponent) styleEditor);
	}

	/**
	 * @see org.geotools.gui.swing.sldeditor.StyleEditor#canEdit(org.geotools.styling.Style)
	 */
	public boolean canEdit(Style s) {
		return true;
	}
    
    public static void main(String[] args) {
        FormUtils.show(new StyleEditorChooser(null));
    }
}
