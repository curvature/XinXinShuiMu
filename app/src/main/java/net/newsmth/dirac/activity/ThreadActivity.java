package net.newsmth.dirac.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.SharedElementCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.PageNavAdapter;
import net.newsmth.dirac.adapter.ThreadAdapter;
import net.newsmth.dirac.data.Post;
import net.newsmth.dirac.decoration.InsetDecoration;
import net.newsmth.dirac.publish.PublishService;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.user.UserManager;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;
import net.newsmth.dirac.widget.SpreadMenuView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class ThreadActivity extends net.newsmth.dirac.audio.ui.BaseActivity implements SpreadMenuView.SpreadMenuCallback, View.OnClickListener {

    private static final String EXTRA_BOARD = "a";
    private static final String EXTRA_ID = "b";
    private static final String EXTRA_POSTS = "c";
    private static final String EXTRA_X = "d";
    private static final String EXTRA_Y = "e";

    static CharSequence sDraft;
    FrameLayout frameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    ThreadAdapter adapter;
    int mCurrentPosition = -1;
    int mGroup;
    int revealRadius;
    View toolbar;
    View playContainer;
    InsetDecoration decoration;
    ImageView pager;
    View back;
    TextView titleView;
    SpreadMenuView spreadMenuView;
    int slop;
    int bottomInset;
    boolean isNight;
    RecyclerView pagerMenu;
    PageNavAdapter pagerAdapter;
    View backLayer;
    private String board;
    private int x;
    private int y;
    private int mReplyPos;

    public static void startActivity(Context context, String board, String id,
                                     ArrayList<Post> posts, int x, int y) {
        context.startActivity(new Intent(context, ThreadActivity.class)
                .putExtra(EXTRA_BOARD, board)
                .putExtra(EXTRA_ID, id)
                .putParcelableArrayListExtra(EXTRA_POSTS, posts)
                .putExtra(EXTRA_X, x).putExtra(EXTRA_Y, y));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        slop = ViewConfiguration.get(this).getScaledTouchSlop();
        Uri uri = getIntent().getData();
        if (uri == null) {
            getWindow().setSharedElementReenterTransition(null);

            setExitSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (mCurrentPosition >= 0 && adapter.getStartingPosition() != mCurrentPosition) {
                        names.clear();
                        sharedElements.clear();
                        View sharedElement = recyclerView.findViewWithTag(mGroup + " " + mCurrentPosition);
                        if (sharedElement != null) {
                            names.add(sharedElement.getTransitionName());
                            sharedElements.put(sharedElement.getTransitionName(), sharedElement);
                        }
                    }
                    mCurrentPosition = -1;
                }
            });

            setContentView(R.layout.activity_thread);

            initViews(getIntent().getParcelableArrayListExtra(EXTRA_POSTS));
            adapter.setId(getIntent().getStringExtra(EXTRA_ID));

            x = getIntent().getIntExtra(EXTRA_X, 0);
            y = getIntent().getIntExtra(EXTRA_Y, 0);

            frameLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    frameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                    revealRadius = (int) Math.sqrt(Math.pow(frameLayout.getWidth(), 2)
                            + Math.pow(frameLayout.getHeight(), 2));
                    Animator show = ViewAnimationUtils.createCircularReveal(frameLayout, x, y, 0, revealRadius)
                            .setDuration(250L);
                    show.setInterpolator(AnimationUtils.loadInterpolator(ThreadActivity.this,
                            android.R.interpolator.fast_out_slow_in));
                    show.start();
                    return false;
                }
            });

            overridePendingTransition(0, 0);
        } else {
            String host = uri.getHost();
            String path = uri.getPath();
            String pid = null;
            if ("www.newsmth.net".equals(host)) {
                if ("/bbstcon.php".equals(path)) {
                    board = uri.getQueryParameter("board");
                    pid = uri.getQueryParameter("gid");
                } else if (path != null && path.startsWith("/nForum/article/")) {
                    List<String> segments = uri.getPathSegments();
                    if (segments.size() == 4) {
                        board = segments.get(2);
                        pid = segments.get(3);
                    }
                }
            } else if ("m.newsmth.net".equals(host)) {
                if (path != null && path.startsWith("/article/")) {
                    List<String> segments = uri.getPathSegments();
                    if (segments.size() == 3) {
                        board = segments.get(1);
                        pid = segments.get(2);
                    }
                }
            }
            if (TextUtils.isEmpty(board) || TextUtils.isEmpty(pid)) {
                finish();
                return;
            }
            setContentView(R.layout.activity_thread);

            initViews(new ArrayList<>());
            adapter.setId(pid);
        }
        mReplyPos = -1;
        lightStatus(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    void initViews(List<Post> data) {
        playContainer = findViewById(R.id.controls_container);
        toolbar = findViewById(R.id.toolbar);
        titleView = toolbar.findViewById(R.id.title);
        if (data.size() > 0) {
            titleView.setText(toHtml(data.get(0).title));
        }
        back = toolbar.findViewById(R.id.back);
        back.setOnClickListener(this);
        pager = toolbar.findViewById(R.id.pager);
        pager.setOnClickListener(this);

        spreadMenuView = findViewById(R.id.spread);
        spreadMenuView.setMenuCallback(this);

        frameLayout = findViewById(android.R.id.content);

        recyclerView = findViewById(R.id.recycler);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int y = recyclerView.computeVerticalScrollOffset();
                if (y <= slop) {
                    toolbar.setVisibility(View.VISIBLE);
                    lightStatus(false);
                } else {
                    if (dy < 0 && dy < -slop / 4) {
                        toolbar.setVisibility(View.VISIBLE);
                        lightStatus(false);
                    } else if (dy > 0 && dy > slop / 4) {
                        toolbar.setVisibility(View.INVISIBLE);
                        if (isNight) {
                            lightStatus(false);
                        } else {
                            lightStatus(true);
                        }
                    }
                }
            }
        });
        decoration = new InsetDecoration(this, getResources().getDimensionPixelSize(R.dimen.action));
        recyclerView.addItemDecoration(decoration);
        adapter = new ThreadAdapter(this, data);
        board = getIntent().getStringExtra(EXTRA_BOARD);
        adapter.setBoardEnglish(board);
        recyclerView.setAdapter(adapter);

        findViewById(android.R.id.content).setOnApplyWindowInsetsListener((v, insets) -> {
            toolbar.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            bottomInset = insets.getSystemWindowInsetBottom();
            decoration.applyInset(insets.getSystemWindowInsetTop(), bottomInset);
            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) pager.getLayoutParams();
            lp.setMarginEnd(insets.getSystemWindowInsetRight());
            pager.setLayoutParams(lp);

            lp = (ConstraintLayout.LayoutParams) playContainer.getLayoutParams();
            lp.bottomMargin = bottomInset;
            playContainer.setLayoutParams(lp);
            return insets;
        });
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mCurrentPosition = data.getIntExtra(ImagePagerActivity.KEY_CUR, 0);
        mGroup = data.getIntExtra(ImagePagerActivity.KEY_GROUP, 0);
    }

    @Override
    public void onBackPressed() {
        if (pagerMenu != null && pagerMenu.isShown()) {
            hidePagerMenu();
            return;
        }
        doExitAnim();
    }

    private void doExitAnim() {
        Animator shrink = ViewAnimationUtils.createCircularReveal(frameLayout,
                x, y, revealRadius, 0f).setDuration(250L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frameLayout.setVisibility(View.INVISIBLE);
                finishAfterTransition();
                overridePendingTransition(0, 0);
            }
        });
        shrink.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK && mReplyPos >= 0) {
                reply(mReplyPos);
                mReplyPos = -1;
            }
        }
    }

    @Override
    public void onMenuItemSelected(int menuPos, int pos) {
        switch (menuPos) {
            case 0:
                if (UserManager.getInstance().needLogin()) {
                    LoginActivity.startActivity(this, getString(R.string.login_before_reply));
                    mReplyPos = pos;
                } else {
                    reply(pos);
                }
                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, adapter.getItem(pos).getShareUrl(board));
                intent.setType("text/plain");
                startActivity(intent);
                break;
            case 2:
                deletePost(null, null);
                break;
            default:
                break;
        }
    }

    private void deletePost(String board, String id) {
        RetrofitUtils.create(ApiService.class).deletePost(board, id)
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void reply(int pos) {
        final Post p = adapter.getItem(pos);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.reply_post);
        dialog.setOnShowListener(d -> {
            final EditText input = dialog.findViewById(R.id.reply);
            final ImageView send = dialog.findViewById(R.id.send);
            input.setHint(getString(R.string.reply_who, p.author.username));
            input.setText(sDraft);
            input.setSelection(input.getText().length());
            send.setEnabled(input.getText().length() > 0);
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    send.setEnabled(s.length() > 0);
                    sDraft = s;
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            dialog.findViewById(R.id.send).setOnClickListener(v -> {
                if (input.getText().length() > 0) {
                    String replyTitle = p.title;
                    if (replyTitle == null) {
                        replyTitle = "Re: ";
                    } else if (!replyTitle.startsWith("Re: ")) {
                        replyTitle = "Re: " + replyTitle;
                    }
                    sDraft = null;
                    PublishService.sendPost(ThreadActivity.this, board, replyTitle,
                            input.getText().toString() + p.replyQuote, p.id);
                    dialog.dismiss();
                }
            });
            ViewUtils.showKeyboard(input);
        });
        dialog.show();
    }

    void lightStatus(boolean flag) {


        View v = getWindow().getDecorView();
        if (flag) {
            v.setSystemUiVisibility(v.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            v.setSystemUiVisibility(v.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pager:
                if (pagerMenu == null) {
                    pagerMenu = findViewById(R.id.menu);
                    backLayer = findViewById(R.id.back_layer);
                    backLayer.setOnTouchListener((v1, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            hidePagerMenu();
                            v1.setVisibility(View.INVISIBLE);
                        }
                        return true;
                    });
                    ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) pagerMenu.getLayoutParams();
                    lp.matchConstraintMaxHeight = backLayer.getMeasuredHeight() - bottomInset;
                    lp.constrainedHeight = true;
                    pagerMenu.setLayoutParams(lp);
                    pagerAdapter = new PageNavAdapter(this);
                    pagerMenu.setLayoutManager(new LinearLayoutManager(this));
                    pagerMenu.setAdapter(pagerAdapter);
                }
                if (pagerMenu.isShown()) {
                    hidePagerMenu();
                } else {
                    pager.setImageResource(R.drawable.close);
                    back.setVisibility(View.INVISIBLE);
                    titleView.setText(R.string.go2page);
                    backLayer.setVisibility(View.VISIBLE);
                    // unreliable, comment out
//          int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
//          pagerAdapter.update(adapter.totalPage, adapter.getCurrentPage(pos));
                    pagerAdapter.update(adapter.totalPage, 0);
                    recyclerView.setEnabled(false);
                    spreadMenuView.setEnabled(false);
                    pagerMenu.setVisibility(View.VISIBLE);
                    pagerMenu.setTranslationY(-backLayer.getMeasuredHeight());
                    pagerMenu.animate()
                            .setInterpolator(new DecelerateInterpolator())
                            .setDuration(200)
                            .translationY(0).setListener(null);
                }
                break;
            case R.id.back:
                onBackPressed();
                break;
        }
    }

    void hidePagerMenu() {
        back.setVisibility(View.VISIBLE);
        titleView.setText(toHtml(adapter.getTitle()));
        pager.setImageResource(R.drawable.page);
        pagerMenu.animate()
                .setDuration(200)
                .translationYBy(-pagerMenu.getMeasuredHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pagerMenu.setVisibility(View.INVISIBLE);
                animation.removeAllListeners();
                recyclerView.setEnabled(true);
                spreadMenuView.setEnabled(true);
            }
        });
        pagerMenu.animate()
                .setDuration(200)
                .translationYBy(-pagerMenu.getMeasuredHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pagerMenu.setVisibility(View.INVISIBLE);
                animation.removeAllListeners();
                recyclerView.setEnabled(true);
                spreadMenuView.setEnabled(true);
            }
        });
    }

    CharSequence toHtml(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Html.fromHtml(s);
    }

    public void jumpTo(int dest) {
        hidePagerMenu();
        layoutManager.scrollToPositionWithOffset(adapter.jumpTo(dest), toolbar.getMeasuredHeight());
    }
}