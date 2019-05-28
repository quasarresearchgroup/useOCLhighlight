package org.tzi.use.gui.plugins.highlight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A dialog for configuring OCL Highlight colors.
 * 
 * @author Maria Sales
 */
@SuppressWarnings("serial")
public class OCLHighlightConfigDialog extends JDialog {
	
	private static final Color CLASS_COLOR = new Color(68, 136, 214);
	private static final Color ENUM_COLOR = CLASS_COLOR;
	private static final Color ATTRIBUTE_COLOR = new Color(167, 202, 242);
	private static final Color OPERATION_COLOR = ATTRIBUTE_COLOR;
	private static final Color ROLENAME_COLOR = ATTRIBUTE_COLOR;
	private static final Color EDGE_COLOR = new Color(255, 132, 66);
	
	private JColorChooser classColorChooser;
	private JColorChooser enumColorChooser;
	private JColorChooser attributeColorChooser;
	private JColorChooser operationColorChooser;
	private JColorChooser rolenameColorChooser;
	private JColorChooser edgeColorChooser;
	
	public OCLHighlightConfigDialog(JFrame parent) {
		super(parent, "Setup OCL Highlight");

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setuColorChoosers();
		
		JPanel textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));
		
		JPanel buttonBox = new JPanel();
		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));
		buttonBox.add(getResetAllButton());
		buttonBox.add(getResetButton("Reset Class Color", 'C', classColorChooser, CLASS_COLOR));
		buttonBox.add(getResetButton("Reset Enum Color", 'E', enumColorChooser, ENUM_COLOR));
		buttonBox.add(getResetButton("Reset Attribute Color", 'A', attributeColorChooser, ATTRIBUTE_COLOR));
		buttonBox.add(getResetButton("Reset Operation Color", 'O', operationColorChooser, OPERATION_COLOR));
		buttonBox.add(getResetButton("Reset Rolename Color", 'N', rolenameColorChooser, ROLENAME_COLOR));
		buttonBox.add(getResetButton("Reset Edge Color", 'G', edgeColorChooser, EDGE_COLOR));

		JPanel chooserBox = new JPanel();
		chooserBox.setLayout(new BoxLayout(chooserBox, BoxLayout.Y_AXIS));
		chooserBox.add(classColorChooser);
		chooserBox.add(enumColorChooser);
		chooserBox.add(attributeColorChooser);
		chooserBox.add(operationColorChooser);
		chooserBox.add(rolenameColorChooser);
		chooserBox.add(edgeColorChooser);
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(new JScrollPane(buttonBox), BorderLayout.WEST);
		p.add(new JScrollPane(chooserBox), BorderLayout.CENTER);

		textPane.add(p);
		
		JComponent contentPane = (JComponent) getContentPane();
		contentPane.add(textPane, BorderLayout.CENTER);

		pack();
		setSize(new Dimension(500, 200));
		setLocationRelativeTo(parent);
	}

	private void setuColorChoosers() {
		classColorChooser = new JColorChooser(CLASS_COLOR);
		classColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Class Color"));
		
		enumColorChooser = new JColorChooser(ENUM_COLOR);
		enumColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Enum Color"));

		attributeColorChooser = new JColorChooser(ATTRIBUTE_COLOR);
		attributeColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Attribute Color"));

		operationColorChooser = new JColorChooser(OPERATION_COLOR);
		operationColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Operation Color"));

		rolenameColorChooser = new JColorChooser(ROLENAME_COLOR);
		rolenameColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Rolename Color"));

		edgeColorChooser = new JColorChooser(EDGE_COLOR);
		edgeColorChooser.setBorder(BorderFactory.createTitledBorder("Choose Edge Color"));
	}
	
	private JButton getResetAllButton() {
		Dimension dim;
		JButton btnClear = new JButton("Reset All");
		btnClear.setMnemonic('R');
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				classColorChooser.setColor(CLASS_COLOR);
				enumColorChooser.setColor(ENUM_COLOR);
				attributeColorChooser.setColor(ATTRIBUTE_COLOR);
				operationColorChooser.setColor(OPERATION_COLOR);
				rolenameColorChooser.setColor(ROLENAME_COLOR);
				edgeColorChooser.setColor(EDGE_COLOR);
			}

		});
		dim = btnClear.getMaximumSize();
		dim.width = Short.MAX_VALUE;
		btnClear.setMaximumSize(dim);
		return btnClear;
	}
	
	private JButton getResetButton(String title, Character mnemonic, JColorChooser chooser, Color color) {
		Dimension dim;
		JButton btnClear = new JButton(title);
		btnClear.setMnemonic(mnemonic);
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setColor(color);
			}

		});
		dim = btnClear.getMaximumSize();
		dim.width = Short.MAX_VALUE;
		btnClear.setMaximumSize(dim);
		return btnClear;
	}

	public Color getClassColor() {
		return classColorChooser.getColor();
	}
	
	public Color getEnumColor() {
		return enumColorChooser.getColor();
	}
	
	public Color getAttributeColor() {
		return attributeColorChooser.getColor();
	}
	
	public Color getOperationColor() {
		return operationColorChooser.getColor();
	}
	
	public Color getRolenameColor() {
		return rolenameColorChooser.getColor();
	}
	
	public Color getEdgeColor() {
		return edgeColorChooser.getColor();
	}
}
