package net.newsmth.dirac.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.chrisbanes.photoview.PhotoView;

public class FrescoPhotoView extends PhotoView {

    public final DraweeHolder<GenericDraweeHierarchy> draweeHolder;
    public boolean waitForAnimEnd;
    public boolean isAnimation;
    private Bitmap mBitmap;

    {
        final GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER).build();
        draweeHolder = DraweeHolder.create(hierarchy, getContext());
    }

    public FrescoPhotoView(Context context) {
        super(context);
    }

    public FrescoPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public FrescoPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        draweeHolder.onDetach();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        draweeHolder.onAttach();
    }

    // 水平滑动冲突PhotoView内部已经处理，这里只处理y方向
    @Override
    public boolean canScrollVertically(int direction) {
        RectF rectF = getAttacher().getDisplayRect();
        if (direction > 0) {
            return rectF.bottom > getHeight();
        } else if (direction < 0) {
            return rectF.top < 0;
        }
        return false;
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable dr) {
        super.verifyDrawable(dr);
        return dr == draweeHolder.getHierarchy().getTopLevelDrawable();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        draweeHolder.onDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        draweeHolder.onAttach();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return draweeHolder.onTouchEvent(event) || super.onTouchEvent(event);
    }

    public void setImage(Uri uri) {
        final ImageRequest imageRequest =
                ImageRequestBuilder.newBuilderWithSource(uri).setResizeOptions(null)
                        .setRotationOptions(RotationOptions.autoRotate()).build();
        final ImagePipeline imagePipeline = Fresco.getImagePipeline();
        final DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline
                .fetchDecodedImage(imageRequest, this);
        final AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(draweeHolder.getController())
                .setAutoPlayAnimations(true)
                .setImageRequest(imageRequest)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        if (animatable != null) {
                            isAnimation = true;
                        }

                        CloseableReference<CloseableImage> imageCloseableReference = null;
                        try {
                            imageCloseableReference = dataSource.getResult();
                            if (imageCloseableReference != null) {
                                final CloseableImage image = imageCloseableReference.get();
                                if (image instanceof CloseableStaticBitmap) {
                                    CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
                                    final Bitmap bitmap = closeableStaticBitmap.getUnderlyingBitmap();
                                    if (bitmap != null) {
                                        if (waitForAnimEnd) {
                                            mBitmap = bitmap;
                                        } else {
                                            // 设置bitmap，为了能够zoom，pan
                                            setImageBitmap(bitmap);
                                        }
                                    }
                                }
                            }
                        } finally {
                            dataSource.close();
                            CloseableReference.closeSafely(imageCloseableReference);
                        }
                    }
                })
                .build();
        draweeHolder.setController(controller);
        setImageDrawable(draweeHolder.getTopLevelDrawable());
    }

    public void endAnim() {
        waitForAnimEnd = false;
        if (mBitmap != null) {
            setImageBitmap(mBitmap);
            mBitmap = null;
        }
    }

}
