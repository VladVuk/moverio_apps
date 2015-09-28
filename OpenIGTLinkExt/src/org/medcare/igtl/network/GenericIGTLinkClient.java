package org.medcare.igtl.network;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.medcare.igtl.messages.ImageMessage;
import org.medcare.igtl.messages.NDArrayMessage;
import org.medcare.igtl.messages.OpenIGTMessage;
import org.medcare.igtl.messages.PolyDataMessage;
import org.medcare.igtl.messages.PositionMessage;
import org.medcare.igtl.messages.StatusMessage;
import org.medcare.igtl.messages.StringMessage;
import org.medcare.igtl.messages.TransformMessage;
import org.medcare.igtl.util.ErrorManager;
import org.medcare.igtl.util.Header;
import org.medcare.igtl.util.PolyData;
import org.medcare.igtl.util.Status;

import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

import Jama.Matrix;


public class GenericIGTLinkClient extends OpenIGTClient implements IOpenIgtPacketListener
{
	ArrayList<IOpenIgtPacketListener> listeners = new ArrayList<IOpenIgtPacketListener>();
	Sender s = new Sender();
	public GenericIGTLinkClient(String hostName, int port) throws Exception{
		super(hostName, port, new ErrorManager(){

			@Override
			public void error(String message, Exception exception, int errorCode) {
				System.err.println(message);
			}
		});
		Log.debug("GenericIGTLinkClient started");
		s.start();

	}

	@Override
	public ResponseHandler getResponseHandler(Header header, byte[] bodyBuf)
	{
		return new GenericClientResponseHandler(header,bodyBuf,this,this);
	}

	
	/**
	 * This method will be called by the IGT server when a transform is received. Supports:
	 * TRANSFORM
	 * QTRANS
	 * QTRANSFORM
	 * POSITION
	 * @param name The string in the 'NAME' field of the IGT packet
	 * @param t
	 */
	public void onRxTransform(String name,TransformNR t){
		for(IOpenIgtPacketListener l:listeners){
			l.onRxTransform(name, t);
		}
	}
	/**
	 * Request for a transform for transmition to IGT
	 * @param name A string of what type of transform to get
	 * @return the requested transform 
	 */
	public TransformNR getTxTransform(String name){
		if(listeners.size() != 1){
			throw new RuntimeException("There can be only one listener for this packet type.");
		}
		return listeners.get(0).getTxTransform(name);
	}
	/**
	 * Request for status from IGT/Slicer
	 * @param name A string of what type of transform to get
	 * @return the requested status
	 */
	public Status onGetStatus(String name){
		if(listeners.size() != 1){
			throw new RuntimeException("There can be only one listener for this packet type.");
		}
		return listeners.get(0).onGetStatus(name);
	}
	/**
	 * This is the handler for a String packet
	 * @param name A string of what type of data to get
	 * @param body A string of the content
	 */
	public void  onRxString(String name,String body){
		for(IOpenIgtPacketListener l:listeners){
			l.onRxString(name, body);
		}
	}


	/**
	 * This is the request handler for a String packet
	 * @param name A string of what type of transform to get
	 */
	public String onTxString(String name){
		if(listeners.size() != 1){
			throw new RuntimeException("There can be only one listener for this packet type.");
		}
		return listeners.get(0).onTxString(name);
	}

	/**
	 * This is the handler for an array of raw data in an array
	 * @param name A string of what type of data to get
	 * @param data An array of data
	 */
	public void onRxDataArray(String name, Matrix data){
		for(IOpenIgtPacketListener l:listeners){
			l.onRxDataArray(name, data);
		}
	}

	/**
	 * THis is a request for an array of data
	 * @param name  A string of what type of data to get
	 * @return an array of data
	 */
	public double [] onTxDataArray(String name){
		if(listeners.size() != 1){
			throw new RuntimeException("There can be only one listener for this packet type.");
		}
		return listeners.get(0).onTxDataArray(name);
	}

	/**
	 * This is a handler for an Image sent from IGT packet
	 * @param name A string of what type of data to get
	 * @param image the image
	 * @param t A transform of where the image is
	 */
	public void onRxImage(String name,ImageMessage image){
		for(IOpenIgtPacketListener l:listeners){
			l.onRxImage(name, image);
		}
	}
	
