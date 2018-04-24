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

import java.util.ArrayList;
import java.util.List;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.Statement;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;


public class Parser {
	
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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token firstToken = t;
		Token progName = match(IDENTIFIER);
		Block b = block();
		Program p = new Program(firstToken, progName, b);
		return p;
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

	public Block block() throws SyntaxException {
		Token firstToken = t;
		List<ASTNode> list = new ArrayList<>();
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement)) {
		    if (isKind(firstDec)) {
				Declaration d = declaration();
				list.add(d);
			} else if (isKind(firstStatement)) {
				Statement s = statement();
				list.add(s);
			}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(firstToken, list);
	}
	
	/*
	 * Declaration ::= Type IDENTIFIER | image IDENTIFIER [ Expression , Expression ]
	 */
	
	public Declaration declaration() throws SyntaxException {
		//TODO
		Token firstToken = t;
		Token type = t;
		Token name;
		Expression width = null, height = null;
		if (!isKind(KW_image)) {
		    consume();
		    name = match(IDENTIFIER);
		} else {
			consume();
			name = match(IDENTIFIER);
			if (isKind(LSQUARE)) {
				consume();
				width = expression();
				match(COMMA);
				height = expression();
				match(RSQUARE);
			}
		}
		return new Declaration(firstToken, type, name, width, height);
	}
	
	Kind[] firstExp = {OP_PLUS, OP_MINUS, OP_EXCLAMATION, INTEGER_LITERAL,  
			           BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN, IDENTIFIER, 
			           KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, 
			           KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,
			           KW_blue, KW_alpha, KW_Z, KW_default_height, KW_default_width,
			           LPIXEL};
	Kind[] followExp = {LSQUARE};
	
	public Expression expression() throws SyntaxException{
		// TODO Auto-generated method stub
		Token firstToken = t;
		Expression guard = orExpression();
		if (isKind(OP_QUESTION)) {
		    consume();
		    Expression trueEx = expression();
	     	match(OP_COLON);
	     	Expression falseEx = expression();
	     	return new ExpressionConditional(firstToken, guard,
	    			trueEx, falseEx);
	    } else {
	    	    return guard;
	    }
	}
	
	private Expression orExpression() throws SyntaxException{
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = andExpression();
		while (isKind(OP_OR)) {
			Token op = t;
			consume();
			if (isKind(OP_OR)) {
				error(" || is illegal");
				return null;
			}
			Expression e1 = andExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}


	private Expression andExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = eqExpression();
		while (isKind(OP_AND)) {
			Token op = t;
			consume();
			if (isKind(OP_AND)) {
				error(" && is illegal");
				return null;
			}
			Expression e1 = eqExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}


	private Expression eqExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = relExpression();
		while (isKind(OP_EQ) | isKind(OP_NEQ)) {
			Token op = t;
			consume();
			Expression e1 = relExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}


	private Expression relExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = addExpression();
		while (isKind(OP_GE) | isKind(OP_LE) | isKind(OP_LT) | isKind(OP_GT)) {
			Token op = t;
			consume();
			Expression e1 = addExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}


	private Expression addExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = multExpression();
		while (isKind(OP_PLUS) | isKind(OP_MINUS)) {
			Token op = t;
			consume();
			Expression e1 = multExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	private Expression multExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = powerExpression();
		while (isKind(OP_TIMES) | isKind(OP_DIV) | isKind(OP_MOD)) {
			Token op = t;
			consume();
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}


	private Expression powerExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		Expression e0 = unaryExpression();
		if (isKind(OP_POWER)) {
			Token op = t;
			consume();
			Expression e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}

    Kind[] firstPrimary = {INTEGER_LITERAL, BOOLEAN_LITERAL, FLOAT_LITERAL, LPAREN,
    		              KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y, KW_polar_a, 
	                  KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,
	                  KW_blue, KW_alpha, IDENTIFIER, KW_Z, KW_default_height, KW_default_width,
			          LPIXEL};
	
	private Expression unaryExpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token first = t;
		if (isKind(OP_PLUS)|isKind(OP_MINUS)|isKind(OP_EXCLAMATION)) {
			Token op = t;
			consume();
			Expression e = unaryExpression();
			return new ExpressionUnary(first, op, e);
		} else {
			switch(t.kind) {
			    case INTEGER_LITERAL:
			    {
			    	    Token intL = match(INTEGER_LITERAL);
		    	        return new ExpressionIntegerLiteral(first, intL);
		        }
		        case BOOLEAN_LITERAL:
		        {
		    	    		Token bl = match(BOOLEAN_LITERAL);
		    	    		return new ExpressionBooleanLiteral(first, bl);
		        }
		        	case FLOAT_LITERAL:
		        	{
			    	    Token fl = t;
		        		consume();
		        		return new ExpressionFloatLiteral(first, fl);
			    }
			    	// PredefinedName
			    case KW_Z: case KW_default_height: case KW_default_width:
			    {
		    	    		Token name = t;
			    		consume();
		    	    		return new ExpressionPredefinedName(first, name);
			    }
			    case LPAREN: {
			    	    consume();
			    	    Expression e = expression();
			    	    match(RPAREN);
			    	    return e;
			    }
			    // function application
			    case KW_sin: case KW_cos: case KW_atan: case KW_abs: case KW_log: case KW_cart_x: 
			    case KW_cart_y: case KW_polar_a: case KW_polar_r: case KW_int: case KW_float: 
			    case KW_width: case KW_height: case KW_red: case KW_green: case KW_blue: case KW_alpha: {
			    	    Token functionName = t;
			    		consume();
			    	    if (isKind(LPAREN)) {
			    	    	    consume();
			    	    	    Expression e = expression();
			    	    	    match(RPAREN);
			    	    	    return new ExpressionFunctionAppWithExpressionArg(first, functionName, e);
			    	    } else if (isKind(LSQUARE)) {
			    	    	    consume();
			    	    	    Expression e0 = expression();
			    	    	    match(COMMA);
			    	    	    Expression e1 = expression();
			    	    	    match(RSQUARE);
			    	    	    return new ExpressionFunctionAppWithPixel(first, functionName, e0, e1);
			    	    } else {
			    	    	    error(" supposed to be ( or [");
			    	    	    return null;
			    	    }
			    }
			    // IDENTIFIER and PixelExpression
			    case IDENTIFIER: {
			    	    Token name = t;
			    	    consume();
			    	    if (isKind(LSQUARE)) {
			    	    	    Token firstToken = t;
			    	    	    consume();
			    	    	    Expression e0 = expression();
			    	    	    match(COMMA);
			    	    	    Expression e1 = expression();
			    	    	    match(RSQUARE);
			    	    	    PixelSelector ps = new PixelSelector(firstToken, e0, e1);
			    	    	    return new ExpressionPixel(first, name, ps);
			    	    } else {
			    	    		return new ExpressionIdent(first, name);
			    	    }
			    }
			    // PixelConstructor
			    case LPIXEL: {
			    	    consume();
			    	    Expression e0 = expression();
			    	    match(COMMA);
			    	    Expression e1 = expression();
			    	    match(COMMA);
			    	    Expression e2 = expression();
			    	    match(COMMA);
			    	    Expression e3 = expression();
			    	    match(RPIXEL);
			    	    return new ExpressionPixelConstructor(first, e0, e1, e2, e3);
			    }
			    default: {
			     	error(" supposed to be the first element of primary expression.");
			     	return null;
			    }
			} // end of switch
		} // end of if
	}


	/*
	 * Statement ::= StatementInput | StatementWrite | StatementAssignment 
	 *              	| StatementWhile | StatementIf | StatementShow | StatementSleep	
	 */
		
	public Statement statement() throws SyntaxException {
		//TODO
		Token first = t;
		switch(t.kind) {
		    case KW_input: {
		    	    consume();
		    	    Token destName = match(IDENTIFIER);
	    	        match(KW_from);
	    	        match(OP_AT);
	    	        Expression e = expression();
	    	        return new StatementInput(first, destName, e);
		    }
		    	case KW_write: {
		    		consume();
		    		Token sourceName = match(IDENTIFIER);
		    		match(KW_to);
		    		Token destName = match(IDENTIFIER);
		    		return new StatementWrite(first, sourceName, destName);
		    	}
		    	// StatementAssignment 1
		    	case IDENTIFIER: {
		    		Token cur = match(IDENTIFIER);
		    		LHS lhs;
		    		if (isKind(LSQUARE)) {
		    			Token firstPs = t;
		    			consume();
		    			Expression e0 = expression();
		    			match(COMMA);
		    			Expression e1 = expression();
		    			match(RSQUARE);
		    			PixelSelector ps = new PixelSelector(firstPs, e0, e1);
		    			lhs = new LHSPixel(first, cur, ps);
		    		} else {
		    			lhs = new LHSIdent(first, cur);
		    		}
		    		match(OP_ASSIGN);
		    		Expression e = expression();
		    		return new StatementAssign(first, lhs, e);
		    	}
		// StatementAssignment 2
		    	case KW_red: 
		    	case KW_green: 
		    	case KW_blue: 
		    	case KW_alpha: {
		    		Token color = t;
		    		consume();
		    		match(LPAREN);
		    		Token name = match(IDENTIFIER);
		    		Token firstPs = t;
		    		match(LSQUARE);
		    		Expression e0 = expression();
	    			match(COMMA);
	    			Expression e1 = expression();
	    			match(RSQUARE);
	    			PixelSelector ps = new PixelSelector(firstPs, e0, e1);
	    			match(RPAREN);
	    			LHS lhs = new LHSSample(first, name, ps, color);
	    			match(OP_ASSIGN);
	    			Expression e = expression();
	    			return new StatementAssign(first, lhs, e);
		    	}
		    	// StatementWhile & StatementIf
		    	case KW_if: {
		    		consume();
		    		match(LPAREN);
		    		Expression e = expression();
	    			match(RPAREN);
	    			Block b = block();
	    			return new StatementIf(first, e, b);
		    	}
		    	case KW_while: {
		    		consume();
		    		match(LPAREN);
		    		Expression e = expression();
	    			match(RPAREN);
	    			Block b = block();
	    			return new StatementWhile(first, e, b);
		    	}
		    	// StatementShow & StatementSleep
		    	case KW_show: {
		    		consume();
		    		Expression e = expression();
		    		return new StatementShow(first, e);
		    	}
		    	case KW_sleep: {
		    		consume();
		    		Expression e = expression();
		    		return new StatementSleep(first, e);
		    	}
		    	default:{
		    		error(" Expected to be the first element of statement.");
		    	}
		}
		error(" Expected to be the first element of statement.");
		return null;
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
		String m = t.line() + ":" + (t.posInLine() - 1) + " " + t.getText() + " is supposed to be " + kind;
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
