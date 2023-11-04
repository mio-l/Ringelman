package com.example.ringelman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * 拖动缩放矩形框
 */
public class DragScaleView extends View implements View.OnTouchListener {
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private static final int TOP = 0x15;
    private static final int LEFT = 0x16;
    private static final int BOTTOM = 0x17;
    private static final int RIGHT = 0x18;
    private static final int LEFT_TOP = 0x11;
    private static final int RIGHT_TOP = 0x12;
    private static final int LEFT_BOTTOM = 0x13;
    private static final int RIGHT_BOTTOM = 0x14;
    private static final int CENTER = 0x19;
    private int offset = 100;
    protected Paint borderPaint = new Paint();  //画边框用
    protected Paint fillPaint = new Paint();    //填充用

    //画四角图标
    private Rect leftTopRect;
    private Rect rightTopRect;
    private Rect leftBottomRect;
    private Rect rightBottomRect;
    private View view;


    /**
     * 初始化获取屏幕宽高
     */
    protected void initScreenW_H() {
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context) {
        super(context);
        setOnTouchListener(this);
        initScreenW_H();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        borderPaint.setColor(Color.RED);
        borderPaint.setStrokeWidth(4.0f);
        borderPaint.setStyle(Paint.Style.STROKE);
        //画矩形边框
        canvas.drawRect(offset, offset, getWidth() - offset, getHeight()
                - offset, borderPaint);

        //矩形填充
        //主要防止第一次进入没有触碰view就获取坐标位置报null问题
        if (view == null) {
            view = this;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //左上角图标
        leftTopRect = new Rect(0, 0, 2 * offset, 2 * offset);
        //右上角图标
        rightTopRect = new Rect(getWidth() - offset * 2, 0, getWidth(), 2 * offset);
        //左下角
        leftBottomRect = new Rect(0, getHeight() - offset * 2, offset * 2, getHeight());
        //右下角
        rightBottomRect = new Rect(getWidth() - offset * 2, getHeight() - offset * 2, getWidth(), getHeight());
    }

    /**
     * 处理view事件，初始化位置
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        view = v;
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                oriLeft = v.getLeft(); // 初始化获取view左边框位置
                oriRight = v.getRight(); // 初始化获取view右边框位置
                oriTop = v.getTop();  // 初始化获取view上边框位置
                oriBottom = v.getBottom(); // 初始化获取view下边框位置
                //获取相对屏幕的X轴位置，和getX()区别在于getX()是获取相对view的X轴位置
                lastX = (int) event.getRawX(); // 初始化点击在屏幕X位置
                lastY = (int) event.getRawY(); // 初始化点击在屏幕Y位置
                //获取点击view上某个位置，例如左边框，中间填充处，或四个角等
                dragDirection = getDirection(v, (int) event.getX(),
                        (int) event.getY());
            case MotionEvent.ACTION_MOVE:
                // 计算出在屏幕移动距离，（移动的位置 - 按下时的位置）
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                switch (dragDirection) {
                    case LEFT: // 左边缘
                        left(v, dx);
                        break;
                    case RIGHT: // 右边缘
                        right(v, dx);
                        break;
                    case BOTTOM: // 下边缘
                        bottom(v, dy);
                        break;
                    case TOP: // 上边缘
                        top(v, dy);
                        break;
                    case CENTER: // 点击中心-->>移动
                        center(v, dx, dy);
                        break;
                    case LEFT_BOTTOM: // 左下
                        left(v, dx);
                        bottom(v, dy);
                        break;
                    case LEFT_TOP: // 左上
                        left(v, dx);
                        top(v, dy);
                        break;
                    case RIGHT_BOTTOM: // 右下
                        right(v, dx);
                        bottom(v, dy);
                        break;
                    case RIGHT_TOP: // 右上
                        right(v, dx);
                        top(v, dy);
                        break;
                }
                if (dragDirection != CENTER) {
                    v.layout(oriLeft, oriTop, oriRight, oriBottom);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                dragDirection = 0;
                break;
        }
        // 处理拖动事件
        invalidate();
        return true;
    }

    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */

    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dx 计算出移动后距离（x轴）
     * @param dy 计算出移动后距离（y轴）
     */
    private void center(View v, int dx, int dy) {
        // 算出各个边框最终位置
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
        // 判断移动到最左边时做响应处理
        if (left < -offset) {
            left = -offset;
            right = left + v.getWidth();
        }
        // 判断移动到最右边做响应处理
        if (right > screenWidth + offset) {
            right = screenWidth + offset;
            left = right - v.getWidth();
        }
        // 判断移动到最上边做响应处理
        if (top < -offset) {
            top = -offset;
            bottom = top + v.getHeight();
        }
        // 判断移动到最下边做响应处理
        if (bottom > screenHeight + offset) {
            bottom = screenHeight + offset;
            top = bottom - v.getHeight();
        }
        // 通过计算最终位置设置布局
        v.layout(left, top, right, bottom);
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;
        if (oriTop < -offset) {
            oriTop = -offset;
        }
        if (oriBottom - oriTop - 2 * offset < 100) {
            oriTop = oriBottom - 2 * offset - 100;
        }
    }

    /**
     * 触摸点为下边缘
     *
     * @param v
     * @param dy
     */
    private void bottom(View v, int dy) {
        oriBottom += dy;
        if (oriBottom > screenHeight + offset) {
            oriBottom = screenHeight + offset;
        }
        if (oriBottom - oriTop - 2 * offset < 100) {
            oriBottom = 100 + oriTop + 2 * offset;
        }
    }

    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
        if (oriRight > screenWidth + offset) {
            oriRight = screenWidth + offset;
        }
        if (oriRight - oriLeft - 2 * offset < 100) {
            oriRight = oriLeft + 2 * offset + 100;
        }
    }

