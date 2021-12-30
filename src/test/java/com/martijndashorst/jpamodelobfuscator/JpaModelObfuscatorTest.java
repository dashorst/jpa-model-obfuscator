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

public class JpaModelObfuscatorTest {
	@Test
	public void stripsAllMethodsGeneratesGettersSettersOneField() {
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
				"",
				"    public String getField() {",
				"        return field;",
				"    }",
				"",
				"    public void setField(String field) {",
				"        this.field = field;",
				"    }",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		JpaModelObfuscator obfuscator = new JpaModelObfuscator("com.example", false);
		List<Result> results = obfuscator.run(compilationUnits);

		SourceFile result = results.get(0).getAfter();
		String resultingSource = result.print(new Cursor(null, null));
		
		Assertions.assertEquals(expected, resultingSource);
	}

	@Test
	public void stripsAllMethodsGeneratesGettersSettersTwoFields() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example;",
				"",
				"import jakarta.persistence.Entity;", 
				"", 
				"@Entity", 
				"public class MyClass {", 
				"    private String field1;",
				"",
				"    private Integer field2;",
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
				"    private String field1;",
				"",
				"    private Integer field2;",
				"",
				"    public String getField1() {",
				"        return field1;",
				"    }",
				"",
				"    public void setField1(String field1) {",
				"        this.field1 = field1;",
				"    }",
				"",
				"    public Integer getField2() {",
				"        return field2;",
				"    }",
				"",
				"    public void setField2(Integer field2) {",
				"        this.field2 = field2;",
				"    }",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		JpaModelObfuscator obfuscator = new JpaModelObfuscator("com.example", false);
		List<Result> results = obfuscator.run(compilationUnits);

		SourceFile result = results.get(0).getAfter();
		String resultingSource = result.print(new Cursor(null, null));
		
		Assertions.assertEquals(expected, resultingSource);
	}

	@Test
	public void stripsAllMethodsGeneratesGettersSettersFourFieldsInclPrimitive() {
		//@formatter:off
		String input = Arrays.asList( //
				"package com.example;",
				"",
				"import java.io.Serializable;",
				"",
				"import jakarta.persistence.Entity;", 
				"import jakarta.persistence.Column;", 
				"", 
				"@Entity", 
				"public class MyClass implements Serializable {", 
				"    private static final long serialVersionUID = 1L;",
				"", 
				"    private String field1;",
				"",
				"    private boolean field2;",
				"",
				"    private Boolean field3;",
				"",
				"    public MyClass() {}",
				"",
				"    public String getField() { return field; }",
				"    public void setField(String f) { field = f; }",
				"    public String toString() { return field; }",
				"    private boolean isEmpty() { return field.isEmpty(); }",
				"",
				"    @Column",
				"    private Integer field4;",
				"}"
				).stream().collect(Collectors.joining("\n"));

		String expected = Arrays.asList( //
				"package com.example;",
				"",
				"import java.io.Serializable;",
				"",
				"import jakarta.persistence.Entity;", 
				"import jakarta.persistence.Column;", 
				"", 
				"@Entity", 
				"public class MyClass implements Serializable {", 
				"    private static final long serialVersionUID = 1L;",
				"", 
				"    private String field1;",
				"", 
				"    private boolean field2;",
				"", 
				"    private Boolean field3;",
				"", 
				"    @Column",
				"    private Integer field4;",
				"",
				"    public String getField1() {",
				"        return field1;",
				"    }",
				"",
				"    public void setField1(String field1) {",
				"        this.field1 = field1;",
				"    }",
				"",
				"    public boolean isField2() {",
				"        return field2;",
				"    }",
				"",
				"    public void setField2(boolean field2) {",
				"        this.field2 = field2;",
				"    }",
				"",
				"    public Boolean getField3() {",
				"        return field3;",
				"    }",
				"",
				"    public void setField3(Boolean field3) {",
				"        this.field3 = field3;",
				"    }",
				"",
				"    public Integer getField4() {",
				"        return field4;",
				"    }",
				"",
				"    public void setField4(Integer field4) {",
				"        this.field4 = field4;",
				"    }",
				"}"
				).stream().collect(Collectors.joining("\n"));
		
		JavaParser jp = JavaParser.fromJavaVersion().build();
		List<CompilationUnit> compilationUnits = jp.parse(input);

		JpaModelObfuscator obfuscator = new JpaModelObfuscator("com.example", false);
		List<Result> results = obfuscator.run(compilationUnits);

		SourceFile result = results.get(0).getAfter();
		String resultingSource = result.print(new Cursor(null, null));
		
		Assertions.assertEquals(expected, resultingSource);
	}
}
