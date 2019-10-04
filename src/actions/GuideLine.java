package actions;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import helpers.Calculator;

public class GuideLine {
    public ArrayList<Point2D> path;
    Point2D location, next;
    int nextIndex = 1;
    boolean arrived = false;

    public GuideLine(Point2D[] _path) {
        path = new ArrayList<Point2D>(_path.length);
        for(int i = 0; i < _path.length; i++) path.add(_path[i]);
        location = new Point2D.Double(_path[0].getX(), _path[0].getY());
        next = new Point2D.Double(_path[1].getX(), _path[1].getY());
    }

    public GuideLine(ArrayList<Point> _path) {
        path = new ArrayList<>(_path.size());
        path.addAll(_path);
        location = path.get(0);
        next = path.get(nextIndex);
    }

    public Point2D follow(double d) {
        if(hasArrived()) return location;
        double dx = next.getX() - location.getX();
        double dy = next.getY() - location.getY();
        double total = Calculator.getDistance(dx, dy);
        if(d >= total) {
            nextIndex++;
            if(nextIndex >= path.size()) {
                arrived = true;
                location = path.get(path.size()-1);
            } else {
                location = next;
                next = path.get(nextIndex);
            }
        } else
            location.setLocation(location.getX() + d/total*dx, location.getY() + d/total*dy);
        return location;
    }

    public boolean hasArrived() {
        return arrived;
    }
    
    public Point2D getDestination() {
    	return path.get(path.size()-1);
    }
}
