/*
 *  InteractiveView.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam.util;

import Jama.Matrix;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

@SuppressLint("DrawAllocation")
public class InteractiveView extends View {
	
	private static String TAG = "InteractiveView";
	private int width, height;
	private int pointRadius = 5;
	private int auxiliaryRadius = 60;
	private int[] auxiliaryX;
	private int[] auxiliaryY;
	private int lc = 60;
	private SPAAM spaamCalculator;
	
	public static enum PointType {PointCursor, PointAlign};
	
	public InteractiveView(Context context, SPAAM calc) {
		super(context);
		spaamCalculator = calc;
		auxiliaryX = new int[spaamCalculator.countMax];
		auxiliaryY = new int[spaamCalculator.countMax];
		auxiliaryX[0] = 120;
		auxiliaryY[0] = 100;
		auxiliaryX[1] = 520;
		auxiliaryY[1] = 380;
		auxiliaryX[2] = 120;
		auxiliaryY[2] = 380;
		auxiliaryX[3] = 520;
		auxiliaryY[3] = 100;
		auxiliaryX[4] = 220;
		auxiliaryY[4] = 240;
		auxiliaryX[5] = 420;
		auxiliaryY[5] = 240;
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

		if ( spaamCalculator.OK ) {
			drawPoint(paint, canvas, PointType.PointAlign);
			drawText(paint, canvas, "SPAAM done");
		}
		else {
			drawPoint(paint, canvas, PointType.PointCursor);
			drawText(paint, canvas, "Current Alignments: " + spaamCalculator.countCurrent + " / " + spaamCalculator.countMax);
			drawAuxiliaryCircle(paint, canvas);
		}
	}
	
	private void drawAuxiliaryCircle(Paint paint, Canvas canvas) {
		paint.setColor(Color.WHITE);
		paint.setAlpha(96);
		canvas.drawCircle(auxiliaryX[spaamCalculator.countCurrent], auxiliaryY[spaamCalculator.countCurrent], auxiliaryRadius, paint);		
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
			Matrix markerPoint = spaamCalculator.markerPoint;
			if (markerPoint == null)
				return;
			int alignX = (int)(markerPoint.get(0, 0)/markerPoint.get(2, 0));
			int alignY = (int)(markerPoint.get(1, 0)/markerPoint.get(2, 0));
			if (alignX >= 0 && alignX < width && alignY >= 0 && alignY < height) {
				paint.setColor(Color.GREEN);
				canvas.drawCircle(alignX, alignY, pointRadius, paint);
			}
		}
		else {
			Matrix cursorPoint = spaamCalculator.getLastCursorPoint();
			if (cursorPoint == null)
				return;
			int cursorX = (int)cursorPoint.get(0, 0);
			int cursorY = (int)cursorPoint.get(1, 0);
			paint.setColor(Color.parseColor("#CD5C5C"));
			canvas.drawCircle(cursorX, cursorY, pointRadius, paint);
		}
	}
}
