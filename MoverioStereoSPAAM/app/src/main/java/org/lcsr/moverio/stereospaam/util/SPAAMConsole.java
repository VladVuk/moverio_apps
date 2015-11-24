package org.lcsr.moverio.stereospaam.util;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import org.lcsr.moverio.stereospaam.util.SPAAM.*;
import java.util.Random;

import Jama.Matrix;

/**
 * Created by qian on 11/22/15.
 */
public class SPAAMConsole {
    private static String TAG = "SPAAMConsole";
    private SPAAMStatus status;
    private SPAAM left, right;
    private List<Point> pointsLeft, pointsRight;
    private boolean updated;

    private int alignCount;
    private int alignMax;
    private Matrix singlePoint;

    public SPAAMConsole(){
        alignCount = 0;
        alignMax = 10;
        updated = false;
        status = SPAAMStatus.CALIB_RAW;
        double[] singlePointArray = {0.0, 0.0, 0.0, 1.0};
        singlePoint = new Matrix( singlePointArray, 4);

        left = new SPAAM( "Left" );
        right = new SPAAM( "Right" );
        left.setAlignMax(alignMax);
        right.setAlignMax(alignMax);
        left.setSinglePointLocation(singlePoint);
        right.setSinglePointLocation(singlePoint);
        initScreenPoints();
        Log.i(TAG, "constructed");
    }

    public void readFile(){
        left.readFile();
        right.readFile();
        synchronizeAlignCount();
        synchronizeStatus();
        updated = true;
    }

    public void writeFile(){
        left.writeFile();
        right.writeFile();
    }


    public Point calculateScreenPointLeft(Matrix T, Matrix spacePoint){
        if (spacePoint == null)
            spacePoint = singlePoint;
        return left.calculateScreenPoint(T, spacePoint);
    }

    public Point calculateScreenPointRight(Matrix T, Matrix spacePoint){
        if (spacePoint == null)
            spacePoint = singlePoint;
        return right.calculateScreenPoint(T, spacePoint);
    }

    public Point getLastCursorPointLeft(){
        return left.getLastCursorPoint();
    }

    public Point getLastCursorPointRight(){
        return right.getLastCursorPoint();
    }

    public Point getNextScreenPointLeft(){
        if (status == SPAAMStatus.DONE_RAW)
            return null;
        else
            return pointsLeft.get(alignCount);
    }

    public Point getNextScreenPointRight(){
        if (status == SPAAMStatus.DONE_RAW)
            return null;
        else
            return pointsRight.get(alignCount);
    }

    public void clicked(Matrix T){
        if (status == SPAAMStatus.DONE_RAW)
            return;
        left.newAlignment(pointsLeft.get(alignCount), T);
        right.newAlignment(pointsRight.get(alignCount), T);
        synchronizeAlignCount();
        synchronizeStatus();
        updated = true;
    }

    public void synchronizeAlignCount(){
        int ac1 = left.getAlignCount();
        int ac2 = right.getAlignCount();
        if (ac1 != ac2){
            Log.e(TAG, "Synchronization of alignCount failed");
        }
        else
            alignCount = ac1;
    }

    public void cancelLast(){
        left.cancelLast();
        right.cancelLast();
        synchronizeAlignCount();
        synchronizeStatus();
        updated = true;
    }

    public void synchronizeStatus(){
        SPAAMStatus st1 = left.getStatus();
        SPAAMStatus st2 = right.getStatus();
        if (st1 != st2){
            Log.e(TAG, "Synchronization of status failed");
        }
        else
            status = st1;
    }


    public void initScreenPoints(){
        pointsLeft = new ArrayList<Point>();
        pointsRight = new ArrayList<Point>();
        Random r = new Random();
        int offsetMin = 300;
        int offsetMax = 400;
        int trapezoidPadding = 100;
        int length = 40;
        int x1 = offsetMax/2 + length/2 + trapezoidPadding;
        int x2 = 960 - offsetMax/2 - length/2 - trapezoidPadding;
        int y1 = length/2;
        int y2 = 492 - length/2;
        for (int i = 0; i < alignMax; i++) {
            int x = r.nextInt(x2-x1) + x1;
            int y = r.nextInt(y2-y1) + y1;
            int offset = r.nextInt(offsetMax-offsetMin) + offsetMin;
            pointsLeft.add(new Point(x+offset/2, y));
            pointsRight.add(new Point(x-offset/2, y));
        }
    }

    public SPAAMStatus getStatus(){
        return status;
    }

    public Matrix getCalibMatLeft(){
        return left.getCalibMat();
    }

    public Matrix getCalibMatRight(){
        return right.getCalibMat();
    }

    public void clearup(){
        left.clearup();
        right.clearup();
        synchronizeAlignCount();
        synchronizeStatus();
    }

    public int getAlignCount(){
        return alignCount;
    }

    public int getAlignMax(){
        return alignMax;
    }

    public boolean getUpdated(){
        return updated;
    }

    public void setUpdated(boolean u){
        updated = u;
    }
}
