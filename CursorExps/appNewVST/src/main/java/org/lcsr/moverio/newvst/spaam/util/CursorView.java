package org.lcsr.moverio.newvst.spaam.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by qian on 1/25/16.
 */
public class CursorView extends View {

    private float x, y, z;
    private Paint pt;
    private float cursorHalfLen, cursorHalfWid;

    public CursorView(Context context)
    {
        super(context);
        pt = new Paint();
        pt.setColor(Color.RED);
        x = 0;
        y = 0;
        z = 0;
        cursorHalfLen = 10;
        cursorHalfWid = 1;
    }

    public void setXYZ(double msgx, double msgy, double msgz){
        x = (float)msgx;
        y = (float)msgy;
        z = (float)msgz;
    }

    public void onDraw(Canvas canvas){
        canvas.drawColor(0);
        canvas.drawRect(x-cursorHalfLen, y-cursorHalfWid, x+cursorHalfLen, y+cursorHalfWid, pt);
        canvas.drawRect(x-cursorHalfWid, y-cursorHalfLen, x+cursorHalfWid, y+cursorHalfLen, pt);
    }

}