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
 * AbstractPanelListEditor.java
 *
 * Created on 13 dicembre 2003, 19.29
 */
package org.geotools.gui.swing.sldeditor.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public abstract class AbstractPanelListEditor extends JComponent {
    JTabbedPane tbpPanels;
    JToolBar toolbar;
    JButton btnAddPanel;
    JButton btnRemovePanel;
    JButton btnMoveUpPanel;
    JButton btnMoveDownPanel;
    protected boolean allowZeroPanels;

    /**
     * Creates a new instance of AbstractPanelListEditor
     *
     * @param allowZeroPanels DOCUMENT ME!
     */
    public AbstractPanelListEditor(boolean allowZeroPanels) {
        this.allowZeroPanels = allowZeroPanels;

        tbpPanels = new JTabbedPane();
        btnAddPanel = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Add16.gif")));
        btnRemovePanel = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Remove16.gif")));
        btnMoveUpPanel = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
        btnMoveDownPanel = new JButton(new ImageIcon(getClass().getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
        FormUtils.forceButtonDimension(btnAddPanel);
        FormUtils.forceButtonDimension(btnRemovePanel);
        FormUtils.forceButtonDimension(btnMoveUpPanel);
        FormUtils.forceButtonDimension(btnMoveDownPanel);
        toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 3));
        // toolbar.setBorderPainted(false);
        toolbar.setFloatable(false);
        // toolbar.setRollover(true);

        // toolbar.set
        toolbar.add(btnAddPanel);
        toolbar.add(btnRemovePanel);
        toolbar.add(btnMoveUpPanel);
        toolbar.add(btnMoveDownPanel);
        toolbar.setVisible(true);

        setLayout(new BorderLayout());
        add(toolbar, BorderLayout.WEST);
        add(tbpPanels, BorderLayout.CENTER);
        tbpPanels.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        btnAddPanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    addButtonPressed();
                }
            });
        btnRemovePanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    removeButtonPressed();
                }
            });
        btnMoveUpPanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    moveUpButtonPressed();
                }
            });
        btnMoveDownPanel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    moveDownButtonPressed();
                }
            });

        // button enabling setup
        tbpPanels.addContainerListener(new ContainerListener() {
                public void componentAdded(ContainerEvent e) {
                    panelChanged();
                }

                public void componentRemoved(ContainerEvent e) {
                    panelChanged();
                }
            });

        tbpPanels.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    TabComponent tc = (TabComponent) tbpPanels
                        .getSelectedComponent();

                    if ((tc != null)
                            && tc.getComponent() instanceof Placeholder) {
                        Component comp = lazyInitialiazePanel(tbpPanels
                                .getSelectedIndex());

                        if (comp != null) {
                            tc.setComponent(comp);
                        }
                    }
                }
            });
    }

    /**
     * To be implemented by subclasses that wants to use lazy initialization,
     * should return the real component that must go into the panelIndex
     * location
     *
     * @param panelIndex
     *
     * @return
     */
    protected Component lazyInitialiazePanel(int panelIndex) {
        return null;
    }

    protected void panelChanged() {
        boolean enabled = tbpPanels.getTabCount() > 1;
        btnMoveDownPanel.setEnabled(enabled);
        btnMoveUpPanel.setEnabled(enabled);
        btnRemovePanel.setEnabled(allowZeroPanels || enabled);
    }

    protected abstract void addButtonPressed();

    protected void removeButtonPressed() {
        int index = tbpPanels.getSelectedIndex();

        if (index != -1) {
            tbpPanels.remove(index);
        }
    }

    protected void moveDownButtonPressed() {
        int index = tbpPanels.getSelectedIndex();

        if ((index >= 0) && (index < (tbpPanels.getTabCount() - 1))) {
            Component c1 = tbpPanels.getComponentAt(index);
            String t1 = tbpPanels.getTitleAt(index);
            Component c2 = tbpPanels.getComponentAt(index + 1);
            String t2 = tbpPanels.getTitleAt(index + 1);

            tbpPanels.remove(c1);
            tbpPanels.remove(c2);
            tbpPanels.add(c2, index);
            tbpPanels.add(c1, index + 1);
            tbpPanels.setTitleAt(index + 1, t1);
            tbpPanels.setTitleAt(index, t2);
            tbpPanels.setSelectedIndex(index + 1);
        }
    }

    protected void moveUpButtonPressed() {
        int index = tbpPanels.getSelectedIndex();

        if (index > 0) {
            Component c1 = tbpPanels.getComponentAt(index);
            String t1 = tbpPanels.getTitleAt(index);
            Component c2 = tbpPanels.getComponentAt(index - 1);
            String t2 = tbpPanels.getTitleAt(index - 1);

            tbpPanels.remove(c1);
            tbpPanels.remove(c2);
            tbpPanels.add(c1, index - 1);
            tbpPanels.add(c2, index);
            tbpPanels.setTitleAt(index - 1, t1);
            tbpPanels.setTitleAt(index, t2);
            tbpPanels.setSelectedIndex(index - 1);
        }
    }

    protected void removeAllPanels() {
        tbpPanels.removeAll();
    }

    protected void addPanel(String title, Component component) {
        tbpPanels.add(title, new TabComponent(component));
    }

    protected void addPlaceholder(String title) {
        tbpPanels.add(title, new TabComponent(new Placeholder()));
    }

    protected Component[] getPanels() {
        Component[] comps = new Component[tbpPanels.getTabCount()];

        for (int i = 0; i < comps.length; i++) {
            TabComponent tc = (TabComponent) tbpPanels.getComponentAt(i);
            Component comp = tc.getComponent();

            if (!(comp instanceof Placeholder)) {
                comps[i] = comp;
            } else {
                comps[i] = null;
            }
        }

        return comps;
    }

    protected int getPanelCount() {
        return tbpPanels.getTabCount();
    }

    protected void setComponents(Component[] components, String[] titles) {
        if ((components == null) || (components.length == 0)) {
            tbpPanels.removeAll();

            return;
        }

        if (components.length != titles.length) {
            throw new IllegalArgumentException(
                "Components and title arrays have different dimensions");
        }

        tbpPanels.removeAll();

        for (int i = 0; i < components.length; i++) {
            tbpPanels.add(titles[i], components[i]);
        }
    }

    public void setSelectedComponent(Component c) {
        tbpPanels.setSelectedComponent(c);
    }

    public void setSelectedIndex(int index) {
        tbpPanels.setSelectedIndex(index);
    }

    public static void main(String[] args) throws Exception {
        FormUtils.show(new AbstractPanelListEditor(true) {
                private int counter = 0;

                protected void addButtonPressed() {
                    String title = String.valueOf(counter++);
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel(title);
                    panel.add(label);
                    addPanel(title, panel);
                }
            });
    }

    private static class TabComponent extends JComponent {
        public TabComponent() {
            setLayout(new BorderLayout());
            add(new Placeholder());
        }

        public TabComponent(Component component) {
            setLayout(new BorderLayout());
            add(component);
        }

        public Component getComponent() {
            return getComponent(0);
        }

        public void setComponent(Component component) {
            removeAll();
            add(component);
        }
    }

    private static class Placeholder extends JLabel {
        public Placeholder() {
            super(
                "Placeholder, if you see this there is a bug in the lazy initialization code!");
        }
    }
}
