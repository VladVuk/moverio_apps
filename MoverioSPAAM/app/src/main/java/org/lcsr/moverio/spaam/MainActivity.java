/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.spaam;

import java.io.File;
import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.igtlink.IGTLServer;
import org.lcsr.moverio.spaam.R;
import org.lcsr.moverio.spaam.util.*;
import org.lcsr.moverio.spaam.util.SPAAM.SPAAMStatus;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import Jama.Matrix;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private MainRenderer renderer = new MainRenderer();

	private FrameLayout mainLayout;
	
	private Button cancelButton;
	private Button modifyButton;
	private Button igtlButton;
	private TextView igtlMessage;
	private boolean igtlStatus = false;
	private IGTLServer igtlServer;
	
	private Button readFileButton, writeFileButton;
	private String filename = "G.txt";

	private SPAAM spaam;
	
	private InteractiveView intView;
	
	private VisualTracker visualTracker = new VisualTracker();	
	
	private String ipAddress;
	
	private Matrix T;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);
        
        spaam = new SPAAM( 0.0, 0.0, 0.0 );
        spaam.setMaxAlignment(20);
        
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
        
        cancelButton = (Button)this.findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( spaam.cancelLast())
					Toast.makeText(MainActivity.this, "Last alignment cancelled", Toast.LENGTH_SHORT).show();
				else
					buildAlertMessageNoCube("You have not made any alignment");
			}
        });
        
        modifyButton = (Button)this.findViewById(R.id.ModifyButton);
        modifyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( spaam.startAddCalib() )
					Toast.makeText(MainActivity.this, "Start additional calibration", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, "Cannot start additional calibration", Toast.LENGTH_SHORT).show();		
			}
		});        

        readFileButton = (Button)this.findViewById(R.id.ReadButton);
        readFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(Environment.getExternalStorageDirectory(), filename);
				if ( !spaam.readFile(file))
					buildAlertMessageNoCube("Read file falied");
				else
					Toast.makeText(MainActivity.this, "File loaded", Toast.LENGTH_SHORT).show();
			}
        });

        writeFileButton = (Button)this.findViewById(R.id.WriteButton);
        writeFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( spaam.status == SPAAMStatus.CALIB_RAW )
					buildAlertMessageNoCube("SPAAM not done");
				else {
					File sdcard = Environment.getExternalStorageDirectory();
					File file = new File(sdcard, filename);
					if ( !spaam.writeFile(file))
						buildAlertMessageNoCube("Write file falied");
					else
						Toast.makeText(MainActivity.this, "File written", Toast.LENGTH_SHORT).show();
				}
			}
        });
        
        mainLayout.setOnTouchListener(new OnTouchListener() {
        	@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {
    	    	int x = (int) event.getX();
                int y = (int) event.getY();
                T = visualTracker.getMarkerTransformation();
                transformationChanged();
                if ( !visualTracker.visibility ) {
                	buildAlertMessageNoCube("You need to see the cube in order for the calibration to work");
                	return true;
                }
                else {
	        		switch (event.getAction()) {
	        	    case MotionEvent.ACTION_DOWN:
	        	    	if ( spaam.status == SPAAMStatus.CALIB_RAW )
							spaam.newAlignment(x, y, T);
						else if ( spaam.status == SPAAMStatus.CALIB_ADD || spaam.status == SPAAMStatus.DONE_ADD )
							spaam.newTuple(x, y, T);
						break;
	        	    case MotionEvent.ACTION_UP:
	        	        v.performClick();
	        	        break;
	        	    default:
	        	        break;
	        	    }
	        		return true;
                }
        	}
        });
    }
    
    @SuppressLint("HandlerLeak")
	private Handler igtlMsgHandler = new Handler() {  
        public void handleMessage (Message msg) { 
            switch(msg.what) {
            case 1:
            	TransformNR t = (TransformNR)msg.obj;
        		igtlMessage.setText("Received Package: Transform" + System.getProperty("line.separator")
						+ "Position[0]: " + t.getPositionArray()[0] + System.getProperty("line.separator")
						+ "Position[1]: " + t.getPositionArray()[1] + System.getProperty("line.separator")
						+ "Position[2]: " + t.getPositionArray()[2]);
                break;  
            case 0:  
                break;
            }
        }  
    };
    

	private void buildAlertMessageNoCube(String alertText)
	{
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(alertText)
				.setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,  final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}
	
	
    
    @Override
    public void onFrameProcessed() {
		T = visualTracker.getMarkerTransformation();
		transformationChanged();
    }
    
    public void transformationChanged() {
    	if ( T != null && igtlStatus ) {
			igtlServer.sendTransform(T);
    	}
    	if ( intView != null  ) {
			intView.updateTransformation(T);
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
        intView = new InteractiveView(this, spaam);
    	mainLayout.addView(intView);
        intView.setGeometry(640, 480);
        Log.i(TAG, "InteractiveView added");
    	hideCameraPreview();
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	mainLayout.removeAllViews();
    	spaam.clearSPAAM();
    	spaam = null;
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
