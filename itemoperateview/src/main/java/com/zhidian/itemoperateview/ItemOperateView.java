package com.zhidian.itemoperateview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.PopupWindowCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.PopupWindow;

import java.util.Arrays;

/**
 * 使用 OperateItemPopupWindow 来以弹窗的形式显示对某个 View 的操作选项，
 * contentView 是 popupWindow 中显示的内容 View，对应的触摸事件由调用者自由添加：
 * OperateItemPopupWindow popupWindow = new OperateItemPopupWindow(contentView);
 * popupWindow.setTitleBar(toolbar); // 可选，主要目的是为了防止 popupWindow 显示在 toolbar 上面
 * popupWindow.show(anchorView); // 显示，并附属在一个 view 上面
 * 在 Activity 摧毁的时候记得调用 popupWindow.dismiss() 方法，以防止窗体泄露:
 * if (popupWindow != null && popupWindow.isShowing()) {
 * popupWindow.dismiss();
 * }
 * popupWindow = null;
 * 显示流程见 {@link #show(View)}
 */
public class ItemOperateView extends PopupWindow {
    private static final String TAG = "ItemOperateView";
    private static final boolean DEBUG = true;

    private Context context;
    // titleBar 的高度、相对于屏幕左上角的 x 坐标和 y 坐标
    private int titleBarHeight;
    private int[] titleBarCoordinate = new int[2];
    // 显示在描点 View 上下两侧和默认的 contentView
    private View contentViewAsUp;
    private View contentViewAsDown;
    private View defaultContentView;
    // OperateItemPopupWindow 显示的 contentView 的宽高尺寸信息：
    private int[] contentViewAsUpSize = new int[2];
    private int[] contentViewAsDownSize = new int[2];
    private int[] defaultContentViewSize = new int[2];
    private View nextShowView; // 下一次要显示的 view （在三个 view 之间）
    private int[] nextShowViewSize; // 下一次要显示的 view 的尺寸
    private boolean isNextShowUp = true; // 下一次显示是否显示在 anchorView 上面
    private static int SCREEN_WIDTH; // 屏幕宽高
    private static int SCREEN_HEIGHT;

