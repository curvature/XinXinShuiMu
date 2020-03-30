package net.newsmth.dirac.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.text.Html;

import androidx.core.content.ContextCompat;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;

public class EmoticonGetter implements Html.ImageGetter {

    @Override
    public Drawable getDrawable(String source) {
        final LevelListDrawable drawable = new LevelListDrawable();
        Drawable empty = ContextCompat.getDrawable(Dirac.obtain(),
                R.drawable.ic_insert_emoticon_black_24dp);
        drawable.addLevel(0, 0, empty);
        drawable.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(RetrofitUtils.getScheme() + "//www.newsmth.net" + source))
                .setImageDecodeOptions(ImageDecodeOptions.newBuilder().setForceStaticImage(true).build()) // for GIF
                .build();

        Fresco.getImagePipeline().fetchDecodedImage(imageRequest, this)
                .subscribe(new BaseBitmapDataSubscriber() {

                    @Override
                    public void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap == null) {
                            return;
                        }
                        Bitmap bmp = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        if (bmp == null) {
                            return;
                        }
                        Drawable d2 = new BitmapDrawable(Dirac.obtain().getResources(), bmp);
                        drawable.addLevel(1, 1, d2);
                        drawable.setBounds(0, 0, ViewUtils.dp2px(bmp.getWidth()), ViewUtils.dp2px(bmp.getHeight()));
                        drawable.setLevel(1);
                        drawable.invalidateSelf();
                    }

                    @Override
                    public void onFailureImpl(DataSource dataSource) {
                    }
                }, UiThreadImmediateExecutorService.getInstance());

        return drawable;
    }
}
