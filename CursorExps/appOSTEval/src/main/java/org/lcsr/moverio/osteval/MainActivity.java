/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.osteval;

import java.io.File;
import java.io.FileOutputStream;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.osteval.util.*;

import Jama.Matrix;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private MainRenderer renderer;

	private FrameLayout mainLayout;

	// Recording stuff
	private Button recordButton;
	private boolean recording = false;
	private File recordFile;
	private FileOutputStream recordStream;

	private Button tcpButton;
	private TextView tcpMessage;
	private TCPServer tcpServer;

	private String filename;
	private Button readFileButton;
	private String recordFilename;

	private SPAAM spaam;
	
	private InteractiveView intView;
	
	private VisualTracker visualTracker;
	
	private String ipAddress;
	private EditText filenameEdit;
	
	private Matrix T, singlePoint;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		visualTracker = new VisualTracker();
        spaam = new SPAAM( 0.0, 0.0, 0.0 );
        spaam.setMaxAlignment(20);
		renderer = new MainRenderer(visualTracker);

		singlePoint = new Matrix(4,1,0.0);
		singlePoint.set(3, 0, 1.0);
        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);

        
        ipAddress = getIPAddress();
        ((TextView)this.findViewById(R.id.IPAddressDisp)).setText("IP: " + ipAddress);
        Log.i(TAG, ipAddress);

		filenameEdit = (EditText) this.findViewById(R.id.editText);

		tcpMessage = (TextView)this.findViewById(R.id.TCPDisp);
        
        tcpButton = (Button)this.findViewById(R.id.TCPButton);
		tcpServer = new TCPServer(18943);
		tcpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !tcpServer.getStatus() ) {
					tcpButton.setText("TCP/IP stop");
					tcpButton.setBackgroundColor(Color.GREEN);
					tcpServer.setHandler(uiHandler);
					tcpServer.startTask();
			        Log.i(TAG, "Start TCP/IP");
				}
				else {
					tcpButton.setText("TCP/IP start");
					tcpButton.setBackgroundColor(Color.MAGENTA);
					tcpServer.stopTask();
			        Log.i(TAG, "Stop TCP/IP");
				}
			}        	
        });


		readFileButton = (Button)this.findViewById(R.id.ReadButton);
		readFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				filename = filenameEdit.getText().toString();
				File file = new File(Environment.getExternalStorageDirectory(), filename + ".txt");
				if (!spaam.readFile(file))
					Toast.makeText(MainActivity.this, "Read file failed", Toast.LENGTH_SHORT).show();
				else {
					Toast.makeText(MainActivity.this, "File loaded", Toast.LENGTH_SHORT).show();
					if (!recording)
						recordButton.performClick();
					if (!tcpServer.getStatus())
						tcpButton.performClick();
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
				}
			}
		});


		recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!recording) {
					try {
						recordFilename = filename + "_EV.txt";
						recordFile = new File(Environment.getExternalStorageDirectory(), recordFilename);
						if (!recordFile.exists())
							recordFile.createNewFile();
						recordStream = new FileOutputStream(recordFile);
						recording = true;
						recordButton.setText("Stop");
						Log.i(TAG, "recording started");
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						recordStream.close();
						recording = false;
						recordButton.setText("Record");
						Log.i(TAG, "recording ended");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

//		mainLayout.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				intView.nextTarget();
//			}
//		});


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
					int s = u.getInt("s");
					String sDisp = "Message: " + "(" + x + ", " + y + ", " + z + ", " + s + ")";
					Log.i(TAG, sDisp);
					tcpMessage.setText(sDisp);

					if (z == 5) {
						intView.nextTarget();
						recordClick(x, y);
					}
					else if (z == 6) {
						intView.lastTarget();
						recordCancel(x, y);
					}
					break;
				default:
					break;
			}
		}
	};

	
    
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
		if ( recording ) {
			try {
				if (T == null) {
					String ss = "#" + System.getProperty("line.separator");
					recordStream.write(ss.getBytes());
				} else {
					String ss = "* " + T.get(0,0) + " " + T.get(0,1) + " " + T.get(0,2) + " " + T.get(0,3) +
							" " + T.get(1,0) + " " + T.get(1,1) + " " + T.get(1,2) + " " + T.get(1,3) +
							" " + T.get(2,0) + " " + T.get(2,1) + " " + T.get(2,2) + " " + T.get(2,3) +  System.getProperty("line.separator");
					recordStream.write(ss.getBytes());

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	if ( intView != null  ) {
			intView.updateTransformation(T);
    		intView.invalidate();
    	}    	
    }

	public void recordClick(int x, int y){
		if (recording) {
			try {
				String ss = "$ " + x + " " + y + System.getProperty("line.separator");
				recordStream.write(ss.getBytes());
			} catch( Exception e){
				e.printStackTrace();
			}
		}
	}

	public void recordCancel(int x, int y) {
		if (recording) {
			try {
				String ss = "% " + x + " " + y + System.getProperty("line.separator");
				recordStream.write(ss.getBytes());
			} catch( Exception e){
				e.printStackTrace();
			}
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
		CaptureCameraPreview.zoomValue = 0;
		CaptureCameraPreview.exposureComp = 0;
		intView = new InteractiveView(this, spaam);
    	mainLayout.addView(intView);
        intView.setGeometry(640, 480);
		Log.i(TAG, "InteractiveView added");
    	hideCameraPreview();
    }

	@Override
	public void onPause(){
		if (recording){
			recordButton.performClick();
		}
		super.onPause();
		if (tcpServer.getStatus()){
			tcpServer.stopTask();
		}
	}
    
    @Override
    public void onStop() {
    	super.onStop();
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
