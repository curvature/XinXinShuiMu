package net.newsmth.dirac.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.material.navigation.NavigationView;

import net.newsmth.dirac.R;
import net.newsmth.dirac.data.User;
import net.newsmth.dirac.favorite.FavoriteItem;
import net.newsmth.dirac.favorite.FavoriteManager;
import net.newsmth.dirac.fragment.MainPageFragment;
import net.newsmth.dirac.fragment.SettingsFragment;
import net.newsmth.dirac.user.UserManager;

public class MainActivity extends net.newsmth.dirac.audio.ui.BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private NavigationView mMenuView;
    private CharSequence mTitle;
    private String[] mPlanetTitles;
    private TextView mLoginView;
    private SimpleDraweeView mAvatarView;
    private TextView mUsernameView;
    private BroadcastReceiver mReceiver;
    private int mPosition;

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setOnClickListener(this);

        mTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.mode_array);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mMenuView = findViewById(R.id.menu);

        View header = getLayoutInflater().inflate(R.layout.drawer_header, mMenuView, false);
        mLoginView = header.findViewById(R.id.login);
        mLoginView.setOnClickListener(v -> LoginActivity.startActivity(MainActivity.this));
        mAvatarView = header.findViewById(R.id.avatar);
        mAvatarView.setOnClickListener(this);
        mUsernameView = header.findViewById(R.id.username);
        mUsernameView.setOnClickListener(this);

        setMenuHeader(UserManager.getInstance().needLogin());

        mMenuView.addHeaderView(header);
        mMenuView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                toolbar,
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        } else {
            selectItem(savedInstanceState.getInt("pos"));
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setMenuHeader(UserManager.getInstance().needLogin());
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter(UserManager.USER_ACTION));
    }

    private void invalidateNavigationMenu() {
        Menu menu = mMenuView.getMenu();
        menu.clear();
        mMenuView.inflateMenu(R.menu.drawer);
        mMenuView.getMenu().getItem(mPosition).setChecked(true);
        for (FavoriteItem item : FavoriteManager.getInstance().getFavoriteItems()) {
            MenuItem menuItem = menu.add(Menu.NONE, item.id, Menu.NONE, item.boardChinese);
            menuItem.setCheckable(false);
            menuItem.setIcon(R.drawable.ic_favorite_24dp);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateNavigationMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos", mPosition);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_page:
                item.setChecked(true);
                selectItem(0);
                break;
            case R.id.settings:
                item.setChecked(true);
                selectItem(1);
                break;
            default:
                FavoriteItem favoriteItem = FavoriteManager.getInstance().find(item.getItemId());
                if (favoriteItem != null) {
                    BoardActivity.startActivity(this, favoriteItem.board, favoriteItem.boardChinese);
                    mDrawerLayout.closeDrawers();
                }
                break;
        }
        return true;
    }

    public void setMenuHeader(boolean needLogin) {
        if (needLogin) {
            mLoginView.setVisibility(View.VISIBLE);
            mAvatarView.setVisibility(View.GONE);
            mUsernameView.setVisibility(View.GONE);
        } else {
            User user = UserManager.getInstance().getUser();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setAutoPlayAnimations(true)
                    .setImageRequest(ImageRequestBuilder
                            .newBuilderWithSource(Uri.parse(user.avatarUrl))
                            .setRotationOptions(RotationOptions.autoRotate())
                            .build())
                    .setControllerListener(new BaseControllerListener<>())
                    .build();
            mAvatarView.setController(controller);

            mUsernameView.setText(user.username);
            mAvatarView.setVisibility(View.VISIBLE);
            mUsernameView.setVisibility(View.VISIBLE);
            mLoginView.setVisibility(View.GONE);
        }
    }

    private void selectItem(int position) {
        mCurrentFragment = getFragment(position);
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.content_frame, mCurrentFragment)
                .commit();

        // update selected item and title, then close the drawer
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawers();
        mPosition = position;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private Fragment getFragment(int position) {
        switch (position) {
            case 1:
                return new SettingsFragment();
            default:
                return new MainPageFragment();
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar:
                // fall through
            case R.id.username:
                mAvatarView.setTransitionName(UserManager.getInstance().getUser().username);
                PersonActivity.startActivity(this, null, null, mAvatarView);
                break;
            case R.id.toolbar:
                if (mPosition == 0) {
                    ((MainPageFragment) mCurrentFragment).onToolbarClicked();
                }
                break;
            default:
                break;
        }
    }
}