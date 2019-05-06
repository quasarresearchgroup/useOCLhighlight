/*
 * USE - UML based specification environment
 * Copyright (C) 1999-2004 Mark Richters, University of Bremen
 * 
 * Modified by: Maria Sales
 */

package org.tzi.use.gui.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tzi.use.config.Options;
import org.tzi.use.gui.util.CloseOnEscapeKeyListener;
import org.tzi.use.gui.util.TextComponentWriter;
import org.tzi.use.gui.views.diagrams.DiagramOptions;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagram;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramData;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramView;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassNode;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassifierNode;
import org.tzi.use.gui.views.diagrams.classdiagram.EnumNode;
import org.tzi.use.gui.views.diagrams.elements.Rolename;
import org.tzi.use.gui.views.diagrams.elements.edges.BinaryAssociationClassOrObject;
import org.tzi.use.gui.views.diagrams.elements.edges.EdgeBase;
import org.tzi.use.gui.views.diagrams.elements.edges.SimpleEdge;
import org.tzi.use.gui.views.evalbrowser.ExprEvalBrowser;
import org.tzi.use.main.ChangeEvent;
import org.tzi.use.main.ChangeListener;
import org.tzi.use.main.Session;
import org.tzi.use.parser.ocl.OCLCompiler;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.ocl.expr.Evaluator;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.MultiplicityViolationException;
import org.tzi.use.uml.ocl.value.Value;
import org.tzi.use.uml.sys.MSystem;
import org.tzi.use.util.StringUtil;
import org.tzi.use.util.TeeWriter;

/**
 * A dialog for entering and evaluating OCL expressions.
 * 
 * @version $ProjectVersion: 0.393 $
 * @author Mark Richters
 */
@SuppressWarnings("serial")
class EvalOCLDialog extends JDialog {
	private MSystem fSystem;

	private final JTextArea fTextIn;

	private final JTextArea fTextOut;

	private ExprEvalBrowser fEvalBrowser;

	private Evaluator evaluator;

	private final JButton btnEval;

	private List<ClassDiagramView> classDiagrams;

