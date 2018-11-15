package com.zhidian.itemoperateview.custom_view;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MyConstraintLayout extends ConstraintLayout {
    private static final String TAG = MyConstraintLayout.class.getSimpleName();

    public enum TouchEventType {
        ON_DOWN, ON_SHOW_PRESS, ON_SINGLE_TAP_UP, ON_SCROLL, ON_LONG_PRESS, ON_FLING
    }

    public interface TouchEventListener {
        boolean onTouchEvent(View view, TouchEventType type, MotionEvent event);
    }

    private GestureDetector detector;
    private TouchEventListener touchEventListener;

    public MyConstraintLayout(Context context) {
        super(context);
        init(context);
    }

    public MyConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setTouchEventListener(TouchEventListener touchEventListener) {
        this.touchEventListener = touchEventListener;
    }

    private void init(Context context) {
        detector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                if (touchEventListener != null) {
                    return touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_DOWN, motionEvent);
                }
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {
                if (touchEventListener != null) {
                    touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_SHOW_PRESS, motionEvent);
                }
            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                if (touchEventListener != null) {
                    return touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_SINGLE_TAP_UP, motionEvent);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                if (touchEventListener != null) {
                    return touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_SCROLL, motionEvent);
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.e(TAG, motionEvent.getAction() + "");
                if (touchEventListener != null) {
                    touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_LONG_PRESS, motionEvent);
                }
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                if (touchEventListener != null) {
                    return touchEventListener.onTouchEvent(MyConstraintLayout.this,
                            TouchEventType.ON_FLING, motionEvent);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
