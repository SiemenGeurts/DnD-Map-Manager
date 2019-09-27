package helpers;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

public class ScalingBounds {
	
	private Rectangle2D source;
	private Rectangle2D destination;
	
	protected ScalingBounds(Rectangle2D source, Rectangle2D destination) {
		this.source = source;
		this.destination = destination;
	}
	
	public double getSourceX() {
		return source.getMinX();
	}
	
	public double getSourceY() {
		return source.getMinY();
	}
	
	public double getSourceWidth() {
		return source.getWidth();
	}
	
	public double getSourceHeight() {
		return source.getHeight();
	}
	
	public double getDestX() {
		return destination.getMinX();
	}
	
	public double getDestY() {
		return destination.getMinY();
	}
	
	public double getDestWidth() {
		return destination.getWidth();
	}
	
	public double getDestHeight() {
		return destination.getHeight();
	}	
	
	public static enum ScaleMode {
		STRETCH, EXTEND, FIT;
	}
	
	public static ScalingBounds getBounds(double parentWidth, double parentHeight, Image image, ScaleMode mode) {
		double aspectratio;

		switch(mode) {
		case STRETCH:
			return new ScalingBounds(new Rectangle2D(0, 0, image.getWidth(), image.getHeight()), new Rectangle2D(0, 0, parentWidth, parentHeight));
		case FIT:
			aspectratio = image.getWidth()/image.getHeight();
			Rectangle2D source = new Rectangle2D(0,0,image.getWidth(),image.getHeight());
			if(aspectratio*parentHeight<parentWidth) {
				//make the height fit
				double scale = parentHeight/image.getHeight();
				Rectangle2D destination = new Rectangle2D((parentWidth-source.getWidth()*scale)/2, 0, source.getWidth()*scale, parentHeight);
				return new ScalingBounds(source, destination);
			} else {
				//make the width fit
				double scale = parentWidth/image.getWidth();
				Rectangle2D destination = new Rectangle2D(0, (parentHeight-source.getHeight()*scale)/2, parentWidth, source.getHeight()*scale);
				return new ScalingBounds(source, destination);
			}
		case EXTEND:
			aspectratio = image.getWidth()/image.getHeight();
			Rectangle2D destination = new Rectangle2D(0, 0, parentWidth, parentHeight);
			if(aspectratio*parentHeight<parentWidth) {
				double height = parentWidth/aspectratio;
				Rectangle2D source2 = new Rectangle2D(0,image.getHeight()*(1-parentHeight/height)/2,image.getWidth(),image.getHeight()*parentHeight/height);
				return new ScalingBounds(source2, destination);
			} else {
				//make the height fit, extend the width
				double width = aspectratio*parentHeight;
				Rectangle2D source2 = new Rectangle2D(image.getWidth()*(1-parentWidth/width)/2,0,image.getWidth()*parentWidth/width, image.getHeight());
				return new ScalingBounds(source2, destination);
			}
		}
		return null;
	}
}
