package net.newsmth.dirac.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by cameoh on 4/23/16.
 */
public class Typewriter extends TextView implements Runnable {

    private static final int INTERVAL = 250;
    private int mLength;
    private int mCur;
    private SpannableString mSpan;
    private ForegroundColorSpan mColor;
    private ForegroundColorSpan mEmpty;
    private Runnable mEndAction;

    public Typewriter(Context context) {
        super(context);
    }

    public Typewriter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void type(String text) {
        mCur = 0;
        mLength = text.length();
        mColor = new ForegroundColorSpan(getCurrentTextColor());
        mEmpty = new ForegroundColorSpan(Color.TRANSPARENT);
        mSpan = new SpannableString(text);
        mSpan.setSpan(mEmpty, 0, mLength, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        setText(mSpan);
        postDelayed(this, INTERVAL);
    }

    public void withEndAction(Runnable runnable) {
        mEndAction = runnable;
    }

    @Override
    public void run() {
        ++mCur;
        if (mCur <= mLength) {
            mSpan.removeSpan(mEmpty);
            mSpan.removeSpan(mColor);
            mSpan.setSpan(mColor, 0, mCur, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            mSpan.setSpan(mEmpty, mCur, mLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            setText(mSpan);
            postDelayed(this, INTERVAL);
        } else if (mEndAction != null) {
            mEndAction.run();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = w;
            lp.height = h;
            setLayoutParams(lp);
        }
    }
}
