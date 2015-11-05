package org.lcsr.moverio.sensor;

import org.lcsr.moverio.spaam.MainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccCompensate implements SensorEventListener {

	private SensorManager accSensorManager, oriSensorManager;
	private Sensor accSensor, oriSensor;
	
	@SuppressLint("InlinedApi")
	public AccCompensate(MainActivity Act) {
		accSensorManager = (SensorManager) Act.getSystemService( Context.SENSOR_SERVICE );
		accSensor = accSensorManager.getDefaultSensor( Sensor.TYPE_LINEAR_ACCELERATION );

		oriSensorManager = (SensorManager) Act.getSystemService( Context.SENSOR_SERVICE );
		oriSensor = oriSensorManager.getDefaultSensor( Sensor.TYPE_ORIENTATION );
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
	}

}
