package org.medcare.igtl.util;

public class PCell<T> {
	public long count; //uint32
	public T[] data; //uint32
	
	@SuppressWarnings("unchecked")
	public PCell(int c) {
		count = c;
		if ( c >= 0 ) {
			data = (T[])new Object[c];
		}
	}
	public PCell() {
		count = 0;
	}	
	public PCell(int c, T[] d) {
		count = c >= 0 ? c : 0;
		if ( c == d.length ) {
			data = d.clone();
		}
	}
	public void setCount( long c ) {
		count = c > 0 ? c : 0;
		data = null;
	}
	public void setData( T[] d ) {
		if ( d.length > 0 ) {
			count = d.length;
			data = d.clone();
		}
	}
	public String toString( boolean printCount ) {
		String output = "";
		if ( printCount )
			output += count + " ";
		for ( int i = 0; i < count-1; i++) {
			output += data[i] + " ";
		}
		output += data[(int) (count-1)];
		output += System.getProperty("line.separator");
		return output;
	}
}
