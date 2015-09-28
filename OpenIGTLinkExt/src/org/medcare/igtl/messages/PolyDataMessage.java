/**
 *** This class create an PolyData object from bytes received or help to generate
 * bytes to send from it
 * 
 * @author Long Qian
 * 
 */

package org.medcare.igtl.messages;

import org.medcare.igtl.util.Header;
import org.medcare.igtl.util.PolyData;

import com.neuronrobotics.sdk.common.Log;

public class PolyDataMessage extends OpenIGTMessage {
	
	private PolyData polydata;
		
	
	public PolyDataMessage(String deviceName) {
        super(deviceName);
	}

	public PolyDataMessage(Header header, byte[] body) throws Exception {
		super(header, body);
	}
	
	public PolyDataMessage(String deviceName, PolyData pd) {
		super(deviceName);
		polydata = pd;
	}

	@Override
	public boolean UnpackBody() throws Exception {
		polydata = new PolyData(getBody());
		Log.debug("Body size: "+getBody().length+" date size: "+polydata.length);
		polydata.parseFromData();
		return true;
	}

	@Override
	public byte[] PackBody() {
		setBody(new byte[polydata.length]);
        System.arraycopy(polydata.data, 0, getBody(), 0, polydata.length);
        setHeader(new Header(VERSION, "POLYDATA", deviceName, getBody()));
		return getBytes();
	}

	@Override
	public String toString() {
		String polyDataString = "POLYDATA Device Name           : " + getDeviceName();
		polyDataString += polydata.toString();
		return polyDataString;
	}

	public PolyData getPolyData() {
		return polydata;
	}

}