    // 创建一个 popupWindow， contentView 为当前 Window 默认显示的内容 View
    public ItemOperateView(@NonNull View contentView) {
        if (contentView == null) {
            if (DEBUG) {
                throw new IllegalArgumentException(TAG + "argument contentView can not be null!");
            } else {
                Log.e(TAG, "Argument contentView should not be null");
            }
        }
        ViewGroup.LayoutParams lp = contentView.getLayoutParams();
        if (lp == null) {
            init(contentView, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            init(contentView, lp.width, lp.height);
        }
    }

    // 创建一个 popupWindow， contentView 为当前 Window 默认显示的内容 View
    public ItemOperateView(View contentView, int width, int height) {
        super(contentView, width, height, true);
        init(contentView, width, height);
    }

    // 创建一个 popupWindow， contentView 为当前 Window 默认显示的内容 View 的 layout id
    public ItemOperateView(@NonNull Context context, int layoutId) {
        super(context);
        if (context == null) {
            throw new IllegalArgumentException(TAG + "argument context can not null!");
        }
        this.context = context;
        View contentView = LayoutInflater.from(context).inflate(layoutId, null);
        init(contentView, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
    }

    protected void init(@NonNull View contentView, int width, int height) {
        if (context == null) {
            context = contentView.getContext();
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        SCREEN_WIDTH = metrics.widthPixels;
        SCREEN_HEIGHT = metrics.heightPixels;
        setWidth(width);
        setHeight(height);
        setOutsideTouchable(true);
        // 设置背景，这句一定要有，不然显示会有问题，具体原因暂时不详
        setBackgroundDrawable(new ColorDrawable());
        setDefaultContentView(contentView);
    }

    // 设置使用 OperateItemPopupWindow 的在的 Activity 的标题栏,
    // 防止当描点 View 接近标题栏时 popupWindow 显示在标题栏上
    public void setTitleBar(@Nullable final View titleBar) {
        if (titleBar == null) {
            titleBarCoordinate[0] = titleBarCoordinate[1] = titleBarHeight = 0;
            return;
        }
        // 为了防止该方法调用的时候 View 的绘制流程还没走完，取到 0 值
        // 在这里通过 titleBar 的 post 方法提交一个 Runnable 到主线程消息队列末尾，
        // 执行这个 Runnable 的时候整个 Window 的 View 树已经绘制完成
        titleBar.post(new Runnable() {
            @Override
            public void run() {
                // 获取 titleBar 在屏幕中的坐标
                titleBar.getLocationOnScreen(titleBarCoordinate);
                // 获取标题栏的高度
                titleBarHeight = titleBar.getHeight();
                Log.d(TAG, "titleBar position、height: " +
                        titleBarCoordinate[0] + ", " + titleBarCoordinate[1] + ", " + titleBarHeight);
            }
        });
    }

    // 设置显示在 anchorView 上面的 contentView
    public void setContentViewAsUp(@Nullable View contentView) {
        // 更新其尺寸信息
        if ((contentViewAsUp = contentView) != null) {
            getContentViewSize(contentViewAsUp, contentViewAsUpSize);
        }
    }

    // 设置显示在 anchorView 下面的 contentView
    public void setContentViewAsDown(@Nullable View contentView) {
        // 更新其尺寸信息
        if ((contentViewAsDown = contentView) != null) {
            getContentViewSize(contentViewAsDown, contentViewAsDownSize);
        }
    }

    // 设置当前 popupWindow 默认显示的 contentView
    public void setDefaultContentView(@Nullable View contentView) {
        super.setContentView(contentView);
        // 设置 contentView 之后更新其尺寸信息
        if ((defaultContentView = contentView) != null) {
            getContentViewSize(defaultContentView, defaultContentViewSize);
        }
    }

    // 设置当前要显示的 contentView（不更新尺寸）
    private void setCurrentContentViewWithoutMeasure(@Nullable View contentView) {
        super.setContentView(contentView);
    }

    // 获取 contentView 的尺寸（宽高）
    public static void getContentViewSize(@NonNull View contentView, @NonNull final int[] size) {
        if (size == null || size.length < 2) {
            throw new IllegalStateException(TAG + "argument size can not be null!");
        }
        if (contentView != null) {
            // 一种获取 View 宽高的方法：使用 UNSPECIFIED 模式和 measure 方法测量 View
            int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            contentView.measure(widthSpec, widthSpec);
            size[0] = contentView.getMeasuredWidth();
            size[1] = contentView.getMeasuredHeight();
            Log.d(TAG, "getContentViewSize invoke, got size: " +
                    contentView + size[0] + ", " + size[1]);
        }
    }

    private boolean judgeShowOnUp(View showView, int anchorViewY, int showViewHeight, int minY) {
        if (showView == null) {
            return false;
        }
        return anchorViewY - showViewHeight >= minY;
    }

    private boolean judgeShowOnDown(View showView, int anchorViewY,
                                    int anchorViewHeight, int showViewHeight, int maxY) {
        if (showView == null) {
            return false;
        }
        return maxY >= anchorViewY + anchorViewHeight + showViewHeight;
    }

    private void getNextShowView(int[] anchorViewLocation, int anchorViewHeight, int minY, int maxY) {
        // 默认先尝试显示在 anchorView 上面
        if (judgeShowOnUp(contentViewAsUp, anchorViewLocation[1], contentViewAsUpSize[1], minY)) {
            nextShowView = contentViewAsUp;
            nextShowViewSize = contentViewAsUpSize;
            isNextShowUp = true;
        }
        if (nextShowView == null && judgeShowOnUp(
                defaultContentView, anchorViewLocation[1], defaultContentViewSize[1], minY)) {
            nextShowView = defaultContentView;
            nextShowViewSize = defaultContentViewSize;
            isNextShowUp = true;
        }
        // 如果 nextShowView 还是为 null，尝试显示在 anchorView 下面
        if (nextShowView == null && judgeShowOnDown(contentViewAsDown,
                anchorViewLocation[1], anchorViewHeight, contentViewAsDownSize[1], maxY)) {
            nextShowView = contentViewAsDown;
            nextShowViewSize = contentViewAsDownSize;
            isNextShowUp = false;
        }
        if (nextShowView == null && judgeShowOnDown(defaultContentView,
                anchorViewLocation[1], anchorViewHeight, defaultContentViewSize[1], maxY)) {
            nextShowView = defaultContentView;
            nextShowViewSize = defaultContentViewSize;
            isNextShowUp = false;
        }
    }

    // 计算 x y 坐标的偏移
    private int[] calculateOffset(int anchorViewWidth, int anchorViewHeight, int minX, int maxX,
                                  int extraX, int extraY) {
        // 水平居中显示
        int xOffset = (anchorViewWidth - nextShowViewSize[0]) / 2 + extraX;
        // 防止水平方向偏移越界
        xOffset = (xOffset < minX ? minX : xOffset) >
                maxX - nextShowViewSize[0] ? maxX - nextShowViewSize[0] : xOffset;
        // y 方向偏移
        int yOffset = extraY - anchorViewHeight;
        // 如果显示在 anchorView 上面，重新计算 y 方向偏移量
        if (isNextShowUp) {
            if (DEBUG) {
                Log.d(TAG, "content view 应该显示在上面");
            }
            yOffset = -anchorViewHeight - nextShowViewSize[1] + extraY;
        } else if (DEBUG) {
            Log.d(TAG, "content view 应该显示在下面");
        }
        return new int[]{xOffset, yOffset};
    }

    /**
     * @param nowOffsetX 当前 x 方向的偏移值，当前为使得 curShowView 相对 anchorView 居中的偏移值
     * @param curShowView 当前显示的 contentView
     * @param curShowViewSize 当前显示的 contentView 的尺寸(width, height)
     * @param anchorView 弹出 view 的依附 view
     * @param anchorViewLocation anchorView 在其依附的 Window 中的坐标（x, y）
     * @param boundaryCoordinate 当前的坐标值边界数组（minX, maxX, minY, maxY）
     * @param isShowUp 是否显示在 anchorView 上方
     * @return 最终的 x 方向的偏移值
     */
    protected int getOffsetX(int nowOffsetX, View curShowView, int[] curShowViewSize,
                             View anchorView, int[] anchorViewLocation,
                             int[] boundaryCoordinate, boolean isShowUp) {
        return nowOffsetX;
    }

    /**
     * @param nowOffsetY 当前 y 方向的偏移值，如果显示在 anchorView 下方（isShowUp为 false），则为 0，
     *                   如果显示在 anchorView 上方，则为 (anchorView.height + curShowView.height)
     * @param curShowView 当前显示的 contentView
     * @param curShowViewSize 当前显示的 contentView 的尺寸(width, height)
     * @param anchorView 弹出 view 的依附 view
     * @param anchorViewLocation anchorView 在其依附的 Window 中的坐标（x, y）
     * @param boundaryCoordinate 当前的坐标值边界数组（minX, maxX, minY, maxY）
     * @param isShowUp 是否显示在 anchorView 上方
     * @return 最终 y 方向的偏移值
     */
    protected int getOffsetY(int nowOffsetY, View curShowView, int[] curShowViewSize,
                             View anchorView, int[] anchorViewLocation,
                             int[] boundaryCoordinate, boolean isShowUp) {
        return nowOffsetY;
    }

    /**
     * 显示当前 popupWindow，默认将 contentView 显示在描点 View 的上面
     * 要显示的 contentView 判断流程：
     * 显示在上面：contentViewAsUp -> defaultContentView；
     * 显示在下面：contentViewAsDown -> defaultContentView
     * contentViewAsUp 只能用于显示在 anchorView 上方
     * contentViewAsDown 只能用于显示在 anchorView 下方
     * defaultContentView 可以用于 anchorView 上下显示
     *
     * @param anchorView popupWindow 显示依附的描点 View
     */
    public void show(@NonNull View anchorView) {
        show(anchorView, 0, 0);
    }

    /**
     * 显示当前 popupWindow ，流程和上面相同，但是显示在 anchorView 中指定的高度
     * @param anchorView
     * @param height
     */
    public void show(@NonNull View anchorView, int height) {
        show(anchorView, 0, height);
    }

    public void show(@NonNull View anchorView, int extraX, int extraY) {
        if (anchorView == null) {
            throw new IllegalArgumentException(TAG + "argument anchorView can not be null!");
        }
        nextShowView = null;
        // 三个 contentView 都为 null，则直接返回
        if (contentViewAsUp == null && contentViewAsDown == null && defaultContentView == null) {
            Log.e(TAG, "show method: there is no one view can be shown!");
            return;
        }

        int[] boundaryCoordinate = new int[4]; // minX, maxX, minY, maxY; 4 个临界坐标值
        // 获取当前依附 view 在屏幕中的位置和宽度
        int anchorViewWidth = anchorView.getWidth();
        int anchorViewHeight = anchorView.getHeight();
        int[] anchorViewLocation = new int[2];
        ViewParent parent = anchorView.getParent();
        // 确保 Window 不会显示在父容器外面
        if (parent instanceof ViewGroup) {
            int[] parentLocation = new int[2];
            int parentWidth, parentHeight;
            ViewGroup parentView = (ViewGroup) parent;
            // 获取 anchorView 和 parent 在 Window 中的位置
            anchorView.getLocationInWindow(anchorViewLocation);
            parentView.getLocationInWindow(parentLocation);
            parentWidth = parentView.getWidth();
            parentHeight = parentView.getHeight();
            boundaryCoordinate[0] = parentLocation[0];
            boundaryCoordinate[1] = parentLocation[0] + parentWidth;
            boundaryCoordinate[2] = parentLocation[1];
            boundaryCoordinate[3] = parentLocation[1] + parentHeight;
        } else { // anchorView 没有父容器，那么确保其不会显示在屏幕外面（比较少见）
            Log.w(TAG, "show method: anchorView don't has parent container");
            boundaryCoordinate[0] = boundaryCoordinate[2] = 0;
            boundaryCoordinate[1] = SCREEN_WIDTH;
            boundaryCoordinate[2] = SCREEN_HEIGHT;
            // 获取 anchorView 在屏幕中的位置
            anchorView.getLocationOnScreen(anchorViewLocation);
        }
        // 获取要显示的 contentView
        getNextShowView(anchorViewLocation, anchorViewHeight, boundaryCoordinate[2], boundaryCoordinate[3]);
        if (nextShowView == null || nextShowViewSize == null) {
            Log.e(TAG, "没有 contentView 可展示, 请确认 contentView 高度是否过高");
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "anchorView position、 width、height: " +
                    anchorViewLocation[0] + ", " + anchorViewLocation[1] + ", " +
                    anchorViewWidth + ", " + anchorViewHeight);
            Log.d(TAG, "curContentViewSize: " + nextShowViewSize[0] + ", " + nextShowViewSize[1]);
            Log.d(TAG, "boundary position: " + Arrays.toString(boundaryCoordinate));
        }
        if (getContentView() != nextShowView) {
            setCurrentContentViewWithoutMeasure(nextShowView);
        }

        int[] offset = calculateOffset(anchorViewWidth, anchorViewHeight,
                boundaryCoordinate[0], boundaryCoordinate[1], extraX, extraY);
        offset[0] = getOffsetX(offset[0], nextShowView, nextShowViewSize, anchorView,
                anchorViewLocation, boundaryCoordinate, isNextShowUp);
        offset[1] = getOffsetY(offset[1], nextShowView, nextShowViewSize, anchorView,
                anchorViewLocation, boundaryCoordinate, isNextShowUp);
        PopupWindowCompat.showAsDropDown(this, anchorView, offset[0], offset[1], Gravity.START);
    }
}

