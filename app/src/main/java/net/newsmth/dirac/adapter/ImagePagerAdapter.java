package net.newsmth.dirac.adapter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.ImagePagerActivity;
import net.newsmth.dirac.util.ViewUtils;
import net.newsmth.dirac.widget.FrescoPhotoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class ImagePagerAdapter extends PagerAdapter implements View.OnLongClickListener, View.OnClickListener {

    private final int mGroup;
    private final List<String> mData;
    public View currentView;
    public boolean waitForAnim;
    private ImagePagerActivity mActivity;
    private LayoutInflater mInflater;
    private LinkedList<View> scrapViews = new LinkedList<>();
    private int lastIndex = -1;
    private FrescoPhotoView heroView;
    private BottomSheetDialog mDialog;

    public ImagePagerAdapter(ImagePagerActivity context, List<String> data, int group, boolean waitForAnim) {
        mActivity = context;
        mGroup = group;
        mInflater = LayoutInflater.from(context);
        mData = data;
        this.waitForAnim = waitForAnim;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public boolean waitingForAnim() {
        if (waitForAnim) {
            return true;
        }
        if (heroView != null) {
            if (heroView.waitForAnimEnd) {
                return true;
            } else {
                heroView = null;
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        //必须返回POSITION_NONE，才会在调用pageAdapter的notifyDatasetChanged方法时调用getView方法
        //return POSITION_NONE;
        int position = POSITION_NONE;
        Object tag = null;
        if ((object instanceof View)) {
            tag = ((View) object).getTag(R.id.progress);
        }
        if (tag instanceof Integer) {
            position = (Integer) tag;
        }
        return position;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View convertView = null;
        if (!scrapViews.isEmpty()) {
            convertView = scrapViews.removeFirst();
        }
        View view = getView(convertView, position, container);
        container.addView(view);
        view.setTag(R.id.progress, position);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View) {
            View view = (View) object;
            container.removeView(view);
            scrapViews.add(view);
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (lastIndex == position || !(object instanceof View)) {
            return;
        }
        lastIndex = position;
        currentView = (View) object;
        ViewHolder holder = getCurrentHolder();
        mActivity.setCurrentView(holder.imageView);
    }

    private ViewHolder getCurrentHolder() {
        if (currentView == null) {
            return null;
        }
        ViewHolder holder = null;
        Object obj = currentView.getTag(R.id.image);
        if (obj instanceof ViewHolder) {
            holder = (ViewHolder) obj;
        }
        return holder;
    }

    private View getView(View convertView, int position, ViewGroup container) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.image_pager_item, container, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.image);
            holder.imageView.setOnLongClickListener(this);
            if (waitForAnim) {
                waitForAnim = false;
                heroView = holder.imageView;
                heroView.waitForAnimEnd = true;
            }
            holder.imageView.setMaximumScale(12f);
            convertView.setTag(R.id.image, holder);
            convertView.setTag(R.id.progress, position);
        } else {
            Object obj = convertView.getTag(R.id.image);
            if (obj instanceof ViewHolder) {
                holder = (ViewHolder) obj;
            }
        }

        holder.imageView.setImage(Uri.parse(mData.get(position)));
        final View view = holder.imageView;
        view.setTransitionName(mGroup + " " + position);
        view.post(new Runnable() {
            @Override
            public void run() {
                view.getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                view.getViewTreeObserver().removeOnPreDrawListener(this);
                                mActivity.startPostponedEnterTransition();
                                return true;
                            }
                        });
            }
        });
        return convertView;
    }

    @Override
    public boolean onLongClick(View view) {
        mDialog = new BottomSheetDialog(mActivity);
        mDialog.setContentView(R.layout.image_menu);
        TextView t = mDialog.findViewById(R.id.share);
        t.setCompoundDrawablesWithIntrinsicBounds(
                ViewUtils.loadDrawableWithColor(mActivity, R.drawable.ic_share_black_24dp,
                        ContextCompat.getColor(mActivity, R.color.bottom_sheet_text_color)),
                null,
                null,
                null);
        t.setOnClickListener(this);

        t = mDialog.findViewById(R.id.save);
        t.setCompoundDrawablesWithIntrinsicBounds(
                ViewUtils.loadDrawableWithColor(mActivity, R.drawable.ic_file_download_black_24dp,
                        ContextCompat.getColor(mActivity, R.color.bottom_sheet_text_color)),
                null,
                null,
                null);
        t.setOnClickListener(this);

        mDialog.show();
        return true;
    }

    @Override
    public void onClick(View view) {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        switch (view.getId()) {
            case R.id.share:
                File localFile = getLocalFile();
                if (localFile == null) {
                    Toast.makeText(mActivity, R.string.load_file_failed, Toast.LENGTH_LONG).show();
                } else {
                    Uri uri = FileProvider.getUriForFile(mActivity, "net.newsmth.dirac.fileprovider",
                            getLocalFile());
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType(getCurrentHolder().imageView.isAnimation ? "image/gif" : "image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mActivity.startActivity(intent);
                }
                break;
            case R.id.save:
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    saveImageToGallery();
                } else {
                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
                break;
        }
    }

    private File getLocalFile() {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(mData.get(lastIndex)), "FrescoUtils");
        if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
            BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
            return ((FileBinaryResource) resource).getFile();
        } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
            BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
            return ((FileBinaryResource) resource).getFile();
        }
        return null;
    }


    public void saveImageToGallery() {
        File localFile = getLocalFile();
        if (localFile == null) {
            Toast.makeText(mActivity, R.string.load_file_failed, Toast.LENGTH_LONG).show();
        } else {
            File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    // 文件名里不能有冒号，在某些手机上文件名不能含冒号
                    new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.US).format(new Date())
                            + "_from_"
                            + mActivity.getString(R.string.app_name)
                            + (getCurrentHolder().imageView.isAnimation ? ".gif" : ".jpg")
            );

            try (Source src = Okio.source(localFile);
                 BufferedSink dst = Okio.buffer(Okio.sink(destFile))) {
                dst.writeAll(src);

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(destFile));
                mActivity.sendBroadcast(mediaScanIntent);
                Toast.makeText(mActivity, R.string.saved, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(mActivity, R.string.save_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class ViewHolder {
        FrescoPhotoView imageView;
    }
}
