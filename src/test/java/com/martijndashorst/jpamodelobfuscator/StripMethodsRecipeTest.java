package com.martijndashorst.jpamodelobfuscator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openrewrite.Cursor;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.tree.J.CompilationUnit;

public class StripMethodsRecipeTest {
	@Test
	public void removesMethodsFromClass() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example;",
				"",
				"import jakarta.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"",
				"    public MyClass() {}",
				"    public String getField() { return field; }",
				"    public void setField(String f) { field = f; }",
				"    public String toString() { return field; }",
				"    private boolean isEmpty() { return field.isEmpty(); }",
				"}"
				).stream().collect(Collectors.joining("\n"));

		String expected = Arrays.asList( //
				"package com.example;",
				"",
				"import jakarta.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		StripMethodsRecipe obfuscator = new StripMethodsRecipe("com.example", false);
		List<Result> results = obfuscator.run(compilationUnits);

		SourceFile result = results.get(0).getAfter();
		String resultingSource = result.print(new Cursor(null, null));
		
		Assertions.assertEquals(expected, resultingSource);
	}
	@Test
	public void removesMethodsFromClassInSubPackage() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example.subpackage;",
				"",
				"import jakarta.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"",
				"    public MyClass() {}",
				"    public String getField() { return field; }",
				"    public void setField(String f) { field = f; }",
				"    public String toString() { return field; }",
				"    private boolean isEmpty() { return field.isEmpty(); }",
				"}"
				).stream().collect(Collectors.joining("\n"));

		String expected = Arrays.asList( //
				"package com.example.subpackage;",
				"",
				"import jakarta.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		StripMethodsRecipe obfuscator = new StripMethodsRecipe("com.example", true);
		List<Result> results = obfuscator.run(compilationUnits);

		SourceFile result = results.get(0).getAfter();
		String resultingSource = result.print(new Cursor(null, null));
		
		Assertions.assertEquals(expected, resultingSource);
	}

	@Test
	public void skipsClassFromDifferentPackageNoRecursive() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example2;",
				"",
				"import jakara.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"",
				"    public MyClass() {}",
				"    public String getField() { return field; }",
				"    public void setField(String f) { field = f; }",
				"    public String toString() { return field; }",
				"    private boolean isEmpty() { return field.isEmpty(); }",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		StripMethodsRecipe obfuscator = new StripMethodsRecipe("com.example", false);
		List<Result> results = obfuscator.run(compilationUnits);

		Assertions.assertTrue(results.isEmpty());
	}

	@Test
	public void skipsClassFromDifferentPackageRecursive() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example2;",
				"",
				"import jakara.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field;",
				"",
				"    public MyClass() {}",
				"    public String getField() { return field; }",
				"    public void setField(String f) { field = f; }",
				"    public String toString() { return field; }",
				"    private boolean isEmpty() { return field.isEmpty(); }",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		StripMethodsRecipe obfuscator = new StripMethodsRecipe("com.example", true);
		List<Result> results = obfuscator.run(compilationUnits);

		Assertions.assertTrue(results.isEmpty());
	}
}