	private final ChangeListener sessionChangeListener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			Session session = (Session) e.getSource();
			fSystem = getSystem(session);
		}
	};

	EvalOCLDialog(final Session session, JFrame parent, List<ClassDiagramView> classDiagrams) {
		super(parent, "Evaluate OCL expression with Highlight");
		fSystem = getSystem(session);
		session.addChangeListener(sessionChangeListener);
		this.classDiagrams = classDiagrams;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// unregister from session on close
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				session.removeChangeListener(sessionChangeListener);
			}
		});

		// Use font specified in the settings
		Font evalFont = Font.getFont("use.gui.evalFont", getFont());

		// create text components and labels
		fTextIn = new JTextArea();
		fTextIn.setFont(evalFont);
		JLabel textInLabel = new JLabel("Enter OCL expression:");
		textInLabel.setDisplayedMnemonic('O');
		textInLabel.setLabelFor(fTextIn);

		fTextOut = new JTextArea();
		fTextOut.setEditable(false);
		fTextOut.setLineWrap(true);
		fTextOut.setFont(evalFont);

		JLabel textOutLabel = new JLabel("Result:");
		textOutLabel.setLabelFor(fTextOut);

		// create panel on the left and add text components
		JPanel textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));

		JPanel p = new JPanel(new BorderLayout());
		p.add(textInLabel, BorderLayout.NORTH);
		p.add(new JScrollPane(fTextIn), BorderLayout.CENTER);
		textPane.add(p);
		textPane.add(Box.createRigidArea(new Dimension(0, 5)));

		p = new JPanel(new BorderLayout());
		p.add(textOutLabel, BorderLayout.NORTH);
		p.add(new JScrollPane(fTextOut), BorderLayout.CENTER);
		textPane.add(p);
		textPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// create panel on the right containing buttons
		JPanel btnPane = new JPanel();
		btnEval = new JButton("Evaluate");
		btnEval.setMnemonic('E');

		btnEval.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (fEvalBrowser != null && fEvalBrowser.getFrame().isVisible()) {
					// if evaluation browser is already open, update it as well
					boolean evalSuccess = evaluate(fTextIn.getText(), true);

					if (evalSuccess) {
						fEvalBrowser.updateEvalBrowser(evaluator.getEvalNodeRoot());
					}
				} else {
					evaluate(fTextIn.getText(), false);
				}
			}
		});
		Dimension dim = btnEval.getMaximumSize();
		dim.width = Short.MAX_VALUE;
		btnEval.setMaximumSize(dim);
		JButton btnClear = new JButton("Clear");
		btnClear.setMnemonic('C');
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fTextOut.setText(null);
				if (fEvalBrowser != null) {
					fEvalBrowser.getFrame().setVisible(false);
					fEvalBrowser.getFrame().dispose();
				}

				// Reset color
				resetClassDiagramColor();
			}

		});
		dim = btnClear.getMaximumSize();
		dim.width = Short.MAX_VALUE;
		btnClear.setMaximumSize(dim);
		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
				if (fEvalBrowser != null) {
					fEvalBrowser.getFrame().setVisible(false);
					fEvalBrowser.getFrame().dispose();
				}
			}
		});
		dim = btnClose.getMaximumSize();
		dim.width = Short.MAX_VALUE;
		btnClose.setMaximumSize(dim);

		btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.Y_AXIS));
		btnPane.add(Box.createVerticalGlue());
		btnPane.add(btnEval);
		btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
		btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
		btnPane.add(btnClear);
		btnPane.add(Box.createRigidArea(new Dimension(0, 5)));
		btnPane.add(btnClose);
		btnPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		JComponent contentPane = (JComponent) getContentPane();
		contentPane.add(textPane, BorderLayout.CENTER);
		contentPane.add(btnPane, BorderLayout.EAST);
		getRootPane().setDefaultButton(btnEval);

		pack();
		setSize(new Dimension(500, 200));
		setLocationRelativeTo(parent);
		fTextIn.requestFocus();

		// allow dialog close on escape key
		CloseOnEscapeKeyListener ekl = new CloseOnEscapeKeyListener(this);
		addKeyListener(ekl);
		fTextIn.addKeyListener(ekl);
		fTextOut.addKeyListener(ekl);
	}
	
	/**
	 * Returns the session's system if available or an empty model.
	 * 
	 * @param session
	 *            current session
	 * @return a system to evaluate expressions in
	 */
	private MSystem getSystem(Session session) {
		if (session.hasSystem()) {
			return session.system();
		} else {
			MModel model = new ModelFactory().createModel("empty model");
			return new MSystem(model);
		}
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}

	private boolean evaluate(String in, boolean evalTree) {
		if (this.fSystem == null) {
			fTextOut.setText("No system!");
			return false;
		}

		// clear previous results
		fTextOut.setText(null);

		// send error output to result window and msg stream
		StringWriter msgWriter = new StringWriter();
		PrintWriter out = new PrintWriter(new TeeWriter(new TextComponentWriter(fTextOut), msgWriter), true);

		// compile query
		Expression expr = OCLCompiler.compileExpression(fSystem.model(), fSystem.state(), in, "Error", out,
				fSystem.varBindings());
		
		// Reset color
		resetClassDiagramColor();

		out.flush();
		fTextIn.requestFocus();

		// compile errors?
		if (expr == null) {
			// try to parse error message and set caret to error position
			String msg = msgWriter.toString();
			int colon1 = msg.indexOf(':');
			if (colon1 != -1) {
				int colon2 = msg.indexOf(':', colon1 + 1);
				int colon3 = msg.indexOf(':', colon2 + 1);

				try {
					int line = Integer.parseInt(msg.substring(colon1 + 1, colon2));
					int column = Integer.parseInt(msg.substring(colon2 + 1, colon3));
					int caret = 1 + StringUtil.nthIndexOf(in, line - 1, Options.LINE_SEPARATOR);
					caret += column;

					// sanity check
					caret = Math.min(caret, fTextIn.getText().length());
					fTextIn.setCaretPosition(caret);
				} catch (NumberFormatException ex) {
				}
			}
			return false;
		}

		try {
			// evaluate it with current system state
			evaluator = new Evaluator(evalTree);
			Value val = evaluator.eval(expr, fSystem.state(), fSystem.varBindings());
			// print result
			fTextOut.setText(val.toStringWithType());

			// paint class diagram
			paintClassDiagram(expr);

			return true;
		} catch (MultiplicityViolationException e) {
			fTextOut.setText("Could not evaluate. " + e.getMessage());
		}
		return false;
	}

	private void resetClassDiagramColor() {
		ClassDiagram classDiagram = getClassDiagram();
		if (classDiagram != null) {
			ClassDiagramData visibleData = (ClassDiagramData) classDiagram.getVisibleData();
			ClassDiagramData hiddenData = (ClassDiagramData) classDiagram.getHiddenData();
			
			visibleData.fClassToNodeMap.values().stream().forEach(this::resetClassNodeColor);
			hiddenData.fClassToNodeMap.values().stream().forEach(this::resetClassNodeColor);
			visibleData.getAllRolenames().stream().forEach(this::resetRolenameColor);
			hiddenData.getAllRolenames().stream().forEach(this::resetRolenameColor);
			visibleData.fEnumToNodeMap.values().forEach(this::resetEnumNodeColor);
			hiddenData.fEnumToNodeMap.values().forEach(this::resetEnumNodeColor);
			visibleData.fAssocClassToEdgeMap.values().forEach(this::resetEdgeColor);
			hiddenData.fAssocClassToEdgeMap.values().forEach(this::resetEdgeColor);
			visibleData.fBinaryAssocToEdgeMap.values().forEach(this::resetEdgeColor);
			hiddenData.fBinaryAssocToEdgeMap.values().forEach(this::resetEdgeColor);
			visibleData.fGenToGeneralizationEdge.values().forEach(this::resetEdgeColor);
			hiddenData.fGenToGeneralizationEdge.values().forEach(this::resetEdgeColor);

			classDiagram.repaint();
		}
	}
	
	private void resetClassNodeColor(ClassNode node) {
		node.setColor(null);
		node.resetAttributeColor();
		node.resetOperationColor();
	}
	
	private void resetRolenameColor(Rolename rolename) {
		rolename.setColor(null);
	}
	
	private void resetEnumNodeColor(EnumNode enumNode) {
		try {
			Field fOptField = ClassifierNode.class.getDeclaredField("fOpt");
			fOptField.setAccessible(true);
			Method method = DiagramOptions.class.getMethod("registerTypeColor", String.class, Color.class,
					Color.class);
			DiagramOptions fOpt = (DiagramOptions) fOptField.get(enumNode);
			Color color = new Color(0xff, 0xf8, 0xb4);
			method.invoke(fOpt, "NODE_COLOR", color, color);
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException
				| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
	}
	
	private void resetEdgeColor(EdgeBase edge) {
		try {
			Field fOptField = EdgeBase.class.getDeclaredField("fOpt");
			fOptField.setAccessible(true);
			
			DiagramOptions fOpt = (DiagramOptions) fOptField.get(edge);
			fOpt.registerTypeColor("EDGE_COLOR", Color.BLACK, Color.BLACK);
			fOptField.set(edge, fOpt);
			
			if(edge instanceof BinaryAssociationClassOrObject) {
				Field associationClassEdgeField = BinaryAssociationClassOrObject.class.getDeclaredField("associationClassEdge");
				associationClassEdgeField.setAccessible(true);
				
				SimpleEdge simpleEdge = (SimpleEdge) associationClassEdgeField.get(edge);
				simpleEdge.setColor(Color.BLACK);
			}
		} catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void paintClassDiagram(Expression expr) {
		ClassDiagram classDiagram = getClassDiagram();
		if (expr != null && classDiagram != null) {
			expr.processWithVisitor(new HighlightExpressionVisitor(((ClassDiagramData) classDiagram.getVisibleData())));
			classDiagram.repaint();
		}
	}
	
	private ClassDiagram getClassDiagram() {
		ClassDiagram classDiagram = null;
		if (!classDiagrams.isEmpty()) {
			try {
				Class<?> classDiagramViewCls =  classDiagrams.get(0).getClass();
				Field field = classDiagramViewCls.getDeclaredField("fClassDiagram");
				field.setAccessible(true);
				classDiagram = (ClassDiagram) field.get(classDiagrams.get(classDiagrams.size()-1));
			} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException  e1) {
				e1.printStackTrace();
			}
		}
		return classDiagram;
	}
	
}