    /**
     * 触摸点为左边缘
     *
     * @param v
     * @param dx 计算出移动后距离（x轴）
     */
    private void left(View v, int dx) {
        // 算出最终位置（left边框最终位置）
        oriLeft += dx;
        if (oriLeft < -offset) {
            oriLeft = -offset;
        }
        if (oriRight - oriLeft - 2 * offset < 100) {
            oriLeft = oriRight - 2 * offset - 100;
        }
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x 点击x轴位置，是在view上x轴位置
     * @param y 点击y轴位置，是在view上y轴位置
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        int left = v.getLeft();
        int right = v.getRight();
        int bottom = v.getBottom();
        int top = v.getTop();
        //判断当前点击的位置，并返回对应位置
        if (x < 50 && y < 50) {
            //如果当前触摸在view身上的位置X，Y轴都小于50那此时点击在view位置就是在左上角的范围
            return LEFT_TOP;
        }
        if (y < 50 && right - left - x < 50) {
            return RIGHT_TOP;
        }
        if (x < 50 && bottom - top - y < 50) {
            return LEFT_BOTTOM;
        }
        if (right - left - x < 50 && bottom - top - y < 50) {
            return RIGHT_BOTTOM;
        }
        if (x < 50) {
            return LEFT;
        }
        if (y < 50) {
            return TOP;
        }
        if (right - left - x < 50) {
            return RIGHT;
        }
        if (bottom - top - y < 50) {
            return BOTTOM;
        }
        return CENTER;
    }

    /**
     * 获取截取宽度
     *
     * @return
     */
    public int getCutWidth() {
        return getWidth() - 2 * offset;
    }

    /**
     * 获取截取高度
     *
     * @return
     */
    public int getCutHeight() {
        return getHeight() - 2 * offset;
    }


    /**
     * 获取左上角坐标位置
     */
    public int[] getLeftTopLocation() {
        if (view != null) {
            int x = view.getLeft() + offset;
            int y = view.getTop() + offset;
            Log.e("DragScaleView", "getLeftTopLocation: " + x + "\n" + y);
            return new int[]{x, y};
        } else {
            return null;
        }
    }

    /**
     * 获取右上角坐标位置
     */
    public int[] getRightTopLocation() {
        if (view != null) {
            int x = view.getLeft() + getWidth() - offset;
            int y = view.getTop() + offset;
            Log.e("DragScaleView", "getLeftTopLocation: " + x + "\n" + y);
            return new int[]{x, y};
        } else {
            return null;
        }
    }

    public int getTopline(){
        if(view!=null){
            int x=view.getTop()+offset;
            return x;
        }
        else {
            return 0;
        }
    }public int getLeftline(){
        if(view!=null){
            int x=view.getLeft()+offset;
            return x;
        }
        else {
            return 0;
        }
    }
    /**
     * 获取左下角坐标位置
     */
    public int[] getLeftBottomLocation() {
        if (view != null) {
            int x = view.getLeft() + offset;
            int y = view.getTop() + getHeight() - offset;
            return new int[]{x, y};
        } else {
            return null;
        }
    }

    /**
     * 获取右下角坐标位置
     */
    public int[] getRightBottomLocation() {
        if (view != null) {
            int x = view.getLeft() + getWidth() - offset;
            int y = view.getTop() + getHeight() - offset;
            return new int[]{x, y};
        } else {
            return null;
        }
    }

    /**
     * 返回view是否为空，确认当前view是否初始化
     * @return
     */
    public boolean isNotNullView() {
        if (view != null) {
            return true;
        } else {
            return false;
        }
    }

}