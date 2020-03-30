package net.newsmth.dirac.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.ImagePagerActivity;
import net.newsmth.dirac.activity.PersonActivity;
import net.newsmth.dirac.audio.model.MusicProvider;
import net.newsmth.dirac.data.Post;
import net.newsmth.dirac.data.TextOrImage;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.http.parser.PostParser;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.EmoticonGetter;
import net.newsmth.dirac.util.EnhancedSpannableStringBuilder;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;
import net.newsmth.dirac.widget.LoadingView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.ViewHolder> implements View.OnClickListener {

    @NonNull
    private final Activity mContext;
    private final List<Post> mData;
    private final Set<String> mPostIdSet;
    private final List<Integer> mConnector = new ArrayList<>();
    private final int secondaryTextColor;
    private final int colorButtonNormal;
    public int totalPage;
    private int page;
    private String boardEnglish;
    private String id;
    private int mStartingPosition;
    private int mCount;
    private int mPositionHint;
    private int requestFlag; // 0未请求，1正在请求，2请求失败,3已到末尾,4论坛错误，可能是未登录错误
    private NewsmthException cause;
    private RecyclerView mView;

    public ThreadAdapter(Activity context, List<Post> data) {
        mContext = context;
        mData = data;
        mPostIdSet = new HashSet<>();
        if (data.size() > 0) {
            for (Post post : data) {
                mPostIdSet.add(post.id);
            }
            page = 1;
            totalPage = getTotalPage(data.get(0).totalPost);
        } else {
            page = 0;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        buildConnector();
        if (data.size() > 0 && data.size() < 10) {
            requestFlag = 3;
        }

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
        // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
        secondaryTextColor = ContextCompat.getColor(context,
                typedValue.resourceId != 0 ? typedValue.resourceId : typedValue.data);

        typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorButtonNormal, typedValue, true);
        // resourceId is used if it's a ColorStateList, and data if it's a color reference or a hex color
        colorButtonNormal = ContextCompat.getColor(context,
                typedValue.resourceId != 0 ? typedValue.resourceId : typedValue.data);
    }

    int getTotalPage(int totalPost) {
        return (totalPost + 9) / 10;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBoardEnglish(String boardEnglish) {
        this.boardEnglish = boardEnglish;
    }

    @Override
    public ThreadAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        final long packed = getPackedPosition(position);
        return getLayoutRes(getPackedPositionGroup(packed), getPackedPositionChild(packed));
    }

    public String getTitle() {
        if (mData.size() > 0) {
            return mData.get(0).title;
        }
        return null;
    }

    public boolean isNotLoadedPage(int pos) {
        int p = getPackedPositionGroup(getPackedPosition(pos));
        return p < 0 || mData.get(p).page > 0;
    }

    public Post getItem(int pos) {
        return mData.get(getPackedPositionGroup(getPackedPosition(pos)));
    }

    private int getLayoutRes(int groupPosition, int childPosition) {
        if (groupPosition == -1) {
            return R.layout.loading;
        }
        Post p = mData.get(groupPosition);
        if (p.page > 0) {
            if (childPosition == 0) {
                return R.layout.loading;
            } else {
                return R.layout.divider;
            }
        }

        if (childPosition == 0) {
            return R.layout.post_head;
        }
        childPosition -= 1;
        List<TextOrImage> contentList = p.contentList;
        if (childPosition < contentList.size()) {
            switch (contentList.get(childPosition).type) {
                case TextOrImage.TYPE_TEXT:
                case TextOrImage.TYPE_AUDIO:
                    return R.layout.post_text;
                case TextOrImage.TYPE_IMAGE:
                    return R.layout.post_image;
                default:
                    throw new IllegalArgumentException("Invalid view type");
            }
        }
        childPosition -= contentList.size();
        if (childPosition == 0 && p.ip == null) {
            return R.layout.divider;
        }
        if (childPosition == 0) {
            return R.layout.post_text;
        }
        if (childPosition == 1) {
            return R.layout.divider;
        }
        throw new RuntimeException("no layout");
    }

    public int getPackedPositionGroup(long packed) {
        return (int) (packed >> 32 & 0xFFFFFFFFL);
    }

    public int getPackedPositionChild(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    private long pack(int group, int child) {
        return (((long) group) << 32) | (child & 0xFFFFFFFFL);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        long packed = getPackedPosition(position);
        final int group = getPackedPositionGroup(packed);
        if (group == -1) {
            // is LoadingView
            switch (requestFlag) {
                case 0:
                    requestFlag = 1;
                    request(null);
                    // fall through
                case 1:
                    ((LoadingView) holder.itemView).showLoading();
                    return;
                case 2:
                    ((LoadingView) holder.itemView).showEmpty(R.string.loading_failed);
                    holder.itemView.setOnClickListener(this);
                    return;
                case 3:
                    ((LoadingView) holder.itemView).showEmpty(R.string.try_load_more);
                    holder.itemView.setOnClickListener(this);
                    return;
                case 4:
                    ((LoadingView) holder.itemView).showEmpty(cause.msg);
                    holder.itemView.setOnClickListener(this);
                    return;
                default:
                    return;
            }
        }
        final Post post = mData.get(group);
        int child = getPackedPositionChild(packed);
        if (post.page > 0) {
            if (child > 0) {
                return;
            }
            // view for one single page
            switch (post.requestFlag) {
                // -1为未请求并保持不请求
                case -1:
                    ((LoadingView) holder.itemView).showEmpty(mContext.getString(R.string.page_x, post.page));
                    holder.itemView.setOnClickListener(this);
                    holder.post = post;
                    holder.itemView.setTag(holder);
                    return;
                case 0:
                    post.requestFlag = 1;
                    request(post);
                    // fall through
                case 1:
                    holder.itemView.setOnClickListener(null);
                    ((LoadingView) holder.itemView).showLoading();
                    return;
                case 2:
                    ((LoadingView) holder.itemView).showEmpty(R.string.loading_failed);
                    holder.itemView.setOnClickListener(this);
                    holder.post = post;
                    holder.itemView.setTag(holder);
                    return;
                case 4:
                    ((LoadingView) holder.itemView).showEmpty(cause.msg);
                    holder.itemView.setOnClickListener(this);
                    holder.post = post;
                    holder.itemView.setTag(holder);
                    return;
                default:
                    return;
            }
        }

        if (child == 0) {
            final String avatarUrl = post.author.avatarUrl;

            if (TextUtils.isEmpty(avatarUrl)) {
                holder.imageView.setVisibility(View.INVISIBLE);
            } else {
                holder.imageView.setVisibility(View.VISIBLE);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setAutoPlayAnimations(true)
                        .setImageRequest(ImageRequestBuilder
                                .newBuilderWithSource(Uri.parse(avatarUrl))
                                .setRotationOptions(RotationOptions.autoRotate())
                                .build())
                        .setControllerListener(new BaseControllerListener<>())
                        .build();
                holder.imageView.setController(controller);
            }

            final String username = post.author.username;
            holder.textView1.setText(new EnhancedSpannableStringBuilder()
                    .append(username).append(" ")
                    .appendWithBackgroundColor(post.author.nickname, colorButtonNormal));
            holder.imageView.setOnClickListener(v -> {
                if ("deliver".equals(username) || "SYSOP".equals(username)) {
                    return;
                }
                holder.imageView.setTransitionName(username);
                PersonActivity.startActivity(mContext, username, avatarUrl, holder.imageView);
            });
            StringBuilder time = new StringBuilder();
            time.append(post.getFloorText());
            if (post.time != null) {
                time.append(" ").append(post.time);
            }
            holder.textView2.setText(time);
        } else {
            child -= 1;
            if (child < post.contentList.size()) {
                final TextOrImage textOrImage = post.contentList.get(child);
                switch (textOrImage.type) {
                    case TextOrImage.TYPE_TEXT:
                        if (!textOrImage.parsed) {
                            textOrImage.data = Html.fromHtml(textOrImage.data.toString(), new EmoticonGetter(), null);
                            textOrImage.parsed = true;
                        }
                        holder.textView1.setCompoundDrawables(null, null, null, null);
                        holder.textView1.setTextColor(secondaryTextColor);
                        holder.textView1.setText(textOrImage.data);
                        holder.textView1.setOnClickListener(null);
                        break;
                    case TextOrImage.TYPE_IMAGE:
                        String url = textOrImage.data.toString();
                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setAutoPlayAnimations(true)
                                .setImageRequest(ImageRequestBuilder
                                        .newBuilderWithSource(Uri.parse(url))
                                        .setRotationOptions(RotationOptions.autoRotate())
                                        .build())
                                .setControllerListener(new BaseControllerListener<>())
                                .build();
                        holder.draweeView.setController(controller);
                        holder.draweeView.getHierarchy().setPlaceholderImage(R.drawable.ic_image_black_24dp);
                        holder.draweeView.getHierarchy().setProgressBarImage(new ProgressBarDrawable());
                        for (mStartingPosition = 0; mStartingPosition < post.imageUrlArray.length; ++mStartingPosition) {
                            if (url.equals(post.imageUrlArray[mStartingPosition])) {
                                break;
                            }
                        }
                        final String name = group + " " + mStartingPosition;
                        final int indexInUrlArray = mStartingPosition;
                        holder.draweeView.setOnClickListener(v -> {
                            mStartingPosition = indexInUrlArray;
                            Intent intent = new Intent(mContext, ImagePagerActivity.class);
                            intent.putExtra(ImagePagerActivity.EXTRA_KEY_IMAGE, post.imageUrlArray);
                            intent.putExtra(ImagePagerActivity.EXTRA_KEY_STARTING_POSITION, indexInUrlArray);
                            intent.putExtra(ImagePagerActivity.EXTRA_KEY_GROUP, group);
                            mContext.startActivity(intent,
                                    ActivityOptions.makeSceneTransitionAnimation(mContext, holder.draweeView,
                                            name).toBundle());
                        });
                        holder.draweeView.setTransitionName(name);
                        holder.draweeView.setTag(name);
                        break;
                    case TextOrImage.TYPE_AUDIO:
                        holder.textView1.setText(textOrImage.data);
                        holder.textView1.setTextColor(ContextCompat.getColor(mContext, R.color.accent));
                        Drawable d = ViewUtils.loadDrawableWithColor(mContext,
                                R.drawable.music, ContextCompat.getColor(mContext, R.color.accent));
                        holder.textView1.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                        holder.textView1.setTag(textOrImage);
                        holder.textView1.setOnClickListener(this);
                        break;
                }
            } else if (child == post.contentList.size() && post.ip != null) {
                holder.textView1.setCompoundDrawables(null, null, null, null);
                holder.textView1.setTextColor(secondaryTextColor);
                if (TextUtils.isEmpty(post.ipLocation)) {
                    holder.textView1.setText(post.ip);
                    holder.textView1.setOnClickListener(null);
                } else {
                    holder.textView1.setText(post.ipLocation);
                    holder.textView1.setTag(post.ip);
                    holder.textView1.setOnClickListener(v -> {
                        final String alternative = (String) v.getTag();
                        v.setTag(holder.textView1.getText().toString());
                        holder.textView1.animate().rotationX(90).setDuration(200).withEndAction(() -> {
                            holder.textView1.setText(alternative);
                            holder.textView1.setRotationX(-90);
                            holder.textView1.animate().rotationX(0).setDuration(200);
                        });
                    });
                }
            }
        }
    }

    public int getStartingPosition() {
        return mStartingPosition;
    }

    private void appendData(List<Post> data) {
        --mCount;
        for (Post post : data) {
            if (mPostIdSet.contains(post.id)) {
                continue;
            }
            mCount += post.size();
            mConnector.add(mCount);
            mData.add(post);
            mPostIdSet.add(post.id);
        }
        ++mCount;
    }

    private void buildConnector() {
        mConnector.clear();
        mCount = 0;
        for (Post post : mData) {
            mCount += post.size();
            mConnector.add(mCount);
        }
        ++mCount;
    }

    private long getPackedPosition(int position) {
        if (mConnector.isEmpty()) {
            return pack(-1, 0);
        }
        int pos = mPositionHint;
        if (position < mConnector.get(pos)) {
            while (pos > 0 && position < mConnector.get(pos - 1)) {
                --pos;
            }
            mPositionHint = pos;
            if (pos == 0) {
                return pack(0, position);
            }
            return pack(pos, position - mConnector.get(pos - 1));
        } else if (position == mConnector.get(pos)) {
            if (pos == mConnector.size() - 1) {
                return pack(-1, 0);
            }
            mPositionHint = pos + 1;
            return pack(mPositionHint, 0);
        } else {
            do {
                if (pos + 1 < mConnector.size()) {
                    ++pos;
                } else {
                    mPositionHint = pos;
                    return pack(-1, 0);
                }
            } while (position >= mConnector.get(pos));
            mPositionHint = pos;
            return pack(pos, position - mConnector.get(pos - 1));
        }
    }

    @SuppressLint("CheckResult")
    private void request(final Post targetPost) {
        int target;
        if (targetPost == null) {
            target = page + 1;
        } else {
            target = targetPost.page;
        }
        RetrofitUtils.create(ApiService.class)
                .getThread(boardEnglish, id, target)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> new PostParser(true).parseResponse(responseBody).data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(posts -> {

                    if (posts.size() > 0) {
                        Post first = posts.get(0);
                        if (first.totalPost > 0) {
                            totalPage = getTotalPage(first.totalPost);
                        }
                    }

                    if (targetPost == null) {
                        boolean mayHasMore = posts.size() == 10;

                        if (mData.size() > 0) {
                            Post lastPost = mData.get(mData.size() - 1);
                            if (lastPost.page == 0) {
                                int last = lastPost.floor;
                                while (posts.size() > 0 && posts.get(0).floor <= last) {
                                    posts.remove(0);
                                }
                            }
                        }

                        if (posts.isEmpty()) {
                            // 说明返回了前一页内容，已到末尾。
                            requestFlag = 3;
                            notifyItemChanged(mCount - 1);
                        } else {
                            final int old = mCount;
                            appendData(posts);
                            notifyItemRangeInserted(old - 1, mCount - old);
                            if (mayHasMore) {
                                // 只有返回了10条才增加页码
                                ++page;
                                requestFlag = 0;
                            } else {
                                requestFlag = 3;
                            }
                            notifyItemChanged(mCount - 1);
                            if (old == 1) {
                                // 通过deep link跳入，第一次返回数据会scroll至末尾（猜是intended behavior），先强制定位到顶部
                                mView.getLayoutManager().scrollToPosition(0);
                            }
                        }
                    } else {
                        insertAt(mData.indexOf(targetPost), posts);
                    }
                }, throwable -> {
                    if (throwable instanceof NewsmthException) {
                        if (targetPost == null) {
                            requestFlag = 4;
                        } else {
                            targetPost.requestFlag = 4;

                        }
                        cause = (NewsmthException) throwable;
                    } else {
                        if (targetPost == null) {
                            requestFlag = 2;
                        } else {
                            targetPost.requestFlag = 2;
                        }
                    }


                    if (targetPost == null) {
                        notifyItemChanged(mCount - 1);
                    } else {
                        notifyItemChanged(mConnector.get(mData.indexOf(targetPost)) - 2);
                    }
                });
    }

    void insertAt(int pos, List<Post> data) {
        int remove = pos;
        mData.remove(pos);
        int start = mConnector.get(pos) - 2;
        int end = start;
        mConnector.remove(pos);

        for (Post post : data) {
            if (mPostIdSet.contains(post.id)) {
                continue;
            }
            end += post.size();
            mConnector.add(pos, end);
            mData.add(pos, post);
            mPostIdSet.add(post.id);
            ++pos;
        }
        end = end - start - 2;
        for (; pos < mConnector.size(); ++pos) {
            mConnector.set(pos, mConnector.get(pos) + end);
        }
        mCount += end;

        notifyItemRangeRemoved(remove, 2);
        notifyItemRangeInserted(remove, end + 2);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.post:
                TextOrImage item = (TextOrImage) v.getTag();
                MusicProvider.add(item);
                // FIXME NPE here
                MediaControllerCompat.getMediaController(mContext).getTransportControls()
                        .playFromMediaId(String.valueOf(item.hashCode()), null);
                break;
            default:
                v.setOnClickListener(null);
                ((LoadingView) v).showLoading();

                ViewHolder holder = (ViewHolder) v.getTag();
                if (holder != null) {
                    holder.post.requestFlag = 0;
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    requestFlag = 1;
                    request(null);
                }
                break;
        }
    }

    public int jumpTo(int dest) {
        if (dest == 1) {
            return 0;
        }
        for (int i = 0; i < mData.size(); i++) {
            Post post = mData.get(i);
            if (post.page > 0) {
                if (post.page == dest) {
                    post.requestFlag = 0;
                    return mConnector.get(i) - 1;
                }
            } else {
                if (post.floor / 10 + 1 == dest) {
                    return mConnector.get(i - 1);
                }
            }
        }
        // not found, create at the end

        int lastPage = 0;

        if (mData.size() > 0) {
            Post lastPost = mData.get(mData.size() - 1);
            if (lastPost.page > 0) {
                lastPage = lastPost.page;
            } else {
                lastPage = lastPost.floor / 10 + 1;
            }
        }

        --mCount;
        for (int i = lastPage + 1; i < dest; i++) {
            Post p = new Post();
            p.requestFlag = -1;
            p.page = i;
            mCount += 2;
            mConnector.add(mCount);
            mData.add(p);
        }
        ++mCount;
        page = dest - 1;
        return mCount - 1;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView imageView;
        SimpleDraweeView draweeView;
        TextView textView1;
        TextView textView2;
        Post post;

        ViewHolder(View v, int layoutRes) {
            super(v);
            switch (layoutRes) {
                case R.layout.post_head:
                    imageView = v.findViewById(R.id.avatar);
                    textView1 = v.findViewById(R.id.author);
                    textView2 = v.findViewById(R.id.title);
                    break;
                case R.layout.post_text:
                    textView1 = (TextView) v;
                    textView1.setGravity(Gravity.CENTER_VERTICAL);
                    textView1.setMovementMethod(LinkMovementMethod.getInstance());
                    break;
                case R.layout.post_image:
                    draweeView = (SimpleDraweeView) v;
                    break;
                default:
                    break;
            }
        }
    }
}
