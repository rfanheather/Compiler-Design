 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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
import static org.junit.Assert.assertFalse;

// import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Scanner.LexicalException;
import cop5556sp18.Scanner.Token;
import static cop5556sp18.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(pos, t.pos);
		assertEquals(length, t.length);
		assertEquals(line, t.line());
		assertEquals(pos_in_line, t.posInLine());
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}
	


	/**
	 * Simple test case with an empty program.  The only Token will be the EOF Token.
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, the end of line 
	 * character would be inserted by the text editor.
	 * Showing the input will let you check your input is 
	 * what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	

	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failIllegalChar() throws LexicalException {
		// Random r = new Random();
		// double randomValue = Float.MAX_VALUE + (Double.MAX_VALUE - Float.MAX_VALUE) * r.nextDouble();
		// String input = Double.toString(Math.abs(randomValue));
		// long randomValue = Integer.MAX_VALUE + (Long.MAX_VALUE - Integer.MAX_VALUE) * r.nextLong();
		// String input = Long.toString(Math.abs(randomValue));
		String input = "999191991991001001101010101001111";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(0,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}

	@Test
	public void testParens() throws LexicalException {
		String input = "()";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN, 0, 1, 1, 1);
		checkNext(scanner, RPAREN, 1, 1, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSeparators() throws LexicalException {
		String input = ",[]{}";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, COMMA, 0, 1, 1, 1);
		checkNext(scanner, LSQUARE, 1, 1, 1, 2);
		checkNext(scanner, RSQUARE, 2, 1, 1, 3);
		checkNext(scanner, LBRACE, 3, 1, 1, 4);
		checkNext(scanner, RBRACE, 4, 1, 1, 5);
		checkNextIsEOF(scanner);
	}

	@Test
	public void testGTAndLT() throws LexicalException {
		String input = "><\n>><<\n>=<=";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_GT, 0, 1, 1, 1);
		checkNext(scanner, OP_LT, 1, 1, 1, 2);
		checkNext(scanner, RPIXEL, 3, 2, 2, 1);
		checkNext(scanner, LPIXEL, 5, 2, 2, 3);
		checkNext(scanner, OP_GE, 8, 2, 3, 1);
		checkNext(scanner, OP_LE, 10, 2, 3, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testEXCLAMATIONandNEQ() throws LexicalException {
		String input = "!!=!";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EXCLAMATION, 0, 1, 1, 1);
		checkNext(scanner, OP_NEQ, 1, 2, 1, 2);
		checkNext(scanner, OP_EXCLAMATION, 3, 1, 1, 4);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testEQ() throws LexicalException {
		String input = "==";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testComment() throws LexicalException {
		String input = "/*ajbfyuehjs53u%*/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIdentifier() throws LexicalException {
		String input = "A$b098_\nagyc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 7, 1, 1);
		checkNext(scanner, IDENTIFIER, 8, 4, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testLiteral() throws LexicalException {
		String input = "0607";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testFPLiteral() throws LexicalException {
		String input = "000. 002.";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 1, 1, 2);
		checkNext(scanner, FLOAT_LITERAL, 2, 2, 1, 3);
		checkNext(scanner, INTEGER_LITERAL, 5, 1, 1, 6);
		checkNext(scanner, INTEGER_LITERAL, 6, 1, 1, 7);
		checkNext(scanner, FLOAT_LITERAL, 7, 2, 1, 8);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testKeyWord() throws LexicalException {
		String input = "cart_x";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_cart_x, 0, 6, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testBoolean() throws LexicalException {
		String input = "true\nfalse";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 5, 5, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	
	@Test
	public void testOutofRange() throws LexicalException {
		String input = "abc 999191991991001001101010101001111";
		//Scanner scanner = new Scanner(input).scan();
		show(input);
		//show(scanner);
		//checkNext(scanner, IDENTIFIER, 0, 3, 1, 1);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //Catch the exception
			show(e);                    //Display it
			assertEquals(4,e.getPos()); //Check that it occurred in the expected position
			throw e;                    //Rethrow exception so JUnit will see it
		}
	}
	
}
	

