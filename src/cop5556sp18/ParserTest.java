 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Parser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;
import static cop5556sp18.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private Parser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		Parser parser = new Parser(scanner);
		return parser;
	}
	

	
	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		@SuppressWarnings("unused")
		Program p = parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);
		assertEquals("b", p.progName);
		assertEquals(0, p.block.decsOrStatements.size());
	}	
	
	
	/**
	 * Checks that an element in a block is a declaration with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param type
	 * @param name
	 * @return
	 */
	Declaration checkDec(Block block, int index, Kind type,
			String name) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(Declaration.class, node.getClass());
		Declaration dec = (Declaration) node;
		assertEquals(type, dec.type);
		assertEquals(name, dec.name);
		return dec;
	}	
	
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		checkDec(p.block, 0, Kind.KW_int, "c");
		checkDec(p.block, 1, Kind.KW_image, "j");
	}
	
	/**
	 * Checks that an element in a block is a write statement with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param source
	 * @param dest
	 * @return
	 */
	Statement checkStatementWrite(Block block, int index, String source, String dest) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(StatementWrite.class, node.getClass());
		StatementWrite st = (StatementWrite) node;
		assertEquals(source, st.sourceName);
		assertEquals(dest, st.destName);
		return st;
	}	
	
	@Test
	public void testStWr() throws LexicalException, SyntaxException {
		String input = "a{write yes to offer; write no to fail;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		assertEquals("a", p.progName);
		checkStatementWrite(p.block, 0, "yes", "offer");
		checkStatementWrite(p.block, 1, "no", "fail");
	}
	
	/**
	 * Checks that an element in a block is a write statement with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param source
	 * @param dest
	 * @return
	 */
	Statement checkStatementInput(Block block, int index, String dest) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(StatementInput.class, node.getClass());
		StatementInput st = (StatementInput) node;
		assertEquals(dest, st.destName);
		//assertEquals(e, st.e);
		return st;
	}	
	
	@Test
	public void testStInput() throws LexicalException, SyntaxException {
		String input = "a{input c from @ x + 2;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		assertEquals("a", p.progName);
		checkStatementInput(p.block, 0, "c");
		//checkStatementWrite(p.block, 1, "no", "fail");
	}
	
	/**
	 * Checks that an element in a block is an assignment statement with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param lhs
	 * @return
	 */
	Statement checkStatementAssign0(Block block, int index, String lhs) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(StatementAssign.class, node.getClass());
		StatementAssign st = (StatementAssign) node;
		LHSIdent l = (LHSIdent) st.lhs;
		assertEquals(lhs, l.name);
		return st;
	}
	
	@Test
	public void testStAss0() throws LexicalException, SyntaxException {
		String input = "a{abc := x + 2;}";
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		assertEquals("a", p.progName);
		checkStatementAssign0(p.block, 0, "abc");
		//checkStatementWrite(p.block, 1, "no", "fail");
	}
	
	/**
	 * Checks that an element in a block is an assignment statement with the given type and name.
	 * The element to check is indicated by the value of index.
	 * 
	 * @param block
	 * @param index
	 * @param name
	 * @param ex
	 * @param ey
	 * @return
	 */
	Statement checkStatementAssign1(Block block, int index, String name, int ex, int ey) {
		ASTNode node = block.decOrStatement(index);
		assertEquals(StatementAssign.class, node.getClass());
		StatementAssign st = (StatementAssign) node;
		LHSPixel l = (LHSPixel) st.lhs;
		assertEquals(name, l.name);
		checkExpIntegerLiteral(ex, l.pixelSelector.ex);
		checkExpIntegerLiteral(ey, l.pixelSelector.ey);
		return st;
	}
	
	void checkExpIntegerLiteral(int n, Expression e) {
		assertEquals(ExpressionIntegerLiteral.class, e.getClass());
		ExpressionIntegerLiteral a = (ExpressionIntegerLiteral) e;
		assertEquals(n, a.value);
	}
	
	@Test
	public void testStAss1() throws LexicalException, SyntaxException {
		String input = "a{a)bc[1, 3] := x + 2; bob[0, 2] := x + 2;}";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Program p = parser.parse();
		show(p);	
		assertEquals("a", p.progName);
		checkStatementAssign1(p.block, 0, "abc", 1, 3);
		checkStatementAssign1(p.block, 1, "bob", 0, 2);
	}
	
	/** This test illustrates how you can test specific grammar elements by themselves by
	 * calling the corresponding parser method directly, instead of calling parse.
	 * This requires that the methods are visible (not private). 
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	
	@Test
	public void testExpression() throws LexicalException, SyntaxException {
		String input = "x + 2";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}
	
	@Test
	public void testExpression1() throws LexicalException, SyntaxException {
		String input = "1 ? 8: 687";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionConditional.class, e.getClass());
		ExpressionConditional b = (ExpressionConditional) e;
		assertEquals(ExpressionIntegerLiteral.class, b.guard.getClass());
		ExpressionIntegerLiteral guard = (ExpressionIntegerLiteral)b.guard;
		assertEquals(1, guard.value);
		assertEquals(ExpressionIntegerLiteral.class, b.trueExpression.getClass());
		ExpressionIntegerLiteral trueEx = (ExpressionIntegerLiteral)b.trueExpression;
		assertEquals(8, trueEx.value);
		assertEquals(ExpressionIntegerLiteral.class, b.falseExpression.getClass());
		ExpressionIntegerLiteral falseEx = (ExpressionIntegerLiteral)b.falseExpression;
		assertEquals(687, falseEx.value);
	}
	
	@Test
	public void testExpression2() throws LexicalException, SyntaxException {
		String input = "x && 2";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary)e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(2, right.value);
		assertEquals(OP_PLUS, b.op);
	}
	
	@Test
	public void testExpression3() throws LexicalException, SyntaxException {
		String input = "sin [x,()]";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
	}
	
	@Test
	public void testExpression4() throws LexicalException, SyntaxException {
		String input = "<<1,2,(),4>>";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
	}
	
	@Test
	public void testExpression5() throws LexicalException, SyntaxException {
		String input = "<<1,2,3,4,5>>";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
	}
	
	@Test
	public void testExpression6() throws LexicalException, SyntaxException {
		String input = "a[1,()]";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
	}
	
	@Test
	public void testExpression7() throws LexicalException, SyntaxException {
		String input = "sin x";
		thrown.expect(SyntaxException.class);
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
	}
	
	@Test
	public void testExpression8() throws LexicalException, SyntaxException {
		String input = "x/y!=(1+2)*35";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary) e;
		assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
		ExpressionBinary left = (ExpressionBinary)b.leftExpression;
		assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
		ExpressionBinary right = (ExpressionBinary)b.rightExpression;
		assertEquals(OP_NEQ, b.op);
		
		assertEquals(ExpressionIdent.class, left.leftExpression.getClass());
		ExpressionIdent leftLeft = (ExpressionIdent)left.leftExpression;
		assertEquals("x", leftLeft.name);
		assertEquals(ExpressionIdent.class, left.rightExpression.getClass());
		ExpressionIdent leftRight = (ExpressionIdent)left.rightExpression;
		assertEquals("y", leftRight.name);
		assertEquals(OP_DIV, left.op);
		
		assertEquals(ExpressionBinary.class, right.leftExpression.getClass());
		ExpressionBinary rightLeft = (ExpressionBinary)right.leftExpression;
		
		ExpressionIntegerLiteral rightLeftLeft = (ExpressionIntegerLiteral)rightLeft.leftExpression;
		assertEquals(1, rightLeftLeft.value);
		ExpressionIntegerLiteral rightLeftRight = (ExpressionIntegerLiteral)rightLeft.rightExpression;
		assertEquals(2, rightLeftRight.value);
		assertEquals(OP_PLUS, rightLeft.op);
		
		assertEquals(ExpressionIntegerLiteral.class, right.rightExpression.getClass());
		ExpressionIntegerLiteral rightRight = (ExpressionIntegerLiteral)right.rightExpression;
		assertEquals(35, rightRight.value);
		assertEquals(OP_TIMES, right.op);
	}

	@Test
	public void testExpression9() throws LexicalException, SyntaxException {
		String input = "(a/b)*(c+d)%(3)";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary) e;
		assertEquals(ExpressionBinary.class, b.leftExpression.getClass());
		ExpressionBinary left = (ExpressionBinary)b.leftExpression;
		assertEquals(ExpressionIntegerLiteral.class, b.rightExpression.getClass());
		ExpressionIntegerLiteral right = (ExpressionIntegerLiteral)b.rightExpression;
		assertEquals(3, right.value);
		assertEquals(OP_MOD, b.op);
		
		assertEquals(ExpressionBinary.class, left.leftExpression.getClass());
		ExpressionBinary leftLeft = (ExpressionBinary)left.leftExpression;
		assertEquals(ExpressionBinary.class, left.rightExpression.getClass());
		ExpressionBinary leftRight = (ExpressionBinary)left.rightExpression;
		assertEquals(OP_TIMES, left.op);
		
		assertEquals(ExpressionIdent.class, leftLeft.leftExpression.getClass());
		ExpressionIdent leftLeftLeft = (ExpressionIdent)leftLeft.leftExpression;
		assertEquals("a", leftLeftLeft.name);
		assertEquals(ExpressionIdent.class, leftLeft.rightExpression.getClass());
		ExpressionIdent leftLeftRight = (ExpressionIdent)leftLeft.rightExpression;
		assertEquals("b", leftLeftRight.name);
		assertEquals(OP_DIV, leftLeft.op);
		
		assertEquals(ExpressionIdent.class, leftRight.leftExpression.getClass());
		ExpressionIdent leftRightLeft = (ExpressionIdent)leftRight.leftExpression;
		assertEquals("c", leftRightLeft.name);
		assertEquals(ExpressionIdent.class, leftRight.rightExpression.getClass());
		ExpressionIdent leftRightRight = (ExpressionIdent)leftRight.rightExpression;
		assertEquals("d", leftRightRight.name);
		assertEquals(OP_PLUS, leftRight.op);
	}
	
	@Test
	public void testExpression10() throws LexicalException, SyntaxException {
		String input = "x==(1+2)*5";
		Parser parser = makeParser(input);
		Expression e = parser.expression();  //call expression here instead of parse
		show(e);	
		assertEquals(ExpressionBinary.class, e.getClass());
		ExpressionBinary b = (ExpressionBinary) e;
		assertEquals(ExpressionIdent.class, b.leftExpression.getClass());
		ExpressionIdent left = (ExpressionIdent)b.leftExpression;
		assertEquals("x", left.name);
		assertEquals(ExpressionBinary.class, b.rightExpression.getClass());
		ExpressionBinary right = (ExpressionBinary)b.rightExpression;
		assertEquals(OP_EQ, b.op);
		
		assertEquals(ExpressionBinary.class, right.leftExpression.getClass());
		ExpressionBinary rightLeft = (ExpressionBinary)right.leftExpression;
		assertEquals(ExpressionIntegerLiteral.class, right.rightExpression.getClass());
		ExpressionIntegerLiteral rightRight = (ExpressionIntegerLiteral)right.rightExpression;
		assertEquals(5, rightRight.value);
		assertEquals(OP_TIMES, right.op);
		
		assertEquals(ExpressionIntegerLiteral.class, rightLeft.leftExpression.getClass());
		ExpressionIntegerLiteral rightLeftLeft = (ExpressionIntegerLiteral)rightLeft.leftExpression;
		assertEquals(1, rightLeftLeft.value);
		assertEquals(ExpressionIntegerLiteral.class, rightLeft.rightExpression.getClass());
		ExpressionIntegerLiteral rightLeftRight = (ExpressionIntegerLiteral)rightLeft.rightExpression;
		assertEquals(2, rightLeftRight.value);
		assertEquals(OP_PLUS, rightLeft.op);
	}
}