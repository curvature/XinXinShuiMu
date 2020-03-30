package net.newsmth.dirac.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.newsmth.dirac.R;
import net.newsmth.dirac.publish.PublishService;
import net.newsmth.dirac.util.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPostActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String EXTRA_BOARD = "a";
    @BindView(R.id.title)
    TextInputEditText mTitleView;
    @BindView(R.id.title_container)
    TextInputLayout mTitleContainer;
    @BindView(R.id.content)
    TextInputEditText mContentView;
    @BindView(R.id.content_container)
    TextInputLayout mContentContainer;
    private String mBoard;
    private Toolbar mActionBarToolbar;
    private View mRootView;

    public static void startActivity(Context context, String board) {
        context.startActivity(new Intent(context, EditPostActivity.class).putExtra(EXTRA_BOARD, board));
    }

    public static void navigateUpOrBack(Activity currentActivity,
                                        Class<? extends Activity> syntheticParentActivity) {
        // Retrieve parent activity from AndroidManifest.
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        // Synthesize the parent activity when a natural one doesn't exist.
        if (intent == null && syntheticParentActivity != null) {
            try {
                intent = NavUtils.getParentActivityIntent(currentActivity, syntheticParentActivity);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (intent == null) {
            // No parent defined in manifest. This indicates the activity may be used by
            // in multiple flows throughout the app and doesn't have a strict parent. In
            // this case the navigation up button should act in the same manner as the
            // back button. This will result in users being forwarded back to other
            // applications if currentActivity was invoked from another application.
            currentActivity.onBackPressed();
        } else if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
            // Need to synthesize a backstack since currentActivity was probably invoked by a
            // different app. The preserves the "Up" functionality within the app according to
            // the activity hierarchy defined in AndroidManifest.xml via parentActivity
            // attributes.
            TaskStackBuilder builder = TaskStackBuilder.create(currentActivity);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            // Navigate normally to the manifest defined "Up" activity.
            NavUtils.navigateUpTo(currentActivity, intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBoard = getIntent().getStringExtra(EXTRA_BOARD);

        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        mTitleView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(mTitleContainer.getError())) {
                    mTitleContainer.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getActionBarToolbar();
        getSupportActionBar().setTitle(R.string.create_new_post);

        mContentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(mContentContainer.getError())) {
                    mContentContainer.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        mActionBarToolbar.setNavigationOnClickListener(view -> navigateUpOrBack(EditPostActivity.this, null));

        doEnterAnim();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        menu.getItem(0).setIcon(ViewUtils.loadDrawableWithColor(this,
                R.drawable.ic_send_black_24dp,
                Color.WHITE));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send) {
            trySend();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void trySend() {
        if (validateData()) {
            PublishService.sendPost(this, mBoard, mTitleView.getText().toString(),
                    mContentView.getText().toString(), null);
            finish();
            overridePendingTransition(0, R.anim.down);
        }
    }

    private boolean validateData() {
        if (TextUtils.isEmpty(mTitleView.getText())) {
            mTitleContainer.setError(getString(R.string.title_empty));
            mTitleView.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(mContentView.getText())) {
            mContentContainer.setError(getString(R.string.content_empty));
            mContentView.requestFocus();
            return false;
        }
        return true;
    }

    protected Toolbar getActionBarToolbar() {
        mActionBarToolbar = findViewById(R.id.toolbar);
        mActionBarToolbar.setNavigationContentDescription(R.string.search_board);
        setSupportActionBar(mActionBarToolbar);
        return mActionBarToolbar;
    }

    @Override
    public void onBackPressed() {
        doExitAnim();
    }

    private void doEnterAnim() {
        mRootView = findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mRootView.getViewTreeObserver().removeOnPreDrawListener(this);
                        int revealRadius = mRootView.getHeight();
                        Animator show = ViewAnimationUtils.createCircularReveal(mRootView,
                                mRootView.getRight(), mRootView.getTop(), 0f, revealRadius)
                                .setDuration(250L);
                        show.setInterpolator(AnimationUtils.loadInterpolator(EditPostActivity.this,
                                android.R.interpolator.fast_out_slow_in));
                        show.start();
                        return false;
                    }
                });
    }

    private void doExitAnim() {
        int revealRadius = (int) Math.sqrt(Math.pow(mRootView.getWidth(), 2)
                + Math.pow(mRootView.getHeight(), 2));
        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(mRootView,
                mRootView.getRight(), mRootView.getTop(), revealRadius, 0f);
        shrink.setDuration(200L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(EditPostActivity.this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRootView.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(EditPostActivity.this);
            }
        });
        shrink.start();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
