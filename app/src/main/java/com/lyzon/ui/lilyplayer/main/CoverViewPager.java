package com.lyzon.ui.lilyplayer.main;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by laoyongzhi on 2017/6/5.
 */

public class CoverViewPager extends ViewPager {

    public CoverViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
       return false;
    }

}
