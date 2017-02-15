package jp.epson.moverio.bt200.demo.bt200ctrldemo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by Akshay on 2/15/2017.
 */
public class LineSeqDrawable extends Drawable {
    private int mBackgroundColor;
    private int mStrokeWidth;
    private int mStrokeColor;
    public Wall[] mWalls;

    public LineSeqDrawable(){
        mBackgroundColor = Color.BLACK;
        mStrokeColor = Color.BLUE;
        mStrokeWidth = 2;
    }

    public LineSeqDrawable(int backgroundColor, int strokeColor, int strokeWidth, Wall[] walls){
        mBackgroundColor = backgroundColor;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mWalls = walls;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(mStrokeColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawColor(mBackgroundColor);
        for(int i=0; i < mWalls.length; i++){
            canvas.drawLine(mWalls[i].startX,mWalls[i].startY,mWalls[i].endX,mWalls[i].endY,mPaint);
        }
        canvas.save();
    }

    public void setWallArray(Wall[] walls){
        mWalls = walls;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setStrokeColor(int color){
        mStrokeColor = color;
    }


    @Override
    public int getIntrinsicHeight() {
        return 60;
    }

    @Override
    public int getIntrinsicWidth() {
        return 60;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
