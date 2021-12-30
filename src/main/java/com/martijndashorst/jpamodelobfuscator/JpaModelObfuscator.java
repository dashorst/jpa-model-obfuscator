package com.martijndashorst.jpamodelobfuscator;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.JavaSourceFile;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JpaModelObfuscator extends Recipe {
	@Option(displayName = "Package name", description = "The package name from which to strip all methods from all classes", example = "com.example.entities")
	private String packageName;

	@Nullable
	@Option(displayName = "Recursive", required = false, description = "Process also the subpackages, default true", example = "true")
	private Boolean recursive = true;

	@Override
	public String getDisplayName() {
		return "Strip all methods and generate getters and setters for all fields";
	}

	public JpaModelObfuscator(@NonNull @JsonProperty("packageName") String packageName,
			@JsonProperty("recursive") Boolean recursive) {
		this.packageName = packageName;
		this.recursive = recursive == null || recursive;

		doNext(new StripMethodsRecipe(this.packageName, this.recursive));
		doNext(new GenerateGettersSettersRecipe());
	}

	@Override
	protected JavaVisitor<ExecutionContext> getSingleSourceApplicableTest() {
		return new JavaIsoVisitor<ExecutionContext>() {
			@Override
			public JavaSourceFile visitJavaSourceFile(JavaSourceFile cu, ExecutionContext executionContext) {
				if (cu.getPackageDeclaration() != null) {
					String original = cu.getPackageDeclaration().getExpression().printTrimmed(getCursor())
							.replaceAll("\\s", "");
					if (original.equals(packageName) || (recursive && original.startsWith(packageName))) {
						return cu.withMarkers(cu.getMarkers().searchResult());
					}
				}
				doAfterVisit(new UsesType<>(packageName + ".*"));
				return cu;
			}
		};
	}
}
