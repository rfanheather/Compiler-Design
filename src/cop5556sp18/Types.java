package cop5556sp18;

import cop5556sp18.Scanner.Kind;

public class Types {

	public static enum Type {
		INTEGER, BOOLEAN, IMAGE, FLOAT, FILE, NONE;
	}
	
	public static String owner;
	public static String method;
	public static String returnType;

	public static Type getType(Kind kind) {
		switch (kind) {
		case KW_int: {
			return Type.INTEGER;
		}
		case KW_boolean: {
			return Type.BOOLEAN;
		}
		case KW_image: {
			return Type.IMAGE;
		}
		case KW_filename: {
			return Type.FILE;
		}
		case KW_float: {
			return Type.FLOAT;
		}
		default:
			break;
		}
		// should not reach here
		assert false: "invoked getType with Kind that is not a type"; 
		return null;
	}
	
	public static String getJVMType(Kind kind) {
		switch (kind) {
		case KW_int: {
			owner = "java/lang/Integer";
			method = "parseInt";
			returnType = "(Ljava/lang/String;)I";
			return "I";
		}
		case KW_boolean: {
			owner = "java/lang/Boolean";
			method = "parseBoolean";
			returnType = "(Ljava/lang/String;)Z";
			return "Z";
		}
		case KW_image: {
			return "Ljava/awt/image/BufferedImage;";
		}
		case KW_filename: {
			return "Ljava/lang/String;";
		}
		case KW_float: {
			owner = "java/lang/Float";
			method = "parseFloat";
			returnType = "(Ljava/lang/String;)F";
			return "F";
		}
		default:
			break;
		}
		// should not reach here
		assert false: "invoked getJVMType with Kind that is not a type"; 
		return null;
	}
}
