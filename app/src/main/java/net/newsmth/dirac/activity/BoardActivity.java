package net.newsmth.dirac.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.ThreadListAdapter;
import net.newsmth.dirac.favorite.FavoriteItem;
import net.newsmth.dirac.favorite.FavoriteManager;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.http.parser.ThreadParser;
import net.newsmth.dirac.search.Query;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.user.UserManager;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BoardActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_BOARD = "board";
    public static final String EXTRA_BOARD_CHINESE = "c";
    SwipeRefreshLayout swipeRefreshLayout;
    ThreadListAdapter adapter;
    private String board;
    private String boardChinese;
    private Disposable mD;
    private Snackbar mSnackbar;

    public static void startActivity(Context context, String board, String boardChinese) {
        context.startActivity(new Intent(context, BoardActivity.class)
                .putExtra(EXTRA_BOARD, board)
                .putExtra(EXTRA_BOARD_CHINESE, boardChinese));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        parseIntent(getIntent());
        if (TextUtils.isEmpty(board)) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        FrameLayout frameLayout = findViewById(R.id.content_frame);

        swipeRefreshLayout = findViewById(R.id.swipe);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ThreadListAdapter(this, frameLayout, null);
        adapter.setBoard(board);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        setTitle();

        getThreadList();
    }

    private void setTitle() {
        if (TextUtils.isEmpty(boardChinese)) {
            setTitle(board);
        } else {
            setTitle(boardChinese + "/" + board);
        }
    }

    private void parseIntent(Intent intent) {
        Uri data = intent.getData();
        if (data == null) {
            board = intent.getStringExtra(EXTRA_BOARD);
            boardChinese = intent.getStringExtra(EXTRA_BOARD_CHINESE);
        } else {
            String host = data.getHost();
            String path = data.getPath();
            if ("www.newsmth.net".equals(host)) {
                if ("/bbsdoc.php".equals(path)) {
                    board = data.getQueryParameter(EXTRA_BOARD);
                } else if (path != null && path.startsWith("/nForum/board/")) {
                    board = path.substring(14);
                }
            } else if ("m.newsmth.net".equals(host)) {
                if (path != null && path.startsWith("/board/")) {
                    board = path.substring(7);
                }
            }
            boardChinese = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.board, menu);
        if (boardChinese == null) {
            // 从deep link跳入，没有中文版名，不显示收藏按钮
            menu.removeItem(R.id.favorite);
        } else {
            if (FavoriteManager.getInstance().contains(board)) {
                menu.getItem(1).setIcon(R.drawable.ic_favorite_24dp);
            } else {
                menu.getItem(1).setIcon(ViewUtils.loadDrawableWithColor(this,
                        R.drawable.ic_favorite_24dp,
                        Color.WHITE));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:
                if (FavoriteManager.getInstance().neverUsedFavorite()) {
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage(R.string.favorite_hint)
                            .setPositiveButton(R.string.ok, null).show();
                }
                if (FavoriteManager.getInstance().contains(board)) {
                    item.setIcon(ViewUtils.loadDrawableWithColor(this,
                            R.drawable.ic_favorite_24dp,
                            Color.WHITE));
                    FavoriteManager.getInstance().remove(new FavoriteItem(board, boardChinese));
                } else if (FavoriteManager.getInstance().getFavoriteItems().size() >= 6) {
                    Snackbar.make(swipeRefreshLayout, R.string.too_many_favorite, Snackbar.LENGTH_LONG).show();
                } else {
                    item.setIcon(R.drawable.ic_favorite_24dp);
                    FavoriteManager.getInstance().add(new FavoriteItem(board, boardChinese));
                }
                break;
            case R.id.create:
                if (UserManager.getInstance().needLogin()) {
                    LoginActivity.startActivity(this, getString(R.string.login_before_post));
                } else {
                    EditPostActivity.startActivity(this, board);
                }
                return true;
            case R.id.search:
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(R.layout.search_article)
                        .setPositiveButton(R.string.search, (dialogInterface, i) -> {

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.setOnShowListener(dialogInterface -> {
                    final EditText titleView = dialog.findViewById(R.id.title);
                    final EditText authorView = dialog.findViewById(R.id.author);
                    final CheckBox gilded = dialog.findViewById(R.id.gilded);
                    final CheckBox att = dialog.findViewById(R.id.att);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setOnClickListener(view -> {
                                if (titleView.getText().length() == 0 && authorView.getText().length() == 0) {
                                    Toast.makeText(BoardActivity.this, R.string.at_least_one, Toast.LENGTH_SHORT).show();
                                } else {
                                    startActivity(new Intent(BoardActivity.this, BoardSearchActivity.class)
                                            .putExtra("q", new Query(
                                                    board,
                                                    titleView.getText().toString(),
                                                    authorView.getText().toString(),
                                                    gilded.isChecked(),
                                                    att.isChecked()
                                            )));
                                    dialogInterface.dismiss();
                                }
                            });
                });
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        parseIntent(intent);

        setTitle();
        invalidateOptionsMenu();

        adapter.clear();
        adapter.setBoard(board);

        if (mD != null) {
            mD.dispose();
            mD = null;
        }
        if (mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
        getThreadList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                EditPostActivity.startActivity(this, board);
            }
        }
    }

    private void getThreadList() {
        mD = RetrofitUtils.create(ApiService.class)
                .getThreadSummary(board, 1)
                .subscribeOn(Schedulers.io())
                .map(responseBody -> new ThreadParser().parseResponse(responseBody).data)
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