package com.carpediem.randy.drawble.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by randy on 15-11-2.
 */
public class HorizontalScrollViewEx extends ViewGroup{
    private int mChildrenSize;
    private int mChildrenWidth;
    private int mChildrenIndex;

    //分别记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    //分别记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    public HorizontalScrollViewEx(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (mScroller == null) {
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;

        int x = (int)ev.getX();
        int y = (int)ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                if (!mScroller.isFinished()) {//如果现在正在滑动，那么停止滑动，拦截事件，否则不拦截
                    mScroller.abortAnimation();
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (Math.abs(deltaX) > Math.abs(deltaY)) { //只拦截x方向上滑动比较大的时候
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
            default:
                break;
        }
        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 可以知道如果拦截了，就会调用自己的onTouchEvent方法啦
        mVelocityTracker.addMovement(event);
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX,0);
                break;
            }
            case MotionEvent.ACTION_UP:{
                int scrollX = getScrollX();
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) >= 50) {
                    mChildrenIndex = xVelocity >0 ? mChildrenIndex -1:mChildrenIndex+1;
                } else {
                    mChildrenIndex = (scrollX + mChildrenWidth/2)/mChildrenWidth;
                }

                mChildrenIndex = Math.max(0,Math.min(mChildrenIndex,mChildrenSize-1));

                int dx = mChildrenIndex*mChildrenWidth - scrollX;
                smoothScrollBy(dx,0);
                mVelocityTracker.clear();
                break;
            }
            default:
                break;
        }
        mLastY = y;
        mLastX = x;
        return true;
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        int childLeft = getPaddingLeft();

        final int childCount = getChildCount();
        mChildrenSize = childCount;
        int childTop = getPaddingTop();
        for (int iter=0;iter<childCount;iter++) {
            final View childView = getChildAt(iter);

            if (childView.getVisibility() != View.GONE) {
                final int width = childView.getMeasuredWidth();
                final  MarginLayoutParams params = (MarginLayoutParams)childView.getLayoutParams();
                mChildrenWidth = width;
                childLeft += params.leftMargin;
                childTop = getPaddingTop() + params.topMargin;
                childView.layout(childLeft,
                                childTop,childLeft+width,childTop+childView.getMeasuredHeight());
                childLeft = childLeft+width+params.rightMargin;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        //先让children measure自己，不然之后的各个数据无法获得
        measureChildren(widthMeasureSpec-getPaddingLeft()-getPaddingRight(),
                                heightMeasureSpec-getPaddingBottom()-getPaddingTop());

        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);



        if (childCount == 0) {
            setMeasuredDimension(widthSpecSize,heightSpecSize);
        } else if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            measuredWidth = measureSelfWidth();
            measuredHeight = measureSelfHeight();
            setMeasuredDimension(measuredWidth,measuredHeight);

        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            measuredHeight = measureSelfHeight();
            setMeasuredDimension(widthSpecSize,measuredHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            measuredWidth = measureSelfWidth();
            setMeasuredDimension(measuredWidth,heightSpecSize);
        }

        Log.i("test", "the widthMeasureSize is " + heightSpecSize + "the child height is" +
                getChildAt(0).getMeasuredHeight()+ "the viewgroup is "+getMeasuredHeight());
    }
    private int measureSelfHeight() {
        int maxHeight = 0;
        for (int i=0;i<mChildrenSize;i++) {
            final View child = getChildAt(i);
            final  MarginLayoutParams params = (MarginLayoutParams)child.getLayoutParams();
            maxHeight = Math.max(maxHeight,child.getMeasuredHeight()+params.bottomMargin+params.topMargin);
        }
        return maxHeight + getPaddingTop() + getPaddingBottom();
    }
    private int measureSelfWidth() {
        mChildrenSize = getChildCount();
        int leftWidth = getPaddingLeft();
        for (int i =0;i<mChildrenSize;i++) {
            final View childView = getChildAt(i);
            final  MarginLayoutParams params = (MarginLayoutParams)childView.getLayoutParams();
            leftWidth = leftWidth + childView.getMeasuredWidth()+params.leftMargin+ params.rightMargin;
        }
        leftWidth += getPaddingRight();
        return leftWidth;
    }
        private void smoothScrollBy(int dx,int dy) {
        mScroller.startScroll(getScrollX(),0,dx,0,500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT,MarginLayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }
}
