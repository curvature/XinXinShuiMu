package net.newsmth.dirac.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import net.newsmth.dirac.R;

/**
 * Created by cameoh on 21/12/2017.
 */

public class KeepPressedRelativeLayout extends RelativeLayout {
    {
        int[] attrs = {R.attr.colorControlHighlight};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        int colorControlHighlight = ta.getColor(0, Color.TRANSPARENT);
        ta.recycle();

        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(colorControlHighlight));
        res.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        setBackground(res);
    }

    public KeepPressedRelativeLayout(Context context) {
        super(context);
    }

    public KeepPressedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            setPressed(false);
        } else if (isPressed()) {
            super.onWindowFocusChanged(hasWindowFocus);
            setPressed(true);
        } else {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }
}
