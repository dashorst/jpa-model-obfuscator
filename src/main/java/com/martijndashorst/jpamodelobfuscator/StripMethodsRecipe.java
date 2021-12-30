/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.martijndashorst.jpamodelobfuscator;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.J.MethodDeclaration;
import org.openrewrite.java.tree.J.Package;

class StripMethodsRecipe extends Recipe {
	private String packageName;

	private boolean recursive = true;

	@Override
	public String getDisplayName() {
		return "Strip all methods";
	}

	public StripMethodsRecipe(String packageName, boolean recursive) {
		this.packageName = packageName;
		this.recursive = recursive;
	}

	@Override
	protected TreeVisitor<?, ExecutionContext> getVisitor() {
		return new RemoveMethodsVisitor<>(packageName, recursive);
	}

	static class RemoveMethodsVisitor<P> extends JavaIsoVisitor<P> {
		private String packageName;
		private boolean recursive = true;

		public RemoveMethodsVisitor(String packageName, boolean recursive) {
			this.packageName = packageName;
			this.recursive = recursive;
		}

		@Override
		public MethodDeclaration visitMethodDeclaration(MethodDeclaration method, P p) {
			MethodDeclaration result = super.visitMethodDeclaration(method, p);

			Package packageDeclaration = getCursor().firstEnclosingOrThrow(CompilationUnit.class)
					.getPackageDeclaration();
			String packageNameOfEnclosingRootClass = packageDeclaration.getExpression().printTrimmed(getCursor())
					.replaceAll("\\s", "");

			if (packageNameOfEnclosingRootClass.equals(packageName))
				return null;
			if (recursive && packageNameOfEnclosingRootClass.startsWith(packageName + "."))
				return null;
			return result;
		}
	}
}
