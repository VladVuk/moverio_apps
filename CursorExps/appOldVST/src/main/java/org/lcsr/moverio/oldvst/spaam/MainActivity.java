/*
 *  MainActivity.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.oldvst.spaam;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.artoolkit.ar.base.*;
import org.artoolkit.ar.base.camera.CaptureCameraPreview;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.lcsr.moverio.oldvst.spaam.util.*;
import org.lcsr.moverio.oldvst.spaam.util.SPAAM.SPAAMStatus;

import Jama.Matrix;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ARActivity {

	private static String TAG = "MainActivity";

//	private MainRenderer renderer;

	private SimpleRenderer renderer;
	private FrameLayout mainLayout;
	
	private Button cancelButton;
	private Button modifyButton;

	// Recording stuff
	private Button recordButton;
	private boolean recording = false;
	private File recordFile;
	private FileOutputStream recordStream;

	private Button readFileButton, writeFileButton;
	private EditText filenameEdit;

	private SPAAM spaam;
	
	private InteractiveView intView;
	
	private VisualTracker visualTracker;

	private Matrix T;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		visualTracker = new VisualTracker();
        spaam = new SPAAM( 0.0, 0.0, 0.0 );
        spaam.setMaxAlignment(20);
//		renderer = new MainRenderer(visualTracker);
		renderer = new SimpleRenderer(visualTracker);

        setContentView(R.layout.main);
		mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);

		filenameEdit = (EditText) this.findViewById(R.id.editText);


        
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
				if ( spaam.status == SPAAMStatus.CALIB_RAW )
					Toast.makeText(MainActivity.this, "SPAAM not done", Toast.LENGTH_SHORT).show();
				else {
					 File file = new File(Environment.getExternalStorageDirectory(), filenameEdit.getText().toString() + ".txt");

					if ( !spaam.writeFile(file))
						Toast.makeText(MainActivity.this, "Write file failed", Toast.LENGTH_SHORT).show();
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
				if (!visualTracker.visibility) {
					Toast.makeText(MainActivity.this, "Marker is invisible", Toast.LENGTH_SHORT).show();
					return true;
				} else {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if (spaam.status == SPAAMStatus.CALIB_RAW) {
								recordClick(x, y);
								spaam.newAlignment(x, y, T);
							}
							else if (spaam.status == SPAAMStatus.CALIB_ADD || spaam.status == SPAAMStatus.DONE_ADD)
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


		recordButton = (Button) findViewById(R.id.recordButton);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( !recording ) {
					try {
						recordFile = new File(Environment.getExternalStorageDirectory(), filenameEdit.getText().toString() + "_OV.txt");
						if (!recordFile.exists())
							recordFile.createNewFile();
						recordStream = new FileOutputStream(recordFile);
						recording = true;
						recordButton.setText("Stop");
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
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
    }
	
	
    
    @Override
    public void onFrameProcessed() {
		T = visualTracker.getMarkerTransformation();
		transformationChanged();
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
							" " + T.get(2,0) + " " + T.get(2,1) + " " + T.get(2,2) + " " + T.get(2,3) + System.getProperty("line.separator");
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
				String ss = "% " + System.getProperty("line.separator");
				recordStream.write(ss.getBytes());
			} catch( Exception e){
				e.printStackTrace();
			}
		}
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
		CaptureCameraPreview.zoomValue = 10;
        intView = new InteractiveView(this, spaam);
		intView.setGeometry(640, 480);
    	mainLayout.addView(intView);
        Log.i(TAG, "InteractiveView added");
    }

	@Override
	public void onPause(){
		if (recording){
			recordButton.performClick();
		}
		super.onPause();
	}
    
    @Override
    public void onStop() {
    	super.onStop();
    	mainLayout.removeAllViews();
    	spaam.clearSPAAM();
    	spaam = null;
    }


}
