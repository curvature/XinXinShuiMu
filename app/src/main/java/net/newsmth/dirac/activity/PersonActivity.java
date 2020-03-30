package net.newsmth.dirac.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import net.newsmth.dirac.R;
import net.newsmth.dirac.data.User;
import net.newsmth.dirac.ip.IpQueryHelper;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.user.UserManager;
import net.newsmth.dirac.util.RetrofitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PersonActivity extends BaseActivity {

    private static final String EXTRA_USERNAME = "a";
    private static final String EXTRA_AVATAR_URL = "b";

    @BindView(R.id.avatar)
    SimpleDraweeView avatarView;
    @BindView(R.id.username)
    TextView mUsernameView;
    @BindView(R.id.nickname)
    TextView mNicknameView;
    @BindView(R.id.identity)
    TextView mIdentitiView;
    @BindView(R.id.post_total)
    TextView mPostTotalView;
    @BindView(R.id.login_total)
    TextView mLoginTotalView;
    @BindView(R.id.level)
    TextView mLevelView;
    @BindView(R.id.points)
    TextView mPointsView;
    @BindView(R.id.last_login)
    TextView mLastLoginView;
    @BindView(R.id.last_ip)
    TextView mLastIpView;
    @BindView(R.id.status_now)
    TextView mStatusNowView;
    private Disposable mCall;

    public static void startActivity(Activity activity, String username, String avatarUrl, View sharedView) {
        activity.startActivity(new Intent(activity, PersonActivity.class)
                        .putExtra(EXTRA_USERNAME, username)
                        .putExtra(EXTRA_AVATAR_URL, avatarUrl),
                ActivityOptions.makeSceneTransitionAnimation(activity, sharedView,
                        sharedView.getTransitionName()).toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);

        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        String avatarUrl = getIntent().getStringExtra(EXTRA_AVATAR_URL);
        if (username == null) {
            username = UserManager.getInstance().getUser().username;
            avatarUrl = UserManager.getInstance().getUser().avatarUrl;
        }

        //avatarView.setTransitionName(username);

        mUsernameView.setText(username);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(getString(R.string.details));

        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setAutoPlayAnimations(true)
                .setImageRequest(ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(avatarUrl))
                        .setRotationOptions(RotationOptions.autoRotate())
                        .build())
                .setControllerListener(new BaseControllerListener<>())
                .build();

        avatarView.setController(controller);

        mCall = RetrofitUtils.create(ApiService.class).getUserInfo(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    try {
                        User user = User.parse(new JSONObject(s));
                        mNicknameView.setText(user.nickname);
                        mIdentitiView.setText(user.identity);
                        mPostTotalView.setText(user.postTotal);
                        mLoginTotalView.setText(user.loginTotal);
                        mLevelView.setText(user.level);
                        mPointsView.setText(user.points);
                        mLastIpView.setText(user.lastLoginIp);
                        if (IpQueryHelper.getInstance().isEnabled()) {
                            final IpQueryHelper.IPZone loc = IpQueryHelper.getInstance().queryIp(user.lastLoginIp);
                            if (loc != null) {
                                mLastIpView.setTag(loc.toString());
                                mLastIpView.setOnClickListener(v -> {
                                    final String alternative = (String) v.getTag();
                                    v.setTag(mLastIpView.getText().toString());
                                    mLastIpView.animate().rotationX(90).setDuration(200).withEndAction(() -> {
                                        mLastIpView.setText(alternative);
                                        mLastIpView.setRotationX(-90);
                                        mLastIpView.animate().rotationX(0).setDuration(200);
                                    });
                                });
                            }
                        }
                        mLastLoginView.setText(user.lastLoginTime);
                        mStatusNowView.setText(user.status);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getStringExtra(EXTRA_USERNAME) == null) {
            getMenuInflater().inflate(R.menu.person, menu);
            menu.getItem(0).getIcon().setTint(Color.WHITE);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                UserManager.getInstance().logout();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mCall != null) {
            mCall.dispose();
        }
        super.onDestroy();
    }
}