package com.example.flexible;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class FlexibleLayout extends LinearLayout {

    Scroller scroller;
    GestureDetector gestureDetector;

    // constants
    private static final int FLING_VELOCITY_SLOP = 80;
    private static final int MAX_SCROLL_DURATION = 400;
    private static final int MIN_SCROLL_DURATION = 100;
    private static final int DRAG_SPEED_SLOP = 30;
    private static final float DRAG_SPEED_MULTIPLIER = 1.2F;
    private static final float SCROLL_TO_CLOSE_OFFSET_FACTOR = 0.5f;
    private static final float SCROLL_TO_EXTEND_OFFSET_FACTOR = 0.8f;
    private final int TOUCH_SLOP = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private int closeOffset = 0;
    private int openOffset = 0;
    private int extendOffset = 0;
    private int extraContentHeight = 0;
    private float lastY;

    private InnerStatus currInnerStatus = InnerStatus.OPENED;
    private Status lastStatus = Status.EXTEND;

    private boolean isAllowOpen = false;
    private boolean isAllowExtend = false;

    /**
     * FlexibleLayout上层需要额外移动的高度（如：Title、StatusBar、浮于FlexibleLayout之上的布局）
     *
     * @param extra 需要额外移动的高度
     */
    public void setExtraContentHeight(int extra) {
        extraContentHeight = extra;
    }

    /**
     * ScrollLayout的状态
     */
    public enum Status {
        EXTEND,
        OPEN,
        CLOSE
    }

    /**
     * 内部状态
     */
    private enum InnerStatus {
        EXTENDED,
        OPENED,
        CLOSED,
        MOVING,
        SCROLLING
    }

    public FlexibleLayout(@NonNull Context context) {
        this(context, null);
    }

    public FlexibleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlexibleLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    /**
     * 滚动手势监听
     */
    private final GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (velocityY > FLING_VELOCITY_SLOP) {

                if (lastStatus.equals(Status.OPEN) && -getScrollY() > openOffset) {
                    scrollToClose();
                } else if (lastStatus.equals(Status.EXTEND) && -getScrollY() > extendOffset) {
                    scrollToOpen();
                }

                return true;

            } else if (velocityY < FLING_VELOCITY_SLOP && getScrollY() <= -openOffset) {

                scrollToOpen();
                return true;

            } else if (velocityY < FLING_VELOCITY_SLOP && getScrollY() > -openOffset) {

                scrollToExtend();
                return true;

            }

            return false;
        }
    };

    /**
     * 滚动为展开状态
     */
    private void scrollToExtend() {
        if (!isAllowExtend) return;

        if (currInnerStatus == InnerStatus.EXTENDED) return;

        if (openOffset == extendOffset) return;

        int dy = -getScrollY() - extendOffset;
        if (dy == 0) return;

        currInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (openOffset - extendOffset));

        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        lastStatus = Status.EXTEND;

        invalidate();
    }

    /**
     * 滚动为打开状态
     */
    private void scrollToOpen() {
        if (!isAllowOpen) return;

        if (currInnerStatus == InnerStatus.OPENED) return;

        if (openOffset == extendOffset) return;

        int dy = -getScrollY() - openOffset;
        if (dy == 0) return;

        currInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (openOffset - extendOffset));

        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        lastStatus = Status.OPEN;

        invalidate();
    }

    /**
     * 滚动为关闭状态
     */
    private void scrollToClose() {
        if (currInnerStatus == InnerStatus.CLOSED) return;

        if (closeOffset == openOffset) return;

        int dy = -getScrollY() - closeOffset;
        if (dy == 0) return;

        currInnerStatus = InnerStatus.SCROLLING;
        int duration = MIN_SCROLL_DURATION
                + Math.abs((MAX_SCROLL_DURATION - MIN_SCROLL_DURATION) * dy / (closeOffset - openOffset));

        scroller.startScroll(0, getScrollY(), 0, dy, duration);
        lastStatus = Status.CLOSE;

        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                if (lastStatus == Status.OPEN
                        && ev.getY() < openOffset)
                    return false;
                if (lastStatus == Status.EXTEND
                        && ev.getY() < extendOffset
                        && getChildCount() > 1)
                    return false;
                break;
            case MotionEvent.ACTION_MOVE:
                // 对layout范围内的触摸事件进行拦截，否则clickable的控件会消费触摸
                if (lastStatus == Status.OPEN
                        && Math.abs(ev.getY() - lastY) > TOUCH_SLOP) {
                    return true;
                }

                if (lastStatus == Status.EXTEND
                        && Math.abs(ev.getY() - lastY) > TOUCH_SLOP) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(ev.getY() - lastY) < TOUCH_SLOP)
                    return false;
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        gestureDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (lastStatus == Status.CLOSE
                        && event.getY() < closeOffset) {
                    return false;
                } else if (lastStatus == Status.OPEN
                        && event.getY() < openOffset) {
                    return false;
                } else if (lastStatus == Status.EXTEND
                        && event.getY() < extendOffset) {
                    return false;
                }

                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                int deltaY = (int) ((event.getY() - lastY) * DRAG_SPEED_MULTIPLIER);
                deltaY = (int) (Math.signum(deltaY)) * Math.min(Math.abs(deltaY), DRAG_SPEED_SLOP);

                if (lastStatus == Status.CLOSE
                        && !isAllowOpen) {
                    return false;
                }

                if (lastStatus == Status.OPEN
                        && !isAllowExtend
                        && event.getY() - lastY < 0) {
                    return false;
                }

                if (disposeEdgeValue(deltaY)) return true;

                currInnerStatus = InnerStatus.MOVING;
                int toScrollY = getScrollY() - deltaY;
                if (toScrollY >= -extendOffset) {
                    scrollTo(0, -extendOffset);
                } else {
                    scrollTo(0, toScrollY);
                }
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getScrollY() < 0
                        && Math.abs(Math.abs(getScrollY()) - Math.abs(extendOffset)) <= TOUCH_SLOP) {
                    lastStatus = Status.EXTEND;
                }

                if (currInnerStatus == InnerStatus.MOVING) {
                    completeMove();
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private OnScrollListener onScrollListener;

    public void setOnScrollListener(OnScrollListener listener) {
        this.onScrollListener = listener;
    }

    private void completeMove() {
        float closeValue = -((openOffset - extendOffset) * SCROLL_TO_CLOSE_OFFSET_FACTOR);
        if (getScrollY() > closeValue) {
            scrollToExtend();
        } else {
            float exitValue = -((closeOffset - openOffset) * SCROLL_TO_EXTEND_OFFSET_FACTOR + openOffset);
            if (getScrollY() <= closeValue && getScrollY() > exitValue) {
                scrollToOpen();
            } else {
                scrollToClose();
            }
        }
    }

    private boolean disposeEdgeValue(int deltaY) {
        return (isAllowExtend && deltaY <= 0 && getScrollY() >= -extendOffset)
                || (!isAllowExtend && deltaY <= 0 && getScrollY() >= -openOffset - TOUCH_SLOP)
                || (deltaY >= 0 && getScrollY() <= -closeOffset);
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int currY = scroller.getCurrY();
            scrollTo(0, currY);
            if (currY == -extendOffset || currY == -openOffset || currY == -closeOffset) {
                scroller.abortAnimation();
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        if (openOffset == extendOffset) {
            return;
        }

        if (-y <= openOffset) {
            float progress = (float) (-y - extendOffset) / (openOffset - extendOffset);
            onScrollProgressChanged(progress);
        } else {
            float progress = (float) (-y - openOffset) / (openOffset - closeOffset);
            onScrollProgressChanged(progress);
        }

        if (y == -extendOffset) {
            if (currInnerStatus != InnerStatus.EXTENDED) {
                currInnerStatus = InnerStatus.EXTENDED;
                onScrollFinished(Status.EXTEND);
            }
        } else if (y == -openOffset) {
            if (currInnerStatus != InnerStatus.OPENED) {
                currInnerStatus = InnerStatus.OPENED;
                onScrollFinished(Status.OPEN);
            }
        } else if (y == -closeOffset) {
            if (currInnerStatus != InnerStatus.CLOSED) {
                currInnerStatus = InnerStatus.CLOSED;
                onScrollFinished(Status.CLOSE);
            }
        }
    }

    private void onScrollFinished(Status status) {
        if (onScrollListener != null) {
            onScrollListener.onScrollFinished(status);
        }
    }

    private void onScrollProgressChanged(float progress) {
        if (onScrollListener != null) {
            onScrollListener.onScrollProgressChanged(progress);
        }
    }

    public void reset() {
        setToClose();
        isAllowExtend = false;
        isAllowOpen = false;
    }

    public void setToClose() {
        scrollTo(0, -closeOffset);
        currInnerStatus = InnerStatus.CLOSED;
        lastStatus = Status.CLOSE;
    }

    public void setToOpen() {
        scrollTo(0, -openOffset);
        currInnerStatus = InnerStatus.OPENED;
        lastStatus = Status.OPEN;
    }

    public void setAllowOpen(boolean canOpen) {
        this.isAllowOpen = canOpen;
    }

    public void setAllowExtend(boolean canExtend) {
        this.isAllowExtend = canExtend;
    }

    public void setExtendOffset(int offset) {
        extendOffset = getScreenHeight() - offset - extraContentHeight;
    }

    public void setOpenOffset(int offset) {
        openOffset = getScreenHeight() - offset - extraContentHeight;
    }

    public void setCloseOffset(int offset) {
        closeOffset = getScreenHeight() - offset - extraContentHeight;
    }

    public int getScreenHeight() {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int result = 0;
        int resourceId = getContext().getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getContext().getResources().getDimensionPixelSize(resourceId);
        }

        return dm.heightPixels - result;
    }

    /**
     * ScrollLayout滚动监听
     */
    public interface OnScrollListener {
        /**
         * 每次滚动改变
         * 0：extend  1：open  -1: close
         */
        void onScrollProgressChanged(float currProgress);

        /**
         * 滚动状态改变时调用
         */
        void onScrollFinished(Status currStatus);

        /**
         * 滚动子View
         */
        void onChildScroll(int top);
    }
}
