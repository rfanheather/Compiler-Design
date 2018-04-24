/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
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
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */
	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;
	
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	final int defaultWidth;
	final int defaultHeight;
	
	//Label startLabel;
	//Label endLabel;
	
	private int slot = 1;  // initialize slot number
	private int arg_slot = 0;
	
	//
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		// determin slots for local variables
		Label blockStart = new Label();
		// startLabel = blockStart;
		Label blockEnd = new Label();
		// endLabel = blockEnd;
		
		for (ASTNode node : block.decsOrStatements) {
			if (node.isDec) {
				Declaration dec = (Declaration) node;
				dec.slot = slot;
				if(Types.getType(dec.type) == Type.IMAGE) {
					dec.visit(this, arg);
			    }
				slot++;
				dec.startL = blockStart;
				dec.endL = blockEnd;
			}
		}

		// visit block start label
		mv.visitLabel(blockStart);
		
		// add instructions
		for (ASTNode node : block.decsOrStatements) {
			if (!node.isDec) {
				node.visit(this, arg);
			}
		}
		
		// visit block end label
		mv.visitLabel(blockEnd);
		
		/*
		for (ASTNode node : block.decsOrStatements) {
			if (node.isDec) {
				node.visit(this, arg);
			}
		}*/
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionBooleanLiteral.value);
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if (Types.getType(declaration.type) == Type.IMAGE) {
			if (declaration.width != null && declaration.height != null) {
				// visit the Expressions to generate code to evaluate them 
				// and leave their value on the stack.
				declaration.width.visit(this, arg);
				declaration.height.visit(this, arg);
				
				// generate code to instantiate an image (invoke RuntimeImageSupport.makeImage) 
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
						"makeImage", RuntimeImageSupport.makeImageSig, false);
			}
			
			if (declaration.width == null && declaration.height == null) {
				mv.visitLdcInsn(defaultWidth);
				mv.visitLdcInsn(defaultHeight);
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
						"makeImage", RuntimeImageSupport.makeImageSig, false);
			}
			
			 // generate code to store the instantiated image in the variable
			 mv.visitVarInsn(ASTORE, declaration.slot);
		}
		return null;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Label setTrue = new Label();
		Label endEB = new Label();
		
		if (expressionBinary.leftExpression.getType() == expressionBinary.rightExpression.getType()) {
			
			if (expressionBinary.leftExpression.getType() == Type.INTEGER) {
				switch (expressionBinary.op) {
				case OP_POWER:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(I2D);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(I2D);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2I);
					break;
				case OP_AND:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IAND);
					break;
				case OP_DIV:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IDIV);
					break;
				case OP_EQ:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GE:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GT:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LE:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LT:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_MINUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(ISUB);
					break;
				case OP_MOD:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IREM);
					break;
				case OP_NEQ:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_OR:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IOR);
					break;
				case OP_PLUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IADD);
					break;
				case OP_TIMES:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(IMUL);
					break;
				default:
					break;
				}
				mv.visitJumpInsn(GOTO, endEB);
				mv.visitLabel(setTrue);
				mv.visitLdcInsn(true);
				mv.visitLabel(endEB);
				
			} else if (expressionBinary.leftExpression.getType() == Type.FLOAT) {
				switch (expressionBinary.op) {
				case OP_POWER:
					expressionBinary.leftExpression.visit(this, arg);
					mv.visitInsn(F2D);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(F2D);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
					mv.visitInsn(D2F);
					break;
				case OP_TIMES:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FMUL);
					break;
				case OP_PLUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FADD);
					break;
				case OP_MINUS:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					break;
				case OP_DIV:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FDIV);
					break;
				case OP_EQ:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GE:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GT:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LE:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LT:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_NEQ:
					expressionBinary.leftExpression.visit(this, arg);
					expressionBinary.rightExpression.visit(this, arg);
					mv.visitInsn(FSUB);
					mv.visitInsn(F2I);
					mv.visitLdcInsn(new Integer(Integer.valueOf(0)));
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				default:
					break;
				}
				mv.visitJumpInsn(GOTO, endEB);
				mv.visitLabel(setTrue);
				mv.visitLdcInsn(true);
				mv.visitLabel(endEB);
				
			} else if (expressionBinary.leftExpression.getType() == Type.BOOLEAN) {
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				switch (expressionBinary.op) {
				case OP_AND:
					mv.visitInsn(IAND);
					break;
				case OP_EQ:
					mv.visitJumpInsn(IF_ICMPEQ, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GE:
					mv.visitJumpInsn(IF_ICMPGE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_GT:
					mv.visitJumpInsn(IF_ICMPGT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LE:
					mv.visitJumpInsn(IF_ICMPLE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_LT:
					mv.visitJumpInsn(IF_ICMPLT, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_NEQ:
					mv.visitJumpInsn(IF_ICMPNE, setTrue);
					mv.visitLdcInsn(false);
					break;
				case OP_OR:
					mv.visitInsn(IOR);
					break;
				default:
					break;
				}
				mv.visitJumpInsn(GOTO, endEB);
				mv.visitLabel(setTrue);
				mv.visitLdcInsn(true);
				mv.visitLabel(endEB);
			}
			
		} else if (expressionBinary.leftExpression.getType() == Type.INTEGER 
				&& expressionBinary.rightExpression.getType() == Type.FLOAT) {
			switch (expressionBinary.op) {
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(F2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
				
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FDIV);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FSUB);
				break;
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FADD);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(I2F);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(FMUL);
				break;
			default:
				break;
			}
		} else if (expressionBinary.leftExpression.getType() == Type.FLOAT 
				&& expressionBinary.rightExpression.getType() == Type.INTEGER) {
			switch (expressionBinary.op) {
			case OP_POWER:
				expressionBinary.leftExpression.visit(this, arg);
				mv.visitInsn(F2D);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2D);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
				mv.visitInsn(D2F);
				break;
				
			case OP_DIV:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FDIV);
				break;
			case OP_MINUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FSUB);
				break;
			case OP_PLUS:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FADD);
				break;
			case OP_TIMES:
				expressionBinary.leftExpression.visit(this, arg);
				expressionBinary.rightExpression.visit(this, arg);
				mv.visitInsn(I2F);
				mv.visitInsn(FMUL);
				break;
			default:
				break;
			}
		}
		
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label start = new Label();
		Label startTrue = new Label();
		Label startFalse = new Label();
		Label end = new Label();
		
		mv.visitLabel(start);
		expressionConditional.guard.visit(this, arg); //GUARD
		mv.visitJumpInsn(IFNE, startTrue); //IFNE True
		
		mv.visitLabel(startFalse);
		expressionConditional.falseExpression.visit(this, arg); //FALSE
		Label endFalse = new Label();
		mv.visitLabel(endFalse);
		mv.visitJumpInsn(GOTO, end); //GOTO END
		
		mv.visitLabel(startTrue);
		expressionConditional.trueExpression.visit(this, arg); //FALSE
		Label endTrue = new Label();
		mv.visitLabel(endTrue);
		
		mv.visitLabel(end);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
        expressionFunctionAppWithExpressionArg.e.visit(this, arg);
		Kind k = expressionFunctionAppWithExpressionArg.function;
		Type expT = expressionFunctionAppWithExpressionArg.e.getType();
		switch(k) {
		case KW_sin:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"sin", RuntimeFunctions.sinSig, false);
			break;
		case KW_cos:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"cos", RuntimeFunctions.cosSig, false);
			break;
		case KW_atan:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"atan", RuntimeFunctions.atanSig, false);
			break;
		case KW_abs:
			if (expT == Type.INTEGER) {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"absInt", RuntimeFunctions.absIntSig, false);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
						"abs", RuntimeFunctions.absSig, false);
			}
			break;
		case KW_log:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"log", RuntimeFunctions.logSig, false);
			break;
		case KW_cart_x:
			break;
		case KW_cart_y:
			break;
		case KW_polar_a:
			break;
		case KW_polar_r:
			break;
		case KW_int:
			if (expT != Type.INTEGER) {
				mv.visitInsn(F2I);
			}
			break;
		case KW_float:
			if (expT != Type.FLOAT) {
				mv.visitInsn(I2F);
			}
			break;
		case KW_width:
			// System.out.println(expressionFunctionAppWithExpressionArg.e.type);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
					"getWidth", RuntimeImageSupport.getWidthSig, false);
			break;
		case KW_height:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
					"getHeight", RuntimeImageSupport.getHeightSig, false);
			break;
		case KW_red: 
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
					"getRed", RuntimePixelOps.getRedSig, false);
			break;
		case KW_green: 
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
					"getGreen", RuntimePixelOps.getGreenSig, false);
			break;
		case KW_blue: 
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
					"getBlue", RuntimePixelOps.getBlueSig, false);
			break;
		case KW_alpha:
			mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
					"getAlpha", RuntimePixelOps.getAlphaSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Kind func = expressionFunctionAppWithPixel.name;
		switch(func) {
		case KW_cart_x:
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"cos", RuntimeFunctions.cosSig, false);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
			break;
		case KW_cart_y:
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"sin", RuntimeFunctions.cosSig, false);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
			break;
		case KW_polar_a:
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"atan2", RuntimeFunctions.atan2Sig, false);
			break;
		case KW_polar_r:
			expressionFunctionAppWithPixel.e0.visit(this, arg);
			expressionFunctionAppWithPixel.e1.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"hypot", RuntimeFunctions.hypotSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration dec = expressionIdent.getDec();
		if (Types.getType(dec.type) == Type.INTEGER 
				|| Types.getType(dec.type) == Type.BOOLEAN) {
			mv.visitVarInsn(ILOAD, dec.slot);
		} else if (Types.getType(dec.type) == Type.FLOAT) {
			mv.visitVarInsn(FLOAD, dec.slot);
		} else if (Types.getType(dec.type) == Type.IMAGE) {
			mv.visitVarInsn(ALOAD, dec.slot);
		} else {
			mv.visitVarInsn(ALOAD, dec.slot);
		}
		
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, expressionPixel.dec.slot);
		if (expressionPixel.pixelSelector.ex.getType() == Type.FLOAT) {
			// cart_x
			expressionPixel.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"cos", RuntimeFunctions.cosSig, false);
			expressionPixel.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
			// cart_y
			expressionPixel.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"sin", RuntimeFunctions.cosSig, false);
			expressionPixel.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
		} else {
			expressionPixel.pixelSelector.visit(this, arg);
		}
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
				"getPixel", RuntimeImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPixelConstructor.alpha.visit(this, arg);
		expressionPixelConstructor.red.visit(this, arg);
		expressionPixelConstructor.green.visit(this, arg);
		expressionPixelConstructor.blue.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
				"makePixel", RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Kind k = expressionPredefinedName.name;
		switch(k) {
		case KW_Z:
			mv.visitLdcInsn(new Integer(255));
			break;
		case KW_default_height:
			mv.visitLdcInsn(new Integer(Integer.valueOf(defaultHeight)));
			break;
		case KW_default_width:
			mv.visitLdcInsn(new Integer(Integer.valueOf(defaultWidth)));
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionUnary.expression.visit(this, arg);
		Kind op = expressionUnary.op;
		switch(op) {
		case OP_PLUS:
			break;
		case OP_MINUS:
			if (expressionUnary.getType() == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(-1));
				mv.visitInsn(IMUL);
			} else if(expressionUnary.getType() == Type.FLOAT) {
				mv.visitLdcInsn(new Float(-1.0));
				mv.visitInsn(FMUL);
			}
			break;
		case OP_EXCLAMATION:
			if (expressionUnary.getType() == Type.INTEGER) {
				mv.visitLdcInsn(new Integer(Integer.MAX_VALUE));
				mv.visitInsn(IXOR);
				mv.visitLdcInsn(new Integer(Integer.MIN_VALUE));
				mv.visitInsn(ISUB);
			} else if(expressionUnary.getType() == Type.BOOLEAN) {
				Label originalT = new Label();
				Label originalF = new Label();
				
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(IF_ICMPEQ, originalT);
				mv.visitLdcInsn(true);
				mv.visitJumpInsn(GOTO, originalF);
				mv.visitLabel(originalT);
				mv.visitLdcInsn(false);
				mv.visitLabel(originalF);
			}
			break;
		default:
			break;
		
		}
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if (lhsIdent.type == Type.IMAGE) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
					"deepCopy", RuntimeImageSupport.deepCopySig, false);
			mv.visitVarInsn(ASTORE, lhsIdent.dec.slot);
		} else {
			if (lhsIdent.type == Type.BOOLEAN || lhsIdent.type == Type.INTEGER) {
				mv.visitVarInsn(ISTORE, lhsIdent.dec.slot);
			} else if (lhsIdent.type == Type.FLOAT) {
				mv.visitVarInsn(FSTORE, lhsIdent.dec.slot);
			} else {
				mv.visitVarInsn(ASTORE, lhsIdent.dec.slot);
			}
		}
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration image = lhsPixel.dec;
		mv.visitVarInsn(ALOAD, image.slot);
		if (lhsPixel.pixelSelector.ex.getType() == Type.FLOAT) {
			// cart_x
			lhsPixel.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"cos", RuntimeFunctions.cosSig, false);
			lhsPixel.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
			// cart_y
			lhsPixel.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"sin", RuntimeFunctions.cosSig, false);
			lhsPixel.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
		} else {
			lhsPixel.pixelSelector.visit(this, arg);
		}
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
				"setPixel", RuntimeImageSupport.setPixelSig, false);
		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration image = lhsSample.dec;
		mv.visitVarInsn(ALOAD, image.slot);
		if (lhsSample.pixelSelector.ex.getType() == Type.FLOAT) {
			// cart_x
			lhsSample.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"cos", RuntimeFunctions.cosSig, false);
			lhsSample.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
			// cart_y
			lhsSample.pixelSelector.ey.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"sin", RuntimeFunctions.cosSig, false);
			lhsSample.pixelSelector.ex.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, 
					"mult", RuntimeFunctions.multSig, false);
		} else {
			lhsSample.pixelSelector.visit(this, arg);
		}
		
		Kind k = lhsSample.color;
		switch(k) {
			case KW_red: 
				mv.visitLdcInsn(RuntimePixelOps.RED);
				break;
			case KW_green: 
				mv.visitLdcInsn(RuntimePixelOps.GREEN);
				break;
			case KW_blue: 
				mv.visitLdcInsn(RuntimePixelOps.BLUE);
				break;
			case KW_alpha:
				mv.visitLdcInsn(RuntimePixelOps.ALPHA);
			default:
				break;
		}
		/*
		mv.visitVarInsn(ALOAD, image.slot);
		lhsSample.pixelSelector.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
				"getPixel", RuntimeImageSupport.getPixelSig, false);
		Kind k = lhsSample.color;
		switch(k) {
			case KW_red: 
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
						"getRed", RuntimePixelOps.getRedSig, false);
				break;
			case KW_green: 
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
						"getGreen", RuntimePixelOps.getGreenSig, false);
				break;
			case KW_blue: 
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
						"getBlue", RuntimePixelOps.getBlueSig, false);
				break;
			case KW_alpha:
				mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, 
						"getAlpha", RuntimePixelOps.getAlphaSig, false);
				break;
			default:
				break;
		}
		*/
		
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
				"updatePixelColor", RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		pixelSelector.ex.visit(this, arg);
		pixelSelector.ey.visit(this, arg);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
	    // cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// it is
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		//startLabel = mainStart;
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		// add instructions
		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		//endLabel = mainEnd;
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart,
				mainEnd, arg_slot);
		
		// visit local variables
		for (ASTNode node : program.block.decsOrStatements) {
			if (node.isDec) {
				Declaration declaration = (Declaration) node;
				mv.visitLocalVariable(declaration.name,
		                Types.getJVMType(declaration.type),
		                null,
		                declaration.startL,
		                declaration.endL,
		                declaration.slot);
			}
		}
				
		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.
		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label startGuard = new Label();
		Label startBody = new Label();
		Label endBody = new Label();
		
		mv.visitLabel(startGuard);
		statementIf.guard.visit(this, arg); //GUARD
		Label endGuard = new Label();
		mv.visitLabel(endGuard);
		mv.visitJumpInsn(IFNE, startBody); //IFNE BODY
		mv.visitJumpInsn(GOTO, endBody); //GOTO GUARD
		
		mv.visitLabel(startBody);
		statementIf.b.visit(this, arg); //BODY
		mv.visitLabel(endBody);
		
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		mv.visitVarInsn(ALOAD, arg_slot);
		statementInput.e.visit(this, arg);
		mv.visitInsn(AALOAD);
		
		Types.getJVMType(statementInput.dec.type);
		
		if (Types.getType(statementInput.getDec().type) == Type.BOOLEAN 
				|| Types.getType(statementInput.getDec().type) == Type.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, Types.owner, Types.method, Types.returnType, false);
			mv.visitVarInsn(ISTORE, statementInput.getDec().slot);
			
		} else if (Types.getType(statementInput.getDec().type) == Type.FLOAT) {
			mv.visitMethodInsn(INVOKESTATIC, Types.owner, Types.method, Types.returnType, false);
			mv.visitVarInsn(FSTORE, statementInput.getDec().slot);
			
		} else if (Types.getType(statementInput.dec.type) == Type.IMAGE) {
			if (statementInput.dec.width != null && statementInput.dec.height != null) {
				statementInput.dec.width.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
				statementInput.dec.height.visit(this, arg);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", 
						"(I)Ljava/lang/Integer;", false);
			} else {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
					"readImage", RuntimeImageSupport.readImageSig, false);
			mv.visitVarInsn(ASTORE, statementInput.getDec().slot);
			// System.out.println(statementInput.getDec().slot);
			
		} else {
			mv.visitVarInsn(ASTORE, statementInput.getDec().slot);
		}
		/*
		else if (Types.getType(statementInput.dec.type) == Type.FILE) {
			mv.visitTypeInsn(CHECKCAST, Types.getJVMType(statementInput.dec.type));
		} else {
			// mv.visitTypeInsn(CHECKCAST, Types.getJVMType(statementInput.dec.type));
		    Types.getJVMType(statementInput.dec.type);
			mv.visitMethodInsn(INVOKEVIRTUAL, Types.owner, Types.method, Types.returnType, false);
		}*/
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();
		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
				break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Z)V", false);
			}
			break; 
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(F)V", false);
				// TODO implement functionality
			}
			break; 
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(Ljava/lang/String;)V", false);
				// TODO implement functionality
			}
			break; 
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				// TODO implement functionality
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, 
						"makeFrame", RuntimeImageSupport.makeFrameSig, false);
				mv.visitInsn(POP);
			}
		    default:
			    break;
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		statementSleep.duration.visit(this, arg);
		// ExpressionIntegerLiteral e = (ExpressionIntegerLiteral) statementSleep.duration;
		// mv.visitVarInsn(statementSleep.duration.value);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Label startGuard = new Label();
		Label startBody = new Label();
		
		mv.visitJumpInsn(GOTO, startGuard); //GOTO GUARD
		
		mv.visitLabel(startBody);
		statementWhile.b.visit(this, arg); //BODY
		Label endBody = new Label();
		mv.visitLabel(endBody);
		
		mv.visitLabel(startGuard);
		statementWhile.guard.visit(this, arg); //GUARD
		Label endGuard = new Label();
		mv.visitLabel(endGuard);
		mv.visitJumpInsn(IFNE, startBody); //IFNE BODY
		
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Declaration img = statementWrite.sourceDec;
		mv.visitVarInsn(ALOAD, img.slot);
		Declaration file = statementWrite.destDec;
		mv.visitVarInsn(ALOAD, file.slot);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", 
				           RuntimeImageSupport.writeSig, false);
		return null;
	}
	
}
