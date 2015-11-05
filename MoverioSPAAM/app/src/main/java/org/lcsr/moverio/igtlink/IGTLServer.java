/*
 *  ITGLinkServer.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.igtlink;


import org.medcare.igtl.messages.ImageMessage;
import org.medcare.igtl.network.GenericIGTLinkServer;
import org.medcare.igtl.network.IOpenIgtPacketListener;
import org.medcare.igtl.util.PolyData;
import org.medcare.igtl.util.Status;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import Jama.Matrix;
import android.os.Handler;
import android.util.Log;

public class IGTLServer implements IOpenIgtPacketListener {
	
	private static String TAG = "IGTLServer";
	private GenericIGTLinkServer server;
	private int port = 18944;
	private int sendCount = 0;
	private int receiveCount = 0;
	private Handler igtlMsgHandler;
	
	public static enum IGTLMsgType {TRANS, NONE};
	
	public IGTLServer (Handler mh ) {
		try {
			server = new GenericIGTLinkServer (port);
			server.addIOpenIgtOnPacket(this);
			igtlMsgHandler = mh;
		}
		catch (Exception e) {
			Log.e(TAG, "Constructor", e);
		}
	}
		
	public void sendTransform(Matrix t) {
//		if ( !server.isConnected() ) {
//			Log.i(TAG, "Server not connected");
//			return;
//		}
		try {
			TransformNR NRt = new TransformNR(t);
			server.pushTransformMessage("From Android, count = " + sendCount, NRt);
			Log.i(TAG, "Push transformation done, count = " + sendCount);
			sendCount = sendCount + 1;
		}
		catch (Exception e) {
			Log.e(TAG, "Push transformation error", e);
		}
	}
	
	public void stop() {
		server.setKeepAlive(false);
		server.removeIOpenIgtOnPacket(this);
		server.stopServer();
	}
	

	@Override
	public void onRxTransform(String name, TransformNR t) {
		receiveCount += 1;
		Log.i(TAG, "Received Transform with name: " + name + " and receive count:" + receiveCount);
		igtlMsgHandler.obtainMessage(1, t).sendToTarget();
	}

	@Override
	public TransformNR getTxTransform(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status onGetStatus(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onRxString(String name, String body) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Received string with name: " + name + " and body:" +body);  
		
	}

	@Override
	public String onTxString(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onRxDataArray(String name, Matrix data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] onTxDataArray(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onRxImage(String name, ImageMessage image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTxNDArray(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRxNDArray(String name, float[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRxPolyData(String arg0, PolyData arg1) {
		// TODO Auto-generated method stub
		
	}

	
	
	
}
