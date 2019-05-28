package org.tzi.use.gui.plugins.highlight;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.tzi.use.gui.views.diagrams.DiagramOptions;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramData;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassDiagramOptions;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassNode;
import org.tzi.use.gui.views.diagrams.classdiagram.ClassifierNode;
import org.tzi.use.gui.views.diagrams.classdiagram.EnumNode;
import org.tzi.use.gui.views.diagrams.elements.Rolename;
import org.tzi.use.gui.views.diagrams.elements.edges.BinaryAssociationClassOrObject;
import org.tzi.use.gui.views.diagrams.elements.edges.BinaryAssociationOrLinkEdge;
import org.tzi.use.gui.views.diagrams.elements.edges.EdgeBase;
import org.tzi.use.gui.views.diagrams.elements.edges.GeneralizationEdge;
import org.tzi.use.gui.views.diagrams.elements.edges.SimpleEdge;
import org.tzi.use.uml.mm.MAssociationEnd;
import org.tzi.use.uml.mm.MAttribute;
import org.tzi.use.uml.mm.MClass;
import org.tzi.use.uml.mm.MOperation;
import org.tzi.use.uml.ocl.expr.ExpAllInstances;
import org.tzi.use.uml.ocl.expr.ExpAny;
import org.tzi.use.uml.ocl.expr.ExpAsType;
import org.tzi.use.uml.ocl.expr.ExpAttrOp;
import org.tzi.use.uml.ocl.expr.ExpBagLiteral;
import org.tzi.use.uml.ocl.expr.ExpClosure;
import org.tzi.use.uml.ocl.expr.ExpCollect;
import org.tzi.use.uml.ocl.expr.ExpCollectNested;
import org.tzi.use.uml.ocl.expr.ExpConstBoolean;
import org.tzi.use.uml.ocl.expr.ExpConstEnum;
import org.tzi.use.uml.ocl.expr.ExpConstInteger;
import org.tzi.use.uml.ocl.expr.ExpConstReal;
import org.tzi.use.uml.ocl.expr.ExpConstString;
import org.tzi.use.uml.ocl.expr.ExpConstUnlimitedNatural;
import org.tzi.use.uml.ocl.expr.ExpEmptyCollection;
import org.tzi.use.uml.ocl.expr.ExpExists;
import org.tzi.use.uml.ocl.expr.ExpForAll;
import org.tzi.use.uml.ocl.expr.ExpIf;
import org.tzi.use.uml.ocl.expr.ExpIsKindOf;
import org.tzi.use.uml.ocl.expr.ExpIsTypeOf;
import org.tzi.use.uml.ocl.expr.ExpIsUnique;
import org.tzi.use.uml.ocl.expr.ExpIterate;
import org.tzi.use.uml.ocl.expr.ExpLet;
import org.tzi.use.uml.ocl.expr.ExpNavigation;
import org.tzi.use.uml.ocl.expr.ExpNavigationClassifierSource;
import org.tzi.use.uml.ocl.expr.ExpObjAsSet;
import org.tzi.use.uml.ocl.expr.ExpObjOp;
import org.tzi.use.uml.ocl.expr.ExpObjRef;
import org.tzi.use.uml.ocl.expr.ExpObjectByUseId;
import org.tzi.use.uml.ocl.expr.ExpOclInState;
import org.tzi.use.uml.ocl.expr.ExpOne;
import org.tzi.use.uml.ocl.expr.ExpOrderedSetLiteral;
import org.tzi.use.uml.ocl.expr.ExpQuery;
import org.tzi.use.uml.ocl.expr.ExpRange;
import org.tzi.use.uml.ocl.expr.ExpReject;
import org.tzi.use.uml.ocl.expr.ExpSelect;
import org.tzi.use.uml.ocl.expr.ExpSelectByKind;
import org.tzi.use.uml.ocl.expr.ExpSelectByType;
import org.tzi.use.uml.ocl.expr.ExpSequenceLiteral;
import org.tzi.use.uml.ocl.expr.ExpSetLiteral;
import org.tzi.use.uml.ocl.expr.ExpSortedBy;
import org.tzi.use.uml.ocl.expr.ExpStdOp;
import org.tzi.use.uml.ocl.expr.ExpTupleLiteral;
import org.tzi.use.uml.ocl.expr.ExpTupleSelectOp;
import org.tzi.use.uml.ocl.expr.ExpUndefined;
import org.tzi.use.uml.ocl.expr.ExpVariable;
import org.tzi.use.uml.ocl.expr.Expression;
import org.tzi.use.uml.ocl.expr.ExpressionVisitor;
import org.tzi.use.uml.ocl.expr.ExpressionWithValue;
import org.tzi.use.uml.ocl.expr.VarDecl;
import org.tzi.use.uml.ocl.expr.VarDeclList;
import org.tzi.use.uml.ocl.type.EnumType;
import org.tzi.use.uml.ocl.type.Type;

