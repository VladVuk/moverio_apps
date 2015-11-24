/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.stereospaam;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.stereospaam.stereo.StereoInteractiveView;
import org.lcsr.moverio.stereospaam.util.SPAAMConsole;
import org.lcsr.moverio.stereospaam.util.VisualTracker;

import jp.epson.moverio.bt200.DisplayControl;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;


import Jama.Matrix;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private StereoRenderer renderer;
	private StereoInteractiveView intView;
	private SPAAMConsole spaam;
	private FrameLayout mainLayout;
	private PopupWindow popupWindow;
	private View popupWindowView;
	
	private VisualTracker visualTracker;
	private Matrix T;


	private DisplayControl displayControl;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		displayControl = new DisplayControl(this);
		displayControl.setMode(DisplayControl.DISPLAY_MODE_3D, false);

		visualTracker = new VisualTracker();

		spaam = new SPAAMConsole();

		renderer = new StereoRenderer(this, visualTracker, spaam);

		setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);

		LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupWindowView = inflater.inflate(R.layout.popup_window, (ViewGroup) findViewById(R.id.popup_view));
		popupWindow = new PopupWindow(popupWindowView, 960, 400, true);


		mainLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						break;
					case MotionEvent.ACTION_UP:
						long elapsedTime = event.getEventTime() - event.getDownTime();
						if (elapsedTime < 200) {
							T = visualTracker.getMarkerTransformation();
							intView.updateTransformation(T);
							renderer.updateTransformation(T);
							if (T == null)
								Log.i(TAG, "Clicked but tag is not visible");
							else {
								spaam.clicked(T);
								Log.i(TAG, "Clicked for spaam");
							}
						} else if (elapsedTime > 300) {
							popupWindow.showAtLocation(popupWindowView, Gravity.CENTER, 0, 0);
							Log.i(TAG, "Popup Window activated");
						}
						break;
					default:
						break;
				}
				return true;
			}
		});

		Button readButton1 = (Button) popupWindowView.findViewById(R.id.readButton1);
		readButton1.setOnClickListener(readButtonClickListener);
		Button readButton2 = (Button) popupWindowView.findViewById(R.id.readButton2);
		readButton2.setOnClickListener(readButtonClickListener);

		Button writeButton1 = (Button) popupWindowView.findViewById(R.id.writeButton1);
		writeButton1.setOnClickListener(writeButtonClickListener);
		Button writeButton2 = (Button) popupWindowView.findViewById(R.id.writeButton2);
		writeButton2.setOnClickListener(writeButtonClickListener);

		Button dismissButton1 = (Button) popupWindowView.findViewById(R.id.dismissButton1);
		dismissButton1.setOnClickListener(dismissButtonClickListener);
		Button dismissButton2 = (Button) popupWindowView.findViewById(R.id.dismissButton2);
		dismissButton2.setOnClickListener(dismissButtonClickListener);

		Button cancelButton1 = (Button) popupWindowView.findViewById(R.id.cancelButton1);
		cancelButton1.setOnClickListener(cancelButtonClickListener);
		Button cancelButton2 = (Button) popupWindowView.findViewById(R.id.cancelButton2);
		cancelButton2.setOnClickListener(cancelButtonClickListener);
    }

	private View.OnClickListener readButtonClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			spaam.readFile();
			popupWindow.dismiss();
			Log.i(TAG, "Read file button pressed");
		}
	};

	private View.OnClickListener writeButtonClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			spaam.writeFile();
			popupWindow.dismiss();
			Log.i(TAG, "Write file button pressed");
		}
	};

	private View.OnClickListener dismissButtonClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			popupWindow.dismiss();
			Log.i(TAG, "Dismiss button pressed");
		}
	};

	private View.OnClickListener cancelButtonClickListener = new View.OnClickListener(){
		@Override
		public void onClick(View v){
			spaam.cancelLast();
			popupWindow.dismiss();
			Log.i(TAG, "Cancel button pressed");
		}
	};

    
    @Override
    public void onFrameProcessed() {
		T = visualTracker.getMarkerTransformation();
		intView.updateTransformation(T);
		renderer.updateTransformation(T);
		intView.invalidate();
    }

    

    @Override
    protected ARRenderer supplyRenderer() {
    	return renderer;
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
    	return mainLayout;
    }


	@Override
    public void onResume() {
		super.onResume();
    	hideCameraPreview();
		intView = new StereoInteractiveView(this);
		intView.loadSPAAMConsole(spaam);
		mainLayout.addView(intView);
    }
    
    @Override
    public void onStop() {
		spaam.clearup();
		super.onStop();
		displayControl.setMode(DisplayControl.DISPLAY_MODE_2D, false);
    }

	@Override
	public void onPause() {
		super.onPause();
		mainLayout.removeView(intView);
	}
    
    private void hideCameraPreview() {
    	//mainLayout.removeView(getCameraPreview());
    	//mainLayout.addView(getCameraPreview(), new LayoutParams(1, 1));
    	FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(1, 1);
    	getCameraPreview().setLayoutParams(layoutParams);
    }
    

}
