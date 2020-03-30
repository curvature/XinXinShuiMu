package net.newsmth.dirac.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.ImagePagerAdapter;
import net.newsmth.dirac.util.ViewUtils;

public class ImageViewPager extends ViewPager implements Runnable {
    private final int offset;
    private final Paint mTextPaint;
    private final Paint mRectPaint;
    private final int padding;
    private final float hi;
    private final float verticalOffset;
    private boolean mDrawPageNum;
    private float len;

    {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.tertiary_text_light));
        mTextPaint.setTextSize(ViewUtils.sp2px(18));
        offset = getContext().getResources().getDimensionPixelOffset(R.dimen.bottom_padding);
        padding = getContext().getResources().getDimensionPixelOffset(R.dimen.indicator_padding);


        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setColor(ContextCompat.getColor(getContext(), android.R.color.secondary_text_light));

        hi = (mTextPaint.descent() - mTextPaint.ascent()) / 2 + padding;
        verticalOffset = (mTextPaint.descent() + mTextPaint.ascent()) / 2;
    }

    public ImageViewPager(Context context) {
        super(context);
    }

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        PagerAdapter adapter = getAdapter();
        if (adapter instanceof ImagePagerAdapter) {
            View view = ((ImagePagerAdapter) adapter).currentView;
            if (view != null) {
                return view.canScrollVertically(direction);
            }
        }
        return super.canScrollVertically(direction);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            int count = adapter.getCount();
            len = mTextPaint.measureText(count + "/" + count);
            len = len / 2 + padding;
        }
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {
        super.onPageScrolled(position, offset, offsetPixels);
        mDrawPageNum = !((ImagePagerAdapter) getAdapter()).waitingForAnim();
        removeCallbacks(this);
        postDelayed(this, 600);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDrawPageNum) {
            PagerAdapter adapter = getAdapter();
            if (adapter != null) {
                canvas.save();
                canvas.translate(getScrollX(), 0);
                canvas.drawRoundRect(getWidth() / 2 - len,
                        getHeight() - offset - hi,
                        getWidth() / 2 + len,
                        getHeight() - offset + hi,
                        padding, padding, mRectPaint);
                canvas.drawText((getCurrentItem() + 1) + "/" + adapter.getCount(),
                        getWidth() / 2, getHeight() - offset - verticalOffset, mTextPaint);
                canvas.restore();
            }
        }
    }

    @Override
    public void run() {
        mDrawPageNum = false;
        invalidate();
    }
}
