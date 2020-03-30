package net.newsmth.dirac.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.transition.TransitionValues;
import android.view.ViewGroup;

import com.facebook.drawee.drawable.ScalingUtils;

import net.newsmth.dirac.widget.FrescoPhotoView;

/**
 * Created by cameoh on 29/11/2017.
 */

public class PhotoViewTransition extends Transition {

    private static final String PROPNAME_BOUNDS = "draweeTransition:bounds";

    private final ScalingUtils.ScaleType mFromScale;
    private final ScalingUtils.ScaleType mToScale;

    private PhotoViewTransition(ScalingUtils.ScaleType fromScale, ScalingUtils.ScaleType toScale) {
        this.mFromScale = fromScale;
        this.mToScale = toScale;
    }

    public static TransitionSet createTransitionSet(
            ScalingUtils.ScaleType fromScale,
            ScalingUtils.ScaleType toScale) {
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new PhotoViewTransition(fromScale, toScale));
        return transitionSet;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public Animator createAnimator(
            ViewGroup sceneRoot,
            TransitionValues startValues,
            TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }
        Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);
        if (startBounds == null || endBounds == null) {
            return null;
        }
        if (mFromScale == mToScale) {
            return null;
        }
        final FrescoPhotoView draweeView = (FrescoPhotoView) startValues.view;
        final ScalingUtils.InterpolatingScaleType scaleType =
                new ScalingUtils.InterpolatingScaleType(mFromScale, mToScale, startBounds, endBounds);
        draweeView.draweeHolder.getHierarchy().setActualImageScaleType(scaleType);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            scaleType.setValue(fraction);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                draweeView.draweeHolder.getHierarchy().setActualImageScaleType(mToScale);
                draweeView.endAnim();
            }
        });

        return animator;
    }

    private void captureValues(TransitionValues transitionValues) {
        if (transitionValues.view instanceof FrescoPhotoView) {
            transitionValues.values.put(
                    PROPNAME_BOUNDS,
                    new Rect(0, 0, transitionValues.view.getWidth(), transitionValues.view.getHeight()));
        }
    }
}
