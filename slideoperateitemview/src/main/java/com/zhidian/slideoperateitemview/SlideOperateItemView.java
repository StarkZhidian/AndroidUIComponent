package com.zhidian.slideoperateitemview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by 指点 on 2018/1/4.
 * 通过向左横向滑动来显示 OperateView，默认将最后一个子 View 作为 OperateView，
 * 当然可以通过 setOperateView 方法来指定 OperateView ，
 * 如果你不需要横向滑动的效果，需设置 OperateView 为 null
 */

public class SlideOperateItemView extends ViewGroup {

    // 触发的滑动 x 方向滑动距离占 operateView 宽度的最小比例
    public float startScrollXYMinRatio = 0.2f;
    // 拦截滑动事件时的 x y 方向滑动距离最小比例
    public float triggerXYRatio = 3;

    protected Context context = null;
    protected View operateView = null; // 操作 view
    protected Scroller scroller = null; // 滑动操作
    protected int lastX; // 上次滑动的 x 坐标
    protected int lastY; // 上次滑动的 y 坐标
    protected boolean isHorizontalScroll = false; // 是否横向滑动
    private int currentScrollX = 0; // 当前滑动事件 x 轴滑动的距离

    // 判断当前滑动状态是处于向左边滑动显示 OperateView 还是向右边滑动隐藏 OperateView
    protected boolean isShowOperateView = false;
    protected boolean isHideOperateView = false;
    protected SlideListener slideListener = null;

    public void setSlideListener(SlideListener slideListener) {
        this.slideListener = slideListener;
    }

    // 将 SlideOperateItemView 向左滑动将 OperateView 显示出来，带有滑动效果
    public void smoothScrollToShowOperatePosition(int currentScrollX) {
        if (operateView == null) {
            return;
        }
        isShowOperateView = true;
        int operateViewWidth = operateView.getWidth();
        // 处理 operateView 布局参数的 leftMargin 和 rightMargin，确保整个 OperateView 能完全显示出来
        ViewGroup.LayoutParams lp = operateView.getLayoutParams();
        if (lp instanceof LayoutParams) {
            operateViewWidth += ((LayoutParams) lp).leftMargin + ((LayoutParams) lp).rightMargin;
        }
        // 加上 OperateView 前一个 View（如果有）的 rightMargin，确保整个 OperateView 能完全显示出来
        int operateViewIndex = indexOfChild(operateView);
        if (operateViewIndex != -1 && operateViewIndex > 0) {
            lp = getChildAt(operateViewIndex-1).getLayoutParams();
            if (lp instanceof LayoutParams) {
                operateViewWidth += ((LayoutParams) lp).rightMargin;
            }
        }
        scroller.startScroll(currentScrollX, 0, operateViewWidth - currentScrollX, 0);
        invalidate();
    }

    // 将 SlideOperateItemView 滑动回初始位置，带有滑动效果
    public void smoothScrollToInitPosition() {
        isHideOperateView = true;
        int scrollX = getScrollX();
        if (scrollX != 0) {
            scroller.startScroll(scrollX, 0, -scrollX, 0);
            invalidate();
        }
    }

    // 将 SlideOperateItemView 滑回初始位置，没有滑动效果
    public void resetScrollPosition() {
        int scrollX = getScrollX();
        if (scrollX != 0) {
            scrollBy(-scrollX, 0);
        }
    }

    /**
     * 设置 operateView，如果参数 operateView 为空，证明移除 operateView
     */
    public void setOperateView(@Nullable View operateView) {
        if (this.operateView == operateView) {
            return ;
        }
        // 移除 OperateView
        if (this.operateView != null && operateView == null && indexOfChild(this.operateView) != -1) {
            removeView(this.operateView);
        } else if (this.operateView == null && operateView != null) { // 设置 OperateView
            if (indexOfChild(operateView) != -1) {
                removeView(operateView);
            }
            addView(operateView);
        } else { // 两个 OperateView 都不为空
            if (indexOfChild(this.operateView) != -1) {
                removeView(this.operateView);
            }
            if (indexOfChild(operateView) != -1) {
                removeView(operateView);
            }
            addView(operateView);
        }
        this.operateView = operateView;
    }

