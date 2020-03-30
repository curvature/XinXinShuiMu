package net.newsmth.dirac.adapter;

import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import net.newsmth.dirac.R;
import net.newsmth.dirac.model.MessageSummary;
import net.newsmth.dirac.widget.BubbleView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    private Context context;
    private int count;
    private List<MessageSummary> data;
    private FrameLayout frameLayout;
    private Call threadCall;

    private SimpleDateFormat format;

    public MessageListAdapter(Context context, FrameLayout frameLayout) {
        this.context = context;
        this.frameLayout = frameLayout;
        format = new SimpleDateFormat(context.getString(R.string.message_time_format), Locale.US);
    }

    public void setData(List<MessageSummary> data) {
        this.data = data;
    }

    public void setCount(int count) {
        this.count = count;
    }


    @Override
    public MessageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == -1 ? R.layout.loading : R.layout.thread_summary, parent, false);
        return new ViewHolder(v, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return position < data.size() ? 0 : -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) >= 0) {
            MessageSummary summary = data.get(position);
            holder.board.setText(summary.getSender());
            holder.author.setText(format.format(new Date(summary.getTime() * 1000)));
            holder.title.setText(summary.getSubject());
            holder.tapListener.setPosition(position);
            holder.title.setTag(data.get(position));
            if (position == data.size() - 1 && data.size() < count) {
                request();
            }
        }
    }


    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size() + 1;
    }


    private void request() {
        if (threadCall == null) {
//      threadCall = HttpHelper.newCall(new MessageRequest(count - data.size() - 20));
//      threadCall.enqueue(new JsonCallback<MessageResponse>() {
//
//        @Override
//        public void onSuccess(MessageResponse messageResponse, Response response, Call call) {
//          threadCall = null;
//          Collections.reverse(messageResponse.getMessages());
//          data.addAll(messageResponse.getMessages());
//          notifyDataSetChanged();
//        }
//
//        @Override
//        public void onFailure(Exception e, Call call) {
//          threadCall = null;
//        }
//      });
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView author;
        public TextView board;
        public TextView title;
        public TapListener tapListener;

        public ViewHolder(View v, int type) {
            super(v);
            if (type == -1) {
                return;
            }

            author = v.findViewById(R.id.author);
            board = v.findViewById(R.id.board);
            title = v.findViewById(R.id.title);
            title.setClickable(true);
            tapListener = new TapListener();
            final GestureDetector gestureDetector = new GestureDetector(v.getContext(), tapListener);
            title.setOnTouchListener((v1, event) -> gestureDetector.onTouchEvent(event));
        }
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {

        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
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

            final MessageSummary messageSummary = data.get(position);


            return true;
        }
    }
}
