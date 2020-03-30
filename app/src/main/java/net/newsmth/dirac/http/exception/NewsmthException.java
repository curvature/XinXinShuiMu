package net.newsmth.dirac.http.exception;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.LoginActivity;

import java.io.IOException;

/**
 * Created by cameoh on 23/11/2017.
 */

public class NewsmthException extends IOException {

    public final String msg;

    public NewsmthException(String msg) {
        if (TextUtils.isEmpty(msg)) {
            this.msg = Dirac.obtain().getString(R.string.failed);
        } else {
            this.msg = msg;
        }
    }

    /**
     * 是否是未登录错误
     *
     * @return
     */
    public boolean isNotLogin() {
        return msg.startsWith("您未登录");
    }

    public Snackbar alert(Activity activity, View view, int duration) {
        Snackbar snackbar = Snackbar.make(view, msg, duration);
        if (isNotLogin()) {
            snackbar.setAction(R.string.login, it -> LoginActivity.startActivity(activity));
        }
        snackbar.show();
        return snackbar;
    }
}