    public View getOperateView() {
        return operateView;
    }

    // 重置滑动信息
    protected void resetScrollInfo() {
        this.isHorizontalScroll = false;
        this.currentScrollX = 0;
    }

    protected void init(Context context) {
        this.context = context;
        scroller = new Scroller(context); // 滑动对象

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 默认将最后一个子 View 作为 OperateView
        int childCount = getChildCount();
        if (childCount > 0) {
            setOperateView(getChildAt(childCount-1));
        }
    }

    public SlideOperateItemView(Context context) {
        super(context);
        init(context);
    }

    public SlideOperateItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideOperateItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 上下左右的 padding 值
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int childCount = getChildCount();
        View childView;
        ViewGroup.LayoutParams childLP = null;
        // 记录子 View 的宽度和高度模式是否为 MATCH_PARENT ，
        // 如果是，那么如果当前 ViewGroup 的高度模式为 WRAP_CONTENT 时要特殊处理，
        boolean[] heightMatchParentMark = new boolean[childCount];
        boolean[] widthMatchParentMark = new boolean[childCount];
        for (int i = 0; i < childCount; i++) {
            childView = getChildAt(i);
            childLP = childView.getLayoutParams();
            if (childLP.width == LayoutParams.MATCH_PARENT) {
                widthMatchParentMark[i] = true;
            }
            if (childLP.height == LayoutParams.MATCH_PARENT) {
                heightMatchParentMark[i] = true;
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                // 带有 margin 属性测量子 view
                measureChildWithMargins(childView, widthMeasureSpec, 0,
                        heightMeasureSpec, 0);
            }
        }
        // 记录是否需要重新测量一遍子 View
        boolean needMeasureAgain = false;
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            width = 0;
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                width += childView.getMeasuredWidth();
            }
            // 如果有子 View 的宽度模式为 MATCH_PARENT ，
            // 那么需要重新测量一遍子 View，保证其的宽度和父容器的宽度相同
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                if (widthMatchParentMark[i]) {
                    childView.getLayoutParams().width = width;
                    needMeasureAgain = true;
                }
            }
            // 处理子 View 的横向 margin ，即当前 ViewGroup 的宽度要加上子 View 的 leftMargin 和 rightMargin
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                childLP = childView.getLayoutParams();
                if (childLP instanceof LayoutParams) {
                    width += ((LayoutParams) childLP).leftMargin + ((LayoutParams) childLP).rightMargin;
                }
            }
            // 最后加上 ViewGroup 本身横向的 padding 值
            width += paddingLeft + paddingRight;
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            height = 0;
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                height = Math.max(height, childView.getMeasuredHeight());
            }
            // 如果有子 View 的高度模式为 MATCH_PARENT ，那么需要重新测量一遍子 View，
            // 保证子 View 的高度和父容器相同。
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                if (heightMatchParentMark[i]) {
                    childView.getLayoutParams().height = height;
                    needMeasureAgain = true;
                }
            }
            // 处理子 View 的竖向 margin。当前 ViewGroup 要加上子 View 的 topMargin 和 bottomMargin
            for (int i = 0; i < childCount; i++) {
                childView = getChildAt(i);
                childLP = childView.getLayoutParams();
                if (childLP instanceof LayoutParams) {
                    height += ((LayoutParams) childLP).topMargin + ((LayoutParams) childLP).bottomMargin;
                }
            }
            // 最后加上 ViewGroup 本身的竖向的 padding 值
            height += paddingTop + paddingBottom;
        }
        // 如果 needMeasureAgain 为 true ，证明有子 View 的宽/高的布局参数为 MATCH_PARENT，
        // 则需要再测量一次子 View
        if (needMeasureAgain) {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    // 水平方向从左往右布局子 view
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 上左方向的 padding 值
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();

        int childCount = getChildCount();
        View childView;
        ViewGroup.LayoutParams lp;
        int currentLeft = paddingLeft, currentTop;
        for (int i = 0; i < childCount; i++) {
            childView = getChildAt(i);
            // 如果当前子 View 不可用，那么不对它进行布局
            if (childView.getVisibility() == GONE) {
                continue;
            }
            lp = childView.getLayoutParams();
            currentTop = paddingTop;
            // 处理每个子 View 的左上方 margin
            if (lp instanceof LayoutParams) {
                currentLeft += ((MarginLayoutParams) lp).leftMargin;
                currentTop += ((MarginLayoutParams) lp).topMargin;
            }
            // 布局子 View 的位置
            childView.layout(currentLeft, currentTop,
                    currentLeft += childView.getMeasuredWidth(),
                    currentTop + childView.getMeasuredHeight());
            // 加上子 View 右边 margin
            if (lp instanceof LayoutParams) {
                currentLeft += ((MarginLayoutParams) lp).rightMargin;
            }
        }
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        } else { // 滑动完成
            // 如果滑动监听对象不为空并且处于对应状态那么，调用相关方法
            if (slideListener != null) {
                if (isShowOperateView) {
                    isShowOperateView = false; // 重置 SlideOperateItemView 滑动状态
                    slideListener.finishShowOperateView();
                } else if (isHideOperateView) {
                    isHideOperateView = false; // 重置 SlideOperateItemView 滑动状态
                    slideListener.finishHideOperateView();
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getX();
                lastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果横向滑动的距离大于纵向滑动的距离，拦截事件，
                // 适用于某个子 View 的 onTouchEvent 方法返回 true 的情况
                if (Math.abs(ev.getX() - lastX) > Math.abs(ev.getY() - lastY)*triggerXYRatio) {
                    return isHorizontalScroll = true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 默认不调用父类的 onTouchEvent，屏蔽该 ViewGroup 的 onClickListener 监听
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        int currentX = (int) event.getX();
        int currentY = (int) event.getY();
        if (operateView != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = currentX;
                    lastY = currentY;
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int disX = currentX - lastX;
                    // 如果横向滑动的距离大于纵向滑动的距离，那么允许横向滑动
                    if (Math.abs(disX) > Math.abs(currentY - lastY)*triggerXYRatio) {
                        isHorizontalScroll = true;
                    }
                    if (isHorizontalScroll) {
                        // 允许横向滑动，包含 3 种情况：在 滑动途中，
                        // 或者当前状态已经滑动到最左边而手指向右滑动，
                        // 或者是当前状态已经滑动到最右边而手指向左滑动，
                        if (getScrollX() < operateView.getWidth() && getScrollX() > 0 ||
                                getScrollX() >= operateView.getWidth() && disX > 0 ||
                                getScrollX() <= 0 && disX < 0) {
                            scrollBy((-disX), 0);
                            lastX = currentX;
                            currentScrollX += disX;
                            result = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    int scrollX = getScrollX();
                    int operateViewWidth = operateView.getWidth();
                    // 如果横向滑动的距离大于某个比例值，那么触发滑动
                    if (Math.abs(currentScrollX) > operateViewWidth * startScrollXYMinRatio) {
                        // 向左滑动，即为将 operateView 全部显示出来
                        if (currentScrollX < 0) {
                            smoothScrollToShowOperatePosition(scrollX);
                        // 向右滑动，即为恢复初始位置
                        } else {
                            smoothScrollToInitPosition();
                        }
                    // 不触发滑动，那么滑动回上一个位置
                    } else {
                        scroller.startScroll(scrollX, 0, currentScrollX, 0);
                        invalidate();
                    }
                    resetScrollInfo();
                    break;
            }
        }
        return result;
    }

    // 生成子 View 的布局参数
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(super.generateDefaultLayoutParams());
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    // SlideOperateItemView 自带的布局参数，支持 margin 属性
    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    // SlideOperateItemView 的滑动监听接口
    public interface SlideListener {
        void finishHideOperateView();
        void finishShowOperateView();
    }
}