/**
 * This is the Expression Visitor Class. It paints the visited classes of a
 * given OCL expression.
 * 
 * @author Maria Sales
 */
public class HighlightExpressionVisitor implements ExpressionVisitor {

	private ClassDiagramData classDiagramData;
	private OCLHighlightConfigDialog configDialog;

	public HighlightExpressionVisitor(ClassDiagramData classDiagramData, OCLHighlightConfigDialog configDialog) {
		this.classDiagramData = classDiagramData;
		this.configDialog = configDialog;
		resetDiagramColor();
	}

	private void resetDiagramColor() {
		classDiagramData.fClassToNodeMap.values().forEach(this::resetClassNodeColor);
		classDiagramData.fEnumToNodeMap.values().forEach(this::resetEnumNodeColor);
		classDiagramData.getAllRolenames().stream().forEach(this::resetRolenameColor);
		classDiagramData.fAssocClassToEdgeMap.values().forEach(this::resetEdgeColor);
		classDiagramData.fBinaryAssocToEdgeMap.values().forEach(this::resetEdgeColor);
		classDiagramData.fGenToGeneralizationEdge.values().forEach(this::resetEdgeColor);
	}

	private void resetClassNodeColor(ClassNode node) {
		node.setColor(Color.WHITE);
		node.resetAttributeColor();
		node.resetOperationColor();
	}

	private void resetEnumNodeColor(EnumNode enumNode) {
		colorizeEnumNode(enumNode, Color.WHITE);
	}

	private void resetRolenameColor(Rolename rolename) {
		rolename.setColor(null);
	}

