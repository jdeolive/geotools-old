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
package org.geotools.gui.swing.sldeditor.style.full;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.gui.swing.sldeditor.SLDEditor;
import org.geotools.gui.swing.sldeditor.style.StyleEditor;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerChooserDialog;
import org.geotools.gui.swing.sldeditor.symbolizer.SymbolizerEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;
import org.geotools.gui.swing.sldeditor.util.SymbolizerUtils;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class FullStyleEditor extends JComponent implements SLDEditor, StyleEditor {
    private JButton btnCollapse;
    private JButton btnExpand;
    private Object currentObject;
    private JScrollPane scpTree;
    private JPanel treePanel;
    private TreeStyleEditor treeEditor;
    private JButton btnMoveDown;
    private JButton btnMoveUp;
    private JButton btnRemove;
    private JButton btnAdd;
    private JToolBar toolbar;
    private JSplitPane splitPane;
    private FeatureType featureType;
    private Style style;

    public FullStyleEditor(FeatureType ft) {
        this(ft, null);
    }

    public FullStyleEditor(FeatureType ft, Style s) {
        this.featureType = ft;

        // build the user interface
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setBorderPainted(false);
        toolbar.setRollover(true);

        btnAdd = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Add16.gif")));
        btnRemove = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Remove16.gif")));
        btnMoveUp = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Up16.gif")));
        btnMoveDown = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Down16.gif")));
        btnExpand = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
        btnCollapse = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
        FormUtils.forceButtonDimension(btnAdd);
        FormUtils.forceButtonDimension(btnRemove);
        FormUtils.forceButtonDimension(btnMoveUp);
        FormUtils.forceButtonDimension(btnMoveDown);
        FormUtils.forceButtonDimension(btnExpand);
        FormUtils.forceButtonDimension(btnCollapse);
        toolbar.add(btnAdd);
        toolbar.add(btnRemove);
        toolbar.add(btnMoveUp);
        toolbar.add(btnMoveDown);
        toolbar.add(new JToolBar.Separator());
        toolbar.add(btnExpand);
        toolbar.add(btnCollapse);

        treeEditor = new TreeStyleEditor(s, ft);
        treeEditor.setSelectionObject(s);
        scpTree = new JScrollPane(treeEditor);

        treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        treePanel.add(toolbar, BorderLayout.NORTH);
        treePanel.add(scpTree);

        if (s != null) {
            StyleMetadataEditor sme = new StyleMetadataEditor();
            sme.setStyle(s);
            splitPane.setRightComponent(sme);
            currentObject = s;
        }

        splitPane.setLeftComponent(treePanel);
        setLayout(new BorderLayout());
        add(splitPane);

        // add the selection listener
        treeEditor.expandTree();
        treeEditor.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    selectionChanged();
                }
            });

        btnExpand.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    treeEditor.expandTree();
                }
            });
        btnCollapse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    treeEditor.collapseTree();
                }
            });

        btnAdd.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addElementInTree();
                }
            });

        btnRemove.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeElementInTree();
                }
            });
        btnMoveDown.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveDownSelection();
                }
            });
        btnMoveUp.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    moveUpSelection();
                }
            });
    }

    protected void moveDownSelection() {
        Object selection = treeEditor.getSelectionObject();

        if (selection == null) {
            return;
        }

        treeEditor.moveDown(selection);
        treeEditor.setSelectionObject(selection);
    }

    protected void moveUpSelection() {
        Object selection = treeEditor.getSelectionObject();

        if (selection == null) {
            return;
        }

        treeEditor.moveUp(selection);
        treeEditor.setSelectionObject(selection);
    }

    protected void removeElementInTree() {
        Object selection = treeEditor.getSelectionObject();

        if (treeEditor.wouldRemoveRoot(selection)) {
            JOptionPane.showMessageDialog(this,
                "Cannot remove this object, would have to remove the style too");

            return;
        }

        Object[] siblings = treeEditor.getSiblings(selection);

        if (siblings.length == 0) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "This object is isolated, will have to remove its parent too. Proceed?",
                    "Style editor", JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.YES_OPTION) {
                treeEditor.remove(selection);
                treeEditor.setSelectionObject(style);
            }
        } else {
            Object sibling = treeEditor.getSiblingAfter(selection);

            if (sibling == null) {
                sibling = treeEditor.getSiblingBefore(selection);
            }

            treeEditor.remove(selection);
            treeEditor.setSelectionObject(sibling);
        }
    }

    protected void addElementInTree() {
        storeChangesIntoStyle();

        JComponent editorComponent = null;
        SymbolizerChooserDialog dialog = symbolizerEditorFactory.createSymbolizerChooserDialog(this, featureType);
        dialog.show();
        if(!dialog.exitOk()) {
           return;
        }
        Symbolizer symbolizer = dialog.getSelectedSymbolizer();
        
        Object selection = treeEditor.getSelectionObject();

        if (selection == null) {
            return;
        } else {
            if (selection instanceof Style) {
                FeatureTypeStyle fts = styleBuilder.createFeatureTypeStyle(symbolizer);
                treeEditor.addFeatureTypeStyle(fts);
            } else if (selection instanceof FeatureTypeStyle) {
                Rule r = styleBuilder.createRule(symbolizer);
                treeEditor.addRule((FeatureTypeStyle) selection, r);
            } else if (selection instanceof Rule) {
                treeEditor.addSymbolizer((Rule) selection, symbolizer);
            } else if (selection instanceof Symbolizer) {
                Rule rule = (Rule) treeEditor.findParentObject(selection);
                treeEditor.addSymbolizer(rule, symbolizer);
            }
        }
    }

    /**
     *
     */
    protected void selectionChanged() {
        // TODO: update button state according to the current selection
        storeChangesIntoStyle();

        JComponent editorComponent = null;
        Object selection = treeEditor.getSelectionObject();

        if (selection == null) {
            splitPane.setRightComponent(null);
        } else {
            if (selection instanceof Style) {
                Style style = (Style) selection;
                StyleMetadataEditor sme = new StyleMetadataEditor();
                sme.setStyle(style);
                editorComponent = sme;
            } else if (selection instanceof FeatureTypeStyle) {
                FeatureTypeStyle fts = (FeatureTypeStyle) selection;
                editorComponent = new FTSMetadataEditor(fts, featureType);
            } else if (selection instanceof Rule) {
                Rule rule = (Rule) selection;
                editorComponent = new RuleMetadataEditor(rule, featureType);
            } else if (selection instanceof Symbolizer) {
                SymbolizerEditor se = SymbolizerUtils.getSymbolizerEditor((Symbolizer) selection,
                        featureType);
                se.setSymbolizer((Symbolizer) selection);
                editorComponent = (JComponent) se;
            }

            currentObject = selection;
        }

        splitPane.setRightComponent(editorComponent);
        splitPane.revalidate();
        FormUtils.repackParentWindow(this);
    }

    private void storeChangesIntoStyle() {
        if ((currentObject != null) && (splitPane.getRightComponent() != null)) {
            if (currentObject instanceof Style) {
                Style style = (Style) currentObject;
                StyleMetadataEditor sme = (StyleMetadataEditor) splitPane.getRightComponent();
                sme.fillMetadata(style);
            } else if (currentObject instanceof FeatureTypeStyle) {
                FeatureTypeStyle fts = (FeatureTypeStyle) currentObject;
                FTSMetadataEditor editor = (FTSMetadataEditor) splitPane.getRightComponent();
                editor.fillFeatureTypeStyle(fts);
            } else if (currentObject instanceof Rule) {
                Rule rule = (Rule) currentObject;
                RuleMetadataEditor editor = (RuleMetadataEditor) splitPane.getRightComponent();
                editor.fillRule(rule);
            } else if (currentObject instanceof Symbolizer) {
                SymbolizerEditor se = (SymbolizerEditor) splitPane.getRightComponent();

                // as a side effect the contained symbolizer gets updated
                se.getSymbolizer();
            }
        }
    }

    public void setStyle(Style s) {
        if (s == null) {
            FeatureTypeStyle featureStyle = styleBuilder.createFeatureTypeStyle(SymbolizerUtils
                    .getDefaultSymbolizer(featureType));
            Rule r = featureStyle.getRules()[0];
            r.setName("Rule1");
            featureStyle.setName("FeatureTypeStyle1");
            this.style = styleBuilder.createStyle();
            style.addFeatureTypeStyle(featureStyle);
        } else {
            this.style = s;
            treeEditor.setStyle(s);
            treeEditor.setSelectionObject(s);
        }
    }

    public Style getStyle() {
        storeChangesIntoStyle();
        return treeEditor.getStyle();
    }

    public static void main(String[] args) throws Exception {
        AttributeType geom = AttributeTypeFactory.newAttributeType("geom",
                com.vividsolutions.jts.geom.Polygon.class);
        AttributeType[] attributeTypes = new AttributeType[] {
                geom, AttributeTypeFactory.newAttributeType("name", String.class),
                AttributeTypeFactory.newAttributeType("population", Long.class)
            };

        FeatureType ft = DefaultFeatureTypeFactory.newFeatureType(attributeTypes, "demo", "",
                false, null, (GeometryAttributeType) geom);
        FeatureTypeStyle featureStyle = styleBuilder.createFeatureTypeStyle(SymbolizerUtils
                .getDefaultSymbolizer(ft));
        Rule r = featureStyle.getRules()[0];
        r.setName("Rule1");
        featureStyle.setName("FeatureTypeStyle1");

        Style style = styleBuilder.createStyle();
        style.addFeatureTypeStyle(featureStyle);
        FormUtils.show(new FullStyleEditor(ft, style));
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.StyleEditor#canEdit(org.geotools.styling.Style)
     */
    public boolean canEdit(Style s) {
        return true;
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        Dimension preferred = super.getPreferredSize();
        preferred.height = 300;

        return preferred;
    }
}
