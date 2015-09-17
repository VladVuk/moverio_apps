package org.lcsr.moverio.spaam.util;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import Jama.*;

public class SPAAM {
	
	private static String TAG = "SPAAM";
	
	private Matrix G = null;

	private List<Alignment> alignPoints = new ArrayList<Alignment>();
	private List<Alignment> alignPointsTrans = new ArrayList<Alignment>();
	private int countMax = 6;
	public static boolean OK = false;
	
	private Matrix singlePoint = null;
	private Matrix transformScreenPoint = null;
	private Matrix transformSpacePoint = null;
	private Matrix transformScreenPointInv = null;
	private Matrix transformSpacePointInv = null;
	
	
	public SPAAM() {
		Log.i(TAG, "SPAAM initialized");
	}
	
	public void setSinglePointLocation(Matrix point) {
		if ( point != null ) {
			singlePoint = point;
		}
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
		if ( alignPoints.size() < countMax ) {
			alignPoints.add(a);
			Log.i(TAG, "New alignment updated");
		}
		if ( alignPoints.size() == countMax && G == null ) {
			calculateG();
			Log.i(TAG, "Calculate G transformation");
		}
	}
	
	public Matrix getG() {
		return G;
	}
	
	public void calculateTransform() {
		double tempAvg;
		double tempNorm;
		int count = alignPoints.size();
		
		// transformScreenPoint
		transformScreenPoint = new Matrix(3, 3, 0.0);
		tempAvg = Alignment.avg(alignPoints, 0, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignPoints, 0, Alignment.PointType.Screen);
		transformScreenPoint.set(0, 2, Alignment.avg(alignPoints, 0, Alignment.PointType.Screen));
		transformScreenPoint.set(0, 0, Math.sqrt(tempNorm - count * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 1, Alignment.PointType.Screen);
		tempNorm = Alignment.norm(alignPoints, 1, Alignment.PointType.Screen);
		transformScreenPoint.set(1, 2, tempAvg);
		transformScreenPoint.set(1, 1, Math.sqrt(tempNorm - count * tempAvg * tempAvg ));
		transformScreenPoint.set(2, 2, 1.0);
		
		// transformSpacePoint
		transformSpacePoint = new Matrix(4, 4, 0.0);
		tempAvg = Alignment.avg(alignPoints, 0, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 0, Alignment.PointType.Space);
		transformSpacePoint.set(0, 3, tempAvg);
		transformSpacePoint.set(0, 0, Math.sqrt(tempNorm - count * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 1, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 1, Alignment.PointType.Space);
		transformSpacePoint.set(1, 3, tempAvg);
		transformSpacePoint.set(1, 1, Math.sqrt(tempNorm - count * tempAvg * tempAvg ));
		tempAvg = Alignment.avg(alignPoints, 2, Alignment.PointType.Space);
		tempNorm = Alignment.norm(alignPoints, 2, Alignment.PointType.Space);
		transformSpacePoint.set(2, 3, tempAvg);
		transformSpacePoint.set(2, 2, Math.sqrt(tempNorm - count * tempAvg * tempAvg ));
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
		int count = alignPoints.size();
		
		for ( int i = 0; i < count; i++ ) {
			Alignment a = alignPoints.get(i);
			alignPointsTrans.add(new Alignment(transformScreenPointInv.times(a.screenPoint),
					transformSpacePointInv.times(a.spacePoint)));
		}
		
		Matrix B = new Matrix(2*count, 12, 0.0);
		for ( int i = 0; i < count; i++ ) {
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

//		Log.i(TAG, G.get(0, 0) + ", " + G.get(0,1) + ", " + G.get(0,2) + ", " + G.get(0,3));
//		Log.i(TAG, G.get(1, 0) + ", " + G.get(1,1) + ", " + G.get(1,2) + ", " + G.get(1,3));
//		Log.i(TAG, G.get(2, 0) + ", " + G.get(2,1) + ", " + G.get(2,2) + ", " + G.get(2,3));

		Log.i(TAG, "Matrix G computed");
		
		OK = true;
		
		updateAlignment();
	}
	
	private void updateAlignment() {
		for ( int i = 0; i < alignPoints.size(); i++ ) {
			Alignment at = alignPointsTrans.get(i);
			Alignment a = alignPoints.get(i);
			a.setPointAligned( transformScreenPoint.times(G.times(at.spacePoint)) );
		}
	}
	
	public List<Alignment> getList() {
		return alignPoints;
	}
	
	public Matrix getSreenPointAligned( Matrix T ) {
		if ( T == null || G == null )
			return null;
		return transformScreenPoint.times( G.times( transformSpacePointInv.times( T.times( singlePoint ))));
	}

	public int getListSize() {
		return alignPoints.size();
	}

	public void setMaxAlignment(int max) {
		countMax = max;
	}
}
