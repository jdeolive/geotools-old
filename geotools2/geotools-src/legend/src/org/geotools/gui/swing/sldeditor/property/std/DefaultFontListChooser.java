/*
 * Created on 30-dic-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.gui.swing.sldeditor.property.std;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.geotools.gui.swing.sldeditor.property.FontListChooser;
import org.geotools.gui.swing.sldeditor.util.FormUtils;

/**
 * @author wolf
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultFontListChooser extends FontListChooser {
	JTextField textField;
	JButton btnChooseFonts;
	String[] fontList;

	public DefaultFontListChooser() {
		this(new String[0]);
	}

	public DefaultFontListChooser(String[] selectedFonts) {
		// setup components
		textField = new JTextField();
		textField.setEditable(false);
		textField.setText(buildFontList(selectedFonts));

		btnChooseFonts = new JButton("...");
		btnChooseFonts.setPreferredSize(FormUtils.getButtonDimension());

		// layout 
		setLayout(new BorderLayout());
		add(textField);
		add(btnChooseFonts, BorderLayout.EAST);

		// events
		btnChooseFonts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFontDialog();
			}
		});
	}
	
	public String[] getFontNames() {
		return fontList;
	}
	
	public void setFontNames(String[] fonts) {
		fontList = fonts;
		textField.setText(buildFontList(fonts));
	}

	private void openFontDialog() {
		FontListChooserDialog dialog;
		Window w = FormUtils.getWindowForComponent(this);
		if (w instanceof Frame) {
			dialog = new FontListChooserDialog((Frame) w, fontList);
		} else {
			dialog = new FontListChooserDialog((Dialog) w, fontList);
		}

		dialog.show();
		if (dialog.exitOk()) {
			fontList = dialog.getSelectedFonts();
			textField.setText(buildFontList(fontList));
		}

	}

	private String buildFontList(String[] selectedFonts) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < selectedFonts.length; i++) {
			sb.append(selectedFonts[i]);
			if (i < (selectedFonts.length - 1))
				sb.append(", ");
		}
		return sb.toString();
	}

	/**
	 * Font dialog chooser
	 * 
	 * @author wolf
	 *
	 */
	private static class FontListChooserDialog extends JDialog {
		protected boolean exitOK;
		JList lstSystemFonts;
		JList lstChosenFonts;
		JCheckBox chkChooseMultipleFonts;
		JButton btnAddFont;
		JButton btnInsertFont;
		JButton btnRemoveFont;
		JButton btnMoveUpFont;
		JButton btnMoveDownFont;
		JButton btnOk;
		JButton btnCancel;
		JScrollPane scpSystemFonts;
		JScrollPane scpChosenFonts;

		public FontListChooserDialog(Frame parent, String[] chosenFonts) {
			super(parent, true);
			init(chosenFonts);
		}

		public FontListChooserDialog(Dialog parent, String[] chosenFonts) {
			super(parent, true);
			init(chosenFonts);
		}

		/**
		 * @return the fonts selected in the dialog, or an empty array if no
		 * 	selection has been performed
		 */
		public String[] getSelectedFonts() {
			if (chkChooseMultipleFonts.isSelected()) {
				DefaultListModel model = (DefaultListModel) lstChosenFonts.getModel();
				if (model.getSize() == 0) {
					return new String[0];
				} else {
					String[] fonts = new String[model.getSize()];
					for (int i = 0; i < fonts.length; i++) {
						fonts[i] = (String) model.getElementAt(i);
					}
					return fonts;
				}
			} else {
				String chosen = (String) lstSystemFonts.getSelectedValue();
				if (chosen == null)
					return new String[0];
				else
					return new String[] { chosen };
			}
		}

		private void init(String[] chosenFonts) {
			if (chosenFonts == null)
				chosenFonts = new String[0];

			// create components
			DefaultListModel model = new DefaultListModel();
			String[] systemFonts =
				GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			lstSystemFonts = new JList(new SimpleListModel(systemFonts));
			scpSystemFonts = new JScrollPane(lstSystemFonts);
			lstChosenFonts = new JList(new SimpleListModel(chosenFonts));
			scpChosenFonts = new JScrollPane(lstChosenFonts);
			chkChooseMultipleFonts = new JCheckBox("Choose a font list");
			chkChooseMultipleFonts.setSelected(true);
			btnAddFont = new JButton("+");
			btnInsertFont = new JButton(">");
			btnRemoveFont = new JButton("x");
			btnMoveUpFont = new JButton("u");
			btnMoveDownFont = new JButton("d");
			btnOk = new JButton("Ok");
			btnCancel = new JButton("Cancel");
			JPanel mainPanel = new JPanel();
			JPanel fontPanel = new JPanel();
			JPanel commandPanel = new JPanel();

			// layout 
			commandPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));
			commandPanel.add(btnOk);
			commandPanel.add(btnCancel);
			fontPanel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			// warning, reusing grid bag constraints object, order it's important
			gbc.insets = FormUtils.getDefaultInsets();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weightx = 1;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			fontPanel.add(chkChooseMultipleFonts, gbc);
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			fontPanel.add(scpSystemFonts, gbc);
			gbc.gridx = 2;
			fontPanel.add(scpChosenFonts, gbc);
			gbc.gridx = 1;
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.BOTH;
			fontPanel.add(btnAddFont, gbc);
			gbc.gridy = 2;
			fontPanel.add(btnInsertFont, gbc);
			gbc.gridy = 3;
			fontPanel.add(btnRemoveFont, gbc);
			gbc.gridy = 4;
			fontPanel.add(btnMoveUpFont, gbc);
			gbc.gridy = 5;
			fontPanel.add(btnMoveDownFont, gbc);
			gbc.gridy = 6;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			fontPanel.add(new JLabel(), gbc);
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(fontPanel);
			mainPanel.add(commandPanel, BorderLayout.SOUTH);
			setContentPane(mainPanel);

			// events
			chkChooseMultipleFonts.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					chooseMultipleFontsStateChanged();
				}

			});
			lstSystemFonts.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					systemFontsMouseClicked(e);
				}
			});
			btnAddFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnAddActionPerformed();
				}
			});
			btnRemoveFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnRemoveFontActionPerformed();
				}
			});
			btnInsertFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnInsertFontActionPerformed();
				}
			});
			btnMoveUpFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnMoveUpActionPerformed();
				}
			});
			btnMoveDownFont.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnMoveDownActionPerformed();
				}
			});
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if ((chkChooseMultipleFonts.isSelected()
						&& lstChosenFonts.getModel().getSize() == 0)
						|| (!chkChooseMultipleFonts.isSelected()
							&& lstSystemFonts.getSelectedIndex() == -1)) {
						JOptionPane.showMessageDialog(
							FontListChooserDialog.this,
							"You should select at least one item",
							"Font chooser",
							JOptionPane.ERROR_MESSAGE);
						return;
					}
					exitOK = true;
					dispose();
				}
			});
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exitOK = false;
					dispose();
				}
			});

			pack();

		}

		public boolean exitOk() {
			return exitOK;
		}

		private void btnMoveDownActionPerformed() {
			int selectedIndex = lstChosenFonts.getSelectedIndex();
			int size = lstChosenFonts.getModel().getSize();
			if (selectedIndex == -1 || selectedIndex == (size - 1))
				return;

			DefaultListModel model = (DefaultListModel) lstChosenFonts.getModel();
			Object font1 = model.getElementAt(selectedIndex);
			Object font2 = model.getElementAt(selectedIndex + 1);
			model.setElementAt(font2, selectedIndex);
			model.setElementAt(font1, selectedIndex + 1);
			lstChosenFonts.setSelectedIndex(selectedIndex + 1);
		}

		/**
		 * 
		 */
		private void btnMoveUpActionPerformed() {
			int selectedIndex = lstChosenFonts.getSelectedIndex();
			if (selectedIndex <= 0)
				return;

			DefaultListModel model = (DefaultListModel) lstChosenFonts.getModel();
			Object font1 = model.getElementAt(selectedIndex);
			Object font2 = model.getElementAt(selectedIndex - 1);
			model.setElementAt(font2, selectedIndex);
			model.setElementAt(font1, selectedIndex - 1);
			lstChosenFonts.setSelectedIndex(selectedIndex - 1);

		}

		/**
		 * 
		 */
		private void btnInsertFontActionPerformed() {
			insertSelectedSystemFonts();
		}

		private void btnRemoveFontActionPerformed() {
			int[] selectedIndices = lstChosenFonts.getSelectedIndices();
			DefaultListModel model = (DefaultListModel) lstChosenFonts.getModel();
			for (int i = 0; i < selectedIndices.length; i++) {
				model.remove(selectedIndices[i] - i);
			}
		}

		private void btnAddActionPerformed() {
			String result = JOptionPane.showInputDialog(this, "Add font");
			if (result != null)
				addToChosenFonts(result);
		}

		private void chooseMultipleFontsStateChanged() {
			boolean selected = chkChooseMultipleFonts.isSelected();

			btnAddFont.setVisible(selected);
			btnInsertFont.setVisible(selected);
			btnRemoveFont.setVisible(selected);
			btnMoveUpFont.setVisible(selected);
			btnMoveDownFont.setVisible(selected);
			scpChosenFonts.setVisible(selected);

			if (selected) {
				String font = (String) lstSystemFonts.getSelectedValue();
				if (font != null)
					addToChosenFonts(font);
			} else {
				lstSystemFonts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				if (lstSystemFonts.getSelectedIndex() != -1)
					lstSystemFonts.setSelectedIndex(lstSystemFonts.getSelectedIndex());
			}
		}

		private void systemFontsMouseClicked(MouseEvent e) {
			if (e.getClickCount() >= 2) {
				insertSelectedSystemFonts();

			}
		}

		private void insertSelectedSystemFonts() {
			Object[] fonts = lstSystemFonts.getSelectedValues();
			if (fonts != null) {
				for (int i = 0; i < fonts.length; i++) {
					addToChosenFonts((String) fonts[i]);
				}
			}
		}

		/**
		 * Adds the font to the chosen fonts list unless it's already in
		 * @param font
		 */
		private void addToChosenFonts(String font) {
			DefaultListModel model = (DefaultListModel) lstChosenFonts.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				if (model.getElementAt(i).equals(font))
					return;
			}
			model.addElement(font);
			lstChosenFonts.setSelectedIndex(model.getSize() - 1);
		}
	}

	/**
	 * Simple extension that adds a decent constructor to the DefaultListModel class
	 * @author wolf
	 */
	private static class SimpleListModel extends DefaultListModel {
		public SimpleListModel(Object[] list) {
			super();
			if (list != null && list.length > 0) {
				ensureCapacity(list.length);
				for (int i = 0; i < list.length; i++) {
					addElement(list[i]);
				}
			}
		}
	}

	/**
	 * Simple main for testing purposes
	 */
	public static void main(String[] args) {
		FormUtils.show(new DefaultFontListChooser(new String[0]));
	}

}