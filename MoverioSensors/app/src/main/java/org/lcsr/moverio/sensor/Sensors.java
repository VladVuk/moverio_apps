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
	private SensorManager sensorManager;
	private Sensor accSensor, magSensor, rotSensor;
	
	private float[] mGravity;
	private float[] mMagnetic;
	
	public Sensors(MainActivity Act)
	{
		accX = (TextView) Act.findViewById( R.id.dataIMUX );
		accY = (TextView) Act.findViewById( R.id.dataIMUY );
		accZ = (TextView) Act.findViewById( R.id.dataIMUZ );

		gyroX = (TextView) Act.findViewById( R.id.dataGyroX );
		gyroY = (TextView) Act.findViewById( R.id.dataGyroY );
		gyroZ = (TextView) Act.findViewById( R.id.dataGyroZ );

		sensorManager = (SensorManager) Act.getSystemService( Context.SENSOR_SERVICE );
		accSensor = sensorManager.getDefaultSensor( Sensor.TYPE_GRAVITY );
		magSensor = sensorManager.getDefaultSensor( Sensor.TYPE_MAGNETIC_FIELD );
		rotSensor = sensorManager.getDefaultSensor( Sensor.TYPE_ROTATION_VECTOR );
	
		this.register();
	}
	
	private float getDirection() 
    {       
        float[] temp = new float[9];
        float[] R = new float[9];
        //Load rotation matrix into R
        SensorManager.getRotationMatrix(temp, null, mGravity, mMagnetic);
       
        //Remap to camera's point-of-view
        SensorManager.remapCoordinateSystem(temp,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z, R);
        
       
        //Return the orientation values
        float[] values = new float[3];
        SensorManager.getOrientation(R, values);
       
        //Convert to degrees
        for (int i=0; i < values.length; i++) {
            Double degrees = (values[i] * 180) / Math.PI;
            values[i] = degrees.floatValue();
        }
        
        gyroX.setText( String.format("%.4f", values[0] ));
		gyroY.setText( String.format("%.4f", values[1] ));
		gyroZ.setText( String.format("%.4f", values[2] ));

        return values[0];
       
    }
	

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()) {
        
        case Sensor.TYPE_GRAVITY:
            mGravity = event.values.clone();
            break;
        case Sensor.TYPE_MAGNETIC_FIELD:
            mMagnetic = event.values.clone();
            break;
        case Sensor.TYPE_ROTATION_VECTOR:
        	float[] rotationMatrix = new float[9];
        	float[] adjustedRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z, adjustedRotationMatrix);
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);
			accX.setText( String.format("%.4f", orientation[0] * 180 / Math.PI ));
			accY.setText( String.format("%.4f", orientation[1] * 180 / Math.PI ));
			accZ.setText( String.format("%.4f", orientation[2] * 180 / Math.PI ));
			break;        	
        default:
            return;
        }
		if(mGravity != null && mMagnetic != null) {
            getDirection();
        }
//		Sensor ssr = event.sensor;
//		int type = ssr.getType();
//		switch( type )
//		{
//		case Sensor.TYPE_ACCELEROMETER:
//			accX.setText( String.format("%.4f", event.values[0] ));
//			accY.setText( String.format("%.4f", event.values[1] ));
//			accZ.setText( String.format("%.4f", event.values[2] ));
//			break;
//		case Sensor.TYPE_GYROSCOPE:
//			gyroX.setText( String.format("%.4f", event.values[0] ));
//			gyroY.setText( String.format("%.4f", event.values[1] ));
//			gyroZ.setText( String.format("%.4f", event.values[2] ));
//			break;
//		}		
	}
	
	public void unregister()
	{
		sensorManager.unregisterListener(this);
	}
	
	public void register()
	{
		sensorManager.registerListener( this, accSensor, SensorManager.SENSOR_DELAY_GAME );
		sensorManager.registerListener( this, magSensor, SensorManager.SENSOR_DELAY_GAME );
		sensorManager.registerListener( this, rotSensor, SensorManager.SENSOR_DELAY_GAME );
	}
}
