package helpers;

import java.awt.Point;
import java.awt.Rectangle;

public class Calculator {
	
	public static double getDistance(double dx, double dy) {
		return Math.sqrt(dx*dx+dy*dy);
	}
	
	/**
	 * Clamps the a number between two given bounds
	 * @param x the value
	 * @param a the lower bound
	 * @param b the upper bound
	 * @return {@code Math.max(a, Math.min(x,b));}
	 */
	public static double clamp(double x, double a, double b) {
		if(a==b) return a;
		if(a>b) return clamp(x, b, a);
		return Math.max(a, Math.min(x, b));
	}
	
	public static Rectangle getRectangle(Point p1, Point p2) {
		return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.abs(p1.x-p2.x), Math.abs(p1.y-p2.y));
	}

}
