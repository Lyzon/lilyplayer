package com.lyzon.ui.lilyplayer.menuswipe;

/**
 * Created by laoyongzhi on 2017/6/4.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class MainView extends LinearLayout {
    private MenuSwipe mMenuSwipe;

    public MainView(Context context) {
        this(context, null, 0);
    }

    public MainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setParent(MenuSwipe menuSwipe) {
        mMenuSwipe = menuSwipe;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mMenuSwipe.isOpened() || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMenuSwipe.isOpened()) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mMenuSwipe.closeMenu();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
}