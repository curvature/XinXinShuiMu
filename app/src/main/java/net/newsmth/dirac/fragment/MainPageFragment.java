package net.newsmth.dirac.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.SearchActivity;
import net.newsmth.dirac.adapter.MainPageAdapter;
import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.data.source.MainPageLoader;
import net.newsmth.dirac.data.source.MainPageRepository;
import net.newsmth.dirac.data.source.local.MainPageLocalDataSource;
import net.newsmth.dirac.data.source.remote.MainPageRemoteDataSource;
import net.newsmth.dirac.data.source.remote.MainPageRemoteDataSource2;
import net.newsmth.dirac.decoration.StickyHeaderDecoration;
import net.newsmth.dirac.http.response.TypedResponse;

import java.util.List;

public class MainPageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<TypedResponse<List<MainPageItem>>> {

    private FrameLayout frameLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MainPageAdapter adapter;
    private View rootView;
    private StickyHeaderDecoration decoration;
    private boolean firstLoadComplete;

    private MainPageLoader loader;
    private MainPageRepository repository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_top_ten, container, false);
            frameLayout = rootView.findViewById(R.id.root);
            swipeRefreshLayout = rootView.findViewById(R.id.swipe);
            recyclerView = rootView.findViewById(R.id.recycler);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            adapter = new MainPageAdapter(getActivity(), frameLayout);
            recyclerView.setAdapter(adapter);

            swipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.post(() -> {
                if (!firstLoadComplete) {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });

            repository = MainPageRepository.getInstance(
                    MainPageLocalDataSource.getInstance(getActivity()),
                    MainPageRemoteDataSource.getInstance(getActivity()),
                    MainPageRemoteDataSource2.getInstance(getActivity()));

            loader = new MainPageLoader(getActivity().getApplicationContext(), repository);

            getLoaderManager().initLoader(0, null, this);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (decoration == null) {
            decoration = new StickyHeaderDecoration(adapter);
            recyclerView.addItemDecoration(decoration);
        } else {
            decoration.clearHeaderCache();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.post(() -> repository.refreshMainPageItems());
    }

    @Override
    public Loader<TypedResponse<List<MainPageItem>>> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<TypedResponse<List<MainPageItem>>> loader,
                               TypedResponse<List<MainPageItem>> data) {
        firstLoadComplete = true;
        swipeRefreshLayout.setRefreshing(false);
        final int oldSize = adapter.size();
        adapter.setData(data.data);
        if (adapter.size() == 0) {
            adapter.notifyItemRangeInserted(0, adapter.size());
        } else if (adapter.size() == oldSize) {
            adapter.notifyItemRangeChanged(0, adapter.size());
        } else {
            adapter.notifyDataSetChanged();
        }
        if (data.error > 0) {
            showHintOnFailure();
        }
    }

    private void showHintOnFailure() {
        Snackbar
                .make(swipeRefreshLayout, R.string.failed_to_refresh, Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, v -> swipeRefreshLayout.setRefreshing(true))
                .show();
    }

    @Override
    public void onLoaderReset(Loader<TypedResponse<List<MainPageItem>>> loader) {

    }

    public void onToolbarClicked() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }
}