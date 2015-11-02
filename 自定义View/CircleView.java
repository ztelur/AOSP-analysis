package com.carpediem.randy.drawble.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.carpediem.randy.drawble.R;

/**
 * Created by randy on 15-10-30.
 */

/**
 *  继承View重写onDraw方法,主要实现一些不规则的效果,一般需要重写onDraw方法
 *  需要自己支持wrap_content,而且padding也需要自己处理，
 *
 */
public class CircleView extends View{
    private int mColor = Color.RED;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = a.getColor(R.styleable.CircleView_circle_color,Color.RED);
        a.recycle();
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = a.getColor(R.styleable.CircleView_circle_color,Color.RED);
        a.recycle();
        init();
    }
    private void init() {
        mPaint.setColor(mColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        widthSpecSize = widthSpecMode == MeasureSpec.AT_MOST ? 200:widthSpecSize;
        heightSpecSize = heightSpecMode == MeasureSpec.AT_MOST?200:heightSpecSize;
        setMeasuredDimension(widthSpecSize,heightSpecSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() - getPaddingRight() - getPaddingLeft();
        int height = getHeight() - getPaddingBottom() - getPaddingTop();
        int radius = Math.min(width,height)/2;
        canvas.drawCircle(width/2+ getPaddingLeft(),height/2+ getPaddingTop(),radius,mPaint);
    }
}
