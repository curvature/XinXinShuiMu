package net.newsmth.dirac.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.FrameLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.ThreadListAdapter;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.http.parser.BoardSearchParser;
import net.newsmth.dirac.search.Query;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.EnhancedSpannableStringBuilder;
import net.newsmth.dirac.util.RetrofitUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BoardSearchActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    ThreadListAdapter adapter;
    private Query query;
    private Disposable mD;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        FrameLayout frameLayout = findViewById(R.id.content_frame);

        swipeRefreshLayout = findViewById(R.id.swipe);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        query = getIntent().getParcelableExtra("q");

        adapter = new ThreadListAdapter(this, frameLayout, query);
        adapter.setBoard(query.board);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        setTitle();

        getThreadList();
    }

    private void setTitle() {
        EnhancedSpannableStringBuilder t = new EnhancedSpannableStringBuilder("搜索：");
        if (!TextUtils.isEmpty(query.title)) {
            t.append(query.title);
        }
        if (!TextUtils.isEmpty(query.author)) {
            if (t.length() > 3) {
                t.appendWithColor(" + ", ContextCompat.getColor(this, R.color.accent));
            }
            t.append(query.author);
        }
        setTitle(t);
    }

    private void getThreadList() {
        mD = RetrofitUtils.create(ApiService.class)
                .searchArticle(
                        query.title,
                        query.author,
                        query.gilded ? "on" : null,
                        query.att ? "on" : null,
                        query.board,
                        1)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> new BoardSearchParser().parseResponse(responseBody).data)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(threadSummaries -> {
                    adapter.setData(threadSummaries);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    if (mSnackbar != null && mSnackbar.isShown()) {
                        mSnackbar.dismiss();
                    }
                    mD = null;
                }, throwable -> {
                    if (throwable instanceof NewsmthException) {
                        mSnackbar = ((NewsmthException) throwable).alert(this,
                                swipeRefreshLayout, Snackbar.LENGTH_INDEFINITE);
                    } else {
                        Snackbar.make(swipeRefreshLayout, R.string.failed_to_refresh, Snackbar.LENGTH_LONG).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    mD = null;
                });
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onRefresh() {
        if (mD == null) {
            getThreadList();
        }
    }

}