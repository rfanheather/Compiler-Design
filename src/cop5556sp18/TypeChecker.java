package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.Types.Type;

import java.util.Arrays;
import java.util.List;

import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
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


public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super(message);
			this.t = t;
		}
	}

	SymbolTable st = new SymbolTable();
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		st.enterScope();
		List<ASTNode> list = block.decsOrStatements;
		for (ASTNode n: list) {
			if (n.isDec) {
				Declaration dec = (Declaration) n;
				dec.visit(this, arg);
			} else {
				Statement stmt = (Statement) n;
				stmt.visit(this, arg);
		    }
	    }
		st.closeScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if (st.duplicate(declaration.name)) {
			String message = "Duplicate declaration.";
			throw new SemanticException(declaration.firstToken, message);
		}

		if (declaration.width == null && declaration.height == null) {
			st.addDec(declaration.name, declaration);
			return null;
		}
		
		Type widthType = (Type) declaration.width.visit(this, arg);
		Type heightType = (Type) declaration.height.visit(this, arg);
		
		if (widthType == Type.INTEGER && heightType == Type.INTEGER && Types.getType(declaration.type) == Type.IMAGE) {
			st.addDec(declaration.name, declaration);
			return null;
		}
		
		String message = "Incompatible declaration type.";
		throw new SemanticException(declaration.firstToken, message);
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration sourceDec = st.lookup(statementWrite.sourceName);
		if (sourceDec == null) {
			String message = "Source should be declared before write.";
			throw new SemanticException(statementWrite.firstToken, message);
		}
		
		Declaration destDec = st.lookup(statementWrite.destName);
		if (destDec == null) {
			String message = "Destination should be declared before write.";
			throw new SemanticException(statementWrite.firstToken, message);
		}
		
		if (Types.getType(sourceDec.type) != Type.IMAGE 
		    || Types.getType(destDec.type) != Type.FILE) {
			String message = "Incompatible types for write statement.";
			throw new SemanticException(statementWrite.firstToken, message);
		}
		statementWrite.setSourceDec(sourceDec);
		statementWrite.setDestDec(destDec);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = st.lookup(statementInput.destName);
		if (dec == null) {
			String message = "Input should be declared.";
			throw new SemanticException(statementInput.firstToken, message);
		}
		
		Type eType = (Type) statementInput.e.visit(this, arg);
		
		if (eType != Type.INTEGER) {
			String message = "Expression should be an integer.";
			throw new SemanticException(statementInput.firstToken, message);
		}
		
		statementInput.setDec(dec);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e1T = (Type) pixelSelector.ex.visit(this, arg);
		Type e2T = (Type) pixelSelector.ey.visit(this, arg);
		
		if (e1T != e2T) {
			String message = "Expression type not compatible";
			throw new SemanticException(pixelSelector.firstToken, message);
		}
		
		if (e1T != Type.INTEGER && e1T != Type.FLOAT) {
			String message = "Expression should be integer or float";
			throw new SemanticException(pixelSelector.firstToken, message);
		}
		return null;
	}

	
	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type guardT = (Type) expressionConditional.guard.visit(this, arg);
		Type trueT = (Type) expressionConditional.trueExpression.visit(this, arg);
		Type falseT = (Type) expressionConditional.falseExpression.visit(this, arg);
		
		if (guardT != Type.BOOLEAN) {
			String message = "Guard type should be boolean.";
			throw new SemanticException(expressionConditional.firstToken, message);
		}
		
		if (trueT !=	 falseT) {
			String message = "Expressions don't have the same type to compare.";
			throw new SemanticException(expressionConditional.firstToken, message);
		}
		
		expressionConditional.setType(trueT);
		return expressionConditional.getType();
	}

	
	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type leftT = (Type) expressionBinary.leftExpression.visit(this, arg);
		Type rightT = (Type) expressionBinary.rightExpression.visit(this, arg);
		Kind op = expressionBinary.op;
		
		List<Kind> basicOpType = Arrays.asList(Kind.OP_PLUS, Kind.OP_MINUS, Kind.OP_POWER, Kind.OP_DIV,Kind.OP_TIMES);
		List<Kind> logicOpType = Arrays.asList(Kind.OP_AND, Kind.OP_OR);
		List<Kind> CompOpType = Arrays.asList(Kind.OP_EQ, Kind.OP_NEQ, Kind.OP_GT, Kind.OP_GE,Kind.OP_LT, Kind.OP_LE);
		
		if (leftT == Type.INTEGER && rightT == Type.INTEGER) {
			if (basicOpType.contains(op) || logicOpType.contains(op) || op == Kind.OP_MOD) {
				expressionBinary.setType(Type.INTEGER);
				return expressionBinary.getType();
			}
			if (CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		if (leftT == Type.FLOAT && rightT == Type.FLOAT) {
			if (basicOpType.contains(op)) {
				expressionBinary.setType(Type.FLOAT);
				return expressionBinary.getType();
			}
			if (CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		if (leftT == Type.FLOAT && rightT == Type.INTEGER || leftT == Type.INTEGER && rightT == Type.FLOAT) {
			if (basicOpType.contains(op)) {
				expressionBinary.setType(Type.FLOAT);
				return expressionBinary.getType();
			}
		}
		
		if (leftT == Type.BOOLEAN && rightT == Type.BOOLEAN) {
			if (logicOpType.contains(op) || CompOpType.contains(op)) {
				expressionBinary.setType(Type.BOOLEAN);
				return expressionBinary.getType();
			}
		}
		
		String message = "Incompatible type for expressionBinary: " + expressionBinary.firstToken.getText();
		throw new SemanticException(expressionBinary.firstToken, message);
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type eT = (Type) expressionUnary.expression.visit(this, arg);
		expressionUnary.setType(eT);
		return expressionUnary.getType();
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.setType(Type.INTEGER);
		return expressionIntegerLiteral.getType();
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.setType(Type.BOOLEAN);
		return expressionBooleanLiteral.getType();
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.setType(Type.INTEGER);
		return expressionPredefinedName.getType();
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.setType(Type.FLOAT);
		return expressionFloatLiteral.getType();
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type eT = (Type) expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Kind function = expressionFunctionAppWithExpressionArg.function;
		
		List<Kind> integerFunction = Arrays.asList(Kind.KW_abs, Kind.KW_red, Kind.KW_green, Kind.KW_blue,Kind.KW_alpha);
		List<Kind> floatFunction = Arrays.asList(Kind.KW_abs, Kind.KW_sin, Kind.KW_cos, Kind.KW_atan,Kind.KW_log);
		List<Kind> imageFunction = Arrays.asList(Kind.KW_width, Kind.KW_height);
		
		if (eT == Type.INTEGER) {
			if (integerFunction.contains(function)) {
				expressionFunctionAppWithExpressionArg.setType(Type.INTEGER);
				return expressionFunctionAppWithExpressionArg.getType();
			}
			if (function == Kind.KW_float) {
				expressionFunctionAppWithExpressionArg.setType(Type.FLOAT);
				return expressionFunctionAppWithExpressionArg.getType();
			}
			if (function == Kind.KW_int) {
				expressionFunctionAppWithExpressionArg.setType(Type.INTEGER);
				return expressionFunctionAppWithExpressionArg.getType();
			}
		}
		
		if (eT == Type.FLOAT) {
			if (floatFunction.contains(function)) {
				expressionFunctionAppWithExpressionArg.setType(Type.FLOAT);
				return expressionFunctionAppWithExpressionArg.getType();
			}
			if (function == Kind.KW_float) {
				expressionFunctionAppWithExpressionArg.setType(Type.FLOAT);
				return expressionFunctionAppWithExpressionArg.getType();
			}
			if (function == Kind.KW_int) {
				expressionFunctionAppWithExpressionArg.setType(Type.INTEGER);
				return expressionFunctionAppWithExpressionArg.getType();
			}
		}
		
		if (eT == Type.IMAGE && imageFunction.contains(function)) {
			expressionFunctionAppWithExpressionArg.setType(Type.INTEGER);
			return expressionFunctionAppWithExpressionArg.getType();
		}
		
		String message = "Incompatible types for expressionFunctionAppWithExpressionArg";
		throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken, message);
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind name = expressionFunctionAppWithPixel.name;
		Type e0T = (Type) expressionFunctionAppWithPixel.e0.visit(this, arg);
		Type e1T = (Type) expressionFunctionAppWithPixel.e1.visit(this, arg);
		
		if (name == Kind.KW_cart_x || name == Kind.KW_cart_y) {
			if (e0T == Type.FLOAT && e1T == Type.FLOAT) {
				expressionFunctionAppWithPixel.setType(Type.INTEGER);
				return expressionFunctionAppWithPixel.getType();
			}
		}
		
		if (name == Kind.KW_polar_a || name == Kind.KW_polar_r) {
			if (e0T == Type.INTEGER && e1T == Type.INTEGER) {
				expressionFunctionAppWithPixel.setType(Type.FLOAT);
				return expressionFunctionAppWithPixel.getType();
			}
		}

        String message = "Incompatible types for ExpressionFunctionAppWithPixel";
		throw new SemanticException(expressionFunctionAppWithPixel.firstToken, message);
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type alphaT = (Type) expressionPixelConstructor.alpha.visit(this, arg);
		Type redT = (Type) expressionPixelConstructor.red.visit(this, arg);
		Type greenT = (Type) expressionPixelConstructor.green.visit(this, arg);
		Type blueT = (Type) expressionPixelConstructor.blue.visit(this, arg);
		
		if (alphaT != Type.INTEGER || redT != Type.INTEGER || greenT != Type.INTEGER || blueT != Type.INTEGER) {
			String message = "Pixels should be integers to construct.";
			throw new SemanticException(expressionPixelConstructor.firstToken, message);
		}
		
		expressionPixelConstructor.setType(Type.INTEGER);
		return expressionPixelConstructor.getType();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type LHS = (Type) statementAssign.lhs.visit(this, arg);
		Type e = (Type) statementAssign.e.visit(this, arg);
		if (LHS != e) {
			String message = "LHS and expression should have same type.";
			throw new SemanticException(statementAssign.firstToken, message);
		}
		return null;
	}

	
	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) statementShow.e.visit(this, arg);
		List<Types.Type> showType = Arrays.asList(Type.INTEGER, Type.BOOLEAN, Type.IMAGE, Type.FLOAT, Type.FILE);
		
		if (!showType.contains(e)) {
			String message = "Incompatible show statement type.";
			throw new SemanticException(statementShow.firstToken, message);
		}
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionPixel.pixelSelector.visit(this, arg);
		Declaration dec = st.lookup(expressionPixel.name);
		if (dec == null) {
			String message = "Missing Declaration.";
			throw new SemanticException(expressionPixel.firstToken, message);
		}
		
		if (Types.getType(dec.type) != Type.IMAGE) {
			String message = "ExpressionPixel can only work with image type.";
			throw new SemanticException(expressionPixel.firstToken, message);
		}
		
		expressionPixel.setDec(dec);
		expressionPixel.setType(Type.INTEGER);
		return expressionPixel.getType();
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = st.lookup(expressionIdent.name);
		if (dec == null) {
			String message = "Missing declaration.";
			throw new SemanticException(expressionIdent.firstToken, message);
		}
		expressionIdent.setDec(dec);
		expressionIdent.setType(Types.getType(dec.type));
		return expressionIdent.getType();
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsSample.pixelSelector.visit(this, arg);
		Declaration dec = st.lookup(lhsSample.name);
		if (dec == null) {
			String message = "Missing declaration.";
			throw new SemanticException(lhsSample.firstToken, message);
		}
		
		if (Types.getType(dec.type) != Type.IMAGE) {
			String message = "Declaration type should be image.";
			throw new SemanticException(lhsSample.firstToken, message);
		}
		
		lhsSample.setDec(dec);
		lhsSample.setType(Type.INTEGER);
		return lhsSample.getType();
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		lhsPixel.pixelSelector.visit(this, arg);
		Declaration dec = st.lookup(lhsPixel.name);
		if (dec == null) {
			String message = "Missing declaration.";
			throw new SemanticException(lhsPixel.firstToken, message);
		}
		
		if (Types.getType(dec.type) != Type.IMAGE) {
			String message = "Declaration type should be image.";
			throw new SemanticException(lhsPixel.firstToken, message);
		}
		
		lhsPixel.setDec(dec);
		lhsPixel.setType(Type.INTEGER);
		return lhsPixel.getType();
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = st.lookup(lhsIdent.name);
		if (dec == null) {
			String message = "Missing declaration.";
			throw new SemanticException(lhsIdent.firstToken, message);
		}
		
		lhsIdent.setDec(dec);
		lhsIdent.setType(Types.getType(dec.type));
		return lhsIdent.getType();
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) statementIf.guard.visit(this, arg);
		if (e != Type.BOOLEAN) {
			String message = "Expression should has type boolean.";
			throw new SemanticException(statementIf.firstToken, message);
		}
		statementIf.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) statementWhile.guard.visit(this, arg);
		if (e != Type.BOOLEAN) {
			String message = "Expression should has type boolean.";
			throw new SemanticException(statementWhile.firstToken, message);
		}
		statementWhile.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type e = (Type) statementSleep.duration.visit(this, arg);
		if (e != Type.INTEGER) {
			String message = "Expression should has type integer.";
			throw new SemanticException(statementSleep.firstToken, message);
		}
		return null;
	}


}
