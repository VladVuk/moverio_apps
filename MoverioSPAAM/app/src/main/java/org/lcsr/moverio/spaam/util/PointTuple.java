package org.lcsr.moverio.spaam.util;

import Jama.Matrix;
import android.graphics.Point;

public class PointTuple {
	
	public Point calcPoint, clickPoint;
	
	public PointTuple(Point pa, Point pb) {
		calcPoint = pb;
		clickPoint = pa;
	}
	
	public PointTuple(Point pa, Matrix pb) {
		clickPoint = pa;
		calcPoint = new Point((int)(pb.get(0, 0) / pb.get(2, 0)), (int)(pb.get(1, 0) / pb.get(2, 0)));
	}
	
	public PointTuple(int x, int y, Matrix pb) {
		clickPoint = new Point(x, y);
		calcPoint = new Point((int)(pb.get(0, 0) / pb.get(2, 0)), (int)(pb.get(1, 0) / pb.get(2, 0)));
	}
}
