package helpers;

public class Calculator {
	
	public static double getDistance(double dx, double dy) {
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	public static double clamp(double x, double a, double b) {
		if(a==b) return a;
		if(a>b) return clamp(x, b, a);
		return Math.max(a, Math.min(x, b));
	}

}
