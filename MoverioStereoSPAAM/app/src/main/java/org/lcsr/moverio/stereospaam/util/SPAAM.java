/*
 *  SPAAM.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.stereospaam.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import Jama.*;

public class SPAAM {

	public enum SPAAMStatus {CALIB_RAW, DONE_RAW};
	private String TAG = "SPAAM";
	private Matrix singlePoint;

	// need synchronization
	private SPAAMStatus status;
	private int alignMax = 10;
	private int alignCount = 0;

	private Matrix G = null;
    private float[] GLProjection;


	private List<Alignment> alignments;

	
	public SPAAM( String spec ) {
		setSinglePointLocation(0.0, 0.0, 0.0);
		alignments = new ArrayList<Alignment>();
		status = SPAAMStatus.CALIB_RAW;
		TAG = TAG + spec;
		Log.i(TAG, "constructed");
	}

	public void setSinglePointLocation(Matrix M) {
		if ( M != null ) {
			singlePoint = M;
			Log.i(TAG, "Single point set successfully");
		}
		else
			Log.e(TAG, "Single point set failed");
	}
	
	public void setSinglePointLocation(double X, double Y, double Z) {
        double[] singlePointArray = {X, Y, Z, 1.0};
        singlePoint = new Matrix( singlePointArray, 4);
		Log.i(TAG, "Single point set successfully");
	}	

	public boolean cancelLast() {
		switch(status){
			case CALIB_RAW:
				if (alignCount == 0){
					Log.e(TAG, "No alignment yet");
					return false;
				}
				else{
					alignments.remove(alignments.size()-1);
					alignCount = alignments.size();
					Log.i(TAG, "Last alignments canceled");
					return true;
				}
			case DONE_RAW:
				status = SPAAMStatus.CALIB_RAW;
				alignments.remove(alignments.size()-1);
				alignCount = alignments.size();
				G = null;
				Log.i(TAG, "Last alignments canceled, SPAAM result cleared");
				return true;
		}
		return false;
	}
	
	public Point getLastCursorPoint() {
		if (alignCount == 0)
			return null;
		else {
			Matrix temp =  alignments.get(alignCount-1).screenPoint;
			return new Point((int)temp.get(0,0), (int)temp.get(1,0));
		}
	}

	public void clearup() {
		alignments = null;
		alignCount = 0;
		G = null;
		status = SPAAMStatus.CALIB_RAW;
		Log.i(TAG, "SPAAM cleared");
	}
	
	public void newAlignment(Point P, Matrix M ){
		if ( status != SPAAMStatus.CALIB_RAW ) {
			Log.e(TAG, "Not in CALIB_RAW status");
		}
		else {
			if ( alignCount < alignMax ) {
				alignments.add(new Alignment(P, M.times(singlePoint)));
				alignCount = alignments.size();
				Log.i(TAG, "New alignment updated");
			}
			if ( alignCount == alignMax ) {
				Log.i(TAG, "Calculate G transformation");
				calculateG();
			}
		}
	}
	
	public void newAlignment(Matrix S, Matrix M ){
		if ( status != SPAAMStatus.CALIB_RAW ) {
			Log.e(TAG, "Not in CALIB_RAW status");
		}
		else {
			if ( alignCount < alignMax ) {
				alignments.add(new Alignment(S, M.times(singlePoint)));
				alignCount = alignments.size();
				Log.i(TAG, "New alignment updated");
			}
			if ( alignCount == alignMax ) {
				Log.i(TAG, "Calculate G transformation");
				calculateG();
			}
		}
	}

	public void calculateG() {
		// Preparing calculation of G

		Matrix transformScreenPoint;
		Matrix transformSpacePoint;
		Matrix transformScreenPointInv;
		Matrix transformSpacePointInv;

		double tempAvg;
		double tempNorm;

		// transformScreenPoint
		transformScreenPoint = new Matrix(3, 3, 0.0);
		tempAvg = Alignment.avg(alignments, 0, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignments, 0, Alignment.PointType.Screen);
		transformScreenPoint.set(0, 2, Alignment.avg(alignments, 0, Alignment.PointType.Screen));
		transformScreenPoint.set(0, 0, Math.sqrt(tempNorm - alignCount * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignments, 1, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignments, 1, Alignment.PointType.Screen);
		transformScreenPoint.set(1, 2, tempAvg);
		transformScreenPoint.set(1, 1, Math.sqrt(tempNorm - alignCount * tempAvg * tempAvg ));
		transformScreenPoint.set(2, 2, 1.0);

		// transformSpacePoint
		transformSpacePoint = new Matrix(4, 4, 0.0);
		tempAvg = Alignment.avg(alignments, 0, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignments, 0, Alignment.PointType.Space);
		transformSpacePoint.set(0, 3, tempAvg);
		transformSpacePoint.set(0, 0, Math.sqrt(tempNorm - alignCount * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignments, 1, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignments, 1, Alignment.PointType.Space);
		transformSpacePoint.set(1, 3, tempAvg);
		transformSpacePoint.set(1, 1, Math.sqrt(tempNorm - alignCount * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignments, 2, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignments, 2, Alignment.PointType.Space);
		transformSpacePoint.set(2, 3, tempAvg);
		transformSpacePoint.set(2, 2, Math.sqrt(tempNorm - alignCount * tempAvg * tempAvg ));
		transformSpacePoint.set(3, 3, 1.0);

		// calculate transformation inverse
		transformScreenPointInv = transformScreenPoint.inverse();
		transformSpacePointInv = transformSpacePoint.inverse();


		// calculate G_core
		List<Alignment> alignPointsTrans = new ArrayList<Alignment>();
		for ( int i = 0; i < alignCount; i++ ) {
			Alignment a = alignments.get(i);
			alignPointsTrans.add(new Alignment(transformScreenPointInv.times(a.screenPoint),
					transformSpacePointInv.times(a.spacePoint)));
		}
		
		Matrix B = new Matrix(2*alignCount, 12, 0.0);
		for ( int i = 0; i < alignCount; i++ ) {
			Alignment a = alignPointsTrans.get(i);

			double xi = a.spacePoint.get(0, 0);
			double yi = a.spacePoint.get(1, 0);
			double zi = a.spacePoint.get(2, 0);
			double px = a.screenPoint.get(0, 0);
			double py = a.screenPoint.get(1, 0);

			B.set( i*2, 0, xi );
			B.set( i*2, 1, yi );
			B.set( i*2, 2, zi );
			B.set( i*2, 3, 1.0 );
			B.set( i*2, 8, -px*xi );
			B.set( i*2, 9, -px*yi );
			B.set( i*2, 10, -px*zi );
			B.set( i*2, 11, -px );
			
			B.set( i*2+1, 4, xi );
			B.set( i*2+1, 5, yi );
			B.set( i*2+1, 6, zi );
			B.set( i*2+1, 7, 1.0 );
			B.set( i*2+1, 8, -py*xi );
			B.set( i*2+1, 9, -py*yi );
			B.set( i*2+1, 10, -py*zi );
			B.set( i*2+1, 11, -py );
		}
		
		SingularValueDecomposition svd = B.svd();
		Matrix eigenVecTranspose = svd.getV().getMatrix(0, 11, 11, 11).transpose();

		Matrix GCore = new Matrix(3, 4);
        GCore.setMatrix(0, 0, 0, 3, eigenVecTranspose.getMatrix(0, 0, 0, 3));
		GCore.setMatrix(1, 1, 0, 3, eigenVecTranspose.getMatrix(0, 0, 4, 7));
		GCore.setMatrix(2, 2, 0, 3, eigenVecTranspose.getMatrix(0, 0, 8, 11));

        G = new Matrix(3, 4);
        G = transformScreenPoint.times( GCore.times( transformSpacePointInv));

		Log.i(TAG, "Matrix G computed");

		status = SPAAMStatus.DONE_RAW;
	}

    public Matrix getCalibMat() {
        return G;
    }

	public Point calculateScreenPoint( Matrix T, Matrix spacePoint ) {
		if ( T == null )
			return null;
        Matrix CalibMat = getCalibMat();
        Matrix screenPoint = null;
        if ( CalibMat != null ) {
            screenPoint = CalibMat.times(T.times(spacePoint));
            Alignment.Normalize(screenPoint);
        }
		if ( screenPoint == null )
			return null;
		else
			return new Point((int)(screenPoint.get(0, 0)), (int)(screenPoint.get(1,0)));
	}

	public List<Alignment> getAlignments() {
		return alignments;
	}

	public void setAlignMax(int max) {
		alignMax = max;
	}

	public void setAlignCount(int count) {
		alignCount = count;
	}

	public int getAlignCount(){
		return alignCount;
	}

	public int getAlignMax(){
		return alignMax;
	}

	public SPAAMStatus getStatus(){
		return status;
	}

	public boolean readFile() {
		File file = new File(Environment.getExternalStorageDirectory(), TAG);
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    List<Double> values = new ArrayList<Double>();
		    while ((line = br.readLine()) != null) {
		    	String[] str_values = line.split(" ");
		    	for (String str : str_values)
	        	{
	        		double str_double = Double.parseDouble(str);
	        		values.add(str_double);
	        	}
		    }
		    br.close();
		    if (values.size() < 12) {
		    	Log.e(TAG, "File format wrong");
		    	return false;
		    }
		    double[] GRead = new double[12];
		    for (int i = 0; i < 12; i++) {
				GRead[i] = values.get(0);
		    	values.remove(0);
		    }
		    if (values.size() % 7 != 0 || values.size() / 7 != alignMax) {
		    	Log.e(TAG, "File format wrong");
		    	return false;
		    }
		    Matrix temp = new Matrix(GRead, 4);
		    G = temp.transpose();
		    alignments = new ArrayList<Alignment>();
		    int count = values.size() / 7;
		    for (int i = 0; i < count; i++) {
		    	Matrix sc = new Matrix(3, 1);
		    	Matrix sp = new Matrix(4, 1);
		    	sc.set(0, 0, values.get(7*i + 0));
		    	sc.set(1, 0, values.get(7*i + 1));
		    	sc.set(2, 0, values.get(7*i + 2));
		    	sp.set(0, 0, values.get(7*i + 3));
		    	sp.set(1, 0, values.get(7*i + 4));
		    	sp.set(2, 0, values.get(7*i + 5));
		    	sp.set(3, 0, values.get(7*i + 6));
				alignments.add(new Alignment(sc, sp));
		    }
			alignMax = count;
			alignCount = count;
		    status = SPAAMStatus.DONE_RAW;
		    Log.i(TAG, "File successfully parsed, with " + alignCount + " alignments");
		    return true;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		    return false;
		}
	}

	public boolean writeFile() {
		File file = new File(Environment.getExternalStorageDirectory(), TAG);
		if ( status == SPAAMStatus.CALIB_RAW ) {
			Log.i(TAG, "Not supported for CALIB_RAW status");
			return false;
		}
		try {
			if (!file.exists())
				file.createNewFile();
			FileOutputStream of = new FileOutputStream(file);
			String ss = G.get(0, 0) + " " + G.get(0, 1) + " " + G.get(0, 2) + " " + G.get(0, 3) + System.getProperty("line.separator")
					  + G.get(1, 0) + " " + G.get(1, 1) + " " + G.get(1, 2) + " " + G.get(1, 3) + System.getProperty("line.separator")
					  + G.get(2, 0) + " " + G.get(2, 1) + " " + G.get(2, 2) + " " + G.get(2, 3) + System.getProperty("line.separator");
			of.write(ss.getBytes());
			for (int i = 0; i < alignCount; i++) {
				Matrix sc = alignments.get(i).screenPoint;
				Matrix sp = alignments.get(i).spacePoint;
				String sss = sc.get(0, 0) + " " + sc.get(1, 0) + " " + sc.get(2, 0) + " "
						   + sp.get(0, 0) + " " + sp.get(1, 0) + " " + sp.get(2, 0) + " " + sp.get(3, 0)
						   + System.getProperty("line.separator");
				of.write(sss.getBytes());
			}
			of.close();
			Log.i(TAG, "Write file done");
			return true;
		}
		catch (Exception e) {
			Log.e(TAG, "Write file failed");
		    return false;
		}
	}
}
