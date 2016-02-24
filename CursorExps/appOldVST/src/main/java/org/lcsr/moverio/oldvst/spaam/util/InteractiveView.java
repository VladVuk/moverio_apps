/*
 *  InteractiveView.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.oldvst.spaam.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lcsr.moverio.oldvst.spaam.util.SPAAM.SPAAMStatus;

import Jama.Matrix;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;
import android.view.View;

@SuppressLint("DrawAllocation")
public class InteractiveView extends View {
	
	private static String TAG = "InteractiveView";
	private int width, height;
	private int pointRadius = 5;
	private float cursorHalfLen = 10;
	private float cursorHalfWid = 1;
	private int lc = 60;
	private SPAAM spaam;
	private Matrix T; // Current Transformation
	
	private static int MAX_ALIGNED = 10;
	private static double KD = 0.8;
	
	private List<Point> centerPoints;
	private Matrix singlePoint;
	private List<Matrix> squareSpacePoints; // length = 4
	private List<Point> squareScreenPoints; // length = 4
	private boolean visibility = false;

	private int x, y;
	
	public static enum PointType {PointCursor, PointCursorAdd, PointAlign, PointAlignAdd};
	
	public InteractiveView(Context context, SPAAM calc) {
		super(context);
		spaam = calc;
		centerPoints = new ArrayList<Point>();
		singlePoint = new Matrix( new double[]{0.0, 0.0, 0.0, 1.0}, 4);
		constructSquarePoints();
		T = null;
		x = 0;
		y = 0;
		Log.i(TAG, "Constructor");
	}
	
	private void constructSquarePoints() {
		squareScreenPoints = new ArrayList<Point>();
		squareSpacePoints = new ArrayList<Matrix>();
		double[] td1 = {20, 20, 0.0, 1.0};
		double[] td2 = {20, -20, 0.0, 1.0};
		double[] td3 = {-20, -20, 0.0, 1.0};
		double[] td4 = {-20, 20, 0.0, 1.0};
		squareSpacePoints.add(new Matrix( td1, 4));
		squareSpacePoints.add(new Matrix( td2, 4));
		squareSpacePoints.add(new Matrix( td3, 4));
		squareSpacePoints.add(new Matrix(td4, 4));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0, 0));
	}
	
	public void setGeometry(int w, int h) {
		width = w;
		height = h;
		Log.i(TAG, "width: " + width + ", height: " + height );
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.TRANSPARENT);
		canvas.drawPaint(paint);
		
		drawBorder(paint, canvas);
		drawVisibility(paint, canvas);

		switch (spaam.status) {
		case CALIB_RAW:
//			drawPoint(paint, canvas, PointType.PointCursor);
			drawText(paint, canvas, "Current Alignments: " + spaam.countCurrent + " / " + spaam.countMax);
			drawAuxiliaryPoint(paint, canvas);
			break;
		case DONE_RAW:
			drawText(paint, canvas, "SPAAM done");
			//drawSquare(paint, canvas);
			drawSquareBorder(paint, canvas);
			drawPoint(paint, canvas, PointType.PointAlign);
			break;
		case CALIB_ADD:
			drawPoint(paint, canvas, PointType.PointCursorAdd);
			drawText(paint, canvas, "Additional Alignments: " + spaam.countTuple + " / " + spaam.countAddMax);
			drawSquareBorder(paint, canvas);
			drawPoint(paint, canvas, PointType.PointAlign);
			break;
		case DONE_ADD:
			drawText(paint, canvas, "Additional SPAAM done: " + spaam.countTuple);
			//drawSquare(paint, canvas);
			drawSquareBorder(paint, canvas);
			drawPoint(paint, canvas, PointType.PointAlignAdd);
			break;
		}

		paint.setColor(Color.RED);
		canvas.drawCircle(x, y, 2, paint);
		
	}
	
	private void drawAuxiliaryPoint(Paint paint, Canvas canvas) {
		Point p = spaam.getAuxiliaryPoint();
		if ( p != null ) {
			paint.setColor(Color.RED);
			canvas.drawRect(p.x-cursorHalfLen, p.y-cursorHalfWid, p.x+cursorHalfLen, p.y+cursorHalfWid, paint);
			canvas.drawRect(p.x-cursorHalfWid, p.y-cursorHalfLen, p.x+cursorHalfWid, p.y+cursorHalfLen, paint);
		}
	}

	private void drawBorder(Paint paint, Canvas canvas) {
		paint.setColor(Color.WHITE);
		canvas.drawLine(0, 0, lc, 0, paint);
		canvas.drawLine(0, 0, 0, lc, paint);
		canvas.drawLine(width-1, 0, width-1, lc, paint);
		canvas.drawLine(width-1, 0, width-1-lc, 0, paint);
		canvas.drawLine(0, height-1, lc, height-1, paint);
		canvas.drawLine(0, height-1, 0, height-1-lc, paint);
		canvas.drawLine(width-1, height-1, width-1-lc, height-1, paint);
		canvas.drawLine(width-1, height-1, width-1, height-1-lc, paint);
	}
	
	private void drawText(Paint paint, Canvas canvas, String text) {
		paint.setColor(Color.WHITE); 
		paint.setTextSize(20);
		canvas.drawText(text, 10, 20, paint); 		
	}
	
	private void drawVisibility(Paint paint, Canvas canvas) {
		String text;
		if (visibility) {
			paint.setColor(Color.GREEN);
			text = "Tag is visible";
		}
		else {
			paint.setColor(Color.RED);
			text = "Tag is invisible";
		}
		canvas.drawRect(480, 2, 638, 32, paint);
		paint.setColor(Color.BLACK);
		paint.setTextSize(20);
		canvas.drawText(text, 500, 24, paint);
	}
	
	private void drawPointList(Paint paint, Canvas canvas, int baseColor) {
		if ( !visibility )
			return;

		byte[] ba = {(byte)(baseColor >> 24), (byte)(baseColor >> 16), (byte)(baseColor >> 8), (byte)baseColor };
		if ( centerPoints.size() == 0 )
			return;
		int[] colors = new int[centerPoints.size()];
		colors[centerPoints.size() - 1] = baseColor;
		for (int i = centerPoints.size()-2; i >= 0; i--) {
			for (int j = 0; j < 4; j++) {
				byte[] tba = {0x00, 0x00, 0x00, ba[j]};
				int tcolor = (int)(ByteBuffer.wrap(tba).getInt() * KD);
				ba[j] = (byte)tcolor;
			}
			colors[i] = ByteBuffer.wrap(ba).getInt();
		}
		for (int i = 0; i < centerPoints.size(); i++) {
			Point tp = centerPoints.get(i);
			paint.setColor(colors[i]);
			canvas.drawCircle(tp.x, tp.y, pointRadius, paint);
		}
	}

	private void drawSquareBorder(Paint paint, Canvas canvas) {
		if ( !visibility )
			return;
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(3);
		canvas.drawLine(squareScreenPoints.get(0).x, squareScreenPoints.get(0).y,
				squareScreenPoints.get(1).x, squareScreenPoints.get(1).y, paint);
		canvas.drawLine(squareScreenPoints.get(1).x, squareScreenPoints.get(1).y,
				squareScreenPoints.get(2).x, squareScreenPoints.get(2).y, paint);
		canvas.drawLine(squareScreenPoints.get(2).x, squareScreenPoints.get(2).y,
				squareScreenPoints.get(3).x, squareScreenPoints.get(3).y, paint);
		canvas.drawLine(squareScreenPoints.get(3).x, squareScreenPoints.get(3).y,
				squareScreenPoints.get(0).x, squareScreenPoints.get(0).y, paint);
		paint.setStrokeWidth(0);
	}
	
	private void drawSquare(Paint paint, Canvas canvas) {
		if ( !visibility )
			return;
		paint.setColor(Color.GRAY);
		Path wallpath = new Path();
		wallpath.reset(); // only needed when reusing this path for a new build
		wallpath.moveTo(squareScreenPoints.get(0).x, squareScreenPoints.get(0).y); // used for first point
		wallpath.lineTo(squareScreenPoints.get(1).x, squareScreenPoints.get(1).y);
		wallpath.lineTo(squareScreenPoints.get(2).x, squareScreenPoints.get(2).y);
		wallpath.lineTo(squareScreenPoints.get(3).x, squareScreenPoints.get(3).y);
		wallpath.lineTo(squareScreenPoints.get(0).x, squareScreenPoints.get(0).y);
		canvas.drawPath(wallpath, paint);
	}
	
	private void drawPoint(Paint paint, Canvas canvas, PointType type) {
		if ( type == PointType.PointAlign ) {
			drawPointList(paint, canvas, Color.GREEN);
		}
		else if ( type == PointType.PointCursor){
			Point cursorPoint = spaam.getLastCursorPoint();
			if (cursorPoint == null)
				return;
			paint.setColor(Color.parseColor("#CD5C5C"));
			canvas.drawCircle(cursorPoint.x, cursorPoint.y, pointRadius, paint);
		}
		else if ( type == PointType.PointCursorAdd){
			Point cursorPoint = spaam.getLastAddCursorPoint();
			if (cursorPoint == null)
				return;
			paint.setColor(Color.parseColor("#CD5C5C"));
			canvas.drawCircle(cursorPoint.x, cursorPoint.y, pointRadius, paint);
		}
		else if ( type == PointType.PointAlignAdd ){
			drawPointList(paint, canvas, Color.MAGENTA);
		}
	}

	public void updateTransformation(Matrix t) {
		if ( t != null ) {
			T = t;
			visibility = true;
			if ( spaam.status != SPAAMStatus.CALIB_RAW ) {
				updateCenterPoints();
				updateSquarePoints();
			}
		}
		else {
			visibility = false;
			T = null;
			centerPoints.clear();
		}
	}

	public void updateXY(int X, int Y){
		x = X;
		y = Y;
	}
	
	public void updateCenterPoints(){
		if ( T == null )
			return;
		centerPoints.add(spaam.calculateScreenPoint(T, singlePoint));
		if (centerPoints.size() >= MAX_ALIGNED)
			centerPoints.remove(0);
	}
	
	public void updateSquarePoints() {
		if ( T == null )
			return;
		Point tp;
		if (squareScreenPoints.size() == 4) {
			for (int i = 0; i < 4; i++) {
				tp = spaam.calculateScreenPoint(T, squareSpacePoints.get(i));
				squareScreenPoints.get(i).set(tp.x, tp.y);
			}
		}
		else {
			for (int i = 0; i < 4; i++) {
				tp = spaam.calculateScreenPoint(T, squareSpacePoints.get(i));
				squareScreenPoints.add(tp);
			}
		}
	}
	

}
