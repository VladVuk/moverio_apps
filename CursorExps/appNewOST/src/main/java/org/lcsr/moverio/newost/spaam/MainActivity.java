/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.newost.spaam;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.newost.spaam.util.*;
import org.lcsr.moverio.newost.spaam.util.SPAAM.SPAAMStatus;

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

	private MainRenderer renderer;

	private FrameLayout mainLayout;
	
	private Button cancelButton;
	private Button modifyButton;

	private Button tcpButton;
	private TextView tcpMessage;
	private TCPServer tcpServer;
	private CursorView cursorView;

	
	private Button readFileButton, writeFileButton;
	private String filename = "G.txt";

	private SPAAM spaam;
	
	private InteractiveView intView;
	
	private VisualTracker visualTracker;
	
	private String ipAddress;
	
	private Matrix T;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		visualTracker = new VisualTracker();
        spaam = new SPAAM( 0.0, 0.0, 0.0 );
        spaam.setMaxAlignment(20);
		renderer = new MainRenderer(visualTracker);

        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);

		cursorView = new CursorView(this);
        
        ipAddress = getIPAddress();
        ((TextView)this.findViewById(R.id.IPAddressDisp)).setText("IP: " + ipAddress);
        Log.i(TAG, ipAddress);
        
        tcpMessage = (TextView)this.findViewById(R.id.TCPDisp);
        
        tcpButton = (Button)this.findViewById(R.id.TCPButton);
		tcpServer = new TCPServer(18944);
		tcpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !tcpServer.getStatus() ) {
					tcpButton.setText("TCP/IP stop");
					tcpButton.setBackgroundColor(Color.GREEN);
					tcpServer.setHandler(uiHandler);
					tcpServer.startTask();
					mainLayout.addView(cursorView);
					cursorView.invalidate();
			        Log.i(TAG, "Start TCP/IP");
				}
				else {
					tcpButton.setText("TCP/IP start");
					tcpButton.setBackgroundColor(Color.MAGENTA);
					tcpServer.stopTask();
					mainLayout.removeView(cursorView);
			        Log.i(TAG, "Stop TCP/IP");
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
					// Write file for analysis
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
					String date = df.format(Calendar.getInstance().getTime());
					File file = new File(Environment.getExternalStorageDirectory(), "G-" + date + ".txt");
					// Normal write file
					// File file = new File(Environment.getExternalStorageDirectory(), filename);

					if ( !spaam.writeFile(file))
						buildAlertMessageNoCube("Write file falied");
					else
						Toast.makeText(MainActivity.this, "File written", Toast.LENGTH_SHORT).show();
				}
			}
        });

    }
    
    @SuppressLint("HandlerLeak")
	private Handler uiHandler = new Handler() {
        public void handleMessage (Message msg) { 
            switch(msg.what) {
            case 1:
				Bundle u = msg.getData();
				int x = u.getInt("x");
				int y = u.getInt("y");
				int z = u.getInt("z");
				String sDisp = "Message: " + "(" + x + ", " + y + ", " + z + ")";
        		tcpMessage.setText(sDisp);

				if ( z < 0 )
					cursorView.setXYZ(x, y, z);
				else {
					T = visualTracker.getMarkerTransformation();
					transformationChanged();
					if ( !visualTracker.visibility ) {
						buildAlertMessageNoCube("You need to see the cube in order for the calibration to work");
					}
					else {
						if ( spaam.status == SPAAMStatus.CALIB_RAW )
							spaam.newAlignment(x, y, T);
						else if ( spaam.status == SPAAMStatus.CALIB_ADD || spaam.status == SPAAMStatus.DONE_ADD )
							spaam.newTuple(x, y, T);
					}
				}
				cursorView.invalidate();
                break;  
            default:
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
        if ( spaam.updated ) {
            renderer.updateCalibMat(spaam.getCalibMat());
			spaam.updated = false;
        }
    }
    
    public void transformationChanged() {
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
		if (tcpServer.getStatus()){
			tcpServer.stopTask();
		}
    	mainLayout.removeAllViews();
    	spaam.clearSPAAM();
    	spaam = null;
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
