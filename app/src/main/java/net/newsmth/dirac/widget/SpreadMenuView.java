package net.newsmth.dirac.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.ThreadAdapter;

public class SpreadMenuView extends FrameLayout implements GestureDetector.OnGestureListener, Runnable {

    private final int width;
    private final float leftMargin;
    private SpreadMenuCallback callback;
    private GestureDetector gestureDetector;
    private boolean viewAdded;
    private boolean disallowInterceptRequested;
    private RecyclerView recyclerView;
    private View scrim;
    private ImageView pivot;
    private ImageView deleteButton;
    private ImageView replyButton;
    private ImageView shareButton;
    private int lastPos;
    private boolean upReady;
    private Rect hitRect;

    public SpreadMenuView(Context context) {
        this(context, null);
    }

    public SpreadMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, this);
        width = getResources().getDimensionPixelSize(R.dimen.spread_width);
        leftMargin = getResources().getDimension(R.dimen.left_margin);
    }

    public void setMenuCallback(SpreadMenuCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        recyclerView = findViewById(R.id.recycler);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (!isEnabled()) {
            return false;
        }
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            disallowInterceptRequested = false;
            upReady = false;
        }
        if (viewAdded) {
            MotionEvent cancel = MotionEvent.obtain(e);
            cancel.setAction(MotionEvent.ACTION_CANCEL);
            gestureDetector.onTouchEvent(cancel);
            if (e.getActionMasked() == MotionEvent.ACTION_UP) {
                /**
                 *  查阅View源码确认，只有在onInterceptTouchEvent返回true之后的事件，才会进onTouchEvent
                 *
                 *  如果up的时候返回true，并不会调用自己的onTouchEvent，手动调用
                 */
                onTouchEvent(e);
            }
            return true;
        }
        gestureDetector.onTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) {
            return false;
        }
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (upReady) {
                    if (hitBy(e, replyButton)) {
                        replyButton.setPressed(true);
                        replyButton.setColorFilter(Color.WHITE);
                        shareButton.setPressed(false);
                        shareButton.clearColorFilter();
                    } else if (hitBy(e, shareButton)) {
                        shareButton.setPressed(true);
                        shareButton.setColorFilter(Color.WHITE);
                        replyButton.setPressed(false);
                        replyButton.clearColorFilter();
                    } else {
                        shareButton.setPressed(false);
                        shareButton.clearColorFilter();
                        replyButton.setPressed(false);
                        replyButton.clearColorFilter();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                replyButton.setPressed(false);
                replyButton.clearColorFilter();
                shareButton.setPressed(false);
                shareButton.clearColorFilter();
                if (upReady && callback != null) {
                    if (hitBy(e, replyButton)) {
                        callback.onMenuItemSelected(0, lastPos);
                    } else if (hitBy(e, shareButton)) {
                        callback.onMenuItemSelected(1, lastPos);
                    }
                }
                if (viewAdded) {
                    removeView(scrim);
                    removeView(pivot);
                    //removeView(deleteButton);
                    removeView(replyButton);
                    removeView(shareButton);
                    viewAdded = false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    private boolean hitBy(MotionEvent event, View view) {
        if (hitRect == null) {
            hitRect = new Rect();
        }
        view.getHitRect(hitRect);
        return hitRect.contains((int) event.getX(), (int) event.getY());
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        disallowInterceptRequested = disallowIntercept;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (disallowInterceptRequested) {
            return;
        }
        viewAdded = spreadMenu(e.getRawX(), e.getRawY());
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private boolean spreadMenu(float x, float y) {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        x -= loc[0];
        y -= loc[1];
        float rightShiftedX = x;
        if (x <= leftMargin) {
            rightShiftedX = leftMargin + 1;
        }
        int pos = recyclerView.getChildAdapterPosition(recyclerView.findChildViewUnder(rightShiftedX, y));
        if (pos == RecyclerView.NO_POSITION
                || ((ThreadAdapter) recyclerView.getAdapter()).isNotLoadedPage(pos)) {
            return false;
        }
        lastPos = pos;
        if (scrim == null) {
            scrim = new View(getContext());
            scrim.setBackgroundColor(Color.BLACK);
        }
        scrim.setAlpha(0);
        addView(scrim);
        scrim.animate().alpha(0.5f);

        if (pivot == null) {
            pivot = new ImageView(getContext());
            pivot.setImageResource(R.drawable.ring);
        }
        addView(pivot, width, width);
        boolean right = x > getWidth() / 2;
        x -= width / 2;
        y -= width / 2;
        pivot.setTranslationX(x);
        pivot.setTranslationY(y);

        //addDelete(x, y, (int) (-width * 1.286f), (int) (-width * 1.5321f));
        addReply(x, y, 0, -width * 2);
        if (right) {
            addShare(x, y, (int) (-width * 1.286f), (int) (-width * 1.5321f));
        } else {
            addShare(x, y, (int) (width * 1.286f), (int) (-width * 1.5321f));
        }

        return true;
    }

    private void addDelete(float left, float top, int shiftX, int shiftY) {
        if (deleteButton == null) {
            deleteButton = new ImageView(getContext());
            deleteButton.setImageResource(R.drawable.ic_delete_black_24dp);
            deleteButton.setBackgroundResource(R.drawable.circle);
            deleteButton.setScaleType(ImageView.ScaleType.CENTER);
            deleteButton.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        addView(deleteButton, width, width);
        deleteButton.setTranslationX(left);
        deleteButton.setTranslationY(top);
        deleteButton.animate().xBy(shiftX).yBy(shiftY);
    }

    private void addReply(float left, float top, int shiftX, int shiftY) {
        if (replyButton == null) {
            replyButton = new ImageView(getContext());
            replyButton.setImageResource(R.drawable.ic_reply_black_24dp);
            replyButton.setBackgroundResource(R.drawable.circle);
            replyButton.setScaleType(ImageView.ScaleType.CENTER);
            replyButton.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        addView(replyButton, width, width);
        replyButton.setTranslationX(left);
        replyButton.setTranslationY(top);
        replyButton.animate().xBy(shiftX).yBy(shiftY);
    }

    private void addShare(float left, float top, int shiftX, int shiftY) {
        if (shareButton == null) {
            shareButton = new ImageView(getContext());
            shareButton.setImageResource(R.drawable.ic_share_black_24dp);
            shareButton.setBackgroundResource(R.drawable.circle);
            shareButton.setScaleType(ImageView.ScaleType.CENTER);
            shareButton.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
        }
        addView(shareButton, width, width);
        shareButton.setTranslationX(left);
        shareButton.setTranslationY(top);
        shareButton.animate().xBy(shiftX).yBy(shiftY).withEndAction(this);
    }

    @Override
    public void run() {
        upReady = true;
    }

    public interface SpreadMenuCallback {
        void onMenuItemSelected(int menuPos, int pos);
    }
}

