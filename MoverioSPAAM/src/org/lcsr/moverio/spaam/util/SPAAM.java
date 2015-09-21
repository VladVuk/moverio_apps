package org.lcsr.moverio.spaam.util;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import Jama.*;

public class SPAAM {
	
	private static String TAG = "SPAAM";
	
	public Matrix G;
	public boolean OK = false;
	public int countMax = 6;
	public int countCurrent = 0;

	private List<Alignment> alignPoints = new ArrayList<Alignment>();
	
	private Matrix singlePoint;
	private Matrix transformScreenPoint;
	private Matrix transformSpacePoint;
	private Matrix transformScreenPointInv;
	private Matrix transformSpacePointInv;
	
	public Matrix markerTrans;
	public Matrix markerPoint;
	
	
	public SPAAM() {
		Log.i(TAG, "SPAAM initialized");
	}
	
	public void setSinglePointLocation(Matrix point) {
		if ( point != null ) {
			singlePoint = point;
		}
	}
	
	public boolean cancalLast() {
		if (OK) {
			OK = false;
			alignPoints.remove(alignPoints.size()-1);
			countCurrent = alignPoints.size();
			transformScreenPoint = null;
			transformSpacePoint = null;
			transformScreenPointInv = null;
			transformSpacePointInv = null;
			G = null;
			markerTrans = null;
			markerPoint = null;
			return true;
		}
		else if (countCurrent != 0) {
			alignPoints.remove(alignPoints.size()-1);
			countCurrent = alignPoints.size();
			return true;
		}
		else
			return false;
	}
	
	public Matrix getLastCursorPoint() {
		if (countCurrent == 0)
			return null;
		else
			return alignPoints.get(countCurrent-1).screenPoint;
	}
	
	public void clearSPAAM() {
		alignPoints = null;
		G = null;
		OK = false;
		countCurrent = 0;
		singlePoint = null;
		Log.i(TAG, "Clear SPAAM");
	}
	
	public void setSinglePointLocation(double X, double Y, double Z) {
        double[] singlePointArray = {0.0, 0.0, 0.0, 1.0};
        singlePoint = new Matrix( singlePointArray, 4);
	}
	
	public void newAlignment(int X, int Y, Matrix M ){
		if ( singlePoint == null ) {
			Log.i(TAG, "Single point has not been set.");
		}
		Alignment a = new Alignment(X, Y, M.times(singlePoint));
		if ( countCurrent < countMax ) {
			alignPoints.add(a);
			countCurrent = alignPoints.size();
			Log.i(TAG, "New alignment updated");
		}
		if ( countCurrent == countMax && G == null ) {
			calculateG();
			Log.i(TAG, "Calculate G transformation");
		}
	}
	
	
	public void calculateTransform() {
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
	}
	
	public void calculateG() {
		if ( singlePoint == null ) {
			Log.i(TAG, "Single point not set");
			return;
		}

		calculateTransform();
		
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

		G = new Matrix(3, 4);
		G.setMatrix(0, 0, 0, 3, eigenVecTranspose.getMatrix(0, 0, 0, 3));
		G.setMatrix(1, 1, 0, 3, eigenVecTranspose.getMatrix(0, 0, 4, 7));
		G.setMatrix(2, 2, 0, 3, eigenVecTranspose.getMatrix(0, 0, 8, 11));

		Log.i(TAG, "Matrix G computed");
		
		OK = true;
		
//		for ( int i = 0; i < countCurrent; i++ ) {
//			Alignment at = alignPointsTrans.get(i);
//			Alignment a = alignPoints.get(i);
//			a.setPointAligned( transformScreenPoint.times(G.times(at.spacePoint)) );
//		}
	}
	
	public List<Alignment> getList() {
		return alignPoints;
	}
	
	public void updateSreenPointAligned( Matrix T ) {
		if ( T == null || G == null )
			return;
		markerTrans = T;
		markerPoint = transformScreenPoint.times( G.times( transformSpacePointInv.times( T.times( singlePoint ))));
	}

	public void setMaxAlignment(int max) {
		countMax = max;
		countCurrent = 0;
	}
}
