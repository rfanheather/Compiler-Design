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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.SimpleParser.SyntaxException;

public class SimpleParserTest {

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
	private SimpleParser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
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
		SimpleParser parser = makeParser(input);
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c; image j;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "boy{boolean girl;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	// boolean boolean
	@Test
	public void testDec2() throws LexicalException, SyntaxException {
		String input = "boy {int float girl;}";
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "boy{image girl[true , false];}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	// StatementWrite
	public void testStatement0() throws LexicalException, SyntaxException {
		String input = "boy{write a to b;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	//  input IDENTIFIER from @ Expression
	public void testStatement1() throws LexicalException, SyntaxException {
		String input = "boy{input a from @ 3;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	//  StatementAssignment
	public void testStatement2() throws LexicalException, SyntaxException {
		String input = "StatementAssignment{green (a [1, 3]) := true;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	//  StatementWhile
	public void testStatement3() throws LexicalException, SyntaxException {
		String input = "StatementWhile{while(89) {} ;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	//  StatementIf
	public void testStatement4() throws LexicalException, SyntaxException {
		String input = "prog{if(a && b){};}";
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	@Test
	//  StatementShow
	public void testStatement5() throws LexicalException, SyntaxException {
		String input = "StatementShow{show  +-default_width | 2;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}
	
	@Test
	//  StatementSleep
	public void testStatement6() throws LexicalException, SyntaxException {
		String input = "StatementSleep{sleep  +-default_width | [2;}";
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
}
	

