/*
 *  mSensorEventListener.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.sensor;

import android.widget.TextView;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class Sensors implements SensorEventListener {

	private TextView accX, accY, accZ;
	private TextView gyroX, gyroY, gyroZ;
	private SensorManager accSensorManager, gyroSensorManager;
	private Sensor accSensor, gyroSensor;
	
	public Sensors(MainActivity Act)
	{
		accX = (TextView) Act.findViewById( R.id.dataIMUX );
		accY = (TextView) Act.findViewById( R.id.dataIMUY );
		accZ = (TextView) Act.findViewById( R.id.dataIMUZ );

		gyroX = (TextView) Act.findViewById( R.id.dataGyroX );
		gyroY = (TextView) Act.findViewById( R.id.dataGyroY );
		gyroZ = (TextView) Act.findViewById( R.id.dataGyroZ );

		accSensorManager = (SensorManager) Act.getSystemService( Context.SENSOR_SERVICE );
		accSensor = accSensorManager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );

		gyroSensorManager = (SensorManager) Act.getSystemService( Context.SENSOR_SERVICE );
		gyroSensor = gyroSensorManager.getDefaultSensor( Sensor.TYPE_GYROSCOPE );
	
		this.register();
	}
	

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor ssr = event.sensor;
		int type = ssr.getType();
		switch( type )
		{
		case Sensor.TYPE_ACCELEROMETER:
			accX.setText( String.format("%.4f", event.values[0] ));
			accY.setText( String.format("%.4f", event.values[1] ));
			accZ.setText( String.format("%.4f", event.values[2] ));
			break;
		case Sensor.TYPE_GYROSCOPE:
			gyroX.setText( String.format("%.4f", event.values[0] ));
			gyroY.setText( String.format("%.4f", event.values[1] ));
			gyroZ.setText( String.format("%.4f", event.values[2] ));
			break;
		}		
	}
	
	public void unregister()
	{
		accSensorManager.unregisterListener(this);
		gyroSensorManager.unregisterListener(this);
	}
	
	public void register()
	{
		accSensorManager.registerListener( this, accSensor, SensorManager.SENSOR_DELAY_NORMAL );
		gyroSensorManager.registerListener( this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL );
	}
}
