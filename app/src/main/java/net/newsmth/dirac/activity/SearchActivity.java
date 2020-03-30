package net.newsmth.dirac.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import net.newsmth.dirac.R;
import net.newsmth.dirac.provider.NewsmthContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    @BindView(R.id.search_view)
    SearchView mSearchView;
    @BindView(R.id.search_results)
    ListView mSearchResults;
    @BindView(R.id.scrim)
    View scrim;
    private String mQuery = "";
    private SimpleCursorAdapter mResultsAdapter;
    private Toolbar mActionBarToolbar;

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
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        mSearchView = findViewById(R.id.search_view);
        setupSearchView();
        mResultsAdapter = new SimpleCursorAdapter(this,
                R.layout.list_item_search_result, null,
                new String[]{NewsmthContract.Boards.BOARD_HIGHLIGHT},
                new int[]{R.id.search_result}, 0);
        mSearchResults.setAdapter(mResultsAdapter);
        mSearchResults.setOnItemClickListener(this);
        getActionBarToolbar();

        mActionBarToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        mActionBarToolbar.setNavigationOnClickListener(view -> navigateUpOrBack(SearchActivity.this, null));

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        mSearchView.setQuery(query, false);

        doEnterAnim();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchFor(query);
                mSearchView.setQuery(query, false);
            }
        }
    }

    protected Toolbar getActionBarToolbar() {
        mActionBarToolbar = findViewById(R.id.toolbar);
        mActionBarToolbar.setNavigationContentDescription(R.string.search_board);
        setSupportActionBar(mActionBarToolbar);
        return mActionBarToolbar;
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);
        mSearchView.setQueryHint(getString(R.string.search_board));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchFor(s);
                return true;
            }
        });
        mSearchView.setOnCloseListener(() -> {
            dismiss(null);
            return false;
        });
        if (!TextUtils.isEmpty(mQuery)) {
            mSearchView.setQuery(mQuery, false);
        }
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    public void dismiss(View view) {
        doExitAnim();
    }

    private void doEnterAnim() {
        // Fade in a background scrim as this is a floating window. We could have used a
        // translucent window background but this approach allows us to turn off window animation &
        // overlap the fade with the reveal animation – making it feel snappier.
        View scrim = findViewById(R.id.scrim);
        scrim.animate()
                .alpha(1f)
                .setDuration(500L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in))
                .start();

        // Next perform the circular reveal on the search panel
        final View searchPanel = findViewById(R.id.search_panel);
        if (searchPanel != null) {
            // We use a view tree observer to set this up once the view is measured & laid out
            searchPanel.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            searchPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                            // As the height will change once the initial suggestions are delivered by the
                            // loader, we can't use the search panels height to calculate the final radius
                            // so we fall back to it's parent to be safe
                            int revealRadius = ((ViewGroup) searchPanel.getParent()).getHeight();
                            // Center the animation on the top right of the panel i.e. near to the
                            // search button which launched this screen.
                            Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                                    searchPanel.getRight(), searchPanel.getTop(), 0f, revealRadius);
                            show.setDuration(250L);
                            show.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                                    android.R.interpolator.fast_out_slow_in));
                            show.start();
                            return false;
                        }
                    });
        }
    }

    private void doExitAnim() {
        final View searchPanel = findViewById(R.id.search_panel);
        // Center the animation on the top right of the panel i.e. near to the search button which
        // launched this screen. The starting radius therefore is the diagonal distance from the top
        // right to the bottom left
        int revealRadius = (int) Math.sqrt(Math.pow(searchPanel.getWidth(), 2)
                + Math.pow(searchPanel.getHeight(), 2));
        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel,
                searchPanel.getRight(), searchPanel.getTop(), revealRadius, 0f);
        shrink.setDuration(200L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(SearchActivity.this);
            }
        });
        shrink.start();

        // We also animate out the translucent background at the same time.
        scrim
                .animate()
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(SearchActivity.this,
                                android.R.interpolator.fast_out_slow_in))
                .start();
    }

    private void searchFor(String query) {
        Bundle args = new Bundle(1);
        if (query == null) {
            query = "";
        }
        args.putString(SearchManager.QUERY, query);
        if (TextUtils.equals(query, mQuery)) {
            getLoaderManager().initLoader(0, args, this);
        } else {
            getLoaderManager().restartLoader(0, args, this);
        }
        mQuery = query;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String arg = "%" + args.getString(SearchManager.QUERY) + "%";
        return new CursorLoader(this,
                NewsmthContract.Boards.CONTENT_URI,
                NewsmthContract.Boards.DEFAULT_PROJECTION,
                NewsmthContract.Boards.BOARD_NAME_CHINESE + " LIKE ? OR "
                        + NewsmthContract.Boards.BOARD_NAME_ENGLISH + " LIKE ?",
                new String[]{arg, arg}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            mResultsAdapter.swapCursor(data);
            mSearchResults.setVisibility(data.getCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mResultsAdapter.getCursor();
        c.moveToPosition(position);
        BoardActivity.startActivity(this, c.getString(1), c.getString(2));
    }
}
