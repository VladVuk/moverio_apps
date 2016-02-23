/*
 *  InteractiveView.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.osteval.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lcsr.moverio.osteval.util.SPAAM.SPAAMStatus;

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
	private int lc = 60;
	private SPAAM spaam;
	private Matrix T; // Current Transformation
	
	private static int MAX_ALIGNED = 10;
	private static double KD = 0.8;
	
	private List<Point> centerPoints;
	private Matrix singlePoint;
	private int count = 0;
	private List<Matrix> squareSpacePoints; // length = 4
	private List<Point> squareScreenPoints; // length = 4
	private boolean visibility = false;
	
	public static enum PointType {PointCursor, PointCursorAdd, PointAlign, PointAlignAdd};
	
	public InteractiveView(Context context, SPAAM calc) {
		super(context);
		spaam = calc;
		centerPoints = new ArrayList<Point>();
		count = -1;
		singlePoint = new Matrix( new double[]{0.0, 0.0, 0.0, 1.0}, 4);
		constructSquarePoints();
		T = null;
		Log.i(TAG, "Constructor");
	}


	public void nextTarget(){
		count = count + 1;
		switch(count % 4){
			case 0:
				singlePoint = new Matrix( new double[]{40.0, 40.0, 0.0, 1.0}, 4);
				break;
			case 1:
				singlePoint = new Matrix( new double[]{40.0, -40.0, 0.0, 1.0}, 4);
				break;
			case 2:
				singlePoint = new Matrix( new double[]{-40.0, -40.0, 0.0, 1.0}, 4);
				break;
			case 3:
				singlePoint = new Matrix( new double[]{-40.0, 40.0, 0.0, 1.0}, 4);
				break;
			default:
				break;
		}
		centerPoints.clear();
	}



	public void lastTarget(){
		count = count - 1;
		if (count < 0){
			count = 0;
		}
		switch(count % 4){
			case 0:
				singlePoint = new Matrix( new double[]{40.0, 40.0, 0.0, 1.0}, 4);
				break;
			case 1:
				singlePoint = new Matrix( new double[]{40.0, -40.0, 0.0, 1.0}, 4);
				break;
			case 2:
				singlePoint = new Matrix( new double[]{-40.0, -40.0, 0.0, 1.0}, 4);
				break;
			case 3:
				singlePoint = new Matrix( new double[]{-40.0, 40.0, 0.0, 1.0}, 4);
				break;
			default:
				break;
		}
		centerPoints.clear();
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
		squareSpacePoints.add(new Matrix( td4, 4));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0,0));
		squareScreenPoints.add(new Point(0,0));
	}
	
	public void setGeometry(int w, int h) {
		width = w;
		height = h;
		Log.i(TAG, "width: " + width + ", height: " + height);
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
			break;
		case DONE_RAW:
			drawText(paint, canvas, "SPAAM done");
			drawSquareBorder(paint, canvas);
			drawPoint(paint, canvas, PointType.PointAlign);
			break;
		case CALIB_ADD:
			break;
		case DONE_ADD:
			break;
		}
		
	}


	private void drawBorder(Paint paint, Canvas canvas) {
		paint.setColor(Color.WHITE);
		canvas.drawLine(0, 0, lc, 0, paint);
		canvas.drawLine(0, 0, 0, lc, paint);
		canvas.drawLine(width-1, 0, width-1, lc, paint);
		canvas.drawLine(width-1, 0, width-1-lc, 0, paint);
		canvas.drawLine(0, height-1, lc, height-1, paint);
		canvas.drawLine(0, height - 1, 0, height - 1 - lc, paint);
		canvas.drawLine(width - 1, height - 1, width - 1 - lc, height - 1, paint);
		canvas.drawLine(width - 1, height - 1, width - 1, height - 1 - lc, paint);
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

	
	private void drawPoint(Paint paint, Canvas canvas, PointType type) {
		if ( type == PointType.PointAlign ) {
			drawPointList(paint, canvas, Color.GREEN);
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
