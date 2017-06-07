package com.lyzon.ui.lilyplayer.menuswipe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * Created by laoyongzhi on 2017/6/5.
 */

public class MenuView extends LinearLayout {

    public boolean canDrag ;
    private int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private int mDownX;
    private int mDownY;

    public MenuView(Context context) {
        this(context, null, 0);
    }

    public MenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canDrag = false;
                mDownY = (int) event.getRawY();
                mDownX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) event.getRawY();
                int x = (int) event.getRawX();
                int dDownY = y - mDownY;
                int dDownX = x - mDownX;
                //如果滑动达到一定距离
                if (Math.abs(dDownX) > touchSlop || Math.abs(dDownY) > touchSlop) {
                        if (Math.abs(dDownX) > Math.abs(dDownY)){
                            canDrag = true;
                        }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                canDrag = false;
                break;
        }

        return canDrag || super.onInterceptTouchEvent(event);
    }
}
