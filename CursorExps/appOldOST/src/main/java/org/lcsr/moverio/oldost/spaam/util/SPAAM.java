/*
 *  SPAAM.java
 *  
 *  Author(s): Long Qian
 *	LCSR, Johns Hopkins University
 *
 */

package org.lcsr.moverio.oldost.spaam.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;
import android.util.Log;
import Jama.*;

public class SPAAM {
	
	private static String TAG = "SPAAM";
	public enum SPAAMStatus {CALIB_RAW, DONE_RAW, CALIB_ADD, DONE_ADD};
	public SPAAMStatus status;
	private Matrix G;
	private Matrix A;
    private float[] GLProjection;
	
	public int countMax = 20;
	public int countAddMax = 4;
	public int countCurrent = 0;
	public int countTuple = 0;

	private List<Alignment> alignPoints;
	private List<PointTuple> alignTuples;
	
	private Matrix singlePoint;
	
	private List<Point> auxiliaryPoints;

    public boolean updated = false;
	
	
	public SPAAM( Matrix point ) {
		setupAuxiliaryPoints();
		setSinglePointLocation(point);
		alignPoints = new ArrayList<Alignment>();
		status = SPAAMStatus.CALIB_RAW;
		Log.i(TAG, "SPAAM initialized");
	}

	public SPAAM( double X, double Y, double Z ) {
		setupAuxiliaryPoints();
		setSinglePointLocation( X, Y, Z );
		alignPoints = new ArrayList<Alignment>();
		status = SPAAMStatus.CALIB_RAW;
		Log.i(TAG, "SPAAM initialized");
	}
	
	public SPAAM( ) {
		setupAuxiliaryPoints();
		setSinglePointLocation(0.0, 0.0, 0.0);
		alignPoints = new ArrayList<Alignment>();
		status = SPAAMStatus.CALIB_RAW;
		Log.i(TAG, "SPAAM initialized");
	}
	
	public void setupAuxiliaryPoints() {
		auxiliaryPoints = new ArrayList<Point>();
		auxiliaryPoints.add(new Point(80, 80));
		auxiliaryPoints.add(new Point(320, 80));
		auxiliaryPoints.add(new Point(560, 80));
		auxiliaryPoints.add(new Point(80, 400));
		auxiliaryPoints.add(new Point(320, 400));
		auxiliaryPoints.add(new Point(560, 400));
		auxiliaryPoints.add(new Point(240, 240));
		auxiliaryPoints.add(new Point(400, 240));
		auxiliaryPoints.add(new Point(80, 80));
		auxiliaryPoints.add(new Point(320, 80));
		auxiliaryPoints.add(new Point(560, 80));
		auxiliaryPoints.add(new Point(80, 400));
		auxiliaryPoints.add(new Point(320, 400));
		auxiliaryPoints.add(new Point(560, 400));
		auxiliaryPoints.add(new Point(240, 240));
		auxiliaryPoints.add(new Point(400, 240));
		auxiliaryPoints.add(new Point(80, 240));
		auxiliaryPoints.add(new Point(560, 240));
		auxiliaryPoints.add(new Point(320, 80));
		auxiliaryPoints.add(new Point(320, 400));
		Log.i(TAG, auxiliaryPoints.size() + " auxiliary points added");
	}
	
	public void setSinglePointLocation(Matrix point) {
		if ( point != null ) {
			singlePoint = point;
			Log.i(TAG, "Single point set successfully");
		}
		else
			Log.i(TAG, "Single point set failed");
	}
	
	public void setSinglePointLocation(double X, double Y, double Z) {
        double[] singlePointArray = {X, Y, Z, 1.0};
        singlePoint = new Matrix( singlePointArray, 4);
	}	
	
