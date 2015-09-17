package org.lcsr.moverio.spaam;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.rendering.ARRenderer;

import org.lcsr.moverio.spaam.R;
import org.lcsr.moverio.spaam.util.*;


import Jama.Matrix;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/**
 * Another simple example that includes a small amount of user interaction.
 */
public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";
	/**
	 * A custom renderer is used to produce a new visual experience.
	 */
	private MainRenderer renderer = new MainRenderer();
	/**
	 * The FrameLayout where the AR view is displayed.
	 */
	private FrameLayout mainLayout;

	private SPAAM spaamCalculator = new SPAAM();
	
	private InteractiveView intView = null;
	
	private VisualTracker visualTracker = new VisualTracker();
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);
        intView = new InteractiveView(this);
        
        spaamCalculator.setSinglePointLocation( 0.0, 0.0, 0.0 );
        spaamCalculator.setMaxAlignment(6);
        intView.setMaxAlignment(6);

        mainLayout.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		switch (event.getAction()) {
        	    case MotionEvent.ACTION_DOWN:
        	    	if ( !SPAAM.OK ){
	        	    	int x = (int) event.getX();
	                    int y = (int) event.getY();
						if( visualTracker.getMarkerVisibility() ) {
		                    intView.setXY(x, y, InteractiveView.PointType.PointCursor); 
							spaamCalculator.newAlignment(x, y, visualTracker.getMarkerTransformation());
							intView.setAlignCount(spaamCalculator.getListSize());
						}
        	    	}
					else {
						buildAlertMessageNoCube();
					}
					break;
        	    case MotionEvent.ACTION_UP:
        	        v.performClick();
        	        break;
        	        
        	    default:
        	        break;
        	    }
        		return true;
        	}
        	
        });        
    }
    

	private void buildAlertMessageNoCube()
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("You need to see the cube in order for the calibration to work")
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,  final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
		Log.i(TAG, "Tag not shown");
	}
	
	
    
    @Override
    public void onFrameProcessed() {
    	if ( intView != null  ) {
    		if ( SPAAM.OK ) {
	    		Matrix T = visualTracker.getMarkerTransformation();
	    		if ( T != null)
	    			intView.setXY( spaamCalculator.getSreenPointAligned(T), InteractiveView.PointType.PointAlign);
    		}
    		if ( !intView.getValid() )
    			intView.invalidate();
    	}
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
    	mainLayout.addView(intView);
        intView.setGeometry(640,480);    	
    	hideCameraPreview();
    }
    
    private void hideCameraPreview() {
    	mainLayout.removeView(getCameraPreview());
    	mainLayout.addView(getCameraPreview(), new LayoutParams(1, 1));    	
    }
}