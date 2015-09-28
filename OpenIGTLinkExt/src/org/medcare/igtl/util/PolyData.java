package org.medcare.igtl.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PolyData {

	private long nPoint; //uint32
	private long nVertex; //uint32
	private long sizeVertex; //uint32
	private long nLine; //uint32
	private long sizeLine; //uint32
	private long nPolygon; //uint32
	private long sizePolygon; //uint32
	private long nTriangle; //uint32
	private long sizeTriangle; //uint32
	private long nAttribute; //uint32
	private double points[][]; //float32
	private List<PCell<Long>> vertices;
	private List<PCell<Long>> lines;
	private List<PCell<Long>> polygons;
	private List<PCell<Long>> triangles;
	private List<PAttribute> attributes;
	
	public byte[] data;
	public int length = 0;
	
	public boolean unpacked = false;
	public boolean packed = false;
	
	public PolyData() {
		;
	}
	
	public PolyData(String filename) {
		parseFromFile(filename);
	}
	
	public PolyData(byte[] body) {
		System.arraycopy(body, 0, data, 0, body.length);
		length = data.length;
		packed = true;
	}
	
	public boolean parseFromFile(String filename) {
		try {
			Path path = Paths.get(filename);
			data = Files.readAllBytes(path);
			length = data.length;
			packed = true;
			return parseFromData();
		}
		catch (IOException e) {
			return false;
		}
	}
	
	public boolean parseFromMessage(byte[] indata) {
		System.arraycopy(indata, 0, data, 0, indata.length);
		length = data.length;
		packed = true;
		return parseFromData();
	}
		
	public boolean parseFromData() {
		BytesArray a = new BytesArray();
		a.putBytes(data);
		nPoint = a.getULong(4);
		nVertex = a.getULong(4);
		sizeVertex = a.getULong(4);
		nLine = a.getULong(4);
		sizeLine = a.getULong(4);
		nPolygon = a.getULong(4);
		sizePolygon = a.getULong(4);
		nTriangle = a.getULong(4);
		sizeTriangle = a.getULong(4);
		nAttribute = a.getULong(4);
		
		points = new double[(int) nPoint][3];
		for ( int i = 0; i < nPoint; i++ ) {
			for ( int j = 0; j < 3; j++ ) {
				points[i][j] = a.getDouble(4);
			}
		}

		int countCheck = 0;
		if ( nVertex != 0) {
			vertices = new ArrayList<PCell<Long>>();
			for ( int i = 0; i < nVertex; i++ ) {
				int c = (int)a.getULong(4);
				countCheck += c;
				Long[] d = new Long[c];
				for ( int j = 0; j < c; j++ ) {
					d[j] = a.getULong(4);
				}
				PCell<Long> pc = new PCell<Long>(c, d);
				vertices.add(pc);
			}
			if ( countCheck != sizeVertex ) {
				System.err.println("Vertex count check failed");
				return false;
			}
		}
		
		if ( nLine != 0 ) {
			countCheck = 0;
			lines = new ArrayList<PCell<Long>>();
			for ( int i = 0; i < nLine; i++ ) {
				int c = (int)a.getULong(4);
				countCheck += c;
				Long[] d = new Long[c];
				for ( int j = 0; j < c; j++ ) {
					d[j] = a.getULong(4);
				}
				PCell<Long> pc = new PCell<Long>(c, d);
				lines.add(pc);
			}
			if ( countCheck != sizeLine ) {
				System.err.println("Line count check failed");
				return false;
			}
		}
		
		if ( nPolygon != 0 ) {
			countCheck = 0;
			polygons = new ArrayList<PCell<Long>>();
			for ( int i = 0; i < nPolygon; i++ ) {
				int c = (int)a.getULong(4);
				countCheck += c;
				Long[] d = new Long[c];
				for ( int j = 0; j < c; j++ ) {
					d[j] = a.getULong(4);
				}
				PCell<Long> pc = new PCell<Long>(c, d);
				polygons.add(pc);
			}
			if ( countCheck != sizePolygon ) {
				System.err.println("Polygon count check failed");
				return false;
			}
		}
		
		if ( nTriangle != 0 ) {
			countCheck = 0;
			triangles = new ArrayList<PCell<Long>>();
			for ( int i = 0; i < nTriangle; i++ ) {
				int c = (int)a.getULong(4);
				countCheck += c;
				Long[] d = new Long[c];
				for ( int j = 0; j < c; j++ ) {
					d[j] = a.getULong(4);
				}
				PCell<Long> pc = new PCell<Long>(c, d);
				triangles.add(pc);
			}
			if ( countCheck != sizeTriangle ) {
				System.err.println("Triangle count check failed");
				return false;
			}
		}
		
		if ( nAttribute != 0 ) {
			attributes = new ArrayList<PAttribute>();
			for ( int i = 0; i < nAttribute; i++ ) {
				PAttribute pa = new PAttribute();
				pa.setType( a.getULong(2) );
				pa.setLength( a.getULong(4) );
				attributes.add(pa);
			}
			
			int charCount = 0;
			for ( PAttribute pa : attributes ) {
				char[] n = getCharList(a);
				pa.setName( n );
				charCount += (n.length + 1);
			}
			if ( charCount % 2 == 1 ) {
				char c = (char) a.getULong(2);
				if ( c != 0 ) {
					System.err.println("Attribute name padding error");
					return false;
				}
			}
			
			for ( PAttribute pa : attributes ) {
				pa.setData(a.getBytes( (int)(pa.component * pa.length)));
			}
		}
				
		
		unpacked = true;
		return unpacked;
	}
	
	public boolean packData() {
		return packed;
	}
	
	public String toString() {
		String output = "DATASET POLYDATA";
		output += System.getProperty("line.separator");
		output += "POINTS " + nPoint + " float" + System.getProperty("line.separator");
		for ( int i = 0; i < nPoint; i++ ) {
			for ( int j = 0; j < 2; j++ ) {
				output += points[i][j] + " ";
			}
			output += points[i][2];
			output += System.getProperty("line.separator");
		}
		if ( vertices != null ) {
			output += "VERTICES " + nVertex + " " + sizeVertex + System.getProperty("line.separator");
			output += toString(vertices);
		}
		if ( lines != null ) {
			output += "LINES " + nLine + " " + sizeLine + System.getProperty("line.separator");
			output += toString(lines);
		}
		if ( polygons != null ) {
			output += "POLYGONS " + nPolygon + " " + sizePolygon + System.getProperty("line.separator");
			output += toString(polygons);
		}
		if ( triangles != null ) {
			output += "TRIANGLES " + nTriangle + " " + sizeTriangle + System.getProperty("line.separator");
			output += toString(triangles);
		}
		if ( attributes != null ) {
			for ( PAttribute pa : attributes) {
				output += pa.toString();
			}
		}
		return output;
	}
		
	public String toString(List<PCell<Long>> lpc) {
		String output = "";
		for ( PCell<Long> pc : lpc ) {
			output += pc.toString(true);
		}
		return output;
	}
	
	public static char[] getCharList(BytesArray a) {
		List<Character> b = new ArrayList<Character>();
		boolean stop = false;
		while ( !stop ) {
			char c = (char) a.getULong(2);
			if ( c == 0 ) {
				stop = true;
			}
			else
				b.add(c);
		}
		char[] ch = new char[b.size()];
		for ( int i = 0; i < b.size(); i++ ) {
			ch[i] = b.get(i);
		}
		return ch;
	}
	
	
}
