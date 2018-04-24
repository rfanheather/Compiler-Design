package cop5556sp18;


public class RuntimeFunctions {
	public static final String className = "cop5556sp18/RuntimeFunctions";
	
	public static String sinSig = "(F)F";
	public static float sin(float arg0) {
		return (float) Math.sin((double) arg0);
	}
	
	public static String cosSig = "(F)F";
	public static float cos(float arg0) {
		return (float) Math.cos((double) arg0);
	}
	
	public static String atanSig = "(F)F";
	public static float atan(float arg0) {
		return (float) Math.atan((double) arg0);
	}
	
	public static String logSig = "(F)F";
	public static float log(float arg0) {
		return (float) Math.log((double) arg0);
	}
	
	public static String absSig = "(F)F";
	public static float abs(float arg0) {
		return Math.abs(arg0);
	}
	
	public static String absIntSig = "(I)I";
	public static int absInt(int arg0) {
		return Math.abs(arg0);
	}
	
	public static String multSig = "(FF)I";
	public static int mult(float arg0, float arg1) {
		return (int) (arg0 * arg1);
	}
	
	public static String atan2Sig = "(II)F";
	public static float atan2(int arg0, int arg1) {
		return (float) Math.atan2((double) arg0, (double) arg1);
	}
	
	public static String hypotSig = "(II)F";
	public static float hypot(int arg0, int arg1) {
		return (float) Math.hypot((double) arg0, (double) arg1);
	}
}