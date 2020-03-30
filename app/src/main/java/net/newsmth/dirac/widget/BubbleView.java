package net.newsmth.dirac.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import net.newsmth.dirac.R;

public class BubbleView extends View implements Runnable {

    private float diffX;
    private float diffY;
    private int xLimit;
    private int yLimit;
    private Paint wavePaint;
    private float[] radius;
    private boolean dismissed;
    private float stroke;
    private float halfStroke;
    private int width;
    private int halfWidth;
    private float radiusLimit;

    public BubbleView(Context context) {
        super(context);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setStyle(Paint.Style.STROKE);
        stroke = getResources().getDimension(R.dimen.stroke);
        halfStroke = stroke / 2;
        wavePaint.setStrokeWidth(stroke);
        wavePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
        radius = new float[2];
    }

    public void dismiss() {
        ((ViewGroup) getParent()).setClipChildren(false);
        dismissed = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius[0] = 0;
        radius[1] = w / 4;
        halfWidth = w / 2;
        radiusLimit = halfWidth - stroke;
        width = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!dismissed) {
            canvas.drawCircle(halfWidth, halfWidth, halfWidth - stroke, wavePaint);
        }
        canvas.drawCircle(halfWidth, halfWidth, radius[0], wavePaint);
        canvas.drawCircle(halfWidth, halfWidth, radius[1], wavePaint);
        radius[0] += halfStroke;
        if (!dismissed && radius[0] >= radiusLimit) {
            radius[0] = 0;
        }
        radius[1] += halfStroke;
        if (!dismissed && radius[1] >= radiusLimit) {
            radius[1] = 0;
        }
        if (dismissed) {
            final int alpha = (int) ((1 - Math.max(radius[0], radius[1]) / width) * 0xff);
            if (alpha == 0) {
                post(this);
                return;
            }
            wavePaint.setAlpha(alpha);
        }
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            setTranslationX(limit(diffX + event.getRawX(), 0, xLimit));
            setTranslationY(limit(diffY + event.getRawY(), 0, yLimit));
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            diffX = getTranslationX() - event.getRawX();
            diffY = getTranslationY() - event.getRawY();
            xLimit = ((View) getParent()).getWidth() - getWidth();
            yLimit = ((View) getParent()).getHeight() - getHeight();
            return true;
        } else return event.getAction() == MotionEvent.ACTION_UP;
    }

    private float limit(float value, int low, int high) {
        if (value < low) return low;
        if (value > high) return high;
        return value;
    }

    @Override
    public void run() {
        ((ViewGroup) getParent()).removeView(this);
    }
}

