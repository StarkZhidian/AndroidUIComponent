package com.zhidian.itemoperateview.custom_view;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.zhidian.itemoperateview.R;

/**
 * 绘制三角形的 View
 */
public class TriangleView extends View {
    private static final String TAG = "TriangleView";
    public enum Type {
        EQUAL_TRIANGLE(0), ISOSCELES_TRIANGLE(1), OTHER_TRIANGLE(2);

        int value;

        Type(int value) {
            this.value = value;
        }
    }

    private Paint paint = new Paint();
    // 三个顶点的坐标
    private Point[] points = new Point[3];
    private int fillColor = Color.BLACK;
    // 是否是倒三角
    private boolean isHandstand = false;
    private Path path = new Path();
    // 是否需要自动计算三角形顶点
    private boolean isAutoComputeCoordinate = true;

    public TriangleView(Context context) {
        super(context);
        init(context, null);
    }

    public TriangleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TriangleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ty = context.obtainStyledAttributes(attrs, R.styleable.TriangleView);
            isHandstand = ty.getBoolean(R.styleable.TriangleView_isHandstand, false);
            Log.e(TAG, "isHandstand: " + isHandstand);
            fillColor = ty.getColor(R.styleable.TriangleView_color, Color.BLACK);
            ty.recycle();
        }
        paint.setAntiAlias(true);
        paint.setColor(fillColor);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point();
        }
    }

    public void setPoints(Point... points) {
        System.arraycopy(points, 0, this.points, 0, this.points.length);
        isAutoComputeCoordinate = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = getWidth();
        int height = getHeight();
        if (isAutoComputeCoordinate) {
            if (isHandstand) {
                points[0].x = points[0].y = 0;
                points[1].x = width;
                points[1].y = 0;
                points[2].x = width / 2;
                points[2].y = height;
            } else {
                points[0].x = width / 2;
                points[0].y = 0;
                points[1].x = 0;
                points[1].y = height;
                points[2].x = width;
                points[2].y = height;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.moveTo(points[0].x, points[0].y);
        path.lineTo(points[1].x, points[1].y);
        path.lineTo(points[2].x, points[2].y);
        path.lineTo(points[0].x, points[0].y);
        path.close();
        canvas.drawPath(path, paint);
    }
}
