package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain an AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "emptyProg{}";
		typeCheck(input);
	}
	
	@Test
	public void statementShow0() throws Exception {
		String input = "prog {show 3; show 7.8;}";
		typeCheck(input);
	}
	
	@Test
	public void statementShow_fail0() throws Exception {
		String input = "prog {image ph [a, 6]; show ph;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void statementShow1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}
	
	@Test
	public void declaration() throws Exception {
		String input = "prog {int a;}";
		typeCheck(input);
	}
	
	@Test
	public void declaration_fail() throws Exception {
		String input = "prog {image ph [a, 6];}"; //error, incompatible types
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}

	@Test
	public void expression_fail() throws Exception {
		String input = "prog { show true+4; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void duplicateDec_fail() throws Exception {
		String input = "prog { int a; int a;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void statementWrite() throws Exception {
		String input = "prog {image ph [3, 6]; filename ne; write ph to ne; }"; 
		typeCheck(input);
	}
	
	@Test
	public void statementWrite_fail() throws Exception {
		String input = "prog {image ph [3, 6]; int ne; write ph to ne; }"; 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void statementInput_fail() throws Exception {
		String input = "prog {image ph [3, 6]; input ph from @ 5 / 3.6;}"; 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void statementInput() throws Exception {
		String input = "prog {filename d; input d from @ 8%5;}"; 
		typeCheck(input);
	}
	
	@Test
	public void expressionConditional() throws Exception {
		String input = "prog {int a; a := true  ?  3  :  4;}"; 
		typeCheck(input);
	}
	
	@Test
	public void expressionConditional_fail() throws Exception {
		String input = "prog {show true  ?  3  :  9.8;}"; 
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	

	@Test
	public void testStatAssign0() throws Exception {
		String input = "program{boolean a; a := false;}";
		typeCheck(input);
	}
	
	@Test
	public void testStatAssign_fail0() throws Exception {
		String input = "program{image a; a[0.7,9] := 6;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testStatAssign_fail1() throws Exception {
		String input = "program{image a; a[1,1] := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testStatAssign1() throws Exception {
		String input = "program{image a; alpha (a[7,9]) := 6;}";
		typeCheck(input);
	}
	
	@Test
	public void testStatWhile() throws Exception {
		String input = "program{int a; int b; while (a > b) {a := b;};}";
		typeCheck(input);
	}
	
	@Test
	public void testStatWhile_fail() throws Exception {
		String input = "program{int a; int b; while (a) {int b; a := b;};}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testExpBinary0() throws Exception {
		String input = "program{int a; a := 4 / 6; a := 4 ** 6; a := 4 | 6; a := 4 & 6;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpBinary1() throws Exception {
		String input = "program{boolean a; a := true|false; a := 4.3 >= 6.9; a := (3>5) >= (5 != 6);}";
		typeCheck(input);
	}
	
	@Test
	public void testExpUnary() throws Exception {
		String input = "program{boolean a; a := !true;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpPC_fail() throws Exception {
		String input = "program{int a; a := << 2.4,3,4,5 >> ;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testExpP() throws Exception {
		String input = "program{int a; image b[4,8]; a := b[4.4,5] ;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testExpFunctionAppWithPixel() throws Exception {
		String input = "program{int a; a := polar_r[3, 3];}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testExpPD() throws Exception {
		String input = "program{int a; a := Z;}";
		typeCheck(input);
	}
	
	@Test
	public void testExpFunctionAppWithExpressionArg () throws Exception {
		String input = "program{float a;image b; a := float(4.9);}";
		typeCheck(input);
	}
	
	
	@Test
	public void testinvalidStatementInput2() throws Exception {
		String input = "prog{if(true){int var;}; if(true){input var from @1;};}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void testScope3() throws Exception {
		String input = "p{int var; if(true) {float var; var := 5;}; var := 5;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
}
