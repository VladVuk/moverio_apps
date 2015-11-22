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
import org.lcsr.moverio.stereospaam.util.SPAAM;
import org.lcsr.moverio.stereospaam.stereo.StereoInteractiveView;
import org.lcsr.moverio.stereospaam.util.SPAAMConsole;
import org.lcsr.moverio.stereospaam.util.VisualTracker;


import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private StereoRenderer renderer;
	private StereoInteractiveView intView;
	private SPAAMConsole spaam;
	private FrameLayout mainLayout;
	
	private VisualTracker visualTracker;
	private Matrix T;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		visualTracker = new VisualTracker();

		spaam = new SPAAMConsole();

		renderer = new StereoRenderer(visualTracker, spaam);

		setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);


		mainLayout.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						break;
					case MotionEvent.ACTION_UP:
						T = visualTracker.getMarkerTransformation();
						intView.updateTransformation(T);
						renderer.updateTransformation(T);
						if ( T == null )
							Log.i(TAG, "Clicked but tag is not visible");
						else {
							spaam.clicked(T);
							Log.i(TAG, "Clicked");
						}
						break;
					default:
						break;
				}
				return true;
			}
		});
    }


    
    @Override
    public void onFrameProcessed() {
		T = visualTracker.getMarkerTransformation();
		intView.updateTransformation(T);
		renderer.updateTransformation(T);
		intView.invalidate();
    }

    
    
    /**
     * By overriding {@link supplyRenderer}, the custom renderer will be used rather than
     * the default renderer which does nothing.
     * @return The custom renderer to use.
     */
    @Override
    protected ARRenderer supplyRenderer() {
    	return renderer;
    }
    
    /**
     * By overriding {@link supplyFrameLayout}, the layout within this Activity's UI will be 
     * used.
     */
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
    	super.onStop();
		spaam.clearup();
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
