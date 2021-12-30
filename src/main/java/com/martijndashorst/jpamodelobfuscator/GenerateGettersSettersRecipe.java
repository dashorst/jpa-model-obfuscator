package com.martijndashorst.jpamodelobfuscator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Modifier;
import org.openrewrite.java.tree.J.Modifier.Type;
import org.openrewrite.java.tree.J.VariableDeclarations;
import org.openrewrite.java.tree.JavaCoordinates;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.JavaType.Primitive;
import org.openrewrite.java.tree.TypeUtils;

public class GenerateGettersSettersRecipe extends Recipe {
	@Override
	public String getDisplayName() {
		return "Generate getters and setters for all fields, assumes no getters/setters are present";
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new GenerateGetterSetterVisitor<>();
	}

	static class GenerateGetterSetterVisitor<P> extends JavaIsoVisitor<P> {
		private final JavaTemplate getter = JavaTemplate
				.builder(this::getCursor, "" + "public #{} #{}() {\n" + "    return #{};\n" + "}").build();
		private final JavaTemplate setter = JavaTemplate
				.builder(this::getCursor, "" + "public void set#{}(#{} #{}) {\n" + "    this.#{} = #{};\n" + "}")
				.build();

		@Override
		public VariableDeclarations visitVariableDeclarations(VariableDeclarations multiVariable, P p) {
			return super.visitVariableDeclarations(multiVariable, p);
		}

		@Override
		public J.VariableDeclarations.NamedVariable visitVariable(J.VariableDeclarations.NamedVariable variable, P p) {
			if (variable.isField(getCursor())) {
				VariableDeclarations variableDeclarations = getCursor().firstEnclosing(VariableDeclarations.class);

				List<Modifier> modifiers = variableDeclarations.getModifiers();

				if (modifiers.stream().map(Modifier::getType).anyMatch(t -> t == Type.Static))
					return super.visitVariable(variable, p);

				Queue<Cursor> cursors = getCursor().getNearestMessage("varCursor");
				if (cursors == null)
					cursors = new LinkedList<>();
				cursors.add(getCursor());
				getCursor().putMessageOnFirstEnclosing(J.ClassDeclaration.class, "varCursor", cursors);
			}
			return super.visitVariable(variable, p);
		}

		@Override
		public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, P p) {
			J.ClassDeclaration c = super.visitClassDeclaration(classDecl, p);

			Queue<Cursor> varCursors = getCursor().pollNearestMessage("varCursor");
			if (varCursors == null)
				varCursors = new LinkedList<>();

			for (Cursor varCursor : varCursors) {
				J.VariableDeclarations.NamedVariable var = varCursor.getValue();

				VariableDeclarations variableDeclarations = varCursor.firstEnclosing(VariableDeclarations.class);

				List<Modifier.Type> modifiers = variableDeclarations.getModifiers().stream().map(Modifier::getType)
						.collect(Collectors.toList());

				if (modifiers.contains(Type.Static))
					continue;

				JavaType.FullyQualified fullyQualified = TypeUtils.asFullyQualified(var.getType());
				Primitive primitive = TypeUtils.asPrimitive(var.getType());

				String getterPrefix = "get";
				String fieldType = null;

				if (primitive != null) {
					getterPrefix = primitive == Primitive.Boolean ? "is" : "get";
					fieldType = primitive.getKeyword();
				} else if (fullyQualified != null) {
					fieldType = fullyQualified.getClassName();
				}

				String varName = var.getSimpleName();
				String methodName = getterPrefix + StringUtils.capitalize(varName);

				JavaCoordinates lastStatement = c.getBody().getCoordinates().lastStatement();
				c = c.withTemplate(getter, lastStatement, fieldType, methodName, varName);

				if (!modifiers.contains(Type.Final))
					c = c.withTemplate(setter, lastStatement, StringUtils.capitalize(varName), fieldType, varName,
							varName, varName);
			}
			return c;
		}
	}
}
