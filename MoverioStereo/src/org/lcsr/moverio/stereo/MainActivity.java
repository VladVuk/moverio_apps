/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.stereo;

import org.lcsr.moverio.stereo.HalfView.HalfViewType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity  {
	
	private static String TAG = "MainActivity";
	
	private HalfView hvLeft, hvRight;
	private LinearLayout topLayout;
	private float y1, y2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		topLayout = (LinearLayout)this.findViewById(R.id.topLayout);
		
		topLayout.setOnTouchListener(new OnTouchListener() {
	    	@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					y1 = event.getY();
					Log.i(TAG, "Touch down");
				}
				else if(event.getAction() == MotionEvent.ACTION_UP) {
					y2 = event.getY();
					Log.i(TAG, "Touch up");
					float dist = y2-y1;
					hvLeft.updateDrag(dist);
					hvRight.updateDrag(dist);
					hvLeft.invalidate();
					hvRight.invalidate();
				}
				return true;
        	}
        });
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
	    return false;
	}
	
	@SuppressLint("RtlHardcoded")
	@Override
	public void onResume() {
		super.onResume();
		hvLeft = new HalfView(this, HalfViewType.LEFT);
		hvRight = new HalfView(this, HalfViewType.RIGHT);
		LayoutParams layoutParamsLeft = new LayoutParams(480, LayoutParams.MATCH_PARENT);
		layoutParamsLeft.gravity = Gravity.LEFT;
		topLayout.addView(hvLeft, layoutParamsLeft);
		LayoutParams layoutParamsRight = new LayoutParams(480, LayoutParams.MATCH_PARENT);
		layoutParamsRight.gravity = Gravity.RIGHT;
		topLayout.addView(hvRight, layoutParamsRight);
	}

	@Override
	public void onStop() {
		super.onStop();
		topLayout.removeAllViews();
		hvLeft = null;
		hvRight = null;
			
	}

}
