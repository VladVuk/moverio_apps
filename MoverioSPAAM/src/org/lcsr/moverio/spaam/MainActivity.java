package org.lcsr.moverio.spaam;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.igtlink.IGTLServer;
import org.lcsr.moverio.spaam.R;
import org.lcsr.moverio.spaam.util.*;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

import Jama.Matrix;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private MainRenderer renderer = new MainRenderer();

	private FrameLayout mainLayout;
	
	private Button igtlButton;
	private TextView igtlMessage;
	private boolean igtlStatus = false;
	private IGTLServer igtlServer;

	private SPAAM spaamCalculator = new SPAAM();
	
	private InteractiveView intView;
	
	private VisualTracker visualTracker = new VisualTracker();	
	
	private String ipAddress;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);
        
        spaamCalculator.setSinglePointLocation( 0.0, 0.0, 0.0 );
        spaamCalculator.setMaxAlignment(6);
        
        ipAddress = getIPAddress();
        ((TextView)this.findViewById(R.id.IPAddressDisp)).setText("IP: " + ipAddress);
        Log.i(TAG, ipAddress);
        
        igtlMessage = (TextView)this.findViewById(R.id.OpenIGTDisp);
        
        igtlButton = (Button)this.findViewById(R.id.IGTButton);
        igtlButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !igtlStatus ) {
					igtlButton.setText("OpenIGTLink stop");
					igtlButton.setBackgroundColor(Color.GREEN);
			        igtlServer = new IGTLServer(igtlMsgHandler);
			        igtlStatus = true;
			        Log.i(TAG, "Start OpenIGTLink");
				}
				else {
					igtlButton.setText("OpenIGTLink start");
					igtlButton.setBackgroundColor(Color.MAGENTA);
					igtlServer.stop();
					igtlServer = null;
					igtlStatus = false;
			        Log.i(TAG, "Stop OpenIGTLink");
				}
			}        	
        });        

        mainLayout.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		switch (event.getAction()) {
        	    case MotionEvent.ACTION_DOWN:
        	    	if ( !SPAAM.OK ){
	        	    	int x = (int) event.getX();
	                    int y = (int) event.getY();
						if( visualTracker.getMarkerVisibility() ) {
		                    intView.setXY(x, y, InteractiveView.PointType.PointCursor);
		                    Matrix t = visualTracker.getMarkerTransformation();
							spaamCalculator.newAlignment(x, y, t);
							intView.setAlignCount(spaamCalculator.getListSize());
						}
						else
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
    
    @SuppressLint("HandlerLeak")
	private Handler igtlMsgHandler = new Handler() {  
        public void handleMessage (Message msg) { 
            switch(msg.what) {
            case 1:
            	TransformNR t = (TransformNR)msg.obj;
        		igtlMessage.setText("Type: Transform" + System.getProperty("line.separator")
						+ "Position[0]: " + t.getPositionArray()[0] + System.getProperty("line.separator")
						+ "Position[1]: " + t.getPositionArray()[1] + System.getProperty("line.separator")
						+ "Position[2]: " + t.getPositionArray()[2]);
                break;  
            case 0:  
                break;  
            }  
        }  
    };
    

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
		Matrix T = visualTracker.getMarkerTransformation();
    	if ( T != null && igtlStatus ) {
			igtlServer.sendTransform(T);
    	}
    	if ( intView != null  ) {
    		if ( SPAAM.OK ) {
	    		if ( T != null) {
	    			intView.setXY( spaamCalculator.getSreenPointAligned(T), InteractiveView.PointType.PointAlign);
	    		}
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
        intView = new InteractiveView(this);
        intView.setMaxAlignment(6);
        intView.setGeometry(640,480);   
    	mainLayout.addView(intView);
        Log.i(TAG, "InteractiveView added");
    	hideCameraPreview();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mainLayout.removeAllViews();
    	spaamCalculator.clearSPAAM();
    	if ( igtlServer != null ) {
    		igtlServer.stop(); 
    		igtlServer = null;
    		igtlStatus = false;
    	}
    }
    
    private void hideCameraPreview() {
    	//mainLayout.removeView(getCameraPreview());
    	//mainLayout.addView(getCameraPreview(), new LayoutParams(1, 1));
    	FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(1, 1);
    	getCameraPreview().setLayoutParams(layoutParams);
    }
    
	
	@SuppressWarnings("deprecation")
	protected String getIPAddress() {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }
}
