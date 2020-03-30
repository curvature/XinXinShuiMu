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
import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.decoration.StickyHeaderAdapter;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.http.parser.PostParser;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;
import net.newsmth.dirac.widget.BubbleView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPageAdapter extends RecyclerView.Adapter<MainPageAdapter.ViewHolder>
        implements StickyHeaderAdapter<MainPageAdapter.ViewHolder> {

    private final List<MainPageItem> data = new ArrayList<>();
    private Activity context;
    private FrameLayout frameLayout;

    public MainPageAdapter(Activity context, FrameLayout frameLayout) {
        this.context = context;
        this.frameLayout = frameLayout;
    }

    public void setData(List<MainPageItem> data) {
        this.data.clear();
        if (data != null) {
            long id = 0;
            String text = null;
            for (MainPageItem item : data) {
                if (item.getId() == null) {
                    ++id;
                    text = item.getSubject();
                } else {
                    item.headerId = id;
                    item.headerText = text;
                    this.data.add(item);
                }
            }
        }
    }

    public int size() {
        return data.size();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainPageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_summary, parent, false);
        return new ViewHolder(v, 1);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.author.setText(data.get(position).getAuthor());
        holder.board.setText(data.get(position).getBoardChinese());
        holder.title.setText(data.get(position).getSubject());
        holder.tapListener.setPosition(position);
        holder.title.setTag(data.get(position));
        if (position + 1 < data.size() && data.get(position).headerId == data.get(position + 1).headerId) {
            holder.divider.setVisibility(View.VISIBLE);
        } else {
            holder.divider.setVisibility(View.GONE);
        }

        // bind时取消按下状态
        holder.itemView.setPressed(false);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getHeaderId(int position) {
        return data.get(position).headerId;
    }

    @Override
    public ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.section_header, parent, false), 0);
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder viewholder, int position) {
        viewholder.title.setText(data.get(position).headerText);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView author;
        public TextView board;
        public TextView title;
        public View divider;
        TapListener tapListener;

        ViewHolder(View v, int viewType) {
            super(v);
            if (viewType == 0) {
                title = (TextView) v;
            } else {
                author = v.findViewById(R.id.author);
                board = v.findViewById(R.id.board);
                title = v.findViewById(R.id.title);
                divider = v.findViewById(R.id.divider);
                tapListener = new TapListener();
                final GestureDetector gestureDetector = new GestureDetector(v.getContext(), tapListener);

                itemView.setOnTouchListener((v1, event) -> gestureDetector.onTouchEvent(event));
            }
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

            final MainPageItem postSummary = data.get(position);

            RetrofitUtils.create(ApiService.class)
                    .getThread(postSummary.getBoardEnglish(), postSummary.getId(), 1)
                    .subscribeOn(Schedulers.io())
                    .map(responseBody -> new PostParser(false).parseResponse(responseBody).data)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(posts -> {
                        int[] l = new int[2];
                        ViewUtils.getLocationInRootView(bubbleView, l);
                        ThreadActivity.startActivity(context,
                                postSummary.getBoardEnglish(),
                                postSummary.getId(),
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
