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
import org.tzi.use.gui.plugins.complexity.ComplexityMetric;
import org.tzi.use.gui.plugins.complexity.ExpressionComplexityVisitor;
import org.tzi.use.gui.plugins.complexity.IComplexityMetric;
import org.tzi.use.gui.plugins.complexity.IComplexityMetricResult;
import org.tzi.use.gui.plugins.highlight.HighlightExpressionVisitor;
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
 * A dialog with information on OCL complexity metrics.
 * 
 * @author Maria Sales
 */
@SuppressWarnings("serial")
class OCLComplexityDialog extends JDialog {
	
	private static final String NNR = "NNR Metric (Number of Navigated Relationships): Measures the total number of navigated relationships in an expression. Relationships are only counted once, even if they're navigated multiple times.";
	private static final String NAN = "NAN Metric (Number of Attributes referred through Navigations): Counts the number of attributes referred through navigations.";
	private static final String WNO = "WNO Metric (Weighted Number of referred Operations through navigations): sum of weighted operations (where the weight is defined by the number of in/out parameters,  including the return type) mentioned through navigations.";
	private static final String NNC = "NNC Metric (Number of Navigated Classes): number of classes, association classes or interfaces referred through navigations.";
	private static final String WNM = "WNM (Weighted Number of Messages): sum of weighted messages (where the weight is defined by the number of parameters) present in an expression.";
	private static final String NPT = "NPT (Number of Parameters whose Types are classes defined in the Class Diagram): number of different classes and interfaces used as in/out parameters or result.";
	private static final String NUCA = "NUCA (Number of Utility Class Attributes used): number of utility classes' attributes used in an expression; attributes that belong to the same utility class are only counted once, even if they are referred multiple times.";
	private static final String NUCO = "NUCO (Number of Utility Class Operations used): similar to the NUCA metric, but instead of attributes it considers operations.";
	private static final String DN = "DN (Depth of Navigations): maximum depth of a navigation tree, where the root node represents the class name of the contextual instance (self); for each navigation that starts from self, we create a branch that connects to a new node, which represents the navigated class; a new tree is created if a navigation contains a collection operation defined in terms of new navigation(s), and is connected to the original tree through a “definition connection”.";
	private static final String WNN = "WNN (Weighted Number of Navigations):  sum of weighted navigations presented in an expression, where the weight is defined by the level on which the operation is used in an expression.";
	private static final String WCO = "WCO (Weighted Number of Collection Operations): sum of weighted collection operations, where the weight is defined by the level on which the operation is used in an expression.";
	private static final String REFERENCE = "L.  Reynoso,  M.  Genero,  and  M.  Piattini,  “Measuring  Ocl  Expressions:  an  Approach  Based  on  Cognitive  Techniques,”  inMetrics  for  Software  ConceptualModels,  Imperial  College  Press,  Distributed  by  WorldScientific Publishing Co., Jan. 2005, pp. 161–206";
	
	OCLComplexityDialog(JFrame parent) {
		super(parent, "Help: OCL Complexity");

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// Use font specified in the settings
		Font evalFont = Font.getFont("use.gui.evalFont", getFont());

		// create text components and labels
		JTextArea textArea = new JTextArea();
		textArea.setFont(evalFont);
		textArea.setText(String.format("OCL metrics definition:\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n\n\nReference: %s", NNR, NAN, WNO, NNC, WNM, NPT, NUCA, NUCO, DN, WNN, WCO, REFERENCE));
		textArea.setEditable(false);
		
//		JLabel textInLabel = new JLabel("NNR: ");

		// create panel on the left and add text components
		JPanel textPane = new JPanel();
		textPane.setLayout(new BoxLayout(textPane, BoxLayout.Y_AXIS));

		JPanel p = new JPanel(new BorderLayout());
		p.add(new JScrollPane(textArea), BorderLayout.CENTER);
//		p.add(textInLabel, BorderLayout.NORTH);
		textPane.add(p);
		textPane.add(Box.createRigidArea(new Dimension(0, 5)));

		JComponent contentPane = (JComponent) getContentPane();
		contentPane.add(textPane, BorderLayout.CENTER);

		pack();
		setSize(new Dimension(500, 200));
		setLocationRelativeTo(parent);
	}
	
}
