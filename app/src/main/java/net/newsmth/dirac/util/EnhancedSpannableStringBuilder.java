package net.newsmth.dirac.util;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class EnhancedSpannableStringBuilder extends SpannableStringBuilder {

    public EnhancedSpannableStringBuilder() {
        super();
    }

    public EnhancedSpannableStringBuilder(CharSequence text) {
        super(text);
    }

    @Override
    public EnhancedSpannableStringBuilder append(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        super.append(text);
        return this;
    }

    public EnhancedSpannableStringBuilder append(Object object) {
        return append(object.toString());
    }

    /**
     * @param text
     * @param size 绝对像素值
     * @return
     */
    public EnhancedSpannableStringBuilder appendWithSize(CharSequence text,
                                                         int size) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        int start = length();
        append(text);
        setSpan(new AbsoluteSizeSpan(size), start, length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return this;
    }

    public EnhancedSpannableStringBuilder appendWithColor(CharSequence text,
                                                          int color) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        int start = length();
        append(text);
        setSpan(new ForegroundColorSpan(color), start, length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return this;
    }

    public EnhancedSpannableStringBuilder appendWithBackgroundColor(CharSequence text,
                                                                    int color) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        int start = length();
        append(text);
        setSpan(new BackgroundColorSpan(color), start, length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return this;
    }

    /**
     * @param text
     * @param color
     * @param proportion 绝对像素值
     * @return
     */
    public EnhancedSpannableStringBuilder appendWithColorAndSize(
            CharSequence text, int color, int proportion) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        int start = length();
        append(text);
        setSpan(new ForegroundColorSpan(color), start, length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        setSpan(new AbsoluteSizeSpan(proportion), start, length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return this;
    }

}
