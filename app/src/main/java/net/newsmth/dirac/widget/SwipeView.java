package net.newsmth.dirac.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.customview.widget.ViewDragHelper;

import net.newsmth.dirac.R;

public class SwipeView extends ViewGroup {

    private final ViewDragHelper mViewDragHelper;
    private int mEnabledDragEdge = ViewDragHelper.EDGE_LEFT;
    private int mDragEdge;
    private int mVerticalDragRange;
    private int mHorizontalDragRange;
    private int mDraggingOffset;
    private float mVerticalFinishAnchor;
    private float mHorizontalFinishAnchor;
    private float mInitialX;
    private float mInitialY;
    private boolean mJustReceivedDown;

    public SwipeView(Context context) {
        this(context, null);
    }

    public SwipeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelperCallBack());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getChildAt(0).setElevation(getResources().getDimension(R.dimen.elevation));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mVerticalDragRange = h;
        mHorizontalDragRange = w;
        mVerticalFinishAnchor = mVerticalFinishAnchor > 0 ?
                mVerticalFinishAnchor : mVerticalDragRange * 0.1f;
        mHorizontalFinishAnchor = mHorizontalFinishAnchor > 0 ?
                mHorizontalFinishAnchor : mHorizontalDragRange * 0.1f;
    }

    private int getDragRange() {
        switch (mDragEdge) {
            case ViewDragHelper.EDGE_TOP:
            case ViewDragHelper.EDGE_BOTTOM:
                return mVerticalDragRange;
            case ViewDragHelper.EDGE_LEFT:
            case ViewDragHelper.EDGE_RIGHT:
                return mHorizontalDragRange;
            default:
                return mVerticalDragRange;
        }
    }

    public void setDragEdge(int edge) {
        mEnabledDragEdge = edge;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean skipCheck = false;
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = ev.getRawX();
                mInitialY = ev.getRawY();
                mJustReceivedDown = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mJustReceivedDown) {
                    mJustReceivedDown = false;
                    final float deltaX = Math.abs(ev.getRawX() - mInitialX);
                    final float deltaY = Math.abs(ev.getRawY() - mInitialY);
                    if (Math.hypot(deltaX, deltaY) < ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                        mJustReceivedDown = true;
                        skipCheck = true;
                    } else if (deltaY > deltaX) {
                        mDragEdge = ViewDragHelper.EDGE_TOP;
                    } else {
                        mDragEdge = ViewDragHelper.EDGE_LEFT;
                    }
                }
                break;
            default:
                break;
        }
        if (mJustReceivedDown || skipCheck || (mDragEdge & mEnabledDragEdge) != 0) {
            return mViewDragHelper.shouldInterceptTouchEvent(ev);
        }
        mViewDragHelper.cancel();
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    public boolean canChildScrollUp() {
        return getChildAt(0).canScrollVertically(-1);
    }

    public boolean canChildScrollDown() {
        return getChildAt(0).canScrollVertically(1);
    }

    private boolean canChildScrollRight() {
        return getChildAt(0).canScrollHorizontally(-1);
    }

    private boolean canChildScrollLeft() {
        return getChildAt(0).canScrollHorizontally(1);
    }

    private float getFinishAnchor() {
        switch (mDragEdge) {
            case ViewDragHelper.EDGE_TOP:
            case ViewDragHelper.EDGE_BOTTOM:
                return mVerticalFinishAnchor;
            case ViewDragHelper.EDGE_LEFT:
            case ViewDragHelper.EDGE_RIGHT:
                return mHorizontalFinishAnchor;
            default:
                return mVerticalFinishAnchor;
        }
    }

    private void smoothScrollToX(int finalLeft) {
        if (mViewDragHelper.settleCapturedViewAt(finalLeft, 0)) {
            postInvalidateOnAnimation();
        }
    }

    private void smoothScrollToY(int finalTop) {
        if (mViewDragHelper.settleCapturedViewAt(0, finalTop)) {
            postInvalidateOnAnimation();
        }
    }

    private class ViewDragHelperCallBack extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return Integer.MAX_VALUE;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (mDragEdge == ViewDragHelper.EDGE_TOP && !canChildScrollUp() && top > 0) {
                return Math.min(top, mVerticalDragRange);
            } else if (mDragEdge == ViewDragHelper.EDGE_BOTTOM && !canChildScrollDown() && top < 0) {
                return Math.min(Math.max(top, -mVerticalDragRange), 0);
            }
            return 0;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mDragEdge == ViewDragHelper.EDGE_LEFT && !canChildScrollRight() && left > 0) {
                return Math.min(Math.max(left, 0), mHorizontalDragRange);
            } else if (mDragEdge == ViewDragHelper.EDGE_RIGHT && !canChildScrollLeft() && left < 0) {
                return Math.min(Math.max(left, -mHorizontalDragRange), 0);
            }
            return 0;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            switch (mDragEdge) {
                case ViewDragHelper.EDGE_LEFT:
                case ViewDragHelper.EDGE_RIGHT:
                    mDraggingOffset = Math.abs(left);
                    break;
                case ViewDragHelper.EDGE_TOP:
                case ViewDragHelper.EDGE_BOTTOM:
                    mDraggingOffset = Math.abs(top);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (mDraggingOffset == 0 || mDraggingOffset == getDragRange()) {
                return;
            }
            boolean isBack = mDraggingOffset >= getFinishAnchor();
            int finalLeft;
            int finalTop;
            switch (mDragEdge) {
                case ViewDragHelper.EDGE_LEFT:
                    finalLeft = isBack ? mHorizontalDragRange : 0;
                    smoothScrollToX(finalLeft);
                    break;
                case ViewDragHelper.EDGE_RIGHT:
                    finalLeft = isBack ? -mHorizontalDragRange : 0;
                    smoothScrollToX(finalLeft);
                    break;
                case ViewDragHelper.EDGE_TOP:
                    finalTop = isBack ? mVerticalDragRange : 0;
                    smoothScrollToY(finalTop);
                    break;
                case ViewDragHelper.EDGE_BOTTOM:
                    finalTop = isBack ? -mVerticalDragRange : 0;
                    smoothScrollToY(finalTop);
                    break;
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE && mDraggingOffset == getDragRange()) {
                Activity act = (Activity) getContext();
                act.finish();
                act.overridePendingTransition(0, 0);
            }
        }
    }

}
