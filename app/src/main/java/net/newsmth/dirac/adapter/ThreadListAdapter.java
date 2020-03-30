package net.newsmth.dirac.adapter;

import android.app.Activity;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.ThreadActivity;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.http.parser.BoardSearchParser;
import net.newsmth.dirac.http.parser.PostParser;
import net.newsmth.dirac.http.parser.ThreadParser;
import net.newsmth.dirac.model.ThreadSummary;
import net.newsmth.dirac.search.Query;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.EnhancedSpannableStringBuilder;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;
import net.newsmth.dirac.widget.BubbleView;
import net.newsmth.dirac.widget.LoadingView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ThreadListAdapter extends RecyclerView.Adapter<ThreadListAdapter.AbsViewHolder> {

    private final Activity context;
    private final Set<String> idSet;
    private final Query mQuery;
    private String board;
    private List<ThreadSummary> data;
    private FrameLayout frameLayout;
    private int page;
    private int requestFlag; // 0未请求，1正在请求，2请求失败，3论坛错误，可能是未登录,4已到末尾
    private NewsmthException cause;
    private Disposable mD1;
    private Disposable mD2;

    public ThreadListAdapter(Activity context, FrameLayout frameLayout, Query query) {
        this.context = context;
        this.frameLayout = frameLayout;
        idSet = new HashSet<>();
        mQuery = query;
    }

    public void setData(List<ThreadSummary> data) {
        this.data = data;
        idSet.clear();
        for (ThreadSummary t : data) {
            idSet.add(t.id);
        }
        page = data.size() > 0 ? 1 : 0;
    }

    public void clear() {
        data = null;
        notifyDataSetChanged();
        if (mD1 != null) {
            mD1.dispose();
        }
        if (mD2 != null) {
            mD2.dispose();
        }
    }

    public void setBoard(String board) {
        this.board = board;
    }

    @Override
    public ThreadListAdapter.AbsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.thread_summary, parent, false));
            case -1:
                return new LoadingViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.loading, parent, false));
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size() ? 0 : -1;
    }

    @Override
    public void onBindViewHolder(AbsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size() + 1;
    }

    private void request() {
        requestFlag = 1;
        Observable<ResponseBody> o = mQuery == null ?
                RetrofitUtils.create(ApiService.class)
                        .getThreadSummary(board, page + 1) :
                RetrofitUtils.create(ApiService.class)
                        .searchArticle(
                                mQuery.title,
                                mQuery.author,
                                mQuery.gilded ? "on" : null,
                                mQuery.att ? "on" : null,
                                mQuery.board,
                                page + 1);
        o = o.subscribeOn(Schedulers.io());

        mD1 = o
                .map(mQuery == null ?
                        responseBody -> new ThreadParser().parseResponse(responseBody).data :
                        responseBody -> new BoardSearchParser().parseResponse(responseBody).data
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(threadSummaries -> {
                    int count = 0;
                    for (ThreadSummary t : threadSummaries) {
                        if (idSet.contains(t.id)) {
                            continue;
                        }
                        data.add(t);
                        idSet.add(t.id);
                        ++count;
                    }
                    ++page;
                    if (count > 0) {
                        notifyItemRangeInserted(getItemCount() - 1, count);
                        requestFlag = 0;
                    } else {
                        requestFlag = 4;
                        notifyItemChanged(getItemCount() - 1);
                    }
                    notifyItemChanged(getItemCount() - 1);
                }, throwable -> {
                    if (throwable instanceof NewsmthException) {
                        requestFlag = 3;
                        cause = (NewsmthException) throwable;
                    } else {
                        requestFlag = 2;
                    }
                    notifyItemChanged(getItemCount() - 1);
                });
    }

    abstract static class AbsViewHolder extends RecyclerView.ViewHolder {


        AbsViewHolder(View itemView) {
            super(itemView);
        }

        abstract void bind(int position);
    }

    class ViewHolder extends AbsViewHolder {
        public TextView author;
        public TextView time;
        public TextView title;
        TapListener tapListener;

        ViewHolder(View v) {
            super(v);
            author = v.findViewById(R.id.author);
            time = v.findViewById(R.id.time);
            title = v.findViewById(R.id.title);
            tapListener = new TapListener();
            final GestureDetector gestureDetector = new GestureDetector(v.getContext(), tapListener);
            v.setOnTouchListener((v1, event) -> gestureDetector.onTouchEvent(event));
        }

        @Override
        void bind(int position) {
            // bind时取消按下状态
            itemView.setPressed(false);

            ThreadSummary summary = data.get(position);

            author.setText(summary.author);

            if (summary.isTop) {
                if (summary.hasAttachment) {
                    time.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_attach_file_black_24dp,
                            0, R.drawable.ic_vertical_align_top_black_24dp, 0);
                } else {
                    time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.ic_vertical_align_top_black_24dp, 0);
                }
            } else {
                if (summary.hasAttachment) {
                    time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_attach_file_black_24dp, 0);
                } else {
                    time.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            EnhancedSpannableStringBuilder builder = new EnhancedSpannableStringBuilder(summary.subject);
//      builder.appendWithColor("(", Color.DKGRAY);
//      builder.appendWithColor(String.valueOf(summary.count), Color.DKGRAY);
//      builder.appendWithColor(")", Color.DKGRAY);

            builder.append("(");
            builder.append(String.valueOf(summary.count));
            builder.append(")");
            title.setText(builder);
            tapListener.setPosition(position);
            title.setTag(data.get(position));
        }
    }

    class LoadingViewHolder extends AbsViewHolder implements View.OnClickListener {

        final LoadingView loadingView;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            loadingView = (LoadingView) itemView;
        }

        @Override
        void bind(int position) {
            switch (requestFlag) {
                case 0:
                    request();
                    // fall through
                case 1:
                    loadingView.showLoading();
                    break;
                case 2:
                    loadingView.showEmpty(R.string.loading_failed);
                    loadingView.setOnClickListener(this);
                    break;
                case 3:
                    loadingView.showEmpty(cause.msg);
                    loadingView.setOnClickListener(this);
                    break;
                case 4:
                    if (mQuery == null) {
                        loadingView.showEmpty(R.string.try_load_more);
                    } else {
                        loadingView.showEmpty(loadingView.getResources()
                                .getString(R.string.try_load_more_search_result, data.size()));
                    }
                    loadingView.setOnClickListener(this);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            loadingView.setOnClickListener(null);
            loadingView.showLoading();
            request();
        }
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {

        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final BubbleView bubbleView = new BubbleView(context);
            final int size = context.getResources().getDimensionPixelSize(R.dimen.bubble_size);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);

            bubbleView.setLayoutParams(lp);
            int[] coordinates = new int[2];
            frameLayout.getLocationOnScreen(coordinates);

            bubbleView.setTranslationX(e.getRawX() - coordinates[0] - size / 2);
            bubbleView.setTranslationY(e.getRawY() - coordinates[1] - size / 2);

            frameLayout.addView(bubbleView);

            final ThreadSummary threadSummary = data.get(position);

            mD2 = RetrofitUtils.create(ApiService.class)
                    .getThread(board, threadSummary.id, 1)
                    .subscribeOn(Schedulers.io())
                    .map(responseBody -> new PostParser(false).parseResponse(responseBody).data)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(posts -> {
                        int[] l = new int[2];
                        ViewUtils.getLocationInRootView(bubbleView, l);
                        ThreadActivity.startActivity(context,
                                board,
                                threadSummary.id,
                                posts,
                                l[0] + ((int) bubbleView.getX()) + size / 2,
                                l[1] + ((int) bubbleView.getY()) + size / 2);
                        frameLayout.removeView(bubbleView);
                    }, throwable -> {
                        bubbleView.dismiss();

                        // 失败的情况下，调用notify以取消按下状态，需要判断下标是否还在范围内
                        if (position < getItemCount()) {
                            notifyItemChanged(position);
                        }

                        if (throwable instanceof NewsmthException) {
                            ((NewsmthException) throwable).alert(context, frameLayout, Snackbar.LENGTH_LONG);
                        } else {
                            Snackbar.make(frameLayout, R.string.load_article_failed, Snackbar.LENGTH_LONG).show();
                        }
                    });

            return true;
        }
    }
}
