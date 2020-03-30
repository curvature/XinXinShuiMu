package net.newsmth.dirac.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.customview.widget.ViewDragHelper;
import androidx.viewpager.widget.ViewPager;

import com.facebook.drawee.drawable.ScalingUtils;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.ImagePagerAdapter;
import net.newsmth.dirac.util.PhotoViewTransition;
import net.newsmth.dirac.widget.FrescoPhotoView;
import net.newsmth.dirac.widget.SwipeView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImagePagerActivity extends BaseActivity {

    public static final String EXTRA_KEY_IMAGE = "img";
    public static final String EXTRA_KEY_STARTING_POSITION = "start";
    public static final String EXTRA_KEY_GROUP = "g";
    public static final String KEY_CUR = "cur";
    public static final String KEY_GROUP = "gro";
    @BindView(R.id.swipe)
    SwipeView mSwipeView;
    @BindView(R.id.pager)
    ViewPager mPager;
    private boolean mIsReturning;
    private int mStartingPosition;
    private FrescoPhotoView mCurrentView;
    private ImagePagerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSharedElementEnterTransition(PhotoViewTransition.createTransitionSet(
                ScalingUtils.ScaleType.CENTER_CROP,
                ScalingUtils.ScaleType.FIT_CENTER));

        getWindow().setSharedElementReturnTransition(PhotoViewTransition.createTransitionSet(
                ScalingUtils.ScaleType.FIT_CENTER,
                ScalingUtils.ScaleType.CENTER_CROP));

        postponeEnterTransition();

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (mIsReturning) {
                    int currentPosition = mPager.getCurrentItem();
                    if (mStartingPosition != currentPosition) {
                        names.clear();
                        names.add(mCurrentView.getTransitionName());
                        sharedElements.clear();
                        sharedElements.put(mCurrentView.getTransitionName(), mCurrentView);
                    }
                }
            }
        });

        setContentView(R.layout.activity_image_pager);
        ButterKnife.bind(this);
        mSwipeView.setDragEdge(ViewDragHelper.EDGE_LEFT | ViewDragHelper.EDGE_TOP);

        mPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_gap));

        final String[] imageUrlArray = getIntent().getExtras().getStringArray(EXTRA_KEY_IMAGE);
        mAdapter = new ImagePagerAdapter(this,
                Arrays.asList(imageUrlArray),
                getIntent().getIntExtra(EXTRA_KEY_GROUP, 0),
                /**
                 * 旋转屏幕重新创建的Activity没有共享动画
                 */
                savedInstanceState == null);
        mPager.setAdapter(mAdapter);
        mStartingPosition = getIntent().getIntExtra(EXTRA_KEY_STARTING_POSITION, 0);
        if (savedInstanceState == null) {
            mPager.setCurrentItem(mStartingPosition);
        } else {
            mPager.setCurrentItem(savedInstanceState.getInt(KEY_CUR));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mAdapter.saveImageToGallery();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CUR, mPager.getCurrentItem());
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent()
                .putExtra(KEY_CUR, mPager.getCurrentItem())
                .putExtra(KEY_GROUP, getIntent().getIntExtra(EXTRA_KEY_GROUP, 0));
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    public void setCurrentView(FrescoPhotoView view) {
        mCurrentView = view;
    }

}
