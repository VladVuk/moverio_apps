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
	private int cursorX, cursorY;
	private int alignX, alignY;
	private int width, height;
	private int radius = 5;
	private boolean valid = true;
	private int alignCount;
	private int countMax = 6;
	private String alignText;
	private int lc = 60;
	
	public static enum PointType {PointCursor, PointAlign};
	
	public InteractiveView(Context context) {
		super(context);
		cursorX = cursorY = -1;
		alignX = alignY = -1;
		setAlignCount(0);
		Log.i(TAG, "Constructor");
	}
	
	public void setGeometry(int w, int h) {
		width = w;
		height = h;
		Log.i(TAG, "width: " + width + ", height: " + height );
	}
	
	public void setXY( Matrix sc, PointType type) {
		if ( type == PointType.PointAlign && sc != null ) {
			setXY( (int)(sc.get(0, 0) / sc.get(2, 0)), (int)(sc.get(1,0)/ sc.get(2,0)), type );
			valid = false;
		}
	}
	
	public void setXY(int X, int Y, PointType type) {
		if ( type == PointType.PointCursor ) {
			cursorX = X;
			cursorY = Y;
			Log.i(TAG, "Cursor location set to be ( " + cursorX + ", " + cursorY + " )");
		}
		else {
			alignX = X;
			alignY = Y;
		}
		valid = false;
	}
	
	public boolean getValid() {
		return valid;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.TRANSPARENT);
		canvas.drawPaint(paint);
		paint.setColor(Color.WHITE);
		canvas.drawLine(0, 0, lc, 0, paint);
		canvas.drawLine(0, 0, 0, lc, paint);
		canvas.drawLine(width-1, 0, width-1, lc, paint);
		canvas.drawLine(width-1, 0, width-1-lc, 0, paint);
		canvas.drawLine(0, height-1, lc, height-1, paint);
		canvas.drawLine(0, height-1, 0, height-1-lc, paint);
		canvas.drawLine(width-1, height-1, width-1-lc, height-1, paint);
		canvas.drawLine(width-1, height-1, width-1, height-1-lc, paint);

		if ( SPAAM.OK ) {
			if ( alignX > 0 && alignY > 0 ) {
				paint.setColor(Color.GREEN);
				canvas.drawCircle(alignX, alignY, radius, paint);
				paint.setColor(Color.WHITE); 
				paint.setTextSize(20);
				canvas.drawText("SPAAM Done", 10, 20, paint); 
			}
			else {
				paint.setColor(Color.WHITE); 
				paint.setTextSize(20);
				canvas.drawText("Tracked point out of range", 10, 20, paint); 
			}
				
		}
		else {
			if ( cursorX > 0 && cursorY > 0 ) {
				paint.setColor(Color.parseColor("#CD5C5C"));
				canvas.drawCircle(cursorX, cursorY, radius, paint);
				paint.setColor(Color.WHITE); 
				paint.setTextSize(20);
				canvas.drawText(alignText, 10, 20, paint); 
			}
			else {
				paint.setColor(Color.WHITE); 
				paint.setTextSize(20);
				canvas.drawText("SPAAM starts, click on " + countMax + " points", 10, 20, paint); 				
			}	
		}
		valid = true;
	}

	public void setAlignCount (int count) {
		alignCount = count;
		alignText = "Current Alignments: " + alignCount + " / " + countMax;
	}

	public void setMaxAlignment(int max) {
		countMax = max;
	}

}
