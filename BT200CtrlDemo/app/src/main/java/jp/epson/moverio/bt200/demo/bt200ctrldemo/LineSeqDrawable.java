package jp.epson.moverio.bt200.demo.bt200ctrldemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by Akshay on 2/15/2017.
 */
public class LineSeqDrawable extends Drawable {
    private int mBackgroundColor;
    private int mStrokeWidth;
    private int mStrokeColor;
    private int mDegreeRotation;
    public Wall[] mWalls;

    public LineSeqDrawable(){
        mBackgroundColor = Color.DKGRAY;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 10;
        mDegreeRotation = 0;
    }

    public LineSeqDrawable(int backgroundColor, int strokeColor, int strokeWidth, Wall[] walls){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mWalls = walls;
        mDegreeRotation = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.rotate((float)mDegreeRotation,150,150);
        canvas.drawColor(mBackgroundColor);
        for(int i=0; i < mWalls.length; i++){
            canvas.drawLine(mWalls[i].startX,mWalls[i].startY,mWalls[i].endX,mWalls[i].endY,mPaint);
        }
        mPaint.setColor(Color.RED);
        Path newPath = new Path();
        newPath.moveTo(140,150);
        newPath.lineTo(150,140);
        newPath.lineTo(160,150);
        newPath.moveTo(150,140);
        newPath.lineTo(160,150);
        newPath.close();
        canvas.drawPath(newPath,mPaint);
    }

    public void setWallArray(Wall[] walls){
        mWalls = walls;
    }

    public Wall[] getWallArray(){return mWalls;}

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setStrokeColor(int color){
        mStrokeColor = color;
    }

    public void setDegreeRotation(int degreeRotation){mDegreeRotation = degreeRotation;}

    @Override
    public int getIntrinsicHeight() {
        return 300;
    }

    @Override
    public int getIntrinsicWidth() {
        return 300;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