	@Override
	public void onTxNDArray(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRxNDArray(String name, float[] data) {
		// TODO Auto-generated method stub
		for(IOpenIgtPacketListener l:listeners){
			l.onRxNDArray(name, data);
		}		
	}

	@Override
	public void onRxPolyData(String name, PolyData pd) {
		for (IOpenIgtPacketListener l:listeners) {
			l.onRxPolyData(name, pd);
		}
	}

	public void stopClient(){
		interrupt();
		while(isConnected());
		
		Log.debug("Stopped IGTLink client");
	}


	public void addIOpenIgtOnPacket(IOpenIgtPacketListener l){
		if(!listeners.contains(l))
			listeners.add(l);
	}
	public void removeIOpenIgtOnPacket(IOpenIgtPacketListener l){
		if(listeners.contains(l))
			listeners.remove(l);
	}
	
	public void pushPose(String deviceName, TransformNR pose){
		PositionMessage  poseMsg = new PositionMessage (deviceName,pose.getPositionArray(), pose.getRotationMatrix());
		s.onTaskSpaceUpdate(poseMsg);
	}
	public void pushStatus(String deviceName, int code, int subCode, String status){
		StatusMessage statMsg = new StatusMessage(deviceName, code, subCode, status);
		s.onStatus(statMsg);
	}
	public void pushStatus(String deviceName, int code, int subCode, String errorName, String status){
		StatusMessage statMsg = new StatusMessage(deviceName, code, subCode, errorName, status);
		s.onStatus(statMsg);
	}
	public void pushStringMessage(String deviceName, String msg){
		StringMessage strMsg = new StringMessage(deviceName , msg);
		s.onStringMessage(strMsg);
	}
	public void pushTransformMessage( String deviceName, TransformNR t){
		TransformMessage transMsg = new TransformMessage(deviceName, t.getPositionArray(), t.getRotationMatrixArray());
		transMsg.PackBody();
		s.onTransformMessage(transMsg);
	}
	public void pushNDArrayMessage( String deviceName, float[] data){
		NDArrayMessage ndArrayMsg = new NDArrayMessage(deviceName, data);				
		s.onNDArrayMessage(ndArrayMsg);
	}
	public void pushPolyDataMessage( String devicename, PolyData pd){
		PolyDataMessage pdmsg = new PolyDataMessage(devicename, pd);
		s.onPolyDataMessage(pdmsg);
	}
	
	public class Sender extends Thread{
		private Queue<OpenIGTMessage> messageQueue = new LinkedList<OpenIGTMessage>();
		TransformMessage curPos = null;
		
		public synchronized void onTaskSpaceUpdate( PositionMessage msg){
			messageQueue.add(msg);
		}
		public void onStatus(StatusMessage msg) {
			//by pass Queue if its STOP or EMERGENCY message
			//if( msg.deviceName == "STOP" ||  msg.deviceName == "EMERGENCY"){
			//	sendMessage(msg);
			//}else{
				messageQueue.add(msg);
			//}
		}
		public void onStringMessage(StringMessage msg){
			//by pass Queue if its STOP or EMERGENCY message
			//if( msg.getMessage() == "STOP" || msg.getMessage() == "EMERGENCY"){
			//	sendMessage(msg);
			//}else{
				messageQueue.add(msg);
			//}
		}
		public void onTransformMessage(TransformMessage msg){
			if( msg.deviceName == "CURRENT_POSITION"){
				curPos = msg;
			}
			else{
				messageQueue.add(msg);
			}
		}
		public void onNDArrayMessage(NDArrayMessage msg){
				messageQueue.add(msg);
		}
		
		public void onPolyDataMessage(PolyDataMessage msg){
			messageQueue.add(msg);
		}
		
		public void run(){
			while(isConnected()){
				/*try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
				//take out a message from Queue and send it
				try {
					if( messageQueue.size() > 0){
						Log.debug("Number of messages in Queue = " + messageQueue.size());
					}
					OpenIGTMessage msg = messageQueue.poll();
					if(msg!=null ){
						sendMessage(msg);
					}
					else if( curPos !=null ){
						sendMessage(curPos);
						curPos = null;
					}
				} catch (Exception e) {
					if( !messageQueue.isEmpty() ){
						messageQueue.clear(); //clear message queue if ws not able to send as that is a connection problem
						Log.debug("Clearing Message Sender Queue ; Number of messages in Queue = " + messageQueue.size());
					}
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.debug("Stopped IGTLink client Sender");

		}
	}
}