	public boolean cancelLast() {
		switch ( status ) {
		case CALIB_RAW:
			if ( countCurrent == 0 ) {
				Log.i(TAG, "None alignment made");
				return false;
			}
			else {
				alignPoints.remove(alignPoints.size()-1);
				countCurrent = alignPoints.size();
				Log.i(TAG, "Last alignment removed");
				break;
			}
		case DONE_RAW:
			status = SPAAMStatus.CALIB_RAW;
			alignPoints.remove(alignPoints.size()-1);
			countCurrent = alignPoints.size();
			G = null;
            updated = true;
			Log.i(TAG, "Last alignment removed, SPAAM result cleared");
			break;
		case CALIB_ADD:
			if ( countTuple == 0 ) {
				Log.i(TAG, "None tuple alignment made");
				return false;
			}
			else {
				alignTuples.remove(alignTuples.size()-1);
				countTuple = alignTuples.size();
				Log.i(TAG, "Last tuple alignment removed");
				break;
			}
		case DONE_ADD:
			alignTuples.remove(alignTuples.size()-1);
			countTuple = alignTuples.size();
			if ( countTuple < countAddMax ) {
				status = SPAAMStatus.CALIB_ADD;
				A = null;
                updated = true;
				Log.i(TAG, "Last tuple alignment removed, additional SPAAM result unavailable");
			}
			else {
				Log.i(TAG, "Recalculate A matrix");
				calculateA();
			}
			break;
		default:
			break;
		}
		return true;
	}
	
	public Point getLastCursorPoint() {
		if (countCurrent == 0)
			return null;
		else {
			Matrix temp =  alignPoints.get(countCurrent-1).screenPoint;
			return new Point((int)temp.get(0,0), (int)temp.get(1,0));
		}
	}
	
	public Point getLastAddCursorPoint() {
		if (countTuple == 0)
			return null;
		else
			return alignTuples.get(countTuple-1).clickPoint;
	}
	
	public Point getAuxiliaryPoint() {
		if ( status == SPAAMStatus.CALIB_RAW && countCurrent < auxiliaryPoints.size() )
			return auxiliaryPoints.get(countCurrent);
		else
			return null;
	}
	
	public void clearSPAAM() {
		alignPoints = null;
		alignTuples = null;
		auxiliaryPoints = null;
		G = null;
		status = SPAAMStatus.CALIB_RAW;
		countCurrent = 0;
		countTuple = 0;
		singlePoint = null;
		A = null;
		Log.i(TAG, "SPAAM cleared");
	}

	public void newAlignment(int X, int Y, Matrix M ){
		if ( status != SPAAMStatus.CALIB_RAW ) {
			Log.i(TAG, "Not in CALIB_RAW status");
		}
		else {
			Matrix temp = M.times(singlePoint);

			// Clearing out outlier
			if (temp.get(2,0) > 0 || temp.get(2,0) < -5000){
				return;
			}
			// Done clearing out outlier
			Point pt = getAuxiliaryPoint();
			Alignment a = new Alignment(pt.x, pt.y, temp);
			if ( countCurrent < countMax ) {
				alignPoints.add(a);
				countCurrent = alignPoints.size();
				Log.i(TAG, "New alignment updated");
			}
			if ( countCurrent == countMax ) {
				Log.i(TAG, "Calculate G transformation");
				calculateG();
			}
		}
	}

	public void newAlignment(Matrix S, Matrix M ){
		if ( status != SPAAMStatus.CALIB_RAW ) {
			Log.i(TAG, "Not in CALIB_RAW status");
		}
		else {
			Matrix temp = M.times(singlePoint);

			// Clearing out outlier
			if (temp.get(2,0) > 0 || temp.get(2,0) < -5000){
				return;
			}
			// Done clearing out outlier

			Alignment a = new Alignment(S, temp);
			if ( countCurrent < countMax ) {
				alignPoints.add(a);
				countCurrent = alignPoints.size();
				Log.i(TAG, "New alignment updated");
			}
			if ( countCurrent == countMax ) {
				Log.i(TAG, "Calculate G transformation");
				calculateG();
			}
		}
	}

	public void newTuple(Point clickPoint, Matrix M) {
		if ( status != SPAAMStatus.CALIB_ADD ) {
			Log.i(TAG, "Not in CALIB_ADD status");
		}
		else {
			Matrix temp = G.times( M.times( singlePoint ));
			PointTuple pt = new PointTuple( clickPoint, temp );
			if ( countTuple < countAddMax ) {
				alignTuples.add(pt);
				countTuple = alignTuples.size();
				Log.i(TAG, "New tuple added");
			}
			if ( countTuple == countAddMax ) {
				Log.i(TAG, "Calculate A matrix");
				calculateA();
			}
		}
	}

