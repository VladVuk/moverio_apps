package org.medcare.igtl.util;

import java.util.ArrayList;
import java.util.List;

public class PAttribute {
	public short type; // uint16
	public char[] name; // 
	public char sep = '\u0000';
	public long length; // uint32
	public byte component; // uint8
	public List<PCell<Double>> attr; //float32

	public final static short POINT_SCALARS = 0x0000;
	public final static short POINT_VECTOR = 0x0001;
	public final static short POINT_NORMALS = 0x0002;
	public final static short POINT_TENSORS = 0x0003;
	public final static short POINT_RGBA = 0x0004;
	public final static short CELL_SCALARS = 0x0010;
	public final static short CELL_VECTOR = 0x0011;
	public final static short CELL_NORMALS = 0x0012;
	public final static short CELL_TENSORS = 0x0013;
	public final static short CELL_RGBA = 0x0014;
	
	
	public PAttribute() {
		;
	}
	
	public void setType( short t ) {
		type = (short) (t & 0x00FF);
		component = (byte) ((t & 0xFF00) >> 8);
	}
	
	public void setType ( long l ) {
		short t = (short)l;
		setType(t);
	}
	
	public void setLength( long l ) {
		length = l;
	}
	
	public void setName( char[] n ) {
		name = new char[n.length];
		System.arraycopy(n, 0, name, 0, n.length);
	}
	
	public void setData(byte[] b) {
		BytesArray ba = new BytesArray();
		ba.putBytes(b);
		attr = new ArrayList<PCell<Double>>();
		for ( int i = 0; i < length; i++) {
			Double[] d = new Double[component];
			for ( int j = 0; j < component; j++ ) {
				d[j] = ba.getDouble(4);
			}
			attr.add( new PCell<Double>((int)component, d) );
		}
	}
	
	public String toString() {
		String output = "";
		switch (type) {
		case POINT_SCALARS:
			output += "POINT_SCALARS";
			break;
		case POINT_VECTOR:
			output += "POINT_VECTOR";
			break;
		case POINT_NORMALS:
			output += "POINT_NORMALS";
			break;
		case POINT_TENSORS:
			output += "POINT_TENSORS";
			break;
		case POINT_RGBA:
			output += "POINT_RGBA";
			break;
		case CELL_SCALARS:
			output += "CELL_SCALARS";
			break;
		case CELL_VECTOR:
			output += "CELL_VECTOR";
			break;
		case CELL_NORMALS:
			output += "CELL_NORMALS";
			break;
		case CELL_TENSORS:
			output += "CELL_TENSORS";
			break;
		case CELL_RGBA:
			output += "CELL_RGBA";
			break;				
		}		
		output += " " + name.toString() + " " + component + " " + length + System.getProperty("line.separator");
		for ( PCell<Double> pc : attr ) {
			output += pc.toString(false);
		}
		return output;
	}
	
}
