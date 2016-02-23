/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.newost.spaam;

import java.io.File;
import java.io.FileOutputStream;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

	private MainRenderer renderer;

	private FrameLayout mainLayout;
	
	private Button cancelButton;
	private Button modifyButton;

	// Recording stuff
	private Button recordButton;
	private boolean recording = false;
	private File recordFile;
	private FileOutputStream recordStream;

	private Button tcpButton;
	private TextView tcpMessage;
	private TCPServer tcpServer;
	private CursorView cursorView;

	
	private Button readFileButton, writeFileButton;
	private String filename = "G.txt";
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

		cursorView = new CursorView(this);
        
        ipAddress = getIPAddress();
        ((TextView)this.findViewById(R.id.IPAddressDisp)).setText("IP: " + ipAddress);
        Log.i(TAG, ipAddress);

		filenameEdit = (EditText) this.findViewById(R.id.editText);

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
				if ( spaam.cancelLast()) {
					Toast.makeText(MainActivity.this, "Last alignment cancelled", Toast.LENGTH_SHORT).show();
					recordCancel();
				}
				else
					Toast.makeText(MainActivity.this, "You have not made any alignment", Toast.LENGTH_SHORT).show();
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
				File file = new File(Environment.getExternalStorageDirectory(), filenameEdit.getText().toString() + ".txt");
				if ( !spaam.readFile(file))
					Toast.makeText(MainActivity.this, "Read file failed", Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(MainActivity.this, "File loaded", Toast.LENGTH_SHORT).show();
			}
		});

		writeFileButton = (Button)this.findViewById(R.id.WriteButton);
		writeFileButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (spaam.status == SPAAMStatus.CALIB_RAW)
					Toast.makeText(MainActivity.this, "SPAAM not done", Toast.LENGTH_SHORT).show();
				else {
					// Write file for analysis
					// DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
					// String date = df.format(Calendar.getInstance().getTime());
					// File file = new File(Environment.getExternalStorageDirectory(), "G-" + date + ".txt");
					// Normal write file
					File file = new File(Environment.getExternalStorageDirectory(), filenameEdit.getText().toString() + ".txt");

					if (!spaam.writeFile(file))
						Toast.makeText(MainActivity.this, "Write file failed", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(MainActivity.this, "File written", Toast.LENGTH_SHORT).show();
				}
			}
		});

		recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !recording ) {
					try {
						DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
						String date = df.format(Calendar.getInstance().getTime());
						recordFilename = "NO_" + date + ".txt";
						recordFile = new File(Environment.getExternalStorageDirectory(), recordFilename);
						if (!recordFile.exists())
							recordFile.createNewFile();
						recordStream = new FileOutputStream(recordFile);
						recording = true;
						recordButton.setText("Stop");
						Log.i(TAG, "recording started");
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
				else {
					try {
						recordStream.close();
						recording = false;
						recordButton.setText("Record");
						Log.i(TAG, "recording ended");
					}
					catch (Exception e){
						e.printStackTrace();
					}
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
				if (!visualTracker.visibility) {
					Toast.makeText(MainActivity.this, "Marker is not visible", Toast.LENGTH_SHORT).show();
					return true;
				} else {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if (spaam.status == SPAAMStatus.CALIB_RAW) {
								recordClick(x, y);
								spaam.newAlignment(x, y, T);
							} else if (spaam.status == SPAAMStatus.CALIB_ADD || spaam.status == SPAAMStatus.DONE_ADD)
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

					if ( z < 0 )
						cursorView.setXYZ(x, y, z);
					else if (z == 1) {
						// video is clicked
						if ( x <= 640 && y <= 480 ) {
							T = visualTracker.getMarkerTransformation();
							transformationChanged();
							if ( !visualTracker.visibility ) {
								Toast.makeText(MainActivity.this, "Marker is not visible", Toast.LENGTH_SHORT).show();
							}
							else {
								if ( spaam.status == SPAAMStatus.CALIB_RAW ) {
									recordClick(x, y);
									spaam.newAlignment(x, y, T);
								}
								else if ( spaam.status == SPAAMStatus.CALIB_ADD || spaam.status == SPAAMStatus.DONE_ADD )
									spaam.newTuple(x, y, T);
							}
						}
						// cancel button is clicked
						else if ( 640 < x && x < 800 && 0 <= y && y < 80 ) {
							cancelButton.performClick();
						}
					}
					else if (z == 2) {
						cancelButton.performClick();
					}
					cursorView.invalidate();
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
					String ss = "*1 " + T.get(0,0) + " " + T.get(0,1) + " " + T.get(0,2) + " " + T.get(0,3) +  System.getProperty("line.separator");
					recordStream.write(ss.getBytes());
					ss = "*2 " + T.get(1,0) + " " + T.get(1,1) + " " + T.get(1,2) + " " + T.get(1,3) +  System.getProperty("line.separator");
					recordStream.write(ss.getBytes());
					ss = "*3 " + T.get(2,0) + " " + T.get(2,1) + " " + T.get(2,2) + " " + T.get(2,3) +  System.getProperty("line.separator");
					recordStream.write(ss.getBytes());


//					Matrix tMatrix = renderer.getmProj().times(T.times(singlePoint));
//					tMatrix = tMatrix.times(1.0 / tMatrix.get(3, 0));
//					double trackX = 640.0 * (tMatrix.get(0,0) + 1.0) / 2.0;
//					double trackY = 480.0 * (-tMatrix.get(1,0) + 1.0) / 2.0;
//					String ss = "* " + trackX + " " + trackY + System.getProperty("line.separator");
//					if ( intView != null ) {
//						intView.updateTrackXY(trackX, trackY);
//					}
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

	public void recordClick(float x, float y){
		if (recording) {
			try {
				String ss = "$ " + x + " " + y + System.getProperty("line.separator");
				recordStream.write(ss.getBytes());
			} catch( Exception e){
				e.printStackTrace();
			}
		}
	}

	public void recordCancel() {
		if (recording) {
			try {
				String ss = "- " + System.getProperty("line.separator");
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