	private void resetEdgeColor(EdgeBase edge) {
		try {
			Field fOptField = EdgeBase.class.getDeclaredField("fOpt");
			fOptField.setAccessible(true);

			DiagramOptions fOpt = (DiagramOptions) fOptField.get(edge);
			fOpt.registerTypeColor(DiagramOptions.EDGE_COLOR, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
			fOptField.set(edge, fOpt);

			if (edge instanceof BinaryAssociationClassOrObject) {
				Field associationClassEdgeField = BinaryAssociationClassOrObject.class
						.getDeclaredField("associationClassEdge");
				associationClassEdgeField.setAccessible(true);

				SimpleEdge simpleEdge = (SimpleEdge) associationClassEdgeField.get(edge);
				simpleEdge.setColor(Color.LIGHT_GRAY);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void colorizeClass(MClass visitedClass) {
		ClassNode classNode = classDiagramData.fClassToNodeMap.get(visitedClass);
		if (!classNode.getColor().equals(configDialog.getClassColor())) {
			classNode.setColor(configDialog.getClassColor());
			visitedClass.attributes().forEach(attribute -> {
				try {

					// Paint white attributes
					Field fAttrColorsField = ClassNode.class.getDeclaredField("fAttrColors");
					fAttrColorsField.setAccessible(true);
					Color[] fAttrColors = (Color[]) fAttrColorsField.get(classNode);

					Field fAttributesField = ClassNode.class.getDeclaredField("fAttributes");
					fAttributesField.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<MAttribute> fAttributes = (List<MAttribute>) fAttributesField.get(classNode);

					if (fAttrColors[fAttributes.indexOf(attribute)] != configDialog.getAttributeColor()) {
						classNode.setAttributeColor(attribute, Color.WHITE);
					}

				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
			});

			visitedClass.operations().forEach(operation -> {
				try {
					// Paint white operations
					Field fOperationColorsField = ClassNode.class.getDeclaredField("fOperationColors");
					fOperationColorsField.setAccessible(true);
					Color[] fOperationColors = (Color[]) fOperationColorsField.get(classNode);

					Field fOperationsField = ClassNode.class.getDeclaredField("fOperations");
					fOperationsField.setAccessible(true);
					@SuppressWarnings("unchecked")
					List<MOperation> fOperations = (List<MOperation>) fOperationsField.get(classNode);

					if (fOperationColors[fOperations.indexOf(operation)] != configDialog.getAttributeColor()) {
						classNode.setOperationColor(operation, Color.WHITE);
					}

				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public void visitAllInstances(ExpAllInstances exp) {
		if (exp.getSourceType().isTypeOfClass()) {
			colorizeClass((MClass) exp.getSourceType());
		}
	}

	@Override
	public void visitAny(ExpAny exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitAsType(ExpAsType exp) {
		exp.getSourceExpr().processWithVisitor(this);
		visitType(exp.getTargetType());

		if (exp.getSourceExpr().type() instanceof MClass && exp.getTargetType() instanceof MClass) {
			colorizeGeneralization((MClass) exp.getSourceExpr().type(), (MClass) exp.getTargetType());
		}
	}

	@Override
	public void visitAttrOp(ExpAttrOp exp) {
		exp.objExp().processWithVisitor(this);
		colorizeClass(exp.attr().owner());
		colorizeAttribute(exp.attr());

		// Colorize generalization
		if (exp.objExp().type().isTypeOfClass()) {
			if (!exp.attr().owner().equals(exp.objExp().type())) {
				colorizeClass((MClass) exp.objExp().type());
				colorizeGeneralization((MClass) exp.objExp().type(), exp.attr().owner());
			}
		}
	}

	private void colorizeGeneralization(MClass source, MClass destination) {
		EdgeBase edgeBase = null;

		Optional<GeneralizationEdge> fGenToGeneralizationEdge = classDiagramData.fGenToGeneralizationEdge.values()
				.stream().map(edge -> (GeneralizationEdge) edge)
				.filter(edge -> generalizationSourceAndDestinationMatch(source, destination, edge)).findFirst();

		if (fGenToGeneralizationEdge.isPresent()) {
			edgeBase = fGenToGeneralizationEdge.get();
		}

		if (edgeBase != null) {
			try {
				Field fOptField = EdgeBase.class.getDeclaredField("fOpt");
				fOptField.setAccessible(true);

				ClassDiagramOptions newOptions = clone((ClassDiagramOptions) fOptField.get(edgeBase));
				newOptions.registerTypeColor(DiagramOptions.EDGE_COLOR, configDialog.getEdgeColor(), configDialog.getEdgeColor());
				fOptField.set(edgeBase, newOptions);

			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean generalizationSourceAndDestinationMatch(MClass sourceClass, MClass destinationClass,
			GeneralizationEdge edge) {
		return (edge.source().name().equals(sourceClass.name()) && edge.target().name().equals(destinationClass.name()))
				|| (edge.source().name().equals(destinationClass.name())
						&& edge.target().name().equals(sourceClass.name()));
	}

	private void colorizeAttribute(MAttribute visitedAttribute) {
		classDiagramData.fClassToNodeMap.get(visitedAttribute.owner()).setAttributeColor(visitedAttribute,
				configDialog.getAttributeColor());
	}

	@Override
	public void visitBagLiteral(ExpBagLiteral exp) {
		// Noop
	}

	@Override
	public void visitCollect(ExpCollect exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitCollectNested(ExpCollectNested exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitConstBoolean(ExpConstBoolean exp) {
		// Noop
	}

	@Override
	public void visitConstEnum(ExpConstEnum exp) {
		if (exp.type().isTypeOfEnum()) {
			colorizeEnumType((EnumType) exp.type(), configDialog.getEnumColor());
		}
	}

	private void colorizeEnumType(EnumType enumType, Color color) {
		Optional<EnumNode> node = classDiagramData.fEnumToNodeMap.values().stream()
				.filter(value -> value.getEnum().equals(enumType)).findFirst();
		if (node.isPresent()) {
			colorizeEnumNode(node.get(), color);
		}
	}

	private void colorizeEnumNode(EnumNode enumNode, Color color) {
		try {
			Field fOptField = ClassifierNode.class.getDeclaredField("fOpt");
			fOptField.setAccessible(true);
			ClassDiagramOptions newOptions = clone((ClassDiagramOptions) fOptField.get(enumNode));
			newOptions.registerTypeColor(DiagramOptions.NODE_COLOR, color, color);
			fOptField.set(enumNode, newOptions);
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void visitConstInteger(ExpConstInteger exp) {
		// Noop
	}

	@Override
	public void visitConstReal(ExpConstReal exp) {
		// Noop
	}

	@Override
	public void visitConstString(ExpConstString exp) {
		// Noop
	}

	@Override
	public void visitEmptyCollection(ExpEmptyCollection exp) {
		// Noop
	}

	@Override
	public void visitExists(ExpExists exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitForAll(ExpForAll exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitIf(ExpIf exp) {
		exp.getCondition().processWithVisitor(this);
		exp.getElseExpression().processWithVisitor(this);
		exp.getThenExpression().processWithVisitor(this);
	}

	@Override
	public void visitIsKindOf(ExpIsKindOf exp) {
		exp.getSourceExpr().processWithVisitor(this);
		visitType(exp.getTargetType());

		if (exp.getSourceExpr().type() instanceof MClass && exp.getTargetType() instanceof MClass) {
			colorizeGeneralization((MClass) exp.getSourceExpr().type(), (MClass) exp.getTargetType());
		}
	}

	@Override
	public void visitIsTypeOf(ExpIsTypeOf exp) {
		exp.getSourceExpr().processWithVisitor(this);
		visitType(exp.getTargetType());

		if (exp.getSourceExpr().type() instanceof MClass && exp.getTargetType() instanceof MClass) {
			colorizeGeneralization((MClass) exp.getSourceExpr().type(), (MClass) exp.getTargetType());
		}
	}

	@Override
	public void visitIsUnique(ExpIsUnique exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitIterate(ExpIterate exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitLet(ExpLet exp) {
		exp.getInExpression().processWithVisitor(this);
		exp.getVarExpression().processWithVisitor(this);
	}

	@Override
	public void visitNavigation(ExpNavigation exp) {
		MClass sourceClass = exp.getSource().cls();
		colorizeClass(sourceClass);

		MClass destinationClass = exp.getDestination().cls();
		colorizeClass(destinationClass);

		String associationName = null;
		if (exp.getDestination() instanceof MAssociationEnd) {
			MAssociationEnd associationEnd = (MAssociationEnd) exp.getDestination();
			String navigationEnd = associationEnd.name();
			associationName = associationEnd.association().name();
			colorizeRolename(navigationEnd, associationName);
		}

		colorizeNavigation(sourceClass, destinationClass, associationName);

		exp.getObjectExpression().processWithVisitor(this);

		// Colorize generalization
		if (exp.getObjectExpression() instanceof ExpNavigation) {
			ExpNavigation objNavigation = (ExpNavigation) exp.getObjectExpression();
			if (objNavigation.type().isTypeOfClass()) {
				MClass classNavigation = (MClass) objNavigation.type();
				if (!classNavigation.equals(sourceClass)) {
					colorizeGeneralization(classNavigation, sourceClass);
				}
			}
		} else if (exp.getObjectExpression() instanceof ExpVariable) {
			ExpVariable objVariable = (ExpVariable) exp.getObjectExpression();
			if (objVariable.type().isTypeOfClass()) {
				MClass classNavigation = (MClass) objVariable.type();
				if (!classNavigation.equals(sourceClass)) {
					colorizeGeneralization(classNavigation, sourceClass);
				}
			}
		}

	}

	private void colorizeRolename(String rolename, String associationName) {
		Optional<Rolename> classDiagramRolename = classDiagramData.getAllRolenames().stream()
				.filter(role -> role.getEnd().name().equals(rolename))
				.filter(role -> role.getAssociation().name().equals(associationName)).findFirst();

		if (classDiagramRolename.isPresent()) {
			classDiagramRolename.get().setColor(configDialog.getRolenameColor());
		}
	}

	private void colorizeNavigation(MClass sourceClass, MClass destinationClass, String associationName) {

		EdgeBase edgeBase = null;

		Optional<BinaryAssociationClassOrObject> fAssocClassToEdge = classDiagramData.fAssocClassToEdgeMap.values()
				.stream().filter(association -> association instanceof BinaryAssociationClassOrObject)
				.map(association -> (BinaryAssociationClassOrObject) association)
				.filter(association -> sourceAndDestinationMatch(sourceClass, destinationClass, association)
						|| associationMatches(sourceClass, destinationClass, association))
				.findFirst();

		if (fAssocClassToEdge.isPresent()) {
			edgeBase = fAssocClassToEdge.get();
		} else {
			Optional<BinaryAssociationOrLinkEdge> binaryAssociationOrLinkEdge = classDiagramData.fBinaryAssocToEdgeMap
					.values().stream()
					.filter(association -> (association.source().name().equals(sourceClass.name())
							&& association.target().name().equals(destinationClass.name()))
							|| (association.source().name().equals(destinationClass.name())
									&& association.target().name().equals(sourceClass.name())))
					.filter(association -> associationName != null
							? association.getAssocName().name().equalsIgnoreCase(associationName)
							: true)
					.findFirst();

			if (binaryAssociationOrLinkEdge.isPresent()) {
				edgeBase = binaryAssociationOrLinkEdge.get();
			}
		}

		if (edgeBase != null) {
			try {
				Field fOptField = EdgeBase.class.getDeclaredField("fOpt");
				fOptField.setAccessible(true);

				ClassDiagramOptions newOptions = clone((ClassDiagramOptions) fOptField.get(edgeBase));
				newOptions.registerTypeColor(DiagramOptions.EDGE_COLOR, configDialog.getEdgeColor(), configDialog.getEdgeColor());
				fOptField.set(edgeBase, newOptions);

				if (edgeBase instanceof BinaryAssociationClassOrObject && associationMatches(sourceClass,
						destinationClass, (BinaryAssociationClassOrObject) edgeBase)) {
					Field associationClassEdgeField = BinaryAssociationClassOrObject.class
							.getDeclaredField("associationClassEdge");
					associationClassEdgeField.setAccessible(true);

					SimpleEdge simpleEdge = (SimpleEdge) associationClassEdgeField.get(edgeBase);
					simpleEdge.setColor(configDialog.getEdgeColor());

				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean sourceAndDestinationMatch(MClass sourceClass, MClass destinationClass,
			BinaryAssociationClassOrObject association) {
		return (association.source().name().equals(sourceClass.name())
				&& association.target().name().equals(destinationClass.name()))
				|| (association.source().name().equals(destinationClass.name())
						&& association.target().name().equals(sourceClass.name()));
	}

	private boolean associationMatches(MClass sourceClass, MClass destinationClass,
			BinaryAssociationClassOrObject association) {
		return (association.getAssociation().name().equals(sourceClass.name())
				&& (association.source().name().equals(destinationClass.name())
						|| association.target().name().equals(destinationClass.name())))
				|| (association.getAssociation().name().equals(destinationClass.name())
						&& (association.source().name().equals(sourceClass.name())
								|| association.target().name().equals(sourceClass.name())));
	}

	private ClassDiagramOptions clone(ClassDiagramOptions oldOptions) {
		ClassDiagramOptions newOptions = new ClassDiagramOptions();
		newOptions.setDoAutoLayout(oldOptions.isDoAutoLayout());
		newOptions.setShowRolenames(oldOptions.isShowRolenames());
		newOptions.setShowAssocNames(oldOptions.isShowAssocNames());
		newOptions.setDoAntiAliasing(oldOptions.isDoAntiAliasing());
		newOptions.setShowMutliplicities(oldOptions.isShowMutliplicities());
		newOptions.setShowAttributes(oldOptions.isShowAttributes());
		newOptions.setShowOperations(oldOptions.isShowOperations());
		newOptions.setGroupMR(oldOptions.isGroupMR());
		newOptions.setShowGrid(oldOptions.showGrid());
		newOptions.setGrayscale(oldOptions.grayscale());

		newOptions.setShowUnionConstraints(oldOptions.isShowUnionConstraints());
		newOptions.setShowSubsetsConstraints(oldOptions.isShowSubsetsConstraints());
		newOptions.setShowRedefinesConstraints(oldOptions.isShowRedefinesConstraints());

		newOptions.setModelFileName(oldOptions.getModelFileName());

		newOptions.registerTypeColor(DiagramOptions.NODE_COLOR, new Color(0xff, 0xf8, 0xb4),
				new Color(0xF0, 0xF0, 0xF0));
		newOptions.registerTypeColor(DiagramOptions.NODE_SELECTED_COLOR, Color.orange, new Color(0xD0, 0xD0, 0xD0));
		newOptions.registerTypeColor(DiagramOptions.NODE_FRAME_COLOR, Color.blue, Color.BLACK);
		newOptions.registerTypeColor(DiagramOptions.NODE_LABEL_COLOR, Color.black, Color.BLACK);
		newOptions.registerTypeColor(DiagramOptions.DIAMONDNODE_COLOR, Color.white, Color.WHITE);
		newOptions.registerTypeColor(DiagramOptions.DIAMONDNODE_FRAME_COLOR, Color.black, Color.BLACK);
		newOptions.registerTypeColor(DiagramOptions.EDGE_COLOR, Color.BLACK, Color.BLACK);
		newOptions.registerTypeColor(DiagramOptions.EDGE_LABEL_COLOR, Color.black, Color.BLACK);
		newOptions.registerTypeColor(DiagramOptions.EDGE_SELECTED_COLOR, Color.ORANGE, new Color(0x50, 0x50, 0x50));

		return newOptions;
	}

	@Override
	public void visitObjAsSet(ExpObjAsSet exp) {
		// Noop
	}

	@Override
	public void visitObjOp(ExpObjOp exp) {
		MOperation operation = exp.getOperation();
		MClass operationClass = operation.cls();
		colorizeOperation(operationClass, operation);

		List<Expression> arguments = Arrays.asList(exp.getArguments());
		arguments.forEach(argument -> {
			argument.processWithVisitor(this);
		});
	}

	private void colorizeOperation(MClass visitedOperationClass, MOperation visitedOperation) {
		classDiagramData.fClassToNodeMap.get(visitedOperationClass).setOperationColor(visitedOperation,
				configDialog.getOperationColor());
	}

	@Override
	public void visitObjRef(ExpObjRef exp) {
		// Noop
	}

	@Override
	public void visitOne(ExpOne exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitOrderedSetLiteral(ExpOrderedSetLiteral exp) {
		// Noop
	}

	@Override
	public void visitQuery(ExpQuery exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitReject(ExpReject exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitWithValue(ExpressionWithValue exp) {
		// Noop
	}

	@Override
	public void visitSelect(ExpSelect exp) {
		visitCollectionOperation(exp);
	}

	private void visitCollectionOperation(ExpQuery exp) {
		exp.getQueryExpression().processWithVisitor(this);
		exp.getRangeExpression().processWithVisitor(this);
	}

	@Override
	public void visitSequenceLiteral(ExpSequenceLiteral exp) {
		// Noop
	}

	@Override
	public void visitSetLiteral(ExpSetLiteral exp) {
		// Noop
	}

	@Override
	public void visitSortedBy(ExpSortedBy exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitStdOp(ExpStdOp exp) {
		Expression[] args = exp.args();

		if (exp.getOperation().isInfixOrPrefix()) {
			if (args.length == 1) {
				args[0].processWithVisitor(this);
			} else { // Infix has two arguments
				args[0].processWithVisitor(this);
				args[1].processWithVisitor(this);
			}
		} else {
			if (args.length == 0) {
			} else {
				args[0].processWithVisitor(this);
				if (args.length > 1) {
					for (int i = 1; i < args.length; i++) {
						if (i > 1) {
						}
						args[i].processWithVisitor(this);
					}
				}
			}
		}
	}

	@Override
	public void visitTupleLiteral(ExpTupleLiteral exp) {
		// Noop
	}

	@Override
	public void visitTupleSelectOp(ExpTupleSelectOp exp) {
		// Noop
	}

	@Override
	public void visitUndefined(ExpUndefined exp) {
		// Noop
	}

	@Override
	public void visitVariable(ExpVariable exp) {
		visitType(exp.type());
	}

	private void visitType(Type type) {
		if (type instanceof MClass) {
			colorizeClass((MClass) type);
		}
	}

	@Override
	public void visitClosure(ExpClosure exp) {
		visitCollectionOperation(exp);
	}

	@Override
	public void visitOclInState(ExpOclInState exp) {
		// Noop
	}

	@Override
	public void visitVarDeclList(VarDeclList varDeclList) {
		// Noop
	}

	@Override
	public void visitVarDecl(VarDecl varDecl) {
		// Noop
	}

	@Override
	public void visitObjectByUseId(ExpObjectByUseId exp) {
		// Noop
	}

	@Override
	public void visitConstUnlimitedNatural(ExpConstUnlimitedNatural exp) {
		// Noop
	}

	@Override
	public void visitSelectByKind(ExpSelectByKind exp) {
		// Noop
	}

	@Override
	public void visitExpSelectByType(ExpSelectByType exp) {
		// Noop
	}

	@Override
	public void visitRange(ExpRange exp) {
		// Noop
	}

	@Override
	public void visitNavigationClassifierSource(ExpNavigationClassifierSource exp) {
		exp.getObjectExpression().processWithVisitor(this);

		MClass destinationClass = exp.getDestination().cls();
		colorizeClass(destinationClass);

		String associationName = null;
		if (exp.getDestination() instanceof MAssociationEnd) {
			MAssociationEnd associationEnd = (MAssociationEnd) exp.getDestination();
			String navigationEnd = associationEnd.name();
			associationName = associationEnd.association().name();
			colorizeRolename(navigationEnd, associationName);
		}

		if (exp.getObjectExpression().type() instanceof MClass) {
			MClass sourceClass = (MClass) exp.getObjectExpression().type();
			colorizeNavigation(sourceClass, destinationClass, associationName);

		}

		// Colorize generalization
		if (exp.getObjectExpression() instanceof ExpNavigation) {
			ExpNavigation objNavigation = (ExpNavigation) exp.getObjectExpression();
			if (objNavigation.type().isTypeOfClass()) {
				MClass classNavigation = (MClass) objNavigation.type();
				if (!classNavigation.equals(destinationClass)) {
					colorizeGeneralization(classNavigation, destinationClass);
				}
			}
		} else if (exp.getObjectExpression() instanceof ExpVariable) {
			ExpVariable objVariable = (ExpVariable) exp.getObjectExpression();
			if (objVariable.type().isTypeOfClass()) {
				MClass classNavigation = (MClass) objVariable.type();
				if (!classNavigation.equals(destinationClass)) {
					colorizeGeneralization(classNavigation, destinationClass);
				}
			}
		}
	}

}