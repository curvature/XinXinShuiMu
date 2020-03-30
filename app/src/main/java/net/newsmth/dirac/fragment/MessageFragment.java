package net.newsmth.dirac.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.MessageListAdapter;
import net.newsmth.dirac.widget.DividerItemDecoration;

import okhttp3.Call;

public class MessageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private FrameLayout frameLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MessageListAdapter adapter;

    private Call ongoingCall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top_ten, container, false);
        frameLayout = rootView.findViewById(R.id.root);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe);
        recyclerView = rootView.findViewById(R.id.recycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new MessageListAdapter(getActivity(), frameLayout);
        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        return rootView;
    }

    private void getMessages() {
        if (ongoingCall == null) {
//      ongoingCall = HttpHelper.newCall(new MessageCountRequest());
//      ongoingCall.enqueue(new JsonCallback<MessageCountResponse>() {
//        @Override
//        public void onSuccess(final MessageCountResponse messageCountResponse, Response response, Call call) {
//          int count = messageCountResponse.getCount();
//          adapter.setCount(count);
//          HttpHelper.newCall(new MessageRequest(count - 20))
//              .enqueue(new JsonCallback<MessageResponse>() {
//
//                @Override
//                public void onSuccess(MessageResponse messageResponse, Response response, Call call) {
//                  ongoingCall = null;
//                  Collections.reverse(messageResponse.getMessages());
//                  adapter.setData(messageResponse.getMessages());
//                  adapter.notifyDataSetChanged();
//                  swipeRefreshLayout.setRefreshing(false);
//                }
//
//                @Override
//                public void onFailure(Exception e, Call call) {
//                  ongoingCall = null;
//                }
//              });
//        }
//
//        @Override
//        public void onFailure(Exception e, Call call) {
//
//        }
//      });
        }
    }

    @Override
    public void onRefresh() {
        getMessages();
    }
}