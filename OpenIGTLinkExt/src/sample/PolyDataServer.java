package sample;

import com.neuronrobotics.sdk.common.Log;
import org.medcare.igtl.messages.ImageMessage;
import org.medcare.igtl.network.GenericIGTLinkServer;
import org.medcare.igtl.network.IOpenIgtPacketListener;
import org.medcare.igtl.util.PolyData;
import org.medcare.igtl.util.Status;

import Jama.Matrix;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;

public class PolyDataServer implements IOpenIgtPacketListener {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		GenericIGTLinkServer server;
		Log.enableDebugPrint(true);
		Log.enableSystemPrint(true);

		try {
			//Set up server
			server = new GenericIGTLinkServer (18944);

			//Add local event listener
			server.addIOpenIgtOnPacket(new ServerSample());
			
			while(!server.isConnected()){
				Thread.sleep(100);
			}
			
			while(true){
				Thread.sleep(1000);
				if(server.isConnected()){
					Log.debug("Wait");
				}else{
					Log.debug("Not connected");
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public PolyDataServer(){
		;
	}
	@Override
	public void onRxTransform(String name, TransformNR t) {
		Log.debug("Received Transform with name: " + name + "and transform:" +t);
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
		//check if its XML format message
		Log.debug("Received string with name: " + name + "and string:" +body);
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
		Log.debug("Name" + name);
		for(int i=0;i<data.length;i++){
			Log.debug("Data[" + i + "]=" + (double)data[i]);
		}
	}
	@Override
	public void onRxPolyData(String name, PolyData pd) {
		// TODO Auto-generated method stub
		Log.debug("Received PolyData, name: " + name);
		System.out.println(pd.toString());
	}

}