	public void newTuple(int X, int Y, Matrix M) {
		if ( status != SPAAMStatus.CALIB_ADD && status != SPAAMStatus.DONE_ADD ) {
			Log.i(TAG, "Not in CALIB_ADD status");
		}
		else {
			Matrix temp = G.times( M.times( singlePoint ));
			PointTuple pt = new PointTuple( X, Y, temp );
			for ( int i = 0; i < countTuple; i++ ) {
				if (alignTuples.get(i).closeTo(pt)) {
					alignTuples.remove(i);
					break;
				}
			}
			alignTuples.add(pt);
			countTuple = alignTuples.size();
			Log.i(TAG, "New tuple added");
			if ( countTuple >= countAddMax ) {
				Log.i(TAG, "Calculate A matrix");
				calculateA();
			}
		}
	}
	
	public void calculateA() {
		double screenWidth = 640.0;
		double screenHeight = 480.0;
		Matrix M = new Matrix(countTuple*2, 4, 0.0);
		Matrix b = new Matrix(countTuple*2, 1, 0.0);
		for ( int i = 0; i < countTuple; i++) {
			PointTuple pt = alignTuples.get(i);
			M.set( 2*i, 0, pt.calcPoint.x / screenWidth );
			M.set( 2*i, 2, 1.0);
			M.set( 2*i+1, 1, pt.calcPoint.y / screenHeight );
			M.set( 2*i+1, 3, 1.0);
			b.set( 2*i, 0, pt.clickPoint.x / screenWidth );
			b.set( 2*i+1, 0, pt.clickPoint.y / screenHeight );
		}

		Matrix temp = M.solve(b);
		A = new Matrix(3, 3);
		A.set(0, 0, temp.get(0, 0));
		A.set(1, 1, temp.get(1, 0));
		A.set(0, 2, temp.get(2, 0)*screenWidth);
		A.set(1, 2, temp.get(3, 0)*screenHeight);
		A.set(2, 2, 1.0);
		status = SPAAMStatus.DONE_ADD;
		
		Log.i(TAG, "Matrix A computed");
		Log.i(TAG, temp.get(0,0) + " " + temp.get(1, 0) + " " + temp.get(2,0) + " " + temp.get(3,0));
        updated = true;
	}
	
