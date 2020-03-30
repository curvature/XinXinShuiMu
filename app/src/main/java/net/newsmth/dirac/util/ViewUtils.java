package net.newsmth.dirac.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import net.newsmth.dirac.Dirac;

public abstract class ViewUtils {

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, Dirac.obtain().getResources().getDisplayMetrics());
    }

    public static int sp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                dp, Dirac.obtain().getResources().getDisplayMetrics());
    }

    public static void getLocationInRootView(View view, int[] l) {
        View root = view.getRootView().findViewById(android.R.id.content);
        l[0] = view.getLeft();
        l[1] = view.getTop();
        while (view.getParent() != root) {
            l[0] += view.getLeft();
            l[1] += view.getTop();
            view = ((View) view.getParent());
        }
    }

    public static Drawable loadDrawableWithColor(Context context, @DrawableRes int res, @ColorInt int color) {
        Drawable d = AppCompatResources.getDrawable(context, res);
        d.mutate();
        DrawableCompat.setTint(d, color);
        return d;
    }
}
