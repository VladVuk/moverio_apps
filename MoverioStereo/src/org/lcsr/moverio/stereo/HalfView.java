package org.lcsr.moverio.stereo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

@SuppressLint("RtlHardcoded")
public class HalfView extends View {

	private String TAG;
	public static enum HalfViewType {LEFT, RIGHT};
	private HalfViewType type;
	private float offset = 0;
	private float w = 50;
	private float h = 60;
	
	public HalfView(Context context, HalfViewType t) {
		super(context);
		if (t == HalfViewType.LEFT) {
			TAG = "HalfViewLeft";
			type = t;
		}
		else {
			TAG = "HalfViewRight";
			type = t;
		}
		Log.i(TAG, "Constructed");
	}
	

	@Override
	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.TRANSPARENT);
		canvas.drawPaint(paint);
		paint.setColor(Color.MAGENTA);
		canvas.drawRect(240+offset-w/2, 200, 240+offset+w/2, 200+h, paint);
	}


	public void updateDrag(float dist) {
		if (type == HalfViewType.LEFT)
			offset -= dist / 5;
		else
			offset += dist / 5;
	}
	
}
