/*
 *  InteractiveView.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.stereospaam.stereo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lcsr.moverio.stereospaam.util.Alignment;
import org.lcsr.moverio.stereospaam.util.SPAAM.SPAAMStatus;
import org.lcsr.moverio.stereospaam.util.SPAAMConsole;

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
public class StereoInteractiveView extends View {
	
	private static String TAG = "InteractiveView";
	private int width, height;
	private SPAAMConsole spaam;
	private Matrix T;
	private Point p1, p2;

	public StereoInteractiveView(Context context) {
		super(context);
		Log.i(TAG, "constructed");
	}

	public void loadSPAAMConsole(SPAAMConsole spaamConsole){
		spaam = spaamConsole;
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		width = getWidth();
		height = getHeight();

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.TRANSPARENT);
		canvas.drawPaint(paint);

		drawStereoViewBorder(paint, canvas);

		// canvas scaled
		canvas.scale(0.5f, 1.0f);
		drawStereoVisibility(paint, canvas);

		switch (spaam.getStatus()) {
			case CALIB_RAW:
				p1 = spaam.getNextScreenPointLeft();
				p2 = spaam.getNextScreenPointRight();
				drawStereoCrosshair(paint, canvas, p1, p2);
				p1 = spaam.getLastCursorPointLeft();
				p2 = spaam.getLastCursorPointRight();
				drawStereoPoint(paint, canvas, p1, p2, Color.RED);
				drawStereoText(paint, canvas, "Alignments: " + spaam.getAlignCount() + " / " + spaam.getAlignMax());
				break;
			case DONE_RAW:
				p1 = spaam.calculateScreenPointLeft(T, null);
				p2 = spaam.calculateScreenPointRight(T, null);
				drawStereoPoint(paint, canvas, p1, p2, Color.GREEN);
				drawStereoText(paint, canvas, "SPAAM done");
				break;
		}

		// canvas scale back
		canvas.scale(2.0f, 1.0f);
	}


	private void drawStereoViewBorder(Paint paint, Canvas canvas) {
		paint.setColor(Color.WHITE);
		int length = 40;
		// Draw on the left screen
		int x1 = 0;
		int y1 = 0;
		int x2 = width/2;
		int y2 = height;
		canvas.drawLine(x1, y1, x1+length, y1, paint);
		canvas.drawLine(x1, y1, x1, y1+length, paint);
		canvas.drawLine(x2-1, y1, x2-1, y1+length, paint);
		canvas.drawLine(x2-1, y1, x2-1-length, y1, paint);
		canvas.drawLine(x1, y2-1, x1+length, y2-1, paint);
		canvas.drawLine(x1, y2-1, x1, y2-1-length, paint);
		canvas.drawLine(x2-1, y2-1, x2-1-length, y2-1, paint);
		canvas.drawLine(x2-1, y2-1, x2-1, y2-1-length, paint);
		// Draw on the right screen
		x1 = width/2;
		y1 = 0;
		x2 = width;
		y2 = height;
		canvas.drawLine(x1, y1, x1+length, y1, paint);
		canvas.drawLine(x1, y1, x1, y1+length, paint);
		canvas.drawLine(x2-1, y1, x2-1, y1+length, paint);
		canvas.drawLine(x2-1, y1, x2-1-length, y1, paint);
		canvas.drawLine(x1, y2-1, x1+length, y2-1, paint);
		canvas.drawLine(x1, y2-1, x1, y2-1-length, paint);
		canvas.drawLine(x2-1, y2-1, x2-1-length, y2-1, paint);
		canvas.drawLine(x2-1, y2-1, x2-1, y2-1-length, paint);
	}

	private void drawStereoText(Paint paint, Canvas canvas, String text) {
		// Make sure that the canvas is scaled before entering this function
		paint.setColor(Color.WHITE);
		paint.setTextSize(20);
		int border = 20;
		canvas.drawText(text, border/2, border, paint);
		canvas.drawText(text, width + border/2, border, paint);
	}

	private void drawStereoVisibility(Paint paint, Canvas canvas) {
		// Make sure that the canvas is scaled before entering this function
		// Visibility information is displayed at the upper right corner
		String text;
		if ( T != null ) {
			paint.setColor(Color.GREEN);
			text = "Tag is visible";
		}
		else {
			paint.setColor(Color.RED);
			text = "Tag is invisible";
		}
		int rectWidth = 160;
		int rectHeight = 30;
		int rectBorder = 2;
		int textPositionX = 820;
		int textPositionY = 24;
		canvas.drawRect(width-rectBorder/2-rectWidth, rectBorder, width-rectBorder/2, rectHeight+rectBorder, paint);
		canvas.drawRect(width*2-rectBorder/2-rectWidth, rectBorder, width*2-rectBorder/2, rectHeight+rectBorder, paint);
		paint.setColor(Color.BLACK);
		paint.setTextSize(20);
		canvas.drawText(text, textPositionX, textPositionY, paint);
		canvas.drawText(text, width + textPositionX, textPositionY, paint);
	}

	private void drawStereoPoint(Paint paint, Canvas canvas, Point p1, Point p2, int color) {
		if ( p1 == null || p2 == null )
			return;
		paint.setColor(color);
		canvas.drawCircle((float) p1.x, (float) p1.y, 5.0f, paint);
		canvas.drawCircle((float) p2.x + width, (float) p2.y, 5.0f, paint);
	}

	private void drawStereoCrosshair(Paint paint, Canvas canvas, Point p1, Point p2) {
		if ( p1 == null || p2 == null )
			return;
		paint.setColor(Color.GREEN);
		int length = 40;
		int thickness = 4;
		canvas.drawRect(p1.x - thickness / 2, p1.y - length / 2, p1.x + thickness / 2, p1.y + length / 2, paint);
		canvas.drawRect(p1.x - length / 2, p1.y - thickness / 2, p1.x + length / 2, p1.y + thickness / 2, paint);
		canvas.drawRect(p2.x - thickness / 2 + width, p2.y - length / 2, p2.x + thickness / 2 + width, p2.y + length / 2, paint);
		canvas.drawRect(p2.x - length / 2 + width, p2.y - thickness / 2, p2.x + length / 2 + width, p2.y + thickness / 2, paint);
	}

	public void updateTransformation(Matrix t) {
		T = t;
	}


	

}
