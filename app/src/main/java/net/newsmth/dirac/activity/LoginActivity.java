package net.newsmth.dirac.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.newsmth.dirac.R;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.user.UserManager;
import net.newsmth.dirac.util.RetrofitUtils;
import net.newsmth.dirac.util.ViewUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends BaseActivity {

    public static final int REQUEST_CODE = 1;
    private static final String EXTRA_HINT = "a";
    @BindView(R.id.login)
    Button mLoginButton;
    @BindView(R.id.username)
    TextInputEditText mUsernameView;
    @BindView(R.id.username_container)
    TextInputLayout mUsernameContainer;
    @BindView(R.id.password)
    TextInputEditText mPasswordView;
    @BindView(R.id.password_container)
    TextInputLayout mPasswordContainer;
    private String mUsername;
    private String mPassword;

    public static void startActivity(Activity activity) {
        startActivity(activity, null);
    }

    public static void startActivity(Activity activity, String hint) {
        activity.startActivityForResult(new Intent(activity, LoginActivity.class)
                .putExtra(EXTRA_HINT, hint), REQUEST_CODE);
        activity.overridePendingTransition(R.anim.bottom_up, R.anim.hold);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mUsernameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(mUsernameContainer.getError())) {
                    mUsernameContainer.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(mPasswordContainer.getError())) {
                    mPasswordContainer.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String hint = getIntent().getStringExtra(EXTRA_HINT);
        if (!TextUtils.isEmpty(hint)) {
            Snackbar.make(mLoginButton, hint, Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.login)
    void tryLogin() {
        if (validate()) {
            mUsername = mUsernameView.getText().toString();
            mPassword = mPasswordView.getText().toString();

            RetrofitUtils.create(ApiService.class)
                    .login(mUsername, mPassword)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        try {
                            JSONObject blob = new JSONObject(s);
                            if (blob.optInt("ajax_st") == 1) {
                                UserManager.getInstance().save(blob, mPassword);
                                UserManager.getInstance().start(false);
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                String msg = blob.optString("ajax_msg");
                                ViewUtils.hideKeyboard(LoginActivity.this);
                                if (TextUtils.isEmpty(msg)) {
                                    Snackbar.make(mLoginButton, R.string.login_failed, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(mLoginButton, msg, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            onloginFail(e);
                        }
                    }, this::onloginFail);
        }
    }

    private void onloginFail(Throwable t) {
        ViewUtils.hideKeyboard(LoginActivity.this);
        Snackbar.make(mLoginButton, R.string.login_failed, Snackbar.LENGTH_LONG).show();
    }

    private boolean validate() {
        if (TextUtils.isEmpty(mUsernameView.getText())) {
            mUsernameContainer.setError(getString(R.string.username_empty));
            mUsernameView.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(mPasswordView.getText())) {
            mPasswordContainer.setError(getString(R.string.password_empty));
            mPasswordView.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.down);
    }
}