	public boolean startAddCalib() {
		if ( status == SPAAMStatus.CALIB_RAW ) {
			Log.i(TAG, "Not supported for CALIB_RAW status");
			return false;
		}
		status = SPAAMStatus.CALIB_ADD;
		countTuple = 0;
		alignTuples = new ArrayList<PointTuple>();
		A = null;
		return true;
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
		tempAvg = Alignment.avg(alignPoints, 0, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignPoints, 0, Alignment.PointType.Screen);
		transformScreenPoint.set(0, 2, Alignment.avg(alignPoints, 0, Alignment.PointType.Screen));
		transformScreenPoint.set(0, 0, Math.sqrt(tempNorm - countCurrent * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 1, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignPoints, 1, Alignment.PointType.Screen);
		transformScreenPoint.set(1, 2, tempAvg);
		transformScreenPoint.set(1, 1, Math.sqrt(tempNorm - countCurrent * tempAvg * tempAvg ));
		transformScreenPoint.set(2, 2, 1.0);

		// transformSpacePoint
		transformSpacePoint = new Matrix(4, 4, 0.0);
		tempAvg = Alignment.avg(alignPoints, 0, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 0, Alignment.PointType.Space);
		transformSpacePoint.set(0, 3, tempAvg);
		transformSpacePoint.set(0, 0, Math.sqrt(tempNorm - countCurrent * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 1, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 1, Alignment.PointType.Space);
		transformSpacePoint.set(1, 3, tempAvg);
		transformSpacePoint.set(1, 1, Math.sqrt(tempNorm - countCurrent * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 2, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 2, Alignment.PointType.Space);
		transformSpacePoint.set(2, 3, tempAvg);
		transformSpacePoint.set(2, 2, Math.sqrt(tempNorm - countCurrent * tempAvg * tempAvg ));
		transformSpacePoint.set(3, 3, 1.0);

		// calculate transformation inverse
		transformScreenPointInv = transformScreenPoint.inverse();
		transformSpacePointInv = transformSpacePoint.inverse();


		// calculate G_core
		List<Alignment> alignPointsTrans = new ArrayList<Alignment>();
		for ( int i = 0; i < countCurrent; i++ ) {
			Alignment a = alignPoints.get(i);
			alignPointsTrans.add(new Alignment(transformScreenPointInv.times(a.screenPoint),
					transformSpacePointInv.times(a.spacePoint)));
		}
		
		Matrix B = new Matrix(2*countCurrent, 12, 0.0);
		for ( int i = 0; i < countCurrent; i++ ) {
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
        updated = true;
	}

    public Matrix getCalibMat() {
        switch (status) {
            case CALIB_RAW:
                return null;
            case DONE_RAW:
                return G;
            case CALIB_ADD:
                return G;
            case DONE_ADD:
                return A.times(G);
        }
        return null;
    }


	public Point calculateScreenPoint( Matrix T, Matrix spacePoint ) {
		if ( T == null || G == null )
			return null;
        Matrix GorGA = getCalibMat();
        Matrix screenPoint = null;
        if ( GorGA != null ) {
            screenPoint = GorGA.times(T.times(spacePoint));
            Alignment.Normalize(screenPoint);
        }
		if ( screenPoint == null )
			return null;
		else
			return new Point((int)(screenPoint.get(0, 0)), (int)(screenPoint.get(1,0)));
	}

	public List<Alignment> getAlignmenttList() {
		return alignPoints;
	}
	
	public List<PointTuple> getTupleList() {
		return alignTuples;
	}
	
	public int getAuxiliaryPointsSize() {
		return auxiliaryPoints.size();
	}
		
	public void setMaxAlignment(int max) {
		countMax = max;
		countCurrent = 0;
	}
	
	public void setMaxTuple(int max) {
		countAddMax = max;
		countTuple = 0;
	}

	public boolean readFile(File file) {
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
		    double[] GA = new double[12];
		    for (int i = 0; i < 12; i++) {
		    	GA[i] = values.get(0);
		    	values.remove(0);
		    }
		    if (values.size() % 7 != 0 && values.size() / 7 != countMax) {
		    	Log.e(TAG, "File format wrong");
		    	return false;
		    }
		    Matrix temp = new Matrix(GA, 4);
		    G = temp.transpose();
		    alignPoints = new ArrayList<Alignment>();
		    int alignCount = values.size() / 7;
		    for (int i = 0; i < alignCount; i++) {
		    	Matrix sc = new Matrix(3, 1);
		    	Matrix sp = new Matrix(4, 1);
		    	sc.set(0, 0, values.get(7*i + 0));
		    	sc.set(1, 0, values.get(7*i + 1));
		    	sc.set(2, 0, values.get(7*i + 2));
		    	sp.set(0, 0, values.get(7*i + 3));
		    	sp.set(1, 0, values.get(7*i + 4));
		    	sp.set(2, 0, values.get(7*i + 5));
		    	sp.set(3, 0, values.get(7*i + 6));
		    	alignPoints.add(new Alignment(sc, sp));
		    }
		    countMax = alignCount;
		    countCurrent = alignCount;
		    values = null;
		    status = SPAAMStatus.DONE_RAW;
		    A = null;
		    alignTuples = null;
            updated = true;
		    Log.e(TAG, "File successfully parsed, with " + countMax + " alignmnets");
		    return true;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		    return false;
		}
	}

	public boolean writeFile(File file) {
		if ( status == SPAAMStatus.CALIB_RAW ) {
			Log.i(TAG, "Not supported fir CALIB_RAW status");
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
			for (int i = 0; i < countCurrent; i++) {
				Matrix sc = alignPoints.get(i).screenPoint;
				Matrix sp = alignPoints.get(i).spacePoint;
				String sss = sc.get(0, 0) + " " + sc.get(1, 0) + " " + sc.get(2, 0) + " "
						   + sp.get(0, 0) + " " + sp.get(1, 0) + " " + sp.get(2, 0) + " " + sp.get(3, 0)
						   + System.getProperty("line.separator");
				of.write(sss.getBytes());
			}
			of.close();
			return true;
		}
		catch (Exception e) {
			Log.e(TAG, "Write file failed");
		    return false;
		}
	}
}
