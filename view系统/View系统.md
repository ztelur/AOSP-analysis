

``` java
    public static int getDefaultSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
        case MeasureSpec.UNSPECIFIED:
            result = size;
            break;
        case MeasureSpec.AT_MOST:
        case MeasureSpec.EXACTLY:
            result = specSize;
            break;
        }
        return result;
    }
```
&emsp;一般来说，这个函数是用来获得view的默认的width和height的，所以它的使用一般如下
``` java
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),            getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));

}
```

#### setMeasuredDimens
&emsp;这个方法必须在onMeasure中调用，否则会引发Exception
而且里边并不简单啊
``` java
    protected final void setMeasuredDimension(int measuredWidth, int measuredHeight) {
        boolean optical = isLayoutModeOptical(this); //是否是拥有可见bound的viewgroup
        if (optical != isLayoutModeOptical(mParent)) { //如果和parent的不一致
            Insets insets = getOpticalInsets(); //一般是获得background的相关性质
            int opticalWidth  = insets.left + insets.right;
            int opticalHeight = insets.top  + insets.bottom;

            measuredWidth  += optical ? opticalWidth  : -opticalWidth;
            measuredHeight += optical ? opticalHeight : -opticalHeight;
        }
        setMeasuredDimensionRaw(measuredWidth, measuredHeight);
    }
```
&emsp;然后在`setMeasuredDimensionRaw(int,int)`中
``` java
  private void setMeasuredDimensionRaw(int measuredWidth, int measuredHeight) {
        mMeasuredWidth = measuredWidth;
        mMeasuredHeight = measuredHeight;

        mPrivateFlags |= PFLAG_MEASURED_DIMENSION_SET;
    }
```
#### computeOpticalInserts
``` java
    Insets computeOpticalInsets() {
        return (mBackground == null) ? Insets.NONE : mBackground.getOpticalInsets();
    }
```

``` java
    // ViewGroup中
        Insets computeOpticalInsets() {
        if (isLayoutModeOptical()) {
            int left = 0;
            int top = 0;
            int right = 0;
            int bottom = 0;
            for (int i = 0; i < mChildrenCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == VISIBLE) {
                    Insets insets = child.getOpticalInsets();
                    left =   Math.max(left,   insets.left);
                    top =    Math.max(top,    insets.top);
                    right =  Math.max(right,  insets.right);
                    bottom = Math.max(bottom, insets.bottom);
                }
            }
            return Insets.of(left, top, right, bottom);
        } else {
            return Insets.NONE;
        }
    }
```
#### getSuggestedMininumWidth()
``` java
    protected int getSuggestedMinimumWidth() {
        return (mBackground == null) ? mMinWidth : max(mMinWidth, mBackground.getMinimumWidth());
    }
```
#### requestLayout
&emsp;这个方法会在导致view tree中的一轮layout,如果现在正在layout,那么就不会执行，
[博客](http://daojin.iteye.com/blog/1860073)
#### layout
这是第二个阶段啦。layout,onLayout,其中onLayout是View,和ViewGroup都没有实现的。
``` java
    public void layout(int l, int t, int r, int b) {
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }

        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;

        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
            //只有当changed或者 FLAG_LAYOUT_REQUIRED被设置时才会layout
            onLayout(changed, l, t, r, b);
            mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;

            //然后通知Listener onLayoutChange
            ListenerInfo li = mListenerInfo;
            if (li != null && li.mOnLayoutChangeListeners != null) {
                ArrayList<OnLayoutChangeListener> listenersCopy =
                        (ArrayList<OnLayoutChangeListener>)li.mOnLayoutChangeListeners.clone();
                int numListeners = listenersCopy.size();
                for (int i = 0; i < numListeners; ++i) {
                    listenersCopy.get(i).onLayoutChange(this, l, t, r, b, oldL, oldT, oldR, oldB);
                }
            }
        }

        mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
        mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;
    }
```
``` java
    /**
     * Flag indicating that a call to measure() was skipped and should be done
     * instead when layout() is invoked.
     */
    static final int PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT = 0x8;
    //这个标记很有意思啊!!!
```

#### measure
``` java
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean optical = isLayoutModeOptical(this);
        if (optical != isLayoutModeOptical(mParent)) {
            Insets insets = getOpticalInsets();
            int oWidth  = insets.left + insets.right;
            int oHeight = insets.top  + insets.bottom;
            widthMeasureSpec  = MeasureSpec.adjust(widthMeasureSpec,  optical ? -oWidth  : oWidth);
            heightMeasureSpec = MeasureSpec.adjust(heightMeasureSpec, optical ? -oHeight : oHeight);
        }
        //首先根据optical来跳转width和height的规格

        // Suppress sign extension for the low bytes
        long key = (long) widthMeasureSpec << 32 | (long) heightMeasureSpec & 0xffffffffL;
        if (mMeasureCache == null) mMeasureCache = new LongSparseLongArray(2);

        final boolean forceLayout = (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;
        final boolean isExactly = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY;
        final boolean matchingSize = isExactly &&
                getMeasuredWidth() == MeasureSpec.getSize(widthMeasureSpec) &&
                getMeasuredHeight() == MeasureSpec.getSize(heightMeasureSpec);
        //经过很多判断才会继续调用的啊，减少measure的次数啊
        if (forceLayout || !matchingSize &&
                (widthMeasureSpec != mOldWidthMeasureSpec ||
                        heightMeasureSpec != mOldHeightMeasureSpec)) {

            // first clears the measured dimension flag
            mPrivateFlags &= ~PFLAG_MEASURED_DIMENSION_SET;

            resolveRtlPropertiesIfNeeded();

            int cacheIndex = forceLayout ? -1 : mMeasureCache.indexOfKey(key);
            //竟然还有一个缓存啊.....
            if (cacheIndex < 0 || sIgnoreMeasureCache) {
                // measure ourselves, this should set the measured dimension flag back
                onMeasure(widthMeasureSpec, heightMeasureSpec);
                mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            } else {
                long value = mMeasureCache.valueAt(cacheIndex);
                // Casting a long to int drops the high 32 bits, no mask needed
                setMeasuredDimensionRaw((int) (value >> 32), (int) value);
                //但是这里置上了标志位，需要在layout时进行measure
                mPrivateFlags3 |= PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
            }

            // flag not set, setMeasuredDimension() was not invoked, we raise
            // an exception to warn the developer
            if ((mPrivateFlags & PFLAG_MEASURED_DIMENSION_SET) != PFLAG_MEASURED_DIMENSION_SET) {
                throw new IllegalStateException("onMeasure() did not set the"
                        + " measured dimension by calling"
                        + " setMeasuredDimension()");
            }

            mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
        }

        mOldWidthMeasureSpec = widthMeasureSpec;
        mOldHeightMeasureSpec = heightMeasureSpec;

        mMeasureCache.put(key, ((long) mMeasuredWidth) << 32 |
                (long) mMeasuredHeight & 0xffffffffL); // suppress sign extension
    }
```
    