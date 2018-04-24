package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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

import static cop5556sp18.Scanner.Kind.*;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;


public class SimpleParser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}
	
	/*
	 * Program ::= Identifier Block
	 */
	public void program() throws SyntaxException {
		match(IDENTIFIER);
		block();
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {/* TODO  correct this */  
			                    IDENTIFIER,
			                    KW_input,
			                    KW_write,
			                    KW_if,
			                    KW_while,
			                    KW_show,
			                    KW_sleep,
			                    KW_red, KW_green, KW_blue, KW_alpha
	                        };

	public void block() throws SyntaxException {
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
		    if (isKind(firstDec)) {
				declaration();
			} else if (isKind(firstStatement)) {
			    statement();
			}
			match(SEMI);
		}
		match(RBRACE);
	}
	
	/*
	 * Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	 */
	
	public void declaration() throws SyntaxException {
		//TODO
		if (!isKind(KW_image)) {
		    consume();
			match(IDENTIFIER);
		} else {
			consume();
			match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				consume();
				expression();
				match(COMMA);
				expression();
				match(RSQUARE);
			}
		}
	}
	
	Kind[] firstExp = {OP_PLUS, OP_MINUS, OP_EXCLAMATION, INTEGER_LITERAL,  
			           BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER, 
			           KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, 
			           KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,
			           KW_blue, KW_alpha, KW_Z, KW_default_height, KW_default_width,
			           LPIXEL};
	Kind[] followExp = {LSQUARE};
	
	public void expression() throws SyntaxException{
		// TODO Auto-generated method stub
		try {
			orExpression();
			if (isKind(OP_QUESTION)) {
			    consume();
			    expression();
		     	match(OP_COLON);
		     	expression();
		    }
		} catch (SyntaxException e) {
			throw e;
		}
	}
	
	private void orExpression() throws SyntaxException{
		// TODO Auto-generated method stub
		try {
			andExpression();
			while (isKind(OP_OR)) {
				consume();
				if (isKind(OP_OR)) {
					error(" || is illegal");
				}
				andExpression();
			}
		} catch (SyntaxException e) {
			throw e;
		}
	}


	private void andExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			eqExpression();
			while (isKind(OP_AND)) {
				consume();
				if (isKind(OP_AND)) {
					error(" && is illegal");
				}
				eqExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(OP_AND)) {
					error(" && is illegal");
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}


	private void eqExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			relExpression();
			while (isKind(OP_EQ) | isKind(OP_NEQ)) {
				consume();
				relExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}


	private void relExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			addExpression();
			while (isKind(OP_GE) | isKind(OP_LE) | isKind(OP_LT) | isKind(OP_GT)) {
				consume();
				addExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}


	private void addExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			multExpression();
			while (isKind(OP_PLUS) | isKind(OP_MINUS)) {
				consume();
				multExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}
	
	private void multExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			powerExpression();
			while (isKind(OP_TIMES) | isKind(OP_DIV) | isKind(OP_MOD)) {
				consume();
				powerExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}


	private void powerExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		try {
			unaryExpression();
			if (isKind(OP_POWER)) {
				consume();
				powerExpression();
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstExp)) {
					expression();
					return;
				} else if (isKind(followExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}

    Kind[] firstPrimary = {INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN,
    		              KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, 
	                  KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,
	                  KW_blue, KW_alpha, IDENTIFIER, KW_Z, KW_default_height, KW_default_width,
			          LPIXEL};
	
	private void unaryExpression() {
		// TODO Auto-generated method stub
		try {
			if (isKind(OP_PLUS)|isKind(OP_MINUS)|isKind(OP_EXCLAMATION)) {
				consume();
				unaryExpression();
			} else {
				switch(t.kind) {
				    case INTEGER_LITERAL: case BOOLEAN_LITERAL: case FLOAT_LITERAL:
				    	// PredefinedName
				    case KW_Z: case KW_default_height: case KW_default_width:
				    {
				    	    consume();
				    }
				    break;
				    case LPAREN: {
				    	    consume();
				    	    expression();
				    	    match(RPAREN);
				    }
				    break;
				    // function application
				    case KW_sin: case KW_cos: case KW_atan: case KW_abs: case KW_log: case KW_cart_x: 
				    case KW_cart_y: case KW_polar_a: case KW_polar_r: case KW_int: case KW_float: 
				    case KW_width: case KW_height: case KW_red: case KW_green: case KW_blue: case KW_alpha: {
				    	    consume();
				    	    if (isKind(LPAREN)) {
				    	    	    consume();
				    	    	    expression();
				    	    	    match(RPAREN);
				    	    } else if (isKind(LSQUARE)) {
				    	    	    consume();
				    	    	    expression();
				    	    	    match(COMMA);
				    	    	    expression();
				    	    	    match(RSQUARE);
				    	    } else {
				    	    	    error(" supposed to be ( or [");
				    	    }
				    }
				    break;
				    // IDENTIFIER and PixelExpression
				    case IDENTIFIER: {
				    	    consume();
				    	    if (isKind(LSQUARE)) {
				    	    	    consume();
				    	    	    expression();
				    	    	    match(COMMA);
				    	    	    expression();
				    	    	    match(RSQUARE);
				    	    }
				    }
				    break;
				    // PixelConstructor
				    case LPIXEL: {
				    	    consume();
				    	    expression();
				    	    match(COMMA);
				    	    expression();
				    	    match(COMMA);
				    	    expression();
				    	    match(COMMA);
				    	    expression();
				    	    match(RPIXEL);
				    }
				    	break;
				    default: {
				    	error(" supposed to be the first element of primary expression.");
				    }
					break;
				} // end of switch
			} // end of if
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstPrimary)) {
					unaryExpression();
					return;
				} else if (isKind(followExp) | isKind(firstExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}


	/*
	 * Statement ::= StatementInput | StatementWrite | StatementAssignment 
	 *              	| StatementWhile | StatementIf | StatementShow | StatementSleep	
	 */
		
	public void statement() {
		//TODO
		try {
			switch(t.kind) {
			    case KW_input: {
			    	    consume();
		    	        match(IDENTIFIER);
		    	        match(KW_from);
		    	        match(OP_AT);
		    	        expression();
			    }
			    	break;
			    	case KW_write: {
			    		consume();
			    		match(IDENTIFIER);
			    		match(KW_to);
			    		match(IDENTIFIER);
			    	}
			    	break;
			    	// StatementAssignment 1
			    	case IDENTIFIER: {
			    		consume();
			    		if (isKind(LSQUARE)) {
			    			consume();
			    			expression();
			    			match(COMMA);
			    			expression();
			    			match(RSQUARE);
			    		}
			    		match(OP_ASSIGN);
			    		expression();
			    	}
			    	break;
			    	// StatementAssignment 2
			    	case KW_red: 
			    	case KW_green: 
			    	case KW_blue: 
			    	case KW_alpha: {
			    		consume();
			    		match(LPAREN);
			    		match(IDENTIFIER);
			    		match(LSQUARE);
			    		expression();
		    			match(COMMA);
		    			expression();
		    			match(RSQUARE);
		    			match(RPAREN);
		    			match(OP_ASSIGN);
			    		expression();
			    	}
			    	break;
			    	// StatementWhile & StatementIf
			    	case KW_if:
			    	case KW_while: {
			    		consume();
			    		match(LPAREN);
			    		expression();
		    			match(RPAREN);
		    			block();
			    	}
			    	break;
			    	// StatementShow & StatementSleep
			    	case KW_show:
			    	case KW_sleep: {
			    		consume();
			    		expression();
			    	}
			    	break;
			    	default:{
			    		error(" Expected to be the first element of statement.");
			    	}
			}
		} catch (SyntaxException e) {
			while (!isKind(EOF)) {
				if (isKind(firstStatement)) {
					statement();
					return;
				} else if (isKind(IDENTIFIER)|isKind(OP_ASSIGN)|isKind(LSQUARE)
						   |isKind(LPAREN)|isKind(firstExp)) {
					return;
				} else {
					t = scanner.nextToken();
				}
			}
		}
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		String m = t.line() + ":" + (t.posInLine() - 1) + " " + "is supposed to be " + kind;
		throw new SyntaxException(t, m); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			String m = (t.line() + 1) + ":" + (t.posInLine() + 1) + " " + "unexpected sudden End-of-File.";
			throw new SyntaxException(t, m); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		String m = (t.line() + 1) + ":" + (t.posInLine() + 1) + " " + "Expected to be the End-of-File.";
		throw new SyntaxException(t, m); //TODO  give a better error message!
	}
	
	private void error(String s) throws SyntaxException {
		String m = (t.line() + 1) + ":" + (t.posInLine() + 1) + s;
		throw new SyntaxException(t, m);
	}
}
