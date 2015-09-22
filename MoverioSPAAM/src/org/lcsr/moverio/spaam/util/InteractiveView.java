/*
 *  InteractiveView.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.View;

@SuppressLint("DrawAllocation")
public class InteractiveView extends View {
	
	private static String TAG = "InteractiveView";
	private int width, height;
	private int pointRadius = 5;
	private int auxiliaryRadius = 60;
	private int lc = 60;
	private SPAAM spaam;
	
	public static enum PointType {PointCursor, PointCursorAdd, PointAlign, PointAlignAdd};
	
	public InteractiveView(Context context, SPAAM calc) {
		super(context);
		spaam = calc;
		Log.i(TAG, "Constructor");
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

		switch (spaam.status) {
		case CALIB_RAW:
			drawPoint(paint, canvas, PointType.PointCursor);
			drawText(paint, canvas, "Current Alignments: " + spaam.countCurrent + " / " + spaam.countMax);
			drawAuxiliaryCircle(paint, canvas);
			break;
		case DONE_RAW:
			drawPoint(paint, canvas, PointType.PointAlign);
			drawText(paint, canvas, "SPAAM done");
			break;
		case CALIB_ADD:
			drawPoint(paint, canvas, PointType.PointCursorAdd);
			drawPoint(paint, canvas, PointType.PointAlign);
			drawText(paint, canvas, "Additional Alignments: " + spaam.countTuple + " / " + spaam.countAddMax);
			break;
		case DONE_ADD:
			drawPoint(paint, canvas, PointType.PointAlignAdd);
			drawText(paint, canvas, "Additional SPAAM done");
			break;
		}
	}
	
	private void drawAuxiliaryCircle(Paint paint, Canvas canvas) {
		Point p = spaam.getAuxiliaryPoint();
		if ( p != null ) {
			paint.setColor(Color.WHITE);
			paint.setAlpha(96);
			canvas.drawCircle(p.x, p.y, auxiliaryRadius, paint);
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
	
	private void drawPoint(Paint paint, Canvas canvas, PointType type) {
		if ( type == PointType.PointAlign ) {
			Point markerPoint = spaam.getMarkerPoint();
			if (markerPoint == null)
				return;
			if (markerPoint.x >= 0 && markerPoint.x < width && markerPoint.y >= 0 && markerPoint.y < height) {
				paint.setColor(Color.GREEN);
				canvas.drawCircle(markerPoint.x, markerPoint.y, pointRadius, paint);
			}
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
		else {
			Point markerPointAdd = spaam.getMarkerPointAdd();
			if (markerPointAdd == null)
				return;
			paint.setColor(Color.MAGENTA);
			canvas.drawCircle(markerPointAdd.x, markerPointAdd.y, pointRadius, paint);
		}
	}
}
